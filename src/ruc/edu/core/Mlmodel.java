package ruc.edu.core;

import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.Semaphore;
import java.util.logging.Logger;

import weka.classifiers.Evaluation;
import weka.classifiers.functions.MultilayerPerceptron;
import weka.classifiers.trees.J48;
import weka.core.FastVector;
import weka.core.Instance;
import weka.core.Instances;
import weka.core.converters.ConverterUtils.DataSource;
import weka.filters.Filter;
import weka.filters.unsupervised.attribute.MathExpression;
import weka.filters.unsupervised.attribute.NumericToNominal;
import weka.filters.unsupervised.attribute.Remove;

public class Mlmodel {
	public static Semaphore mlFinished = new Semaphore(0, true);

	Instances clusterMetricData = null;
	Instances clusterThroughputData = null;
	Instances latencyData = null;
	
	MultilayerPerceptron cpuModel = null;
	MultilayerPerceptron memoryModel = null;
	J48 throughtputModel = null;
	J48 latencyModel = null;
	
	Writer sampleWriter = null;
	FileOutputStream fos = null;
	
	Writer resultWriter = null;
	FileOutputStream resultFos = null;

	public Mlmodel() {
		// train the model
		
		try {
			// append write
			fos = new FileOutputStream(
					"addedSamples.arff", true);
			sampleWriter = new BufferedWriter(
					new OutputStreamWriter(fos, "utf-8"));
			
			resultFos = new FileOutputStream(
					"resultForDraw.arff", true);
			resultWriter = new BufferedWriter(
					new OutputStreamWriter(resultFos, "utf-8"));
			
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		initModels();
		
	}
	
	// retrain the model
	public void updateModel(Map<String, Long> values) {
		// actually not really update the model , just write new sample to training set
		long spoutRate = values.get("spoutRate");
		long onBoltRate = values.get("onBoltRate");
		long joinBoltRate = values.get("joinBoltRate");
		String comma = ",";
		try {
			String data = ""+onBoltRate + comma + joinBoltRate + comma + spoutRate + comma
					+ values.get("supervisors") +comma + values.get("cpucores")+ comma + values.get("memory") 
					+ comma + values.get("workers") + comma + values.get("onBoltNumber") +comma
					+ values.get("joinBoltNumber") + comma + values.get("kafkaBrokers") + comma
					+ values.get("kafkaPartitions") + comma
					+ values.get("onBolt") + comma + values.get("joinBolt") + comma + values.get("spouts")
					+ comma + values.get("windowLength") + comma + values.get("emitFrenquency")
					+ comma + values.get("cpuUsed") + comma
					+ values.get("memoryUsage") + comma + values.get("latency") + "\n";
			sampleWriter.write(data);
			sampleWriter.flush();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	
	public void updateResult(String time, Map<String, Long> values) {
		// actually not really update the model , just write new sample to training set
		long spoutRate = values.get("spoutRate");
		long onBoltRate = values.get("onBoltRate");
		long joinBoltRate = values.get("joinBoltRate");
		String comma = ",";
		try {
			String data = ""+ time.split(" ")[1] + comma + spoutRate
					+ comma + values.get("cpuUsed") + comma
					+ values.get("memoryUsage") + comma + values.get("latency") + "\n";
			resultWriter.write(data);
			resultWriter.flush();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}

	/**
	 * get predicted cpu metric
	 * 
	 * @return
	 */
	public double getPredictedCPUResult(double[] values) {
		// get result according to trained models
		// first construct dataset
		FastVector attributes = new FastVector();
		for (int i = 0; i < clusterMetricData.numAttributes() - 2; i++) {
			attributes.addElement(clusterMetricData.attribute(i));
		}
		Instances datasetToPredicted = new Instances("Test-dataset",
				attributes, 0);
		// double[] values = new double[clusterMetricData.numAttributes()];
		// values[0] = 243286;
		// values[1] = 107466;
		// values[2] = 244914;
		// values[3] = 4;
		// values[4] = 96;
		// values[5] = 192;
		// values[6] = 43;
		// values[7] = 4;
		// values[8] = 2;
		// values[9] = 6;
		// values[10] = 13;
		// values[11] = 10;
		// values[12] = 30;
		// values[13] = 5;
		// values[14] = 70;
		datasetToPredicted.add(new Instance(1.0, values));
		datasetToPredicted.setClassIndex(14);
		double result = 0;
		try {
			result = cpuModel.classifyInstance(datasetToPredicted
					.firstInstance());
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return result;
	}

	/**
	 * get predicted cpu metric
	 * 
	 * @return
	 */
	public double getPredictedThroughputResult(double[] values) {
		// get result according to trained models
		// first construct dataset
		FastVector attributes = new FastVector();
		for (int i = 0; i < clusterThroughputData.numAttributes(); i++) {
			attributes.addElement(clusterThroughputData.attribute(i));
		}
		Instances datasetToPredicted = new Instances("Test-dataset",
				attributes, 0);
		// double[] values = new double[clusterMetricData.numAttributes()];
		// values[0] = 243286;
		// values[1] = 107466;
		// values[2] = 244914;
		// values[3] = 4;
		// values[4] = 96;
		// values[5] = 192;
		// values[6] = 43;
		// values[7] = 4;
		// values[8] = 2;
		// values[9] = 6;
		// values[10] = 13;
		// values[11] = 10;
		// values[12] = 30;
		// values[13] = 5;
		// values[14] = 70;
		datasetToPredicted.add(new Instance(1.0, values));
		datasetToPredicted.setClassIndex(11);
		double result = 0;
		try {
			result = throughtputModel.classifyInstance(datasetToPredicted
					.firstInstance());
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return result;
	}

	/**
	 * get predicted cpu metric
	 * 
	 * @return
	 */
	public double getPredictedMemoryResult(double[] values) {
		// get result according to trained models
		// first construct dataset
		FastVector attributes = new FastVector();
		for (int i = 0; i < clusterMetricData.numAttributes() - 1; i++) {
			attributes.addElement(clusterMetricData.attribute(i));
		}
		Instances datasetToPredicted = new Instances("Test-dataset",
				attributes, 0);
		datasetToPredicted
				.deleteAttributeAt(datasetToPredicted.numAttributes() - 2);
		// double[] values = new double[clusterMetricData.numAttributes()];
		// values[0] = 243286;
		// values[1] = 107466;
		// values[2] = 244914;
		// values[3] = 4;
		// values[4] = 96;
		// values[5] = 192;
		// values[6] = 43;
		// values[7] = 4;
		// values[8] = 2;
		// values[9] = 6;
		// values[10] = 13;
		// values[11] = 10;
		// values[12] = 30;
		// values[13] = 5;
		// values[14] = 70;
		datasetToPredicted.add(new Instance(1.0, values));
		datasetToPredicted.setClassIndex(14);
		double result = 0;
		try {
			result = memoryModel.classifyInstance(datasetToPredicted
					.firstInstance());
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return result;
	}

	public void initModels() {

		new MLModelInit().start();
	}

	public class MLModelInit extends Thread {

		@Override
		public void run() {
			// TODO Auto-generated method stub
			super.run();
			try {
				// load data
				clusterMetricData = DataSource
						.read("Weka Files/v2.0/clusterMetricARFF.arff");
				clusterThroughputData = DataSource
						.read("Weka Files/clusterMetricARFF_withOnBolt.arff");
				latencyData = DataSource.read("Weka Files/clusterMetricARFF_withLatency.arff");
				
				// train the latency model
				latencyData.setClassIndex(latencyData.numAttributes() - 1);
				latencyModel = new J48();
				latencyModel.setMinNumObj(3);
				latencyModel.buildClassifier(latencyData);
				// evaluation the thorughtput model
//				Evaluation latencyEval = new Evaluation(latencyData);
//				latencyEval.crossValidateModel(latencyModel,
//						latencyData, 10, new Random(1));
				//System.out.println(latencyEval.toSummaryString("\nResults\n\n", false));
				
				int metricNumAttributes = clusterMetricData.numAttributes();
				int throughputAttributes = clusterThroughputData
						.numAttributes();
				Remove cpuRm = new Remove();
				Remove memoryRm = new Remove();
				// remove unnecessary columns
				cpuRm.setAttributeIndicesArray(new int[] {
						metricNumAttributes - 1, metricNumAttributes - 2 });
				memoryRm.setAttributeIndicesArray(new int[] {
						metricNumAttributes - 1, metricNumAttributes - 3 });

				// train cpu model
				clusterMetricData.setClassIndex(metricNumAttributes - 3);
				cpuRm.setInputFormat(clusterMetricData); // called after filter
															// options

				Instances cpuSampleDatas = Filter.useFilter(clusterMetricData,
						cpuRm);
				cpuModel = new MultilayerPerceptron();
				cpuModel.setLearningRate(0.1); // set learning rate
				cpuModel.buildClassifier(cpuSampleDatas);

				// evaluation the cpu model
//				Evaluation eval = new Evaluation(cpuSampleDatas);
//				eval.crossValidateModel(cpuModel, cpuSampleDatas, 10,
//						new Random(1));

				//Log.info(eval.toSummaryString("\nResults\n\n", false));

				// train memory model
				clusterMetricData.setClassIndex(metricNumAttributes - 2);
				memoryRm.setInputFormat(clusterMetricData); // called after
															// filter options

				Instances memorySampleDatas = Filter.useFilter(
						clusterMetricData, memoryRm);
				memoryModel = new MultilayerPerceptron();
				memoryModel.setLearningRate(0.1); // set learning rate
				memoryModel.buildClassifier(memorySampleDatas);

				// evaluation the memory model
//				Evaluation memoryEval = new Evaluation(memorySampleDatas);
//				memoryEval.crossValidateModel(memoryModel, memorySampleDatas,
//						10, new Random(1));
				// logger.info(memoryEval.toSummaryString("\nResults\n\n",
				// false))

				System.out.println("finish model training");
				mlFinished.release();
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

		}

	}

	// get optimal parameters for storm
	public int[] getOptimalParameters(int[] dataRates) {
		int spoutRate = dataRates[0];
		int onBoltRate = dataRates[1];
		int joinBoltRate = dataRates[2];
		
		//System.out.println("get optimal: spout " + dataRates[0] + " onBolt " 
		//+ dataRates[1] + " joinBolt " + dataRates[2]);
		int resultWorkerCount = 0;
		int resultSpoutCount = 0;
		int resultOnBoltCount = 0;
		int resultJoinBoltCount = 0;
		int lowestCPUAndMemory = Integer.MAX_VALUE;
		double maxThroughput = 0;
		int[] maxThroughputParm = new int[]{28,10,16,4};
		Instances tempThroughputData = null;
		
		try {
			// train the thorughput model according to onBoltRate
			clusterThroughputData.setClassIndex(3);
			MathExpression expression = new MathExpression();
			String[] options = 
					weka.core.Utils.splitOptions
					("-E \"ifelse(A>"+ onBoltRate +",1,0)\" -V -R 1 ");
			expression.setOptions(options);
			expression.setInputFormat(clusterThroughputData);
			tempThroughputData = Filter.useFilter(clusterThroughputData, expression);
			NumericToNominal number2Nomi = new NumericToNominal();
			number2Nomi.setAttributeIndices("first");
			number2Nomi.setInputFormat(tempThroughputData);
			tempThroughputData = Filter.useFilter(tempThroughputData, number2Nomi);
			
			tempThroughputData.setClassIndex(0);
			throughtputModel = new J48();
			throughtputModel.buildClassifier(tempThroughputData);
			Evaluation eval = new Evaluation(tempThroughputData);
			eval.crossValidateModel(throughtputModel, tempThroughputData, 10, new Random(1));
			//System.out.println(eval.toSummaryString("\n model Results\n", false));
			
		} catch (Exception e2) {
			// TODO Auto-generated catch block
			e2.printStackTrace();
		}
		
		try {

			FastVector attributes = new FastVector();
			for (int i = 0; i < clusterMetricData.numAttributes() - 1; i++) {
				attributes.addElement(clusterMetricData.attribute(i));
			}
			Instances cpuInstances = new Instances("cpu-dataset", attributes, 0);
			cpuInstances.deleteAttributeAt(cpuInstances.numAttributes() - 2);

			attributes.removeAllElements();
			for (int i = 0; i < clusterMetricData.numAttributes() - 1; i++) {
				attributes.addElement(clusterMetricData.attribute(i));
			}
			Instances memoryInstances = new Instances("memory-dataset",
					attributes, 0);
			memoryInstances
					.deleteAttributeAt(memoryInstances.numAttributes() - 2);

			attributes.removeAllElements();
			for (int i = 0; i < latencyData.numAttributes(); i++) {
				attributes.addElement(latencyData.attribute(i));
			}
			Instances latencyInstances = new Instances("latency-dataset",
					attributes, 0);
			
			FastVector throughputAttr = new FastVector();
			for (int i = 0; i < tempThroughputData.numAttributes(); i++) {
				throughputAttr.addElement(tempThroughputData.attribute(i));
			}
			Instances throughputInstances = new Instances("throughput-dataset",
					throughputAttr, 0);

			// for cpu and memory predictor
			double[] values = new double[clusterMetricData.numAttributes()];
			values[0] = onBoltRate;
			values[1] = joinBoltRate;
			values[2] = spoutRate;
			values[3] = 4;
			values[4] = 96;
			values[5] = 192;
			values[6] = 43;
			values[7] = 4;
			values[8] = 2;
			values[9] = 6;
			values[10] = 13;
			values[11] = 10;
			values[12] = 60;
			values[13] = 10;
			values[14] = 70;
			
			double[] latencyTempValues = new double[latencyData.numAttributes()];
			latencyTempValues[4] = 60;
			latencyTempValues[5] = 10;
			
			double[] throughputTempValues = new double[clusterThroughputData.numAttributes()];
			throughputTempValues[5] = 60;
			throughputTempValues[6] = 10;

			Writer fw = null;
			try {
				FileOutputStream fos = new FileOutputStream("tempResult");
				fw = new BufferedWriter(new OutputStreamWriter(fos, "utf-8"));
			} catch (FileNotFoundException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			} catch (UnsupportedEncodingException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			// 4-for
			for (int i = 4; i <= 96; i += 1) {
				// workers
				for (int j = 4; j <= 32; j += 1) {
					// onBolt
					for (int k = 4; k <= 32; k += 1) {
						// joinBolt
						for (int z = 1; z <= 32; z += 1) {
							// spout
							double[] latencyValues = latencyTempValues.clone();
							latencyValues[0] = i;
							latencyValues[1] = j;
							latencyValues[2] = k;
							latencyValues[3] = z;
							
							double[] cpuValues = values.clone();
							cpuValues[6] = i;
							cpuValues[9] = j; // onBolt
							cpuValues[10] = k; // joinBolt
							cpuValues[11] = z; // spout

							double[] memoryValues = values.clone();
							memoryValues[6] = i;
							memoryValues[9] = j; // onBolt
							memoryValues[10] = k; // joinBolt
							memoryValues[11] = z; // spout

							double[] throughputValues = throughputTempValues
									.clone();
							throughputValues[1] = i;
							throughputValues[2] = j; // onBolt
							throughputValues[3] = k; // joinBolt
							throughputValues[4] = z; // spout

							try {
								latencyInstances.delete();
								latencyInstances.add(new Instance(1.0, latencyValues));
								latencyInstances.
									setClassIndex(latencyInstances.numAttributes() -1);
								
								cpuInstances.delete();
								cpuInstances.add(new Instance(1.0, cpuValues));
								cpuInstances.setClassIndex(14);

								memoryInstances.delete();
								memoryInstances.add(new Instance(1.0,
										memoryValues));
								memoryInstances.setClassIndex(14);

								throughputInstances.delete();
								throughputInstances.add(new Instance(1.0,
										throughputValues));
								throughputInstances.setClassIndex(0);
								
								double latencyResult[] = latencyModel.distributionForInstance(
										latencyInstances.firstInstance());
								
								if( latencyResult[0] > 0.95) {
									// latency less than 5000 ms
									double throughputResult[] = throughtputModel
											.distributionForInstance(throughputInstances
													.firstInstance());
									double cpuResult = cpuModel
											.classifyInstance(cpuInstances
													.firstInstance());
									double memoryResult = memoryModel
											.classifyInstance(memoryInstances
													.firstInstance());
									/*fw.write("cpu: " + cpuResult + " memory:"
											+ memoryResult + " throughput:"
											+ throughputResult[1] + " workers: " + i
											+ " onBolt: " + j + " joinBolt: " + k
											+ " spout: " + z + " latency:" + latencyResult[0] + "\n");*/
									
									if( throughputResult[1] > 0.90 && cpuResult 
											+ (memoryResult / 1000 / 160) < lowestCPUAndMemory) {
										// find better parameters
										lowestCPUAndMemory = 
												(int) (cpuResult + (memoryResult / 1000 / 160));
										resultWorkerCount = i;
										resultOnBoltCount = j;
										resultJoinBoltCount = k;
										resultSpoutCount = z;

										fw.write("cpu: " + cpuResult
												+ " memory:" + memoryResult
												+ " throughput:"
												+ throughputResult
												+ " workers: " + i
												+ " onBolt: " + j
												+ " joinBolt: " + k
												+ " spout: " + z + "\n");
									}
									
								}
							} catch (Exception e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
						}
					}
				}
			}

			fw.flush();
			fw.close();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		if( resultWorkerCount == 0) {
			// return maximum throughput parameters
			return maxThroughputParm;
		}

		return new int[] { resultWorkerCount, resultOnBoltCount,
				resultJoinBoltCount, resultSpoutCount };

	}

}
