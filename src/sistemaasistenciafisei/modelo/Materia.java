package sistemaasistenciafisei.modelo;

/**
 * Entidad que representa a una materia académica.
 */
public class Materia {

    private int id;
    private String nombre;
    private Integer idDocente;

    public Materia() {
    }

    public Materia(int id, String nombre, Integer idDocente) {
        this.id = id;
        this.nombre = nombre;
        this.idDocente = idDocente;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public Integer getIdDocente() {
        return idDocente;
    }

    public void setIdDocente(Integer idDocente) {
        this.idDocente = idDocente;
    }

    @Override
    public String toString() {
        return nombre;
    }
}
