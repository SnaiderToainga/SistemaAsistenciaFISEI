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

    // 1. Cargar en la tabla las asistencias registradas en el parcial y sesión actual
    public void cargarMarcacionesSesion(String parcial, int sesion, JTable tabla) {
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
                   + "WHERE a.parcial = ? AND a.sesion = ? "
                   + "ORDER BY a.hora DESC";

        try (Connection cn = con.conectar();
             PreparedStatement ps = cn.prepareStatement(sql)) {

            ps.setString(1, parcial);
            ps.setInt(2, sesion);
            ResultSet rs = ps.executeQuery();

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
            tabla.setModel(modelo);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(null, "Error al cargar marcaciones: " + e.getMessage());
        }
    }

    // 2. Registrar la marcación al acercar el llavero RFID
    public void registrarMarcacion(String uid, String parcial, int sesion, JTable tabla, JLabel lblMensaje) {
        String codigoUid = uid.replaceAll("[^0-9]", "").trim();
        if (codigoUid.isEmpty()) {
            return;
        }

        String sqlEstudiante = "SELECT id, nombres, apellidos FROM estudiantes WHERE uid_rfid = ?";
        String sqlVerificar = "SELECT id FROM asistencias WHERE id_estudiante = ? AND parcial = ? AND sesion = ?";
        String sqlInsertar = "INSERT INTO asistencias (id_estudiante, parcial, sesion, fecha, hora, estado) "
                           + "VALUES (?, ?, ?, CURDATE(), CURTIME(), 'PRESENTE')";

        try (Connection cn = con.conectar()) {
            // a) Buscar si el UID pertenece a un estudiante matriculado
            PreparedStatement psEst = cn.prepareStatement(sqlEstudiante);
            psEst.setString(1, codigoUid);
            ResultSet rsEst = psEst.executeQuery();

            if (!rsEst.next()) {
                lblMensaje.setText("Tarjeta no reconocida / Estudiante no matriculado");
                return;
            }

            int idEstudiante = rsEst.getInt("id");
            String alumno = rsEst.getString("nombres") + " " + rsEst.getString("apellidos");

            // c) Verificar que no haya marcado previamente en ese mismo parcial y sesión
            PreparedStatement psVer = cn.prepareStatement(sqlVerificar);
            psVer.setInt(1, idEstudiante);
            psVer.setString(2, parcial);
            psVer.setInt(3, sesion);
            ResultSet rsVer = psVer.executeQuery();

            if (rsVer.next()) {
                lblMensaje.setText("Estudiante ya marcó asistencia en esta sesión");
                return;
            }

            // e) Registrar la asistencia en MySQL
            PreparedStatement psIns = cn.prepareStatement(sqlInsertar);
            psIns.setInt(1, idEstudiante);
            psIns.setString(2, parcial);
            psIns.setInt(3, sesion);
            psIns.executeUpdate();

            lblMensaje.setText("Asistencia registrada: " + alumno + " (Sesión " + sesion + ")");
            cargarMarcacionesSesion(parcial, sesion, tabla);

        } catch (Exception e) {
            JOptionPane.showMessageDialog(null, "Error al registrar asistencia: " + e.getMessage());
        }
    }
}

