package com.bosch.asc.pipeline

import org.apache.spark.sql.{DataFrame, SparkSession}
import org.apache.spark.sql.functions._

object DataQualityChecks {

  // Check for missing values in specified columns
  def checkMissingValues(df: DataFrame, columns: Seq[String]): DataFrame = {
    df.select(columns.map(c => count(when(col(c).isNull, c)).alias(c)): _*)
      .filter(row => row.anyNull)
  }

  // Check for duplicate rows
  def checkDuplicates(df: DataFrame): DataFrame = {
    df.groupBy(df.columns.map(col): _*)
      .count()
      .filter("count > 1")
  }

  // Check for invalid ranges in a column (e.g., negative age values)
  def checkInvalidRanges(df: DataFrame, column: String, min: Int, max: Int): DataFrame = {
    df.filter(col(column) < min || col(column) > max)
  }

  // Check if a column contains invalid data types (for example, age should be an integer)
  def checkInvalidDataTypes(df: DataFrame, column: String, expectedType: String): DataFrame = {
    expectedType match {
      case "int" => df.filter(!col(column).cast("int").isNotNull)
      case "string" => df.filter(!col(column).cast("string").isNotNull)
      case "double" => df.filter(!col(column).cast("double").isNotNull)
      case _ => df // Handle other cases as needed
    }
  }

  // Check for data consistency (e.g., age should be greater than 0)
  def checkDataConsistency(df: DataFrame, column: String, condition: String): DataFrame = {
    df.filter(condition)
  }

  // Method to run all DQ checks and show results
  def runAllChecks(df: DataFrame, columns: Seq[String]):  (DataFrame,DataFrame) = {
    println("Checking for missing values...")
    val missingValues = checkMissingValues(df, columns)

    println("Checking for duplicates...")
    val duplicates = checkDuplicates(df).select(columns.map(col): _*)

    println("Checking for invalid data types in 'age' column...")
    //val invalidDataTypes = checkInvalidDataTypes(df, "VendorID", "int")

//    val corruptRecords = missingValues
//      .union(duplicates.select(columns.map(col): _*))
//      .distinct()

    val validRecords = df.except(duplicates)


    (validRecords, duplicates)

    // Add more checks as needed
  }
}
