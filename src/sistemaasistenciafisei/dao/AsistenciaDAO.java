package sistemaasistenciafisei.dao;

import java.sql.SQLException;
import java.util.List;
import sistemaasistenciafisei.modelo.Asistencia;

/**
 * Interfaz DAO para registro y consulta de asistencias y control de marcaciones RFID.
 */
public interface AsistenciaDAO {

    /**
     * Lista las asistencias registradas para una materia, parcial y sesión determinados.
     * @param idMateria ID de la materia
     * @param parcial "Parcial 1" o "Parcial 2"
     * @param sesion número de sesión
     * @return lista de marcaciones
     * @throws SQLException si ocurre un error SQL
     */
    List<Asistencia> listarPorSesion(int idMateria, String parcial, int sesion) throws SQLException;

    /**
     * Verifica si el estudiante está formalmente matriculado en la materia.
     * @param idEstudiante ID del estudiante
     * @param idMateria ID de la materia
     * @return true si tiene matrícula activa
     * @throws SQLException si ocurre un error SQL
     */
    boolean estaMatriculado(int idEstudiante, int idMateria) throws SQLException;

    /**
     * Verifica si el estudiante ya registró marcación para la materia, parcial y sesión indicados.
     * @param idEstudiante ID del estudiante
     * @param idMateria ID de la materia
     * @param parcial "Parcial 1" o "Parcial 2"
     * @param sesion número de sesión
     * @return true si ya existe la marcación
     * @throws SQLException si ocurre un error SQL
     */
    boolean existeMarcacion(int idEstudiante, int idMateria, String parcial, int sesion) throws SQLException;

    /**
     * Registra una nueva asistencia con estado 'PRESENTE' tomando la fecha y hora actuales del servidor de base de datos.
     * @param idEstudiante ID del estudiante
     * @param idMateria ID de la materia
     * @param parcial "Parcial 1" o "Parcial 2"
     * @param sesion número de sesión
     * @return true si se insertó el registro
     * @throws SQLException si ocurre un error SQL
     */
    boolean registrarMarcacion(int idEstudiante, int idMateria, String parcial, int sesion) throws SQLException;

    /**
     * Obtiene el número máximo de sesión registrado para una materia y parcial (mínimo 5).
     * @param idMateria ID de la materia
     * @param parcial "Parcial 1" o "Parcial 2"
     * @return número de sesiones registradas (>= 5)
     * @throws SQLException si ocurre un error SQL
     */
    int obtenerMaxSesion(int idMateria, String parcial) throws SQLException;
}
