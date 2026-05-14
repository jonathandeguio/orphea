!/bin/bash
# List triggers:  gcloud beta builds triggers list

for i in boson funnel parler julia frontend callisto spark-history-server superset
do 
   echo "Importing trigger $i"
   gcloud beta builds triggers import --source="${i}.yaml"
done

