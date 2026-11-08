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
        DefaultTableModel modelo = new DefaultTableModel(null, titulos);

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
}
