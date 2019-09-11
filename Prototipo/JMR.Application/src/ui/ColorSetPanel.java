package ui;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.RenderingHints;
import java.awt.geom.Ellipse2D;
import java.util.ArrayList;

/**
 *
 * @author Jesús Chamorro Martínez (jesus@decsai.ugr.es)
 */
public class ColorSetPanel extends javax.swing.JPanel {

    private ArrayList<Circulo> colores = new ArrayList();
    private int LADO = 25;
    private int actualX = LADO/2, actualY = LADO/2;
    private String label = "";
    
    /**
     * Creates new form ColorSetPanel
     */
    public ColorSetPanel() {
        initComponents();
        this.setPreferredSize(new Dimension(LADO*2,LADO*2));
    }

    @Override
    public void paint(Graphics g){
        super.paint(g);
        Graphics2D g2d = (Graphics2D)g;
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON);
        for(Circulo c:colores){
            g2d.setColor(c.color);
            g2d.fill(c);
            g2d.setColor(Color.BLACK);
            g2d.draw(c);
        }
        if(!"".equals(label)){
            g2d.setFont(new Font("Arial",Font.PLAIN,7));
            Point p = this.getMousePosition();
            if(p!=null) g2d.drawString(label,p.x-LADO/2,p.y);
        }
    }
    
    public void addColor(Color color){
        colores.add(new Circulo(actualX,actualY,LADO,LADO, color));
        actualY+=LADO+3;
    }
    
    
    /**
     * NetBeans code
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        setBorder(javax.swing.BorderFactory.createEtchedBorder());
        setPreferredSize(new java.awt.Dimension(30, 0));
        addMouseMotionListener(new java.awt.event.MouseMotionAdapter() {
            public void mouseMoved(java.awt.event.MouseEvent evt) {
                formMouseMoved(evt);
            }
        });

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 36, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 223, Short.MAX_VALUE)
        );
    }// </editor-fold>//GEN-END:initComponents

    private void formMouseMoved(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_formMouseMoved
        Point p = evt.getPoint();
        label = "";
        for(Circulo c:colores){
            if(c.contains(p)){
               label = "[" + c.color.getRed() + "," + c.color.getGreen() + "," + c.color.getBlue()+"]"; 
            }
        }
        this.repaint();
    }//GEN-LAST:event_formMouseMoved
    
    /**
     * Clase interna para representar un círculo coloreado
     */
    private class Circulo extends Ellipse2D.Float {
        public final Color color;
        
        public Circulo(Point origen, Dimension size, Color color){
            this(origen.x,origen.y,size.width,size.height,color);
        }
        
        public Circulo(int x, int y, int w, int h, Color color){
            super(x,y,w,h);
            this.color = color;
        }
    }
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    // End of variables declaration//GEN-END:variables
}
