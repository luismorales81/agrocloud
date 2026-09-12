# Script para subir cambios del modal de recordatorio
$ErrorActionPreference = "Stop"

Write-Host "Cambiando a rama modificaciones-pre-porcinos..." -ForegroundColor Cyan
git checkout modificaciones-pre-porcinos 2>&1 | Out-Null
if ($LASTEXITCODE -ne 0) {
    Write-Host "Creando nueva rama modificaciones-pre-porcinos..." -ForegroundColor Yellow
    git checkout -b modificaciones-pre-porcinos
}

Write-Host "Agregando cambios de CalendarioDashboard.tsx..." -ForegroundColor Cyan
git add agrogestion-frontend/src/components/CalendarioDashboard.tsx

Write-Host "Haciendo commit..." -ForegroundColor Cyan
git commit -m "feat: agregar modal de detalle de recordatorio en calendario"

Write-Host "Subiendo cambios a origin..." -ForegroundColor Cyan
git push origin modificaciones-pre-porcinos

Write-Host "¡Listo! Cambios subidos exitosamente." -ForegroundColor Green

