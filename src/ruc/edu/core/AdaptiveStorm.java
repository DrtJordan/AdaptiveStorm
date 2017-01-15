package ruc.edu.core;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Timer;
import java.util.TimerTask;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import javax.swing.JTextArea;

import org.jfree.data.time.TimeSeries;

import ruc.edu.components.ChooseOPTimerTask;
import ruc.edu.components.DrawTimerTask;
import ruc.edu.components.MJFreeChartPanel;
import ruc.edu.tools.ComponentMetric;
import ruc.edu.tools.GetStormUiMetrics;
import kafka.consumer.ConsumerConfig;
import kafka.consumer.ConsumerIterator;
import kafka.consumer.KafkaStream;
import kafka.javaapi.consumer.ConsumerConnector;

public class AdaptiveStorm {

	public AdaptiveStorm adaptiveStorm = null;
	String groupId = UUID.randomUUID().toString();
	String zookeeper = "192.168.0.19:2181,192.168.0.21:2181,192.168.0.22:2181,192.168.0.23:2181"
			+ ",192.168.0.24:2181";
	String drawTopic = "drawtopics";				// 接收画图的kafka topic
	//String tpchTemptopic = "tpchtemptopics";		// 接收是否改变配置的kafka topic
	String oldTopologyName = "tpchquery";
	
	// 可在界面中动态改变的参数
	String regressionAlg = "J48";
	String classifiAlg = "Multilayer Perceptron";
	public int collecInterval = 3;
	public int maxDRate = 30000;
	public int checkpoints = 2;
	public int maxLatency = 2000;
	public int dataRateLevel = 1;

	private ConsumerConnector consumer;
	private ExecutorService executor1;
	private ExecutorService executor2;
	public Mlmodel mlModel = null;
	// 用户获得实时延时数据
	private GetStormUiMetrics stormUiMetrics = null;
	//private CollectThroughputSamples collectSamples = null;
	public JTextArea[] logs;
	public String jarFileName;
	int[] oldParameters = new int[4];
	// 4条曲线
	public MJFreeChartPanel[] plots = new MJFreeChartPanel[4];
	public ComponentMetric spoutMetric = null;
	public ComponentMetric onBoltMetric = null;
	public ComponentMetric joinBoltMetric = null;
	public ComponentMetric kafkaMetric = null;
	public Timer adaStorm = null;
	
	public AdaptiveStorm() {
		this.adaptiveStorm = this;
		spoutMetric = new ComponentMetric();
		onBoltMetric = new ComponentMetric();
		joinBoltMetric = new ComponentMetric();
		kafkaMetric = new ComponentMetric();
		adaStorm = new Timer();
	}
	
	public void setLogPanels(JTextArea[] logs, MJFreeChartPanel[] plots ) {
		this.logs = logs;
		this.plots = plots;
	}
	/**
	 * 开始运行Storm
	 */
	public void startStorm( String jarFileName) {
		this.jarFileName = jarFileName;
		consumer = kafka.consumer.Consumer
				.createJavaConsumerConnector(createConsumerConfig(zookeeper,
						groupId));
		// 训练模型
		mlModel = new Mlmodel();
		Process process;
		try {
			process = Runtime
					.getRuntime()
					.exec(new String[] {
							"bash",
							"-c",
							"ssh wamdm7 \"source /etc/profile ; cd ~/wengzujian/ ;"
									+ "storm jar " + jarFileName
									+ " storm.starter.TPCHQuery3 tpchquery"
									+ " 50 10 30 28 60 10 false && exit\" "});
			process.waitFor();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		// 初始化kafka消费线程
		Map<String, Integer> topicCountMap = new HashMap<String, Integer>();
		topicCountMap.put(drawTopic, new Integer(1));
		//topicCountMap.put(tpchTemptopic, new Integer(1));
		Map<String, List<KafkaStream<byte[], byte[]>>> consumerMap = consumer
				.createMessageStreams(topicCountMap);
		List<KafkaStream<byte[], byte[]>> streams = consumerMap.get(drawTopic);
		//List<KafkaStream<byte[], byte[]>> streams2 = consumerMap.get(tpchTemptopic);
		
		//collectSamples.startCollect();	// upload storm topology
		// now launch all the threads
		//
		executor1 = Executors.newFixedThreadPool(1);
		//executor2 = Executors.newFixedThreadPool(1);

		// now create an object to consume the messages
		//
		int threadNumber = 0;
		
		for (final KafkaStream<byte[], byte[]> stream : streams) {
			executor1.submit(new ConsumerTest(stream, threadNumber));
			threadNumber++;
		}
		
		/*for (final KafkaStream<byte[], byte[]> stream : streams2) {
			executor2.submit(new ConsumerTest(stream, threadNumber));
			threadNumber++;
		}*/
		//collectSamples = new CollectThroughputSamples();
	}

	public void shutdown() {
		if (consumer != null)
			consumer.shutdown();
		if (executor1 != null)
			executor1.shutdown();
		if (executor2 != null)
			executor2.shutdown();
		try {
			if (!executor1.awaitTermination(5000, TimeUnit.MILLISECONDS)) {
				System.out
						.println("Timed out waiting for consumer threads to shut down, exiting uncleanly");
			}
			if (!executor2.awaitTermination(5000, TimeUnit.MILLISECONDS)) {
				System.out
						.println("Timed out waiting for consumer threads to shut down, exiting uncleanly");
			}
		} catch (InterruptedException e) {
			System.out
					.println("Interrupted during shutdown, exiting uncleanly");
		}
	}

	private static ConsumerConfig createConsumerConfig(String a_zookeeper,
			String a_groupId) {
		Properties props = new Properties();
		props.put("zookeeper.connect", a_zookeeper);
		props.put("group.id", a_groupId);
		props.put("zookeeper.session.timeout.ms", "400");
		props.put("zookeeper.sync.time.ms", "200");
		props.put("auto.commit.interval.ms", "1000");

		return new ConsumerConfig(props);
	}

	/*public static void main(String[] args) {

		AdaptiveStorm example = new AdaptiveStorm();
		example.run(1);
	}*/

	/**
	 * 处理kafka的中间结果 得到一系列metrics
	 * @author hankwing
	 *
	 */
	public class ConsumerTest implements Runnable {
		private KafkaStream m_stream;
		//private int m_threadNumber;
		//private SimpleDateFormat df = null;
		//private Date thisTime = null;
		private double oldSpoutRate = 0;
		private Map<String, Long> otherMetrics = null;
		boolean isError = false;
		Timer drawTimer = null;

		public ConsumerTest(KafkaStream a_stream, int a_threadNumber) {
			//m_threadNumber = a_threadNumber;
			m_stream = a_stream;
			//df = new SimpleDateFormat("dd HH:mm");
			otherMetrics = new HashMap<String,Long>();
			drawTimer = new Timer();				// 画图线程
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
				drawTimer.schedule(new DrawTimerTask( new ComponentMetric[]{ spoutMetric,
						onBoltMetric, joinBoltMetric, kafkaMetric}, plots), 6000, 6000);
				
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

	// change storm's parameters
	public void changeStormParameters(int[] args, String jarFileName) {
		
		if( oldParameters[0] == args[0] && oldParameters[1] == args[1] 
				&& oldParameters[2] == args[2] && oldParameters[3] == args[3]) {
			// the same as old paramters
			return;
		}
		
		oldParameters = args.clone();
		
		Process process = null;
		List<String> processList = new ArrayList<String>();
		try {
			String newTopologyName = "tpchquery"+ UUID.randomUUID().toString();
			process = Runtime
					.getRuntime()
					.exec(new String[] {
							"bash",
							"-c",
							"ssh wamdm7 \"source /etc/profile ; cd ~/wengzujian/ ;"
									+ "storm jar " + jarFileName 
									+ " storm.starter.TPCHQuery3 "
									+ newTopologyName + " "
									+ args[0] + " " + args[3] + " " + args[1]
									+ " " + args[2] + " 60 10 false && ./apache-storm-0.9.5/bin/storm "
											+ "kill "+ oldTopologyName + " && exit\" "});
			
			process.waitFor();
			oldTopologyName = newTopologyName;
			//BufferedReader input = new BufferedReader(new InputStreamReader(
			//		process.getInputStream()));
			//String line = "";.
			//while ((line = input.readLine()) != null) {
			//	processList.add(line);
			//}
			//input.close();
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		//for (String templine : processList) {
		//	System.out.println(templine);
		//}
	}
	
	public void startAdaStorm() {
		// 寻找最优参数定时线程
		// 先清空之前的数据  因为可能非常小
		spoutMetric.reSetMinutes();
		onBoltMetric.reSetMinutes();
		joinBoltMetric.reSetMinutes();
		kafkaMetric.reSetMinutes();
		adaStorm.schedule(new ChooseOPTimerTask( new ComponentMetric[]{ spoutMetric,
				onBoltMetric, joinBoltMetric, kafkaMetric}, adaptiveStorm), 18000,
				18000);
	}

}
