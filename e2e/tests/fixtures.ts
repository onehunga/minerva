import { expect, test as baseTest, type APIRequestContext, type Page } from "@playwright/test";
import { Client } from "pg";

const INITIAL_ADMIN_PASSWORD = "e2e-password";
const INITIAL_ADMIN_USERNAME = "admin";
// Fester Testhash für "e2e-password", erzeugt mit Spring BCryptPasswordEncoder.
const INITIAL_ADMIN_PASSWORD_HASH = "$2a$10$G.Q9z7KLTUFLfYHCao7Fiex.zGWpt3gpR6GDHfB4gzjGaW8aCdcvC";

// Diese Typen bilden nur die API-Daten ab, welche die E2E-Helfer tatsächlich benötigen.
type Tokens = { accessToken: string; refreshToken: string };
type TicketPriority = "LOWEST" | "LOW" | "NORMAL" | "HIGH" | "HIGHEST";
type ProjectInput = {
	name: string;
	description: string;
	ticketConfiguration: {
		tickets: Array<{
			name: string;
			description: string;
			states: Array<{ name: string; category: "OPEN" | "IN_PROGRESS" | "COMPLETED" }>;
			transitions: Array<{ name: string; from: string | null; to: string }>;
			children: string[];
		}>;
	};
};
type TicketInput = {
	name: string;
	description: string;
	priority: TicketPriority;
	assignedTo: number | null;
};
type TicketResponse = TicketInput & { id: number; projectId: number };

// Tests erhalten diese Methoden über die `api`-Fixture und müssen keine IDs oder SQL kennen.
type ApiFixture = {
	createProject(input?: Partial<Pick<ProjectInput, "name" | "description">>): Promise<number>;
	createTicket(projectId: number, input?: Partial<TicketInput>): Promise<TicketResponse>;
};

const databaseUrl = process.env.DATABASE_URL;

if (!databaseUrl) {
	throw new Error("DATABASE_URL is required");
}

async function waitForInitialMigration(): Promise<void> {
	// Playwright wiederholt die Abfrage, bis Liquibase die erste Anwendungstabelle angelegt hat.
	await expect(async () => {
		const client = new Client({ connectionString: databaseUrl });
		try {
			await client.connect();
			const result = await client.query<{ ready: boolean }>(
				"SELECT to_regclass('public.users') IS NOT NULL AS ready",
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

	const client = new Client({ connectionString: databaseUrl });
	await client.connect();

	try {
		// Der gesamte Reset ist atomar, damit kein Test einen teilweise geleerten Zustand sieht.
		await client.query("BEGIN");
		// Liquibase-Tabellen bleiben erhalten; alle fachlichen Daten und Sequenzen starten neu.
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

		// Jeder Test beginnt mit genau den Rollen und dem Admin, die er zur Anmeldung benötigt.
		await client.query("INSERT INTO workspace_roles (name) VALUES ('ADMIN'), ('USER')");
		await client.query(
			`INSERT INTO users (username, password, workspace_role_id)
			 SELECT 'admin', $1, id FROM workspace_roles WHERE name = 'ADMIN'`,
			[INITIAL_ADMIN_PASSWORD_HASH],
		);
		await client.query("COMMIT");
	} catch (error) {
		await client.query("ROLLBACK");
		throw error;
	} finally {
		await client.end();
	}
}

async function requireOk(response: Awaited<ReturnType<APIRequestContext["post"]>>): Promise<void> {
	// API-Setup soll mit Status und Antworttext scheitern, nicht erst später an fehlenden Daten.
	if (!response.ok()) {
		throw new Error(`${response.status()} ${response.url()} failed: ${await response.text()}`);
	}
}

async function login(request: APIRequestContext): Promise<Tokens> {
	// Die API-Fixtures melden sich unabhängig vom Browser an und verwenden den Access-Token.
	const response = await request.post("/api/v1/auth/login", {
		data: { username: INITIAL_ADMIN_USERNAME, password: INITIAL_ADMIN_PASSWORD },
	});
	await requireOk(response);
	return response.json() as Promise<Tokens>;
}

async function authenticatePage(page: Page, refreshToken: string): Promise<void> {
	// Das Frontend liest den Refresh-Token beim Start und erzeugt daraus seine In-Memory-Sitzung.
	await page.addInitScript(
		(token) => sessionStorage.setItem("minerva.refreshToken", token),
		refreshToken,
	);
}

async function waitForBackend(request: APIRequestContext): Promise<void> {
	// Eine erreichbare Datenbank bedeutet noch nicht, dass Nginx und Backend schon Anfragen bedienen.
	await expect(async () => {
		const response = await request.post("/api/v1/auth/login", {
			data: { username: INITIAL_ADMIN_USERNAME, password: INITIAL_ADMIN_PASSWORD },
		});
		expect(response.ok()).toBe(true);
	}).toPass({ timeout: 30_000 });
}

const test = baseTest.extend<{
	api: ApiFixture;
	authenticatedPage: Page;
	database: void;
}>({
	// Die automatische Fixture läuft vor jeder anderen Fixture und isoliert dadurch jeden Test.
	database: [
		async ({ request }, use) => {
			await resetDatabase();
			await waitForBackend(request);
			await use();
		},
		{ auto: true },
	],
	// Diese Fixture stellt angemeldete API-Helfer für schnelles, UI-unabhängiges Test-Setup bereit.
	api: async ({ database: _, request }, use) => {
		const { accessToken } = await login(request);
		const headers = { Authorization: `Bearer ${accessToken}` };
		// Das Standardprojekt enthält zusätzlich einen Übergang für unabhängige Bearbeitungstests.
		const defaultProject: ProjectInput = {
			name: "E2E Projekt",
			description: "Per API für einen unabhängigen E2E-Test angelegt",
			ticketConfiguration: {
				tickets: [
					{
						name: "Aufgabe",
						description: "",
						states: [
							{ name: "Offen", category: "OPEN" },
							{ name: "In Arbeit", category: "IN_PROGRESS" },
						],
						transitions: [{ name: "Start", from: "Offen", to: "In Arbeit" }],
						children: [],
					},
				],
			},
		};

		await use({
			async createProject(input = {}) {
				// Einzelne Tests überschreiben nur Name oder Beschreibung; der Workflow bleibt gültig.
				const response = await request.post("/api/v1/projects", {
					headers,
					data: { ...defaultProject, ...input },
				});
				await requireOk(response);
				return response.json() as Promise<number>;
			},
			async createTicket(projectId, input = {}) {
				// Tickettyp- und Status-IDs werden aus der API gelesen statt aus der Datenbank geraten.
				const typesResponse = await request.get(
					`/api/v1/projects/${projectId}/ticket-types`,
					{ headers },
				);
				await requireOk(typesResponse);
				const { ticketTypes } = (await typesResponse.json()) as {
					ticketTypes: Array<{ id: number; states: Array<{ id: number }> }>;
				};
				const ticketType = ticketTypes[0];
				const status = ticketType?.states[0];
				if (!ticketType || !status) {
					throw new Error(`Project ${projectId} has no ticket type with a state`);
				}

				// Die optionalen Eingaben überschreiben nur fachliche Ticketwerte, nicht die ermittelten IDs.
				const response = await request.post(`/api/v1/projects/${projectId}/tickets`, {
					headers,
					data: {
						name: "E2E Ticket",
						description: "",
						priority: "NORMAL",
						assignedTo: null,
						...input,
						ticketTypeId: ticketType.id,
						statusId: status.id,
						parentTicketId: null,
					},
				});
				await requireOk(response);
				return response.json() as Promise<TicketResponse>;
			},
		});
	},
	// UI-Tests erhalten eine bereits angemeldete Seite und testen nicht jedes Mal den Login-Dialog.
	authenticatedPage: async ({ database: _, page, request }, use) => {
		const { refreshToken } = await login(request);
		await authenticatePage(page, refreshToken);
		await use(page);
	},
});

export { expect, test };
