package model;
public class Country {
    private String code;      // CHAR(2) - e.g., "PK", "US"
    private String name;      // VARCHAR(100)
    private String region;    // VARCHAR(100)
    private boolean isActive;
    public Country(){}

    public Country(String code, String name, String region, boolean isActive) {
        this.code = code;
        this.name = name;
        this.region = region;
        this.isActive = isActive;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getRegion() {
        return region;
    }

    public void setRegion(String region) {
        this.region = region;
    }

    public boolean isActive() {
        return isActive;
    }

    public void setActive(boolean active) {
        isActive = active;
    }

    @Override
    public String toString() {
        return "Country{" +
                "code='" + code + '\'' +
                ", name='" + name + '\'' +
                ", region='" + region + '\'' +
                ", isActive=" + isActive +
                '}';
    }
}
