package sistemaasistenciafisei.modelo;

/**
 * Entidad que representa a un usuario del sistema (Administrador o Docente).
 */
public class Usuario {

    private int id;
    private String usuario;
    private String clave;
    private String nombreCompleto;
    private String perfil; // "Administrador" o "Docente"
    private int idMateria; // ID de la materia asignada (si es docente)
    private String nombreMateria; // Nombre de la materia asignada

    public Usuario() {
    }

    public Usuario(int id, String usuario, String clave, String nombreCompleto, String perfil) {
        this.id = id;
        this.usuario = usuario;
        this.clave = clave;
        this.nombreCompleto = nombreCompleto;
        this.perfil = perfil;
    }

    public Usuario(int id, String usuario, String clave, String nombreCompleto, String perfil, int idMateria, String nombreMateria) {
        this.id = id;
        this.usuario = usuario;
        this.clave = clave;
        this.nombreCompleto = nombreCompleto;
        this.perfil = perfil;
        this.idMateria = idMateria;
        this.nombreMateria = nombreMateria;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getUsuario() {
        return usuario;
    }

    public void setUsuario(String usuario) {
        this.usuario = usuario;
    }

    public String getClave() {
        return clave;
    }

    public void setClave(String clave) {
        this.clave = clave;
    }

    public String getNombreCompleto() {
        return nombreCompleto;
    }

    public void setNombreCompleto(String nombreCompleto) {
        this.nombreCompleto = nombreCompleto;
    }

    public String getPerfil() {
        return perfil;
    }

    public void setPerfil(String perfil) {
        this.perfil = perfil;
    }

    public int getIdMateria() {
        return idMateria;
    }

    public void setIdMateria(int idMateria) {
        this.idMateria = idMateria;
    }

    public String getNombreMateria() {
        return nombreMateria;
    }

    public void setNombreMateria(String nombreMateria) {
        this.nombreMateria = nombreMateria;
    }

    @Override
    public String toString() {
        return nombreCompleto + " (" + perfil + ")";
    }
}
