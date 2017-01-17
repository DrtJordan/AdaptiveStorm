package ruc.edu.components;

import java.util.TimerTask;

import org.jfree.data.time.Millisecond;
import org.jfree.data.time.RegularTimePeriod;

import ruc.edu.core.AdaptiveStorm;
import ruc.edu.tools.ComponentMetric;
import ruc.edu.tools.GetStormUiMetrics;

/**
 * 每5秒钟更新一次曲线图像
 * @author hankwing
 *
 */
public class DrawTimerTask extends TimerTask {

	ComponentMetric spoutMetric = null;
	ComponentMetric onBoltMetric = null;
	ComponentMetric joinBoltMetric = null;
	ComponentMetric kafkaMetric = null;
	MJFreeChartPanel[] plots;
	AdaptiveStorm adaptiveStorm = null;
	
	public DrawTimerTask( ComponentMetric[] components, MJFreeChartPanel[] plots, AdaptiveStorm adaptiveStorm) {
		this.plots = plots;
		this.adaptiveStorm = adaptiveStorm;
		spoutMetric = components[0];
		onBoltMetric = components[1];
		joinBoltMetric = components[2];
		kafkaMetric = components[3];
	}
	
	@Override
	public void run() {
		// TODO Auto-generated method stub
		int avgCPU = ( spoutMetric.getAvgCpuMetric() + onBoltMetric.getAvgCpuMetric() +
				joinBoltMetric.getAvgCpuMetric() ) / 3;
		int avgMem = (int) (( spoutMetric.getAvgMemoryMetric() + onBoltMetric.getAvgMemoryMetric() +
				joinBoltMetric.getAvgMemoryMetric() ) / 600) ;
		long throughput = spoutMetric.getAvgThr() / 10000;
		//long kafka = kafkaMetric.getAvgThr() / 10000;
		System.out.println(" cpu:" + avgCPU + " avgMem:" + avgMem + 
				" throughput:" + throughput);
		// 4条曲线都需要添加kafka的数据速率
		RegularTimePeriod time = new Millisecond();
		/*plots[0].dataRateSeries.add(time, kafka);
		plots[1].dataRateSeries.add(time , kafka);
		plots[2].dataRateSeries.add(time , kafka);
		plots[3].dataRateSeries.add(time, kafka);*/
		
		// 添加CPU数据
		synchronized (plots) {
			plots[0].adaStormSeries.add(time , avgCPU);
			
			// 添加memory数据
			plots[1].adaStormSeries.add(time , avgMem);
			
			// 添加throughput数据
			plots[2].adaStormSeries.add(time , throughput);
			
			// 添加latency数据
			plots[3].userDefinedSeries.add(time , adaptiveStorm.maxLatency);
			plots[3].adaStormSeries.add(time ,  Math.round(
					new GetStormUiMetrics("192.168.0.17").getSpoutLatency()));
		}
		// 清空5秒钟的数据
		spoutMetric.reSetFiveSeconds(true);
		onBoltMetric.reSetFiveSeconds(true);
		joinBoltMetric.reSetFiveSeconds(true);
		//kafkaMetric.reSetFiveSeconds(false);
		
	}
	
}
