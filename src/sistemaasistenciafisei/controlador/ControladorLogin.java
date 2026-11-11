package sistemaasistenciafisei.controlador;

import java.awt.Cursor;
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
import sistemaasistenciafisei.dao.UsuarioDAO;
import sistemaasistenciafisei.dao.impl.UsuarioDAOImpl;
import sistemaasistenciafisei.modelo.Usuario;

/**
 * Controlador para la autenticación de usuarios y navegación inicial.
 */
public class ControladorLogin {

    private static final Logger LOG = Logger.getLogger(ControladorLogin.class.getName());
    private final UsuarioDAO usuarioDAO;
    private char caracterOculto = '*';
    private SwingWorker<Usuario, Void> tareaIngreso;

    public ControladorLogin() {
        this(new UsuarioDAOImpl());
    }

    public ControladorLogin(UsuarioDAO usuarioDAO) {
        this.usuarioDAO = usuarioDAO;
    }

    public void configurarCampos(JTextField txtUsuario, JPasswordField txtClave, JCheckBox mostrarClave) {
        caracterOculto = txtClave.getEchoChar() == 0 ? '*' : txtClave.getEchoChar();
        mostrarClave.setSelected(false);
        txtClave.setEchoChar(caracterOculto);
        SwingUtilities.invokeLater(txtUsuario::requestFocusInWindow);
    }

    public void mostrarClave(JPasswordField txtClave, JCheckBox mostrarClave) {
        txtClave.setEchoChar(mostrarClave.isSelected() ? (char) 0 : caracterOculto);
    }

    public void ingresar(JTextField txtUsuario, JPasswordField txtClave,
                          JButton btnIngresar, JCheckBox chkMostrarClave,
                          JFrame ventanaLogin) {
        // Una sola consulta por intento
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

        actualizarFormulario(true, txtUsuario, txtClave, btnIngresar, chkMostrarClave, ventanaLogin);

        tareaIngreso = new SwingWorker<>() {
            @Override
            protected Usuario doInBackground() throws SQLException {
                return usuarioDAO.autenticar(usuario, clave);
            }

            @Override
            protected void done() {
                try {
                    if (!isCancelled() && ventanaLogin.isDisplayable()) {
                        mostrarResultado(ventanaLogin, get());
                    }
                } catch (InterruptedException ex) {
                    Thread.currentThread().interrupt();
                    informarError(ventanaLogin, ex);
                } catch (ExecutionException ex) {
                    informarError(ventanaLogin, ex.getCause());
                } finally {
                    actualizarFormulario(false, txtUsuario, txtClave, btnIngresar,
                            chkMostrarClave, ventanaLogin);
                    tareaIngreso = null;
                }
            }
        };
        tareaIngreso.execute();
    }

    private void mostrarResultado(JFrame ventanaLogin, Usuario usuario) {
        if (usuario == null) {
            JOptionPane.showMessageDialog(ventanaLogin, "Usuario o contraseña incorrectos.",
                    "Acceso denegado", JOptionPane.WARNING_MESSAGE);
            return;
        }

        String curso = (usuario.getNombreMateria() != null && !usuario.getNombreMateria().isBlank())
                ? "\nMateria: " + usuario.getNombreMateria()
                : "";
        JOptionPane.showMessageDialog(ventanaLogin,
                "Bienvenido " + usuario.getNombreCompleto() + "\nPerfil: " + usuario.getPerfil() + curso,
                "Acceso correcto", JOptionPane.INFORMATION_MESSAGE);

        if (ventanaLogin.isDisplayable()) {
            abrirVentanaPrincipal(usuario);
            ventanaLogin.dispose();
        }
    }

    protected void abrirVentanaPrincipal(Usuario usuario) {
        try {
            Class<?> clazz = Class.forName("sistemaasistenciafisei.VentanaFISEI");
            java.lang.reflect.Constructor<?> ctor = clazz.getConstructor(String.class, int.class, String.class);
            JFrame ventana = (JFrame) ctor.newInstance(usuario.getPerfil(), usuario.getIdMateria(), usuario.getNombreMateria());
            ventana.setVisible(true);
        } catch (Exception e) {
            LOG.log(Level.SEVERE, "Error al abrir VentanaFISEI", e);
        }
    }

    private void actualizarFormulario(boolean validando, JTextField txtUsuario,
                                      JPasswordField txtClave, JButton btnIngresar,
                                      JCheckBox chkMostrarClave, JFrame ventanaLogin) {
        txtUsuario.setEnabled(!validando);
        txtClave.setEnabled(!validando);
        chkMostrarClave.setEnabled(!validando);
        btnIngresar.setEnabled(!validando);
        btnIngresar.setText(validando ? "Validando..." : "Ingresar");
        ventanaLogin.setCursor(Cursor.getPredefinedCursor(
                validando ? Cursor.WAIT_CURSOR : Cursor.DEFAULT_CURSOR));

        if (!validando) {
            txtClave.setText("");
            chkMostrarClave.setSelected(false);
            mostrarClave(txtClave, chkMostrarClave);
            if (ventanaLogin.isDisplayable()) {
                txtClave.requestFocusInWindow();
            }
        }
    }

    private void informarError(JFrame ventanaLogin, Throwable error) {
        LOG.log(Level.SEVERE, "No se pudo validar el acceso", error);
        JOptionPane.showMessageDialog(ventanaLogin,
                "No se pudo validar el acceso. Verifique que MySQL esté iniciado e intente nuevamente.",
                "Error de conexión", JOptionPane.ERROR_MESSAGE);
    }

    public void cerrarSesion(JFrame ventanaActual) {
        int confirmacion = JOptionPane.showConfirmDialog(
                ventanaActual,
                "¿Desea cerrar la sesión actual?",
                "Cerrar Sesión",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE
        );

        if (confirmacion == JOptionPane.YES_OPTION) {
            ventanaActual.dispose();
            abrirVentanaLogin();
        }
    }

    protected void abrirVentanaLogin() {
        try {
            Class<?> clazz = Class.forName("sistemaasistenciafisei.VentanaLogin");
            JFrame login = (JFrame) clazz.getDeclaredConstructor().newInstance();
            login.setVisible(true);
        } catch (Exception e) {
            LOG.log(Level.SEVERE, "Error al abrir VentanaLogin", e);
        }
    }

    public void salir(JFrame ventanaLogin) {
        if (tareaIngreso != null) {
            tareaIngreso.cancel(true);
        }
        ventanaLogin.dispose();
    }
}
