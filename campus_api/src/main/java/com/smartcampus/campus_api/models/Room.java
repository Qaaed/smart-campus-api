package com.smartcampus.campus_api.models;

import java.util.ArrayList;
import java.util.List;

//room object shows a single room in the api
public class Room {

    private String id;
    private String name;
    private int capacity;
    //By storing a list of string IDs instead of full Sensor objects, we make the responses faster.
    private List<String> sensorIds = new ArrayList<>();

    // JAX-RS strictly requires an empty constructor to automatically translate incoming json payloads to java objects
    public Room() {}

    public Room(String id, String name, int capacity) {
        this.id = id;
        this.name = name;
        this.capacity = capacity;
    }
    //The JSON translator relies completely on these public methods to read the data and write the JSON response for Postman.
    //getters and setters
    public String getId() {
        return id;
    }
    
    public void setId(String id) { 
        this.id = id; 
    }

    public String getName() { 
        return name; 
    }
    
    public void setName(String name) { 
        this.name = name; 
    }

    public int getCapacity() {
        return capacity; 
    }
    
    public void setCapacity(int capacity) { 
        this.capacity = capacity;
    }

    public List<String> getSensorIds() { 
        return sensorIds; 
    }
    
    public void setSensorIds(List<String> sensorIds) { 
        this.sensorIds = sensorIds;
    }
}