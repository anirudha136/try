from scipy.signal import butter,cheby1,filtfilt,decimate,ellip
import numpy as np
import math




__author__ = 'tangy'

class WPDataProcessor(object):

    def __init__(self,Xraw,filter_type = "cheby1",fc1 = 0.1,fc2 = 15,order = 8):
        self.Xraw = Xraw
        self.num_flashes,self.time_period,self.num_channels = self.Xraw.shape
        self.filter_type = filter_type
        self.fe = self.time_period
        self.fc1 = fc1
        self.fc2 = fc2
        self.order = order

    def preprocess(self):
        self.process_input()
        self.filter()
        self.down_sample()
        return self.Xprocessed

    def info(self):
        print "Number of Flashes : " + str(self.num_flashes)
        print "Number of Channels : " + str(self.num_channels)

    def process_input(self):
        Xprocessed = np.zeros((self.num_flashes,self.num_channels*self.time_period))
        for flash in range(self.num_flashes):
            for channel in range(self.num_channels):
                Xprocessed[flash,channel*self.time_period:(channel + 1)*self.time_period] = self.Xraw[flash,:,channel]
        self.Xprocessed = Xprocessed

    def filter(self):
        Xfilt = np.zeros((self.num_flashes,self.num_channels*self.time_period))
        if self.filter_type == "cheby1":
            b,a = cheby1(self.order,0.6,[2*self.fc1/self.fe,2*self.fc2/self.fe],btype="bandpass",output="ba")
        if self.filter_type == "butter":
            b,a = butter(self.order,[2*self.fc1/self.fe,2*self.fc2/self.fe],output="ba",btype="bandpass")
        if self.filter_type == "elliptic":
            b,a = ellip(self.order,0.6,60,[2*self.fc1/self.fe,2*self.fc2/self.fe],output="ba",btype="bandpass")

        print b
        print a
        for flash in range(self.num_flashes):
            for channel in range(self.num_channels):
                Xfilt[flash,channel*self.time_period:(channel + 1)*self.time_period] = filtfilt(b,a,self.Xprocessed[flash,channel*self.time_period:(channel + 1)*self.time_period])
        self.Xfilt = Xfilt

    def down_sample(self):
        Xdownsample = decimate(self.Xprocessed,(self.fe/self.fc2))
        self.Xdownsample = Xdownsample



