package sistemaasistenciafisei.dao.impl;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import sistemaasistenciafisei.conexion.Conexion;
import sistemaasistenciafisei.dao.UsuarioDAO;
import sistemaasistenciafisei.modelo.Usuario;

/**
 * Implementación JDBC de UsuarioDAO.
 */
public class UsuarioDAOImpl implements UsuarioDAO {

    @Override
    public Usuario autenticar(String usuario, String clave) throws SQLException {
        String sql = "SELECT u.id, u.usuario, u.clave, u.nombre_completo, u.perfil, "
                   + "       COALESCE(m.id, 0) AS id_materia, m.nombre AS nombre_materia "
                   + "FROM usuarios u "
                   + "LEFT JOIN materias m ON u.id = m.id_docente "
                   + "WHERE u.usuario = ? AND u.clave = ? "
                   + "ORDER BY m.id ASC LIMIT 1";

        try (Connection cn = Conexion.getConexion();
             PreparedStatement ps = cn.prepareStatement(sql)) {

            ps.setString(1, usuario);
            ps.setString(2, clave);
            ps.setQueryTimeout(10);

            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    return null;
                }
                Usuario u = new Usuario();
                u.setId(rs.getInt("id"));
                u.setUsuario(rs.getString("usuario"));
                u.setClave(rs.getString("clave"));
                u.setNombreCompleto(rs.getString("nombre_completo"));
                u.setPerfil(rs.getString("perfil"));
                u.setIdMateria(rs.getInt("id_materia"));
                u.setNombreMateria(rs.getString("nombre_materia"));
                return u;
            }
        }
    }

    @Override
    public Usuario buscarPorId(int id) throws SQLException {
        String sql = "SELECT u.id, u.usuario, u.clave, u.nombre_completo, u.perfil, "
                   + "       COALESCE(m.id, 0) AS id_materia, m.nombre AS nombre_materia "
                   + "FROM usuarios u "
                   + "LEFT JOIN materias m ON u.id = m.id_docente "
                   + "WHERE u.id = ?";

        try (Connection cn = Conexion.getConexion();
             PreparedStatement ps = cn.prepareStatement(sql)) {

            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    return null;
                }
                Usuario u = new Usuario();
                u.setId(rs.getInt("id"));
                u.setUsuario(rs.getString("usuario"));
                u.setClave(rs.getString("clave"));
                u.setNombreCompleto(rs.getString("nombre_completo"));
                u.setPerfil(rs.getString("perfil"));
                u.setIdMateria(rs.getInt("id_materia"));
                u.setNombreMateria(rs.getString("nombre_materia"));
                return u;
            }
        }
    }

    @Override
    public List<Usuario> listarTodos() throws SQLException {
        List<Usuario> lista = new ArrayList<>();
        String sql = "SELECT u.id, u.usuario, u.clave, u.nombre_completo, u.perfil, "
                   + "       COALESCE(m.id, 0) AS id_materia, m.nombre AS nombre_materia "
                   + "FROM usuarios u "
                   + "LEFT JOIN materias m ON u.id = m.id_docente "
                   + "ORDER BY u.nombre_completo ASC";

        try (Connection cn = Conexion.getConexion();
             PreparedStatement ps = cn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                Usuario u = new Usuario();
                u.setId(rs.getInt("id"));
                u.setUsuario(rs.getString("usuario"));
                u.setClave(rs.getString("clave"));
                u.setNombreCompleto(rs.getString("nombre_completo"));
                u.setPerfil(rs.getString("perfil"));
                u.setIdMateria(rs.getInt("id_materia"));
                u.setNombreMateria(rs.getString("nombre_materia"));
                lista.add(u);
            }
        }
        return lista;
    }
}
