import { expect, test } from "./fixtures";

test("admin can create a project", async ({ authenticatedPage: page }) => {
	// Der Test durchläuft bewusst den echten Projekt-Wizard statt das Projekt per API anzulegen.
	await page.goto("/create-project");

	// Im ersten Schritt werden die allgemeinen Projektdaten erfasst.
	await page.getByLabel("Projektname").fill("Playwright Projekt");
	await page.getByLabel("Projektbeschreibung").fill("Über die Oberfläche angelegt");
	await page.getByRole("button", { name: "Weiter" }).click();

	// Ein Projekt benötigt mindestens einen Tickettyp, bevor der Wizard fortgesetzt werden kann.
	await page.getByLabel("Name", { exact: true }).fill("Aufgabe");
	await page.getByRole("button", { name: "Hinzufügen" }).click();
	await page.getByRole("button", { name: "Weiter" }).click();

	// Der Tickettyp benötigt mindestens einen Zustand; Übergänge sind für dieses Szenario optional.
	await page.getByRole("tab", { name: "Zustände" }).click();
	await page.getByLabel("Neuer Zustand").fill("Offen");
	await page.getByRole("button", { name: "Zustand hinzufügen" }).click();
	await page.getByRole("button", { name: "Projekt erstellen", exact: true }).last().click();

	// Weiterleitung und Überschrift bestätigen, dass Backend und UI das Projekt angelegt haben.
	await expect(page).toHaveURL(/\/project\/\d+$/);
	await expect(page.getByRole("heading", { name: "Playwright Projekt" })).toBeVisible();
});

test("admin can create a ticket with priority and assignee", async ({
	api,
	authenticatedPage: page,
}) => {
	// Das Projekt entsteht per API, damit dieser Test ausschließlich die Ticket-Oberfläche prüft.
	const projectId = await api.createProject();
	// Nach dem Öffnen des Projekts wird der Ticket-Tab und anschließend das Formular geöffnet.
	await page.goto(`/project/${projectId}`);
	await page.getByRole("tab", { name: "Tickets" }).click();
	await page.getByRole("button", { name: "Ticket erstellen" }).click();

	// Alle Eingaben werden auf den Dialog begrenzt, damit gleichnamige Elemente nicht kollidieren.
	const form = page.getByRole("dialog", { name: "Ticket erstellen" });
	await form.getByLabel("Titel").fill("Playwright Ticket");
	await form.getByLabel("Beschreibung").fill("Mit Priorität und Bearbeiter");
	// Abweichend von den Defaults werden Priorität und angemeldeter Admin explizit ausgewählt.
	await form.getByLabel("Priorität").click();
	await page.getByRole("option", { name: "Hoch", exact: true }).click();
	await form.getByLabel("Bearbeiter").click();
	await page.getByRole("option", { name: "admin", exact: true }).click();
	await form.getByRole("button", { name: "Ticket erstellen" }).click();

	// Ein geschlossener Dialog und das sichtbare Ticket bestätigen den erfolgreichen Request.
	await expect(form).toBeHidden();
	await expect(page.getByText("Playwright Ticket", { exact: true })).toBeVisible();
});

test("admin can update ticket priority and status", async ({ api, authenticatedPage: page }) => {
	// Projekt und Ticket werden per API vorbereitet, damit nur die Bearbeitungsoberfläche getestet wird.
	const projectId = await api.createProject();
	await api.createTicket(projectId, { name: "Zu bearbeitendes Ticket" });

	// Das erste Ticket wird nach dem Öffnen des Ticket-Tabs automatisch im Detailbereich ausgewählt.
	await page.goto(`/project/${projectId}`);
	await page.getByRole("tab", { name: "Tickets" }).click();
	await expect(
		page.getByRole("button", { name: "Zu bearbeitendes Ticket", exact: true }),
	).toBeVisible();

	// Die Priorität wird über das Detailformular geändert und direkt im Select bestätigt.
	await page.getByLabel("Priorität").click();
	await page.getByRole("option", { name: "Höchste", exact: true }).click();
	await expect(page.getByLabel("Priorität")).toContainText("Höchste");

	// Der vorhandene Workflow erlaubt den Übergang von "Offen" nach "In Arbeit".
	await page.getByLabel("Status").click();
	await page.getByRole("option", { name: "Start → In Arbeit", exact: true }).click();
	await expect(page.getByLabel("Status")).toContainText("In Arbeit");
});

test("admin can restore an archived ticket", async ({ api, authenticatedPage: page }) => {
	const projectId = await api.createProject();
	const ticket = await api.createTicket(projectId, { name: "Archiviertes Ticket" });
	await api.archiveTicket(projectId, ticket.id);

	await page.goto(`/project/${projectId}`);
	await page.getByRole("tab", { name: "Tickets" }).click();
	await page.getByLabel("Ticketansicht").click();
	await page.getByRole("option", { name: "Archivierte Tickets" }).click();
	await expect(page.getByRole("button", { name: ticket.name, exact: true })).toBeVisible();

	await page.getByRole("button", { name: "Ticket wiederherstellen" }).click();
	await expect(page.getByRole("button", { name: ticket.name, exact: true })).toBeHidden();

	await page.getByLabel("Ticketansicht").click();
	await page.getByRole("option", { name: "Aktive Tickets" }).click();
	await expect(page.getByRole("button", { name: ticket.name, exact: true })).toBeVisible();
});

test("admin can restore an archived project", async ({ api, authenticatedPage: page }) => {
	const projectName = "Archiviertes Projekt";
	const projectId = await api.createProject({ name: projectName });

	await page.goto(`/project/${projectId}`);
	await page.getByRole("tab", { name: "Einstellungen" }).click();
	await page.getByRole("button", { name: "Projekt archivieren", exact: true }).click();
	const archiveDialog = page.getByRole("alertdialog");
	await archiveDialog.getByRole("button", { name: "Projekt archivieren" }).click();
	await expect(page).toHaveURL("/");

	const navigation = page.getByText("Navigation", { exact: true }).locator("..");
	await navigation.getByRole("button", { name: "Archivierte Projekte", exact: true }).click();
	await expect(page).toHaveURL("/projects/archived");
	await expect(page.getByRole("cell", { name: projectName, exact: true })).toBeVisible();
	await navigation.getByRole("button", { name: "Übersicht", exact: true }).click();
	await expect(page).toHaveURL("/");

	await navigation.getByRole("button", { name: "Archivierte Projekte aufklappen" }).click();
	await navigation.getByRole("button", { name: projectName, exact: true }).click();
	await expect(page.getByText("Dieses Projekt ist archiviert.")).toBeVisible();

	await page.getByRole("tab", { name: "Einstellungen" }).click();
	await page.getByRole("button", { name: "Projekt wiederherstellen" }).click();
	await expect(page.getByText("Dieses Projekt ist archiviert.")).toBeHidden();
	await expect(
		navigation.getByRole("button", { name: "Archivierte Projekte", exact: true }),
	).toBeVisible();
	await navigation.getByRole("button", { name: "Projekte aufklappen" }).click();
	await expect(navigation.getByRole("button", { name: projectName, exact: true })).toBeVisible();

	await navigation.getByRole("button", { name: "Übersicht", exact: true }).click();
	await navigation.getByRole("button", { name: projectName, exact: true }).click();
	await page.getByRole("tab", { name: "Aktivitäten" }).click();
	await expect(page.getByText("Projekt wiederhergestellt")).toBeVisible();
});
