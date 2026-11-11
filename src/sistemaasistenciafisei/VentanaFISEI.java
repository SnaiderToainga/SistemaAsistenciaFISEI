/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/GUIForms/JFrame.java to edit this template
 */
package sistemaasistenciafisei;

/**
 *
 * @author User
 */
public class VentanaFISEI extends javax.swing.JFrame {
    
    MetodosEstudiante metodos = new MetodosEstudiante();
    MetodosAsistencia metodosAsistencia = new MetodosAsistencia();
    MetodosReporte metodosReporte = new MetodosReporte();
    private final MetodosLogin metodosLogin = new MetodosLogin();
    private final java.util.Map<String, Integer> sesionesPorCurso = new java.util.HashMap<>();
    private boolean mostrandoPromedio = false;

    public VentanaFISEI() {
        this("Administrador", 0, null); // Entra por defecto con todos los permisos
    }

    public VentanaFISEI(String rol) {
        this(rol, 0, null);
    }

    /**
     * Creates new form VentanaFISEI
     */
    public VentanaFISEI(String rol, int idMateriaDocente, String nombreMateria) {
        initComponents();
        this.setLocationRelativeTo(null);
        for (int idMat : new int[]{1, 2}) {
            sesionesPorCurso.put(claveCurso(idMat, "Parcial 1"), metodosReporte.obtenerSesionesRegistradas(idMat, "Parcial 1"));
            sesionesPorCurso.put(claveCurso(idMat, "Parcial 2"), metodosReporte.obtenerSesionesRegistradas(idMat, "Parcial 2"));
        }
        botonesModoNuevo();
        configurarEventosExtra();
        aplicarPermisosPorRol(rol, idMateriaDocente, nombreMateria);

        int idMatAsis = getIdMateriaSeleccionada(jcbxMateriaAsistencia);
        int idMatRep = getIdMateriaSeleccionada(jcbxMateriaReporte);
        cargarComboSesiones(idMatAsis, "Parcial 1", 1);
        metodos.cargarTabla(jtblEstudiantes);
        metodosAsistencia.cargarMarcacionesSesion(idMatAsis, "Parcial 1", 1, jtblMarcacionesSesion);
        metodosReporte.calcularConsolidado(idMatRep, "Parcial 1", getTotalSesiones(idMatRep, "Parcial 1"), jtblReporteFinal);
    }

    private String claveCurso(int idMateria, String parcial) {
        return idMateria + "-" + parcial;
    }

    private int getIdMateriaSeleccionada(javax.swing.JComboBox<String> combo) {
        return "Metodologías Ágiles".equals(combo.getSelectedItem()) ? 2 : 1;
    }

    private int getTotalSesiones(int idMateria, String parcial) {
        return sesionesPorCurso.getOrDefault(claveCurso(idMateria, parcial), 5);
    }

    private void cargarComboSesiones(int idMateria, String parcial, int sesionSeleccionada) {
        int total = getTotalSesiones(idMateria, parcial);
        javax.swing.DefaultComboBoxModel<String> modelo = new javax.swing.DefaultComboBoxModel<>();
        for (int i = 1; i <= total; i++) {
            modelo.addElement(String.valueOf(i));
        }
        if (sesionSeleccionada >= 1 && sesionSeleccionada <= total) {
            modelo.setSelectedItem(String.valueOf(sesionSeleccionada));
        }
        jcbxSesion.setModel(modelo);
    }
    
    private void aplicarPermisosPorRol(String rol, int idMateriaDocente, String nombreMateria) {
        if ("Docente".equalsIgnoreCase(rol)) {
            // El Docente no tiene acceso a matricular ni eliminar estudiantes
            jTabbedPane1.setEnabledAt(0, false);
            int indiceMateria = (idMateriaDocente == 2) ? 1 : 0;
            jcbxMateriaAsistencia.setSelectedIndex(indiceMateria);
            jcbxMateriaReporte.setSelectedIndex(indiceMateria);
            jcbxMateriaAsistencia.setEnabled(false);
            jcbxMateriaReporte.setEnabled(false);

            // Lo manda directo a la pestaña de pasar asistencia con el llavero RFID
            jTabbedPane1.setSelectedIndex(1);
            jtxtLectorRfid.requestFocusInWindow();
            String curso = (nombreMateria != null && !nombreMateria.isBlank()) ? " - " + nombreMateria : "";
            this.setTitle("Sistema Asistencia FISEI - [Perfil: Docente" + curso + "]");
        } else {
            // Administrador: acceso total a las 3 pestañas y ambas materias
            jTabbedPane1.setEnabledAt(0, true);
            jcbxMateriaAsistencia.setEnabled(true);
            jcbxMateriaReporte.setEnabled(true);
            jTabbedPane1.setSelectedIndex(0);
            this.setTitle("Sistema Asistencia FISEI - [Perfil: Administrador]");
        }
    }

    private void botonesModoNuevo() {
        jbtnNuevoEstudiante.setEnabled(true);
        jbtnGuardarEstudiante.setEnabled(true);
        jbtnModificarEstudiante.setEnabled(false);
        jbtnEliminarEstudiante.setEnabled(false);
    }

    private void botonesModoSeleccionado() {
        jbtnNuevoEstudiante.setEnabled(true);
        jbtnGuardarEstudiante.setEnabled(false);
        jbtnModificarEstudiante.setEnabled(true);
        jbtnEliminarEstudiante.setEnabled(true);
    }

    private void botonesModoEditando() {
        jbtnNuevoEstudiante.setEnabled(false);
        jbtnGuardarEstudiante.setEnabled(false);
        jbtnModificarEstudiante.setEnabled(true);
        jbtnEliminarEstudiante.setEnabled(false);
    }

    private void configurarEventosExtra() {
        // Detectar cuando el usuario empieza a editar cualquier campo de un estudiante seleccionado
        java.awt.event.KeyAdapter detectorEdicion = new java.awt.event.KeyAdapter() {
            @Override
            public void keyTyped(java.awt.event.KeyEvent evt) {
                char c = evt.getKeyChar();
                if (c != java.awt.event.KeyEvent.VK_ESCAPE && c != '\n' && metodos.estaEditando()) {
                    botonesModoEditando();
                }
            }

            @Override
            public void keyPressed(java.awt.event.KeyEvent evt) {
                // Si el usuario presiona Esc mientras edita, cancela y vuelve al modo normal
                if (evt.getKeyCode() == java.awt.event.KeyEvent.VK_ESCAPE && metodos.estaEditando()) {
                    metodos.limpiarCampos(jtxtCedula, jtxtNombre, jtxtApellidos, jtxtRfidMatricula,
                            jchkComputacionVisual, jchkMetodologiasAgiles);
                    jtblEstudiantes.clearSelection();
                    botonesModoNuevo();
                }
            }
        };
        jtxtCedula.addKeyListener(detectorEdicion);
        jtxtNombre.addKeyListener(detectorEdicion);
        jtxtApellidos.addKeyListener(detectorEdicion);
        jtxtRfidMatricula.addKeyListener(detectorEdicion);

        jcbxMateriaAsistencia.addActionListener(e -> {
            int idMat = getIdMateriaSeleccionada(jcbxMateriaAsistencia);
            String parcial = jcbxParcial.getSelectedItem().toString();
            cargarComboSesiones(idMat, parcial, 1);
            metodosAsistencia.cargarMarcacionesSesion(idMat, parcial, 1, jtblMarcacionesSesion);
            jtxtLectorRfid.requestFocusInWindow();
        });

        jcbxMateriaReporte.addActionListener(e -> {
            int idMat = getIdMateriaSeleccionada(jcbxMateriaReporte);
            if (mostrandoPromedio) {
                metodosReporte.calcularPromedioParciales(idMat, getTotalSesiones(idMat, "Parcial 1"), getTotalSesiones(idMat, "Parcial 2"), jtblReporteFinal);
            } else {
                String parcial = jcbxParcialReporte.getSelectedItem().toString();
                metodosReporte.calcularConsolidado(idMat, parcial, getTotalSesiones(idMat, parcial), jtblReporteFinal);
            }
        });

        // Al cambiar de pestaña, enfocar el lector RFID o refrescar la tabla de reportes
        jTabbedPane1.addChangeListener(new javax.swing.event.ChangeListener() {
            @Override
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                int pestana = jTabbedPane1.getSelectedIndex();
                if (pestana == 1) {
                    jtxtLectorRfid.requestFocusInWindow();
                } else if (pestana == 2) {
                    int idMat = getIdMateriaSeleccionada(jcbxMateriaReporte);
                    if (mostrandoPromedio) {
                        metodosReporte.calcularPromedioParciales(idMat, getTotalSesiones(idMat, "Parcial 1"), getTotalSesiones(idMat, "Parcial 2"), jtblReporteFinal);
                    } else {
                        String parcial = jcbxParcialReporte.getSelectedItem().toString();
                        metodosReporte.calcularConsolidado(idMat, parcial, getTotalSesiones(idMat, parcial), jtblReporteFinal);
                    }
                }
            }
        });
    }
   
    
    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jScrollPane1 = new javax.swing.JScrollPane();
        jTable1 = new javax.swing.JTable();
        jTabbedPane1 = new javax.swing.JTabbedPane();
        jPanel1 = new javax.swing.JPanel();
        jtxtCedula = new javax.swing.JTextField();
        jtxtNombre = new javax.swing.JTextField();
        jtxtApellidos = new javax.swing.JTextField();
        jtxtRfidMatricula = new javax.swing.JTextField();
        jLabel1 = new javax.swing.JLabel();
        jLabel2 = new javax.swing.JLabel();
        jLabel3 = new javax.swing.JLabel();
        jLabel4 = new javax.swing.JLabel();
        jLabel9 = new javax.swing.JLabel();
        jchkComputacionVisual = new javax.swing.JCheckBox();
        jchkMetodologiasAgiles = new javax.swing.JCheckBox();
        jbtnNuevoEstudiante = new javax.swing.JButton();
        jbtnGuardarEstudiante = new javax.swing.JButton();
        jbtnEliminarEstudiante = new javax.swing.JButton();
        jbtnModificarEstudiante = new javax.swing.JButton();
        jScrollPane2 = new javax.swing.JScrollPane();
        jtblEstudiantes = new javax.swing.JTable();
        jPanel2 = new javax.swing.JPanel();
        jLabel10 = new javax.swing.JLabel();
        jcbxMateriaAsistencia = new javax.swing.JComboBox<>();
        jcbxParcial = new javax.swing.JComboBox<>();
        jcbxSesion = new javax.swing.JComboBox<>();
        jbtnAgregarSesion = new javax.swing.JButton();
        jLabel5 = new javax.swing.JLabel();
        jtxtLectorRfid = new javax.swing.JTextField();
        jLabel6 = new javax.swing.JLabel();
        jLabel7 = new javax.swing.JLabel();
        jScrollPane3 = new javax.swing.JScrollPane();
        jtblMarcacionesSesion = new javax.swing.JTable();
        jlblMensajeAsistencia = new javax.swing.JLabel();
        jPanel3 = new javax.swing.JPanel();
        jLabel11 = new javax.swing.JLabel();
        jcbxMateriaReporte = new javax.swing.JComboBox<>();
        jcbxParcialReporte = new javax.swing.JComboBox<>();
        jLabel8 = new javax.swing.JLabel();
        jbtnCalcularReporte = new javax.swing.JButton();
        jbtnPromedioParciales = new javax.swing.JButton();
        jbtnImprimirJasper = new javax.swing.JButton();
        jScrollPane4 = new javax.swing.JScrollPane();
        jtblReporteFinal = new javax.swing.JTable();
        jbtnCerrarSesion = new javax.swing.JButton();

        jTable1.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null}
            },
            new String [] {
                "Title 1", "Title 2", "Title 3", "Title 4"
            }
        ));
        jScrollPane1.setViewportView(jTable1);

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);

        jtxtRfidMatricula.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jtxtRfidMatriculaActionPerformed(evt);
            }
        });

        jLabel1.setText("Cedula:");

        jLabel2.setText("Nombres:");

        jLabel3.setText("Apellidos:");

        jLabel4.setText("Id_Matricula");

        jLabel9.setText("Materias:");

        jchkComputacionVisual.setText("Computación Visual");

        jchkMetodologiasAgiles.setText("Metodologías Ágiles");

        jbtnNuevoEstudiante.setText("Nuevo");
        jbtnNuevoEstudiante.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jbtnNuevoEstudianteActionPerformed(evt);
            }
        });

        jbtnGuardarEstudiante.setText("Guardar");
        jbtnGuardarEstudiante.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jbtnGuardarEstudianteActionPerformed(evt);
            }
        });

        jbtnEliminarEstudiante.setText("Eliminar");
        jbtnEliminarEstudiante.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jbtnEliminarEstudianteActionPerformed(evt);
            }
        });

        jbtnModificarEstudiante.setText("Modificar");
        jbtnModificarEstudiante.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jbtnModificarEstudianteActionPerformed(evt);
            }
        });

        jtblEstudiantes.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null}
            },
            new String [] {
                "Title 1", "Title 2", "Title 3", "Title 4"
            }
        ));
        jtblEstudiantes.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                jtblEstudiantesMouseClicked(evt);
            }
        });
        jScrollPane2.setViewportView(jtblEstudiantes);

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel1Layout.createSequentialGroup()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(jbtnGuardarEstudiante, javax.swing.GroupLayout.PREFERRED_SIZE, 84, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addGap(16, 16, 16)
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                .addComponent(jLabel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addComponent(jLabel2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addComponent(jLabel3, javax.swing.GroupLayout.DEFAULT_SIZE, 57, Short.MAX_VALUE))
                            .addComponent(jLabel4)
                            .addComponent(jLabel9))
                        .addGap(4, 4, 4)
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jtxtRfidMatricula)
                            .addComponent(jtxtNombre)
                            .addComponent(jtxtApellidos)
                            .addComponent(jtxtCedula, javax.swing.GroupLayout.PREFERRED_SIZE, 169, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addGroup(jPanel1Layout.createSequentialGroup()
                                .addComponent(jchkComputacionVisual)
                                .addGap(12, 12, 12)
                                .addComponent(jchkMetodologiasAgiles)))
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(jPanel1Layout.createSequentialGroup()
                                .addGap(76, 76, 76)
                                .addComponent(jbtnModificarEstudiante))
                            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel1Layout.createSequentialGroup()
                                .addGap(77, 77, 77)
                                .addComponent(jbtnEliminarEstudiante, javax.swing.GroupLayout.PREFERRED_SIZE, 84, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addGroup(jPanel1Layout.createSequentialGroup()
                                .addGap(77, 77, 77)
                                .addComponent(jbtnNuevoEstudiante, javax.swing.GroupLayout.PREFERRED_SIZE, 84, javax.swing.GroupLayout.PREFERRED_SIZE)))))
                .addGap(26, 26, 26))
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addGap(26, 26, 26)
                .addComponent(jScrollPane2)
                .addGap(26, 26, 26))
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addGap(19, 19, 19)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel1)
                    .addComponent(jtxtCedula, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jbtnNuevoEstudiante))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jtxtNombre, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel2)
                    .addComponent(jbtnGuardarEstudiante))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jtxtApellidos, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel3)
                    .addComponent(jbtnEliminarEstudiante))
                .addGap(11, 11, 11)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel4)
                    .addComponent(jtxtRfidMatricula, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jbtnModificarEstudiante))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel9)
                    .addComponent(jchkComputacionVisual)
                    .addComponent(jchkMetodologiasAgiles))
                .addGap(24, 24, 24)
                .addComponent(jScrollPane2, javax.swing.GroupLayout.DEFAULT_SIZE, 356, Short.MAX_VALUE)
                .addContainerGap())
        );

        jTabbedPane1.addTab("Matriculacion", jPanel1);

        jLabel10.setText("Seleccione Materia");

        jcbxMateriaAsistencia.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Computación Visual", "Metodologías Ágiles" }));

        jcbxParcial.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Parcial 1", "Parcial 2" }));
        jcbxParcial.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jcbxParcialActionPerformed(evt);
            }
        });

        jcbxSesion.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "1", "2", "3", "4", "5" }));
        jcbxSesion.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jcbxSesionActionPerformed(evt);
            }
        });

        jbtnAgregarSesion.setText("+ Sesión");
        jbtnAgregarSesion.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jbtnAgregarSesionActionPerformed(evt);
            }
        });

        jLabel5.setText("\"Acercar Llavero\"");

        jtxtLectorRfid.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jtxtLectorRfidActionPerformed(evt);
            }
        });

        jLabel6.setText("Seleccione Parcial");

        jLabel7.setText("Seleccione Sesion");

        jtblMarcacionesSesion.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null}
            },
            new String [] {
                "Title 1", "Title 2", "Title 3", "Title 4"
            }
        ));
        jScrollPane3.setViewportView(jtblMarcacionesSesion);

        jlblMensajeAsistencia.setText("Esperando Marcacion:");

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addGap(15, 15, 15)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(jLabel10, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(jLabel6, javax.swing.GroupLayout.PREFERRED_SIZE, 109, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jLabel7, javax.swing.GroupLayout.DEFAULT_SIZE, 115, Short.MAX_VALUE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jcbxMateriaAsistencia, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addGroup(jPanel2Layout.createSequentialGroup()
                                .addComponent(jcbxSesion, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(jbtnAgregarSesion))
                            .addComponent(jcbxParcial, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                    .addComponent(jLabel5, javax.swing.GroupLayout.PREFERRED_SIZE, 132, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jScrollPane3, javax.swing.GroupLayout.DEFAULT_SIZE, 630, Short.MAX_VALUE)
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addComponent(jlblMensajeAsistencia, javax.swing.GroupLayout.DEFAULT_SIZE, 592, Short.MAX_VALUE)
                        .addGap(38, 38, 38))
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addComponent(jtxtLectorRfid, javax.swing.GroupLayout.DEFAULT_SIZE, 330, Short.MAX_VALUE)
                        .addGap(300, 300, 300)))
                .addGap(15, 15, 15))
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addGap(14, 14, 14)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jcbxMateriaAsistencia, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel10))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jcbxParcial, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel6))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jcbxSesion, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel7)
                    .addComponent(jbtnAgregarSesion))
                .addGap(24, 24, 24)
                .addComponent(jLabel5)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jtxtLectorRfid, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jlblMensajeAsistencia)
                .addGap(24, 24, 24)
                .addComponent(jScrollPane3, javax.swing.GroupLayout.DEFAULT_SIZE, 374, Short.MAX_VALUE))
        );

        jTabbedPane1.addTab("Asistencia RFID", jPanel2);

        jLabel11.setText("Seleccione la materia");

        jcbxMateriaReporte.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Computación Visual", "Metodologías Ágiles" }));

        jcbxParcialReporte.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Parcial 1", "Parcial 2" }));
        jcbxParcialReporte.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jcbxParcialReporteActionPerformed(evt);
            }
        });

        jLabel8.setText("Seleccione el parcial");

        jbtnCalcularReporte.setText("Reporte");
        jbtnCalcularReporte.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jbtnCalcularReporteActionPerformed(evt);
            }
        });

        jbtnPromedioParciales.setText("Promedio Final");
        jbtnPromedioParciales.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jbtnPromedioParcialesActionPerformed(evt);
            }
        });

        jbtnImprimirJasper.setText("Imprimir");
        jbtnImprimirJasper.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jbtnImprimirJasperActionPerformed(evt);
            }
        });

        jtblReporteFinal.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null, null, null, null},
                {null, null, null, null, null},
                {null, null, null, null, null},
                {null, null, null, null, null}
            },
            new String [] {
                "Cédula", "Estudiante", "Asistencia", "Porcentaje", "Estado"
            }
        ));
        jScrollPane4.setViewportView(jtblReporteFinal);

        javax.swing.GroupLayout jPanel3Layout = new javax.swing.GroupLayout(jPanel3);
        jPanel3.setLayout(jPanel3Layout);
        jPanel3Layout.setHorizontalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addGap(15, 15, 15)
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jScrollPane4, javax.swing.GroupLayout.DEFAULT_SIZE, 630, Short.MAX_VALUE)
                    .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                        .addGroup(jPanel3Layout.createSequentialGroup()
                            .addComponent(jbtnCalcularReporte)
                            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                            .addComponent(jbtnPromedioParciales)
                            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                            .addComponent(jbtnImprimirJasper))
                        .addGroup(jPanel3Layout.createSequentialGroup()
                            .addComponent(jLabel11, javax.swing.GroupLayout.PREFERRED_SIZE, 129, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                            .addComponent(jcbxMateriaReporte, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGroup(jPanel3Layout.createSequentialGroup()
                            .addComponent(jLabel8, javax.swing.GroupLayout.PREFERRED_SIZE, 129, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                            .addComponent(jcbxParcialReporte, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))))
                .addGap(15, 15, 15))
        );
        jPanel3Layout.setVerticalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jcbxMateriaReporte, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel11))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jcbxParcialReporte, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel8))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jbtnCalcularReporte)
                    .addComponent(jbtnPromedioParciales)
                    .addComponent(jbtnImprimirJasper))
                .addGap(18, 18, 18)
                .addComponent(jScrollPane4, javax.swing.GroupLayout.DEFAULT_SIZE, 445, Short.MAX_VALUE)
                .addGap(15, 15, 15))
        );

        jTabbedPane1.addTab("Reporte y Estado", jPanel3);

        jbtnCerrarSesion.setText("Cerrar Sesión");
        jbtnCerrarSesion.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jbtnCerrarSesionActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
            .addComponent(jTabbedPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 660, Short.MAX_VALUE)
            .addGroup(layout.createSequentialGroup()
                .addComponent(jbtnCerrarSesion)
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jbtnCerrarSesion)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jTabbedPane1))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void jbtnCerrarSesionActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jbtnCerrarSesionActionPerformed
        metodosLogin.cerrarSesion(this);
    }//GEN-LAST:event_jbtnCerrarSesionActionPerformed

    private void jbtnEliminarEstudianteActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jbtnEliminarEstudianteActionPerformed
        if (metodos.eliminar(jtblEstudiantes, jtxtCedula, jtxtNombre, jtxtApellidos, jtxtRfidMatricula,
                jchkComputacionVisual, jchkMetodologiasAgiles)) {
            botonesModoNuevo();
        }
    }//GEN-LAST:event_jbtnEliminarEstudianteActionPerformed

    private void jbtnImprimirJasperActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jbtnImprimirJasperActionPerformed
        int idMat = getIdMateriaSeleccionada(jcbxMateriaReporte);
        String nombreMateria = jcbxMateriaReporte.getSelectedItem().toString();
        if (mostrandoPromedio) {
            metodosReporte.exportarPdfPromedioJasper(idMat, nombreMateria,
                    getTotalSesiones(idMat, "Parcial 1"), getTotalSesiones(idMat, "Parcial 2"));
        } else {
            String parcial = jcbxParcialReporte.getSelectedItem().toString();
            metodosReporte.exportarPdfJasper(idMat, nombreMateria, parcial, getTotalSesiones(idMat, parcial));
        }
    }//GEN-LAST:event_jbtnImprimirJasperActionPerformed

    private void jbtnPromedioParcialesActionPerformed(java.awt.event.ActionEvent evt) {
        mostrandoPromedio = true;
        int idMat = getIdMateriaSeleccionada(jcbxMateriaReporte);
        String nombreMateria = jcbxMateriaReporte.getSelectedItem().toString();
        int sesionesP1 = getTotalSesiones(idMat, "Parcial 1");
        int sesionesP2 = getTotalSesiones(idMat, "Parcial 2");
        metodosReporte.calcularPromedioParciales(idMat, sesionesP1, sesionesP2, jtblReporteFinal);
        metodosReporte.mostrarResumen("Promedio Final (P1 y P2)", nombreMateria, jtblReporteFinal);
        metodosReporte.exportarPdfPromedioJasper(idMat, nombreMateria, sesionesP1, sesionesP2);
    }

    private void jbtnCalcularReporteActionPerformed(java.awt.event.ActionEvent evt) {
        mostrandoPromedio = false;
        int idMat = getIdMateriaSeleccionada(jcbxMateriaReporte);
        String nombreMateria = jcbxMateriaReporte.getSelectedItem().toString();
        String parcial = jcbxParcialReporte.getSelectedItem().toString();
        metodosReporte.calcularConsolidado(idMat, parcial, getTotalSesiones(idMat, parcial), jtblReporteFinal);
        metodosReporte.mostrarResumen(parcial, nombreMateria, jtblReporteFinal);
    }

    private void jcbxParcialReporteActionPerformed(java.awt.event.ActionEvent evt) {
        mostrandoPromedio = false;
        int idMat = getIdMateriaSeleccionada(jcbxMateriaReporte);
        String parcial = jcbxParcialReporte.getSelectedItem().toString();
        metodosReporte.calcularConsolidado(idMat, parcial, getTotalSesiones(idMat, parcial), jtblReporteFinal);
    }

    private void jbtnNuevoEstudianteActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jbtnNuevoEstudianteActionPerformed
        metodos.limpiarCampos(jtxtCedula, jtxtNombre, jtxtApellidos, jtxtRfidMatricula,
                jchkComputacionVisual, jchkMetodologiasAgiles);
        jtblEstudiantes.clearSelection();
        botonesModoNuevo();
    }//GEN-LAST:event_jbtnNuevoEstudianteActionPerformed

    private void jbtnGuardarEstudianteActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jbtnGuardarEstudianteActionPerformed
        if (metodos.guardar(jtxtCedula, jtxtNombre, jtxtApellidos, jtxtRfidMatricula,
                jchkComputacionVisual, jchkMetodologiasAgiles, jtblEstudiantes)) {
            botonesModoNuevo();
        }
    }//GEN-LAST:event_jbtnGuardarEstudianteActionPerformed

    private void jbtnModificarEstudianteActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jbtnModificarEstudianteActionPerformed
        if (metodos.modificar(jtxtCedula, jtxtNombre, jtxtApellidos, jtxtRfidMatricula,
                jchkComputacionVisual, jchkMetodologiasAgiles, jtblEstudiantes)) {
            botonesModoNuevo();
        }
    }//GEN-LAST:event_jbtnModificarEstudianteActionPerformed

    private void jtxtRfidMatriculaActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jtxtRfidMatriculaActionPerformed
        boolean yaEstabaEditando = metodos.estaEditando();
        metodos.detectarRfid(jtxtRfidMatricula, jtxtCedula, jtxtNombre, jtxtApellidos,
                jchkComputacionVisual, jchkMetodologiasAgiles);
        if (yaEstabaEditando) {
            botonesModoEditando();
        } else if (metodos.estaEditando()) {
            botonesModoSeleccionado();
        }
    }//GEN-LAST:event_jtxtRfidMatriculaActionPerformed

    private void jtblEstudiantesMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jtblEstudiantesMouseClicked
        metodos.seleccionarFila(jtblEstudiantes, jtxtRfidMatricula, jtxtCedula, jtxtNombre, jtxtApellidos,
                jchkComputacionVisual, jchkMetodologiasAgiles);
        if (metodos.estaEditando()) {
            botonesModoSeleccionado();
        }
    }//GEN-LAST:event_jtblEstudiantesMouseClicked

    private void jbtnAgregarSesionActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jbtnAgregarSesionActionPerformed
        int confirmacion = javax.swing.JOptionPane.showConfirmDialog(
                this,
                "¿Quiere agregar una nueva sesión?",
                "Confirmar nueva sesión",
                javax.swing.JOptionPane.YES_NO_OPTION
        );
        if (confirmacion != javax.swing.JOptionPane.YES_OPTION) {
            jtxtLectorRfid.requestFocusInWindow();
            return;
        }

        int idMat = getIdMateriaSeleccionada(jcbxMateriaAsistencia);
        String parcial = jcbxParcial.getSelectedItem().toString();
        int nuevaSesion = getTotalSesiones(idMat, parcial) + 1;
        sesionesPorCurso.put(claveCurso(idMat, parcial), nuevaSesion);

        cargarComboSesiones(idMat, parcial, nuevaSesion);
        metodosAsistencia.cargarMarcacionesSesion(idMat, parcial, nuevaSesion, jtblMarcacionesSesion);
        jlblMensajeAsistencia.setText("Sesión " + nuevaSesion + " habilitada en " + parcial + ".");
        jtxtLectorRfid.requestFocusInWindow();
    }//GEN-LAST:event_jbtnAgregarSesionActionPerformed

    private void jcbxParcialActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jcbxParcialActionPerformed
        int idMat = getIdMateriaSeleccionada(jcbxMateriaAsistencia);
        String parcial = jcbxParcial.getSelectedItem().toString();
        cargarComboSesiones(idMat, parcial, 1);
        metodosAsistencia.cargarMarcacionesSesion(idMat, parcial, 1, jtblMarcacionesSesion);
        jtxtLectorRfid.requestFocusInWindow();
    }//GEN-LAST:event_jcbxParcialActionPerformed

    private void jcbxSesionActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jcbxSesionActionPerformed
        if (jcbxSesion.getSelectedItem() == null) {
            return;
        }
        int idMat = getIdMateriaSeleccionada(jcbxMateriaAsistencia);
        String parcial = jcbxParcial.getSelectedItem().toString();
        int sesion = Integer.parseInt(jcbxSesion.getSelectedItem().toString());
        metodosAsistencia.cargarMarcacionesSesion(idMat, parcial, sesion, jtblMarcacionesSesion);
        jtxtLectorRfid.requestFocusInWindow();
    }//GEN-LAST:event_jcbxSesionActionPerformed

    private void jtxtLectorRfidActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jtxtLectorRfidActionPerformed
        int idMat = getIdMateriaSeleccionada(jcbxMateriaAsistencia);
        String parcial = jcbxParcial.getSelectedItem().toString();
        int sesion = Integer.parseInt(jcbxSesion.getSelectedItem().toString());
        metodosAsistencia.registrarMarcacion(jtxtLectorRfid.getText(), idMat, parcial, sesion, jtblMarcacionesSesion, jlblMensajeAsistencia);
        jtxtLectorRfid.setText("");
        jtxtLectorRfid.requestFocus();
    }//GEN-LAST:event_jtxtLectorRfidActionPerformed

    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        /* Set the Nimbus look and feel */
        //<editor-fold defaultstate="collapsed" desc=" Look and feel setting code (optional) ">
        /* If Nimbus (introduced in Java SE 6) is not available, stay with the default look and feel.
         * For details see http://download.oracle.com/javase/tutorial/uiswing/lookandfeel/plaf.html 
         */
        try {
            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (ClassNotFoundException ex) {
            java.util.logging.Logger.getLogger(VentanaFISEI.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(VentanaFISEI.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(VentanaFISEI.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(VentanaFISEI.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>

        /* Create and display the form */
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                new VentanaFISEI().setVisible(true);
            }
        });
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel10;
    private javax.swing.JLabel jLabel11;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JLabel jLabel8;
    private javax.swing.JLabel jLabel9;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JScrollPane jScrollPane3;
    private javax.swing.JScrollPane jScrollPane4;
    private javax.swing.JTabbedPane jTabbedPane1;
    private javax.swing.JTable jTable1;
    private javax.swing.JButton jbtnAgregarSesion;
    private javax.swing.JButton jbtnCalcularReporte;
    private javax.swing.JButton jbtnCerrarSesion;
    private javax.swing.JButton jbtnEliminarEstudiante;
    private javax.swing.JButton jbtnGuardarEstudiante;
    private javax.swing.JButton jbtnImprimirJasper;
    private javax.swing.JButton jbtnModificarEstudiante;
    private javax.swing.JButton jbtnNuevoEstudiante;
    private javax.swing.JButton jbtnPromedioParciales;
    private javax.swing.JComboBox<String> jcbxMateriaAsistencia;
    private javax.swing.JComboBox<String> jcbxMateriaReporte;
    private javax.swing.JComboBox<String> jcbxParcial;
    private javax.swing.JComboBox<String> jcbxParcialReporte;
    private javax.swing.JComboBox<String> jcbxSesion;
    private javax.swing.JCheckBox jchkComputacionVisual;
    private javax.swing.JCheckBox jchkMetodologiasAgiles;
    private javax.swing.JLabel jlblMensajeAsistencia;
    private javax.swing.JTable jtblEstudiantes;
    private javax.swing.JTable jtblMarcacionesSesion;
    private javax.swing.JTable jtblReporteFinal;
    private javax.swing.JTextField jtxtApellidos;
    private javax.swing.JTextField jtxtCedula;
    private javax.swing.JTextField jtxtLectorRfid;
    private javax.swing.JTextField jtxtNombre;
    private javax.swing.JTextField jtxtRfidMatricula;
    // End of variables declaration//GEN-END:variables
}
