import { expect, test } from "./fixtures";

test("initial admin can log in", async ({ page }) => {
	await page.goto("/login");
	await page.getByLabel("Username").fill("admin");
	await page.getByLabel("Password").fill("e2e-password");
	await page.getByRole("button", { name: "Login" }).click();

	await expect(page).toHaveURL(/\/$/);
	await expect(page.getByText("admin", { exact: true })).toBeVisible();
});

test("admin can create a user", async ({ page }) => {
	await test.step("initial admin logs in", async () => {
		await page.goto("/login");
		await page.getByLabel("Username").fill("admin");
		await page.getByLabel("Password").fill("e2e-password");
		await page.getByRole("button", { name: "Login" }).click();

		await expect(page).toHaveURL(/\/$/);
	});

	await test.step("admin opens user management", async () => {
		await page.getByRole("button", { name: "Benutzer", exact: true }).click();

		await expect(page).toHaveURL(/\/admin\/users$/);
		await expect(page.getByRole("heading", { name: "Benutzerverwaltung" })).toBeVisible();
	});

	await test.step("admin creates a user", async () => {
		await page.getByRole("button", { name: "Nutzer hinzufügen" }).click();

		const drawer = page.getByRole("dialog", { name: "Nutzer hinzufügen" });
		await drawer.getByLabel("Benutzername").fill("test-user");
		await drawer.getByLabel("Passwort").fill("test-password");
		await drawer.getByLabel("Rolle").click();
		await page.getByRole("option", { name: "Administrator" }).click();
		await drawer.getByRole("button", { name: "Benutzer erstellen" }).click();

		await expect(drawer).toBeHidden();
		const userRow = page.getByRole("row").filter({ hasText: "test-user" });
		await expect(userRow.getByRole("cell", { name: "Administrator" })).toBeVisible();
	});
});
