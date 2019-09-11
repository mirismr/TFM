from matplotlib import pyplot as plt
import pandas as pd
import numpy as np
import statistics
import os
record_paths = ["TrainData/record1.csv", "TrainData/record2.csv", "TrainData/record3.csv", "TrainData/record4.csv", "TrainData/record5.csv"]

mean_overlapping = []
class_acc = []
loss_rpn_cls = []
loss_rpn_regr = []
loss_class_cls = []
loss_class_regr = []
curr_loss = []
elapsed_time = []
r_epochs = 0
for record_path in record_paths:
    
    record_df = pd.read_csv(record_path)
    r_epochs = len(record_df)

    mean_overlapping.append(record_df['mean_overlapping_bboxes'])
    class_acc.append(record_df['class_acc'])
    loss_rpn_cls.append(record_df['loss_rpn_cls'])
    loss_rpn_regr.append(record_df['loss_rpn_regr'])
    loss_class_cls.append(record_df['loss_class_cls'])
    loss_class_regr.append(record_df['loss_class_regr'])
    curr_loss.append(record_df['curr_loss'])
    elapsed_time.append(record_df['elapsed_time'])


plt.figure(figsize=(15,5))
plt.plot(np.arange(0, r_epochs), mean_overlapping[0], 'r', label='fold 1')
plt.plot(np.arange(0, r_epochs), mean_overlapping[1], 'g', label='fold 2')
plt.plot(np.arange(0, r_epochs), mean_overlapping[2], 'b', label='fold 3')
plt.plot(np.arange(0, r_epochs), mean_overlapping[3], 'c', label='fold 4')
plt.plot(np.arange(0, r_epochs), mean_overlapping[4], 'm', label='fold 5')
plt.title('Mean overlapping bounding boxes')
plt.legend(loc='upper left')

plt.figure(figsize=(15,5))
plt.plot(np.arange(0, r_epochs), class_acc[0], 'r', label='fold 1')
plt.plot(np.arange(0, r_epochs), class_acc[1], 'g', label='fold 2')
plt.plot(np.arange(0, r_epochs), class_acc[2], 'b', label='fold 3')
plt.plot(np.arange(0, r_epochs), class_acc[3], 'c', label='fold 4')
plt.plot(np.arange(0, r_epochs), class_acc[4], 'm', label='fold 5')
plt.title('Classifier accuracy')
plt.legend(loc='upper left')

plt.figure(figsize=(15,5))
plt.plot(np.arange(0, r_epochs), loss_rpn_cls[0], 'r', label='fold 1')
plt.plot(np.arange(0, r_epochs), loss_rpn_cls[1], 'g', label='fold 2')
plt.plot(np.arange(0, r_epochs), loss_rpn_cls[2], 'b', label='fold 3')
plt.plot(np.arange(0, r_epochs), loss_rpn_cls[3], 'c', label='fold 4')
plt.plot(np.arange(0, r_epochs), loss_rpn_cls[4], 'm', label='fold 5')
plt.title('Loss RPN class')
plt.legend(loc='upper left')

plt.figure(figsize=(15,5))
plt.plot(np.arange(0, r_epochs), loss_rpn_regr[0], 'r', label='fold 1')
plt.plot(np.arange(0, r_epochs), loss_rpn_regr[1], 'g', label='fold 2')
plt.plot(np.arange(0, r_epochs), loss_rpn_regr[2], 'b', label='fold 3')
plt.plot(np.arange(0, r_epochs), loss_rpn_regr[3], 'c', label='fold 4')
plt.plot(np.arange(0, r_epochs), loss_rpn_regr[4], 'm', label='fold 5')
plt.title('Loss RPN regression')
plt.legend(loc='upper left')

plt.figure(figsize=(15,5))
plt.plot(np.arange(0, r_epochs), loss_class_cls[0], 'r', label='fold 1')
plt.plot(np.arange(0, r_epochs), loss_class_cls[1], 'g', label='fold 2')
plt.plot(np.arange(0, r_epochs), loss_class_cls[2], 'b', label='fold 3')
plt.plot(np.arange(0, r_epochs), loss_class_cls[3], 'c', label='fold 4')
plt.plot(np.arange(0, r_epochs), loss_class_cls[4], 'm', label='fold 5')
plt.title('Loss class class')
plt.legend(loc='upper left')

plt.figure(figsize=(15,5))
plt.plot(np.arange(0, r_epochs), loss_class_regr[0], 'r', label='fold 1')
plt.plot(np.arange(0, r_epochs), loss_class_regr[1], 'g', label='fold 2')
plt.plot(np.arange(0, r_epochs), loss_class_regr[2], 'b', label='fold 3')
plt.plot(np.arange(0, r_epochs), loss_class_regr[3], 'c', label='fold 4')
plt.plot(np.arange(0, r_epochs), loss_class_regr[4], 'm', label='fold 5')
plt.title('Loss class regression')
plt.legend(loc='upper left')

plt.figure(figsize=(15,5))
plt.plot(np.arange(0, r_epochs), curr_loss[0], 'r', label='fold 1')
plt.plot(np.arange(0, r_epochs), curr_loss[1], 'g', label='fold 2')
plt.plot(np.arange(0, r_epochs), curr_loss[2], 'b', label='fold 3')
plt.plot(np.arange(0, r_epochs), curr_loss[3], 'c', label='fold 4')
plt.plot(np.arange(0, r_epochs), curr_loss[4], 'm', label='fold 5')
plt.title('Curr Loss')
plt.legend(loc='upper left')

plt.figure(figsize=(15,5))
plt.plot(np.arange(0, r_epochs), elapsed_time[0], 'r', label='fold 1')
plt.plot(np.arange(0, r_epochs), elapsed_time[1], 'g', label='fold 2')
plt.plot(np.arange(0, r_epochs), elapsed_time[2], 'b', label='fold 3')
plt.plot(np.arange(0, r_epochs), elapsed_time[3], 'c', label='fold 4')
plt.plot(np.arange(0, r_epochs), elapsed_time[4], 'm', label='fold 5')
plt.title('Elapsed time')
plt.legend(loc='upper left')

plt.show()