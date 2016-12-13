package ruc.edu.window;

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

import kafka.consumer.ConsumerConfig;
import kafka.consumer.ConsumerIterator;
import kafka.consumer.KafkaStream;
import kafka.javaapi.consumer.ConsumerConnector;

public class AdaptiveStorm {

	String groupId = UUID.randomUUID().toString();
	String zookeeper = "192.168.0.19:2181,192.168.0.21:2181,192.168.0.22:2181,192.168.0.23:2181"
			+ ",192.168.0.24:2181";
	String topic = "tpchtemptopics";
	String oldTopologyName = "tpchquery";

	private ConsumerConnector consumer;
	private ExecutorService executor;
	private Mlmodel mlModel = null;
	private GetStormUiMetrics stormUiMetrics = null;
	//private CollectThroughputSamples collectSamples = null;
	
	int[] oldParameters = new int[4];

	public AdaptiveStorm() {
		consumer = kafka.consumer.Consumer
				.createJavaConsumerConnector(createConsumerConfig(zookeeper,
						groupId));
		mlModel = new Mlmodel();
		Process process;
		try {
			process = Runtime
					.getRuntime()
					.exec(new String[] {
							"bash",
							"-c",
							"ssh wamdm7 \"source /etc/profile ; cd ~/wengzujian/ ;"
									+ "storm jar StormTest-0.0.1-SNAPSHOT-jar-with-dependencies.jar "
									+ "storm.starter.TPCHQuery3 tpchquery"
									+ " 50 10 30 28 60 10 false && exit\" "});
			process.waitFor();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		//collectSamples = new CollectThroughputSamples();
	}

	public void shutdown() {
		if (consumer != null)
			consumer.shutdown();
		if (executor != null)
			executor.shutdown();
		try {
			if (!executor.awaitTermination(5000, TimeUnit.MILLISECONDS)) {
				System.out
						.println("Timed out waiting for consumer threads to shut down, exiting uncleanly");
			}
		} catch (InterruptedException e) {
			System.out
					.println("Interrupted during shutdown, exiting uncleanly");
		}
	}

	public void run(int a_numThreads) {
		Map<String, Integer> topicCountMap = new HashMap<String, Integer>();
		topicCountMap.put(topic, new Integer(a_numThreads));
		Map<String, List<KafkaStream<byte[], byte[]>>> consumerMap = consumer
				.createMessageStreams(topicCountMap);
		List<KafkaStream<byte[], byte[]>> streams = consumerMap.get(topic);
		
		//collectSamples.startCollect();	// upload storm topology
		// now launch all the threads
		//
		executor = Executors.newFixedThreadPool(a_numThreads);

		// now create an object to consume the messages
		//
		int threadNumber = 0;
		for (final KafkaStream stream : streams) {
			executor.submit(new ConsumerTest(stream, threadNumber));
			threadNumber++;
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

	public static void main(String[] args) {

		AdaptiveStorm example = new AdaptiveStorm();
		example.run(1);

		try {
			while (true) { 
				Thread.sleep(100000000); 
			}
		} catch (InterruptedException ie) {

		}
		example.shutdown();
	}

	// consumer thread
	public class ConsumerTest implements Runnable {
		private KafkaStream m_stream;
		private int m_threadNumber;
		private SimpleDateFormat df = null;
		private Date thisTime = null;
		private double oldSpoutRate = 0;
		private double kafkaProducerThroughput = 0;
		private Map<String, Long> otherMetrics = null;
		
		ComponentMetric spoutMetric = null;
		ComponentMetric onBoltMetric = null;
		ComponentMetric joinBoltMetric = null;
		boolean isError = false;
		Timer timer = new Timer();

		public ConsumerTest(KafkaStream a_stream, int a_threadNumber) {
			m_threadNumber = a_threadNumber;
			m_stream = a_stream;
			df = new SimpleDateFormat("dd HH:mm");
			spoutMetric = new ComponentMetric();
			onBoltMetric = new ComponentMetric();
			joinBoltMetric = new ComponentMetric();
			otherMetrics = new HashMap<String,Long>();
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

					if (datas[0].equals("kafkaProducer")) {
						// update kafka throughput

						kafkaProducerThroughput = Integer.valueOf(datas[1]);
						System.out.println("kafka throughput: "
								+ kafkaProducerThroughput);
						continue;
					}

					/*if (thisTime == null) {
						// set time
						thisTime = df.parse(datas[2]);
					}*/
/*
					if (Math.abs(df.parse(datas[2]).getTime()
							- thisTime.getTime()) < 120000) {*/
					if (datas[0].equals("spoutRate")) {
						//if (spoutMetric.getTaskCount() == 0) {
							spoutMetric.taskCount = Integer.valueOf(datas[1]);

						//}
						spoutMetric.cpuMetricList.add((int) (Double.valueOf( datas[5]) * 100));
						spoutMetric.memoryMetricList.add((int) (Long.valueOf(datas[6]) / 1000000));
						spoutMetric.metricList.add(Integer.valueOf(datas[3]));
						if (spoutMetric.metricList.size() > spoutMetric.taskCount) {
							isError = true;
							timer.schedule(new TimerTask(){

								@Override
								public void run() {
									// TODO Auto-generated method stub
									isError = false;
								}
								
							}, 10000);
							System.out.println("spout error happen! " + data);
						}
						if (spoutMetric.metricList.size() == spoutMetric.taskCount) {
							// need to calculate sum\
							spoutMetric.metricSum = 0;
							System.out.println(" spoutRateNumber :" +
										spoutMetric.metricList.size() / 3);
							for (Integer temp : spoutMetric.metricList) {
								
								spoutMetric.metricSum += temp;
							}
							spoutMetric.getAvgCpuMetric();
							spoutMetric.getAvgMemoryMetric();
							/*System.out.println(" spoutRateSum :" +
									spoutMetric.metricSum);*/
							
						}
					} else if (datas[0].equals("onBolt")) {
						
						if( otherMetrics.isEmpty()) {
							// need to extract other metrics
							otherMetrics.put("supervisors", Long.valueOf( datas[6] ));
							otherMetrics.put("cpucores", Long.valueOf( datas[7] ));
							otherMetrics.put("memory", Long.valueOf( datas[8] ));
							otherMetrics.put("workers", Long.valueOf( datas[9] ));
							otherMetrics.put("onBoltNumber", Long.valueOf( datas[10] ));
							otherMetrics.put("joinBoltNumber", Long.valueOf( datas[11] ));
							otherMetrics.put("kafkaBrokers", Long.valueOf( datas[12] ));
							otherMetrics.put("kafkaPartitions", Long.valueOf( datas[13] ));
							otherMetrics.put("onBolt", Long.valueOf( datas[14] ));
							otherMetrics.put("joinBolt", Long.valueOf( datas[15] ));
							otherMetrics.put("spouts", Long.valueOf( datas[16] ));
							otherMetrics.put("windowLength", Long.valueOf( datas[17] ));
							otherMetrics.put("emitFrenquency", Long.valueOf( datas[18] ));
							
						}
						
						onBoltMetric.taskCount = Integer.valueOf(datas[1]);
						onBoltMetric.cpuMetricList.add((int) (Double.valueOf( datas[4]) * 100));
						onBoltMetric.memoryMetricList.add((int) (Long.valueOf(datas[5]) / 1000000));
						onBoltMetric.metricList.add(Integer.valueOf(datas[3]));
						if (onBoltMetric.metricList.size() > onBoltMetric.taskCount) {
							isError = true;
							timer.schedule(new TimerTask(){

								@Override
								public void run() {
									// TODO Auto-generated method stub
									isError = false;
								}
								
							}, 10000);
							System.out.println("onBolt error happen! " + data);
						}
						if (onBoltMetric.metricList.size() == onBoltMetric.taskCount) {
							// need to calculate sum
							onBoltMetric.metricSum = 0;
							System.out.println(" onBoltNumber :" +
										onBoltMetric.metricList.size() / 4);
							for (Integer temp : onBoltMetric.metricList) {
								
								onBoltMetric.metricSum += temp;
							}
							onBoltMetric.getAvgCpuMetric();
							onBoltMetric.getAvgMemoryMetric();
							 /*System.out.println(" onBoltSum :" +
									 onBoltMetric.metricSum);*/
						}
						
					} else if (datas[0].equals("joinBolt")) {
						//if (joinBoltMetric.taskCount == 0) {
							joinBoltMetric.taskCount = Integer.valueOf(datas[1]);
							// System.out.println(" joinBoltCount :" +
							// joinBoltTaskCount);
						//}
						joinBoltMetric.cpuMetricList.add((int) (Double.valueOf( datas[4]) * 100));
						joinBoltMetric.memoryMetricList.add((int) (Long.valueOf(datas[5]) / 1000000));
						joinBoltMetric.metricList.add(Integer.valueOf(datas[3]));
						if (joinBoltMetric.metricList.size() > joinBoltMetric.taskCount) {
							isError = true;
							timer.schedule(new TimerTask(){

								@Override
								public void run() {
									// TODO Auto-generated method stub
									isError = false;
								}
								
							}, 10000);
							System.out.println("joinBolt error happen! " + data);
						}
						if (joinBoltMetric.metricList.size() == joinBoltMetric.taskCount) {
							// need to calculate sum
							joinBoltMetric.metricSum = 0;
							System.out.println(" joinBoltNumber :" +
									joinBoltMetric.metricList.size() / 2);
							for (Integer temp : joinBoltMetric.metricList) {
								
								joinBoltMetric.metricSum += temp;
							}
							joinBoltMetric.getAvgCpuMetric();
							joinBoltMetric.getAvgMemoryMetric();
							 //System.out.println(" joinBoltSum :" + joinBoltMetric.metricSum);
						}
					}

					if (spoutMetric.metricSum != 0 && onBoltMetric.metricSum != 0
							&& joinBoltMetric.metricSum != 0) {
						// need to update model\
						stormUiMetrics = new GetStormUiMetrics();
						
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
						//mlModel.updateModel(otherMetrics);
						//CollectThroughputSamples.sampleFinished.release();
						
						mlModel.updateResult(datas[2], otherMetrics);
						System.out.println("time: " + datas[2] + 
								"\n spoutRate : " + spoutMetric.metricSum
								+ "\n onBoltRate :" + onBoltMetric.metricSum
								+ "\n joinBoltSum : " + joinBoltMetric.metricSum 
								+ "\n latency: " + otherMetrics.get("latency"));
						
						// choose better parameters
						/*if (oldSpoutRate == 0 || Math.abs(oldSpoutRate
								- spoutMetric.metricSum) / oldSpoutRate > 0.2) {
							// need change storm parameters
							double multiNumber = kafkaProducerThroughput / spoutMetric.metricSum ;
							int[] result = mlModel
									.getOptimalParameters(new int[] {
											(int) (spoutMetric.metricSum * multiNumber), 
											(int) (onBoltMetric.metricSum * multiNumber),
											(int) (joinBoltMetric.metricSum * multiNumber)});
							System.out
									.println("optimal parameters changed because spoutRate:"
											+ result[0] + " " + result[1]
											+ " " + result[2] + " "
											+ result[3]);
							oldSpoutRate = spoutMetric.metricSum;
							
							// change storm parameters according to results
							changeStormParameters(result);

						}
						else if( Math.abs(kafkaProducerThroughput
								- spoutMetric.metricSum) > 30000) {
							// change parameter according to producer
							
							double multiNumber = kafkaProducerThroughput / spoutMetric.metricSum ;
							int[] result = mlModel
									.getOptimalParameters(new int[] {
											(int) (spoutMetric.metricSum * multiNumber),
											(int) (onBoltMetric.metricSum * multiNumber),
											(int) (joinBoltMetric.metricSum * multiNumber) });
							System.out
									.println("optimal parameters changed because producer:"
											+ result[0] + " " + result[1]
											+ " " + result[2] + " "
											+ result[3]);
							oldSpoutRate = spoutMetric.metricSum;
							
							// change storm parameters according to results
							changeStormParameters(result);
						}*/
						
						thisTime = null;
						// reset all metric
						spoutMetric.reSet();
						onBoltMetric.reSet();
						joinBoltMetric.reSet();
						otherMetrics.clear();
						
					}

				}
			} catch (Exception e) {
				e.printStackTrace();
			}

		}
	}

	// change storm's parameters
	public void changeStormParameters(int[] args) {
		
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
									+ "storm jar StormTest-0.0.1-SNAPSHOT-jar-with-dependencies.jar "
									+ "storm.starter.TPCHQuery3 "
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

}
