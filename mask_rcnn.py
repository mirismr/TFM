import numpy as np
import skimage.io
	
def detect_objects(path_img, model, config):
	class_names = ['BG', 'person', 'bicycle', 'car', 'motorcycle', 'airplane',
               'bus', 'train', 'truck', 'boat', 'traffic light',
               'fire hydrant', 'stop sign', 'parking meter', 'bench', 'bird',
               'cat', 'dog', 'horse', 'sheep', 'cow', 'elephant', 'bear',
               'zebra', 'giraffe', 'backpack', 'umbrella', 'handbag', 'tie',
               'suitcase', 'frisbee', 'skis', 'snowboard', 'sports ball',
               'kite', 'baseball bat', 'baseball glove', 'skateboard',
               'surfboard', 'tennis racket', 'bottle', 'wine glass', 'cup',
               'fork', 'knife', 'spoon', 'bowl', 'banana', 'apple',
               'sandwich', 'orange', 'broccoli', 'carrot', 'hot dog', 'pizza',
               'donut', 'cake', 'chair', 'couch', 'potted plant', 'bed',
               'dining table', 'toilet', 'tv', 'laptop', 'mouse', 'remote',
               'keyboard', 'cell phone', 'microwave', 'oven', 'toaster',
               'sink', 'refrigerator', 'book', 'clock', 'vase', 'scissors',
               'teddy bear', 'hair drier', 'toothbrush']
	image = skimage.io.imread(path_img)
	if image.shape[-1] == 4:
		image = image[..., :3]

	# Run detection
	results = model.detect([image], verbose=0)

	# Visualize results
	r = results[0]
	bounding_box = r['rois']
	classes_integer = r['class_ids']
	scores = r['scores']

	result = []
	for i in range(len(classes_integer)):
		class_index = classes_integer[i]
		dictionary = {str(class_names[class_index]) : {'score' : float(scores[i]), 'bbox' : bounding_box[i].tolist()}}
		result.append(dictionary)



	return result
#visualize.display_instances(image, r['rois'], r['masks'], r['class_ids'], class_names, r['scores'])






















