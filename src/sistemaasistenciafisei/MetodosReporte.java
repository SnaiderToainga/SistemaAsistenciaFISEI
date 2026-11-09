/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package sistemaasistenciafisei;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import javax.swing.JOptionPane;
import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;
import net.sf.jasperreports.engine.JasperCompileManager;
import net.sf.jasperreports.engine.JasperFillManager;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.JasperReport;
import net.sf.jasperreports.engine.util.JRLoader;
import net.sf.jasperreports.view.JasperViewer;

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

    /**
     * 2. Compilar el reporte JRXML, inyectar la conexión MySQL y abrir el visor JasperViewer.
     * @param parcial "Parcial 1" o "Parcial 2"
     */
    public void exportarPdfJasper(String parcial) {
        Connection cn = null;
        try {
            cn = con.conectar();
            if (cn == null) {
                JOptionPane.showMessageDialog(null, "No se pudo conectar a la base de datos.", "Error de Conexión", JOptionPane.ERROR_MESSAGE);
                return;
            }

            Map<String, Object> parametros = new HashMap<>();
            parametros.put("parcial", parcial);

            JasperReport reporte = null;

            // Intentar cargar reporte previamente compilado .jasper
            InputStream jasperStream = getClass().getResourceAsStream("/sistemaasistenciafisei/ReporteEstudiantes.jasper");
            if (jasperStream != null) {
                try {
                    reporte = (JasperReport) JRLoader.loadObject(jasperStream);
                } catch (Exception ignored) {
                    reporte = null;
                }
            }

            // Si no está compilado, compilar dinámicamente desde el JRXML
            if (reporte == null) {
                InputStream jrxmlStream = getClass().getResourceAsStream("/sistemaasistenciafisei/ReporteEstudiantes.jrxml");
                if (jrxmlStream == null) {
                    File archivo = new File("src/sistemaasistenciafisei/ReporteEstudiantes.jrxml");
                    if (archivo.exists()) {
                        jrxmlStream = new FileInputStream(archivo);
                    }
                }

                if (jrxmlStream == null) {
                    JOptionPane.showMessageDialog(null, "No se encontró la plantilla ReporteEstudiantes.jrxml", "Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }

                reporte = JasperCompileManager.compileReport(jrxmlStream);
            }

            // Llenar el reporte con los parámetros y la conexión MySQL
            JasperPrint print = JasperFillManager.fillReport(reporte, parametros, cn);

            if (print == null || print.getPages().isEmpty()) {
                JOptionPane.showMessageDialog(null, "El reporte no arrojó registros para " + parcial + ".", "Aviso", JOptionPane.INFORMATION_MESSAGE);
                return;
            }

            // Abrir visor JasperViewer sin cerrar la aplicación principal (false)
            JasperViewer viewer = new JasperViewer(print, false);
            viewer.setTitle("Reporte de Asistencia FISEI - " + parcial);
            viewer.setVisible(true);

        } catch (Exception e) {
            JOptionPane.showMessageDialog(null, "Error al generar reporte Jasper: " + e.getMessage(), "Error JasperReports", JOptionPane.ERROR_MESSAGE);
        } finally {
            if (cn != null) {
                try {
                    cn.close();
                } catch (Exception ignored) {
                }
            }
        }
    }
}
