package sistemaasistenciafisei.controlador;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JCheckBox;
import javax.swing.JOptionPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.table.DefaultTableModel;
import sistemaasistenciafisei.dao.EstudianteDAO;
import sistemaasistenciafisei.dao.impl.EstudianteDAOImpl;
import sistemaasistenciafisei.modelo.Estudiante;

/**
 * Controlador para la gestión de estudiantes y matrículas mediante EstudianteDAO.
 */
public class ControladorEstudiante {

    private final EstudianteDAO estudianteDAO;
    private int idSeleccionado = -1;

    public ControladorEstudiante() {
        this(new EstudianteDAOImpl());
    }

    public ControladorEstudiante(EstudianteDAO estudianteDAO) {
        this.estudianteDAO = estudianteDAO;
    }

    public boolean estaEditando() {
        return idSeleccionado != -1;
    }

    public int getIdSeleccionado() {
        return idSeleccionado;
    }

    public void setIdSeleccionado(int idSeleccionado) {
        this.idSeleccionado = idSeleccionado;
    }

    /**
     * 1. Cargar la tabla de estudiantes matriculados usando EstudianteDAO.
     */
    public void cargarTabla(JTable tabla) {
        String[] titulos = {"ID", "UID Llavero", "Cédula", "Nombres", "Apellidos", "Materias"};
        DefaultTableModel modelo = new DefaultTableModel(null, titulos) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        try {
            List<Estudiante> lista = estudianteDAO.listarTodos();
            for (Estudiante e : lista) {
                modelo.addRow(new Object[]{
                    e.getId(),
                    e.getUidRfid(),
                    e.getCedula(),
                    e.getNombres(),
                    e.getApellidos(),
                    e.getMateriasTexto() != null ? e.getMateriasTexto() : "Sin matrícula"
                });
            }
            tabla.setModel(modelo);
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null, "Error al listar estudiantes: " + e.getMessage(), "Error BD", JOptionPane.ERROR_MESSAGE);
        }
    }

    /**
     * 2. Limpiar campos de texto (Botón Nuevo).
     */
    public void limpiarCampos(JTextField txtCedula, JTextField txtNombre, JTextField txtApellidos,
                              JTextField txtRfid, JCheckBox chkVisual, JCheckBox chkAgiles) {
        idSeleccionado = -1;
        txtCedula.setText("");
        txtNombre.setText("");
        txtApellidos.setText("");
        txtRfid.setText("");
        chkVisual.setSelected(false);
        chkAgiles.setSelected(false);
        txtCedula.requestFocus();
    }

    private boolean validarCampos(String cedula, String nombres, String apellidos, String uid,
                                  boolean visual, boolean agiles, JTextField txtCedula) {
        if (cedula.isEmpty() || nombres.isEmpty() || apellidos.isEmpty() || uid.isEmpty()) {
            JOptionPane.showMessageDialog(null, "Debe completar Cédula, Nombres, Apellidos y pasar el llavero.", "Atención", JOptionPane.WARNING_MESSAGE);
            return false;
        }
        if (!cedula.matches("\\d{10}")) {
            JOptionPane.showMessageDialog(null, "La cédula debe tener exactamente 10 dígitos numéricos.", "Atención", JOptionPane.WARNING_MESSAGE);
            txtCedula.requestFocus();
            return false;
        }
        if (!nombres.matches("[a-zA-ZáéíóúÁÉÍÓÚñÑ ]+") || !apellidos.matches("[a-zA-ZáéíóúÁÉÍÓÚñÑ ]+")) {
            JOptionPane.showMessageDialog(null, "Nombres y Apellidos solo deben contener letras.", "Atención", JOptionPane.WARNING_MESSAGE);
            return false;
        }
        if (!visual && !agiles) {
            JOptionPane.showMessageDialog(null, "Debe seleccionar al menos una materia.", "Atención", JOptionPane.WARNING_MESSAGE);
            return false;
        }
        return true;
    }

    private List<Integer> obtenerMateriasSeleccionadas(boolean visual, boolean agiles) {
        List<Integer> mats = new ArrayList<>();
        if (visual) mats.add(1);
        if (agiles) mats.add(2);
        return mats;
    }

    /**
     * 3. Guardar un nuevo estudiante (Botón Guardar).
     */
    public boolean guardar(JTextField txtCedula, JTextField txtNombre, JTextField txtApellidos,
                           JTextField txtRfid, JCheckBox chkVisual, JCheckBox chkAgiles, JTable tabla) {
        String cedula = txtCedula.getText().trim();
        String nombres = txtNombre.getText().trim();
        String apellidos = txtApellidos.getText().trim();
        String uid = txtRfid.getText().replaceAll("[^0-9]", "").trim();

        if (!validarCampos(cedula, nombres, apellidos, uid, chkVisual.isSelected(), chkAgiles.isSelected(), txtCedula)) {
            return false;
        }

        Estudiante estudiante = new Estudiante(0, uid, cedula, nombres, apellidos);
        List<Integer> materiasIds = obtenerMateriasSeleccionadas(chkVisual.isSelected(), chkAgiles.isSelected());

        try {
            if (estudianteDAO.insertar(estudiante, materiasIds)) {
                JOptionPane.showMessageDialog(null, "¡Estudiante registrado exitosamente!");
                limpiarCampos(txtCedula, txtNombre, txtApellidos, txtRfid, chkVisual, chkAgiles);
                cargarTabla(tabla);
                return true;
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null, "Error al registrar (posible cédula o llavero duplicado): " + e.getMessage(), "Error SQL", JOptionPane.ERROR_MESSAGE);
        }
        return false;
    }

    /**
     * 4. Seleccionar fila de la tabla para editar.
     */
    public void seleccionarFila(JTable tabla, JTextField txtRfid, JTextField txtCedula,
                                JTextField txtNombre, JTextField txtApellidos,
                                JCheckBox chkVisual, JCheckBox chkAgiles) {
        int fila = tabla.getSelectedRow();
        if (fila != -1) {
            idSeleccionado = Integer.parseInt(tabla.getValueAt(fila, 0).toString());
            txtRfid.setText(tabla.getValueAt(fila, 1).toString());
            txtCedula.setText(tabla.getValueAt(fila, 2).toString());
            txtNombre.setText(tabla.getValueAt(fila, 3).toString());
            txtApellidos.setText(tabla.getValueAt(fila, 4).toString());

            chkVisual.setSelected(false);
            chkAgiles.setSelected(false);

            try {
                List<Integer> materias = estudianteDAO.obtenerMateriasPorEstudiante(idSeleccionado);
                for (int idMat : materias) {
                    if (idMat == 1) chkVisual.setSelected(true);
                    if (idMat == 2) chkAgiles.setSelected(true);
                }
            } catch (SQLException e) {
                System.err.println("Error al cargar materias del estudiante: " + e.getMessage());
            }
        }
    }

    /**
     * 5. Modificar los datos del estudiante seleccionado (Botón Modificar).
     */
    public boolean modificar(JTextField txtCedula, JTextField txtNombre, JTextField txtApellidos,
                             JTextField txtRfid, JCheckBox chkVisual, JCheckBox chkAgiles, JTable tabla) {
        if (idSeleccionado == -1) {
            JOptionPane.showMessageDialog(null, "Seleccione un estudiante de la tabla para modificar.", "Atención", JOptionPane.WARNING_MESSAGE);
            return false;
        }

        String cedula = txtCedula.getText().trim();
        String nombres = txtNombre.getText().trim();
        String apellidos = txtApellidos.getText().trim();
        String uid = txtRfid.getText().replaceAll("[^0-9]", "").trim();

        if (!validarCampos(cedula, nombres, apellidos, uid, chkVisual.isSelected(), chkAgiles.isSelected(), txtCedula)) {
            return false;
        }

        Estudiante estudiante = new Estudiante(idSeleccionado, uid, cedula, nombres, apellidos);
        List<Integer> materiasIds = obtenerMateriasSeleccionadas(chkVisual.isSelected(), chkAgiles.isSelected());

        try {
            if (estudianteDAO.actualizar(estudiante, materiasIds)) {
                JOptionPane.showMessageDialog(null, "¡Datos del estudiante actualizados correctamente!");
                limpiarCampos(txtCedula, txtNombre, txtApellidos, txtRfid, chkVisual, chkAgiles);
                cargarTabla(tabla);
                return true;
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null, "Error al actualizar estudiante: " + e.getMessage(), "Error SQL", JOptionPane.ERROR_MESSAGE);
        }
        return false;
    }

    /**
     * 6. Eliminar el estudiante seleccionado (Botón Eliminar).
     */
    public boolean eliminar(JTable tabla, JTextField txtCedula, JTextField txtNombre, JTextField txtApellidos,
                            JTextField txtRfid, JCheckBox chkVisual, JCheckBox chkAgiles) {
        int fila = tabla.getSelectedRow();
        if (fila == -1) {
            JOptionPane.showMessageDialog(null, "Seleccione un estudiante de la tabla para eliminar.", "Atención", JOptionPane.WARNING_MESSAGE);
            return false;
        }

        int confirmacion = JOptionPane.showConfirmDialog(null, "¿Está seguro de eliminar al estudiante seleccionado?", "Confirmar eliminación", JOptionPane.YES_NO_OPTION);
        if (confirmacion == JOptionPane.YES_OPTION) {
            int id = Integer.parseInt(tabla.getValueAt(fila, 0).toString());
            try {
                if (estudianteDAO.eliminar(id)) {
                    JOptionPane.showMessageDialog(null, "Estudiante eliminado.");
                    limpiarCampos(txtCedula, txtNombre, txtApellidos, txtRfid, chkVisual, chkAgiles);
                    cargarTabla(tabla);
                    return true;
                }
            } catch (SQLException e) {
                JOptionPane.showMessageDialog(null, "Error al eliminar: " + e.getMessage(), "Error BD", JOptionPane.ERROR_MESSAGE);
            }
        }
        return false;
    }

    /**
     * 7. Detección automática al pasar el llavero por el lector RFID en la pestaña de matrícula.
     */
    public void detectarRfid(JTextField txtRfid, JTextField txtCedula, JTextField txtNombre, JTextField txtApellidos,
                             JCheckBox chkVisual, JCheckBox chkAgiles) {
        String uid = txtRfid.getText().replaceAll("[^0-9]", "").trim();
        if (uid.isEmpty()) return;

        boolean camposVacios = txtCedula.getText().trim().isEmpty()
                && txtNombre.getText().trim().isEmpty()
                && txtApellidos.getText().trim().isEmpty();

        try {
            Estudiante est = estudianteDAO.buscarPorUid(uid);
            if (est != null) {
                int idEncontrado = est.getId();
                if (idSeleccionado == -1 && camposVacios) {
                    idSeleccionado = idEncontrado;
                    txtCedula.setText(est.getCedula());
                    txtNombre.setText(est.getNombres());
                    txtApellidos.setText(est.getApellidos());

                    chkVisual.setSelected(false);
                    chkAgiles.setSelected(false);
                    List<Integer> materias = estudianteDAO.obtenerMateriasPorEstudiante(idEncontrado);
                    for (int idMat : materias) {
                        if (idMat == 1) chkVisual.setSelected(true);
                        if (idMat == 2) chkAgiles.setSelected(true);
                    }
                    JOptionPane.showMessageDialog(null, "Llavero ya registrado a este estudiante.");
                } else if (idSeleccionado == -1 || idEncontrado != idSeleccionado) {
                    JOptionPane.showMessageDialog(null, "Este llavero ya pertenece a otro estudiante.", "Llavero duplicado", JOptionPane.WARNING_MESSAGE);
                    txtRfid.setText("");
                    txtRfid.requestFocus();
                }
            } else {
                if (idSeleccionado == -1 && camposVacios) {
                    txtCedula.requestFocus();
                }
            }
        } catch (SQLException e) {
            System.err.println("Error al detectar RFID: " + e.getMessage());
        }
    }
}
