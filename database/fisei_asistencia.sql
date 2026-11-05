-- Creación de la base de datos
CREATE DATABASE IF NOT EXISTS fisei_asistencia CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE fisei_asistencia;

-- 1. Tabla de Usuarios y Roles (Para el Login de Cristian según la Guía PAE)
CREATE TABLE IF NOT EXISTS usuarios (
    id INT AUTO_INCREMENT PRIMARY KEY,
    usuario VARCHAR(50) NOT NULL UNIQUE,
    clave VARCHAR(100) NOT NULL,
    nombre_completo VARCHAR(100) NOT NULL,
    perfil VARCHAR(30) NOT NULL -- 'Administrador' o 'Docente'
);

-- 2. Tabla de Estudiantes Matriculados (Módulo de Snaider)
CREATE TABLE IF NOT EXISTS estudiantes (
    id INT AUTO_INCREMENT PRIMARY KEY,
    uid_rfid VARCHAR(30) NOT NULL UNIQUE, -- Código leído del llavero RFID
    cedula VARCHAR(10) NOT NULL UNIQUE,
    nombres VARCHAR(60) NOT NULL,
    apellidos VARCHAR(60) NOT NULL,
    carrera VARCHAR(60) DEFAULT 'Ingeniería de Software'
);

-- 3. Tabla de Asistencias por Sesión y Parcial (Módulo de César y Joel)
CREATE TABLE IF NOT EXISTS asistencias (
    id INT AUTO_INCREMENT PRIMARY KEY,
    id_estudiante INT NOT NULL,
    parcial VARCHAR(15) NOT NULL,         -- 'Parcial 1' o 'Parcial 2'
    sesion INT NOT NULL,                  -- Sesiones del 1 al 5
    fecha DATE NOT NULL,
    hora TIME NOT NULL,
    estado VARCHAR(15) NOT NULL,          -- 'PRESENTE' o 'FALTA'
    FOREIGN KEY (id_estudiante) REFERENCES estudiantes(id) ON DELETE CASCADE,
    UNIQUE KEY uq_est_par_ses (id_estudiante, parcial, sesion) -- Evita que un estudiante marque dos veces en la misma sesión
);

-- ==============================================================
-- DATOS INICIALES DE PRUEBA
-- ==============================================================

-- Usuarios base para el login de Cristian:
INSERT INTO usuarios (usuario, clave, nombre_completo, perfil) VALUES
('admin', '1234', 'Administrador FISEI', 'Administrador'),
('docente', '1234', 'Ing. Docente Titular', 'Docente')
ON DUPLICATE KEY UPDATE id=id;

-- 3 Estudiantes de prueba para la simulación inicial:
INSERT INTO estudiantes (uid_rfid, cedula, nombres, apellidos, carrera) VALUES
('1001', '1801234567', 'Juan Carlos', 'Pérez Morales', 'Ingeniería de Software'),
('1002', '1809876543', 'María Belén', 'Gómez Castro', 'Ingeniería de Software'),
('1003', '1805544332', 'Carlos Andrés', 'López Vega', 'Ingeniería de Software')
ON DUPLICATE KEY UPDATE id=id;