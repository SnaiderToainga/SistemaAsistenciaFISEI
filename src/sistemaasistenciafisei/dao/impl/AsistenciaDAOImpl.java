package sistemaasistenciafisei.dao.impl;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import sistemaasistenciafisei.conexion.Conexion;
import sistemaasistenciafisei.dao.AsistenciaDAO;
import sistemaasistenciafisei.modelo.Asistencia;

/**
 * Implementación JDBC de AsistenciaDAO.
 */
public class AsistenciaDAOImpl implements AsistenciaDAO {

    @Override
    public List<Asistencia> listarPorSesion(int idMateria, String parcial, int sesion) throws SQLException {
        List<Asistencia> lista = new ArrayList<>();
        String sql = "SELECT e.cedula, e.nombres, e.apellidos, a.fecha, a.hora, a.estado "
                   + "FROM asistencias a "
                   + "JOIN estudiantes e ON a.id_estudiante = e.id "
                   + "WHERE a.id_materia = ? AND a.parcial = ? AND a.sesion = ? "
                   + "ORDER BY a.hora DESC";

        try (Connection cn = Conexion.getConexion();
             PreparedStatement ps = cn.prepareStatement(sql)) {

            ps.setInt(1, idMateria);
            ps.setString(2, parcial);
            ps.setInt(3, sesion);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Asistencia asis = new Asistencia();
                    asis.setCedula(rs.getString("cedula"));
                    asis.setNombres(rs.getString("nombres"));
                    asis.setApellidos(rs.getString("apellidos"));
                    asis.setFecha(rs.getDate("fecha"));
                    asis.setHora(rs.getTime("hora"));
                    asis.setEstado(rs.getString("estado"));
                    lista.add(asis);
                }
            }
        }
        return lista;
    }

    @Override
    public boolean estaMatriculado(int idEstudiante, int idMateria) throws SQLException {
        String sql = "SELECT id FROM matriculas WHERE id_estudiante = ? AND id_materia = ?";
        try (Connection cn = Conexion.getConexion();
             PreparedStatement ps = cn.prepareStatement(sql)) {

            ps.setInt(1, idEstudiante);
            ps.setInt(2, idMateria);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        }
    }

    @Override
    public boolean existeMarcacion(int idEstudiante, int idMateria, String parcial, int sesion) throws SQLException {
        String sql = "SELECT id FROM asistencias WHERE id_estudiante = ? AND id_materia = ? AND parcial = ? AND sesion = ?";
        try (Connection cn = Conexion.getConexion();
             PreparedStatement ps = cn.prepareStatement(sql)) {

            ps.setInt(1, idEstudiante);
            ps.setInt(2, idMateria);
            ps.setString(3, parcial);
            ps.setInt(4, sesion);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        }
    }

    @Override
    public boolean registrarMarcacion(int idEstudiante, int idMateria, String parcial, int sesion) throws SQLException {
        String sql = "INSERT INTO asistencias (id_estudiante, id_materia, parcial, sesion, fecha, hora, estado) "
                   + "VALUES (?, ?, ?, ?, CURDATE(), CURTIME(), 'PRESENTE')";

        try (Connection cn = Conexion.getConexion();
             PreparedStatement ps = cn.prepareStatement(sql)) {

            ps.setInt(1, idEstudiante);
            ps.setInt(2, idMateria);
            ps.setString(3, parcial);
            ps.setInt(4, sesion);
            return ps.executeUpdate() > 0;
        }
    }

    @Override
    public int obtenerMaxSesion(int idMateria, String parcial) throws SQLException {
        int maxSesion = 5;
        String sql = "SELECT MAX(sesion) AS max_sesion FROM asistencias WHERE id_materia = ? AND parcial = ?";
        try (Connection cn = Conexion.getConexion();
             PreparedStatement ps = cn.prepareStatement(sql)) {

            ps.setInt(1, idMateria);
            ps.setString(2, parcial);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    int valor = rs.getInt("max_sesion");
                    if (valor > maxSesion) {
                        maxSesion = valor;
                    }
                }
            }
        }
        return maxSesion;
    }
}
