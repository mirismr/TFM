import os
os.environ['TF_CPP_MIN_LOG_LEVEL'] = '3' 
import tensorflow as tf
tf.compat.v1.logging.set_verbosity(tf.compat.v1.logging.ERROR)

import numpy as np
import cv2
import json
import keras.backend as K
from keras.preprocessing import image
from keras.applications.vgg16 import VGG16, preprocess_input, decode_predictions
from keras.applications.inception_v3 import InceptionV3, preprocess_input, decode_predictions


IMG_SIZE = (224, 224, 3)
data_json_classes = ''
with open('imagenet_class_index.json', 'r') as json_file:
    data_json_classes = json.load(json_file)

def get_CAM(image_path, model, classes, layer_name, contour=False, umbral=130):
    img = image.load_img(image_path, target_size=(IMG_SIZE[0], IMG_SIZE[1]))

    x = image.img_to_array(img)
    x = np.expand_dims(x, axis=0)
    x = preprocess_input(x)

    

    #preds = model.predict(x)
    #print("Predicted: ", decode_predictions(preds, top=3)[0])


    img = cv2.imread(image_path)
    # initialize = img for various heatmap
    superimposed_img = img
    outlined_image = img
    boundings = []
    for class_name in classes:
        class_output = model.output[:, class_name]
        last_conv_layer = model.get_layer(layer_name)

        grads = K.gradients(class_output, last_conv_layer.output)[0]
        pooled_grads = K.mean(grads, axis=(0, 1, 2))
        iterate = K.function([model.input], [pooled_grads, last_conv_layer.output[0]])
        pooled_grads_value, conv_layer_output_value = iterate([x])

        # number of filter / channel last_conv_layer
        for i in range(last_conv_layer.output_shape[3]):
            conv_layer_output_value[:, :, i] *= pooled_grads_value[i]

        heatmap = np.mean(conv_layer_output_value, axis=-1)
        heatmap = np.maximum(heatmap, 0)
        heatmap /= np.max(heatmap)

        
        cam_img = cv2.resize(heatmap, (img.shape[1], img.shape[0]))
        cam_img = np.uint8(255 * cam_img)

        # Using cv2 to superimpose the heatmap on original image to clearly illustrate activated portion of image
        heatmap = cam_img.copy()
        heatmap = cv2.applyColorMap(heatmap, cv2.COLORMAP_JET)
        superimposed_img = heatmap * 0.4 + superimposed_img

        path = image_path.split('.')
        path_heatmap = path[0]+'_heatmap.jpg'
        cv2.imwrite(path_heatmap, superimposed_img)

        if (contour):
            class_str = data_json_classes[str(class_name)]
            class_str = class_str[1]
            ret, cam_img = cv2.threshold(cam_img, umbral, 255, cv2.THRESH_BINARY)
            contours, hierarchy = cv2.findContours(cam_img, cv2.RETR_LIST, cv2.CHAIN_APPROX_SIMPLE)
            
            for i, c in enumerate(contours):
                boundRect = cv2.boundingRect(c)
                cv2.drawContours(outlined_image, contours, i, (255, 255, 0, 0), 3, 4, hierarchy)
                outlined_image = cv2.rectangle(outlined_image, (int(boundRect[0]), int(boundRect[1])), \
                              (int(boundRect[0] + boundRect[2]), int(boundRect[1] + boundRect[3])), (255, 0, 0, 0), 2)
                cv2.putText(outlined_image, str(class_str), (boundRect[0], boundRect[1] - 20), cv2.FONT_HERSHEY_SIMPLEX, 0.8, (255, 0, 0), 1)

                boundings.append({class_name : boundRect})

            

    if (contour):    
        path_bounding = path[0]+'_bounding_box.jpg'
        cv2.imwrite(path_bounding, outlined_image)
        return boundings, path_bounding

    return path_heatmap



def get_activation_maps(image_path):
    from keras.applications.inception_v3 import InceptionV3, preprocess_input, decode_predictions
    import matplotlib
    from matplotlib import pyplot as plt
    from keras import models
    img = cv2.imread(image_path)
    img = image.load_img(img_path, target_size=(224, 224))
    img_tensor = image.img_to_array(img)
    img_tensor = np.expand_dims(img_tensor, axis=0)
    img_tensor = preprocess_input(img_tensor)
    
    classifier =  InceptionV3(weights='imagenet')
    layer_outputs = [layer.output for layer in classifier.layers[1:30]] 
    print(len(classifier.layers))
    # Extracts the outputs of the top 12 layers
    activation_model = models.Model(inputs=classifier.input, outputs=layer_outputs) # Creates a model that will return these outputs, given the model input
    activations = activation_model.predict(img_tensor) # Returns a list of five Numpy arrays: one array per layer activation

    layer_names = []
    for layer in classifier.layers[1:30]:
        layer_names.append(layer.name) # Names of the layers, so you can have them as part of your plot
        
    images_per_row = 16
    for layer_name, layer_activation in zip(layer_names, activations): # Displays the feature maps
        n_features = layer_activation.shape[-1] # Number of features in the feature map
        size = layer_activation.shape[1] #The feature map has shape (1, size, size, n_features).
        n_cols = n_features // images_per_row # Tiles the activation channels in this matrix
        display_grid = np.zeros((size * n_cols, images_per_row * size))
        for col in range(n_cols): # Tiles each filter into a big horizontal grid
            for row in range(images_per_row):
                channel_image = layer_activation[0,
                                                :, :,
                                                col * images_per_row + row]
                channel_image -= channel_image.mean() # Post-processes the feature to make it visually palatable
                tiny = 1e-15
                channel_image /= (channel_image.std()+tiny)
                channel_image *= 64
                channel_image += 128
                channel_image = np.clip(channel_image, 0, 255).astype('uint8')
                display_grid[col * size : (col + 1) * size, # Displays the grid
                            row * size : (row + 1) * size] = channel_image
        scale = 1. / size
        plt.figure(figsize=(scale * display_grid.shape[1],
                            scale * display_grid.shape[0]))
        plt.title(layer_name)
        plt.grid(False)
        plt.imshow(display_grid, aspect='auto', cmap='plasma')
        plt.colorbar()

        plt.show()
    

'''
model = InceptionV3(weights='imagenet')
img_path = "images/perro_gato_1.jpg"
class_name = [260, 282]
print(get_CAM(img_path, model, class_name, 'mixed10', True))

img_path = "images/labrador.jpg"
get_activation_maps(img_path)
'''