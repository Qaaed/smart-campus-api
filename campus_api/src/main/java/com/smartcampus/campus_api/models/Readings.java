package com.smartcampus.campus_api.models;

//everytime a sensor takes a measurement a Readings Object is created
public class Readings {
    

    private String id;
    private long timestamp; // Storing time as a 'long' (millisecond) instead of a formatted String
    private double value;

    // JAX-RS strictly requires an empty constructor to automatically translate incoming json payloads to java objects
    public Readings() {
    }

    public Readings(String id, long timestamp, double value) {
        this.id = id;
        this.timestamp = timestamp;
        this.value = value;
    }   
    //The JSON translator relies completely on these public methods to read the data and write the JSON response for Postman.
    //getters and setters
    public String getId() { 
        return id; 
    }
    
    public void setId(String id) { 
        this.id = id;
    }

    public long getTimestamp() {
        return timestamp; 
    }
    
    public void setTimestamp(long timestamp) { 
        this.timestamp = timestamp; 
    }

    public double getValue() { 
        return value; 
    }
    
    public void setValue(double value) { 
        this.value = value; 
    }
}