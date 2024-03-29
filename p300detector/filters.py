from __future__ import division
from scipy.signal import cheby1,filtfilt,butter,ellip
import numpy as np

"""
Contains
->Chebyshev Filter
->Butterworth Filter
->Elliptic Filter
Add
-2d data handling
"""

class BaseFilter:

    def fit(self,X,y=None):
        return self

    def transform(self,X):
        return X

class ChebshevFilter(BaseFilter):

    def __init__(self,fc1 = 0.1,fc2 = 15,order = 8,ripple = 0.5,sampling_rate = 128,input_type="epoched"):
        self.fc1 = fc1
        self.fc2 = fc2
        self.order = order
        self.ripple = ripple
        self.fe = sampling_rate
        self.input_type = input_type

    def make_filter(self):
        b,a = cheby1(self.order,self.ripple,[float(2*self.fc1)/self.fe,float(2*self.fc2)/self.fe],btype="bandpass",output="ba")
        return b,a

    def transform(self,X):
        b,a = self.make_filter()
        if self.input_type == "epoched":
            n_epochs,n_samples,n_channels = X.shape
            Xfilt = np.zeros((n_epochs,n_samples,n_channels))
            for epoch in xrange(n_epochs):
                for channel in xrange(n_channels):
                    Xfilt[epoch,:,channel] = filtfilt(b,a,X[epoch,:,channel])
        elif self.input_type == "continuous":
            n_samples,n_channels = X.shape
            Xfilt = np.zeros((n_samples,n_channels))
            for channel in xrange(n_channels):
                Xfilt[:,channel] = filtfilt(b,a,X[:,channel].T)
        else:
            raise Exception("Incorrect input-type : choose 'epoched'/'continuous'")
        return Xfilt

class ButterworthFilter(BaseFilter):

    def __init__(self,fc1 = 0.1,fc2 = 15,order = 8,sampling_rate = 128,input_type="epoched"):
        self.fc1 = fc1
        self.fc2 = fc2
        self.order = order
        self.fe = sampling_rate
        self.input_type = input_type

    def make_filter(self):
        b,a = butter(self.order,[2*self.fc1/self.fe,2*self.fc2/self.fe],output="ba",btype="bandpass")
        return b,a

    def transform(self,X):
        b,a = self.make_filter()
        if self.input_type == "epoched":
            n_epochs,n_samples,n_channels = X.shape
            Xfilt = np.zeros((n_epochs,n_samples,n_channels))
            for epoch in xrange(n_epochs):
                for channel in xrange(n_channels):
                    Xfilt[epoch,:,channel] = filtfilt(b,a,X[epoch,:,channel])
        elif self.input_type == "continuous":
            n_samples,n_channels = X.shape
            Xfilt = np.zeros((n_samples,n_channels))
            for channel in xrange(n_channels):
                Xfilt[:,channel] = filtfilt(b,a,X[:,channel].T)
        else:
            raise Exception("Incorrect input-type : choose 'epoched'/'continuous'")
        return Xfilt

class EllipticFilter(BaseFilter):

    def __init__(self,fc1 = 0.1,fc2 = 15,order = 8,sampling_rate = 128,attentuation = 60,input_type="epoch"):
        self.fc1 = fc1
        self.fc2 = fc2
        self.order = order
        self.fe = sampling_rate
        self.input_type = input_type
        self.attentuation = attentuation

    def make_filter(self):
        b,a = ellip(self.order,self.attentuation,[2*self.fc1/self.fe,2*self.fc2/self.fe],output="ba",btype="bandpass")
        return b,a

    def transform(self,X):
        b,a = self.make_filter()
        if self.input_type == "epoched":
            n_epochs,n_samples,n_channels = X.shape
            Xfilt = np.zeros((n_epochs,n_channels*n_samples))
            for epoch in xrange(n_epochs):
                for channel in xrange(n_channels):
                    Xfilt[epoch,:,channel] = filtfilt(b,a,X[epoch,:,channel])
        elif self.input_type == "continuous":
            n_samples,n_channels = X.shape
            Xfilt = np.zeros((n_samples,n_channels))
            for channel in xrange(n_channels):
                Xfilt[:,channel] = filtfilt(b,a,X[:,channel])
        else:
            raise Exception("Incorrect input-type : choose 'epoched'/'continuous'")
        return Xfilt

class FilterBank(BaseFilter):

    def __init__(self,fc1 = 0.1,fc2 = 15,order = 8,ripple = 0.5,fs = 128,M = 8,input_type="epoched"):
        self.fc1 = fc1
        self.fc2 = fc2
        self.order = order
        self.fs = fs
        self.input_type = input_type
        self.ripple = ripple
        self.M = M
        self.d = (fc2 - fc1)/M

    def filter_params(self,m):
        Wn = [2*(self.fc1 + self.d*(m))/self.fs,2*(self.fc1 + self.d*(m+1))/self.fs]
        b,a = cheby1(self.order,self.ripple,Wn,btype="bandpass")
        return b,a

    def transform(self,X):
        if self.input_type == "epoched":
            n_epochs,n_samples,n_channels = X.shape
            Xfilt = np.zeros((n_epochs,n_samples,n_channels*self.M))
            for epoch in xrange(n_epochs):
                for channel in xrange(n_channels):
                    for m in xrange(self.M):
                        b,a = self.filter_params(m)
                        Xfilt[epoch,:,self.M*channel + m] = filtfilt(b,a,X[epoch,:,channel])
        elif self.input_type == "continuous":
            n_samples,n_channels = X.shape
            Xfilt = np.zeros((n_samples,n_channels*self.M))
            for channel in xrange(n_channels):
                for m in xrange(self.M):
                    b,a = self.filter_params(m)
                    Xfilt[:,self.M*channel + m] = filtfilt(b,a,X[:,channel])
        else:
            raise Exception("Incorrect input-type : choose 'epoched'/'continuous'")
        return Xfilt

class WaveletTransform(BaseFiltere):

 #added by Ani

    def __init__(self,samples=512,fs=256,fc=47):
        self.samples=samples
        self.fc=fc
        self.fs=fs

    def transform(self,X):
        for i in range(1,9):
            b,a=scipy.signal.cheby1(4,0.5,2*fc/(i*sampling_freq),btype='low')
            flag=scipy.signal.filtfilt(b,a,signal)
            #signal_downsample=scipy.signal.resample(signal,samples/np.matrix_power(2,i))
            flag_downsample=scipy.signal.resample(flag,samples/np.power(2,i))
            wt=np.append(wt,flag_downsample)

        return wt