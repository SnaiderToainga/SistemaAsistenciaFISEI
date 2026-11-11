package sistemaasistenciafisei.modelo;

/**
 * Entidad intermedia que representa la matrícula de un estudiante en una materia.
 */
public class Matricula {

    private int id;
    private int idEstudiante;
    private int idMateria;

    public Matricula() {
    }

    public Matricula(int id, int idEstudiante, int idMateria) {
        this.id = id;
        this.idEstudiante = idEstudiante;
        this.idMateria = idMateria;
    }

    public Matricula(int idEstudiante, int idMateria) {
        this.idEstudiante = idEstudiante;
        this.idMateria = idMateria;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getIdEstudiante() {
        return idEstudiante;
    }

    public void setIdEstudiante(int idEstudiante) {
        this.idEstudiante = idEstudiante;
    }

    public int getIdMateria() {
        return idMateria;
    }

    public void setIdMateria(int idMateria) {
        this.idMateria = idMateria;
    }
}
