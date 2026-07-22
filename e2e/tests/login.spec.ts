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
		await page.goto("/admin");

		await expect(page.getByRole("heading", { name: "Administrator Ansicht" })).toBeVisible();
	});

	await test.step("admin creates a user", async () => {
		await page.getByLabel("Benutzername").fill("test-user");
		await page.getByLabel("Passwort").fill("test-password");
		await page.getByLabel("Rolle").selectOption("USER");
		await page.getByRole("button", { name: "Benutzer erstellen" }).click();

		await expect(page.getByText("Benutzer wurde erstellt.")).toBeVisible();
		await expect(page.getByRole("cell", { name: "test-user", exact: true })).toBeVisible();
	});
});
