package com.bosch.asc.pipeline

// FileReader.scala
import org.apache.spark.sql.{DataFrame, SparkSession}
import org.apache.spark.sql.types.StructType
import pureconfig.ConfigSource
import pureconfig.generic.auto._

object FileReader {

  val conf: PipelineProperties = ConfigSource
    .default
    .loadOrThrow[PipelineProperties]

  def readCsv(spark: SparkSession, path: String, schema: StructType, hasHeader: Boolean = true): DataFrame = {
    spark.read
      .option("header", hasHeader.toString)
      .schema(schema)
      .csv(path)
  }

  def readJson(spark: SparkSession, path: String, schema: StructType): DataFrame = {
    spark.read
      .schema(schema)
      .json(path)
  }

  def readParquet(spark: SparkSession, path: String): DataFrame = {
    spark.read.parquet(path)
  }

  def writeParquet(df: DataFrame, path: String, mode: String = "overwrite"): Unit = {
    df.write
      .mode(mode)
      .parquet(path)
  }


  // Method to write DataFrame to Parquet
  def writeDB(df: DataFrame,table_name: String): Unit = {
    df.write
      .mode("append")
      .jdbc(
        url = conf.jdbcUrl,
        table = table_name,
        connectionProperties = conf.connectionProperties
      )
    }
  }
