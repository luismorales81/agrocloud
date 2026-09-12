import { test, expect } from '@playwright/test';

test.describe('Página de login', () => {
  test('muestra formulario de acceso', async ({ page }) => {
    await page.goto('/login');
    await expect(page.getByTestId('email-input')).toBeVisible();
    await expect(page.getByTestId('password-input')).toBeVisible();
    await expect(page.getByTestId('login-button')).toBeVisible();
    await expect(page.getByRole('heading', { name: 'AgroCloud' })).toBeVisible();
  });

  test('valida campos vacíos', async ({ page }) => {
    await page.goto('/login');
    await page.getByTestId('login-button').click();
    await expect(page.getByText('Por favor completa todos los campos')).toBeVisible();
  });
});

test.describe('Login con backend', () => {
  test.skip(
    !process.env.E2E_EMAIL || !process.env.E2E_PASSWORD,
    'Requiere E2E_EMAIL y E2E_PASSWORD (ver .env.e2e.example)'
  );

  test('inicia sesión y llega al dashboard', async ({ page }) => {
    await page.goto('/login');
    await page.getByTestId('email-input').fill(process.env.E2E_EMAIL!);
    await page.getByTestId('password-input').fill(process.env.E2E_PASSWORD!);
    await page.getByTestId('login-button').click();

    // Selector de empresa (varias empresas) o dashboard directo
    const selectorEmpresa = page.getByText('Seleccionar Empresa');
    if (await selectorEmpresa.isVisible({ timeout: 15_000 }).catch(() => false)) {
      await page.locator('button').filter({ hasText: /empresa/i }).first().click();
    }

    await expect(page).toHaveURL(/dashboard/, { timeout: 30_000 });
  });
});
