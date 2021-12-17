package com.jellybean.stepaway.model;

import android.os.Build;

public class DeviceModel {
    private String version;
    private String buildNumber;
    private String model;
    private String manufacturer;

    public DeviceModel() {
        this(
                Build.VERSION.RELEASE,
                Build.ID,
                Build.MODEL,
                Build.MANUFACTURER
        );
    }
    public DeviceModel(String version, String buildNumber, String model, String manufacturer) {
        this.version = version;
        this.buildNumber = buildNumber;
        this.model = model;
        this.manufacturer = manufacturer;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public String getBuildNumber() {
        return buildNumber;
    }

    public void setBuildNumber(String buildNumber) {
        this.buildNumber = buildNumber;
    }

    public String getModel() {
        return model;
    }

    public void setModel(String model) {
        this.model = model;
    }

    public String getManufacturer() {
        return manufacturer;
    }

    public void setManufacturer(String manufacturer) {
        this.manufacturer = manufacturer;
    }

    public int calScore(DeviceModel otherModel) {
        int score = 0;
        if (this.manufacturer.equalsIgnoreCase(otherModel.manufacturer)) {
            score = 1;
        }
        if (score ==1 && this.model.equals(otherModel.model)) {
            score = 2;
        }
        if (score == 2 && this.buildNumber.equals(otherModel.buildNumber)) {
            score = 3;
        }
        if (score == 3 && this.version.equals(otherModel.version)) {
            score = 4;
        }
        return score;
    }
}
