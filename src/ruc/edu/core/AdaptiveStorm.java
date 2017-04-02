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
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import javax.swing.JTextArea;

import org.jfree.data.time.TimeSeries;
import org.jfree.ui.ApplicationFrame;

import ruc.edu.components.ChooseOPTimerTask;
import ruc.edu.components.DrawConsumerRunner;
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
	String zookeeper2 = "192.168.0.19:2181,192.168.0.21:2181,192.168.0.22:2181,192.168.0.23:2181"
			+ ",192.168.0.25:2181";
	//String zookeeper1 = "192.168.0.100:2181,192.168.0.91:2181,192.168.0.92:2181,192.168.0.93:2181"
	//		+ ",192.168.0.94:2181";
	String drawTopic = "drawtopic";				// 接收画图的kafka topic
	//String tpchTemptopic = "tpchtemptopics";		// 接收是否改变配置的kafka topic
	String oldAdaTopologyName = "tpchquery";
	String oldEmpiricalTopologyName = "tpchquery";
	
	// 可在界面中动态改变的参数
	String regressionAlg = "J48";
	String classifiAlg = "Multilayer Perceptron";
	public int collecInterval = 3;
	public int maxDRate = 30000;
	public int checkpoints = 2;
	public int maxLatency = 1000;
	public int dataRateLevel = 1;

	private ConsumerConnector consumer1;
	private ConsumerConnector consumer2;
	private ExecutorService executor1;
	private ExecutorService executor2;			// for empicical cluster
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
	public ComponentMetric eSpoutMetric = null;
	public ComponentMetric eOnBoltMetric = null;
	public ComponentMetric eJoinBoltMetric = null;
	public ComponentMetric eKafkaMetric = null;
	public Timer adaStormTimer = null;
	public Timer randomConfTimer = null;
	public Timer sampleCollectTimer = null;
	public Timer drawTimer = null;				// 更新曲线线程
	public Timer eDrawTimer = null;
	
	public int workerNum = 25;
	public int spoutNum = 4;
	public int onBoltNum = 8;
	public int joinBoltNum = 16;
	
	public int windowLength = 30;
	
	public ApplicationFrame parentFrame = null;
	public Map<String, Long> sample = new HashMap<String, Long>();
	
	public AdaptiveStorm( ApplicationFrame parentFrame) {
		this.parentFrame = parentFrame;
		this.adaptiveStorm = this;
		spoutMetric = new ComponentMetric();
		onBoltMetric = new ComponentMetric();
		joinBoltMetric = new ComponentMetric();
		kafkaMetric = new ComponentMetric();
		eSpoutMetric = new ComponentMetric();
		eOnBoltMetric = new ComponentMetric();
		eJoinBoltMetric = new ComponentMetric();
		eKafkaMetric = new ComponentMetric();
		adaStormTimer = new Timer();
		randomConfTimer = new Timer();
		drawTimer = new Timer();
		eDrawTimer = new Timer();
		sampleCollectTimer = new Timer();
		

		sample.put("supervisors", (long)4);
		sample.put("cpucores", (long)40);
		sample.put("memory", (long)160);
		sample.put("onBoltNumber", (long)4);
		sample.put("joinBoltNumber", (long)2);
		sample.put("emitFrenquency", (long)10);
		sample.put("windowLength", (long)30);
		sample.put("emitFrenquency", (long)10);
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
		/*consumer1 = kafka.consumer.Consumer
				.createJavaConsumerConnector(createConsumerConfig(zookeeper1,
						groupId));*/
		consumer2 = kafka.consumer.Consumer
				.createJavaConsumerConnector(createConsumerConfig(zookeeper2,
						groupId));
		// 训练模型
		mlModel = new Mlmodel();
		Process process;
		try {
			/*process = Runtime
					.getRuntime()
					.exec(new String[] {
							"bash",
							"-c",
							"ssh wamdm7 \"source /etc/profile ; cd ~/wengzujian/ ;"
									+ "storm jar " + jarFileName
									+ " storm.starter.TPCHQuery3 tpchquery " + workerNum + " " + spoutNum + " "
									+ onBoltNum + " " + joinBoltNum + " " + windowLength
									+ " 10 false && exit\" "});
			process.waitFor();*/
			// 进入empirical集群并开启storm
			process = Runtime
					.getRuntime()
					.exec(new String[] {
							"bash",
							"-c",
							"ssh 192.168.0.100 \"source /etc/profile ; cd ~/wengzujian/ ;"
									+ "storm jar " + jarFileName
									+ " storm.starter.TPCHQuery3 tpchquery " +workerNum + " " + spoutNum + " "
									+ onBoltNum + " " + joinBoltNum + " " + windowLength
									+ " 10 false && exit\" "});
			process.waitFor();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		// 初始化kafka消费线程
		Map<String, Integer> topicCountMap1 = new HashMap<String, Integer>();
		Map<String, Integer> topicCountMap2 = new HashMap<String, Integer>();
		topicCountMap1.put(drawTopic, new Integer(1));
		topicCountMap2.put(drawTopic, new Integer(1));
		//topicCountMap.put(tpchTemptopic, new Integer(1));
		/*Map<String, List<KafkaStream<byte[], byte[]>>> consumerMap1 = consumer1
				.createMessageStreams(topicCountMap1);*/
		Map<String, List<KafkaStream<byte[], byte[]>>> consumerMap2 = consumer2
				.createMessageStreams(topicCountMap2);
		//List<KafkaStream<byte[], byte[]>> streams1 = consumerMap1.get(drawTopic);
		List<KafkaStream<byte[], byte[]>> streams2 = consumerMap2.get(drawTopic);
		//List<KafkaStream<byte[], byte[]>> streams2 = consumerMap.get(tpchTemptopic);
		
		//collectSamples.startCollect();	// upload storm topology
		// now launch all the threads
		//
		//executor1 = Executors.newFixedThreadPool(1);
		executor2 = Executors.newFixedThreadPool(1);
		//executor2 = Executors.newFixedThreadPool(1);

		// now create an object to consume the messages
		//
		int threadNumber = 0;
		
		/*for (final KafkaStream<byte[], byte[]> stream : streams1) {
			executor1.submit(new DrawConsumerRunner(stream, threadNumber, new ComponentMetric[]{ spoutMetric,
					onBoltMetric, joinBoltMetric, kafkaMetric}, this, false));
			threadNumber++;
		}*/
		// 开启empirical集群的kafka中间数据收集线程
		for (final KafkaStream<byte[], byte[]> stream : streams2) {
			executor2.submit(new DrawConsumerRunner(stream, threadNumber, new ComponentMetric[]{ eSpoutMetric,
					eOnBoltMetric, eJoinBoltMetric, eKafkaMetric}, this, true));
			threadNumber++;
		}
		
		/*for (final KafkaStream<byte[], byte[]> stream : streams2) {
			executor2.submit(new ConsumerTest(stream, threadNumber));
			threadNumber++;
		}*/
		//collectSamples = new CollectThroughputSamples();
		startCollectionSample();
	}

	public void shutdown() {
		if (consumer1 != null)
			consumer1.shutdown();
		if (consumer2 != null)
			consumer2.shutdown();
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
									+ " " + args[2] + " 30 10 false && ./apache-storm-0.9.5/bin/storm "
											+ "kill "+ oldAdaTopologyName + " && exit\" "});
			
			process.waitFor();
			oldAdaTopologyName = newTopologyName;
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
	
	// change empirical's parameters
	public void changeEmpiricalParameters() {

		Process process = null;
		List<String> processList = new ArrayList<String>();
		try {
			String newTopologyName = "tpchquery"+ UUID.randomUUID().toString();
			process = Runtime
					.getRuntime()
					.exec(new String[] {
							"bash",
							"-c",
							"ssh 192.168.0.100 \"source /etc/profile ; cd ~/wengzujian/ ;"
									+ "storm jar " + jarFileName
									+ " storm.starter.TPCHQuery3 "+ newTopologyName + " "
									+ workerNum + " " + spoutNum + " "
									+ onBoltNum + " " + joinBoltNum + " " + windowLength
									+ " 10 false && ./apache-storm-0.9.5/bin/storm "
											+ "kill "+ oldEmpiricalTopologyName + " && exit\" "});
			
			process.waitFor();
			oldEmpiricalTopologyName = newTopologyName;
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
		adaStormTimer.schedule(new ChooseOPTimerTask( new ComponentMetric[]{ spoutMetric,
				onBoltMetric, joinBoltMetric, kafkaMetric}, adaptiveStorm), 18000,
				18000);
	}
	
	public void scheduleSampleCollect() {
		sampleCollectTimer.cancel();
		sampleCollectTimer = new Timer();
		sampleCollectTimer.schedule(new TimerTask() {
			boolean isFirst = true;
			@Override
			public void run() {
				// TODO Auto-generated method stub
				if( !isFirst) {
					long onThroughput = eOnBoltMetric.getAvgThrMinutes();
					long joinThroughput = eJoinBoltMetric.getAvgThrMinutes();
					long spoutThroughput = eSpoutMetric.getAvgThrMinutes();
					int avgCPU = ( eSpoutMetric.getAvgCpuMetricMinutes() + eOnBoltMetric.getAvgCpuMetricMinutes() +
							eJoinBoltMetric.getAvgCpuMetricMinutes() ) / 3;
					int avgMem = (int) (( eSpoutMetric.getAvgMemoryMetricMinutes() + eOnBoltMetric.getAvgMemoryMetricMinutes() +
							eJoinBoltMetric.getAvgMemoryMetricMinutes())) ;
					
					// 将结果写入文件中
					sample.put("workers", (long)workerNum);
					sample.put("onBolt", (long)onBoltNum);
					sample.put("joinBolt", (long)joinBoltNum);
					sample.put("spouts", (long)spoutNum);
					sample.put("spoutRate", spoutThroughput);
					sample.put("onBoltRate", onThroughput);
					sample.put("joinBoltRate", joinThroughput);
					sample.put("cpuUsed", (long) avgCPU);
					sample.put("memoryUsage", (long) avgMem);
					sample.put("latency", Math.round(
							new GetStormUiMetrics( "192.168.0.100").getSpoutLatency()));
					
					// update the ml model according to new sample
					mlModel.updateModel(sample);
				}
				else {
					isFirst = false;
				}
				
				// 清空统计数据
				eKafkaMetric.reSetFiveSeconds(false);
				eSpoutMetric.reSetMinutes();
				eOnBoltMetric.reSetMinutes();
				eJoinBoltMetric.reSetMinutes();
			}
			
		}, 20000, 300000);
	}
	
	public void startCollectionSample() {
		spoutMetric.reSetMinutes();
		onBoltMetric.reSetMinutes();
		joinBoltMetric.reSetMinutes();
		kafkaMetric.reSetMinutes();
		scheduleSampleCollect();
		// 每半小时改变一次配置
		randomConfTimer.schedule(new TimerTask() {

			@Override
			public void run() {
				// TODO Auto-generated method stub
				sampleCollectTimer.cancel();
				// 清空统计数据
				//CollectThroughputSamples.sampleFinished.release();
				// new random configuration
				Random random = new Random();
				workerNum = 4 + random.nextInt(50);
				spoutNum = 4 + random.nextInt(20);
				onBoltNum = 4 + random.nextInt(20);
				joinBoltNum = 4 + random.nextInt(20);
				
				Process process;
				try {
					String newTopologyName = "tpchquery"+ UUID.randomUUID().toString();
					process = Runtime
							.getRuntime()
							.exec(new String[] {
									"bash",
									"-c",
									"ssh wamdm90 \"source /etc/profile ; cd ~/wengzujian/ ;"
											+ "./apache-storm-0.9.5/bin/storm "
													+ "kill "+ oldAdaTopologyName + " && exit\" "});
					
					process.waitFor();
					Thread.sleep(30000);
					
					process = Runtime
							.getRuntime()
							.exec(new String[] {
									"bash",
									"-c",
									"ssh 192.168.0.100 \"source /etc/profile ; cd ~/wengzujian/ ;"
											+ "storm jar " + jarFileName
											+ " storm.starter.TPCHQuery3 " 
											+ newTopologyName + " " + workerNum + " " + spoutNum + " "
											+ onBoltNum + " " + joinBoltNum + " "
											+ " 30 10 false && exit\" "});
					
					process.waitFor();
					oldAdaTopologyName = newTopologyName;

					eKafkaMetric.reSetFiveSeconds(false);
					eSpoutMetric.reSetMinutes();
					eOnBoltMetric.reSetMinutes();
					eJoinBoltMetric.reSetMinutes();
					scheduleSampleCollect();
				} catch (IOException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			
		}, 1800000, 1800000);
	}

}
