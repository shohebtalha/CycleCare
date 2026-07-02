package com.cyclecare.nutrition;

import java.util.List;

public class FoodInfo {

    private String name;

    private List<String> aliases;

    private String category;

    private boolean healthy;

    private String benefit;

    private String periodBenefit;

    private String warning;

    public FoodInfo() {
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name.toLowerCase();
    }

    public List<String> getAliases() {
        return aliases;
    }

    public void setAliases(List<String> aliases) {
        this.aliases = aliases;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public boolean isHealthy() {
        return healthy;
    }

    public void setHealthy(boolean healthy) {
        this.healthy = healthy;
    }

    public String getBenefit() {
        return benefit;
    }

    public void setBenefit(String benefit) {
        this.benefit = benefit;
    }

    public String getPeriodBenefit() {
        return periodBenefit;
    }

    public void setPeriodBenefit(String periodBenefit) {
        this.periodBenefit = periodBenefit;
    }

    public String getWarning() {
        return warning;
    }

    public void setWarning(String warning) {
        this.warning = warning;
    }
}