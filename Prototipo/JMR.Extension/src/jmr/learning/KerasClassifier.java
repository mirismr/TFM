/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jmr.learning;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.IntStream;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import jmr.descriptor.label.Classifier;
import jmr.descriptor.label.LabeledClassification;
import org.datavec.image.loader.NativeImageLoader;
import org.deeplearning4j.exception.DL4JInvalidInputException;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.nd4j.linalg.api.ndarray.INDArray;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

/**
 * A classifier representing a Convolutional Neural Network from Keras.
 *
 * @author Míriam Mengíbar Rodríguez (mirismr@correo.ugr.es)
 */
public class KerasClassifier implements Classifier<BufferedImage, LabeledClassification> {

    final boolean DEFAULT_WEIGHTED = true;
    /**
     * CNN's architecture (number and type of layers)
     */
    private KerasArchitecture architecture;
    /**
     * Input layer' size (height, width, depth)
     */
    private Dimension3D<Integer, Integer, Integer> inputDimension;
    /**
     * List of classes that the classifier can produce at the output
     */
    private List<String> labels;
    /**
     * The threshold that weight' class have to pass
     */
    private double threshold;
    /**
     * Indicates if the classifier is weighted
     */
    private boolean weighted;

    /**
     * Constructs a classifier, initializes it from the architecture and the
     * input dimension given by parameter
     *
     * @param architecture the architecture
     * @param inputDimension the input dimension
     * @throws Exception is thrown if the inputDimension doesn't match with the
     * input dimension architecture
     */
    public KerasClassifier(KerasArchitecture architecture, Dimension3D<Integer, Integer, Integer> inputDimension) throws Exception {
        this.architecture = architecture;
        this.threshold = 0.5;
        this.weighted = false;
        this.setInputDimension(inputDimension);
        this.initLabels();
    }

    /**
     * Constructs a classifier, initializes it from the architecture given by
     * parameter. Only for internal library' use
     *
     * @param architecture the architectures
     */
    private KerasClassifier(KerasArchitecture architecture) {
        this.architecture = architecture;
        this.threshold = 0.5;
        this.weighted = false;
        this.inputDimension = null;
        this.initLabels();
    }

    /**
     * Load from a HDF5 file or a XML file (given by parameter) a classifier
     * exported from Keras. If XML file is given, it should follow the JMR file
     * rules.
     *
     * @param modelFile the HDF5 o XML file
     * @return the classifier imported
     * @throws Exception is thrown if the input dimension (read from XML file)
     * doesn't match with the input dimension architecture, the number of
     * classes (read from XML file) doesn't match with the ouput dimension
     * architecture or a malformed XML file.
     */
    private static KerasClassifier openFileModel(File modelFile) throws Exception {
        String name = modelFile.getName();
        String weightsPath = modelFile.getAbsolutePath();
        String absolutePathXML = weightsPath;
        Dimension3D<Integer, Integer, Integer> dimension = null;
        List<String> labels = new ArrayList<>();
        int index = name.lastIndexOf('.');
        if (index > 0) {
            String extension = name.substring(index + 1);
            if (extension.equals("xml")) {
                try {
                    DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
                    DocumentBuilder documentBuilder = null;
                    documentBuilder = documentBuilderFactory.newDocumentBuilder();

                    Document document = documentBuilder.parse(modelFile);
                    NodeList items = null;
                    String height, width, depth = "";
                    try {
                        height = document.getElementsByTagName("dim_height").item(0).getTextContent();
                        width = document.getElementsByTagName("dim_width").item(0).getTextContent();
                        depth = document.getElementsByTagName("dim_depth").item(0).getTextContent();

                        //we assume weightsPath is an absolute path
                        //if ./ or nothing, we assume that the weights file is in the xml file's path
                        weightsPath = document.getElementsByTagName("weights_file").item(0).getTextContent();

                        int indexBackSlash = Math.max(weightsPath.indexOf("./"), weightsPath.indexOf(".\\"));
                        int indexAbsolutePath = Math.max(absolutePathXML.lastIndexOf("/"), absolutePathXML.lastIndexOf("\\"));
                        //if found ./, we remove it and append the weightsPath to absolute path
                        if (indexBackSlash >= 0) {
                            weightsPath = absolutePathXML.substring(0, indexAbsolutePath + 1) + weightsPath.substring(indexBackSlash + 2);
                        } //if not found /, we directly append the weightsPath to absolute path 
                        else {
                            indexBackSlash = Math.max(weightsPath.indexOf("/"), weightsPath.indexOf("\\"));
                            if (indexBackSlash < 0) {
                                weightsPath = absolutePathXML.substring(0, indexAbsolutePath + 1) + weightsPath;
                            }
                        }

                        //optionals labels in XML 
                        items = document.getElementsByTagName("item");
                        if (items.getLength() != 0) {
                            for (int i = 0; i < items.getLength(); i++) {
                                labels.add(items.item(i).getTextContent());
                            }
                        }
                    } catch (NullPointerException ex) {
                        throw new Exception("Malformed XML file. Please, follow the JMR rules to construct the XML file.");
                    }

                    dimension = new Dimension3D<>(Integer.valueOf(height), Integer.valueOf(width), Integer.valueOf(depth));

                } catch (SAXException ex) {
                    Logger.getLogger(KerasClassifier.class.getName()).log(Level.SEVERE, null, ex);
                } catch (IOException ex) {
                    Logger.getLogger(KerasClassifier.class.getName()).log(Level.SEVERE, null, ex);
                } catch (ParserConfigurationException ex) {
                    Logger.getLogger(KerasClassifier.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }

        KerasArchitecture architecture = KerasArchitecture.loadArchitecture(weightsPath);

        KerasClassifier classifier = new KerasClassifier(architecture);

        // if the dimension is not null, we have a XML file
        if (dimension != null) {
            classifier = new KerasClassifier(architecture, dimension);

            // if the labels is not empty, the XML file have labels
            if (!labels.isEmpty()) {
                classifier.setLabels(labels);
            }
        }

        return classifier;
    }

    /**
     * Load from a XML file (given by parameter) a classifier exported from
     * Keras. The XML file should follow the JMR file rules.
     *
     * @param modelFile the XML file
     * @return the classifier imported
     * @throws Exception is thrown if the input dimension (read from XML file)
     * doesn't match with the input dimension architecture or the number of
     * classes (read from XML file) doesn't match with the ouput dimension
     * architecture
     */
    public static KerasClassifier loadModel(File modelFile) throws Exception {
        String name = modelFile.getAbsolutePath();
        KerasClassifier kerasClassifier = null;
        int index = name.lastIndexOf('.');
        if (index > 0) {
            String extension = name.substring(index + 1);
            if (extension.equals("xml")) {
                kerasClassifier = KerasClassifier.openFileModel(modelFile);
            } else {
                throw new Exception("The file should be a XML following the JMR rules");
            }
        }

        return kerasClassifier;
    }

    /**
     * Load from a HDF5 file or a XML file (given by parameter) a classifier
     * exported from Keras. If XML file is given, it should follow the JMR file
     * rules. It set the input dimension of classifier by the triplet given by
     * parameter.
     *
     * @param modelFile the HDF5 o XML file
     * @param inputDimension model's input dimension
     * @return the classifier imported
     * @throws Exception is thrown if the input dimension (read from XML file)
     * doesn't match with the input dimension architecture or the number of
     * classes (read from XML file) doesn't match with the ouput dimension
     * architecture
     */
    public static KerasClassifier loadModel(File modelFile, Dimension3D<Integer, Integer, Integer> inputDimension) throws Exception {
        KerasClassifier kerasClassifier = KerasClassifier.openFileModel(modelFile);

        kerasClassifier.setInputDimension(inputDimension);

        return kerasClassifier;
    }

    /**
     * Given a image it returns the labels (and the weights if the classifier is
     * weighted) from the classes found in that image.
     *
     * @param image the image to classify
     * @return the labels (and the weights if applicable)
     */
    @Override
    public LabeledClassification apply(BufferedImage image) {
        ArrayList<String> labels = new ArrayList();
        ArrayList<Double> weights = new ArrayList();

        INDArray imageArray = null;
        try {

            imageArray = new NativeImageLoader(this.inputDimension.getHeight(), this.inputDimension.getWidth(), this.inputDimension.getDepth()).asMatrix(image);

            List<Double> predictions = this.architecture.predict(imageArray);
            for (int i = 0; i < predictions.size(); i++) {
                if (predictions.get(i) >= this.threshold) {
                    labels.add(this.labels.get(i));

                    if (weighted) {
                        weights.add(predictions.get(i));
                    }
                }
            }

        } catch (IOException ex) {
            java.util.logging.Logger.getLogger(KerasClassifier.class.getName()).log(Level.SEVERE, null, ex);
        } catch (Exception ex) {
            java.util.logging.Logger.getLogger(KerasClassifier.class.getName()).log(Level.SEVERE, null, ex);
        }

        return new LabeledClassification() {
            @Override
            public List<String> getLabels() {
                return labels;
            }

            public boolean isWeighted() {
                return weighted;
            }

            public List<Double> getWeights() {
                if (weighted) {
                    return weights;
                }

                return null;
            }
        };
    }

    /**
     * Set value of the classifier's threshold
     *
     * @param threshold the classifier's threshold
     */
    public void setThreshold(double threshold) {
        this.threshold = threshold;
    }

    /**
     * Returns the value of classifier's threshold
     *
     * @return classifier's threshold
     */
    public double getThreshold() {
        return this.threshold;
    }

    /**
     * Set if the classifier is weighted
     *
     * @param weighted true if the classifier is weighted, false in other case
     */
    public void setWeighted(boolean weighted) {
        this.weighted = weighted;
    }

    /**
     * Returns if this classifier is weighted.
     *
     * @return true if this classifier is weighted, false in other case.
     */
    public boolean isWeighted() {
        return this.weighted;
    }

    /**
     * Set the classifier's input dimension
     *
     * @param inputDimension the classifier's input dimension
     * @throws Exception is thrown if the input dimension doesn't match with the
     * input dimension architecture
     */
    public void setInputDimension(Dimension3D<Integer, Integer, Integer> inputDimension) throws Exception {
        try {
            INDArray imageArray = new NativeImageLoader(inputDimension.getHeight(), inputDimension.getWidth(), inputDimension.getDepth()).asMatrix(new BufferedImage(1, 1, BufferedImage.TYPE_INT_ARGB));
            this.architecture.predict(imageArray);

        } catch (IOException ex) {
            Logger.getLogger(KerasClassifier.class.getName()).log(Level.SEVERE, null, ex);
        } catch (DL4JInvalidInputException exInput) {
            throw new Exception("Invalid dimension of input");
        }

        this.inputDimension = inputDimension;
    }

    /**
     * TODO Load labels from file given by parameter. File have to be .json (key
     * : label)
     *
     * @param labelsFile the classifier classes
     * @throws Exception is thrown if the number of classes doesn't match with
     * the ouput dimension architecture
     */
    public void loadLabels(File labelsFile) throws Exception {
        List<String> labels = new ArrayList<>();
        String name = labelsFile.getName();
        int index = name.lastIndexOf('.');
        if (index > 0) {
            String extension = name.substring(index + 1);
            if (extension.equals("json")) {

                JSONParser jsonParser = new JSONParser();
                FileReader reader = new FileReader(labelsFile);
                    //Read JSON file
                    JSONObject obj = (JSONObject) jsonParser.parse(reader);

                for (Object key : obj.keySet()) {
                    String keyStr = (String) key;
                    JSONArray array = (JSONArray) obj.get(keyStr);

                    String label = (String) array.get(1);

                    labels.add(label);
                }

            } else {
                throw new Exception("Incorrect file extension. File have to be a json file.");
            }
        }
        
        this.setLabels(labels);
    }

    /**
     * Set the classifier classes by the list given by parameter.
     *
     * @param labels the classifier classes
     * @throws Exception is thrown if the number of classes doesn't match with
     * the ouput dimension architecture
     */
    public void setLabels(List<String> labels) throws Exception {
        if (this.labels.size() != labels.size()) {
            throw new Exception("Incorrect number of classes. The model has a output layer' size of " + this.labels.size());
        }

        this.labels = labels;
    }

    /**
     * Initialize the classifier labels. The values are strings from 0 to
     * label's size - 1.
     */
    private void initLabels() {
        this.labels = null;

        if (this.architecture.isSequential()) {
            this.labels = Arrays.asList(IntStream.rangeClosed(0, this.architecture.getSequentialArchitecture().layerSize(this.architecture.getSequentialArchitecture().getLayers().length - 1) - 1).mapToObj(String::valueOf).toArray(String[]::new));
        } else {
            this.labels = Arrays.asList(IntStream.rangeClosed(0, this.architecture.getComplexArchitecture().layerSize(this.architecture.getComplexArchitecture().getLayers().length - 1) - 1).mapToObj(String::valueOf).toArray(String[]::new));
        }
    }

    /**
     * Returns the list of classifier labels
     *
     * @return the list of classifier labels
     */
    public List<String> getLabels() {
        return this.labels;
    }
}
