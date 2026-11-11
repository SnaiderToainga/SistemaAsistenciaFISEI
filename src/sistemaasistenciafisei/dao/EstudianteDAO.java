package sistemaasistenciafisei.dao;

import java.sql.SQLException;
import java.util.List;
import sistemaasistenciafisei.modelo.Estudiante;

/**
 * Interfaz DAO para gestión de estudiantes y sus matrículas en materias.
 */
public interface EstudianteDAO {

    /**
     * Obtiene el listado completo de estudiantes con el texto consolidado de sus materias matriculadas.
     * @return lista de estudiantes
     * @throws SQLException si ocurre un error SQL
     */
    List<Estudiante> listarTodos() throws SQLException;

    /**
     * Busca un estudiante por su clave primaria.
     * @param id identificador
     * @return Estudiante o null si no se encuentra
     * @throws SQLException si ocurre un error SQL
     */
    Estudiante buscarPorId(int id) throws SQLException;

    /**
     * Busca un estudiante por su UID físico de llavero/tarjeta RFID.
     * @param uidRfid código UID del llavero
     * @return Estudiante o null si no está registrado
     * @throws SQLException si ocurre un error SQL
     */
    Estudiante buscarPorUid(String uidRfid) throws SQLException;

    /**
     * Busca un estudiante por su cédula de identidad.
     * @param cedula número de cédula (10 dígitos)
     * @return Estudiante o null si no existe
     * @throws SQLException si ocurre un error SQL
     */
    Estudiante buscarPorCedula(String cedula) throws SQLException;

    /**
     * Registra un nuevo estudiante y sus matrículas en una única transacción atómica.
     * @param estudiante objeto estudiante con datos personales
     * @param materiasIds lista de IDs de materias en las que se matricula
     * @return true si se registró exitosamente
     * @throws SQLException si ocurre un error de concurrencia o duplicados
     */
    boolean insertar(Estudiante estudiante, List<Integer> materiasIds) throws SQLException;

    /**
     * Actualiza los datos de un estudiante y sincroniza sus matrículas en una transacción atómica.
     * @param estudiante datos del estudiante con su ID
     * @param materiasIds lista actualizada de IDs de materias
     * @return true si se actualizó correctamente
     * @throws SQLException si ocurre un error SQL
     */
    boolean actualizar(Estudiante estudiante, List<Integer> materiasIds) throws SQLException;

    /**
     * Elimina a un estudiante y sus registros vinculados (matrículas y asistencias en cascada).
     * @param idEstudiante ID del estudiante
     * @return true si fue eliminado
     * @throws SQLException si ocurre un error SQL
     */
    boolean eliminar(int idEstudiante) throws SQLException;

    /**
     * Obtiene la lista de IDs de materias en las que está matriculado un estudiante.
     * @param idEstudiante ID del estudiante
     * @return lista de identificadores de materias
     * @throws SQLException si ocurre un error SQL
     */
    List<Integer> obtenerMateriasPorEstudiante(int idEstudiante) throws SQLException;
}
