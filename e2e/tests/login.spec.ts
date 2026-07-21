import { expect, test } from "./fixtures";

test("initial admin can log in", async ({ page }) => {
	await page.goto("/login");
	await page.getByLabel("Username").fill("admin");
	await page.getByLabel("Password").fill("e2e-password");
	await page.getByRole("button", { name: "Login" }).click();

	await expect(page).toHaveURL(/\/$/);
	await expect(page.getByText("admin", { exact: true })).toBeVisible();
});
