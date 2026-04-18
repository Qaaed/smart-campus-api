package com.smartcampus.campus_api;

import com.smartcampus.campus_api.models.Readings;
import com.smartcampus.campus_api.models.Room;
import com.smartcampus.campus_api.models.Sensor;


import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

// storage for entire api
// uses singleteon pattern (data is static but shared globally)
public class Storage {

    // Using ConcurrentHashMap ensures that if multiple API requests happen at the same time, the server will not crash
    private static final Map<String, Room> rooms = new ConcurrentHashMap<>();
    private static final Map<String, Sensor> sensors = new ConcurrentHashMap<>();
    private static final Map<String, List<Readings>> Readings = new ConcurrentHashMap<>();

    //getters
    public static Map<String, Room> getRooms() {
        return rooms;
    }

    public static Map<String, Sensor> getSensors() {
        return sensors;
    }

    public static Map<String, List<Readings>> getSensorReadings() {
        return Readings;
    }

    public static List<Readings> getReadingsForSensor(String sensorId) {
        return Readings.computeIfAbsent(sensorId, k -> new ArrayList<>());
    }
}
