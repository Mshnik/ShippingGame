#!/bin/bash
if [ "$#" -ne 2 ] || [ ! -d "Submissions" ]; then
	echo "Usage instructions:" 1>&2;
	echo "Place this script in the root directory of your eclipse project" 1>&2;
	echo "and use your name as the first argument and your netid as the second" 1>&2;
	echo "argument" 1>&2;
	echo "" 1>&2;
	echo "Place all submissions in a folder named Submissions" 1>&2;
	echo "Do NOT rename any folders.  This script expects all folders to be named" 1>&2;
	echo "as a normal CMS download would name it" 1>&2;
	exit 1;
fi
echo "Now starting automatic testing..."
if [ -e "scoreFile" ]; then
	rm scoreFile;
fi
cd Submissions;
if [ -e "noCompiles.txt" ]; then
	rm noCompiles.txt;
fi
echo "NetID,Grade," > grades.csv;
for D in *; do
	if [ -d "${D}" ]; then
		cd ${D};
		for Z in *.zip; do
			if [ -e "${Z}" ]; then
				unzip -o "${Z}";
				if [ -e "__MACOSX" ]; then
					rm -rf __MACOSX;
				fi
				for dir in *; do
					if [ -d "${dir}" ]; then
						mv ./"${dir}"/* .
					fi
				done
			fi
		done
		if [ -e "MyManager.java" ]; then
			echo "TESTING ${D}...";
			mkdir ../../src/student/TMP;
			cp *.java ../../src/student/TMP/;
			cd ../../src/student/TMP;
			for f in *.java; do
				iconv -f utf-8 -t utf-8 -c $f > ../$f;
			done;
			cd ..;
			rm -rf TMP;
			cd ../..;
			make > /dev/null 2> .tmpErr;
			if [ $? -ne 0 ]; then
				echo "${D}'s submission DID NOT COMPILE" 1>&2;
				echo "Writing error message to noCompiles.txt" 1>&2;
				cd Submissions;
				echo "The compiler produced the following error compiling ${D}'s Submission:" >> noCompiles.txt;
				echo "BEGIN COMPILER ERROR" >> noCompiles.txt;
				cat ../.tmpErr >> noCompiles.txt;
				echo "END COMPILER ERROR" >> noCompiles.txt;
				echo "" >> noCompiles.txt;
				cd ..;
			else
				java -classpath bin solution.Grader $1 $2 ${D} >> Submissions/grades.csv;
			fi
			rm .tmpErr;
			make clean > /dev/null;
			rm src/student/*.java;
			cd Submissions/${D};
			echo "FINISHED ${D}";
		fi
		cd ..;
	fi
done
echo "Finished Automated Testing!";
echo "Producing grades file";
cp */*.txt .;
cp ../reprocess.py .;
python reprocess.py 0;
rm reprocess.py;
rm *feedback.txt
cd ..;
rm scoreFile;
