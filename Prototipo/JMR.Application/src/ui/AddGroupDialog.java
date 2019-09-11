
package ui;

import java.util.ArrayList;
import javax.swing.DefaultListModel;
import jmr.application.LabelGroup;

/**
 * Diálogo para mostrar/modificar los parámetros globales (preferencias) de la
 * aplicación
 * 
 * @author Jesús Chamorro Martínez (jesus@decsai.ugr.es)
 */
public class AddGroupDialog extends javax.swing.JDialog {

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
    
    private ArrayList<LabelGroup> listLabels;
    
    public AddGroupDialog(java.awt.Frame parent, ArrayList<LabelGroup> listLabels) {
        super(parent, true);           //Siempre modal
        initComponents();              //Inicializacón de componentes -> NetBeans 
        setLocationRelativeTo(parent); //Posicionamos en centro
        
        this.listLabels = listLabels;
        DefaultListModel<String> listModel = new DefaultListModel<>();
        for(LabelGroup lg : this.listLabels) {
            for(String s : lg.getLabels()) {
                listModel.addElement(s);
            }
        }
        
        this.listaLabels.setModel(listModel);
    }
    
    /**
     * Muestra este diálogo de forma modal
     * 
     * @return el estado final (aceptado o cancelado)
     */
    public ArrayList<String> showDialog(){
        this.setVisible(true);
        ArrayList<String> result = new ArrayList<String>();
        
        int indices [] = this.listaLabels.getSelectedIndices();
        
        for(int i : indices) {
            for(String s : this.listLabels.get(i).getLabels()) {
                result.add(s);
            }
        }
        
        return result; //Dialogo modal -> no ejecutará el return hasta que no se cierre el diálogo
    }
    
    /**
     * Cierra este diálogo, actualizando previamente el estado
     * @param retStatus el estado del diálogo (aceptado o cancelado)
     */
    private void doClose(int retStatus) {
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
        panelBotones = new javax.swing.JPanel();
        botonAceptar = new javax.swing.JButton();
        botonCancelar = new javax.swing.JButton();
        jScrollPane2 = new javax.swing.JScrollPane();
        listaLabels = new javax.swing.JList<>();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setTitle("Add group label");

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

        jScrollPane2.setViewportView(listaLabels);

        getContentPane().add(jScrollPane2, java.awt.BorderLayout.CENTER);

        setSize(new java.awt.Dimension(416, 339));
        setLocationRelativeTo(null);
    }// </editor-fold>//GEN-END:initComponents

    private void botonCancelarActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_botonCancelarActionPerformed
        doClose(CANCEL_OPTION);
    }//GEN-LAST:event_botonCancelarActionPerformed

    private void botonAceptarActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_botonAceptarActionPerformed

        doClose(APPROVE_OPTION);
    }//GEN-LAST:event_botonAceptarActionPerformed

    
    
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton botonAceptar;
    private javax.swing.JButton botonCancelar;
    private javax.swing.ButtonGroup grupoBotonesPxPo;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JList<String> listaLabels;
    private javax.swing.JPanel panelBotones;
    // End of variables declaration//GEN-END:variables
}
