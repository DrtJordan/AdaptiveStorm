package ruc.edu.tools;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Properties;
import java.util.Timer;

import kafka.javaapi.producer.Producer;
import kafka.producer.KeyedMessage;
import kafka.producer.Partitioner;
import kafka.producer.ProducerConfig;
import kafka.utils.VerifiableProperties;

public class AllTPCHProducer {

	public void startProducing() {
		
		// Build the configuration required for connecting to Kafka
		Properties props = new Properties();

		// List of Kafka brokers. Complete list of brokers is not
		// required as the producer will auto discover the rest of
		// the brokers. Change this to suit your deployment.
		props.put("metadata.broker.list", "192.168.0.19:9092,192.168.0.21:9092,"
				+ "192.168.0.22:9092,192.168.0.23:9092");
		//props.put("partitioner.class", "storm.starter.kafka.SimplePartitioner");
		// Serializer used for sending data to kafka. Since we are sending
		// string,
		// we are using StringEncoder.
		props.put("topic.metadata.refresh.interval.ms", "2000");
		props.put("serializer.class", "kafka.serializer.StringEncoder");
		props.put("producer.type", "async");	// async means ignore the result of sending function
		/*props.put("queue.buffering.max.messages", "200000");
		props.put("queue.buffering.max.ms", "1000");*/
		Timer calThroughput = new Timer();
		// Create the producer instance
		ProducerConfig config = new ProducerConfig(props);
		Producer<String, String> producer = new Producer<String, String>(config);

		// Now we break each word from the paragraph
//		FileReader lineitemFr = null;
//		FileReader orderFr = null;
//		FileReader customerFr = null;
//		try {
//			lineitemFr = new FileReader(
//					"/home/wamdm/wengzujian/tpch_2_17_0/dbgen/lineitem");
//			orderFr = new FileReader(
//					"/home/wamdm/wengzujian/tpch_2_17_0/dbgen/orders");
//			customerFr = new FileReader(
//					"/home/wamdm/wengzujian/tpch_2_17_0/dbgen/customer");
//		} catch (FileNotFoundException e1) {
//			// TODO Auto-generated catch block
//			e1.printStackTrace();
//		}
		
		TopicProducerThread lineitemThread = 
				new TopicProducerThread(producer, "lineitem.tbl", "lineitem");
		TopicProducerThread ordersThread = 
				new TopicProducerThread(producer, "orders.tbl", "order");
		TopicProducerThread customersThread = 
				new TopicProducerThread(producer, "customer.tbl", "customer");
		
		lineitemThread.start();
		ordersThread.start();
		customersThread.start();
		
	}
	
	public static class TopicProducerThread extends Thread {

		String topicName = null;
		String tableName = null;
		Producer<String, String> producer = null;
		
		public TopicProducerThread( Producer<String, String> producer, 
				String tableName, String topicName ) {
			this.producer = producer;
			this.topicName = topicName;
			this.tableName = tableName;
		}
		
		@Override
		public void run() {
			// TODO Auto-generated method stub
			super.run();
			FileReader fr = null;
			try {
				fr = new FileReader(
						"/home/wamdm/wengzujian/tpch_2_17_0/dbgen/" + tableName);
			} catch (FileNotFoundException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
			BufferedReader br = new BufferedReader(fr);
			String StringData = null;
			long before = System.currentTimeMillis();
			//List<KeyedMessage<String,String>> dataList = new ArrayList<KeyedMessage<String,String>>();
			//int messageBufferSize = 0;
			try {
				while( (StringData = br.readLine()) != null) {
					// Create message to be sent to "words_topic" topic with the word
					KeyedMessage<String, String> data = new KeyedMessage<String, String>(
							topicName,StringData);
					/*dataList.add(data);
					if( messageBufferSize < 1000) {
						messageBufferSize ++;
					}
					else {
						// Send the message
						producer.send(dataList);
						dataList.clear();
						messageBufferSize = 0;
					}*/
					producer.send(data);
				}
				br.close();
				fr.close();
				producer.close();
				
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			//producer.send(dataList);
			//dataList.clear();
			long after = System.currentTimeMillis();
			
			System.out.println("Produced data time: " +String.valueOf( (after-before) /1000));
			//return;
			// First paragraph from Franz Kafka's Metamorphosis
			
		}
		
	}
	 
}
