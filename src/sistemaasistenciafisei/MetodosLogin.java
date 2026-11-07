package sistemaasistenciafisei;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPasswordField;
import javax.swing.JTextField;

public class MetodosLogin {

    private String nombreUsuario;
    private String perfilUsuario;

    public boolean validarAcceso(String user, String pass, String perfil)
            throws SQLException {

        // Limpia los datos de cualquier intento anterior
        nombreUsuario = null;
        perfilUsuario = null;

        if (user == null || pass == null || perfil == null
                || user.trim().isEmpty()
                || pass.isEmpty()
                || perfil.trim().isEmpty()) {
            return false;
        }

        String sql = "SELECT id, nombre_completo, perfil "
                + "FROM usuarios "
                + "WHERE usuario = ? AND clave = ? AND perfil = ?";

        Conexion conexion = new Conexion();

        try (Connection cn = conexion.conectar()) {

            if (cn == null) {
                throw new SQLException(
                        "No se pudo conectar con la base de datos.");
            }

            try (PreparedStatement ps = cn.prepareStatement(sql)) {

                ps.setString(1, user.trim());
                ps.setString(2, pass);
                ps.setString(3, perfil.trim());

                try (ResultSet rs = ps.executeQuery()) {

                    if (rs.next()) {
                        nombreUsuario = rs.getString("nombre_completo");
                        perfilUsuario = rs.getString("perfil");
                        return true;
                    }

                    return false;
                }
            }
        }
    }

    public void cargarPerfiles(JComboBox<String> comboPerfil) {
        comboPerfil.removeAllItems();
        comboPerfil.addItem("Administrador");
        comboPerfil.addItem("Docente");
        comboPerfil.setSelectedIndex(-1);
    }

    public void ingresar(JTextField txtUsuario,
            JPasswordField txtClave,
            JComboBox<String> comboPerfil,
            JFrame ventanaLogin) {

        String usuario = txtUsuario.getText().trim();
        String clave = new String(txtClave.getPassword());
        String perfil = (String) comboPerfil.getSelectedItem();

        if (usuario.isEmpty()) {
            JOptionPane.showMessageDialog(
                    ventanaLogin,
                    "Ingrese su usuario.",
                    "Datos incompletos",
                    JOptionPane.WARNING_MESSAGE);
            txtUsuario.requestFocusInWindow();
            return;
        }

        if (clave.isEmpty()) {
            JOptionPane.showMessageDialog(
                    ventanaLogin,
                    "Ingrese su contraseña.",
                    "Datos incompletos",
                    JOptionPane.WARNING_MESSAGE);
            txtClave.requestFocusInWindow();
            return;
        }

        if (perfil == null) {
            JOptionPane.showMessageDialog(
                    ventanaLogin,
                    "Seleccione un perfil.",
                    "Datos incompletos",
                    JOptionPane.WARNING_MESSAGE);
            comboPerfil.requestFocusInWindow();
            return;
        }

        try {
            if (validarAcceso(usuario, clave, perfil)) {
                JOptionPane.showMessageDialog(
                        ventanaLogin,
                        "Bienvenido " + getNombreUsuario(),
                        "Acceso correcto",
                        JOptionPane.INFORMATION_MESSAGE);

                VentanaFISEI ventanaPrincipal = new VentanaFISEI();
                ventanaPrincipal.setVisible(true);

                txtClave.setText("");
                ventanaLogin.dispose();

            } else {
                JOptionPane.showMessageDialog(
                        ventanaLogin,
                        "Credenciales inválidas. Verifique usuario, "
                        + "contraseña y perfil.",
                        "Acceso denegado",
                        JOptionPane.WARNING_MESSAGE);

                txtClave.setText("");
                txtClave.requestFocusInWindow();
            }

        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(
                    ventanaLogin,
                    "No se pudo validar el acceso. "
                    + "Verifique que MySQL esté iniciado.\n"
                    + ex.getMessage(),
                    "Error de base de datos",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    public void salir(JFrame ventanaLogin) {
        ventanaLogin.dispose();
    }

    public String getNombreUsuario() {
        return nombreUsuario;
    }

    public String getPerfilUsuario() {
        return perfilUsuario;
    }
}
