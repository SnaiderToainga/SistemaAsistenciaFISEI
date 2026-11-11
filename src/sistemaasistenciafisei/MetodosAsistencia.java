/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package sistemaasistenciafisei;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;

/**
 *
 * @author User
 */
public class MetodosAsistencia {

    private Conexion con = new Conexion();

    // 1. Cargar en la tabla las asistencias registradas en la materia, parcial y sesión actual
    public void cargarMarcacionesSesion(int idMateria, String parcial, int sesion, JTable tabla) {
        String[] titulos = {"Cédula", "Nombres", "Apellidos", "Fecha", "Hora", "Estado"};
        DefaultTableModel modelo = new DefaultTableModel(null, titulos) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        String sql = "SELECT e.cedula, e.nombres, e.apellidos, a.fecha, a.hora, a.estado "
                   + "FROM asistencias a "
                   + "JOIN estudiantes e ON a.id_estudiante = e.id "
                   + "WHERE a.id_materia = ? AND a.parcial = ? AND a.sesion = ? "
                   + "ORDER BY a.hora DESC";

        try (Connection cn = con.conectar();
             PreparedStatement ps = cn.prepareStatement(sql)) {

            ps.setInt(1, idMateria);
            ps.setString(2, parcial);
            ps.setInt(3, sesion);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    modelo.addRow(new Object[]{
                        rs.getString("cedula"),
                        rs.getString("nombres"),
                        rs.getString("apellidos"),
                        rs.getString("fecha"),
                        rs.getString("hora"),
                        rs.getString("estado")
                    });
                }
            }
            tabla.setModel(modelo);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(null, "Error al cargar marcaciones: " + e.getMessage());
        }
    }

    // 2. Registrar la marcación al acercar el llavero RFID validando matrícula por materia
    public void registrarMarcacion(String uid, int idMateria, String parcial, int sesion, JTable tabla, JLabel lblMensaje) {
        String codigoUid = uid.replaceAll("[^0-9]", "").trim();
        if (codigoUid.isEmpty()) {
            return;
        }

        String sqlEstudiante = "SELECT id, nombres, apellidos FROM estudiantes WHERE uid_rfid = ?";
        String sqlMatricula = "SELECT id FROM matriculas WHERE id_estudiante = ? AND id_materia = ?";
        String sqlVerificar = "SELECT id FROM asistencias WHERE id_estudiante = ? AND id_materia = ? AND parcial = ? AND sesion = ?";
        String sqlInsertar = "INSERT INTO asistencias (id_estudiante, id_materia, parcial, sesion, fecha, hora, estado) "
                           + "VALUES (?, ?, ?, ?, CURDATE(), CURTIME(), 'PRESENTE')";

        try (Connection cn = con.conectar()) {
            if (cn == null) {
                return;
            }

            // a) Buscar si el UID pertenece a un estudiante registrado
            int idEstudiante;
            String alumno;
            try (PreparedStatement psEst = cn.prepareStatement(sqlEstudiante)) {
                psEst.setString(1, codigoUid);
                try (ResultSet rsEst = psEst.executeQuery()) {
                    if (!rsEst.next()) {
                        lblMensaje.setText("Tarjeta no reconocida / Estudiante no matriculado");
                        return;
                    }
                    idEstudiante = rsEst.getInt("id");
                    alumno = rsEst.getString("nombres") + " " + rsEst.getString("apellidos");
                }
            }

            // b) Verificar que el estudiante esté matriculado en la materia seleccionada
            try (PreparedStatement psMat = cn.prepareStatement(sqlMatricula)) {
                psMat.setInt(1, idEstudiante);
                psMat.setInt(2, idMateria);
                try (ResultSet rsMat = psMat.executeQuery()) {
                    if (!rsMat.next()) {
                        lblMensaje.setText("Estudiante no matriculado en esta materia");
                        return;
                    }
                }
            }

            // c) Verificar que no haya marcado previamente en esa misma materia, parcial y sesión
            try (PreparedStatement psVer = cn.prepareStatement(sqlVerificar)) {
                psVer.setInt(1, idEstudiante);
                psVer.setInt(2, idMateria);
                psVer.setString(3, parcial);
                psVer.setInt(4, sesion);
                try (ResultSet rsVer = psVer.executeQuery()) {
                    if (rsVer.next()) {
                        lblMensaje.setText("Estudiante ya marcó asistencia en esta sesión");
                        return;
                    }
                }
            }

            // d) Registrar la asistencia en MySQL con su id_materia
            try (PreparedStatement psIns = cn.prepareStatement(sqlInsertar)) {
                psIns.setInt(1, idEstudiante);
                psIns.setInt(2, idMateria);
                psIns.setString(3, parcial);
                psIns.setInt(4, sesion);
                psIns.executeUpdate();
            }

            lblMensaje.setText("Asistencia registrada: " + alumno + " (Sesión " + sesion + ")");
            cargarMarcacionesSesion(idMateria, parcial, sesion, tabla);

        } catch (Exception e) {
            JOptionPane.showMessageDialog(null, "Error al registrar asistencia: " + e.getMessage());
        }
    }
}

