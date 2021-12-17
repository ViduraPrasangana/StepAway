package com.jellybean.stepaway.model;

import java.util.ArrayList;

public class Device {

    private String macAddress;
    private long lastIdentifiedTime;
    private Threat threatLevel;
    private ArrayList<Double> distances;

    public Device(String macAddress, long lastIdentifiedTime, Threat threatLevel) {
        this.macAddress = macAddress;
        this.lastIdentifiedTime = lastIdentifiedTime;
        this.threatLevel = threatLevel;
        distances = new ArrayList<>();
    }

    public String getMacAddress() {
        return macAddress;
    }

    public void setMacAddress(String macAddress) {
        this.macAddress = macAddress;
    }

    public long getLastIdentifiedTime() {
        return lastIdentifiedTime;
    }

    public void setLastIdentifiedTime(long lastIdentifiedTime) {
        this.lastIdentifiedTime = lastIdentifiedTime;
    }

    public Threat getThreatLevel() {
        return threatLevel;
    }

    public void setThreatLevel(Threat threatLevel) {
        this.threatLevel = threatLevel;
    }

    public enum Threat{
        NONE(0),
        LEVEL1(1),
        LEVEL2(2),
        LEVEL3(3),
        ;
        int value;
        Threat(int val) {
            this.value = val;
        }

        public int getValue() {
            return value;
        }
    }

    public ArrayList<Double> getDistances() {
        return distances;
    }

    public void addDistance(double distance){
        distances.add(distance);
    }
}
