package ruc.edu.tools;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

/**
 * �������ݵ��ռ��ͼ����ֵ
 * @author hankwing
 *
 */
public class ComponentMetric {

	public int taskCount = 0;
	//public List<Integer> metricList = null;
	//public List<Integer> cpuMetricList = null;
	//public List<Integer> memoryMetricList = null;
	// ���ڼ���ƽ��5�������
	public AtomicLong throughputSum;
	public AtomicInteger cpuUsage;
	public AtomicInteger memoryUsage;
	public AtomicInteger metricNumber;
	
	//���ڼ���ƽ��n���ӵ����� ���ڸ���ģ��
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
	 * �õ�ƽ��CPU����
	 * @return
	 */
	public int getAvgCpuMetric() {
		return metricNumber.get() == 0 ? 0 : cpuUsage.get() / metricNumber.get();
	}
	
	/**
	 * �õ�ƽ��5���ӵ�CPU����
	 * @return
	 */
	public int getAvgCpuMetricMinutes() {
		return metricNumberSum.get() == 0 ? 0 : cpuUsageSum.get() / metricNumberSum.get();
	}
	
	/**
	 * �õ�ƽ���ڴ�����
	 * @return
	 */
	public long getAvgMemoryMetric() {
		return metricNumber.get() == 0 ? 0 : memoryUsage.get() / metricNumber.get();
		
	}
	
	/**
	 * �õ�ƽ��n���ӵ��ڴ�����
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
	 * ���5���ӵ�����
	 */
	public void reSetFiveSeconds( boolean isNeedAvg) {

		if( isNeedAvg ) {
			metricNumberSum.incrementAndGet();		// ��������ݵĸ�����1
			// �Ȱ�5���ӵ������ۼӵ�n���ӵ�������
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
	 * ���n���ӵ�����
	 */
	public void reSetMinutes() {
		cpuUsageSum.set(0);
		memoryUsageSum.set(0);
		throughputSumSum.set(0);
		metricNumberSum.set(0);
		letency = 0;
	}
	
}
