package sistemaasistenciafisei;

import java.awt.Cursor;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.concurrent.ExecutionException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;

public class MetodosLogin {

    private static final Logger LOG = Logger.getLogger(MetodosLogin.class.getName());
    private String nombreUsuario;
    private String perfilUsuario;
    private char caracterOculto = '*';
    private SwingWorker<Boolean, Void> tareaIngreso;

    public boolean validarAcceso(String user, String pass) throws SQLException {
        nombreUsuario = null;
        perfilUsuario = null;

        if (user == null || pass == null
                || user.trim().isEmpty() || pass.isEmpty()) {
            return false;
        }

        String sql = "SELECT id, nombre_completo, perfil FROM usuarios "
                + "WHERE usuario = ? AND clave = ?";

        try (Connection cn = new Conexion().conectar()) {
            if (cn == null) {
                throw new SQLException("No se pudo conectar con la base de datos.");
            }

            try (PreparedStatement ps = cn.prepareStatement(sql)) {
                ps.setString(1, user.trim());
                ps.setString(2, pass);
                ps.setQueryTimeout(10);

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

    public void configurarCampos(JTextField txtUsuario, JPasswordField txtClave,
                                 JCheckBox mostrarClave) {
        caracterOculto = txtClave.getEchoChar() == 0 ? '*' : txtClave.getEchoChar();
        mostrarClave.setSelected(false);
        txtClave.setEchoChar(caracterOculto);
        SwingUtilities.invokeLater(txtUsuario::requestFocusInWindow);
    }

    public void mostrarClave(JPasswordField txtClave, JCheckBox mostrarClave) {
        txtClave.setEchoChar(mostrarClave.isSelected() ? (char) 0 : caracterOculto);
    }

    public void ingresar(JTextField txtUsuario, JPasswordField txtClave,
                         JButton btnIngresar, JCheckBox mostrarClave,
                         JFrame ventanaLogin) {
        // Una sola consulta por intento, aunque se pulse Enter varias veces.
        if (tareaIngreso != null) {
            return;
        }

        String usuario = txtUsuario.getText().trim();
        char[] caracteres = txtClave.getPassword();
        String clave = new String(caracteres);
        Arrays.fill(caracteres, '\0');

        if (usuario.isEmpty()) {
            JOptionPane.showMessageDialog(ventanaLogin, "Ingrese su usuario.",
                    "Datos incompletos", JOptionPane.WARNING_MESSAGE);
            txtUsuario.requestFocusInWindow();
            return;
        }

        if (clave.isEmpty()) {
            JOptionPane.showMessageDialog(ventanaLogin, "Ingrese su contraseña.",
                    "Datos incompletos", JOptionPane.WARNING_MESSAGE);
            txtClave.requestFocusInWindow();
            return;
        }

        cambiarEstado(true, txtUsuario, txtClave, btnIngresar, mostrarClave, ventanaLogin);

        tareaIngreso = new SwingWorker<Boolean, Void>() {
            @Override
            protected Boolean doInBackground() throws SQLException {
                return validarAcceso(usuario, clave);
            }

            @Override
            protected void done() {
                try {
                    // Si se cerró el login, no abrir otra ventana al terminar.
                    if (isCancelled() || !ventanaLogin.isDisplayable()) {
                        return;
                    }

                    if (get()) {
                        JOptionPane.showMessageDialog(ventanaLogin,
                                "Bienvenido " + getNombreUsuario()
                                        + "\nPerfil: " + getPerfilUsuario(),
                                "Acceso correcto", JOptionPane.INFORMATION_MESSAGE);

                        if (!ventanaLogin.isDisplayable()) {
                            return;
                        }
                        VentanaFISEI principal = new VentanaFISEI(getPerfilUsuario());
                        principal.setVisible(true);
                        ventanaLogin.dispose();
                    } else {
                        JOptionPane.showMessageDialog(ventanaLogin,
                                "Usuario o contraseña incorrectos.",
                                "Acceso denegado", JOptionPane.WARNING_MESSAGE);
                    }
                } catch (InterruptedException ex) {
                    Thread.currentThread().interrupt();
                    informarError(ventanaLogin, ex);
                } catch (ExecutionException ex) {
                    informarError(ventanaLogin, ex.getCause());
                } finally {
                    txtClave.setText("");
                    mostrarClave.setSelected(false);
                    txtClave.setEchoChar(caracterOculto);
                    cambiarEstado(false, txtUsuario, txtClave, btnIngresar,
                            mostrarClave, ventanaLogin);
                    tareaIngreso = null;
                    if (ventanaLogin.isDisplayable()) {
                        txtClave.requestFocusInWindow();
                    }
                }
            }
        };
        tareaIngreso.execute();
    }

    private void cambiarEstado(boolean validando, JTextField txtUsuario,
                               JPasswordField txtClave, JButton btnIngresar,
                               JCheckBox mostrarClave, JFrame ventanaLogin) {
        txtUsuario.setEnabled(!validando);
        txtClave.setEnabled(!validando);
        mostrarClave.setEnabled(!validando);
        btnIngresar.setEnabled(!validando);
        btnIngresar.setText(validando ? "Validando..." : "Ingresar");
        ventanaLogin.setCursor(Cursor.getPredefinedCursor(
                validando ? Cursor.WAIT_CURSOR : Cursor.DEFAULT_CURSOR));
    }

    private void informarError(JFrame ventanaLogin, Throwable error) {
        LOG.log(Level.SEVERE, "No se pudo validar el acceso", error);
        JOptionPane.showMessageDialog(ventanaLogin,
                "No se pudo validar el acceso. Verifique que MySQL esté iniciado "
                        + "e intente nuevamente.",
                "Error de conexión", JOptionPane.ERROR_MESSAGE);
    }

    public void salir(JFrame ventanaLogin) {
        if (tareaIngreso != null) {
            tareaIngreso.cancel(true);
        }
        ventanaLogin.dispose();
    }

    public String getNombreUsuario() {
        return nombreUsuario;
    }

    public String getPerfilUsuario() {
        return perfilUsuario;
    }
}
