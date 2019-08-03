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
import matplotlib.pyplot as plt
from time import time
import keras.backend as K
from keras.preprocessing.image import ImageDataGenerator
from keras.preprocessing import image
from keras.models import Model
from keras.layers import Input, Dense, Dropout
from keras.applications.vgg16 import VGG16, preprocess_input, decode_predictions
from keras.applications.inception_v3 import InceptionV3, preprocess_input, decode_predictions

# -------------------
# Declaring constants
# -------------------

TRAIN_DIR = "train"
VALID_DIR = "validation"
IMG_SIZE = (224, 224, 3)
BATCH_SIZE = 32

# ------------------------------------------
# Creating InceptionV3 model and training it
# ------------------------------------------

inp = Input(IMG_SIZE)

'''
model = VGG16(include_top=True, weights='imagenet')


# ---------------------------------------------------------------------------------------
# Getting outputs for intermediate convolution layers by running prediction on test image
# ---------------------------------------------------------------------------------------
img_path = 'perros.jpg'
img = image.load_img(img_path, target_size=IMG_SIZE)
img_tensor = image.img_to_array(img)
img_tensor = np.expand_dims(img_tensor, axis=0)
img_tensor = preprocess_input(img_tensor)
last_conv_layer_index = 18

layer_outputs = [layer.output for layer in model.layers[1:last_conv_layer_index]]
# Extracts the outputs of the top 12 layers
activation_model = Model(inputs=model.input, outputs=layer_outputs) # Creates a model that will return these outputs, given the model input
activations = activation_model.predict(img_tensor)
'''
'''
#show an activation map (feature map), in this example the activation map 14
first_layer_activation = activations[0]
plt.matshow(first_layer_activation[0, :, :, 14], cmap='viridis')
plt.colorbar()
plt.show()
'''

'''
#show mean activation of all filter in last conv layer -> hacer todos los filtros para poder buscar el maximo y ver cual es el que activa mas
print(model.summary())
print(layer_outputs[16])
means = []
for i in range(activations[last_conv_layer_index-2][0, :,:,:].shape[2]):
    means.append(activations[last_conv_layer_index-2][0, :, :, i].mean())

plt.bar(range(512), means)
plt.show()

print(means.index(max(means)))
preds = model.predict(img_tensor)
print("Predicted: ", decode_predictions(preds, top=3)[0])
'''


'''
layer_names = []
for layer in model.layers[1:12]:
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
            channel_image /= channel_image.std()
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
    plt.imshow(display_grid, aspect='auto', cmap='viridis')

plt.show()

print(model.summary())
'''


#ok
# -----------------------------------------------------------------------------------
# Here, I am initializing an InceptionV3 model and making prediction on a test image.
# Following which, I am creating a activaton heatmap of the last layer of this model,
# which is a mixed layer. This heatmap is then superimposed on the original image.
# -----------------------------------------------------------------------------------

model = InceptionV3(weights='imagenet')
img_path = 'images/aleman.jpg'

img = image.load_img(img_path, target_size=(IMG_SIZE[0], IMG_SIZE[1]))
x = image.img_to_array(img)
x = np.expand_dims(x, axis=0)
x = preprocess_input(x)

preds = model.predict(x)
print("Predicted: ", decode_predictions(preds, top=3)[0])

# 985 is the class index for class 'Daisy' in Imagenet dataset on which my model is pre-trained
flower_output = model.output[:, 235]
last_conv_layer = model.get_layer('mixed10')

grads = K.gradients(flower_output, last_conv_layer.output)[0]
pooled_grads = K.mean(grads, axis=(0, 1, 2))
iterate = K.function([model.input], [pooled_grads, last_conv_layer.output[0]])
pooled_grads_value, conv_layer_output_value = iterate([x])

# 2048 is the number of filters/channels in 'mixed10' layer
for i in range(2048):
    conv_layer_output_value[:, :, i] *= pooled_grads_value[i]

heatmap = np.mean(conv_layer_output_value, axis=-1)
heatmap = np.maximum(heatmap, 0)
heatmap /= np.max(heatmap)
#plt.savefig(heatmap)

# Using cv2 to superimpose the heatmap on original image to clearly illustrate activated portion of image
img = cv2.imread(img_path)
heatmap = cv2.resize(heatmap, (img.shape[1], img.shape[0]))
heatmap = np.uint8(255 * heatmap)
heatmap = cv2.applyColorMap(heatmap, cv2.COLORMAP_JET)
superimposed_img = heatmap * 0.4 + img
cv2.imwrite('image_name.jpg', superimposed_img)

