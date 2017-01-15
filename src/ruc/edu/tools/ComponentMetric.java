package ruc.edu.tools;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

/**
 * 用于数据的收集和计算均值
 * @author hankwing
 *
 */
public class ComponentMetric {

	public int taskCount = 0;
	//public List<Integer> metricList = null;
	//public List<Integer> cpuMetricList = null;
	//public List<Integer> memoryMetricList = null;
	// 用于计算平均5秒的数据
	public AtomicLong throughputSum;
	public AtomicInteger cpuUsage;
	public AtomicInteger memoryUsage;
	public AtomicInteger metricNumber;
	
	//用于计算平均n分钟的数据 用于更新模型
	public AtomicLong throughputSumSum;
	public AtomicInteger cpuUsageSum;
	public AtomicLong memoryUsageSum;
	public AtomicInteger metricNumberSum;
	
	public long letency = 0;
	
	public ComponentMetric() {
		
		throughputSum = new AtomicLong(0);
		cpuUsage = new AtomicInteger(0);
		memoryUsage = new AtomicInteger(0);
		metricNumber = new AtomicInteger(0);
		
		throughputSumSum = new AtomicLong(0);
		cpuUsageSum = new AtomicInteger(0);
		memoryUsageSum = new AtomicLong(0);
		metricNumberSum = new AtomicInteger(0);
		//metricList = new ArrayList<Integer>();
		//cpuMetricList = new ArrayList<Integer>();
		//memoryMetricList = new ArrayList<Integer>();
	}
	
	public int getTaskCount() {
		return taskCount;
	}
	
	/**
	 * 得到平均CPU数据
	 * @return
	 */
	public int getAvgCpuMetric() {
		return metricNumber.get() == 0 ? 0 : cpuUsage.get() / metricNumber.get();
	}
	
	/**
	 * 得到平均5分钟的CPU数据
	 * @return
	 */
	public int getAvgCpuMetricMinutes() {
		return metricNumberSum.get() == 0 ? 0 : cpuUsageSum.get() / metricNumberSum.get();
	}
	
	/**
	 * 得到平均内存数据
	 * @return
	 */
	public long getAvgMemoryMetric() {
		return metricNumber.get() == 0 ? 0 : memoryUsage.get() / metricNumber.get();
		
	}
	
	/**
	 * 得到平均n分钟的内存数据
	 * @return
	 */
	public long getAvgMemoryMetricMinutes() {
		return metricNumberSum.get() == 0 ? 0 :memoryUsageSum.get() / metricNumberSum.get();
		
	}
	
	public long getAvgThr() {
		if(taskCount == 0) {
			return metricNumber.get() == 0 ? 0 : throughputSum.get() / metricNumber.get();
		}
		else {
			return metricNumber.get() == 0 ? 0 : throughputSum.get() / metricNumber.get() * taskCount;
		}
		
	}
	
	public long getAvgThrMinutes() {
		return metricNumberSum.get() == 0 ? 0 : throughputSumSum.get() / metricNumberSum.get();
	}
	
	/**
	 * 清空5秒钟的数据
	 */
	public void reSetFiveSeconds( boolean isNeedAvg) {

		if( isNeedAvg ) {
			metricNumberSum.incrementAndGet();		// 五分钟数据的个数加1
			// 先把5秒钟的数据累加到n分钟的数据中
			cpuUsageSum.addAndGet(getAvgCpuMetric());
			memoryUsageSum.addAndGet(getAvgMemoryMetric());
			throughputSumSum.addAndGet(getAvgThr());
		}
		
		cpuUsage.set(0);
		memoryUsage.set(0);
		throughputSum.set(0);
		metricNumber.set(0);
		letency = 0;
	}
	
	/**
	 * 清空n分钟的数据
	 */
	public void reSetMinutes() {
		cpuUsageSum.set(0);
		memoryUsageSum.set(0);
		throughputSumSum.set(0);
		metricNumberSum.set(0);
		letency = 0;
	}
	
}
