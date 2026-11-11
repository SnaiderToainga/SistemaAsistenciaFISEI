package sistemaasistenciafisei.controlador;

import java.sql.SQLException;
import java.util.List;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;
import sistemaasistenciafisei.dao.AsistenciaDAO;
import sistemaasistenciafisei.dao.EstudianteDAO;
import sistemaasistenciafisei.dao.impl.AsistenciaDAOImpl;
import sistemaasistenciafisei.dao.impl.EstudianteDAOImpl;
import sistemaasistenciafisei.modelo.Asistencia;
import sistemaasistenciafisei.modelo.Estudiante;

/**
 * Controlador para la gestión y registro de asistencias mediante AsistenciaDAO y EstudianteDAO.
 */
public class ControladorAsistencia {

    private final AsistenciaDAO asistenciaDAO;
    private final EstudianteDAO estudianteDAO;

    public ControladorAsistencia() {
        this(new AsistenciaDAOImpl(), new EstudianteDAOImpl());
    }

    public ControladorAsistencia(AsistenciaDAO asistenciaDAO, EstudianteDAO estudianteDAO) {
        this.asistenciaDAO = asistenciaDAO;
        this.estudianteDAO = estudianteDAO;
    }

    /**
     * 1. Cargar en la tabla las asistencias registradas en la materia, parcial y sesión actual.
     */
    public void cargarMarcacionesSesion(int idMateria, String parcial, int sesion, JTable tabla) {
        String[] titulos = {"Cédula", "Nombres", "Apellidos", "Fecha", "Hora", "Estado"};
        DefaultTableModel modelo = new DefaultTableModel(null, titulos) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        try {
            List<Asistencia> lista = asistenciaDAO.listarPorSesion(idMateria, parcial, sesion);
            for (Asistencia a : lista) {
                modelo.addRow(new Object[]{
                    a.getCedula(),
                    a.getNombres(),
                    a.getApellidos(),
                    a.getFecha() != null ? a.getFecha().toString() : "",
                    a.getHora() != null ? a.getHora().toString() : "",
                    a.getEstado()
                });
            }
            tabla.setModel(modelo);
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null, "Error al cargar marcaciones: " + e.getMessage(), "Error BD", JOptionPane.ERROR_MESSAGE);
        }
    }

    /**
     * 2. Registrar la marcación al acercar el llavero RFID validando matrícula por materia.
     */
    public void registrarMarcacion(String uid, int idMateria, String parcial, int sesion, JTable tabla, JLabel lblMensaje) {
        String codigoUid = uid.replaceAll("[^0-9]", "").trim();
        if (codigoUid.isEmpty()) {
            return;
        }

        try {
            // a) Buscar si el UID pertenece a un estudiante registrado
            Estudiante estudiante = estudianteDAO.buscarPorUid(codigoUid);
            if (estudiante == null) {
                lblMensaje.setText("Tarjeta no reconocida / Estudiante no matriculado");
                return;
            }

            int idEstudiante = estudiante.getId();
            String alumno = estudiante.getNombres() + " " + estudiante.getApellidos();

            // b) Verificar que el estudiante esté matriculado en la materia seleccionada
            if (!asistenciaDAO.estaMatriculado(idEstudiante, idMateria)) {
                lblMensaje.setText("Estudiante no matriculado en esta materia");
                return;
            }

            // c) Verificar que no haya marcado previamente en esa misma materia, parcial y sesión
            if (asistenciaDAO.existeMarcacion(idEstudiante, idMateria, parcial, sesion)) {
                lblMensaje.setText("Estudiante ya marcó asistencia en esta sesión");
                return;
            }

            // d) Registrar la asistencia en la base de datos
            asistenciaDAO.registrarMarcacion(idEstudiante, idMateria, parcial, sesion);

            lblMensaje.setText("Asistencia registrada: " + alumno + " (Sesión " + sesion + ")");
            cargarMarcacionesSesion(idMateria, parcial, sesion, tabla);

        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null, "Error al registrar asistencia: " + e.getMessage(), "Error BD", JOptionPane.ERROR_MESSAGE);
        }
    }
}
