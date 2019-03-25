package com.eprs.carchecker;

public class CarTag {
    private String carNumber;
    private String date;
    private String tagType;

    public CarTag(String carNumber, String date, String tagType) {
        this.carNumber = carNumber;
        this.date = date;
        this.tagType = tagType;
    }

    public String getCarNumber() {
        return carNumber;
    }

    public String getDate() {
        return date;
    }

    public String getTagType() {
        return tagType;
    }
}
