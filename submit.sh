#!/bin/bash
#SBATCH --job-name=parallel_job_pi    # Job name
#SBATCH -N 4                          # Number of nodes
#SBATCH --ntasks-per-node 4           # Numer of tasks per node
#SBATCH --mem=1gb                     # Required RAM
#SBATCH --time=00:10:00               # Required time
#SBATCH --output=parallel_pi_%j.log   # Standard output and error log


srun hostname > nodes.txt
srun -N 4 -n 4 -c 4 java -cp .:pcj-5.1.0.jar ParallelPi 10000000
