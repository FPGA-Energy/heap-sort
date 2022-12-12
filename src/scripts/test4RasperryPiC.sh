#!/bin/bash

# only real time
TIMEFORMAT=%R
LOGFILE="results/runtime.log"
REP1=400

  benchcmd() {

		echo "$@;"  >> $LOGFILE
		T="$(date +%s%N)"
		eval "$@"
		T="$(($(date +%s%N)-T))"
		T=$((T/1000000))
		echo "${T};::" >> $LOGFILE
		#sleep 1

  }

  run_files() {

	for file in $1
	do 
		echo $file 
		echo "[$(date)]" >> $LOGFILE
	
		for  (( j1=1; j1<=$REP1; j1++ ))
		do	
			echo "$j1; "
					echo "$j1; " >> $LOGFILE
					benchcmd $file 
		done

		echo "[$(date)]" >> $LOGFILE
		sleep 5

	done

  }


rm -f $LOGFILE

pwd >> $LOGFILE
echo "[$(date)]" >> $LOGFILE

#run_files "build/*K-random_*.heapsort.out" "results/runtime_random.log"
run_files "build/*K-sorted_*.heapsort.out"
#run_files "build/*K-reverse-sorted_*.heapsort.out" "results/runtime_revSorted.log"


echo "done"
cat $LOGFILE
