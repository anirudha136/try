package com.wheelchairproj.mainstuff;

import com.wheelchairproj.helpers.MainFilter;

public class Tester {

	static double ipData[] = new double[1000];
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		//Fill ipData
		for(int i = 0 ; i < 100 ; i++)
			ipData[i] = genRndom(-10, 10);
		MainFilter myFilterDesigner = new MainFilter(10, -10, 0.1, 24, -0.5, 6, 12);
		double filteredData[] = myFilterDesigner.filterData(myFilterDesigner.makeFilter(), ipData);
		double downSampledData[] = myFilterDesigner.downSampleData(filteredData);
	}

	private static double genRndom(int start,int end){
		return (Math.random()*(end - start) + start);
	}
}
