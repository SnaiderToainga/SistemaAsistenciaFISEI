package sistemaasistenciafisei.dao.impl;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import sistemaasistenciafisei.conexion.Conexion;
import sistemaasistenciafisei.dao.EstudianteDAO;
import sistemaasistenciafisei.modelo.Estudiante;

/**
 * Implementación JDBC de EstudianteDAO con control transaccional para matrículas.
 */
public class EstudianteDAOImpl implements EstudianteDAO {

    @Override
    public List<Estudiante> listarTodos() throws SQLException {
        List<Estudiante> lista = new ArrayList<>();
        String sql = "SELECT e.id, e.uid_rfid, e.cedula, e.nombres, e.apellidos, e.carrera, "
                   + "       COALESCE(GROUP_CONCAT(m.nombre ORDER BY m.id SEPARATOR ', '), 'Sin matrícula') AS materias "
                   + "FROM estudiantes e "
                   + "LEFT JOIN matriculas mat ON e.id = mat.id_estudiante "
                   + "LEFT JOIN materias m ON mat.id_materia = m.id "
                   + "GROUP BY e.id, e.uid_rfid, e.cedula, e.nombres, e.apellidos, e.carrera "
                   + "ORDER BY e.id DESC";

        try (Connection cn = Conexion.getConexion();
             PreparedStatement ps = cn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                Estudiante est = new Estudiante();
                est.setId(rs.getInt("id"));
                est.setUidRfid(rs.getString("uid_rfid"));
                est.setCedula(rs.getString("cedula"));
                est.setNombres(rs.getString("nombres"));
                est.setApellidos(rs.getString("apellidos"));
                est.setCarrera(rs.getString("carrera"));
                est.setMateriasTexto(rs.getString("materias"));
                lista.add(est);
            }
        }
        return lista;
    }

    @Override
    public Estudiante buscarPorId(int id) throws SQLException {
        String sql = "SELECT id, uid_rfid, cedula, nombres, apellidos, carrera FROM estudiantes WHERE id = ?";
        try (Connection cn = Conexion.getConexion();
             PreparedStatement ps = cn.prepareStatement(sql)) {

            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    return null;
                }
                return mapearEstudianteSimple(rs);
            }
        }
    }

    @Override
    public Estudiante buscarPorUid(String uidRfid) throws SQLException {
        String sql = "SELECT id, uid_rfid, cedula, nombres, apellidos, carrera FROM estudiantes WHERE uid_rfid = ?";
        try (Connection cn = Conexion.getConexion();
             PreparedStatement ps = cn.prepareStatement(sql)) {

            ps.setString(1, uidRfid);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    return null;
                }
                return mapearEstudianteSimple(rs);
            }
        }
    }

    @Override
    public Estudiante buscarPorCedula(String cedula) throws SQLException {
        String sql = "SELECT id, uid_rfid, cedula, nombres, apellidos, carrera FROM estudiantes WHERE cedula = ?";
        try (Connection cn = Conexion.getConexion();
             PreparedStatement ps = cn.prepareStatement(sql)) {

            ps.setString(1, cedula);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    return null;
                }
                return mapearEstudianteSimple(rs);
            }
        }
    }

    @Override
    public boolean insertar(Estudiante estudiante, List<Integer> materiasIds) throws SQLException {
        String sqlEst = "INSERT INTO estudiantes (uid_rfid, cedula, nombres, apellidos, carrera) VALUES (?, ?, ?, ?, ?)";
        Connection cn = null;
        try {
            cn = Conexion.getConexion();
            cn.setAutoCommit(false);

            int idGenerado = -1;
            try (PreparedStatement ps = cn.prepareStatement(sqlEst, Statement.RETURN_GENERATED_KEYS)) {
                ps.setString(1, estudiante.getUidRfid());
                ps.setString(2, estudiante.getCedula());
                ps.setString(3, estudiante.getNombres());
                ps.setString(4, estudiante.getApellidos());
                ps.setString(5, estudiante.getCarrera() != null ? estudiante.getCarrera() : "Ingeniería de Software");

                int filas = ps.executeUpdate();
                if (filas <= 0) {
                    cn.rollback();
                    return false;
                }

                try (ResultSet rsKeys = ps.getGeneratedKeys()) {
                    if (rsKeys.next()) {
                        idGenerado = rsKeys.getInt(1);
                        estudiante.setId(idGenerado);
                    }
                }
            }

            if (idGenerado > 0 && materiasIds != null && !materiasIds.isEmpty()) {
                String sqlMat = "INSERT IGNORE INTO matriculas (id_estudiante, id_materia) VALUES (?, ?)";
                try (PreparedStatement psMat = cn.prepareStatement(sqlMat)) {
                    for (Integer idMat : materiasIds) {
                        psMat.setInt(1, idGenerado);
                        psMat.setInt(2, idMat);
                        psMat.executeUpdate();
                    }
                }
            }

            cn.commit();
            return true;

        } catch (SQLException ex) {
            if (cn != null) {
                try {
                    cn.rollback();
                } catch (SQLException ignored) {
                }
            }
            throw ex;
        } finally {
            if (cn != null) {
                try {
                    cn.setAutoCommit(true);
                    cn.close();
                } catch (SQLException ignored) {
                }
            }
        }
    }

    @Override
    public boolean actualizar(Estudiante estudiante, List<Integer> materiasIds) throws SQLException {
        String sqlEst = "UPDATE estudiantes SET uid_rfid = ?, cedula = ?, nombres = ?, apellidos = ?, carrera = ? WHERE id = ?";
        Connection cn = null;
        try {
            cn = Conexion.getConexion();
            cn.setAutoCommit(false);

            try (PreparedStatement ps = cn.prepareStatement(sqlEst)) {
                ps.setString(1, estudiante.getUidRfid());
                ps.setString(2, estudiante.getCedula());
                ps.setString(3, estudiante.getNombres());
                ps.setString(4, estudiante.getApellidos());
                ps.setString(5, estudiante.getCarrera() != null ? estudiante.getCarrera() : "Ingeniería de Software");
                ps.setInt(6, estudiante.getId());

                int filas = ps.executeUpdate();
                if (filas <= 0) {
                    cn.rollback();
                    return false;
                }
            }

            // Sincronizar materias soportadas (1 = Computación Visual, 2 = Metodologías Ágiles)
            int[] materiasConocidas = {1, 2};
            String sqlInsertMat = "INSERT IGNORE INTO matriculas (id_estudiante, id_materia) VALUES (?, ?)";
            String sqlDeleteMat = "DELETE FROM matriculas WHERE id_estudiante = ? AND id_materia = ?";
            String sqlDeleteAsis = "DELETE FROM asistencias WHERE id_estudiante = ? AND id_materia = ?";

            for (int idMat : materiasConocidas) {
                boolean matriculado = materiasIds != null && materiasIds.contains(idMat);
                if (matriculado) {
                    try (PreparedStatement psIns = cn.prepareStatement(sqlInsertMat)) {
                        psIns.setInt(1, estudiante.getId());
                        psIns.setInt(2, idMat);
                        psIns.executeUpdate();
                    }
                } else {
                    try (PreparedStatement psDelMat = cn.prepareStatement(sqlDeleteMat);
                         PreparedStatement psDelAsis = cn.prepareStatement(sqlDeleteAsis)) {
                        psDelMat.setInt(1, estudiante.getId());
                        psDelMat.setInt(2, idMat);
                        psDelMat.executeUpdate();

                        psDelAsis.setInt(1, estudiante.getId());
                        psDelAsis.setInt(2, idMat);
                        psDelAsis.executeUpdate();
                    }
                }
            }

            cn.commit();
            return true;

        } catch (SQLException ex) {
            if (cn != null) {
                try {
                    cn.rollback();
                } catch (SQLException ignored) {
                }
            }
            throw ex;
        } finally {
            if (cn != null) {
                try {
                    cn.setAutoCommit(true);
                    cn.close();
                } catch (SQLException ignored) {
                }
            }
        }
    }

    @Override
    public boolean eliminar(int idEstudiante) throws SQLException {
        String sql = "DELETE FROM estudiantes WHERE id = ?";
        try (Connection cn = Conexion.getConexion();
             PreparedStatement ps = cn.prepareStatement(sql)) {

            ps.setInt(1, idEstudiante);
            return ps.executeUpdate() > 0;
        }
    }

    @Override
    public List<Integer> obtenerMateriasPorEstudiante(int idEstudiante) throws SQLException {
        List<Integer> materias = new ArrayList<>();
        String sql = "SELECT id_materia FROM matriculas WHERE id_estudiante = ?";
        try (Connection cn = Conexion.getConexion();
             PreparedStatement ps = cn.prepareStatement(sql)) {

            ps.setInt(1, idEstudiante);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    materias.add(rs.getInt("id_materia"));
                }
            }
        }
        return materias;
    }

    private Estudiante mapearEstudianteSimple(ResultSet rs) throws SQLException {
        Estudiante est = new Estudiante();
        est.setId(rs.getInt("id"));
        est.setUidRfid(rs.getString("uid_rfid"));
        est.setCedula(rs.getString("cedula"));
        est.setNombres(rs.getString("nombres"));
        est.setApellidos(rs.getString("apellidos"));
        est.setCarrera(rs.getString("carrera"));
        return est;
    }
}
