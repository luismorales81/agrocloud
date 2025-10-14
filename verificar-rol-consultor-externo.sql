-- Verificar el usuario Consultor Externo y su rol
SELECT 
    u.id as usuario_id,
    u.email,
    u.first_name,
    u.last_name,
    u.activo,
    ue.id as relacion_id,
    e.id as empresa_id,
    e.nombre as empresa_nombre,
    ue.rol,
    ue.estado,
    r.id as role_id,
    r.nombre as role_nombre
FROM usuarios u
LEFT JOIN usuario_empresas ue ON u.id = ue.usuario_id
LEFT JOIN empresas e ON ue.empresa_id = e.id
LEFT JOIN roles r ON ue.rol_id = r.id
WHERE u.email LIKE '%consultor%' 
   OR u.first_name LIKE '%Consultor%'
   OR u.last_name LIKE '%Externo%'
   OR r.nombre LIKE '%CONSULTOR%'
ORDER BY u.id, ue.id;

-- Verificar la estructura de usuario_empresas
SELECT COLUMN_NAME, DATA_TYPE 
FROM INFORMATION_SCHEMA.COLUMNS 
WHERE TABLE_NAME = 'usuario_empresas';

