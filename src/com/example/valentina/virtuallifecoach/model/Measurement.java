package com.example.valentina.virtuallifecoach.model;

public class Measurement {
    private int _measurementId;
    private int personId;
    private MeasureType measureType;
    private double value;
    private String measuringDate;

    public int get_measurementId() {
        return _measurementId;
    }

    public void set_measurementId(int _measurementId) {
        this._measurementId = _measurementId;
    }

    public MeasureType getMeasureType() {
        return measureType;
    }

    public void setMeasureType(MeasureType measureType) {
        this.measureType = measureType;
    }

    public String getMeasuringDate() {
        return measuringDate;
    }

    public void setMeasuringDate(String measuringDate) {
        this.measuringDate = measuringDate;
    }

    public int getPersonId() {
        return personId;
    }

    public void setPersonId(int personId) {
        this.personId = personId;
    }

    public double getValue() {
        return value;
    }

    public void setValue(double value) {
        this.value = value;
    }
}
