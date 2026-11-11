package sistemaasistenciafisei.dao.impl;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import sistemaasistenciafisei.conexion.Conexion;
import sistemaasistenciafisei.dao.ReporteDAO;
import sistemaasistenciafisei.modelo.ReporteAsistenciaDTO;

/**
 * Implementación JDBC de ReporteDAO para cálculos académicos y consolidados FISEI.
 */
public class ReporteDAOImpl implements ReporteDAO {

    @Override
    public List<ReporteAsistenciaDTO> obtenerConsolidadoParcial(int idMateria, String parcial, int totalSesiones) throws SQLException {
        if (totalSesiones < 5) {
            totalSesiones = 5;
        }
        List<ReporteAsistenciaDTO> lista = new ArrayList<>();
        String sql = "SELECT e.id, e.cedula, e.nombres, e.apellidos, "
                   + "       COUNT(a.id) AS total_asistencias "
                   + "FROM estudiantes e "
                   + "INNER JOIN matriculas m ON e.id = m.id_estudiante AND m.id_materia = ? "
                   + "LEFT JOIN asistencias a ON e.id = a.id_estudiante "
                   + "     AND a.id_materia = m.id_materia "
                   + "     AND a.parcial = ? "
                   + "     AND a.estado = 'PRESENTE' "
                   + "GROUP BY e.id, e.cedula, e.nombres, e.apellidos "
                   + "ORDER BY e.apellidos ASC, e.nombres ASC";

        try (Connection cn = Conexion.getConexion();
             PreparedStatement ps = cn.prepareStatement(sql)) {

            ps.setInt(1, idMateria);
            ps.setString(2, parcial);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    ReporteAsistenciaDTO dto = new ReporteAsistenciaDTO();
                    dto.setIdEstudiante(rs.getInt("id"));
                    dto.setCedula(rs.getString("cedula"));
                    dto.setNombreCompleto(rs.getString("nombres") + " " + rs.getString("apellidos"));
                    dto.setTotalAsistencias(rs.getInt("total_asistencias"));
                    dto.setTotalSesiones(totalSesiones);
                    dto.calcularEstadoParcial();
                    lista.add(dto);
                }
            }
        }
        return lista;
    }

    @Override
    public List<ReporteAsistenciaDTO> obtenerPromedioParciales(int idMateria, int sesionesP1, int sesionesP2) throws SQLException {
        int s1 = sesionesP1 < 5 ? 5 : sesionesP1;
        int s2 = sesionesP2 < 5 ? 5 : sesionesP2;
        List<ReporteAsistenciaDTO> lista = new ArrayList<>();

        String sql = "SELECT e.id, e.cedula, e.nombres, e.apellidos, "
                   + "       SUM(CASE WHEN a.parcial = 'Parcial 1' THEN 1 ELSE 0 END) AS asist_p1, "
                   + "       SUM(CASE WHEN a.parcial = 'Parcial 2' THEN 1 ELSE 0 END) AS asist_p2 "
                   + "FROM estudiantes e "
                   + "INNER JOIN matriculas m ON e.id = m.id_estudiante AND m.id_materia = ? "
                   + "LEFT JOIN asistencias a ON e.id = a.id_estudiante "
                   + "     AND a.id_materia = m.id_materia "
                   + "     AND a.parcial IN ('Parcial 1', 'Parcial 2') "
                   + "     AND a.estado = 'PRESENTE' "
                   + "GROUP BY e.id, e.cedula, e.nombres, e.apellidos "
                   + "ORDER BY e.apellidos ASC, e.nombres ASC";

        try (Connection cn = Conexion.getConexion();
             PreparedStatement ps = cn.prepareStatement(sql)) {

            ps.setInt(1, idMateria);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    ReporteAsistenciaDTO dto = new ReporteAsistenciaDTO();
                    dto.setIdEstudiante(rs.getInt("id"));
                    dto.setCedula(rs.getString("cedula"));
                    dto.setNombreCompleto(rs.getString("nombres") + " " + rs.getString("apellidos"));
                    dto.setAsistP1(rs.getInt("asist_p1"));
                    dto.setAsistP2(rs.getInt("asist_p2"));
                    dto.setSesionesP1(s1);
                    dto.setSesionesP2(s2);
                    dto.calcularEstadoPromedio();
                    lista.add(dto);
                }
            }
        }
        return lista;
    }
}
