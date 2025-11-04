-- Crear tabla para trackear el uso de la API del clima
CREATE TABLE IF NOT EXISTS weather_api_usage (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    fecha DATE NOT NULL UNIQUE,
    usos_hoy INT NOT NULL DEFAULT 0,
    limite_diario INT NOT NULL DEFAULT 1000,
    ultima_actualizacion DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_fecha (fecha)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='Registro de uso diario de la API del clima';

-- Insertar registro inicial para hoy si no existe
INSERT IGNORE INTO weather_api_usage (fecha, usos_hoy, limite_diario, ultima_actualizacion)
VALUES (CURDATE(), 0, 1000, NOW());

