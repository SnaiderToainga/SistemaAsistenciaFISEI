package sistemaasistenciafisei.dao;

import java.sql.SQLException;
import java.util.List;
import sistemaasistenciafisei.modelo.ReporteAsistenciaDTO;

/**
 * Interfaz DAO para obtención de datos estadísticos, consolidados y promedios académicos de asistencia.
 */
public interface ReporteDAO {

    /**
     * Obtiene el consolidado de asistencias de un parcial específico para los estudiantes matriculados en una materia.
     * @param idMateria ID de la materia
     * @param parcial "Parcial 1" o "Parcial 2"
     * @param totalSesiones número total de sesiones programadas para el cálculo porcentual
     * @return lista de DTOs con cálculos y estados académicos FISEI
     * @throws SQLException si ocurre un error SQL
     */
    List<ReporteAsistenciaDTO> obtenerConsolidadoParcial(int idMateria, String parcial, int totalSesiones) throws SQLException;

    /**
     * Obtiene el promedio acumulado de los dos parciales (P1 y P2) por estudiante para la materia seleccionada.
     * @param idMateria ID de la materia
     * @param sesionesP1 total de sesiones del Parcial 1
     * @param sesionesP2 total de sesiones del Parcial 2
     * @return lista de DTOs con promedios y estados académicos finales
     * @throws SQLException si ocurre un error SQL
     */
    List<ReporteAsistenciaDTO> obtenerPromedioParciales(int idMateria, int sesionesP1, int sesionesP2) throws SQLException;
}
