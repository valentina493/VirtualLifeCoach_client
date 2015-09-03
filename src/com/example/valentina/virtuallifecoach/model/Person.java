package com.example.valentina.virtuallifecoach.model;

public class Person {
    private int _personId;
    private String firstname;
    private String lastname;
    private String birthdate;

    public String getFirstname() {
        return firstname;
    }

    public void setFirstname(String firstname) {
        this.firstname = firstname;
    }

    public int get_personId() {
        return _personId;
    }

    public void set_personId(int _personId) {
        this._personId = _personId;
    }

    public String getLastname() {
        return lastname;
    }

    public void setLastname(String lastname) {
        this.lastname = lastname;
    }

    public String getBirthdate() {
        return birthdate;
    }

    public void setBirthdate(String birthdate) {
        this.birthdate = birthdate;
    }

    @Override
    public String toString() {
        return this._personId + ": " + this.firstname + " " + this.lastname + " " + this.birthdate;
    }
}
