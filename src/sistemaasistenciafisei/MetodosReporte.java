/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package sistemaasistenciafisei;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Locale;
import javax.swing.JOptionPane;
import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;

/**
 * Módulo de Reporte y Estados Académicos FISEI con JasperReports
 * @author Joel
 */
public class MetodosReporte {

    private Conexion con = new Conexion();

    /**
     * 1. Calcular el consolidado de asistencias de 5 sesiones y determinar el estado académico FISEI.
     * @param parcial "Parcial 1" o "Parcial 2"
     * @param tabla JTable jtblReporteFinal donde se visualizarán los datos
     */
    public void calcularConsolidado(String parcial, JTable tabla) {
        String[] titulos = {"Cédula", "Estudiante", "Asistencia", "Porcentaje", "Estado"};
        DefaultTableModel modelo = new DefaultTableModel(null, titulos) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        // a) Obtener los estudiantes matriculados y contar cuántas sesiones marcaron (de 5 totales)
        String sql = "SELECT e.id, e.cedula, e.nombres, e.apellidos, "
                   + "       COUNT(a.id) AS total_asistencias "
                   + "FROM estudiantes e "
                   + "LEFT JOIN asistencias a ON e.id = a.id_estudiante AND a.parcial = ? AND a.estado = 'PRESENTE' "
                   + "GROUP BY e.id, e.cedula, e.nombres, e.apellidos "
                   + "ORDER BY e.apellidos ASC, e.nombres ASC";

        try (Connection cn = con.conectar();
             PreparedStatement ps = cn.prepareStatement(sql)) {

            ps.setString(1, parcial);
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                String cedula = rs.getString("cedula");
                String nombres = rs.getString("nombres") + " " + rs.getString("apellidos");
                int totalAsistencias = rs.getInt("total_asistencias");

                // b) Aplicar la fórmula académica FISEI:
                double porcentaje = (totalAsistencias / 5.0) * 100.0;

                // c) Clasificar en los 3 estados académicos normados:
                String estado;
                if (porcentaje >= 80.0) {
                    estado = "APROBADO";
                } else if (porcentaje >= 70.0) {
                    estado = "EN RIESGO";
                } else {
                    estado = "REPROBADO";
                }

                // d) Cargar las columnas en el modelo de la tabla
                modelo.addRow(new Object[]{
                    cedula,
                    nombres,
                    totalAsistencias + " / 5",
                    String.format(Locale.US, "%.1f%%", porcentaje),
                    estado
                });
            }
            tabla.setModel(modelo);

        } catch (Exception e) {
            JOptionPane.showMessageDialog(null, "Error al calcular el consolidado: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}
