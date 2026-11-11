package sistemaasistenciafisei.conexion;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * Gestor centralizado de conexiones JDBC a la base de datos MySQL de FISEI.
 */
public class Conexion {

    private static final String DRIVER = "com.mysql.cj.jdbc.Driver";
    private static final String URL = "jdbc:mysql://localhost:3306/fisei_asistencia?useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true";
    private static final String USUARIO = "root";
    private static final String CLAVE = "";

    static {
        try {
            Class.forName(DRIVER);
        } catch (ClassNotFoundException ex) {
            System.err.println("Driver MySQL no encontrado en el classpath: " + ex.getMessage());
        }
    }

    /**
     * Obtiene una nueva conexión a la base de datos.
     * @return Connection activa
     * @throws SQLException si ocurre un error de conexión
     */
    public static Connection getConexion() throws SQLException {
        return DriverManager.getConnection(URL, USUARIO, CLAVE);
    }

    /**
     * Método de compatibilidad para instancias previas.
     * @return Connection activa o null en caso de fallo
     */
    public Connection conectar() {
        try {
            return getConexion();
        } catch (SQLException ex) {
            System.err.println("Error al conectar a la base de datos: " + ex.getMessage());
            return null;
        }
    }

    /**
     * Cierra de forma segura múltiples recursos JDBC (Connection, Statement, ResultSet).
     * @param recursos recursos a cerrar en bloque finally
     */
    public static void cerrar(AutoCloseable... recursos) {
        if (recursos == null) return;
        for (AutoCloseable rec : recursos) {
            if (rec != null) {
                try {
                    rec.close();
                } catch (Exception ignored) {
                }
            }
        }
    }
}
