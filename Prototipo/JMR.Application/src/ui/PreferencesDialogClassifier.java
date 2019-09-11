
package ui;

import java.awt.Dimension;
import jmr.application.SettingsClassifier;

/**
 * Diálogo para mostrar/modificar los parámetros del clasificador de la
 * aplicación
 * 
 * @author Míriam Mengíbar Rodríguez
 */
public class PreferencesDialogClassifier extends javax.swing.JDialog {

    /**
     * Valor devuelto en caso de cancelación
     */
    public static final int CANCEL_OPTION = 1;
    /**
     * Valor devuelto en caso de aceptación
     */
    public static final int APPROVE_OPTION = 0;
    /**
     * Valor devuelto (por cefecto, cancelación)
     */
    private int returnStatus = CANCEL_OPTION;
    
    public PreferencesDialogClassifier(java.awt.Frame parent) {
        super(parent, true);           //Siempre modal
        initComponents();              //Inicializacón de componentes -> NetBeans 
        inicializaGlobalSetings();     //Inicializamos variables globales
        setLocationRelativeTo(parent); //Posicionamos en centro
        
    }

    /**
     * Inicializa los campos del diálogo con los valores globales de la aplicación
     */
    private void inicializaGlobalSetings(){
        this.spUmbral.setValue(SettingsClassifier.getThresholdHeatmap());
        this.spHeight.setValue(SettingsClassifier.getHeigthWS());
        this.spWidth.setValue(SettingsClassifier.getWidthWS());
        this.spStep.setValue(SettingsClassifier.getStepSizeWS());
    }
    
    /**
     * Actualiza las preferencias de la aplicación (global settings) con los 
     * valores introducidos en los campos del diálogo.Este método se llama en 
     * caso de que el usuario pulse el botón 'Aceptar"
     */
    private void actualizaGlobalSetings(){  
        SettingsClassifier.setParameters((int)this.spWidth.getValue(), (int)this.spHeight.getValue(), (int)this.spStep.getValue(), (int)this.spUmbral.getValue());
    }
    
    
    /**
     * Muestra este diálogo de forma modal
     * 
     * @return el estado final (aceptado o cancelado)
     */
    public int showDialog(){
        this.setVisible(true);
        return returnStatus; //Dialogo modal -> no ejecutará el return hasta que no se cierre el diálogo
    }
    
    /**
     * Cierra este diálogo, actualizando previamente el estado
     * @param retStatus el estado del diálogo (aceptado o cancelado)
     */
    private void doClose(int retStatus) {
        returnStatus = retStatus;
        setVisible(false);
        dispose();
    }
    
    /*
     * Código generado por Netbeans para el diseño del interfaz
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        grupoBotonesPxPo = new javax.swing.ButtonGroup();
        panelShape = new javax.swing.JPanel();
        labelGridSize = new javax.swing.JLabel();
        labelNormali = new javax.swing.JLabel();
        labelWeighted = new javax.swing.JLabel();
        labelWeighted1 = new javax.swing.JLabel();
        spWidth = new javax.swing.JSpinner();
        spHeight = new javax.swing.JSpinner();
        spStep = new javax.swing.JSpinner();
        spUmbral = new javax.swing.JSpinner();
        panelBotones = new javax.swing.JPanel();
        botonAceptar = new javax.swing.JButton();
        botonCancelar = new javax.swing.JButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setTitle("Preferences");

        panelShape.setBorder(javax.swing.BorderFactory.createTitledBorder(null, "Settings Classifier", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Tahoma", 0, 14))); // NOI18N
        panelShape.setPreferredSize(new java.awt.Dimension(150, 150));

        labelGridSize.setFont(new java.awt.Font("Arial", 0, 12)); // NOI18N
        labelGridSize.setText("Threshold heatmap:");

        labelNormali.setFont(new java.awt.Font("Arial", 0, 12)); // NOI18N
        labelNormali.setText("Step size WS:");

        labelWeighted.setFont(new java.awt.Font("Arial", 0, 12)); // NOI18N
        labelWeighted.setText("Width WS:");

        labelWeighted1.setFont(new java.awt.Font("Arial", 0, 12)); // NOI18N
        labelWeighted1.setText("Height WS:");

        javax.swing.GroupLayout panelShapeLayout = new javax.swing.GroupLayout(panelShape);
        panelShape.setLayout(panelShapeLayout);
        panelShapeLayout.setHorizontalGroup(
            panelShapeLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panelShapeLayout.createSequentialGroup()
                .addGap(30, 30, 30)
                .addComponent(labelGridSize)
                .addGap(18, 18, 18)
                .addComponent(spUmbral, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
            .addGroup(panelShapeLayout.createSequentialGroup()
                .addGap(38, 38, 38)
                .addGroup(panelShapeLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addGroup(panelShapeLayout.createSequentialGroup()
                        .addComponent(labelNormali, javax.swing.GroupLayout.PREFERRED_SIZE, 75, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(18, 18, 18)
                        .addComponent(spStep, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(panelShapeLayout.createSequentialGroup()
                        .addComponent(labelWeighted, javax.swing.GroupLayout.PREFERRED_SIZE, 58, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(26, 26, 26)
                        .addComponent(spWidth, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 36, Short.MAX_VALUE)
                .addComponent(labelWeighted1, javax.swing.GroupLayout.PREFERRED_SIZE, 75, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(spHeight, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(23, 23, 23))
        );
        panelShapeLayout.setVerticalGroup(
            panelShapeLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panelShapeLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(panelShapeLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(labelWeighted, javax.swing.GroupLayout.PREFERRED_SIZE, 20, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(labelWeighted1, javax.swing.GroupLayout.PREFERRED_SIZE, 20, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(spWidth, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(spHeight, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(18, 18, 18)
                .addGroup(panelShapeLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(labelNormali, javax.swing.GroupLayout.PREFERRED_SIZE, 20, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(spStep, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(18, 18, 18)
                .addGroup(panelShapeLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(labelGridSize, javax.swing.GroupLayout.PREFERRED_SIZE, 20, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(spUmbral, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        getContentPane().add(panelShape, java.awt.BorderLayout.PAGE_START);

        panelBotones.setPreferredSize(new java.awt.Dimension(100, 30));
        panelBotones.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.RIGHT));

        botonAceptar.setText("OK");
        botonAceptar.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                botonAceptarActionPerformed(evt);
            }
        });
        panelBotones.add(botonAceptar);

        botonCancelar.setText("Cancel");
        botonCancelar.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                botonCancelarActionPerformed(evt);
            }
        });
        panelBotones.add(botonCancelar);

        getContentPane().add(panelBotones, java.awt.BorderLayout.SOUTH);

        setSize(new java.awt.Dimension(351, 221));
        setLocationRelativeTo(null);
    }// </editor-fold>//GEN-END:initComponents

    private void botonAceptarActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_botonAceptarActionPerformed
        actualizaGlobalSetings();
        doClose(APPROVE_OPTION);
    }//GEN-LAST:event_botonAceptarActionPerformed

    private void botonCancelarActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_botonCancelarActionPerformed
        doClose(CANCEL_OPTION);
    }//GEN-LAST:event_botonCancelarActionPerformed

    
    
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton botonAceptar;
    private javax.swing.JButton botonCancelar;
    private javax.swing.ButtonGroup grupoBotonesPxPo;
    private javax.swing.JLabel labelGridSize;
    private javax.swing.JLabel labelNormali;
    private javax.swing.JLabel labelWeighted;
    private javax.swing.JLabel labelWeighted1;
    private javax.swing.JPanel panelBotones;
    private javax.swing.JPanel panelShape;
    private javax.swing.JSpinner spHeight;
    private javax.swing.JSpinner spStep;
    private javax.swing.JSpinner spUmbral;
    private javax.swing.JSpinner spWidth;
    // End of variables declaration//GEN-END:variables
}
