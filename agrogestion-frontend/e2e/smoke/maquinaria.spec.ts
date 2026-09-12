import { test, expect } from '@playwright/test';

test.describe('Maquinaria (autenticado)', () => {
  test.skip(
    !process.env.E2E_EMAIL || !process.env.E2E_PASSWORD,
    'Requiere E2E_EMAIL y E2E_PASSWORD'
  );

  test.beforeEach(async ({ page }) => {
    await page.goto('/login');
    await page.getByTestId('email-input').fill(process.env.E2E_EMAIL!);
    await page.getByTestId('password-input').fill(process.env.E2E_PASSWORD!);
    await page.getByTestId('login-button').click();

    const selectorEmpresa = page.getByText('Seleccionar Empresa');
    if (await selectorEmpresa.isVisible({ timeout: 15_000 }).catch(() => false)) {
      await page.locator('button').filter({ has: page.locator('h4') }).first().click();
    }
    await page.waitForURL(/dashboard/, { timeout: 30_000 });
  });

  test('pantalla de maquinaria carga listado', async ({ page }) => {
    await page.goto('/cultivos/maquinaria');
    await expect(page.getByText('Gestión de Maquinaria')).toBeVisible({ timeout: 20_000 });
    await expect(page.getByPlaceholder('Buscar maquinaria...')).toBeVisible();
  });
});
