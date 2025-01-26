package com.bosch.asc.pipeline

import org.apache.spark.sql.types._

object Schema {
  val lookup_Schema = StructType(Array(
    StructField("LocationID", IntegerType, true),
    StructField("Borough", StringType, true),
    StructField("Zone", StringType, true),
    StructField("service_zone", StringType, true)
  ))
}

