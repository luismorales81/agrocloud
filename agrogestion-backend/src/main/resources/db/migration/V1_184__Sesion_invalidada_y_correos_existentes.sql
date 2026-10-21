-- Corte de sesión al cambiar la contraseña. NULL = los JWT emitidos hasta ahora siguen válidos.
ALTER TABLE usuarios
    ADD COLUMN sesion_invalidada_en DATETIME NULL;

-- Las cuentas que ya operan no quedan afuera por el nuevo control de correo.
-- El registro público posterior nace con email_verified = 0 hasta abrir el enlace.
UPDATE usuarios
SET email_verified = 1
WHERE email_verified IS NULL OR email_verified = 0;
