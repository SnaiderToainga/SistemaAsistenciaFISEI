package sistemaasistenciafisei.modelo;

import java.util.Locale;

/**
 * Data Transfer Object (DTO) para los datos calculados de consolidados y reportes FISEI.
 */
public class ReporteAsistenciaDTO {

    private int idEstudiante;
    private String cedula;
    private String nombreCompleto;
    private int totalAsistencias;
    private int totalSesiones;
    private double porcentaje;
    private String estado; // "APROBADO", "EN RIESGO", "REPROBADO"

    // Atributos para promedio de parciales (P1 y P2)
    private int asistP1;
    private int asistP2;
    private int sesionesP1;
    private int sesionesP2;
    private double promedio;

    public ReporteAsistenciaDTO() {
    }

    public ReporteAsistenciaDTO(int idEstudiante, String cedula, String nombreCompleto, int totalAsistencias, int totalSesiones) {
        this.idEstudiante = idEstudiante;
        this.cedula = cedula;
        this.nombreCompleto = nombreCompleto;
        this.totalAsistencias = totalAsistencias;
        this.totalSesiones = totalSesiones;
        calcularEstadoParcial();
    }

    public void calcularEstadoParcial() {
        if (totalSesiones <= 0) {
            this.porcentaje = 0.0;
        } else {
            this.porcentaje = (totalAsistencias / (double) totalSesiones) * 100.0;
        }
        if (this.porcentaje >= 80.0) {
            this.estado = "APROBADO";
        } else if (this.porcentaje >= 70.0) {
            this.estado = "EN RIESGO";
        } else {
            this.estado = "REPROBADO";
        }
    }

    public void calcularEstadoPromedio() {
        int s1 = sesionesP1 > 0 ? sesionesP1 : 5;
        int s2 = sesionesP2 > 0 ? sesionesP2 : 5;
        double porcP1 = (asistP1 / (double) s1) * 100.0;
        double porcP2 = (asistP2 / (double) s2) * 100.0;
        this.promedio = (porcP1 + porcP2) / 2.0;

        if (this.promedio >= 80.0) {
            this.estado = "APROBADO";
        } else if (this.promedio >= 70.0) {
            this.estado = "EN RIESGO";
        } else {
            this.estado = "REPROBADO";
        }
    }

    public int getIdEstudiante() {
        return idEstudiante;
    }

    public void setIdEstudiante(int idEstudiante) {
        this.idEstudiante = idEstudiante;
    }

    public String getCedula() {
        return cedula;
    }

    public void setCedula(String cedula) {
        this.cedula = cedula;
    }

    public String getNombreCompleto() {
        return nombreCompleto;
    }

    public void setNombreCompleto(String nombreCompleto) {
        this.nombreCompleto = nombreCompleto;
    }

    public int getTotalAsistencias() {
        return totalAsistencias;
    }

    public void setTotalAsistencias(int totalAsistencias) {
        this.totalAsistencias = totalAsistencias;
    }

    public int getTotalSesiones() {
        return totalSesiones;
    }

    public void setTotalSesiones(int totalSesiones) {
        this.totalSesiones = totalSesiones;
    }

    public double getPorcentaje() {
        return porcentaje;
    }

    public void setPorcentaje(double porcentaje) {
        this.porcentaje = porcentaje;
    }

    public String getPorcentajeFormateado() {
        return String.format(Locale.US, "%.1f%%", porcentaje);
    }

    public String getPromedioFormateado() {
        return String.format(Locale.US, "%.1f%%", promedio);
    }

    public String getAsistenciaTexto() {
        return totalAsistencias + " / " + totalSesiones;
    }

    public String getAsistenciaPromedioTexto() {
        int totalSes = sesionesP1 + sesionesP2;
        return (asistP1 + asistP2) + " / " + totalSes + " (P1:" + asistP1 + "/" + sesionesP1 + ", P2:" + asistP2 + "/" + sesionesP2 + ")";
    }

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }

    public int getAsistP1() {
        return asistP1;
    }

    public void setAsistP1(int asistP1) {
        this.asistP1 = asistP1;
    }

    public int getAsistP2() {
        return asistP2;
    }

    public void setAsistP2(int asistP2) {
        this.asistP2 = asistP2;
    }

    public int getSesionesP1() {
        return sesionesP1;
    }

    public void setSesionesP1(int sesionesP1) {
        this.sesionesP1 = sesionesP1;
    }

    public int getSesionesP2() {
        return sesionesP2;
    }

    public void setSesionesP2(int sesionesP2) {
        this.sesionesP2 = sesionesP2;
    }

    public double getPromedio() {
        return promedio;
    }

    public void setPromedio(double promedio) {
        this.promedio = promedio;
    }
}
