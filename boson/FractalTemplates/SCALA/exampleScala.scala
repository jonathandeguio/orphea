# This is a tempalate for Scala


import org.apache.spark.sql.SparkSession
val spark:SparkSession = SparkSession.builder()
      .master("local[1]")
      .appName("Bosler")
      .getOrCreate()   
