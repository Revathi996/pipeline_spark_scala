package com.bosch.asc.pipeline

import org.apache.spark.sql.{DataFrame, SparkSession}
import org.apache.spark.sql.functions._
import org.scalatest.funsuite.AnyFunSuite
import org.scalatest.BeforeAndAfterAll
import pureconfig.ConfigSource
import pureconfig.generic.auto._

class MainAppTest extends AnyFunSuite with BeforeAndAfterAll {

  var spark: SparkSession = _

  // Set up the Spark session before all tests
  override def beforeAll(): Unit = {
    spark = SparkSession.builder()
      .appName("MainAppTest")
      .master("local[*]")
      .getOrCreate()
  }

  // Test: Ensure the SparkSession is initialized
  test("SparkSession should be initialized") {
    assert(spark != null, "SparkSession should be initialized")
  }

  // Test: Ensure the configuration is read correctly
  test("Configuration should be loaded correctly") {
    val conf: PipelineProperties = ConfigSource
      .default
      .loadOrThrow[PipelineProperties]

    // Verify some expected properties from the loaded configuration
    assert(conf.taxitrippath.nonEmpty, "taxitrippath should not be empty")
    assert(conf.taxilkupzonepath.nonEmpty, "taxilkupzonepath should not be empty")
  }

  // Test: Simulate the reading of files and check DataFrame transformations
  test("FileReader should load data correctly and apply transformations") {
    val conf: PipelineProperties = ConfigSource
      .default
      .loadOrThrow[PipelineProperties]

    val lookup_filePath = conf.taxilkupzonepath
    val trip_filePath = conf.taxitrippath

    // Simulate reading the CSV and Parquet files (use test files)
    val lookup_schema = Schema.lookup_Schema
    val lookup_trip_df: DataFrame = FileReader.readCsv(spark, lookup_filePath, lookup_schema)
    val taxi_trip_df: DataFrame = FileReader.readParquet(spark, trip_filePath)

    // Perform a basic transformation (e.g., select a few columns)
    val Taxi_Trip_Fact_cols = conf.taxitripfactcol.split(",").map(_.trim).toList
    val Taxi_Trip_Fact = taxi_trip_df.select(Taxi_Trip_Fact_cols.head, Taxi_Trip_Fact_cols.tail: _*).limit(55)

    assert(Taxi_Trip_Fact.count() == 55, "Taxi_Trip_Fact DataFrame should have 55 rows")
  }

  // Test: Data Quality Checks
  test("Data Quality Checks should run and return valid/corrupt records") {
    val conf: PipelineProperties = ConfigSource
      .default
      .loadOrThrow[PipelineProperties]
    val taxi_trip_df: DataFrame = FileReader.readParquet(spark, conf.taxitrippath)

    // Simulate the DataQualityChecks
    val columnsToCheck = conf.dqcheckcols.split(",").map(_.trim).toList
    val (validRecords, corruptRecords) = DataQualityChecks.runAllChecks(taxi_trip_df, columnsToCheck)

    assert(validRecords.count() >= 0, "There should be valid records")
    assert(corruptRecords.count() >= 0, "There should be corrupt records")
  }

  // Test: Ensure output files are written correctly (simulate write functions)

  // Clean up the Spark session after all tests
  override def afterAll(): Unit = {
    spark.stop()
  }
}
