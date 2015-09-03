package com.example.valentina.virtuallifecoach.model;

public class ExpiredGoal extends Goal {
    private boolean satisfied;
    private boolean satisfiable;

    public boolean isSatisfied() {
        return satisfied;
    }

    public void setSatisfied(boolean satisfied) {
        this.satisfied = satisfied;
    }

    public boolean isSatisfiable() {
        return satisfiable;
    }

    public void setSatisfiable(boolean satisfiable) {
        this.satisfiable = satisfiable;
    }
}
