	#!/bin/bash

	# only real time
	TIMEFORMAT=%R
	LOGFILE="results/runtime.log"
	REP1=40
	REP2=40
	REP3=20

	benchcmd() {

#			echo "$@;"  >> $LOGFILE
			T="$(date +%s%N)"
			eval "$@"
			T="$(($(date +%s%N)-T))"
			T=$((T/1000000))
			echo "$@;${T};" >> $LOGFILE
			sleep 5

	}

	run_files() {
		echo "[$(date)]" >> $LOGFILE		

		for file in $1
		do 
			echo $file 
			for j1 in 1 2 3 4 5 6 7 8 9 10
			do	
				echo "$j1; "

				for j2 in 1 2 3 4 5 6 7 8 9 10
				do	
					for i in 1 2 3 4
					do
						#echo "$j1*$j2*$i; " >> $LOGFILE
						benchcmd $file 

					done
				done

			done
			#echo "[$(date)]" >> $LOGFILE
			sleep 10

		done

	}


	rm -f $LOGFILE

	pwd >> $LOGFILE
	echo "[$(date)]" >> $LOGFILE

	#run_files "build/*K-random_*.heapsort.out"
	run_files "build/executables*K-sorted_*.heapsort.out"
	#run_files "build/*K-reverse-sorted_*.heapsort.out"


	echo "done"
	cat $LOGFILE
