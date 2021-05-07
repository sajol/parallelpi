# parallelpi
Parallel implementation of estimating pi using PCJ library.

PCJ is a Java library for parallel computing in PGAS (Partitioned Global Address Space) paradigm.


#Requirements:
Java 11 is required.

#To load java 11 on HPC

$module load Java/11.0.2

#To run it on laptop/desktop

$javac -cp .:pcj-5.1.0.jar ParallelPi.java
$java -cp .:pcj-5.1.0.jar ParallelPi 1000000

This pi calculation will be run using 8 threads on local/desktop. There is a nodes.txt file which contains 8 lines of hostname. Each line will represent one thread.

#To run it on HPC

$sbatch submit.sh

This would run the calculation on 4 nodes. Each node will run the caculation using 4 threads. So overall 4 multiplied by 4 = 16 threads will be used to calculate the pi. Adust the config in submit.sh as per your need.
