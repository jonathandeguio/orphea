/**
 * E2E tests – Authentication / Login
 *
 * Page under test : /auth/login
 * Component       : src/pages/Auth/Login.tsx + LoginModal.tsx
 *
 * Selectors used:
 *   #username            – Ant Design Form.Item name="username" sets id on child input
 *   #password            – Ant Design Input.Password inner <input id="password">
 *   button[type="submit"]– BoslerButton htmlType="submit"
 *   .ant-notification-notice-message – Ant Design notification title
 *   .ant-form-item-explain-error     – Ant Design inline validation message
 */

import { expect, test } from '@playwright/test';
import { expectLoginError, loginAs } from '../helpers/auth';

// ---------------------------------------------------------------------------
// Happy path
// ---------------------------------------------------------------------------

test.describe('Login – happy path', () => {
  test('should redirect to /portal/home after valid credentials', async ({ page }) => {
    await page.goto('/auth/login');

    await page.locator('#username').fill(process.env.TEST_USER ?? 'admin');
    await page.locator('#password').fill(process.env.TEST_PASSWORD ?? 'admin');
    await page.locator('button[type="submit"]').click();

    await page.waitForURL('**/portal/home', { timeout: 15000 });
    await expect(page).toHaveURL(/\/portal\/home/);
  });
});

// ---------------------------------------------------------------------------
// Error path – wrong password
// ---------------------------------------------------------------------------

test.describe('Login – wrong credentials', () => {
  test('should show "Login Error" notification for bad password', async ({ page }) => {
    await page.goto('/auth/login');

    await page.locator('#username').fill(process.env.TEST_USER ?? 'admin');
    // Deliberately use an invalid password
    await page.locator('#password').fill('__invalid_password__');
    await page.locator('button[type="submit"]').click();

    // The Login component calls openNotification("Login Error", ...) on error.
    // Ant Design 5 renders the notification title inside .ant-notification-notice-message.
    await expectLoginError(page);

    // Must remain on the login page
    await expect(page).toHaveURL(/\/auth\/login/);
  });
});

// ---------------------------------------------------------------------------
// Validation – empty fields
// ---------------------------------------------------------------------------

test.describe('Login – empty fields', () => {
  test('should show required-field messages and block submission', async ({ page }) => {
    await page.goto('/auth/login');

    // Click submit without filling any field
    await page.locator('button[type="submit"]').click();

    // Ant Design Form validates and renders .ant-form-item-explain-error elements.
    // Rules from LoginModal.tsx:
    //   username: "Please input your username!"
    //   password: "Please input your password!"
    await expect(
      page.locator('.ant-form-item-explain-error', {
        hasText: 'Please input your username!',
      }),
    ).toBeVisible({ timeout: 5000 });

    await expect(
      page.locator('.ant-form-item-explain-error', {
        hasText: 'Please input your password!',
      }),
    ).toBeVisible({ timeout: 5000 });

    // Should stay on the login page (no redirect)
    await expect(page).toHaveURL(/\/auth\/login/);
  });

  test('should show username validation when only password is filled', async ({ page }) => {
    await page.goto('/auth/login');

    await page.locator('#password').fill('somepassword');
    await page.locator('button[type="submit"]').click();

    await expect(
      page.locator('.ant-form-item-explain-error', {
        hasText: 'Please input your username!',
      }),
    ).toBeVisible({ timeout: 5000 });

    await expect(page).toHaveURL(/\/auth\/login/);
  });
});

// ---------------------------------------------------------------------------
// Already authenticated
// ---------------------------------------------------------------------------

test.describe('Login – already authenticated', () => {
  test('should redirect to /portal/home when a valid token is already set', async ({
    page,
  }) => {
    // First login to obtain a valid session token stored in localStorage.
    await loginAs(page);
    await expect(page).toHaveURL(/\/portal\/home/);

    // Now navigate to the login page; Login.tsx reads tokenStatus from Redux
    // and calls navigate("/portal/home") when isTokenValid === true.
    await page.goto('/auth/login');

    await page.waitForURL('**/portal/home', { timeout: 10000 });
    await expect(page).toHaveURL(/\/portal\/home/);
  });
});
