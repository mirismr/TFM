/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jmr.application;

import javax.swing.JFrame;
import javax.swing.UIManager;

/**
 *
 * @author Jesús Chamorro Martínez (jesus@decsai.ugr.es)
 */
public class JMRApplication {

    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        //<editor-fold defaultstate="collapsed" desc=" Look and feel setting code (optional) ">       
        //setNimbusLF();
        //</editor-fold>        
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            JFrame.setDefaultLookAndFeelDecorated(true);
        } catch (Exception e) {
        }      
        JMRFrame ventana =  new JMRFrame();
        ventana.setSize(1300,800);
        ventana.setLocationRelativeTo(null);
        ventana.splitPanelCentral.setDividerLocation(1.0);
        ventana.setVisible(true);  
        
    }
    
    /**
     * Set the Nimbus look and feel
     */
    private static void setNimbusLF() {
        try {
            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (ClassNotFoundException | InstantiationException | IllegalAccessException | javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(JMRFrame.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
    }
    
}
