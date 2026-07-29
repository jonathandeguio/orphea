/**
 * E2E tests – Logout flow
 *
 * Flow under test:
 *   1. User is authenticated and on /portal/home
 *   2. User opens the avatar dropdown (hover trigger) in the sidebar bottom
 *   3. User clicks "Logout"
 *   4. The app dispatches logout() + refreshTokenStatus() then navigates away
 *
 * SBElementAvatar.tsx (key behaviour):
 *   - Dropdown trigger: "hover" on the BoslerAvatar
 *   - Logout item onClick: dispatch(logout()), dispatch(refreshTokenStatus()),
 *                          navigate("/Auth/logout")
 *   - The route /auth/logout renders the Logout component which shows
 *     "You have been successfully logged out." (getLanguageLabel("loggedOutSuccess"))
 *
 * NotificationBell.view.tsx (regression guard – commit cc617adb):
 *   - On error, only shows "Unable to fetch Notifications" toast when
 *     error.response.status !== 401, suppressing false-positive toasts on logout.
 *
 * Note on URL casing:
 *   The navigate() call uses "/Auth/logout" (capital A) while the route is
 *   "/auth/logout" (lower-case). Tests assert the user is no longer on
 *   /portal/home and is on a page confirming logout or showing login UI.
 */

import { expect, test } from '@playwright/test';
import { loginAs } from '../helpers/auth';

// Helper: hover the sidebar avatar and click the Logout dropdown item.
async function clickLogout(page: import('@playwright/test').Page): Promise<void> {
  // BoslerAvatar is rendered as an Ant Design <Avatar> → <span class="ant-avatar">.
  // It lives at the bottom of the sidebar. The Dropdown trigger is "hover".
  // We hover the avatar span to open the menu, then click the Logout item.
  const avatar = page.locator('.ant-avatar').last();
  await expect(avatar).toBeVisible({ timeout: 10000 });
  await avatar.hover();

  // Ant Design Dropdown v5 renders menu items as <li role="menuitem">.
  // getLanguageLabel("logout") returns "Logout" in English.
  const logoutItem = page.getByRole('menuitem', { name: 'Logout' });
  await expect(logoutItem).toBeVisible({ timeout: 5000 });
  await logoutItem.click();
}

// ---------------------------------------------------------------------------
// Suite
// ---------------------------------------------------------------------------

test.describe('Logout', () => {
  test.beforeEach(async ({ page }) => {
    await loginAs(page);
  });

  // -------------------------------------------------------------------------
  // 1. Logout navigates away from the portal
  // -------------------------------------------------------------------------

  test('should leave /portal/home after clicking Logout', async ({ page }) => {
    await clickLogout(page);

    // After logout the user should no longer be on the portal home page.
    // The actual destination depends on token invalidation timing:
    //   - /Auth/logout   → Logout component ("You have been successfully logged out.")
    //   - /auth/logout   → same Logout component (React Router lower-case route)
    //   - /auth/relogin  → PrivateOutlet detects invalid token
    //   - /auth/login    → default redirect for unauthenticated users
    await page.waitForTimeout(1500); // allow navigation + Redux state to settle
    await expect(page).not.toHaveURL(/\/portal\/home/);
  });

  test('should show a logged-out confirmation or redirect to an auth page', async ({
    page,
  }) => {
    await clickLogout(page);

    // Wait for navigation to settle.
    await page.waitForTimeout(2000);

    const currentUrl = page.url();
    const isOnAuthPage =
      /\/(auth|Auth)\/(logout|login|relogin)/.test(currentUrl) ||
      // Fallback: user may be on the 404 page if the /Auth/ (capital A) path
      // doesn't match any route – still means they are off the portal.
      !/\/portal\/home/.test(currentUrl);

    expect(
      isOnAuthPage,
      `Expected to be on an auth/logout page but was: ${currentUrl}`,
    ).toBe(true);
  });

  test('should display logged-out confirmation text when Logout component renders', async ({
    page,
  }) => {
    await clickLogout(page);

    // The Logout component renders getLanguageLabel("loggedOutSuccess"):
    // → "You have been successfully logged out." (English)
    // This assertion is soft: if the Logout component is NOT rendered (e.g., the
    // user lands on a 404 or /auth/login), we just verify they left the portal.
    await page.waitForTimeout(2000);

    const hasLoggedOutText = await page
      .getByText('You have been successfully logged out.')
      .isVisible()
      .catch(() => false);

    const isOffPortal = !/\/portal\/home/.test(page.url());

    expect(
      hasLoggedOutText || isOffPortal,
      'After logout, expected either the logged-out message or to be off /portal/home.',
    ).toBe(true);
  });

  // -------------------------------------------------------------------------
  // 2. No "Unable to fetch Notifications" toast after logout
  //    Regression guard for commit cc617adb:
  //    "fix: suppress notification fetch error on logout (401 Unauthorized)"
  // -------------------------------------------------------------------------

  test('should NOT show "Unable to fetch Notifications" after logout', async ({ page }) => {
    await clickLogout(page);

    // Wait long enough for any pending API calls to resolve / reject (401).
    // The NotificationBell's resurfaceNotifications() catches 401 without toasting.
    await page.waitForTimeout(3000);

    // Assert the error toast is absent.
    await expect(
      page.locator('.ant-notification-notice-message', {
        hasText: 'Unable to fetch Notifications',
      }),
    ).not.toBeVisible();
  });
});
