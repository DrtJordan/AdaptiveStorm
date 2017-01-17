package ruc.edu.components;

import java.util.HashMap;
import java.util.Map;

import kafka.consumer.ConsumerIterator;
import kafka.consumer.KafkaStream;
import ruc.edu.core.AdaptiveStorm;
import ruc.edu.core.Mlmodel;
import ruc.edu.tools.ComponentMetric;

/**
 *
	 处理kafka的中间结果 得到一系列metrics
	 @author hankwing
	
	
 * @author hankwing
 *
 */
public class DrawConsumerRunner implements Runnable{
	private KafkaStream m_stream;
	//private int m_threadNumber;
	//private SimpleDateFormat df = null;
	//private Date thisTime = null;
	//private double oldSpoutRate = 0;
	//private Map<String, Long> otherMetrics = null;
	boolean isError = false;
	ComponentMetric spoutMetric = null;
	ComponentMetric onBoltMetric = null;
	ComponentMetric joinBoltMetric = null;
	ComponentMetric kafkaMetric = null;
	AdaptiveStorm adaptiveStorm = null;
	boolean isEmpirical = false;

	public DrawConsumerRunner(KafkaStream a_stream, int a_threadNumber,
			ComponentMetric[] components, AdaptiveStorm adaptiveStorm, boolean isEmpirical) {
		//m_threadNumber = a_threadNumber;\
		this.isEmpirical = isEmpirical;
		m_stream = a_stream;
		spoutMetric = components[0];
		onBoltMetric = components[1];
		joinBoltMetric = components[2];
		kafkaMetric = components[3];
		this.adaptiveStorm = adaptiveStorm;
		//df = new SimpleDateFormat("dd HH:mm");
		//otherMetrics = new HashMap<String,Long>();
	}

	public void run() {

		// wait until ml model finish
		try {
			Mlmodel.mlFinished.acquire();
		} catch (InterruptedException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		
		try {
			// 画图定时线程
			if( isEmpirical) {
				adaptiveStorm.drawTimer.schedule(new EmpiricalDrawTimerTask( new ComponentMetric[]{ spoutMetric,
						onBoltMetric, joinBoltMetric, kafkaMetric}, 
						adaptiveStorm.plots, adaptiveStorm), 6000, 6000);
			}
			else {
				adaptiveStorm.drawTimer.schedule(new DrawTimerTask( new ComponentMetric[]{ spoutMetric,
					onBoltMetric, joinBoltMetric, kafkaMetric}, 
					adaptiveStorm.plots, adaptiveStorm), 6000, 6000);
			}
			
			ConsumerIterator<byte[], byte[]> it = m_stream.iterator();
			while (it.hasNext()) {
				
				String data = new String(it.next().message());
				data = data.replaceAll("\n", "");
				if( isError) {
					continue;
				}
				// process received data
				String[] datas = data.split(",");
				//System.out.println(data);
				
				switch( datas[0]) {
				/*case "kafkaProducer":
					int kafkaThroughput = Integer.valueOf(datas[1]);
					//System.out.println("kafka throughput: "
					//		+ kafkaThroughput);
					kafkaMetric.throughputSum.addAndGet(kafkaThroughput);
					kafkaMetric.metricNumber.incrementAndGet();
					logs[1].append("kafka throughput: " + kafkaThroughput + "\n");
					logs[3].append("kafka throughput: " + kafkaThroughput + "\n");
					break;*/
				case "spoutDraw":
					spoutMetric.taskCount = Integer.valueOf(datas[1]);
					spoutMetric.cpuUsage.addAndGet((int) (Double.valueOf( datas[3]) * 100));
					spoutMetric.memoryUsage.addAndGet(Long.valueOf(datas[4]).intValue());
					spoutMetric.throughputSum.addAndGet(Integer.valueOf(datas[2]));
					spoutMetric.metricNumber.incrementAndGet();
					break;
				case "onBoltDraw":
					onBoltMetric.taskCount = Integer.valueOf(datas[1]);
					onBoltMetric.cpuUsage.addAndGet((int) (Double.valueOf( datas[3]) * 100));
					onBoltMetric.memoryUsage.addAndGet(Long.valueOf(datas[4]).intValue());
					onBoltMetric.throughputSum.addAndGet(Integer.valueOf(datas[2]));
					onBoltMetric.metricNumber.incrementAndGet();
					break;
				case "joinBoltDraw":
					joinBoltMetric.taskCount = Integer.valueOf(datas[1]);
					joinBoltMetric.cpuUsage.addAndGet((int) (Double.valueOf( datas[3]) * 100));
					joinBoltMetric.memoryUsage.addAndGet(Long.valueOf(datas[4]).intValue());
					joinBoltMetric.throughputSum.addAndGet(Integer.valueOf(datas[2]));
					joinBoltMetric.metricNumber.incrementAndGet();
					break;
				}

			}
		} catch (Exception e) {
			e.printStackTrace();
		}

	}
}
