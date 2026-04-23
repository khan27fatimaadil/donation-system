package model;
public class Currency {
    public Currency(){}
    private String code;      // CHAR(3) - e.g., "PKR", "USD", "GBP"
    private String name;      // VARCHAR(100) - e.g., "Pakistani Rupee"
    private String symbol;    // VARCHAR(10) - e.g., "₨", "$"
    private boolean isActive;

    public String getCode() {
        return code;
    }

    public Currency(String code, String name, String symbol, boolean isActive) {
        this.code = code;
        this.name = name;
        this.symbol = symbol;
        this.isActive = isActive;
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

    public String getSymbol() {
        return symbol;
    }

    public void setSymbol(String symbol) {
        this.symbol = symbol;
    }

    public boolean isActive() {
        return isActive;
    }

    public void setActive(boolean active) {
        isActive = active;
    }

    @Override
    public String toString() {
        return "Currency{" +
                "code='" + code + '\'' +
                ", name='" + name + '\'' +
                ", symbol='" + symbol + '\'' +
                ", isActive=" + isActive +
                '}';
    }
}
