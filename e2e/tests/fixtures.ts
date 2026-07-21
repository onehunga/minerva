import { expect, test } from "@playwright/test";
import { hash } from "bcryptjs";
import { Client } from "pg";

const INITIAL_ADMIN_PASSWORD = process.env.INITIAL_ADMIN_PASSWORD ?? "e2e-password";

const databaseUrl = process.env.DATABASE_URL;

if (!databaseUrl) {
	throw new Error("DATABASE_URL is required");
}

async function waitForInitialMigration(): Promise<void> {
	await expect(async () => {
		const client = new Client({ connectionString: databaseUrl });
		try {
			await client.connect();
			const result = await client.query<{ ready: boolean }>(
				"SELECT EXISTS (SELECT 1 FROM users WHERE username = 'admin') AS ready",
			);
			expect(result.rows[0]?.ready).toBe(true);
		} finally {
			await client.end().catch(() => undefined);
		}
	}).toPass({ timeout: 60_000 });
}

/**
 * Setzt alle Datenbanktabellen zurück und erstellt einen Admin-Benutzer mit dem Passwort "e2e-password".
 */
async function resetDatabase(): Promise<void> {
	await waitForInitialMigration();

	const password = await hash(INITIAL_ADMIN_PASSWORD, 10);
	const client = new Client({ connectionString: databaseUrl });
	await client.connect();

	try {
		await client.query("BEGIN");
		await client.query(`
			DO $$
			DECLARE
				table_list text;
			BEGIN
				SELECT string_agg(format('%I.%I', schemaname, tablename), ', ')
				INTO table_list
				FROM pg_tables
				WHERE schemaname = 'public'
					AND lower(tablename) NOT IN ('databasechangelog', 'databasechangeloglock');

				IF table_list IS NOT NULL THEN
					EXECUTE format('TRUNCATE TABLE %s RESTART IDENTITY CASCADE', table_list);
				END IF;
			END
			$$;

			DO $$
			DECLARE
				sequence_record record;
			BEGIN
				FOR sequence_record IN
					SELECT schemaname, sequencename
					FROM pg_sequences
					WHERE schemaname = 'public'
				LOOP
					EXECUTE format(
						'ALTER SEQUENCE %I.%I RESTART WITH 1',
						sequence_record.schemaname,
						sequence_record.sequencename
					);
				END LOOP;
			END
			$$;
		`);

		await client.query("INSERT INTO workspace_roles (name) VALUES ('ADMIN'), ('USER')");
		await client.query(
			`INSERT INTO users (username, password, workspace_role_id)
			 SELECT 'admin', $1, id FROM workspace_roles WHERE name = 'ADMIN'`,
			[password],
		);
		await client.query("COMMIT");
	} catch (error) {
		await client.query("ROLLBACK");
		throw error;
	} finally {
		await client.end();
	}
}

test.beforeEach(resetDatabase);

export { expect, test };
