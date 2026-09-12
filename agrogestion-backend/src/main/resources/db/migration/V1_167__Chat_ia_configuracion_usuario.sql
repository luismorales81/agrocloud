-- Configuración de IA (Gemini BYOK) por usuario
CREATE TABLE IF NOT EXISTS usuario_configuracion_ia (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    usuario_id BIGINT NOT NULL,
    clave_api_cifrada VARCHAR(512) NOT NULL,
    modelo VARCHAR(64) NOT NULL DEFAULT 'gemini-2.5-flash',
    activo BOOLEAN NOT NULL DEFAULT TRUE,
    clave_valida BOOLEAN NOT NULL DEFAULT TRUE,
    fecha_actualizacion TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT uk_usuario_config_ia_usuario UNIQUE (usuario_id),
    CONSTRAINT fk_usuario_config_ia_usuario FOREIGN KEY (usuario_id) REFERENCES usuarios(id)
);
