__author__ = 'tangy'

import numpy as np
from scipy.signal import decimate

class BasePreprocessor:
    def fit(self,X,y=None):
        return self

    def transform(self,X):
        return X

class ConcatEEGPreprocessor(BasePreprocessor):

    def __init__(self,downsample=True):
        self.downsample = downsample

    def transform(self,X):
        n_epochs,n_samples,n_channels = X.shape
        X_tf = np.zeros((n_epochs,n_channels*n_samples))
        X_ds = []
        for epoch in xrange(n_epochs):
            for channel in xrange(n_channels):
                X_tf[epoch,channel*n_samples:(channel + 1)*n_samples] = X[epoch,:,channel]
        if self.downsample:
            for epoch in xrange(n_epochs):
                X_ds.append(decimate(X_tf[epoch,:],4))
        else:
            X_ds = X_tf
        return np.array(X_ds)


