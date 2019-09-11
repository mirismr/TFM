package jmr.descriptor;

import java.lang.reflect.Constructor;
import java.security.InvalidParameterException;

/**
 * A factory class for generating instances of media descriptors.
 * 
 * @author Jesús Chamorro Martínez (jesus@decsai.ugr.es)
 */
public class MediaDescriptorFactory {
    
    
   /**
    * Constructs a descriptor of a given class and initializes it on the basis 
    * of a given media.
    * 
    * The descriptor class have to provide a constructor with a single parameter
    * of the media type.
    * 
    * @param <D> the type of the output descriptor
    * @param <M> the type of the media
    * @param descriptorClass the class of the descriptor to be constructed.
    * @param media the media used to initialize the descriptor
    * @return a descriptor object
    * @throws InvalidParameterException if the descriptor class have not provide 
    * a constructor with a single parameter of the media type.
    */
   public static <D extends MediaDescriptor, M> D getInstance(Class<D> descriptorClass, M media){
      D descriptor = null;
      try{
        Constructor constructor = descriptorClass.getConstructor(media.getClass());
        descriptor = (D)constructor.newInstance(media);
      } catch (Exception ex) {
          String mediaClassName = media.getClass().getSimpleName();
          String descriptorClassName = descriptorClass.getSimpleName();
          throw new InvalidParameterException("A constructor with a single parameter of type "+mediaClassName+" must be provided for the class "+descriptorClassName+".");
      }
      return descriptor;
  }
    
    
}
