package jmr.learning;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.stream.DoubleStream;
import org.deeplearning4j.nn.graph.ComputationGraph;
import org.deeplearning4j.nn.modelimport.keras.KerasModelImport;
import org.deeplearning4j.nn.modelimport.keras.exceptions.InvalidKerasConfigurationException;
import org.deeplearning4j.nn.modelimport.keras.exceptions.UnsupportedKerasConfigurationException;
import org.deeplearning4j.nn.multilayer.MultiLayerNetwork;
import org.nd4j.linalg.api.ndarray.INDArray;

/**
 * Abstract diferent types of architectures from Keras.
 * Deep Learning for Java (DL4J) is used to import Keras' model to Java.
 * 
 * @author Míriam Mengíbar Rodríguez (mirismr@correo.ugr.es)
 */
public class KerasArchitecture {
    /**
     * Represents a complex architecture (Different branches).
     */
    private ComputationGraph complexArchitecture;
    /**
     * Represents a sequential architecture (Layer' stack).
     */
    private MultiLayerNetwork sequentialArchitecture;
    
    /**
     * Constructs a Keras Architecture, initializes it from
     * the non sequential architecture given by parameter
     * 
     * @param model the non sequential architecture fromm
     */
    public KerasArchitecture(ComputationGraph model) {
        this.complexArchitecture = model;
        this.sequentialArchitecture = null;
    }
    
    /**
     * Constructs a Keras Architecture, initializes it from
     * the sequential architecture given by parameter
     * 
     * @param sequentialModel the sequential architecture
     */
    public KerasArchitecture(MultiLayerNetwork sequentialModel) {
        this.complexArchitecture = null;
        this.sequentialArchitecture = sequentialModel;
    }
    
    /**
     * Load from a HDF5 file (given by parameter) a architecture exported from Keras.
     * @param modelFile the HDF5 file
     * @return KerasArchitecture imported from HDF5 file
     * @throws Exception is thrown if the file contains a wrong exported architecture 
     * (not valid or not supported by DL4J)
     */
    public static KerasArchitecture loadArchitecture(File modelFile) throws Exception {
        return loadArchitecture(modelFile.getAbsolutePath());
    }
    
    /**
     * Load from a HDF5 file (given by parameter) a architecture exported from Keras.
     * @param modelPath the HDF5 file's path
     * @return KerasArchitecture imported from HDF5 file
     * @throws Exception is thrown if the file contains a wrong exported architecture
     * (not valid or not supported by DL4J)
     */
    public static KerasArchitecture loadArchitecture(String modelPath) throws Exception {
        KerasArchitecture kerasArchitecture = null;
        
        try {
            ComputationGraph complexArchitecture = KerasModelImport.importKerasModelAndWeights(modelPath, false);
            kerasArchitecture = new KerasArchitecture(complexArchitecture);
        } catch (IOException ex) {
            Logger.getLogger(KerasClassifier.class.getName()).log(Level.SEVERE, null, ex); 
        } catch (InvalidKerasConfigurationException ex) {           
            try {
                MultiLayerNetwork sequentialArchitecture = KerasModelImport.importKerasSequentialModelAndWeights(modelPath);
                kerasArchitecture = new KerasArchitecture(sequentialArchitecture);
            } catch (IOException ex1) {
                Logger.getLogger(KerasClassifier.class.getName()).log(Level.SEVERE, null, ex1);
            } catch (InvalidKerasConfigurationException ex1) {
                //.h5 no valido para secuencial tampoco
                Logger.getLogger(KerasClassifier.class.getName()).log(Level.SEVERE, null, ex);
            } catch (UnsupportedKerasConfigurationException ex1) {
                //.h5 no lo soporta la libreria
                Logger.getLogger(KerasClassifier.class.getName()).log(Level.SEVERE, null, ex);
            }
            
        } catch (UnsupportedKerasConfigurationException ex) {
            //Configuracion no soportada por la libreria
            Logger.getLogger(KerasClassifier.class.getName()).log(Level.SEVERE, null, ex);
        }

        return kerasArchitecture;
    }
    
    /**
    * Returns true if this architecture is sequential.
    * @return true if this architecture is sequential, false in
    * other case.
    */
    public boolean isSequential() {
        return this.sequentialArchitecture != null;
    }
    
    /**
     * Returns the sequential architecture.
     * @return the sequential architecture, null if the architecture is complex.
     */
    public MultiLayerNetwork getSequentialArchitecture() {
        return this.sequentialArchitecture;
    }
    
    /**
     * Returns the complex architecture.
     * @return the complex architecture, null if the architecture is sequential.
     */
    public ComputationGraph getComplexArchitecture() {
        return this.complexArchitecture;
    }
    
    /**
     * Given a n-dimensional array returns imported architecture's output
     * @param input the n-dimensional array
     * @return the imported architecture's output
     */
    public List<Double> predict(INDArray input) {
        double [] predicts = null;
        if (this.sequentialArchitecture != null) predicts = this.sequentialArchitecture.output(input).toDoubleVector();
        else predicts = this.complexArchitecture.outputSingle(input).toDoubleVector();
        
        return DoubleStream.of(predicts).boxed().collect(Collectors.toCollection(ArrayList::new));
    }
}
