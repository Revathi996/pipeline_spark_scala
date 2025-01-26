package com.bosch.asc.pipeline

import java.util.Properties


/**
 * holds connection parameters loaded from conf
 */
case class PipelineProperties(
  jdbcUrl: String,
  postgresUser: String,
  postgresPassword: String,
  minioEndpoint: String,
  minioAccessKey: String,
  minioSecretKey: String,
  taxitrippath: String,
  taxilkupzonepath: String,
  errorrecordspath: String,
  taxitripfactcol: String,
  dqcheckcols : String,
  locationdimcols : String


                             ) {

  // construct java properties
  var connectionProperties: Properties = new java.util.Properties()
  connectionProperties.setProperty("user", this.postgresUser)
  connectionProperties.setProperty("password", postgresPassword)






}