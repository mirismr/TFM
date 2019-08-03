from skimage.transform import pyramid_gaussian
import imutils
import time
import cv2
from keras.applications.inception_v3 import InceptionV3, preprocess_input, decode_predictions
from keras.preprocessing import image
import numpy as np

IMG_PATH = "collage1.png"
image_in = cv2.imread(IMG_PATH)
WINDOW_SIZE=(200, 200)
STEP_SIZE = 60


def pyramid(image, scale=1.5, min_size=(30,30)):
	yield image

	while image.shape[0] >= min_size[1] and image.shape[1] >= min_size[0]:
		new_width = int(image.shape[1] / scale)
		image = imutils.resize(image, width=new_width)

		yield image

def sliding_window(image, step_size, window_size):
	# slide a window across the image
	for y in range(0, image.shape[0], step_size):
		for x in range(0, image.shape[1], step_size):
			# yield the current window
			yield (x, y, image[y:y + window_size[1], x:x + window_size[0]])




model = InceptionV3(weights='imagenet')
rectangles_found = []
# loop over the image pyramid
for resized in pyramid(image_in, scale=1.3):
	# loop over the sliding window for each layer of the pyramid
	for (x, y, window) in sliding_window(resized, step_size=STEP_SIZE, window_size=WINDOW_SIZE):
		# if the window does not meet our desired window size, ignore it
		if window.shape[0] != WINDOW_SIZE[0] or window.shape[1] != WINDOW_SIZE[1]:
			continue

		img_tensor = image.img_to_array(window)
		img_tensor = np.expand_dims(img_tensor, axis=0)
		img_tensor = preprocess_input(img_tensor)

		preds = model.predict(img_tensor)

		#print("Predicted: ", decode_predictions(preds, top=3)[0])

		# since we do not have a classifier, we'll just draw the window

		predicted_class = decode_predictions(preds, top=1)[0][0][1]
		predicted_value = round(decode_predictions(preds, top=1)[0][0][2], 3)


		clone = resized.copy()
		outlined_image = cv2.rectangle(clone, (x, y), (x + WINDOW_SIZE[1], y + WINDOW_SIZE[0]), (0, 255, 0), 2)
		if (predicted_value > 0.95):
			#cv2.putText(outlined_image, predicted_class+": "+str(predicted_value), (x, y+10), cv2.FONT_HERSHEY_SIMPLEX, 0.5, (0, 255, 0), 1)
			print("Found %s with %f" % (predicted_class,predicted_value))

			rectangles_found.append((x,y, predicted_class, predicted_value))
		cv2.imshow("Window", clone)
		#cv2.imwrite('aleman/window_'+str(x)+'_'+str(y)+'.png',window)
		cv2.waitKey(1)
		time.sleep(0.1)


clone = image_in.copy()
for r in rectangles_found:
	outlined_image = cv2.rectangle(clone, (r[0], r[1]), (r[0] + WINDOW_SIZE[1], r[1] + WINDOW_SIZE[0]), (0, 255, 0), 2)
	cv2.putText(outlined_image, r[2]+": "+str(r[3]), (r[0], r[1] + 10), cv2.FONT_HERSHEY_SIMPLEX, 0.8, (0, 255, 0), 1)

cv2.imwrite('out.png',clone)
