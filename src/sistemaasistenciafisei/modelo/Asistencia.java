package sistemaasistenciafisei.modelo;

import java.sql.Date;
import java.sql.Time;

/**
 * Entidad que representa una marcación de asistencia.
 */
public class Asistencia {

    private int id;
    private int idEstudiante;
    private int idMateria;
    private String parcial; // "Parcial 1" o "Parcial 2"
    private int sesion;
    private Date fecha;
    private Time hora;
    private String estado; // "PRESENTE" o "FALTA"

    // Atributos de conveniencia provenientes de consultas con JOIN
    private String cedula;
    private String nombres;
    private String apellidos;

    public Asistencia() {
    }

    public Asistencia(int id, int idEstudiante, int idMateria, String parcial, int sesion, Date fecha, Time hora, String estado) {
        this.id = id;
        this.idEstudiante = idEstudiante;
        this.idMateria = idMateria;
        this.parcial = parcial;
        this.sesion = sesion;
        this.fecha = fecha;
        this.hora = hora;
        this.estado = estado;
    }

    public Asistencia(String cedula, String nombres, String apellidos, Date fecha, Time hora, String estado) {
        this.cedula = cedula;
        this.nombres = nombres;
        this.apellidos = apellidos;
        this.fecha = fecha;
        this.hora = hora;
        this.estado = estado;
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

    public String getParcial() {
        return parcial;
    }

    public void setParcial(String parcial) {
        this.parcial = parcial;
    }

    public int getSesion() {
        return sesion;
    }

    public void setSesion(int sesion) {
        this.sesion = sesion;
    }

    public Date getFecha() {
        return fecha;
    }

    public void setFecha(Date fecha) {
        this.fecha = fecha;
    }

    public Time getHora() {
        return hora;
    }

    public void setHora(Time hora) {
        this.hora = hora;
    }

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
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

    public String getEstudianteCompleto() {
        return (nombres != null ? nombres : "") + " " + (apellidos != null ? apellidos : "");
    }
}
