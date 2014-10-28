package com.wheelchairproj.mainstuff;

import java.awt.Color;
import java.awt.Frame;
import java.awt.SystemColor;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.util.ArrayList;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSplitPane;
import javax.swing.JTextField;

import biz.source_code.dsp.filter.IirFilter;

import com.jmatio.io.MatFileWriter;
import com.jmatio.types.MLDouble;
import com.wheelchairproj.helpers.MainFilter;
import com.wheelchairproj.helpers.WeightReader;

/*
 * Need to add
 * -> Auto find and removal of channels not giving any data
 * -> ICA
 * -> Artifact Removal
 */

public class TrainTestGUI {
	private int mode;		//0 => Train	1 => Test
	private JFrame frmWheelchairGui,wGui;
	private JButton trainButton,testButton;
	private JLabel flash,pause;
	private JPanel panel,panel_1;
	private JSplitPane splitPane,splitPane_1,splitPane_2;
	private JPanel[] panels;
	private int flashTime,pauseTime;
	private int num_flashes;
	private JTextField flashValue;
	private JTextField pauseValue;
	private WheelchairController myController;
	private WeightReader myManager;
	private MainFilter myFilterDesigner;
	private IirFilter myFilter;
	private double myWeights[];
	private int flashCount = 0;
	private double[][] Xraw,Xfilt,X;
	private double[][][] Xtimeseries;
	private double[] y;
	private int[] p300BNo = {1,3};
	
	
	public TrainTestGUI() {
		/*
		 * Emotiv Initialization
		 */
		initializeEEG();
		/*
		 * GUI Initialization
		 */
		initializeOptionsGUI();
	}

	private void initializeEEG(){
		flashTime = 500;
		pauseTime = 1500;
		num_flashes = 300;
		myController = new WheelchairController();
		myManager = new WeightReader();
		myFilterDesigner = new MainFilter(-10, 10, 0.1, 15, -0.5, 8, 5);
		myFilter = myFilterDesigner.makeFilter();
	}
	
	private void initializeOptionsGUI() {
		frmWheelchairGui = new JFrame();
		frmWheelchairGui.setVisible(true);
		trainButton = new JButton("Train");
		testButton = new JButton("Test");
		flashValue = new JTextField("" + flashTime);
		pauseValue = new JTextField("" + pauseTime);
		flash = new JLabel("Flash Time");
		flash.setToolTipText("Needs to be edited");
		pause = new JLabel("Pause Time");
		pause.setToolTipText("Needs to be edited");
		frmWheelchairGui.getContentPane().setForeground(
				SystemColor.inactiveCaptionBorder);
		frmWheelchairGui.setTitle("Wheelchair GUI");
		frmWheelchairGui.setBounds(100, 100, 509, 300);
		frmWheelchairGui.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

		trainButton.setBounds(200, 50, 100, 40);
		testButton.setBounds(200, 100, 100, 40);
		flash.setBounds(120, 150, 130, 40);
		pause.setBounds(350, 150, 70, 40);
		flashValue.setBounds(80, 190, 110, 30);
		pauseValue.setBounds(320, 190, 110, 30);
		trainButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				// TODO Auto-generated method stub
				try {
					flashTime = Integer.parseInt(flashValue.getText());
					pauseTime = Integer.parseInt(pauseValue.getText());
					mode = 0;
					frmWheelchairGui.setVisible(false);
					frmWheelchairGui.dispose();
					initializewgui();
					startBlinking();
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		});
		testButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				// TODO Auto-generated method stub
				try {
					flashTime = Integer.parseInt(flashValue.getText());
					pauseTime = Integer.parseInt(pauseValue.getText());
					mode = 1;
					frmWheelchairGui.setVisible(false);
					frmWheelchairGui.dispose();
					initializewgui();
					startBlinking();
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		});

		frmWheelchairGui.getContentPane().setLayout(null);
		frmWheelchairGui.getContentPane().add(trainButton);
		frmWheelchairGui.getContentPane().add(testButton);
		frmWheelchairGui.getContentPane().add(flash);
		frmWheelchairGui.getContentPane().add(pause);
		frmWheelchairGui.getContentPane().add(flashValue);
		frmWheelchairGui.getContentPane().add(pauseValue);
	}
	
	private void initializewgui() {
		wGui = new JFrame();
		wGui.setVisible(true);
		wGui.setAlwaysOnTop(true);
		wGui.setExtendedState(Frame.MAXIMIZED_BOTH);
		wGui.setTitle("Wheelchair GUI");
		wGui.getContentPane().setBackground(Color.BLACK);
		wGui.getContentPane().setLayout(new BoxLayout(wGui.getContentPane(), BoxLayout.X_AXIS));
		
		splitPane = new JSplitPane();
		splitPane.setResizeWeight(0.5);
		wGui.getContentPane().add(splitPane);
		
		panel = new JPanel();
		panel.setBackground(SystemColor.desktop);
		splitPane.setLeftComponent(panel);
		panel.setLayout(new BoxLayout(panel, BoxLayout.X_AXIS));
		
		splitPane_1 = new JSplitPane();
		splitPane_1.setOrientation(JSplitPane.VERTICAL_SPLIT);
		splitPane_1.setResizeWeight(0.5);
		panel.add(splitPane_1);
		
		panels = new JPanel[4];
		panels[0] = new JPanel();
		panels[0].setBackground(SystemColor.desktop);
		splitPane_1.setLeftComponent(panels[0]);
		
		panels[1] = new JPanel();
		panels[1].setBackground(SystemColor.desktop);
		splitPane_1.setRightComponent(panels[1]);
		
		panel_1 = new JPanel();
		panel_1.setBackground(SystemColor.desktop);
		splitPane.setRightComponent(panel_1);
		panel_1.setLayout(new BoxLayout(panel_1, BoxLayout.X_AXIS));
		
		JSplitPane splitPane_2 = new JSplitPane();
		splitPane_2.setBackground(SystemColor.desktop);
		splitPane_2.setResizeWeight(0.5);
		splitPane_2.setOrientation(JSplitPane.VERTICAL_SPLIT);
		panel_1.add(splitPane_2);
		
		panels[2] = new JPanel();
		panels[2].setBackground(SystemColor.desktop);
		splitPane_2.setLeftComponent(panels[2]);
		
		panels[3] = new JPanel();
		panels[3].setBackground(SystemColor.desktop);
		splitPane_2.setRightComponent(panels[3]);
		wGui.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
	}
	
	public void startBlinking() throws InterruptedException {
		new Thread(new blinkThread()).start();
	}


	class blinkThread implements Runnable {
		public void run() {
			myController.initializeEmotiv();
			dataSyn ds = new dataSyn();
			X = new double[num_flashes][];
			Xraw = new double[num_flashes][];
			Xfilt = new double[num_flashes][];
			Xtimeseries = new double[num_flashes][WheelchairController.p300_freq][WheelchairController.channels];
			y = new double[num_flashes];
			while (true) {
				int i = 0;
				while (i < 4) {
					if(flashCount >= num_flashes){
						save(X,Xfilt,Xraw,Xtimeseries,y);
						System.exit(0);
					}
					if(ispresent(i, p300BNo)){
						System.out.println("Here");
						y[flashCount] = 1;
					}else{
						System.out.println("Not Here");
						y[flashCount] = 0;
					}
					try {
						panels[i].setBackground(Color.YELLOW);
						new DataCollector(ds);
						Thread.sleep(flashTime);
						panels[i].setBackground(SystemColor.desktop);
						new DataProcessor(ds);
						Thread.sleep(pauseTime);
						i++;
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			}
		}
	}

	class dataSyn {
		boolean flag = false;

		public synchronized void collect() {
			if (flag) {
				try {
					wait();
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}

			//System.out.println("collection started");
			myController.recordData();
			//myController.printData(Xtimeseries[flashCount]);
			//System.out.println(Xtimeseries[0][0][0]);
			//System.out.println(Xtimeseries[1][0][0]);
			//System.out.println(Xtimeseries[flashCount][0][0]);
			flag = true;
			notify();
		}

		public synchronized void process() {
			if (!flag) {
				try {
					wait();
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
			System.out.println("processing started");
			double[][] recordedData  = myController.getRecordedData();
			double processedData[] = myController.processData();
			double filteredData[] = myFilterDesigner.filterData(myFilter,
					processedData);
			double downsampledData[] = myFilterDesigner
					.downSampleData(filteredData);
			for(int i = 0 ; i < recordedData.length ; i++)
				for(int j = 0 ; j < recordedData[0].length ; j++)
					Xtimeseries[flashCount][i][j] = recordedData[i][j];
			Xraw[flashCount] = processedData;
			Xfilt[flashCount] = filteredData;
			X[flashCount] = downsampledData;
			System.out.println(Xtimeseries[0][0][0]);
			flashCount++;
			if(mode == 1){
				if (myManager.predictor(myWeights, downsampledData) == 1) {
					System.out.println("P300 detected");
				} else {
					System.out.println("Non-P300");
				}
			}

			flag = false;
			System.out.println("processing complete");
			notify();
		}
	}
	
	class DataCollector implements Runnable {
		dataSyn ds;

		public DataCollector(dataSyn ds2) {
			this.ds = ds2;
			new Thread(this, "collect").start();
		}

		public void run() {
			ds.collect();
		}
	}
	
	class DataProcessor implements Runnable {
		dataSyn ds;

		public DataProcessor(dataSyn ds2) {
			this.ds = ds2;
			new Thread(this, "process").start();
		}

		public void run() {
			ds.process();
		}
	}
	
	//Extras
	private boolean ispresent(int num,int[] arr){
		for(int elem : arr){
			if(elem == num){
				return true; 
			}
		}
		return false;
	}
	
	@SuppressWarnings({ "unchecked"})
	private void save(double[][] X,double[][] Xfilt,double[][] Xraw,double[][][] Xtimeseries,double[] y){
		MLDouble Xmat = new MLDouble("X", X);
		MLDouble Xrawmat = new MLDouble("Xraw", Xraw);
		MLDouble Xfiltmat = new MLDouble("Xraw", Xfilt);
		int[] dims = {Xtimeseries.length,Xtimeseries[0].length,Xtimeseries[0][0].length};
		MLDouble Xtimeseriesmat = new MLDouble("Xtimeseries", dims);
		for(int i = 0 ; i < dims[0]; i++)
			for(int j = 0 ; j < dims[1] ; j++)
				for(int k = 0 ; k < dims[2] ; k++){
					Xtimeseriesmat.set(Xtimeseries[i][j][k],i,j + k*dims[1]);
				}
		MLDouble ymat = new MLDouble("y", y,1);
		@SuppressWarnings("rawtypes")
		ArrayList list = new ArrayList();
		list.add(Xmat);
		list.add(Xrawmat);
		list.add(Xfiltmat);
		list.add(ymat);
		list.add(Xtimeseriesmat);
		try {
			new MatFileWriter("WPTrainingData.mat", list);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
