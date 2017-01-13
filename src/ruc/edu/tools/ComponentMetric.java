package ruc.edu.tools;

import java.util.ArrayList;
import java.util.List;

public class ComponentMetric {

	public int taskCount = 0;
	public List<Integer> metricList = null;
	public List<Integer> cpuMetricList = null;
	public List<Integer> memoryMetricList = null;
	public double metricSum = 0;
	public int cpuUsage = 0;
	public int memoryUsage = 0;
	public long letency = 0;
	
	public ComponentMetric() {
		
		metricList = new ArrayList<Integer>();
		cpuMetricList = new ArrayList<Integer>();
		memoryMetricList = new ArrayList<Integer>();
	}
	
	public int getTaskCount() {
		return taskCount;
	}
	
	public int getAvgCpuMetric() {
		int cpuSum = 0;
		for (Integer temp : cpuMetricList) {
			cpuSum += temp;
		}
		cpuUsage = cpuSum / taskCount;
		return cpuUsage;
	}
	
	public long getAvgMemoryMetric() {
		int memorySum = 0;
		for (Integer temp : memoryMetricList) {
			memorySum += temp;
		}
		memoryUsage = memorySum / taskCount;
		return memoryUsage;
		
	}
	
	public void reSet() {
		taskCount = 0;
		metricList.clear();
		cpuMetricList.clear();
		memoryMetricList.clear();
		cpuUsage = 0;
		memoryUsage = 0;
		metricSum = 0;
		letency = 0;
	}
	
}
