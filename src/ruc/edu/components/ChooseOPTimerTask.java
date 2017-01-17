package ruc.edu.components;

import java.util.TimerTask;

import javax.swing.JOptionPane;
import javax.swing.JTextArea;

import org.jfree.data.time.Millisecond;
import org.jfree.data.time.RegularTimePeriod;

import ruc.edu.core.AdaptiveStorm;
import ruc.edu.core.Mlmodel;
import ruc.edu.tools.ComponentMetric;
import ruc.edu.tools.GetStormUiMetrics;

/**
 * ÿ5���Ӹ���һ������ͼ��
 * @author hankwing
 *
 */
public class ChooseOPTimerTask extends TimerTask {

	ComponentMetric spoutMetric = null;
	ComponentMetric onBoltMetric = null;
	ComponentMetric joinBoltMetric = null;
	ComponentMetric kafkaMetric = null;
	long oldThroughput = 1;
	int isJustChange = 1;
	long lastKafka = 0;			// ���ڿ����ظ�Ԥ������
	AdaptiveStorm adaptiveStorm;
	
	public ChooseOPTimerTask( ComponentMetric[] components, AdaptiveStorm adaptiveStorm) {
		spoutMetric = components[0];
		onBoltMetric = components[1];
		joinBoltMetric = components[2];
		kafkaMetric = components[3];
		this.adaptiveStorm = adaptiveStorm;
	}
	
	@Override
	public void run() {
		// TODO Auto-generated method stub
		isJustChange ++ ;		//	���ڿ��Ƹոı�����ʱ����Ҫ�ı����õ����
		long spoutThroughput = spoutMetric.getAvgThrMinutes();
		long onThroughput = onBoltMetric.getAvgThrMinutes();
		long joinThroughput = joinBoltMetric.getAvgThrMinutes();
		long kafka = kafkaMetric.getAvgThr();
		System.out.println("\n spoutThroughput:" + spoutThroughput + 
				" onThroughput:" + onThroughput + " joinThroughput: " + 
				joinThroughput + " kafkaThroughput:" + kafka);
		String changeInfo = null;
		// need to update model\
		/*stormUiMetrics = new GetStormUiMetrics();
		
		long cpuUsage = (long) ((spoutMetric.cpuUsage + 
				onBoltMetric.cpuUsage + joinBoltMetric.cpuUsage) / 3);
		long memoryUsage = (long) (( spoutMetric.memoryUsage +
				onBoltMetric.memoryUsage + joinBoltMetric.memoryUsage ) / 3);
		otherMetrics.put("spoutRate", (long) spoutMetric.metricSum);
		otherMetrics.put("onBoltRate", (long) onBoltMetric.metricSum);
		otherMetrics.put("joinBoltRate", (long) joinBoltMetric.metricSum);
		otherMetrics.put("cpuUsed", cpuUsage);
		otherMetrics.put("memoryUsage", memoryUsage);
		otherMetrics.put("latency", Math.round(stormUiMetrics.getSpoutLatency()) );
		
		// update the ml model according to new sample
		mlModel.updateModel(otherMetrics);
		//CollectThroughputSamples.sampleFinished.release();
		
		mlModel.updateResult(datas[2], otherMetrics);
		String sampleInfo = "time: " + datas[2] + 
				"\n throughput : " + spoutMetric.metricSum
				+ "\n latency: " + otherMetrics.get("latency") + "\n\n";
		System.out.println(sampleInfo);
		logs[0].append(sampleInfo);
		logs[2].append(sampleInfo);
		*/
		// choose better parameters
		if( kafka - spoutThroughput > adaptiveStorm.maxDRate) {
			// change parameter according to producer
			if( spoutThroughput == 0 || onThroughput == 0 || kafka < 1000 || isJustChange < 4) {
				// ���15���ڵ�����
				oldThroughput = kafka;
				resetMinutes();
				return ;
			}
			// ������Ԥ���kafkaֵ���ϴ�һ�� ����Ҫ��Ԥ����  ����ֻ��stormû��Ԥ�ȶ���
			if(Math.abs(lastKafka - kafka) < 20000) {
				
				return ;
			}
			lastKafka = kafka;
			double multiNumber = (double )kafka / (double)spoutThroughput ;
			System.out.println("predict spoutThroughput: " + (int) (spoutThroughput * multiNumber) + " "
					+ (int) (onThroughput * multiNumber) + " " + (int) (joinThroughput * multiNumber));
			int[] result = adaptiveStorm.mlModel
					.getOptimalParameters(new int[] {
							(int) (spoutThroughput * multiNumber),
							(int) (onThroughput * multiNumber),
							(int) (joinThroughput * multiNumber) });
			if( result == null) return ;	// wekaѡ���ʱ������� ���ؿ�ָ 
			// show message dialog
			changeInfo = "Optimal configuration has to change because producer rate is too high:\n"
					+ "worker: " + result[0] + "  spouts: " + result[3] + "  onBolt:"
					+ result[1] + "  joinBolt: " + result[2] + "\n" + "Predicted CPU usage:" + result[4]
					+ "  memory Usage:" + result[5] + "  throughput confidence:" + result[6] +
					"  latency confidence:" + result[7];
			JOptionPane.showMessageDialog(adaptiveStorm.parentFrame, changeInfo);
			changeInfo = changeInfo + "\n**********************"
								+ "**************************************************\n";
			System.out.println(changeInfo);
			adaptiveStorm.logs[0].append(changeInfo);
			adaptiveStorm.logs[2].append(changeInfo);
			isJustChange = 0;
			// change storm parameters according to results
			adaptiveStorm.changeStormParameters(result, adaptiveStorm.jarFileName);
		}
		else if ( Math.abs(oldThroughput - kafka) > adaptiveStorm.maxDRate) {
			// need change storm parameters
			if( spoutThroughput == 0 || onThroughput == 0 || kafka < 1000 || isJustChange < 4) {
				// ���15���ڵ�����
				oldThroughput = kafka;
				resetMinutes();
				return ;
			}
			// ������Ԥ���kafkaֵ���ϴ�һ�� ����Ҫ��Ԥ����  ����ֻ��stormû��Ԥ�ȶ���
			if(Math.abs(lastKafka - kafka) < 20000) {
				
				return ;
			}
			lastKafka = kafka;
			double multiNumber = (double )kafka / (double)spoutThroughput ;
			System.out.println("predict spoutThroughput: " + (int) (spoutThroughput * multiNumber) + " "
					+ (int) (onThroughput * multiNumber) + " " + (int) (joinThroughput * multiNumber));
			int[] result = adaptiveStorm.mlModel
					.getOptimalParameters(new int[] {
							(int) (spoutThroughput * multiNumber), 
							(int) (onThroughput * multiNumber),
							(int) (joinThroughput * multiNumber)});
			if( result == null) return ;	// wekaѡ���ʱ������� ���ؿ�ָ 
			// show message dialog
			changeInfo = "Optimal configuration has to change because producer rate has changed:\n"
					+ "worker: " + result[0] + "  spouts: " + result[3] + "  onBolt:"
					+ result[1] + "  joinBolt: " + result[2] + "\n" + "Predicted CPU usage:" + result[4]
					+ "  memory Usage:" + result[5] + "  throughput confidence:" + result[6] +
					"  latency confidence:" + result[7];
			JOptionPane.showMessageDialog(adaptiveStorm.parentFrame, changeInfo);
			changeInfo = changeInfo + "\n**********************"
					+ "**************************************************\n";
			System.out.println(changeInfo);
			adaptiveStorm.logs[0].append(changeInfo);
			adaptiveStorm.logs[2].append(changeInfo);
			// ���ڿ��Ƹ�change����������change
			isJustChange = 0;
			// change storm parameters according to results
			adaptiveStorm.changeStormParameters(result, adaptiveStorm.jarFileName);
		}
		oldThroughput = kafka;
		
		resetMinutes();
	}
	
	/**
	 * ���15�������
	 */
	public void resetMinutes() {
		kafkaMetric.reSetFiveSeconds(false);
		spoutMetric.reSetMinutes();
		onBoltMetric.reSetMinutes();
		joinBoltMetric.reSetMinutes();
	}
	
}
