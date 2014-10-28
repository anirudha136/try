from filters import ChebshevFilter
from sklearn.metrics import confusion_matrix,accuracy_score
from preprocessors import ConcatEEGPreprocessor
from scipy.io import loadmat
from sklearn.pipeline import Pipeline
from sklearn.linear_model import LogisticRegression
from sklearn.cross_validation import KFold
from ds import EEGData
import numpy as np
import math


"""
P300 Detector
References
 - http://hiran6.blogspot.in/2010/12/p300-and-emotiv-epoc.html
"""


def intialize_source():
    """
    Initialize source to prepare data ready for processing
    :return:Returns matrix of EEG Data,String List of Channels
     and an array containing marker time series positions
    """
    raw = loadmat("p300backrec2.mat")
    channels_raw =  raw['channels']
    channels = []
    for i in channels_raw[0]:
        channels.extend(list(i))
    X = raw['data']
    marker = raw['marker']
    return X,channels,marker




def main():
    Xraw,channels,marker = intialize_source()
    Xraw = EEGData(Xraw,channels)
    #Xraw.reject_channels(chnames=["O1","O2","P7","P8","T7","T8"])
    Xraw.avg_channel_pairs([('O1','O2'),('F3','F4'),('AF3','AF4'),('P7','P8')])
    #View Filtered Raw Data
    X_p300 = Xraw.extract_epochs(Xmarker = marker,marker = 1)
    X_nonp300 = Xraw.extract_epochs(Xmarker = marker,marker = 2)
    X = np.concatenate((X_p300,X_nonp300))
    y = np.concatenate((np.ones(X_p300.shape[0]),np.zeros(X_nonp300.shape[0])))
    xy_tuples = zip(X,y)
    np.random.seed(15)
    np.random.shuffle(xy_tuples)
    acc_list = []
    kf = KFold(n=len(xy_tuples),n_folds=4,shuffle=False,random_state=None)
    for train_indices,test_indices in kf:
        X_train,y_train = (np.array(l) for l in zip(*[xy_tuples[i] for i in train_indices]))
        X_test,y_test = (np.array(l) for l in zip(*[xy_tuples[i] for i in test_indices]))
        eeg_clf = Pipeline([
            ('filter',ChebshevFilter(order=6)),
            ('featurecreation',ConcatEEGPreprocessor(downsample=False)),
            ('predictor',LogisticRegression())
        ])
        eeg_clf.fit(X_train,y_train)
        print confusion_matrix(y_train,eeg_clf.predict(X_train))
        print confusion_matrix(y_test,eeg_clf.predict(X_test))
        acc_list.append(accuracy_score(y_test,eeg_clf.predict(X_test))*100)
    print "Average Accuracy :" + str(np.mean(acc_list))


if __name__ == '__main__':
    main()