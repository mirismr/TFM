'''
This python file is used for doing following things:

1. Creating training and validation generator using Keras' inbuilt ImageDataGenerator object with image augmentation.
2. Training a pre-trained InceptionV3 model.
3. Displaying and saving layer activations for first 100 layers on a test image.
4. Displaying and saving layer filters for few convolution layers
5. Displaying and saving heatmaps for test images.
'''

import numpy as np
import cv2
import keras.backend as K
from keras.preprocessing import image
from keras.applications.vgg16 import VGG16, preprocess_input, decode_predictions
from keras.applications.inception_v3 import InceptionV3, preprocess_input, decode_predictions

IMG_SIZE = (224, 224, 3)

def get_CAM(image_path, model, class_name, layer_name):
    img = image.load_img(image_path, target_size=(IMG_SIZE[0], IMG_SIZE[1]))
    x = image.img_to_array(img)
    x = np.expand_dims(x, axis=0)
    x = preprocess_input(x)

    preds = model.predict(x)
    print("Predicted: ", decode_predictions(preds, top=3)[0])

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

    img = cv2.imread(image_path)
    cam_img = cv2.resize(heatmap, (img.shape[1], img.shape[0]))
    cam_img = np.uint8(255 * cam_img)

    # Using cv2 to superimpose the heatmap on original image to clearly illustrate activated portion of image
    heatmap = cam_img.copy()
    heatmap = cv2.applyColorMap(heatmap, cv2.COLORMAP_JET)
    superimposed_img = heatmap * 0.4 + img
    cv2.imwrite('heatmap.jpg', superimposed_img)


    ret, cam_img = cv2.threshold(cam_img, 130, 255, cv2.THRESH_BINARY)
    contours, hierarchy = cv2.findContours(cam_img, cv2.RETR_LIST, cv2.CHAIN_APPROX_SIMPLE)

    for i, contour in enumerate(contours):
        boundRect = cv2.boundingRect(contour)
        cv2.drawContours(img, contours, i, (255, 255, 0, 0), 3, 4, hierarchy)
        cv2.rectangle(img, (int(boundRect[0]), int(boundRect[1])), \
                      (int(boundRect[0] + boundRect[2]), int(boundRect[1] + boundRect[3])), (255, 0, 0, 0), 2)
    cv2.imwrite('bounding_box.jpg', img)

    return boundRect



model = InceptionV3(weights='imagenet')
img_path = 'images/collage1.png'
class_name = 208
bounding_box = get_CAM(img_path, model, class_name, 'mixed10')
print(bounding_box)

