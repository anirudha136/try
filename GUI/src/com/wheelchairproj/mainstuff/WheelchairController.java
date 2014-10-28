package com.wheelchairproj.mainstuff;
import java.util.HashMap;

import com.sun.jna.Pointer;
import com.sun.jna.ptr.IntByReference;
import com.wheelchairproj.helpers.Edk;
import com.wheelchairproj.helpers.EdkErrorCode;


public class WheelchairController {
	static Pointer eEvent,eState,hData;
	static IntByReference nSamplesTaken,userID;
	static int state;
	static float secs;
	static boolean readytocollect;
	static int recording_freq;
	public static int channelsTotal;
	public static int channels;
	public static int p300_freq;
	double myData[][];
	double Sig[];
	@SuppressWarnings("rawtypes")
	public HashMap channelMapping;
	private String[] channelsToRemove;
	
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public WheelchairController(){
		recording_freq = 128;
		channelsTotal  = 16;
		p300_freq = (int)Math.ceil((double)recording_freq*(2.0/3.0));
		channelsToRemove = new String[]{"AF3","AF4"};
		channels = channelsTotal - channelsToRemove.length;
		myData = new double[p300_freq][channels];
		Sig = new double[p300_freq*channels];
		channelMapping  = new HashMap();
		channelMapping.put("T", 0);
		channelMapping.put("AF3", 1);
		channelMapping.put("AF4", 2);
		channelMapping.put("F3", 3);
		channelMapping.put("F4", 4);
		channelMapping.put("F7", 5);
		channelMapping.put("F8", 6);
		channelMapping.put("FC5", 7);
		channelMapping.put("FC6", 8);
		channelMapping.put("P3", 9);//CMS
		channelMapping.put("P4", 10);//DRL
		channelMapping.put("P7", 11);
		channelMapping.put("P8", 12);
		channelMapping.put("T7", 13);
		channelMapping.put("T8", 14);
		channelMapping.put("O1", 15);
		channelMapping.put("O2", 16);
	}

	public void initializeEmotiv(){
		eEvent = Edk.INSTANCE.EE_EmoEngineEventCreate();
		eState = Edk.INSTANCE.EE_EmoStateCreate();
		nSamplesTaken = null;
		userID = null;
		state = 0;
		secs = 1;
		readytocollect = false;
		
		nSamplesTaken = new IntByReference(0);
		userID = new IntByReference(0);
		if (Edk.INSTANCE.EE_EngineConnect("Emotiv Systems-5") != EdkErrorCode.EDK_OK.ToInt()) {
			System.out.println("Emotiv Engine start up failed.");
		}
		
		hData = Edk.INSTANCE.EE_DataCreate();
		Edk.INSTANCE.EE_DataSetBufferSizeInSec(secs);
		System.out.print("Buffer size in secs: ");
		System.out.println(secs);
		
		System.out.println("Start receiving EEG Data!");
	}
	
	public void recordData(){
		int addition = -1;
		while (true) 
		{	
			state = Edk.INSTANCE.EE_EngineGetNextEvent(eEvent);

			// New event needs to be handled
			if (state == EdkErrorCode.EDK_OK.ToInt()) 
			{
				int eventType = Edk.INSTANCE.EE_EmoEngineEventGetType(eEvent);
				Edk.INSTANCE.EE_EmoEngineEventGetUserId(eEvent, userID);

				// Log the EmoState if it has been updated
				if (eventType == Edk.EE_Event_t.EE_UserAdded.ToInt()) 
				if (userID != null)
					{
						System.out.println("User added");
						Edk.INSTANCE.EE_DataAcquisitionEnable(userID.getValue(),true);
						readytocollect = true;
					}
			}
			else if (state != EdkErrorCode.EDK_NO_EVENT.ToInt()) {
				System.out.println("Internal error in Emotiv Engine!");
				break;
			}
			
			if (readytocollect) 
			{
				Edk.INSTANCE.EE_DataUpdateHandle(0, hData);

				Edk.INSTANCE.EE_DataGetNumberOfSample(hData, nSamplesTaken);

				if (nSamplesTaken != null)
				{
					if (nSamplesTaken.getValue() != 0) {
						
						//System.out.print("Updated: ");
						//System.out.println(nSamplesTaken.getValue());
						
						double[] data = new double[nSamplesTaken.getValue()];
						for (int sampleIdx=0 ; sampleIdx<nSamplesTaken.getValue() ; ++ sampleIdx) {
							int index = 0;
							addition += 1;
							if(addition > p300_freq - 1){
								return;
							}else{
								for (int i = 1 ; i <= channelsTotal ; i++) {
									if(checkChannel(i)){
										continue;
									}else{
									Edk.INSTANCE.EE_DataGet(hData, i, data, nSamplesTaken.getValue());
									//addition refers to time
									myData[addition][index++] = data[sampleIdx];
									//System.out.print(data[sampleIdx]);
									}
								}	
							}
						}
					}
				} 
			}
		}
    	
    	Edk.INSTANCE.EE_EngineDisconnect();
    	Edk.INSTANCE.EE_EmoStateFree(eState);
    	Edk.INSTANCE.EE_EmoEngineEventFree(eEvent);
    	//System.out.println("Recording done");
}
	
	public double[][] getRecordedData(){
		return myData;
	}
	
	public void printData(double[][] myData){
		for(int i = 0 ; i < p300_freq; i++){
			for(int j = 0; j < channels ; j++){
				System.out.print(myData[i][j] + "  ");
			}
			System.out.print("\n");
		}
	}
	
	public void printSig(){
		for(int i = 0 ; i < channels*p300_freq ; i++){
			System.out.println(Sig[i]);
		}
	}

	
	public double[] processData(){
		//First accumulate the data
		for(int channel = 0; channel < channels;channel++){
			for(int j = channel*p300_freq ; j < (channel + 1)*p300_freq;j++ ){
					Sig[j] = myData[j % p300_freq][channel];
					//System.out.print(Sig[j]);
				}
			}
		return Sig;
		}
	
	public boolean checkChannel(int chNum){
		for(int i = 0 ; i < channelsToRemove.length ; i++){
			if(chNum == (int)channelMapping.get(channelsToRemove[i])){
				return true;
			}
		}
		return false;
	}
	
	class LogThread implements Runnable {
		public void run() {
			int j = 0;
			while (j <= 128) 
			{	
				state = Edk.INSTANCE.EE_EngineGetNextEvent(eEvent);

				// New event needs to be handled
				if (state == EdkErrorCode.EDK_OK.ToInt()) 
				{
					int eventType = Edk.INSTANCE.EE_EmoEngineEventGetType(eEvent);
					Edk.INSTANCE.EE_EmoEngineEventGetUserId(eEvent, userID);

					// Log the EmoState if it has been updated
					if (eventType == Edk.EE_Event_t.EE_UserAdded.ToInt()) 
					if (userID != null)
						{
							System.out.println("User added");
							Edk.INSTANCE.EE_DataAcquisitionEnable(userID.getValue(),true);
							readytocollect = true;
						}
				}
				else if (state != EdkErrorCode.EDK_NO_EVENT.ToInt()) {
					System.out.println("Internal error in Emotiv Engine!");
					break;
				}
				
				if (readytocollect) 
				{
					Edk.INSTANCE.EE_DataUpdateHandle(0, hData);

					Edk.INSTANCE.EE_DataGetNumberOfSample(hData, nSamplesTaken);

					if (nSamplesTaken != null)
					{
						if (nSamplesTaken.getValue() != 0) {
							
							System.out.print("Updated: ");
							System.out.println(nSamplesTaken.getValue());
							
							double[] data = new double[nSamplesTaken.getValue()];
							for (int sampleIdx=0 ; sampleIdx<nSamplesTaken.getValue() ; ++ sampleIdx) {
								for (int i = 0 ; i < 16 ; i++) {

									Edk.INSTANCE.EE_DataGet(hData, i, data, nSamplesTaken.getValue());
									System.out.print(data[sampleIdx]);
									System.out.print(",");
								}	
								System.out.println();
								j+=1;
							}
						}
					}
				}
			}
	    	
	    	Edk.INSTANCE.EE_EngineDisconnect();
	    	Edk.INSTANCE.EE_EmoStateFree(eState);
	    	Edk.INSTANCE.EE_EmoEngineEventFree(eEvent);
	    	System.out.println("Disconnected!");

		}
	}
}

/*
public static void main(String[] args){
	WheelchairController myController = new WheelchairController();
	System.out.println("P300 Freq : " + p300_freq + "\n");
	myController.initializeEmotiv();
	//new Thread(myController.new LogThread()).start();
	myController.recordData();
	myController.processData();
	//myController.printSig();
	//myController.printData();
}
*/

