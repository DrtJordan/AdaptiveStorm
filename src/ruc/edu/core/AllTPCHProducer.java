package ruc.edu.core;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Properties;
import java.util.Timer;
import java.util.TimerTask;
import java.util.UUID;
import java.util.concurrent.Semaphore;

import org.jfree.data.time.Millisecond;
import org.jfree.data.time.RegularTimePeriod;

import kafka.javaapi.producer.Producer;
import kafka.producer.KeyedMessage;
import kafka.producer.Partitioner;
import kafka.producer.ProducerConfig;
import kafka.utils.Utils;
import kafka.utils.VerifiableProperties;

public class AllTPCHProducer {

	static Semaphore available = new Semaphore(0, true); // control loop
	static Semaphore controlPriority = new Semaphore(0, true);
	static long spoutNum = 0; // save throughput
	static long avgThroughout = 0;
	public static long calThroughtInterval = 5000; // calculate throughput every
													// this millseconds
	static int throughtNum = 0;
	static int sampleNumber = 1;
	static long fixedAvgThroughout = 0;
	static long spoutInterval = 400000;
	static int sleepTime = 200;					// 每当controlSpeedNum 超过 spoutInterval时 睡sleepTime秒
	static int sampleTotal = 10;
	static String intermediateTopic = "drawtopics";
	static int controlSpeedNum = 0;				// 用于控制发送速率
	
	static int[] sharkChange = {0,1,2,3,4,5,6,7,8,9};
	static String customerTopicName = "customer";
	static String lineitemTopicName = "lineitem";
	static String orderTopicName = "order";
	
	public AdaptiveStorm adaptiveStorm = null;
	public Timer calThroughput = null;
	
	public AllTPCHProducer(AdaptiveStorm adaptiveStorm) {
		this.adaptiveStorm = adaptiveStorm;
	}

	public void startProducing() {

		calThroughput = new Timer();
		//Timer stopCollectAndChangeRate = new Timer();
		//Timer sendThroughput = new Timer();
		// Build the configuration required for connecting to Kafka

		Properties props1 = new Properties();
		Properties props2 = new Properties();

		// List of Kafka brokers. Complete list of brokers is not
		// required as the producer will auto discover the rest of
		// the brokers. Change this to suit your deployment.
		props1.put("metadata.broker.list",
				"192.168.0.19:9092,192.168.0.21:9092,192.168.0.22:9092,"
				+ "192.168.0.23:9092,192.168.0.25:9092");
		props2.put("metadata.broker.list",
				"192.168.0.73:9092,192.168.0.74:9092,192.168.0.75:9092,"
				+ "192.168.0.76:9092,192.168.0.77:9092");
		// props.put("partitioner.class",
		// "storm.starter.kafka.SimplePartitioner");
		// Serializer used for sending data to kafka. Since we are sending
		// string,
		// we are using StringEncoder.
		props1.put("topic.metadata.refresh.interval.ms", "2000");
		props2.put("topic.metadata.refresh.interval.ms", "2000");
		props1.put("serializer.class", "kafka.serializer.StringEncoder");
		props2.put("serializer.class", "kafka.serializer.StringEncoder");
		props1.put("producer.type", "async"); // async means ignore the result of
												// sending function
		props2.put("producer.type", "async");
		// props.put("queue.buffering.max.messages", "200000");
		// props.put("batch.num.messages", "10000");
		// props.put("send.buffer.byte", "550000");

		// Create the producer instance
		ProducerConfig config1 = new ProducerConfig(props1);
		ProducerConfig config2 = new ProducerConfig(props2);
		Producer<String, String> lineitemProducer1 = new Producer<String, String>(
				config1);
		Producer<String, String> orderProducer1 = new Producer<String, String>(
				config1);
		Producer<String, String> customerProducer1 = new Producer<String, String>(
				config1);
		
		Producer<String, String> lineitemProducer2 = new Producer<String, String>(
				config2);
		Producer<String, String> orderProducer2 = new Producer<String, String>(
				config2);
		Producer<String, String> customerProducer2 = new Producer<String, String>(
				config2);
		//final Producer<String, String> kafkaThroughputProducer = new Producer<String, String>(
		//		config);

		TopicProducerThread lineitemThread = new TopicProducerThread(
				lineitemProducer1,lineitemProducer2, "lineitem.tbl", lineitemTopicName);
		TopicProducerThread ordersThread = new TopicProducerThread(
				orderProducer1, orderProducer2, "orders.tbl", orderTopicName);
		TopicProducerThread customersThread = new TopicProducerThread(
				customerProducer1, customerProducer2, "customer.tbl", customerTopicName);

		// 开始发送数据
		lineitemThread.start();
		ordersThread.start();
		customersThread.start();

		/**
		 * 用于计算2秒内的平均吞吐量
		 */
		calThroughput.schedule(new TimerTask() {

			@Override
			public void run() {
				// TODO Auto-generated method stub
				// LOG .info("latency : " + hm.get( "default"));

				if (avgThroughout == 0) {
					avgThroughout = (int) (spoutNum / (calThroughtInterval / 1000));
					throughtNum = 1;
					// 舍弃第一条数据
					resetThroughput();
					return ;
				} else {
					avgThroughout = (int) ((avgThroughout * throughtNum + spoutNum
							/ (calThroughtInterval / 1000)) / (throughtNum + 1));
					throughtNum++;
				}
				
				adaptiveStorm.kafkaMetric.throughputSum.addAndGet(avgThroughout);
				adaptiveStorm.kafkaMetric.metricNumber.incrementAndGet();
				adaptiveStorm.logs[1].append("Kafka throughput (tuples/s): " + avgThroughout + "\n");
				adaptiveStorm.logs[3].append("Kafka throughput (tuples/s): " + avgThroughout + "\n");
				
				RegularTimePeriod time = new Millisecond();
				synchronized( adaptiveStorm.plots) {
					adaptiveStorm.plots[0].dataRateSeries.add(time, avgThroughout/ 10000);
					adaptiveStorm.plots[1].dataRateSeries.add(time , avgThroughout / 10000);
					adaptiveStorm.plots[2].dataRateSeries.add(time , avgThroughout / 10000);
					adaptiveStorm.plots[3].dataRateSeries.add(time, avgThroughout / 10000);
				}
				
				//KeyedMessage<String, String> data = new KeyedMessage<String, String>(
				//		intermediateTopic, "kafkaProducer," + avgThroughout);
				// send intermediate data to kafka topic
				//kafkaThroughputProducer.send(data);
				//System.out.println("avg throughput: " + avgThroughout);
				resetThroughput();
			}

		}, calThroughtInterval, calThroughtInterval);

		/*while (true) {
//			if( after.getTimeInMillis() - before.getTimeInMillis() > 7200000) {
//				System.exit(0);
//			}
			try {
				// wait for loop
				available.acquire();

				switch (available.availablePermits()) {
				case 0:
					customersThread = new TopicProducerThread(customerProducer,
							"customer.tbl", customerTopicName);
					customersThread.start();
					break;
				case 1:
					available.acquire(1);
					ordersThread = new TopicProducerThread(orderProducer,
							"orders.tbl", orderTopicName);
					ordersThread.start();
					break;
				case 2:
					available.acquire(2);
					lineitemThread = new TopicProducerThread(customerProducer,
							"lineitem.tbl", lineitemTopicName);
					lineitemThread.start();
					break;
				}
				//after = Calendar.getInstance();
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}*/

	}

	public static class TopicProducerThread extends Thread {

		String topicName = null;
		String tableName = null;
		Producer<String, String> producer = null;
		Producer<String, String> eProducer = null;

		public TopicProducerThread(Producer<String, String> producer, Producer<String, String> eProducer,
				String tableName, String topicName) {
			this.eProducer = eProducer;
			this.producer = producer;
			this.topicName = topicName;
			this.tableName = tableName;
		}

		@Override
		public void run() {
			// TODO Auto-generated method stub
			while( true ) {
				// 持续不断得发送数据
				super.run();
				FileReader fr = null;
				try {
					fr = new FileReader("/data/tpch_2_17_0/dbgen/"
							+ tableName);
				} catch (FileNotFoundException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
				BufferedReader br = new BufferedReader(fr);
				String StringData = null;
				long before = System.currentTimeMillis();
				// List<KeyedMessage<String,String>> dataList = new
				// ArrayList<KeyedMessage<String,String>>();
				// int messageBufferSize = 0;
				try {
					while ((StringData = br.readLine()) != null) {
						// Create message to be sent to "words_topic" topic with the
						// word
						KeyedMessage<String, String> data = new KeyedMessage<String, String>(
								topicName, StringData);
						/*
						 * dataList.add(data); if( messageBufferSize < 1000) {
						 * messageBufferSize ++; } else { // Send the message
						 * producer.send(dataList); dataList.clear();
						 * messageBufferSize = 0; }
						 */
						producer.send(data);
						eProducer.send(data);
						addThroughput();
					}
					br.close();
					fr.close();
					// producer.close();
					if (topicName.equals(lineitemTopicName)) {
						available.release(3);
					} else if (topicName.equals(orderTopicName)) {
						available.release(2);
					} else if (topicName.equals(customerTopicName)) {
						available.release(1);
					}

				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				// producer.send(dataList);
				// dataList.clear();
				long after = System.currentTimeMillis();

				System.out.println("Produced data time: "
						+ String.valueOf((after - before) / 1000));
				// return;
				// First paragraph from Franz Kafka's Metamorphosis
			}
		}

	}
	
	/**
	 * 改变数据发送速率
	 * @param level
	 */
	public void changeRateLevel( int level) {
		
		if (sampleNumber == 1) {
			fixedAvgThroughout = 300000;
		}
		
		if( level == 10) {
			System.out.println("change to highest spout rate");
			spoutInterval = 0;
			sleepTime = 0;
		}else {
			if( level == 5) {
				spoutInterval = level == 1? 1000 : fixedAvgThroughout / sampleTotal * sharkChange[level -1];
				sleepTime = 600 * (1 - sharkChange[level -1] / sampleTotal);
			}
			else if( level <= 4) {
				spoutInterval = level == 1? 1000 : fixedAvgThroughout / sampleTotal * sharkChange[level -1];
				sleepTime = 1000 * (1 - sharkChange[level -1] / sampleTotal);
			}
			else {
				spoutInterval = level == 1? 1000 : fixedAvgThroughout * 2 / sampleTotal * sharkChange[level -1];
				sleepTime = 600 * (1 - sharkChange[level -1] / sampleTotal);
			}
		}
		// 将2秒内的统计数据清零
		avgThroughout = throughtNum = 0;
		sampleNumber++;
		// kafka的统计数据清空
		adaptiveStorm.kafkaMetric.reSetFiveSeconds(false);
		controlSpeedNum = 0;
	}

	public synchronized static void addThroughput() {
		spoutNum++;
		if (controlSpeedNum++ > spoutInterval) {
			try {
				Thread.sleep(sleepTime);
				controlSpeedNum = 0;
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	/**
	 * 改变刷新频率
	 * @param refreshTime
	 */
	public void changeRefreshInterval( int refreshTime) {
		calThroughput.cancel();
		calThroughput = new Timer();
		calThroughtInterval = refreshTime;
		/**
		 * 用于计算2秒内的平均吞吐量
		 */
		calThroughput.schedule(new TimerTask() {

			@Override
			public void run() {
				// TODO Auto-generated method stub
				// LOG .info("latency : " + hm.get( "default"));

				if (avgThroughout == 0) {
					avgThroughout = (int) (spoutNum / (calThroughtInterval / 1000));
					throughtNum = 1;
					// 舍弃第一条数据
					resetThroughput();
					return ;
				} else {
					avgThroughout = (int) ((avgThroughout * throughtNum + spoutNum
							/ (calThroughtInterval / 1000)) / (throughtNum + 1));
					throughtNum++;
				}
				
				adaptiveStorm.kafkaMetric.throughputSum.addAndGet(avgThroughout);
				adaptiveStorm.kafkaMetric.metricNumber.incrementAndGet();
				adaptiveStorm.logs[1].append("Kafka throughput (tuples/s): " + avgThroughout + "\n");
				adaptiveStorm.logs[3].append("Kafka throughput (tuples/s): " + avgThroughout + "\n");
				
				RegularTimePeriod time = new Millisecond();
				synchronized( adaptiveStorm.plots) {
					adaptiveStorm.plots[0].dataRateSeries.add(time, avgThroughout/ 10000);
					adaptiveStorm.plots[1].dataRateSeries.add(time , avgThroughout / 10000);
					adaptiveStorm.plots[2].dataRateSeries.add(time , avgThroughout / 10000);
					adaptiveStorm.plots[3].dataRateSeries.add(time, avgThroughout / 10000);
				}
				
				//KeyedMessage<String, String> data = new KeyedMessage<String, String>(
				//		intermediateTopic, "kafkaProducer," + avgThroughout);
				// send intermediate data to kafka topic
				//kafkaThroughputProducer.send(data);
				//System.out.println("avg throughput: " + avgThroughout);
				resetThroughput();
			}

		}, refreshTime, refreshTime);
	}
	
	public synchronized static void resetThroughput() {
		spoutNum = 0;
	}

}
