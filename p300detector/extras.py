import matplotlib.pyplot as plt
import numpy as np


class EEGPlotter:

    def __init__(self):
        pass

    def plot_continuous_eeg(self,Xcont,chname,markers = None):
        """
        Plots continuous 2d eeg data of the form n_samples x n_channels
        :param Xcont: Signal of form n_samples x n_channels
        :param chname:Channel to plot
        :param markers:Pending - To show marker positions
        """
        n_samples,_ = Xcont.data.shape
        del _
        ch_index = Xcont.channels.index(chname)
        plt.plot(Xcont.data[:,ch_index])
        plt.xlabel('Time')
        plt.ylabel('Units : uV')
        plt.title(chname)
        plt.show()

    def plot_epoched_eeg_avg(self,Xepoch):
        """
        Plot average of all epoched signals
        :param Xepoch:Epoched EEG
        """
        n_epochs,n_samples,n_channels = Xepoch.shape
        Xavg = np.zeros((n_samples,n_channels))
        for epoch in xrange(n_epochs):
            Xavg += Xepoch[epoch,:,:]
        Xmean = np.average(Xavg,axis=0)
        Xavg = Xavg - Xmean
        for ch in xrange(n_channels):
            plt.plot(Xavg[:,ch])
        plt.show()
        exit()


