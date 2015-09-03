package com.example.valentina.virtuallifecoach.model;

public class MeasureType {
    private int _measureTypeId;
    private String unit;
    private String name;

    public int get_measureTypeId() {
        return _measureTypeId;
    }

    public void set_measureTypeId(int _measureTypeId) {
        this._measureTypeId = _measureTypeId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getUnit() {
        return unit;
    }

    public void setUnit(String unit) {
        this.unit = unit;
    }
}
