/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package sistemaasistenciafisei;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import javax.swing.JOptionPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.table.DefaultTableModel;

/**
 *
 * @author User
 */
public class MetodosEstudiante {

    private Conexion con = new Conexion();
    private int idSeleccionado = -1;

    // 1. Cargar la tabla de estudiantes matriculados
    public void cargarTabla(JTable tabla) {
        String[] titulos = {"ID", "UID Llavero", "Cédula", "Nombres", "Apellidos"};
        DefaultTableModel modelo = new DefaultTableModel(null, titulos) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        String sql = "SELECT id, uid_rfid, cedula, nombres, apellidos FROM estudiantes ORDER BY id DESC";

        try (Connection cn = con.conectar();
             Statement st = cn.createStatement();
             ResultSet rs = st.executeQuery(sql)) {

            while (rs.next()) {
                modelo.addRow(new Object[]{
                    rs.getInt("id"),
                    rs.getString("uid_rfid"),
                    rs.getString("cedula"),
                    rs.getString("nombres"),
                    rs.getString("apellidos")
                });
            }
            tabla.setModel(modelo);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(null, "Error al listar estudiantes: " + e.getMessage());
        }
    }

    public boolean estaEditando() {
        return idSeleccionado != -1;
    }

    // 2. Limpiar campos de texto (Botón Nuevo)
    public void limpiarCampos(JTextField txtCedula, JTextField txtNombre, JTextField txtApellidos, JTextField txtRfid) {
        idSeleccionado = -1;
        txtCedula.setText("");
        txtNombre.setText("");
        txtApellidos.setText("");
        txtRfid.setText("");
        txtCedula.requestFocus();
    }

    private boolean validarCampos(String cedula, String nombres, String apellidos, String uid, JTextField txtCedula) {
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
        return true;
    }

    // 3. Guardar un nuevo estudiante (Botón Guardar)
    public boolean guardar(JTextField txtCedula, JTextField txtNombre, JTextField txtApellidos, JTextField txtRfid, JTable tabla) {
        String cedula = txtCedula.getText().trim();
        String nombres = txtNombre.getText().trim();
        String apellidos = txtApellidos.getText().trim();
        String uid = txtRfid.getText().replaceAll("[^0-9]", "").trim();

        if (!validarCampos(cedula, nombres, apellidos, uid, txtCedula)) {
            return false;
        }

        String sql = "INSERT INTO estudiantes (uid_rfid, cedula, nombres, apellidos) VALUES (?, ?, ?, ?)";
        try (Connection cn = con.conectar();
             PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setString(1, uid);
            ps.setString(2, cedula);
            ps.setString(3, nombres);
            ps.setString(4, apellidos);

            if (ps.executeUpdate() > 0) {
                JOptionPane.showMessageDialog(null, "¡Estudiante registrado exitosamente!");
                limpiarCampos(txtCedula, txtNombre, txtApellidos, txtRfid);
                cargarTabla(tabla);
                return true;
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(null, "Error al registrar (posible cédula o llavero duplicado): " + e.getMessage(), "Error SQL", JOptionPane.ERROR_MESSAGE);
        }
        return false;
    }

    // 4. Seleccionar fila de la tabla para editar
    public void seleccionarFila(JTable tabla, JTextField txtRfid, JTextField txtCedula, JTextField txtNombre, JTextField txtApellidos) {
        int fila = tabla.getSelectedRow();
        if (fila != -1) {
            idSeleccionado = Integer.parseInt(tabla.getValueAt(fila, 0).toString());
            txtRfid.setText(tabla.getValueAt(fila, 1).toString());
            txtCedula.setText(tabla.getValueAt(fila, 2).toString());
            txtNombre.setText(tabla.getValueAt(fila, 3).toString());
            txtApellidos.setText(tabla.getValueAt(fila, 4).toString());
        }
    }
    
    // 5. Modificar los datos del estudiante seleccionado (Botón Modificar)
    public boolean modificar(JTextField txtCedula, JTextField txtNombre, JTextField txtApellidos, JTextField txtRfid, JTable tabla) {
        if (idSeleccionado == -1) {
            JOptionPane.showMessageDialog(null, "Seleccione un estudiante de la tabla para modificar.", "Atención", JOptionPane.WARNING_MESSAGE);
            return false;
        }

        String cedula = txtCedula.getText().trim();
        String nombres = txtNombre.getText().trim();
        String apellidos = txtApellidos.getText().trim();
        String uid = txtRfid.getText().replaceAll("[^0-9]", "").trim();

        if (!validarCampos(cedula, nombres, apellidos, uid, txtCedula)) {
            return false;
        }

        String sql = "UPDATE estudiantes SET uid_rfid = ?, cedula = ?, nombres = ?, apellidos = ? WHERE id = ?";
        try (Connection cn = con.conectar();
             PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setString(1, uid);
            ps.setString(2, cedula);
            ps.setString(3, nombres);
            ps.setString(4, apellidos);
            ps.setInt(5, idSeleccionado);

            if (ps.executeUpdate() > 0) {
                JOptionPane.showMessageDialog(null, "¡Datos del estudiante actualizados correctamente!");
                limpiarCampos(txtCedula, txtNombre, txtApellidos, txtRfid);
                cargarTabla(tabla);
                return true;
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(null, "Error al actualizar estudiante: " + e.getMessage(), "Error SQL", JOptionPane.ERROR_MESSAGE);
        }
        return false;
    }

    // 6. Eliminar el estudiante seleccionado (Botón Eliminar)
    public boolean eliminar(JTable tabla, JTextField txtCedula, JTextField txtNombre, JTextField txtApellidos, JTextField txtRfid) {
        int fila = tabla.getSelectedRow();
        if (fila == -1) {
            JOptionPane.showMessageDialog(null, "Seleccione un estudiante de la tabla para eliminar.", "Atención", JOptionPane.WARNING_MESSAGE);
            return false;
        }

        int confirmacion = JOptionPane.showConfirmDialog(null, "¿Está seguro de eliminar al estudiante seleccionado?", "Confirmar eliminación", JOptionPane.YES_NO_OPTION);
        if (confirmacion == JOptionPane.YES_OPTION) {
            int id = Integer.parseInt(tabla.getValueAt(fila, 0).toString());
            String sql = "DELETE FROM estudiantes WHERE id = ?";
            try (Connection cn = con.conectar();
                 PreparedStatement ps = cn.prepareStatement(sql)) {
                ps.setInt(1, id);
                if (ps.executeUpdate() > 0) {
                    JOptionPane.showMessageDialog(null, "Estudiante eliminado.");
                    limpiarCampos(txtCedula, txtNombre, txtApellidos, txtRfid);
                    cargarTabla(tabla);
                    return true;
                }
            } catch (Exception e) {
                JOptionPane.showMessageDialog(null, "Error al eliminar: " + e.getMessage());
            }
        }
        return false;
    }

    // 7. Detección automática al pasar el llavero por Id_Matricula
    public void detectarRfid(JTextField txtRfid, JTextField txtCedula, JTextField txtNombre, JTextField txtApellidos) {
        String uid = txtRfid.getText().replaceAll("[^0-9]", "").trim();
        if (uid.isEmpty()) return;

        boolean camposVacios = txtCedula.getText().trim().isEmpty()
                && txtNombre.getText().trim().isEmpty()
                && txtApellidos.getText().trim().isEmpty();

        String sql = "SELECT id, cedula, nombres, apellidos FROM estudiantes WHERE uid_rfid = ?";
        try (Connection cn = con.conectar();
             PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setString(1, uid);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                int idEncontrado = rs.getInt("id");
                if (idSeleccionado == -1 && camposVacios) {
                    idSeleccionado = idEncontrado;
                    txtCedula.setText(rs.getString("cedula"));
                    txtNombre.setText(rs.getString("nombres"));
                    txtApellidos.setText(rs.getString("apellidos"));
                    JOptionPane.showMessageDialog(null, "Llavero ya registrado a este estudiante.");
                } else if (idSeleccionado == -1 || idEncontrado != idSeleccionado) {
                    JOptionPane.showMessageDialog(null, "Este llavero ya pertenece a otro estudiante.", "Llavero duplicado", JOptionPane.WARNING_MESSAGE);
                    txtRfid.setText("");
                    txtRfid.requestFocus();
                }
            } else {
                // Si aún no llenaba los datos, llevar el cursor a Cédula; si ya los llenó, no borrar nada
                if (idSeleccionado == -1 && camposVacios) {
                    txtCedula.requestFocus();
                }
            }
        } catch (Exception e) {
            System.err.println("Error al detectar RFID: " + e.getMessage());
        }
    }
}