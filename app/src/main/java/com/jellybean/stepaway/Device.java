package com.jellybean.stepaway;

public class Device {

    private String macAddress;
    private String identifiedTime;
    private Threat threatLevel;

    public Device(String macAddress, String identifiedTime, Threat threatLevel) {
        this.macAddress = macAddress;
        this.identifiedTime = identifiedTime;
        this.threatLevel = threatLevel;
    }

    public String getMacAddress() {
        return macAddress;
    }

    public void setMacAddress(String macAddress) {
        this.macAddress = macAddress;
    }

    public String getIdentifiedTime() {
        return identifiedTime;
    }

    public void setIdentifiedTime(String identifiedTime) {
        this.identifiedTime = identifiedTime;
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
}
