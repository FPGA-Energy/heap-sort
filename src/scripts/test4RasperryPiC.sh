#!/bin/bash

# only real time
TIMEFORMAT=%R
LOGFILE="results/runtime.log"
REP1=40
REP2=40
REP3=20

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

			for (( j2=1; j2<=$REP2; j2++ ))
			do	
				for (( i=1; i<=$REP3; i++ ))
				do
					echo "$j1*$j2*$i; " >> $LOGFILE
					benchcmd $file 

				done
			done

		done
		echo "[$(date)]" >> $LOGFILE
		#sleep 20

	done

  }


rm -f $LOGFILE

pwd >> $LOGFILE
echo "[$(date)]" >> $LOGFILE

run_files "build/*K-random_*.heapsort.out"
run_files "build/*K-sorted_*.heapsort.out"
run_files "build/*K-reverse-sorted_*.heapsort.out"


echo "done"
cat $LOGFILE
