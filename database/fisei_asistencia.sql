-- Creación / actualización idempotente de la base de datos FISEI
CREATE DATABASE IF NOT EXISTS fisei_asistencia CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE fisei_asistencia;

-- 1. Tabla de Usuarios y Roles
CREATE TABLE IF NOT EXISTS usuarios (
    id INT AUTO_INCREMENT PRIMARY KEY,
    usuario VARCHAR(50) NOT NULL UNIQUE,
    clave VARCHAR(100) NOT NULL,
    nombre_completo VARCHAR(100) NOT NULL,
    perfil VARCHAR(30) NOT NULL -- 'Administrador' o 'Docente'
);

-- 2. Tabla de Estudiantes Matriculados (UID físico único por estudiante)
CREATE TABLE IF NOT EXISTS estudiantes (
    id INT AUTO_INCREMENT PRIMARY KEY,
    uid_rfid VARCHAR(30) NOT NULL UNIQUE,
    cedula VARCHAR(10) NOT NULL UNIQUE,
    nombres VARCHAR(60) NOT NULL,
    apellidos VARCHAR(60) NOT NULL,
    carrera VARCHAR(60) DEFAULT 'Ingeniería de Software'
);

-- 3. Tabla de Materias precargadas y su docente asignado
CREATE TABLE IF NOT EXISTS materias (
    id INT AUTO_INCREMENT PRIMARY KEY,
    nombre VARCHAR(80) NOT NULL UNIQUE,
    id_docente INT NULL,
    CONSTRAINT fk_materias_docente FOREIGN KEY (id_docente) REFERENCES usuarios(id) ON DELETE SET NULL
);

-- 4. Tabla intermedia de Matrículas (un estudiante puede cursar 1 o ambas materias)
CREATE TABLE IF NOT EXISTS matriculas (
    id INT AUTO_INCREMENT PRIMARY KEY,
    id_estudiante INT NOT NULL,
    id_materia INT NOT NULL,
    CONSTRAINT fk_matriculas_estudiante FOREIGN KEY (id_estudiante) REFERENCES estudiantes(id) ON DELETE CASCADE,
    CONSTRAINT fk_matriculas_materia FOREIGN KEY (id_materia) REFERENCES materias(id) ON DELETE CASCADE,
    UNIQUE KEY uq_est_mat (id_estudiante, id_materia)
);

-- 5. Tabla de Asistencias por Estudiante, Materia, Parcial y Sesión
CREATE TABLE IF NOT EXISTS asistencias (
    id INT AUTO_INCREMENT PRIMARY KEY,
    id_estudiante INT NOT NULL,
    id_materia INT NOT NULL DEFAULT 1,
    parcial VARCHAR(15) NOT NULL,         -- 'Parcial 1' o 'Parcial 2'
    sesion INT NOT NULL,                  -- Sesiones desde 1 (ampliables dinámicamente)
    fecha DATE NOT NULL,
    hora TIME NOT NULL,
    estado VARCHAR(15) NOT NULL,          -- 'PRESENTE' o 'FALTA'
    FOREIGN KEY (id_estudiante) REFERENCES estudiantes(id) ON DELETE CASCADE,
    CONSTRAINT fk_asistencias_materia FOREIGN KEY (id_materia) REFERENCES materias(id) ON DELETE CASCADE,
    UNIQUE KEY uq_est_mat_par_ses (id_estudiante, id_materia, parcial, sesion)
);

-- ==============================================================
-- ACTUALIZACIÓN SEGURA PARA BASES EXISTENTES (CONSERVA DATOS)
-- ==============================================================

-- Agregar columna id_materia si la tabla asistencias venía de la versión anterior
SET @sql = (SELECT IF(
    (SELECT COUNT(*) FROM information_schema.COLUMNS
     WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'asistencias' AND COLUMN_NAME = 'id_materia') = 0,
    'ALTER TABLE asistencias ADD COLUMN id_materia INT NOT NULL DEFAULT 1 AFTER id_estudiante',
    'SELECT 1'
));
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- Crear el nuevo índice único por (estudiante, materia, parcial, sesión) antes de borrar el anterior
SET @sql = (SELECT IF(
    (SELECT COUNT(*) FROM information_schema.STATISTICS
     WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'asistencias' AND INDEX_NAME = 'uq_est_mat_par_ses') = 0,
    'ALTER TABLE asistencias ADD UNIQUE KEY uq_est_mat_par_ses (id_estudiante, id_materia, parcial, sesion)',
    'SELECT 1'
));
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- Eliminar la restricción única anterior si aún existe
SET @sql = (SELECT IF(
    (SELECT COUNT(*) FROM information_schema.STATISTICS
     WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'asistencias' AND INDEX_NAME = 'uq_est_par_ses') > 0,
    'ALTER TABLE asistencias DROP INDEX uq_est_par_ses',
    'SELECT 1'
));
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- ==============================================================
-- DATOS BASE (USUARIOS, MATERIAS Y MIGRACIÓN DE MATRÍCULAS)
-- ==============================================================

-- Usuarios base: mantiene admin y docente existentes, y añade docente2
INSERT IGNORE INTO usuarios (usuario, clave, nombre_completo, perfil) VALUES
('admin', '1234', 'Administrador FISEI', 'Administrador'),
('docente', '1234', 'Ing. Docente Computación Visual', 'Docente'),
('docente2', '1234', 'Ing. Docente Metodologías Ágiles', 'Docente');

-- Precargar las 2 materias y vincularlas con cada docente
INSERT INTO materias (id, nombre, id_docente) VALUES
(1, 'Computación Visual', (SELECT id FROM usuarios WHERE usuario = 'docente' LIMIT 1)),
(2, 'Metodologías Ágiles', (SELECT id FROM usuarios WHERE usuario = 'docente2' LIMIT 1))
ON DUPLICATE KEY UPDATE
    nombre = VALUES(nombre),
    id_docente = COALESCE(id_docente, VALUES(id_docente));

-- Agregar FK hacia materias en asistencias (después de asegurar que materias 1 y 2 existen)
SET @sql = (SELECT IF(
    (SELECT COUNT(*) FROM information_schema.TABLE_CONSTRAINTS
     WHERE CONSTRAINT_SCHEMA = DATABASE() AND TABLE_NAME = 'asistencias' AND CONSTRAINT_NAME = 'fk_asistencias_materia') = 0,
    'ALTER TABLE asistencias ADD CONSTRAINT fk_asistencias_materia FOREIGN KEY (id_materia) REFERENCES materias(id) ON DELETE CASCADE',
    'SELECT 1'
));
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- Solo si la tabla estudiantes está completamente vacía (instalación desde cero), insertar 3 de prueba
INSERT INTO estudiantes (uid_rfid, cedula, nombres, apellidos, carrera)
SELECT tmp.uid_rfid, tmp.cedula, tmp.nombres, tmp.apellidos, tmp.carrera
FROM (
    SELECT '1001' AS uid_rfid, '1801234567' AS cedula, 'Juan Carlos' AS nombres, 'Pérez Morales' AS apellidos, 'Ingeniería de Software' AS carrera
    UNION ALL
    SELECT '1002', '1809876543', 'María Belén', 'Gómez Castro', 'Ingeniería de Software'
    UNION ALL
    SELECT '1003', '1805544332', 'Carlos Andrés', 'López Vega', 'Ingeniería de Software'
) AS tmp
WHERE NOT EXISTS (SELECT 1 FROM estudiantes LIMIT 1);

-- Matricular automáticamente en Computación Visual (id=1) a los estudiantes existentes sin matrícula previa
-- (Conserva intactos los llaveros reales de prueba y los relaciona con sus asistencias actuales)
INSERT IGNORE INTO matriculas (id_estudiante, id_materia)
SELECT e.id, 1
FROM estudiantes e
WHERE NOT EXISTS (
    SELECT 1 FROM matriculas m WHERE m.id_estudiante = e.id
);
