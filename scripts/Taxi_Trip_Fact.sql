CREATE TABLE taxi_trip_fact (
    VendorID INT,
    tpep_pickup_datetime TIMESTAMP,
    tpep_dropoff_datetime TIMESTAMP,
    passenger_count INT,
    trip_distance NUMERIC,
    PULocationID INT,
    DOLocationID INT,
    payment_type INT,
    fare_amount NUMERIC,
    tip_amount NUMERIC,
    tolls_amount NUMERIC,
    total_amount NUMERIC,
    congestion_surcharge NUMERIC
);
