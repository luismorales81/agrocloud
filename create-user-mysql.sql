-- Script para crear el usuario y base de datos para AgroGestion
-- Ejecutar este script como root en MySQL

-- Crear la base de datos
CREATE DATABASE IF NOT EXISTS agrocloud CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

-- Crear el usuario
CREATE USER IF NOT EXISTS 'agrocloudbd'@'localhost' IDENTIFIED BY 'Jones1212';

-- Otorgar permisos al usuario
GRANT ALL PRIVILEGES ON agrocloud.* TO 'agrocloudbd'@'localhost';

-- Aplicar los cambios
FLUSH PRIVILEGES;

-- Verificar que el usuario fue creado
SELECT User, Host FROM mysql.user WHERE User = 'agrocloudbd';

-- Verificar que la base de datos existe
SHOW DATABASES LIKE 'agrocloud';
