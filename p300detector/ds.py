from sklearn.decomposition import FastICA
import numpy as np

class EEGData:
    """
    EEGData accepts raw 2d data in the form n_samples x n_channels and allows the following operations
    - Select/Reject Channels
    - ICA
    """

    def __init__(self,data,channels):
        self.data = data
        self.channels = channels

    def reject_channels(self,chnames = []):
        if len(self.data.shape) == 2:
            n_samples,n_channels = self.data.shape
            chkeepers = [index for index in range(n_channels) if self.channels[index] not in chnames]
            self.data = self.data[:,chkeepers]
            for ch in chnames:
                self.channels.remove(ch)
        else:
            raise Exception("Need 2d array n_samples x n_channels for operations")

    def keep_channels(self,chnames = []):
        if len(self.data.shape) == 2:
            n_samples,n_channels = self.data.shape
            chkeepers = [index for index in range(n_channels) if self.channels[index] in chnames]
            self.data = self.data[:,chkeepers]
            channelnames = []
            for ch in chkeepers:
                channelnames.append(self.channels[ch])
            self.channels = channelnames
        else:
            raise Exception("Need 2d array n_samples x n_channels for operations")

    def run_ica(self):
        if len(self.data.shape) == 2:
            self.data = FastICA().fit_transform(self.data)
        else:
            raise Exception("Need 2d array n_samples x n_channels for operations")

    def extract_epochs(self,Xmarker,marker,start_marker_time = 0,end_marker_time = 86):
        """
        Extract epochs by first finding index positions of markers
        when they first appear in a series and then only pick up next index
        when you encounter it after a set of different markers
        Iterate over these indexes to extract epochs
        :param Xmarker:
        :param marker:
        :param start_marker_time:
        :param end_marker_time:
        :return:
        """
        n_samples,n_channels = self.data.shape
        Xepoch = []
        s = 0
        while(s < n_samples):
            if Xmarker[s] == marker:
                if (Xmarker[s] != Xmarker[s-1]) or (s == 0):
                    Xepoch.append(self.data[s + start_marker_time:s + end_marker_time,:])
            s += 1
        return np.array(Xepoch)

    def avg_channel_pairs(self,chpairs):
        """
        :param chpairs:list of tuples containing channel pairs
        :return:raw matrix with specific channel replaced by pairs
        """
        for (ch1,ch2) in chpairs:
            chpair_indices = [index for index in range(self.data.shape[1]) if self.channels[index] in [ch1,ch2]]
            Xpair = np.mean(self.data[:,chpair_indices],axis=1)
            Xpair = Xpair.reshape(Xpair.shape[0],1)
            self.data = np.delete(self.data,chpair_indices,axis=1)
            self.data = np.concatenate((self.data,Xpair),axis=1)
            self.channels.remove(ch1)
            self.channels.remove(ch2)
            self.channels.append(str(ch1 + ch2))
