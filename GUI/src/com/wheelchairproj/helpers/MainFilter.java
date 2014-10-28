package com.wheelchairproj.helpers;
import biz.source_code.dsp.filter.FilterCharacteristicsType;
import biz.source_code.dsp.filter.FilterPassType;
import biz.source_code.dsp.filter.IirFilter;
import biz.source_code.dsp.filter.IirFilterCoefficients;
import biz.source_code.dsp.filter.IirFilterDesignFisher;
public class MainFilter {

	//Parameters
	public int upperbound;
	public int lowerbound;
	public double fcf1,fcf2,ripple;
	public int filterorder;
	public int downsample_factor;
	
	public MainFilter(int uBound,int lBound,double fcf_1,double fcf_2,
			double rip_ple,int filter_order,int downsamplefactor){
		upperbound = uBound;
		lowerbound = lBound;
		fcf1 = fcf_1;
		fcf2 = fcf_2;
		ripple = rip_ple;
		filterorder = filter_order;
		downsample_factor = downsamplefactor;
	}
	
	public IirFilter makeFilter(){
		IirFilterCoefficients myFilterCoeff = IirFilterDesignFisher.design(FilterPassType.lowpass, FilterCharacteristicsType.chebyshev, 
				filterorder,ripple,fcf1, fcf2);
		return (new IirFilter(myFilterCoeff));
	}
	
	public double[] filterData(IirFilter myFilter,double ipData[]){
		double filt_seq[] = new double[ipData.length];
		for(int j=0;j<ipData.length;j++){
			filt_seq[j]=myFilter.step(ipData[j]);
			//System.out.print(seq[j]+"\t");
			//System.out.println(filt_seq[j]);
			}
		return filt_seq;
	}
	
	public double[] downSampleData(double[] myFilteredData){
		double down_seq[] = new double[(myFilteredData.length/downsample_factor) + 1];
		for(int k=0; k<(myFilteredData.length/downsample_factor) + 1;k++)
	    {
	    	down_seq[k]=myFilteredData[k*downsample_factor];
	    }
		return down_seq;
	}
}
