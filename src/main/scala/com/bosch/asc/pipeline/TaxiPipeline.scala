package com.bosch.asc.pipeline

import org.apache.spark.internal.Logging
import org.apache.spark.sql.SparkSession
import org.apache.spark.sql.functions.{col, uuid}
import pureconfig.ConfigSource
import pureconfig.generic.auto._

/**
 * object containing main method for this application.
 * contains sample starter code
 *
 * TODO: replace this with your implementation
 */
object TaxiPipeline extends Logging {

  /**
   * entry point for the pipeline
   *
   * @param args command line arguments
   */
  def main(args: Array[String]): Unit = {

    // conf is loaded from resources/application.conf
    val conf: PipelineProperties = ConfigSource
      .default
      .loadOrThrow[PipelineProperties]

    // init spark session
    val spark = SparkSession.builder()
      .appName("taxi-data-pipeline")
      .config("spark.hadoop.fs.s3a.path.style.access", "true")
      .config("spark.hadoop.fs.s3a.impl", "org.apache.hadoop.fs.s3a.S3AFileSystem")
      .config("fs.s3a.endpoint", conf.minioEndpoint)
      .config("fs.s3a.access.key", conf.minioAccessKey)
      .config("fs.s3a.secret.key", conf.minioSecretKey)
      .master("local[*]")
      .getOrCreate()

    // TODO: this is just an example
    val exampleCols: Seq[String] = Seq(
      "trip_id",
      "VendorID",
      "tpep_pickup_datetime",
      "tpep_dropoff_datetime",
      "passenger_count",
      "fare_amount"
    )

    val df = spark
      .read
      .parquet("s3a://taxi-bucket/taxi-data/")
      .withColumn("trip_id", uuid().cast("string"))
      .select(exampleCols.map(col): _*)
      .limit(100)

    // write the DataFrame to PostgreSQL
    df.write
      .mode("append")
      .jdbc(
        url = conf.jdbcUrl,
        table = "test_taxi_table",
        connectionProperties = conf.connectionProperties
      )

    // Stop SparkSession
    spark.stop()
  }
}