import { expect, Page } from '@playwright/test';

/**
 * Logs in a user via the /auth/login page and waits for the portal home redirect.
 *
 * Credentials are resolved in the following order:
 *   1. Arguments passed directly to this function
 *   2. TEST_USER / TEST_PASSWORD environment variables
 *   3. Hardcoded development defaults ('admin' / 'admin')
 */
export async function loginAs(
  page: Page,
  user: string = process.env.TEST_USER ?? 'admin',
  password: string = process.env.TEST_PASSWORD ?? 'admin',
): Promise<void> {
  await page.goto('/auth/login');

  // Ant Design Form.Item with name="username" sets id="username" on the child input.
  await page.locator('#username').fill(user);

  // Ant Design Input.Password renders <input id="password" type="password" /> inside
  // a wrapper span – #password targets the inner input directly.
  await page.locator('#password').fill(password);

  await page.locator('button[type="submit"]').click();

  // After a successful login the app redirects to /portal/home.
  await page.waitForURL('**/portal/home', { timeout: 15000 });
}

/**
 * Returns true when the Ant Design "Login Error" notification is visible.
 * The notification is rendered with .ant-notification-notice-message as the title.
 */
export async function expectLoginError(page: Page): Promise<void> {
  await expect(
    page.locator('.ant-notification-notice-message', { hasText: 'Login Error' }),
  ).toBeVisible({ timeout: 10000 });
}
