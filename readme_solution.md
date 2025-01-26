# NYC Taxi Traffic Regulation Data Analysis

## **Introduction**
This project analyzes NYC Yellow Taxi trip data to provide insights that help regulate traffic, optimize infrastructure, and improve transportation efficiency. Using a dimensional data model, we transform raw trip data into actionable insights to assist city planners, policymakers, and traffic regulators.

#### scripts
This folder has the create table statements necessary for creating fact and dimension tables, before executing MainApp.Scala make sure these tables are created in postgres DB


#### MainApp.scala --> Main script
serves as the entry point for your data processing pipeline. It coordinates the different steps involved in reading, 
processing, checking data quality, and writing data to various outputs such as files and databases.
After processing the fact and dimension tables are loaded into postgres sql

##### PipelineProperties.scala - 
Contains configuration data (file paths and column names).

##### FileReader.scala - 
Handles reading and writing data (CSV, Parquet, and DB).

##### DataQualityChecks.scala - 
Performs data quality checks on DataFrames.

##### Schema.scala -
Contains schemas for reading files.


#### MainAppTest --> Unit Test Script
The unit test script (MainTest.scala) primarily validates the data quality checks and verifies the expected outcome when running these checks on mock data. It ensures that the application logic is correct,

## **Dimensional Data Model**

### **Fact Table: Taxi_Trip_Fact**

| Column Name         | Data Type   | Description                                   |
| :------------------ | :---------- | :------------------------------------------- |
| vendor_id           | INT         | Unique vendor ID                             |
| pickup_datetime     | DATETIME    | Date and time of trip start                  |
| dropoff_datetime    | DATETIME    | Date and time of trip end                    |
| passenger_count     | INT         | Number of passengers in the trip             |
| trip_distance       | FLOAT       | Distance traveled during the trip (miles)    |
| pu_location_id      | INT         | Pickup zone location ID                      |
| do_location_id      | INT         | Drop-off zone location ID                    |
| payment_type        | INT         | Type of payment used                         |
| fare_amount         | FLOAT       | Base fare for the trip                       |
| tip_amount          | FLOAT       | Tips given during the trip                   |
| tolls_amount        | FLOAT       | Toll costs incurred during the trip          |
| congestion_surcharge| FLOAT       | Surcharge applied due to congestion          |
| total_amount        | FLOAT       | Total cost of the trip                       |

### **Dimension Tables**

#### **Time_Dim**

| Column Name    | Data Type | Description                |
| :------------- | :-------- | :------------------------- |
| datetime_key   | INT       | Unique identifier for a timestamp |
| hour           | INT       | Hour of the day (0-23)     |
| day            | INT       | Day of the month           |
| week           | INT       | Week of the year           |
| month          | INT       | Month of the year          |
| year           | INT       | Year                       |

#### **Location_Dim**

| Column Name    | Data Type | Description                |
| :------------- | :-------- | :------------------------- |
| location_id    | INT       | Unique identifier for a zone |
| borough        | STRING    | Borough where the location is situated |
| zone           | STRING    | Name of the zone           |
| service_zone   | STRING    | Type of zone (e.g., Yellow Zone, Boro Zone, EWR) |

#### **Vendor_Dim**

| Column Name    | Data Type | Description                |
| :------------- | :-------- | :------------------------- |
| vendor_id      | INT       | Unique identifier for the vendor |
| vendor_name    | STRING    | Vendor name (if known)     |

---

## **Analytical Questions and Insights**

### **1. Which areas have the most pickups and drop-offs?**
- **Columns Used**: `pu_location_id`, `do_location_id` (linked to `Location_Dim`).
- **How It Helps**:
    - Identify high-traffic zones prone to congestion.
    - Implement dedicated taxi stands or lanes to reduce roadblocks.
    - Prioritize traffic lights and road expansions in key areas.
- **SQL Query**:
  ```sql
  SELECT 
      loc.borough, 
      loc.zone, 
      COUNT(t.pu_location_id) AS pickup_count, 
      COUNT(t.do_location_id) AS dropoff_count
  FROM 
      taxi_trip_fact t
  JOIN 
      location_dim loc ON t.pu_location_id = loc.location_id
  GROUP BY 
      loc.borough, loc.zone
  ORDER BY 
      pickup_count DESC, dropoff_count DESC;
  ```

### **2. What time of day has the most taxi rides?**
- **Columns Used**: `pickup_datetime` (linked to `Time_Dim`).
- **How It Helps**:
    - Identify peak hours to manage traffic effectively.
    - Implement congestion pricing during rush hours.
    - Optimize public transport schedules to alleviate road congestion.
- **SQL Query**:
  ```sql
  SELECT 
      td.hour, 
      COUNT(*) AS ride_count
  FROM 
      taxi_trip_fact t
  JOIN 
      time_dim td ON t.pickup_datetime = td.datetime_key
  GROUP BY 
      td.hour
  ORDER BY 
      ride_count DESC;
  ```


### **3. What is the busiest location for airport trips?**
- **Columns Used**: `pu_location_id`, `do_location_id`, `airport_fee`.
- **How It Helps**:
    - Manage taxi lanes and traffic routing at airports.
    - Plan shuttle or ride-sharing options to reduce individual taxi usage.
- **SQL Query**:
  ```sql
  SELECT 
      loc.borough, 
      loc.zone, 
      COUNT(*) AS airport_trips
  FROM 
      taxi_trip_fact t
  JOIN 
      location_dim loc ON t.pu_location_id = loc.location_id OR t.do_location_id = loc.location_id
  WHERE 
      t.airport_fee > 0
  GROUP BY 
      loc.borough, loc.zone
  ORDER BY 
      airport_trips DESC;
  ```

### **4. How does traffic vary by the number of passengers?**
- **Columns Used**: `passenger_count`, `pu_location_id`, `do_location_id`.
- **How It Helps**:
    - Encourage ride-sharing to reduce the number of vehicles on the road.
    - Set policies that incentivize multiple passengers per trip.
- **SQL Query**:
  ```sql
  SELECT 
      t.passenger_count, 
      COUNT(*) AS trip_count, 
      AVG(t.trip_distance) AS avg_distance
  FROM 
      taxi_trip_fact t
  GROUP BY 
      t.passenger_count
  ORDER BY 
      trip_count DESC;
  ```

---

## **Summary**
This analysis empowers NYC traffic regulators by:
- Identifying congestion hotspots.
- Optimizing taxi traffic patterns with time- and location-based insights.
- Implementing demand-responsive policies such as congestion pricing and ride-sharing incentives.
- Enhancing infrastructure planning and public transport synchronization.

By leveraging the structured dimensional model and targeted SQL queries, this project provides actionable insights for improving traffic flow and reducing congestion in NYC.
