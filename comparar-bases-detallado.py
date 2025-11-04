#!/usr/bin/env python3
"""
Script para comparar estructuras de bases de datos MySQL
y generar un script SQL con las diferencias
"""

import mysql.connector
from mysql.connector import Error
import sys
from datetime import datetime

# Configuración Local
LOCAL_CONFIG = {
    'host': 'localhost',
    'port': 3306,
    'user': 'root',
    'password': '123456',
    'database': 'agrocloud'
}

# Configuración Railway
RAILWAY_CONFIG = {
    'host': 'gondola.proxy.rlwy.net',
    'port': 54893,
    'user': 'root',
    'password': 'WSoobrppUQbaPINdsRcoQVkUvtYKjmSe',
    'database': 'railway'
}

def get_connection(config):
    """Establecer conexión a MySQL"""
    try:
        connection = mysql.connector.connect(**config)
        return connection
    except Error as e:
        print(f"Error conectando a {config['host']}: {e}")
        return None

def get_tables(connection):
    """Obtener lista de tablas"""
    tables = []
    try:
        cursor = connection.cursor()
        cursor.execute("SHOW TABLES")
        tables = [table[0] for table in cursor.fetchall()]
        cursor.close()
    except Error as e:
        print(f"Error obteniendo tablas: {e}")
    return tables

def get_table_structure(connection, table_name):
    """Obtener estructura de una tabla"""
    structure = {
        'columns': [],
        'indexes': [],
        'constraints': []
    }
    try:
        cursor = connection.cursor()
        
        # Obtener columnas
        cursor.execute(f"DESCRIBE `{table_name}`")
        columns = cursor.fetchall()
        for col in columns:
            structure['columns'].append({
                'field': col[0],
                'type': col[1],
                'null': col[2],
                'key': col[3],
                'default': col[4],
                'extra': col[5]
            })
        
        # Obtener índices
        cursor.execute(f"SHOW INDEX FROM `{table_name}`")
        indexes = cursor.fetchall()
        for idx in indexes:
            structure['indexes'].append({
                'table': idx[0],
                'non_unique': idx[1],
                'key_name': idx[2],
                'seq_in_index': idx[3],
                'column_name': idx[4],
                'collation': idx[5],
                'cardinality': idx[6],
                'sub_part': idx[7],
                'packed': idx[8],
                'null': idx[9],
                'index_type': idx[10]
            })
        
        # Obtener CREATE TABLE statement
        cursor.execute(f"SHOW CREATE TABLE `{table_name}`")
        create_table = cursor.fetchone()
        structure['create_statement'] = create_table[1] if create_table else None
        
        cursor.close()
    except Error as e:
        print(f"Error obteniendo estructura de {table_name}: {e}")
    
    return structure

def compare_structures(local_structure, railway_structure, table_name):
    """Comparar estructuras y generar diferencias"""
    differences = []
    
    # Comparar columnas
    local_cols = {col['field']: col for col in local_structure['columns']}
    railway_cols = {col['field']: col for col in railway_structure['columns']}
    
    # Columnas en local pero no en railway
    for col_name, col_info in local_cols.items():
        if col_name not in railway_cols:
            differences.append(f"ALTER TABLE `{table_name}` ADD COLUMN `{col_name}` {col_info['type']} {'NULL' if col_info['null'] == 'YES' else 'NOT NULL'} {'DEFAULT ' + str(col_info['default']) if col_info['default'] is not None else ''} {'AUTO_INCREMENT' if 'auto_increment' in col_info['extra'].lower() else ''};")
    
    # Columnas en railway pero no en local (solo informativo)
    for col_name in railway_cols:
        if col_name not in local_cols:
            differences.append(f"-- COLUMNA EN RAILWAY PERO NO EN LOCAL: {table_name}.{col_name}")
    
    # Comparar columnas existentes por diferencias
    for col_name in local_cols:
        if col_name in railway_cols:
            local_col = local_cols[col_name]
            railway_col = railway_cols[col_name]
            
            if (local_col['type'] != railway_col['type'] or 
                local_col['null'] != railway_col['null'] or
                local_col['default'] != railway_col['default']):
                differences.append(f"ALTER TABLE `{table_name}` MODIFY COLUMN `{col_name}` {local_col['type']} {'NULL' if local_col['null'] == 'YES' else 'NOT NULL'} {'DEFAULT ' + str(local_col['default']) if local_col['default'] is not None else ''};")
    
    return differences

def main():
    print("=" * 60)
    print("COMPARACIÓN DE BASES DE DATOS LOCAL Y RAILWAY")
    print("=" * 60)
    print()
    
    # Conectar a bases de datos
    print("[1/5] Conectando a base de datos LOCAL...")
    local_conn = get_connection(LOCAL_CONFIG)
    if not local_conn:
        print("ERROR: No se pudo conectar a la base de datos local")
        sys.exit(1)
    print("✓ Conectado a LOCAL")
    
    print("[2/5] Conectando a base de datos RAILWAY...")
    railway_conn = get_connection(RAILWAY_CONFIG)
    if not railway_conn:
        print("ERROR: No se pudo conectar a Railway")
        local_conn.close()
        sys.exit(1)
    print("✓ Conectado a RAILWAY")
    
    # Obtener tablas
    print("[3/5] Obteniendo listas de tablas...")
    local_tables = get_tables(local_conn)
    railway_tables = get_tables(railway_conn)
    print(f"✓ Tablas en LOCAL: {len(local_tables)}")
    print(f"✓ Tablas en RAILWAY: {len(railway_tables)}")
    
    # Tablas que faltan en Railway
    missing_tables = [t for t in local_tables if t not in railway_tables]
    extra_tables = [t for t in railway_tables if t not in local_tables]
    
    print()
    print("[4/5] Comparando estructuras...")
    
    # Generar script SQL
    sql_script = []
    sql_script.append("-- Script generado automáticamente para aplicar cambios de LOCAL a RAILWAY")
    sql_script.append(f"-- Fecha: {datetime.now().strftime('%Y-%m-%d %H:%M:%S')}")
    sql_script.append("")
    sql_script.append("USE railway;")
    sql_script.append("")
    
    # Tablas que faltan completamente
    if missing_tables:
        sql_script.append("-- ========================================")
        sql_script.append("-- TABLAS QUE FALTAN EN RAILWAY")
        sql_script.append("-- ========================================")
        sql_script.append("")
        for table in missing_tables:
            structure = get_table_structure(local_conn, table)
            if structure.get('create_statement'):
                sql_script.append(f"-- Crear tabla: {table}")
                sql_script.append(structure['create_statement'] + ";")
                sql_script.append("")
    
    # Comparar estructuras de tablas existentes
    common_tables = [t for t in local_tables if t in railway_tables]
    if common_tables:
        sql_script.append("-- ========================================")
        sql_script.append("-- MODIFICACIONES A TABLAS EXISTENTES")
        sql_script.append("-- ========================================")
        sql_script.append("")
        
        for table in common_tables:
            print(f"  Comparando tabla: {table}...")
            local_structure = get_table_structure(local_conn, table)
            railway_structure = get_table_structure(railway_conn, table)
            
            differences = compare_structures(local_structure, railway_structure, table)
            if differences:
                sql_script.append(f"-- Tabla: {table}")
                sql_script.extend(differences)
                sql_script.append("")
    
    # Tablas extra en Railway (solo informativo)
    if extra_tables:
        sql_script.append("-- ========================================")
        sql_script.append("-- TABLAS EN RAILWAY PERO NO EN LOCAL (solo informativo)")
        sql_script.append("-- ========================================")
        for table in extra_tables:
            sql_script.append(f"-- {table}")
        sql_script.append("")
    
    print("[5/5] Generando script SQL...")
    
    # Guardar script
    output_file = "aplicar_cambios_railway.sql"
    with open(output_file, 'w', encoding='utf-8') as f:
        f.write('\n'.join(sql_script))
    
    print()
    print("=" * 60)
    print("COMPARACIÓN COMPLETADA")
    print("=" * 60)
    print()
    print(f"Tablas en LOCAL: {len(local_tables)}")
    print(f"Tablas en RAILWAY: {len(railway_tables)}")
    print(f"Tablas faltantes en RAILWAY: {len(missing_tables)}")
    print(f"Tablas extra en RAILWAY: {len(extra_tables)}")
    print()
    print(f"Script SQL generado: {output_file}")
    print()
    
    if missing_tables:
        print("Tablas que faltan en Railway:")
        for table in missing_tables:
            print(f"  - {table}")
    
    # Cerrar conexiones
    local_conn.close()
    railway_conn.close()
    
    print()
    print("¡Listo! Revisa el archivo aplicar_cambios_railway.sql")

if __name__ == "__main__":
    try:
        main()
    except KeyboardInterrupt:
        print("\n\nOperación cancelada por el usuario")
        sys.exit(1)
    except Exception as e:
        print(f"\nERROR: {e}")
        import traceback
        traceback.print_exc()
        sys.exit(1)

