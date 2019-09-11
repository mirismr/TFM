/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package detection;

import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import jmr.descriptor.label.Classifier;
import jmr.descriptor.label.LabeledClassification;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

/**
 * A classifier representing a Convolutional Neural Network from Keras.
 *
 * @author Míriam Mengíbar Rodríguez (mirismr@correo.ugr.es)
 */
public class PythonClassifier implements Classifier<String, LabeledClassification> {

    protected TCPClient tcpClient;
    /**
     * Indicates if the classifier is weighted
     */
    protected boolean activeRCNN;

    /**
     * Constructs a classifier, initializes it from the architecture given by
     * parameter. Only for internal library' use
     *
     
     */
    public PythonClassifier() {

        // create tcp client connecting to python scripts
        this.tcpClient = new TCPClient("localhost", 5000);
        this.activeRCNN = false;
    }

    /**
     * Given a image it returns the labels (and the weights if the classifier is
     * weighted) from the classes found in that image.
     *
     * @param image the image to classify
     * @return the labels (and the weights if applicable)
     */
    @Override
    public LabeledClassification apply(String pathImage) {
        if (this.activeRCNN) return this.applyRCNN(pathImage);
        return this.applyCNN(pathImage);
    }
    
    public void closeConnection(){
        this.tcpClient.closeConnection();
    }
    
    public LabeledClassification applyCNN(String pathImage) {
        ArrayList<String> labels = new ArrayList();
        ArrayList<Double> weights = new ArrayList();
        try {

            JSONObject object = new JSONObject();
            object.put("option", "P");
            object.put("path_img_selected", pathImage);

            String info = this.tcpClient.sendPetition(object.toJSONString() + "\n");
            JSONParser parser = new JSONParser();
            JSONObject response = (JSONObject) parser.parse(info);

            JSONArray classesPredicted = (JSONArray) response.get("classes");

            for (int i = 0; i < classesPredicted.size(); i++) {
                JSONObject objLabel = (JSONObject) classesPredicted.get(i);

                objLabel.keySet().forEach(classLabel -> {
                    String strWeight = (String) objLabel.get(classLabel);
                    Double weight = Double.valueOf(strWeight);

                    labels.add((String) classLabel);
                    weights.add(weight);
                });

            }

        } catch (ParseException ex) {
            Logger.getLogger(PythonClassifier.class.getName()).log(Level.SEVERE, null, ex);
        }

        return new LabeledClassification() {
            @Override
            public List<String> getLabels() {
                return labels;
            }

            public boolean isWeighted() {
                return true;
            }

            public List<Double> getWeights() {
                return weights;
            }
        };
    }
    
    public LabeledRegionClassification applyRCNN(String pathImage) {
        ArrayList<String> labels = new ArrayList();
        ArrayList<Double> weights = new ArrayList();
        ArrayList<Rectangle2D> boundingBoxs = new ArrayList();
        try {
            JSONObject object = new JSONObject();
            object.put("option", "D");
            object.put("path_img_selected", pathImage);
            
            String info = this.tcpClient.sendPetition(object.toJSONString() + "\n");
            JSONParser parser = new JSONParser();
            JSONArray response = (JSONArray) parser.parse(info);

            for (Object obj : response) {
                JSONObject objJson = (JSONObject) obj;
                
                objJson.keySet().forEach(classId -> {
                    String classIdString = (String) classId;
                    JSONObject infoClass = (JSONObject) objJson.get(classIdString);
                    
                    Double weight = (Double) infoClass.get("score");
                    JSONArray bbox = (JSONArray) infoClass.get("bbox");

                    Double x1 = Double.valueOf((Long) bbox.get(1));
                    Double y1 = Double.valueOf((Long) bbox.get(0));
                    Double x2 = Double.valueOf((Long) bbox.get(3));
                    Double y2 = Double.valueOf((Long) bbox.get(2));

                    Double width = Math.abs(x2-x1);
                    Double height = Math.abs(y2-y1);

                    Rectangle2D boundingBox = new Rectangle2D.Double(x1, y1, width, height);
                    
                    labels.add(classIdString);
                    weights.add(weight);
                    boundingBoxs.add(boundingBox);
                });
            }
            
            

        } catch (ParseException ex) {
            Logger.getLogger(PythonClassifier.class.getName()).log(Level.SEVERE, null, ex);
        }

        return new LabeledRegionClassification() {
            @Override
            public List<String> getLabels() {
                return labels;
            }

            public boolean isWeighted() {
                return true;
            }

            public List<Double> getWeights() {
                return weights;
            }

            public List<Rectangle2D> getBoundingBoxs() {
                return boundingBoxs;
            }
        };
    }

    public JSONObject getBoundingBoxs(ArrayList<String> classesSelected, String pathImageSelected, int threshold) {
        JSONObject response = new JSONObject();
        try {
            JSONObject object = new JSONObject();
            JSONArray classes = new JSONArray();
            for (String label : classesSelected) {
                classes.add(label);
            }
            object.put("option", "B");
            object.put("path_img_selected", pathImageSelected);
            object.put("classes_selected", classes);
            object.put("threshold", threshold);

            String info = this.tcpClient.sendPetition(object.toJSONString() + "\n");
            JSONParser parser = new JSONParser();
            response = (JSONObject) parser.parse(info);
        } catch (ParseException ex) {
            Logger.getLogger(PythonClassifier.class.getName()).log(Level.SEVERE, null, ex);
        }
        return response;
    }
    
    public JSONObject getHeatmap(ArrayList<String> classesSelected, String pathImageSelected) {
        JSONObject response = new JSONObject();
        try {
            JSONObject object = new JSONObject();
            JSONArray classes = new JSONArray();
            for (String label : classesSelected) {
                classes.add(label);
            }
            object.put("option", "HM");
            object.put("path_img_selected", pathImageSelected);
            object.put("classes_selected", classes);

            String info = this.tcpClient.sendPetition(object.toJSONString() + "\n");
            JSONParser parser = new JSONParser();
            response = (JSONObject) parser.parse(info);
        } catch (ParseException ex) {
            Logger.getLogger(PythonClassifier.class.getName()).log(Level.SEVERE, null, ex);
        }
        return response;
    }

    public JSONObject windowSliding(String pathImageSelected, int width, int height, int step) {
        JSONObject response = new JSONObject();
        try {
            JSONObject object = new JSONObject();
            object.put("option", "WS");
            object.put("path_img_selected", pathImageSelected);
            object.put("widthWS", width);
            object.put("heightWS", height);
            object.put("stepSizeWS", step);

            String info = this.tcpClient.sendPetition(object.toJSONString() + "\n");
            JSONParser parser = new JSONParser();
            response = (JSONObject) parser.parse(info);
        } catch (ParseException ex) {
            Logger.getLogger(PythonClassifier.class.getName()).log(Level.SEVERE, null, ex);
        }

        return response;
    }


    public void setActiveRCNN(boolean activeRCNN) {
        this.activeRCNN = activeRCNN;
    }

    public boolean isActiveRCNN() {
        return this.activeRCNN;
    }

}
