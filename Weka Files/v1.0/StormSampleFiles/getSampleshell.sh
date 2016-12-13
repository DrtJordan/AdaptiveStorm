#!/bin/bash
if [ $# -ne 2 ]
then
	echo Usage: 2 parameters
	exit 2
else
	workersFrom=$1
	workersEnd=$2
fi

for (( workers=$workersFrom ; workers <=$workersEnd ; workers += 2 ))
do
	for (( spout=4; spout <= $workers * 4; spout *= 2 ))
	do
		echo "begin spout loop: spout = $spout"
		ssh -t wamdm12 "echo "rdidke" | sudo -S rm -r /tmp/kafka-logs/tpch* ; cd ~/wengzujian/kafka_2.10-0.8.2.1 ; sudo ./bin/kafka-server-stop.sh ; sudo nohup ./bin/kafka-server-start.sh config/server.properties & sleep 10 && ./bin/kafka-topics.sh --zookeeper 192.168.0.73:2181,192.168.0.21:2181,192.168.0.22:2181 --alter --topic tpch_2 --partitions $spout && cd .. && java -jar tpchproducer.jar && exit" &&
		for (( bolt=4; bolt <= $workers * 4; bolt *= 2 ))
		do
			echo "begin bolt loop: bolt=$bolt"
			cd ~/Desktop/StormSampleFiles
			dictionName=2_8_3.8_$workers\_$spout\_$bolt
			if [ -e $dictionName ]
			then
				rm -r "$dictionName"	
			fi
                        mkdir "$dictionName"
			date >> log.txt
			echo $dictionName >> log.txt
                        cd "$dictionName" && mkdir wamdm13 && mkdir wamdm14
                        ssh wamdm11 "cd ~/wengzujian/ ; ./apache-storm-0.9.5/bin/storm jar StormTest-0.0.1-SNAPSHOT-jar-with-dependencies.jar storm.starter.TPCHQuery1 tpchquery $workers $spout $bolt && exit" &&
			sleep 1h 
                        ssh wamdm11 "cd ~/wengzujian/ ; ./apache-storm-0.9.5/bin/storm kill tpchquery && exit" &&
			sleep 1m
                        ssh wamdm13 "cd ~/wengzujian && scp -r stormResult/ 192.168.0.75:/home/wamdm/Desktop/StormSampleFiles/$dictionName/wamdm13/ && cd stormResult && rm * && exit" &&
                        ssh wamdm14 "cd ~/wengzujian && scp -r stormResult/ 192.168.0.75:/home/wamdm/Desktop/StormSampleFiles/$dictionName/wamdm14/ && cd stormResult && rm * && exit"
		done			
	done
done
