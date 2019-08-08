from skimage.transform import pyramid_gaussian
import imutils
import time
import cv2
from keras.applications.inception_v3 import InceptionV3, preprocess_input, decode_predictions
from keras.preprocessing import image
import numpy as np

IMG_PATH = "images/perro_campo.jpg"
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


def non_max_suppression_slow(boxes, probabilities, overlapThresh):
	# if there are no boxes, return an empty list
	if len(boxes) == 0:
		return []

	# initialize the list of picked indexes
	pick = []

	# grab the coordinates of the bounding boxes
	x1 = boxes[:, 0]
	y1 = boxes[:, 1]
	x2 = boxes[:, 2]
	y2 = boxes[:, 3]

	# compute the area of the bounding boxes and sort the bounding
	# boxes by the bottom-right y-coordinate of the bounding box
	area = (x2 - x1 + 1) * (y2 - y1 + 1)
	idxs = np.argsort(probabilities[0])

	# keep looping while some indexes still remain in the indexes
	# list
	while len(idxs) > 0:
		# grab the last index in the indexes list, add the index
		# value to the list of picked indexes, then initialize
		# the suppression list (i.e. indexes that will be deleted)
		# using the last index
		last = len(idxs) - 1
		i = idxs[last]
		pick.append(i)
		suppress = [last]

		# loop over all indexes in the indexes list
		for pos in range(0, last):
			# grab the current index
			j = idxs[pos]

			# find the largest (x, y) coordinates for the start of
			# the bounding box and the smallest (x, y) coordinates
			# for the end of the bounding box
			xx1 = max(x1[i], x1[j])
			yy1 = max(y1[i], y1[j])
			xx2 = min(x2[i], x2[j])
			yy2 = min(y2[i], y2[j])

			# compute the width and height of the bounding box
			w = max(0, xx2 - xx1 + 1)
			h = max(0, yy2 - yy1 + 1)

			# compute the ratio of overlap between the computed
			# bounding box and the bounding box in the area list
			overlap = float(w * h) / area[j]

			# if there is sufficient overlap, suppress the
			# current bounding box
			if overlap > overlapThresh:
				suppress.append(pos)

		# delete all indexes from the index list that are in the
		# suppression list
		idxs = np.delete(idxs, suppress)

	# return only the bounding boxes ids that were picked
	return pick



model = InceptionV3(weights='imagenet')
rectangles_found = []
rectangles_found_supressed = []
probabilities = []
downscale_power = 0
scale = 1.3
# loop over the image pyramid
for resized in pyramid(image_in, scale):
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

			width = x + int(WINDOW_SIZE[1] * (scale ** downscale_power))
			height = y + int(WINDOW_SIZE[0] * (scale ** downscale_power))

			rectangles_found.append((x,y, width, height, predicted_class, predicted_value))
			rectangles_found_supressed.append((x,y, width, height))
			probabilities.append((predicted_value, predicted_class))
		#cv2.imshow("Window", clone)
		#cv2.imwrite('aleman/window_'+str(x)+'_'+str(y)+'.png',window)
		#cv2.waitKey(1)
		#time.sleep(0.1)

	# step for scale
	downscale_power +=1

clone = image_in.copy()
for r in rectangles_found:
	outlined_image = cv2.rectangle(clone, (r[0], r[1]), (r[2], r[3]), (0, 255, 0), 2)
	cv2.putText(outlined_image, r[4]+": "+str(r[5]), (r[0], r[1] + 10), cv2.FONT_HERSHEY_SIMPLEX, 0.8, (0, 255, 0), 1)

cv2.imwrite('out.png',clone)


rectangles_found_supressed = np.array(rectangles_found_supressed)
idxs = non_max_suppression_slow(rectangles_found_supressed,probabilities, 0.2)

clone = image_in.copy()
for i in idxs:
	r = rectangles_found[i]
	outlined_image = cv2.rectangle(clone, (r[0], r[1]), (r[2], r[3]), (0, 255, 0), 2)
	cv2.putText(outlined_image, str(probabilities[i][0]) + ": " + str(probabilities[i][1]), (r[0], r[1] + 10), cv2.FONT_HERSHEY_SIMPLEX, 0.8, (0, 255, 0),
				1)


cv2.imwrite('out2.png',clone)
