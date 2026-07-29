import { expect, test } from "./fixtures";
import { Client } from "pg";

const databaseUrl = process.env.DATABASE_URL;

test("admin can delete a project via UI and all data is removed", async ({
	api,
	authenticatedPage: page,
}) => {
	const projectId = await api.createProject({ name: "Zu löschendes Projekt" });
	const ticket = await api.createTicket(projectId, { name: "Ticket im Zielprojekt" });

	const controlProjectId = await api.createProject({
		name: "Kontrollprojekt",
		description: "Dieses Projekt muss nach der Löschung intakt bleiben",
	});

	const client = new Client({ connectionString: databaseUrl! });
	await client.connect();
	try {
		const activityEvents = await client.query<{ id: string }>(
			`SELECT event_id AS id FROM activity_scopes
			 WHERE scope_type = 'PROJECT' AND scope_id = $1`,
			[projectId],
		);
		const eventIds = activityEvents.rows.map(({ id }) => id);
		expect(eventIds.length).toBeGreaterThan(0);

		const ticketScopesBeforeDeletion = await client.query(
			`SELECT id FROM activity_scopes
			 WHERE scope_type = 'TICKET' AND scope_id = $1`,
			[ticket.id],
		);
		expect(ticketScopesBeforeDeletion.rowCount).toBeGreaterThan(0);

		await page.goto(`/project/${projectId}`);
		await page.getByRole("tab", { name: "Einstellungen" }).click();
		await page.getByRole("button", { name: "Projekt löschen" }).click();

		const dialog = page.getByRole("alertdialog");
		await expect(dialog).toBeVisible();
		await expect(dialog).toContainText("Zu löschendes Projekt");
		await expect(dialog).toContainText("unwiderruflich");

		await dialog.getByRole("button", { name: "Projekt löschen" }).click();

		await expect(page).toHaveURL("/projects");
		await expect(page.getByText("Zu löschendes Projekt")).not.toBeVisible();
		await expect(page.getByRole("link", { name: /^Kontrollprojekt/ })).toBeVisible();

		const projectResult = await client.query("SELECT id FROM projects WHERE id = $1", [
			projectId,
		]);
		expect(projectResult.rowCount).toBe(0);

		const ticketResult = await client.query("SELECT id FROM tickets WHERE project_id = $1", [
			projectId,
		]);
		expect(ticketResult.rowCount).toBe(0);

		const activityEventsAfterDeletion = await client.query(
			"SELECT id FROM activity_events WHERE id = ANY($1::bigint[])",
			[eventIds],
		);
		expect(activityEventsAfterDeletion.rowCount).toBe(0);

		const activityScopesAfterDeletion = await client.query(
			"SELECT id FROM activity_scopes WHERE event_id = ANY($1::bigint[])",
			[eventIds],
		);
		expect(activityScopesAfterDeletion.rowCount).toBe(0);

		const ticketScopesAfterDeletion = await client.query(
			`SELECT id FROM activity_scopes
			 WHERE scope_type = 'TICKET' AND scope_id = $1`,
			[ticket.id],
		);
		expect(ticketScopesAfterDeletion.rowCount).toBe(0);

		const controlResult = await client.query("SELECT id FROM projects WHERE id = $1", [
			controlProjectId,
		]);
		expect(controlResult.rowCount).toBe(1);

		const controlTickets = await client.query("SELECT id FROM tickets WHERE project_id = $1", [
			controlProjectId,
		]);
		expect(controlTickets.rowCount).toBe(0);
	} finally {
		await client.end();
	}
});
