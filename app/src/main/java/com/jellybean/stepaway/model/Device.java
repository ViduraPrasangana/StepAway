package com.jellybean.stepaway.model;

import com.jellybean.stepaway.service.CloudService;
import com.jellybean.stepaway.service.DeviceIdentifierService;

import java.util.ArrayList;

public class Device {

    private String macAddress;
    private String user = null;
    private String userName;
    private long lastIdentifiedTime;
    private Threat threatLevel;
    private Threat maximumThreat;
    private ArrayList<Integer> rssis;
    private double averageDistance;

    public Device() {
    }

    public Device(String macAddress, long lastIdentifiedTime, Threat threatLevel,String user) {
        this.macAddress = macAddress;
        this.lastIdentifiedTime = lastIdentifiedTime;
        this.threatLevel = threatLevel;
        this.averageDistance = 0;
        this.maximumThreat = threatLevel;
        this.user = user;
        rssis = new ArrayList<Integer>();


    }

    public String getUser() {
        return user;
    }

    public void setUser(String user) {
        this.user = user;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
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
        if(this.threatLevel != null && threatLevel.getValue() > this.threatLevel.getValue()){
            maximumThreat = threatLevel;
        }
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

        public static Threat getThreat(double distance){
            if(distance< DeviceIdentifierService.DISTANCE_DANGER){
                return LEVEL3;
            }
            if(distance< DeviceIdentifierService.DISTANCE_POTENTIAL_RISK){
                return LEVEL2;
            }
            if(distance< DeviceIdentifierService.DISTANCE_WARNING){
                return LEVEL3;
            }

            return NONE;
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
        setThreatLevel(Threat.getThreat(averageDistance));
    }

    public Threat getMaximumThreat() {
        return maximumThreat;
    }

    public void setMaximumThreat(Threat maximumThreat) {
        this.maximumThreat = maximumThreat;
    }

    public void setRssis(ArrayList<Integer> rssis) {
        this.rssis = rssis;
    }
}
