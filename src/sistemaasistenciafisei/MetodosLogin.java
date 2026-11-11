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
    private char caracterOculto = '*';
    private SwingWorker<UsuarioAutenticado, Void> tareaIngreso;

    private UsuarioAutenticado autenticar(String usuario, String clave) throws SQLException {
        String sql = "SELECT u.nombre_completo, u.perfil, "
                   + "       COALESCE(m.id, 0) AS id_materia, m.nombre AS nombre_materia "
                   + "FROM usuarios u "
                   + "LEFT JOIN materias m ON u.id = m.id_docente "
                   + "WHERE u.usuario = ? AND u.clave = ? "
                   + "ORDER BY m.id ASC LIMIT 1";

        try (Connection cn = new Conexion().conectar()) {
            if (cn == null) {
                throw new SQLException("No se pudo conectar con la base de datos.");
            }

            try (PreparedStatement ps = cn.prepareStatement(sql)) {
                ps.setString(1, usuario);
                ps.setString(2, clave);
                ps.setQueryTimeout(10);

                try (ResultSet rs = ps.executeQuery()) {
                    if (!rs.next()) {
                        return null;
                    }

                    String nombre = rs.getString("nombre_completo");
                    String perfil = rs.getString("perfil");
                    int idMateria = rs.getInt("id_materia");
                    String nombreMateria = rs.getString("nombre_materia");
                    return new UsuarioAutenticado(nombre, perfil, idMateria, nombreMateria);
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
                         JButton btnIngresar, JCheckBox chkMostrarClave,
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

        actualizarFormulario(true, txtUsuario, txtClave, btnIngresar, chkMostrarClave, ventanaLogin);

        tareaIngreso = new SwingWorker<>() {
            @Override
            protected UsuarioAutenticado doInBackground() throws SQLException {
                return autenticar(usuario, clave);
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

    private void mostrarResultado(JFrame ventanaLogin, UsuarioAutenticado usuario) {
        if (usuario == null) {
            JOptionPane.showMessageDialog(ventanaLogin, "Usuario o contraseña incorrectos.",
                    "Acceso denegado", JOptionPane.WARNING_MESSAGE);
            return;
        }

        String curso = (usuario.nombreMateria() != null && !usuario.nombreMateria().isBlank())
                ? "\nMateria: " + usuario.nombreMateria()
                : "";
        JOptionPane.showMessageDialog(ventanaLogin,
                "Bienvenido " + usuario.nombre() + "\nPerfil: " + usuario.perfil() + curso,
                "Acceso correcto", JOptionPane.INFORMATION_MESSAGE);

        if (ventanaLogin.isDisplayable()) {
            new VentanaFISEI(usuario.perfil(), usuario.idMateria(), usuario.nombreMateria()).setVisible(true);
            ventanaLogin.dispose();
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
                "No se pudo validar el acceso. Verifique que MySQL esté iniciado "
                        + "e intente nuevamente.",
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
            VentanaLogin login = new VentanaLogin();
            login.setVisible(true);
        }
    }

    public void salir(JFrame ventanaLogin) {
        if (tareaIngreso != null) {
            tareaIngreso.cancel(true);
        }
        ventanaLogin.dispose();
    }

    private record UsuarioAutenticado(String nombre, String perfil, int idMateria, String nombreMateria) {
    }
}
