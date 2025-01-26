package com.bosch.asc.pipeline

import org.apache.spark.sql.{DataFrame, SparkSession}
import org.apache.spark.sql.types._
import org.apache.spark.sql.functions._
import pureconfig.ConfigSource
import pureconfig.generic.auto._

//import DataQualityChecker.performDQChecks

object MainApp {
  def main(args: Array[String]): Unit = {
    val spark = SparkSession.builder()
      .appName("DataQualityApp")
      .master("local[*]")
      .getOrCreate()
    val conf: PipelineProperties = ConfigSource
      .default
      .loadOrThrow[PipelineProperties]
    val trip_filePath = conf.taxitrippath
    val lookup_filePath = conf.taxilkupzonepath

    val lookup_schema = Schema.lookup_Schema
    val lookup_trip_df: DataFrame = FileReader.readCsv(spark, lookup_filePath, lookup_schema)
    val taxi_trip_df: DataFrame = FileReader.readParquet(spark, trip_filePath)
    val columnsToCheck = conf.dqcheckcols.split(",").map(_.trim).toList
    val (validRecords, corruptRecords) = DataQualityChecks.runAllChecks(taxi_trip_df, columnsToCheck)
    val corrupt_filePath = conf.errorrecordspath
    FileReader.writeParquet(corruptRecords,corrupt_filePath)
    val Taxi_Trip_Fact_cols = conf.taxitripfactcol.split(",").map(_.trim).toList
    val Location_Dim_cols =  conf.locationdimcols.split(",").map(_.trim).toList
    val Taxi_Trip_Fact = validRecords.select(Taxi_Trip_Fact_cols.head, Taxi_Trip_Fact_cols.tail: _*).limit(55)
    val vendor_data_dim = validRecords.select("VendorId").distinct().withColumn("VendorName", lit("NONE")).limit(55)
    val Location_Dim = lookup_trip_df.select(Location_Dim_cols.head, Location_Dim_cols.tail: _*).limit(55)

    FileReader.writeDB(Taxi_Trip_Fact,"taxi_trip_fact")
    FileReader.writeDB(vendor_data_dim,"vendor_dim")
    FileReader.writeDB(Location_Dim,"location_dim")


    spark.stop()
  }
}
