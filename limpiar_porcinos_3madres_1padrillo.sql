-- ============================================================================
-- LIMPIEZA DE CONTENIDO (NO BORRA TABLAS)
-- Dejar solo 3 madres y 1 padrillo en el sistema
-- ============================================================================

SET @empresa_id = (SELECT id FROM empresas LIMIT 1);
SET @usuario_id = (SELECT id FROM usuarios LIMIT 1);
SET @empresa_id = COALESCE(@empresa_id, 1);
SET @usuario_id = COALESCE(@usuario_id, 1);

-- Borrar contenido de tablas relacionadas (orden por dependencias)
DELETE FROM porcinos_consumos_diarios_detalle;
DELETE FROM porcinos_consumos_diarios_automaticos;
DELETE FROM porcinos_movimientos_stock;
DELETE FROM porcinos_derrames_perdidas;
DELETE FROM porcinos_dias_alimentacion;
DELETE FROM porcinos_consumos_alimento;
DELETE FROM porcinos_muertes_lactancia;
DELETE FROM porcinos_destetes;
DELETE FROM porcinos_partos;
DELETE FROM porcinos_chequeos_gestacion;
DELETE FROM porcinos_gestacion;
DELETE FROM porcinos_servicios;
DELETE FROM porcinos_historial_estados_madres;
DELETE FROM porcinos_muertes_madres;
DELETE FROM porcinos_eventos_sanitarios;
DELETE FROM porcinos_registros_peso;
DELETE FROM porcinos_muertes_recria;
DELETE FROM porcinos_transferencias_lechones;
DELETE FROM porcinos_lechones_nn;
DELETE FROM porcinos_ventas_porcinos;
DELETE FROM porcinos_faena;
DELETE FROM porcinos_recria;
DELETE FROM porcinos_madres;
DELETE FROM porcinos_padrillos;

-- Insertar 3 madres y 1 padrillo
SET @raza_id = (SELECT id FROM porcinos_razas_porcinos LIMIT 1);
SET @ubicacion_id = (SELECT id FROM porcinos_ubicaciones_internas LIMIT 1);

INSERT INTO porcinos_madres
  (identificacion, fecha_nacimiento, cantidad_tetas, origen, estado_actual, fecha_ingreso_granja,
   raza_id, numero_partos, ubicacion_interna_id, activo, empresa_id, usuario_id, fecha_creacion)
VALUES
  ('MADRE-001', '2022-01-15', 14, 'EXTERNA', 'CACHORRA', '2023-01-10',
   @raza_id, 0, @ubicacion_id, TRUE, @empresa_id, @usuario_id, NOW()),
  ('MADRE-002', '2022-03-20', 14, 'EXTERNA', 'CACHORRA', '2023-01-10',
   @raza_id, 0, @ubicacion_id, TRUE, @empresa_id, @usuario_id, NOW()),
  ('MADRE-003', '2022-05-10', 14, 'EXTERNA', 'CACHORRA', '2023-01-10',
   @raza_id, 0, @ubicacion_id, TRUE, @empresa_id, @usuario_id, NOW());

INSERT INTO porcinos_padrillos
  (identificacion, fecha_nacimiento, origen, fecha_ingreso_granja,
   raza_id, proveedor_genetica_id, ubicacion_interna_id, activo, empresa_id, usuario_id, fecha_creacion)
VALUES
  ('PADRILLO-001', '2021-06-01', 'EXTERNA', '2022-06-15',
   @raza_id, NULL, @ubicacion_id, TRUE, @empresa_id, @usuario_id, NOW());
