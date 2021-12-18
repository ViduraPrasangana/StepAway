package com.jellybean.stepaway.model;

import java.util.ArrayList;

public class Device {

    private String macAddress;
    private long lastIdentifiedTime;
    private Threat threatLevel;
    private ArrayList<Integer> rssis;
    private double averageDistance;

    public Device(String macAddress, long lastIdentifiedTime, Threat threatLevel) {
        this.macAddress = macAddress;
        this.lastIdentifiedTime = lastIdentifiedTime;
        this.threatLevel = threatLevel;
        this.averageDistance = 0;
        rssis = new ArrayList<Integer>();
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

    public ArrayList<Integer> getRssis() {
        return rssis;
    }

    public void addRssi(Integer rssi){
        rssis.add(rssi);
    }

    public double getAverageDistance() {
        return averageDistance;
    }

    public void setAverageDistance(double averageDistance) {
        this.averageDistance = averageDistance;
    }
}
