package sistemaasistenciafisei.controlador;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
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
import sistemaasistenciafisei.conexion.Conexion;
import sistemaasistenciafisei.dao.AsistenciaDAO;
import sistemaasistenciafisei.dao.ReporteDAO;
import sistemaasistenciafisei.dao.impl.AsistenciaDAOImpl;
import sistemaasistenciafisei.dao.impl.ReporteDAOImpl;
import sistemaasistenciafisei.modelo.ReporteAsistenciaDTO;

/**
 * Controlador de Reportes y Estados Académicos FISEI con JasperReports y ReporteDAO.
 */
public class ControladorReporte {

    private final ReporteDAO reporteDAO;
    private final AsistenciaDAO asistenciaDAO;

    public ControladorReporte() {
        this(new ReporteDAOImpl(), new AsistenciaDAOImpl());
    }

    public ControladorReporte(ReporteDAO reporteDAO, AsistenciaDAO asistenciaDAO) {
        this.reporteDAO = reporteDAO;
        this.asistenciaDAO = asistenciaDAO;
    }

    /**
     * Obtener el total de sesiones registradas para una materia y parcial (mínimo 5).
     */
    public int obtenerSesionesRegistradas(int idMateria, String parcial) {
        try {
            return asistenciaDAO.obtenerMaxSesion(idMateria, parcial);
        } catch (SQLException e) {
            System.err.println("Error al obtener sesiones: " + e.getMessage());
            return 5;
        }
    }

    public int obtenerSesionesRegistradas(String parcial) {
        return obtenerSesionesRegistradas(1, parcial);
    }

    public void calcularConsolidado(int idMateria, String parcial, JTable tabla) {
        calcularConsolidado(idMateria, parcial, obtenerSesionesRegistradas(idMateria, parcial), tabla);
    }

    /**
     * 1. Calcular el consolidado de asistencias por materia y determinar el estado académico FISEI.
     */
    public void calcularConsolidado(int idMateria, String parcial, int totalSesiones, JTable tabla) {
        String[] titulos = {"Cédula", "Estudiante", "Asistencia", "Porcentaje", "Estado"};
        DefaultTableModel modelo = new DefaultTableModel(null, titulos) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        try {
            List<ReporteAsistenciaDTO> lista = reporteDAO.obtenerConsolidadoParcial(idMateria, parcial, totalSesiones);
            for (ReporteAsistenciaDTO dto : lista) {
                modelo.addRow(new Object[]{
                    dto.getCedula(),
                    dto.getNombreCompleto(),
                    dto.getAsistenciaTexto(),
                    dto.getPorcentajeFormateado(),
                    dto.getEstado()
                });
            }
            tabla.setModel(modelo);
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null, "Error al calcular el consolidado: " + e.getMessage(), "Error BD", JOptionPane.ERROR_MESSAGE);
        }
    }

    /**
     * 2. Calcular el promedio de asistencia de los dos parciales (Parcial 1 y Parcial 2) por materia.
     */
    public void calcularPromedioParciales(int idMateria, int sesionesP1, int sesionesP2, JTable tabla) {
        String[] titulos = {"Cédula", "Estudiante", "Asistencia (P1 + P2)", "Promedio", "Estado"};
        DefaultTableModel modelo = new DefaultTableModel(null, titulos) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        try {
            List<ReporteAsistenciaDTO> lista = reporteDAO.obtenerPromedioParciales(idMateria, sesionesP1, sesionesP2);
            for (ReporteAsistenciaDTO dto : lista) {
                modelo.addRow(new Object[]{
                    dto.getCedula(),
                    dto.getNombreCompleto(),
                    dto.getAsistenciaPromedioTexto(),
                    dto.getPromedioFormateado(),
                    dto.getEstado()
                });
            }
            tabla.setModel(modelo);
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null, "Error al calcular el promedio de parciales: " + e.getMessage(), "Error BD", JOptionPane.ERROR_MESSAGE);
        }
    }

    /**
     * 3. Mostrar un resumen de la materia y parcial con el conteo y la lista de estudiantes por estado.
     */
    public void mostrarResumen(String parcial, String nombreMateria, JTable tabla) {
        int total = tabla.getRowCount();
        String titulo = "Resumen " + nombreMateria + " - " + parcial;
        if (total == 0) {
            JOptionPane.showMessageDialog(null, "No hay estudiantes registrados en " + nombreMateria + " para generar el resumen.", titulo, JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        int aprobados = 0;
        int enRiesgo = 0;
        int reprobados = 0;

        StringBuilder listaAprobados = new StringBuilder();
        StringBuilder listaEnRiesgo = new StringBuilder();
        StringBuilder listaReprobados = new StringBuilder();

        for (int i = 0; i < total; i++) {
            String nombre = tabla.getValueAt(i, 1).toString();
            String porcentaje = tabla.getValueAt(i, 3).toString();
            String estado = tabla.getValueAt(i, 4).toString();

            String linea = " - " + nombre + " (" + porcentaje + ")\n";
            if ("APROBADO".equals(estado)) {
                aprobados++;
                listaAprobados.append(linea);
            } else if ("EN RIESGO".equals(estado)) {
                enRiesgo++;
                listaEnRiesgo.append(linea);
            } else {
                reprobados++;
                listaReprobados.append(linea);
            }
        }

        StringBuilder mensaje = new StringBuilder();
        mensaje.append("RESUMEN DE ASISTENCIA - ").append(nombreMateria).append(" (").append(parcial).append(")\n");
        mensaje.append("Total de estudiantes: ").append(total).append("\n\n");

        mensaje.append("APROBADOS (").append(aprobados).append("):\n");
        mensaje.append(aprobados > 0 ? listaAprobados.toString() : " - Ninguno\n");

        if (enRiesgo > 0) {
            mensaje.append("\nEN RIESGO (").append(enRiesgo).append("):\n");
            mensaje.append(listaEnRiesgo.toString());
        }

        mensaje.append("\nREPROBADOS (").append(reprobados).append("):\n");
        mensaje.append(reprobados > 0 ? listaReprobados.toString() : " - Ninguno\n");

        JOptionPane.showMessageDialog(null, mensaje.toString(), titulo, JOptionPane.INFORMATION_MESSAGE);
    }

    public void exportarPdfJasper(int idMateria, String nombreMateria, String parcial) {
        exportarPdfJasper(idMateria, nombreMateria, parcial, obtenerSesionesRegistradas(idMateria, parcial));
    }

    public void exportarPdfPromedioJasper(int idMateria, String nombreMateria, int sesionesP1, int sesionesP2) {
        exportarPdfJasperInterno(idMateria, nombreMateria, "Promedio Final", sesionesP1, sesionesP2);
    }

    public void exportarPdfJasper(int idMateria, String nombreMateria, String parcial, int totalSesiones) {
        exportarPdfJasperInterno(idMateria, nombreMateria, parcial, totalSesiones, totalSesiones);
    }

    private void exportarPdfJasperInterno(int idMateria, String nombreMateria, String parcial, int totalSesiones, int totalSesionesP2) {
        if (totalSesiones < 5) {
            totalSesiones = 5;
        }
        if (totalSesionesP2 < 5) {
            totalSesionesP2 = 5;
        }
        Connection cn = null;
        try {
            cn = Conexion.getConexion();
            if (cn == null) {
                JOptionPane.showMessageDialog(null, "No se pudo conectar a la base de datos.", "Error de Conexión", JOptionPane.ERROR_MESSAGE);
                return;
            }

            Map<String, Object> parametros = new HashMap<>();
            parametros.put("idMateria", idMateria);
            parametros.put("nombreMateria", nombreMateria);
            parametros.put("parcial", parcial);
            parametros.put("totalSesiones", totalSesiones);
            parametros.put("totalSesionesP2", totalSesionesP2);

            JasperReport reporte = null;

            // Intentar cargar reporte previamente compilado .jasper
            try (InputStream jasperStream = getClass().getResourceAsStream("/sistemaasistenciafisei/ReporteEstudiantes.jasper")) {
                if (jasperStream != null) {
                    reporte = (JasperReport) JRLoader.loadObject(jasperStream);
                }
            } catch (Exception ignored) {
                reporte = null;
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

                try (InputStream streamCerrar = jrxmlStream) {
                    reporte = JasperCompileManager.compileReport(streamCerrar);
                }
            }

            // Llenar el reporte con los parámetros y la conexión MySQL
            JasperPrint print = JasperFillManager.fillReport(reporte, parametros, cn);

            if (print == null || print.getPages().isEmpty()) {
                JOptionPane.showMessageDialog(null, "El reporte no arrojó registros para " + nombreMateria + " (" + parcial + ").", "Aviso", JOptionPane.INFORMATION_MESSAGE);
                return;
            }

            // Abrir visor JasperViewer sin cerrar la aplicación principal (false)
            JasperViewer viewer = new JasperViewer(print, false);
            viewer.setTitle("Reporte de Asistencia FISEI - " + nombreMateria + " (" + parcial + ")");
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
