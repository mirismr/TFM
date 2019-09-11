package jmr.iu;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;

import javax.swing.*;
import jmr.descriptor.Comparator;
import jmr.descriptor.color.SingleColorDescriptor;
import jmr.descriptor.label.Classifier;
import jmr.descriptor.label.LabelDescriptor;
import jmr.descriptor.label.LabelDescriptor.WeightBasedComparator;
import jmr.descriptor.label.SingleLabelDescriptor;
import jmr.media.JMRBufferedImage;
import jmr.video.FrameCollection;
import jmr.video.FrameCollectionIO;
import jmr.video.KeyFrameDescriptor;
import jmr.video.MinMinComparator;


public class Test {
    boolean packFrame = false;

    /**
     * Construct and show the application.
     */
    public Test() {
        TestLabel();
    }
    
    
    private void TestColor(){
        JMRBufferedImage img = null;
        SingleColorDescriptor d = new SingleColorDescriptor(img);
        System.out.println("Color: " + d.getColor());
        SingleColorDescriptor d2 = new SingleColorDescriptor(Color.LIGHT_GRAY);
        System.out.println("Color: " + d2.getColor());
        SingleColorDescriptor d3 = new SingleColorDescriptor(Color.WHITE);
        System.out.println("Color: " + d3.getColor());
        
        Object o;
        o = d2.compare(d3);
        System.out.println("Output: " + o + " de tipo " + o.getClass());
        Comparator<SingleColorDescriptor, Double> c = (a,b)->1.0;
        d2.setComparator(c);
        o = d2.compare(d3);
        System.out.println("Output: " + o + " de tipo " + o.getClass());
        Comparator<SingleColorDescriptor, Color> c2 = (a,b)->Color.blue;
        d2.setComparator(c2);
        o = d2.compare(d3);
        System.out.println("Output: " + o + " de tipo " + o.getClass()); 
    }
    
    private void TestVideo(){
        File file = new File("C:\\Users\\Jesús\\Documents\\_JMR_TestImages\\video");
        FrameCollection fc = FrameCollectionIO.read(file);
        KeyFrameDescriptor kfd = new KeyFrameDescriptor(fc,jmr.descriptor.color.SingleColorDescriptor.class);
                
        System.out.println("KFD: \n"+kfd);
        
        file = new File("C:\\Users\\Jesús\\Documents\\_JMR_TestImages\\036.jpg");
        FrameCollection fc2 = FrameCollectionIO.read(file);
        KeyFrameDescriptor kfd2 = new KeyFrameDescriptor(fc2,jmr.descriptor.color.SingleColorDescriptor.class);
        System.out.println("KFD: \n"+kfd2);
        
        Double dist = kfd.compare(kfd2);
        System.out.println(dist);
        
        Comparator<KeyFrameDescriptor, Double> ckfd = new MinMinComparator();
        kfd.setComparator(ckfd);
        dist = kfd.compare(kfd2);
        System.out.println(dist);
        
    } 
    
    private Double miApply(String s){
        return 2.0;
    }
    
     private void TestLabel(){
         
         BufferedImage img = new BufferedImage(100,100,BufferedImage.TYPE_INT_RGB);
         
//         SingleLabelDescriptor<BufferedImage> slabel1 = new SingleLabelDescriptor("Hola");
//         SingleLabelDescriptor<BufferedImage> slabel2 = new SingleLabelDescriptor("Hola");
//         System.out.println(slabel1+"\n"+slabel2+"\n Distancia:"+slabel1.compare(slabel2));
//         
//         slabel1 = new SingleLabelDescriptor(img);
//         System.out.println(slabel1+"\n"+slabel2+"\n Distancia:"+slabel1.compare(slabel2));
         
         LabelDescriptor<BufferedImage> mlabel1 = new LabelDescriptor("Hola","Adios","Casa");
         LabelDescriptor<BufferedImage> mlabel2 = new LabelDescriptor("Adios","Hola","Casa");
         LabelDescriptor<BufferedImage> mlabel3 = new LabelDescriptor("Hola","Salta");
         mlabel1.setWeights(1.7,3.0,0.7);
         mlabel2.setWeights(1.7,1.0,0.7);
                  
         System.out.println(mlabel1+"\n"+mlabel2);    
         
         System.out.println("SoftIncluded: "+mlabel1.isSoftIncluded(mlabel3));
         
         mlabel1.setComparator(new LabelDescriptor.InclusionComparator());
         System.out.println(" Distancia Inclusion (sin peso):"+mlabel1.compare(mlabel2));          
         mlabel1.setComparator(new LabelDescriptor.EqualComparator());
         System.out.println(" Distancia Equal (sin peso):"+mlabel1.compare(mlabel2)); 
         mlabel1.setComparator(new LabelDescriptor.WeightBasedComparator());  // TYPE_EUCLIDEAN
         System.out.println(" Distancia WEqual (euclídea):"+mlabel1.compare(mlabel2)); 
         mlabel1.setComparator(new WeightBasedComparator(WeightBasedComparator.TYPE_MIN));
         System.out.println(" Distancia WEqual (min):"+mlabel1.compare(mlabel2)); 
         mlabel1.setComparator(new WeightBasedComparator(WeightBasedComparator.TYPE_MAX));
         System.out.println(" Distancia WEqual (max):"+mlabel1.compare(mlabel2)); 
         mlabel1.setComparator(new WeightBasedComparator(WeightBasedComparator.TYPE_MEAN));
         System.out.println(" Distancia WEqual (mean):"+mlabel1.compare(mlabel2)); 
         
         mlabel1.setComparator(new WeightBasedComparator(WeightBasedComparator.TYPE_MEAN,true));
         System.out.println(" Distancia WInclusion (mean):"+mlabel1.compare(mlabel2)); 
         
         
         
         mlabel1 = new LabelDescriptor(img);
         System.out.println(mlabel1+"\n"+mlabel2+"\n Distancia:"+mlabel1.compare(mlabel2));  
         
         LabelDescriptor<BufferedImage> label1 = new LabelDescriptor(img);
         LabelDescriptor<BufferedImage> label2 = new LabelDescriptor("Adios");
         System.out.println(label1+"\n"+label2+"\n Distancia:"+label1.compare(label2));
                  
     }

    /**
     * Application entry point.
     *
     * @param args String[]
     */
    public static void main(String[] args) {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {                
                new Test();
            }
        });
    }
}
