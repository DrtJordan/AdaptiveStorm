package ruc.edu.tools;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.Semaphore;

public class CollectThroughputSamples {

	public static Semaphore sampleFinished = new Semaphore(0, true);
	public static int samplePeriod = 50000;
	public static int workersFrom = 4;
	public static int workersTo = 96;
	public static int workers = 0;
	public static int spouts = 0;
	public static int onBolt = 0;
	public static int joinBolt = 0;
	public static int windowLength = 0;
	public static int trunk = 0;
	public static int emitFrenquency = 0;
	
	public void startCollect() {
		
		Timer collectSamples = new Timer();
		collectSamples.schedule(new TimerTask() {

			@Override
			public void run() {
				// TODO Auto-generated method stub
				Random rand = new Random();
				workers = rand.nextInt((workersTo - workersFrom) + 1) + workersFrom;
				spouts = rand.nextInt(32) + 1;
				onBolt = rand.nextInt(32) + 4;
				joinBolt = rand.nextInt(32) + 4;
				windowLength = rand.nextInt((120 - 30) + 1) + 30;
				trunk = rand.nextInt((10 - 3) + 1) + 3;
				emitFrenquency = windowLength / trunk;
				
				System.out.println("workers = " + workers
						+ " spout " + spouts +" onbolt = " + onBolt
						+ " joinbolt = " + joinBolt
						+ " windowlength = " + windowLength + " trunk = " + trunk);
				
				try {
					
					Process process = Runtime
							.getRuntime()
							.exec(new String[] {
									"bash",
									"-c",
									"ssh wamdm7 \"source /etc/profile ; cd ~/wengzujian/ ;"
											+ "storm jar StormTest-0.0.1-SNAPSHOT-jar-with-dependencies.jar "
											+ "storm.starter.TPCHQuery3 tpchquery "
											+ workers + " " + spouts + " " + onBolt
											+ " " + joinBolt + " " + emitFrenquency * trunk + " " + 
											emitFrenquency + " true && exit\" "});
					process.waitFor();
					
					SampleCollector.isError = false;
					SampleCollector.spoutMetric.reSet();
					SampleCollector.onBoltMetric.reSet();
					SampleCollector.joinBoltMetric.reSet();
					SampleCollector.otherMetrics.clear();
					/*List<String> processList = new ArrayList<String>();
					BufferedReader input = new BufferedReader(new InputStreamReader(
							process.getInputStream()));
					String line = "";
					while ((line = input.readLine()) != null) {
						processList.add(line);
					}
					input.close();
					for (String templine : processList) {
						System.out.println(templine);
					}*/
					
					sampleFinished.acquire();
					
					process = Runtime.getRuntime()
							.exec(new String[] {
									"bash",
									"-c",
									"ssh wamdm7 \"source /etc/profile ; cd ~/wengzujian/ ;"
									+ " ./apache-storm-0.9.5/bin/storm kill tpchquery && exit\" "
							});
					process.waitFor();
					
					/*processList.clear();
					input = new BufferedReader(new InputStreamReader(
							process.getInputStream()));
					line = "";
					while ((line = input.readLine()) != null) {
						processList.add(line);
					}
					input.close();
					for (String templine : processList) {
						System.out.println(templine);
					}*/
					
					Thread.sleep(30*1000);
					
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				
			}
			
		}, 0, 1000);
	}
	
}
