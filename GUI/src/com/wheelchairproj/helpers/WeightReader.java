package com.wheelchairproj.helpers;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;



public class WeightReader {
   private static String add;

   /*
	public static void main(String[] args) {
		WeightReader myReader = new WeightReader();
		double theta[] = myReader.getWeights("C:\\Program Files\\MATLAB\\R2012a\\bin\\ML\\Work\\preProcessing\\TESTS\\TEST1\\weights.txt");
		double x[] = myReader.getWeights("C:\\Program Files\\MATLAB\\R2012a\\bin\\ML\\Work\\preProcessing\\TESTS\\TEST1\\X.txt");
		System.out.println(myReader.predictor(theta, x));
		//System.out.println(a[0]);
	}
	*/


   public double[] getWeights(String address){
	   String a[] = read_file(address);
	   double weights[] = new double[a.length];
	   for(int i = 0 ; i < a.length ; i++){
		   weights[i] = Double.parseDouble(a[i]);
		   //System.out.println(weights[i]);
	   }
	   return weights;
   }
	@SuppressWarnings("resource")
	private String[] read_file(String add){
		int size = 0;
		BufferedReader br = null;
		String data[] = null;
		try {

			String sCurrentLine;

			br = new BufferedReader(new FileReader(add));
		
			
			while ((sCurrentLine = br.readLine()) != null) {
				size++;	
			}
			data = new String[size];
			//System.out.print(size);
			int i = 0;
			br.close();
			br = new BufferedReader(new FileReader(add));
			while ((sCurrentLine = br.readLine()) != null) {
				if(i < size){
				data[i]=sCurrentLine;
				//System.out.println(data[i]);
				i++;
				}
			}
		}catch(Exception e){
			e.printStackTrace();
		}
			return data;
	}
	
	public int predictor(double[] theta,double[] cleanedInput){
		double weight = weightMatrixMultiply(cleanedInput,theta);
		double pred = sigmoid(weight);
		//System.out.println(weight + "");
		if(pred > 0.5){
			return 1;
		}else{
			return 0;
		}
	}
	
	private double sigmoid(double ip){
		return (1/(1 + Math.exp(-ip)));
	}
	
	private double weightMatrixMultiply(double[] cleanedInput,double[] theta){
		double mop = 0;
		double m1[] = new double[cleanedInput.length + 1];
		m1[0] = 1;
		double m2[] = theta;
		for(int i = 1; i < m1.length;i++){
			m1[i] = cleanedInput[i-1];
		}
		for(int i = 0; i < m2.length; i++){
			mop = mop + m1[i]*m2[i];
		}
		return mop;
	}
	
}
