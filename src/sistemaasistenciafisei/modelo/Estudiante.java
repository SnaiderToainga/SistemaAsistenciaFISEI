package sistemaasistenciafisei.modelo;

import java.util.ArrayList;
import java.util.List;

/**
 * Entidad que representa a un estudiante matriculado.
 */
public class Estudiante {

    private int id;
    private String uidRfid;
    private String cedula;
    private String nombres;
    private String apellidos;
    private String carrera;
    private String materiasTexto; // Texto descriptivo de materias para la tabla (ej: "Computación Visual, Metodologías Ágiles")
    private List<Integer> materiasIds = new ArrayList<>();

    public Estudiante() {
        this.carrera = "Ingeniería de Software";
    }

    public Estudiante(int id, String uidRfid, String cedula, String nombres, String apellidos) {
        this.id = id;
        this.uidRfid = uidRfid;
        this.cedula = cedula;
        this.nombres = nombres;
        this.apellidos = apellidos;
        this.carrera = "Ingeniería de Software";
    }

    public Estudiante(int id, String uidRfid, String cedula, String nombres, String apellidos, String carrera, String materiasTexto) {
        this.id = id;
        this.uidRfid = uidRfid;
        this.cedula = cedula;
        this.nombres = nombres;
        this.apellidos = apellidos;
        this.carrera = (carrera != null && !carrera.isBlank()) ? carrera : "Ingeniería de Software";
        this.materiasTexto = materiasTexto;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getUidRfid() {
        return uidRfid;
    }

    public void setUidRfid(String uidRfid) {
        this.uidRfid = uidRfid;
    }

    public String getCedula() {
        return cedula;
    }

    public void setCedula(String cedula) {
        this.cedula = cedula;
    }

    public String getNombres() {
        return nombres;
    }

    public void setNombres(String nombres) {
        this.nombres = nombres;
    }

    public String getApellidos() {
        return apellidos;
    }

    public void setApellidos(String apellidos) {
        this.apellidos = apellidos;
    }

    public String getCarrera() {
        return carrera;
    }

    public void setCarrera(String carrera) {
        this.carrera = carrera;
    }

    public String getMateriasTexto() {
        return materiasTexto;
    }

    public void setMateriasTexto(String materiasTexto) {
        this.materiasTexto = materiasTexto;
    }

    public List<Integer> getMateriasIds() {
        return materiasIds;
    }

    public void setMateriasIds(List<Integer> materiasIds) {
        this.materiasIds = materiasIds != null ? materiasIds : new ArrayList<>();
    }

    public String getNombreCompleto() {
        return (nombres != null ? nombres : "") + " " + (apellidos != null ? apellidos : "");
    }

    @Override
    public String toString() {
        return getNombreCompleto() + " (" + cedula + ")";
    }
}
