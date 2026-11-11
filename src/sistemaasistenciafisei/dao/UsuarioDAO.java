package sistemaasistenciafisei.dao;

import java.sql.SQLException;
import java.util.List;
import sistemaasistenciafisei.modelo.Usuario;

/**
 * Interfaz DAO para operaciones sobre la tabla usuarios y autenticación.
 */
public interface UsuarioDAO {

    /**
     * Autentica a un usuario y carga sus datos de rol y materia asociada.
     * @param usuario nombre de usuario
     * @param clave contraseña
     * @return Usuario autenticado o null si las credenciales son incorrectas
     * @throws SQLException si ocurre un error en la base de datos
     */
    Usuario autenticar(String usuario, String clave) throws SQLException;

    /**
     * Busca un usuario por su identificador primario.
     * @param id identificador de usuario
     * @return Usuario o null si no existe
     * @throws SQLException si ocurre un error SQL
     */
    Usuario buscarPorId(int id) throws SQLException;

    /**
     * Lista todos los usuarios registrados.
     * @return lista de usuarios
     * @throws SQLException si ocurre un error SQL
     */
    List<Usuario> listarTodos() throws SQLException;
}
