package com.example.valentina.virtuallifecoach.model;

public class Goal {

    private int _goalId;
    private int personId;
    private MeasureType measureType;
    private double minvalue;
    private double maxvalue;
    private String deadline;
    private String created;

    public int get_goalId() {
        return _goalId;
    }

    public void set_goalId(int _goalId) {
        this._goalId = _goalId;
    }

    public String getCreated() {
        return created;
    }

    public void setCreated(String created) {
        this.created = created;
    }

    public String getDeadline() {
        return deadline;
    }

    public void setDeadline(String deadline) {
        this.deadline = deadline;
    }

    public double getMaxvalue() {
        return maxvalue;
    }

    public void setMaxvalue(double maxvalue) {
        this.maxvalue = maxvalue;
    }

    public MeasureType getMeasureType() {
        return measureType;
    }

    public void setMeasureType(MeasureType measureType) {
        this.measureType = measureType;
    }

    public double getMinvalue() {
        return minvalue;
    }

    public void setMinvalue(double minvalue) {
        this.minvalue = minvalue;
    }

    public int getPersonId() {
        return personId;
    }

    public void setPersonId(int personId) {
        this.personId = personId;
    }
}
