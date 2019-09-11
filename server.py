import socket
import json
from visualization import get_CAM
from window_sliding import window_sliding
from keras.preprocessing import image
import numpy as np
import os
import sys
from keras.applications.inception_v3 import InceptionV3, preprocess_input, decode_predictions


IMG_SIZE = (224, 224, 3)
model = InceptionV3(weights='imagenet')




# Root directory of the project
ROOT_DIR = os.path.abspath("../Mask_RCNN")

# Import Mask RCNN
sys.path.append(ROOT_DIR)  # To find local version of the library
from mrcnn import utils
import mrcnn.model as modellib
from mrcnn import visualize
# Import COCO config
sys.path.append(os.path.join(ROOT_DIR, "samples/coco/"))  # To find local version
import coco

# Local path to trained weights file
COCO_MODEL_PATH = os.path.join(ROOT_DIR, "mask_rcnn_coco.h5")

# Directory to save logs and trained model
MODEL_DIR = os.path.join(ROOT_DIR, "logs")

class InferenceConfig(coco.CocoConfig):
	# Set batch size to 1 since we'll be running inference on
	# one image at a time. Batch size = GPU_COUNT * IMAGES_PER_GPU
	GPU_COUNT = 1
	IMAGES_PER_GPU = 1

from mask_rcnn import detect_objects
configRCNN = InferenceConfig()
# Create model object in inference mode.
modelRCNN = modellib.MaskRCNN(mode="inference", model_dir=MODEL_DIR, config=configRCNN)
# Load weights trained on MS-COCO
modelRCNN.load_weights(COCO_MODEL_PATH, by_name=True)





server_socket = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
server_socket.bind(("", 5000))
server_socket.listen(5)

while 1:
	print("TCPServer Waiting for client on port 5000")
	client_socket, address = server_socket.accept()
	print("I got a connection from "+str(address))
	
	while 1:
		print("Esperando recibir datos...")
		data = client_socket.recv(512)
		string_data = data.decode('utf-8')
		string_data = string_data.rstrip()

		data = json.loads(string_data)

		option = data['option']
		
		print("RECIEVED: "+str(data))		

		# bounding box
		if (option == "B"):
			img_path = data['path_img_selected']
			threshold = data['threshold']
			classes = []

			for i in data['classes_selected']:
				classes.append(int(i))
			bounding_box, path = get_CAM(img_path, model, classes, 'mixed10', True, threshold)

			data_response = {"path_img_output" : path, "bounding_box" : str(bounding_box)}
			string_data = json.dumps(data_response)+"\n"

		# close
		elif (option == "C"):
			data_response = {"closed" : True}
			string_data = json.dumps(data_response)+"\n"
			data=string_data.encode()
			print("Cerrando socket..."+str(data))
			client_socket.send(data)			
			client_socket.close()
			print("Cerrado")
			break

		# heatmap
		elif (option == "HM"):
			img_path = data['path_img_selected']
			
			classes = []

			for i in data['classes_selected']:
				classes.append(int(i))
			path = get_CAM(img_path, model, classes, 'mixed10', False)

			data_response = {"path_img_output" : path}
			string_data = json.dumps(data_response)+"\n"
		# predict
		elif (option == "P"):
			img_path = data['path_img_selected']
			img = image.load_img(img_path, target_size=(IMG_SIZE[0], IMG_SIZE[1]))
			x = image.img_to_array(img)
			x = np.expand_dims(x, axis=0)
			x = preprocess_input(x)

			preds = model.predict(x)
			preds = decode_predictions(preds, top=3)[0]

			classes = []
			for pred in preds:
				x = { pred[1] : str(pred[2]) }
				classes.append(x)

			data_response = {"classes" : classes}
			string_data = json.dumps(data_response)+"\n"
			
		# window sliding
		elif (option == "WS"):
			img_path = data['path_img_selected']
			width = data['widthWS']
			height = data['heightWS']
			step = data['stepSizeWS']

			boundings, path_output = window_sliding(model, img_path, window_size=(width, height), step_size=step)
			data_response = {"path_img_output" : path_output, "bounding_box" : str(boundings)}
			string_data = json.dumps(data_response)+"\n"

		# RCNN
		elif (option == "D"):
			img_path = data['path_img_selected']

			result = detect_objects(img_path,modelRCNN, configRCNN)
			string_data = json.dumps(result)+"\n"			


		data=string_data.encode()
		print("Enviando datos al cliente..."+str(data))
		client_socket.send(data)
		print("Enviado")
		#client_socket.close()