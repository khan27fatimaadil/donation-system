package model;
import java.math.BigDecimal;
import java.time.LocalDateTime;

public class Donation {
    public Donation(){}
    private String id;              // CHAR(36) PK
    private String donorId;         // CHAR(36) FK to donors
    private String campaignId;      // CHAR(36) FK to campaigns
    private String fundId;          // CHAR(36) FK to funds
    private BigDecimal amount;       // DECIMAL(15,2)
    private String currencyCode;    // CHAR(3) FK to currencies
    private String status;          // ENUM/String: 'pending', 'completed', 'failed'
    private String transactionId;   // Optional: For external payment reference
    private LocalDateTime donatedAt;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getDonorId() {
        return donorId;
    }

    public void setDonorId(String donorId) {
        this.donorId = donorId;
    }

    public String getCampaignId() {
        return campaignId;
    }

    public void setCampaignId(String campaignId) {
        this.campaignId = campaignId;
    }

    public String getFundId() {
        return fundId;
    }

    public void setFundId(String fundId) {
        this.fundId = fundId;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public String getCurrencyCode() {
        return currencyCode;
    }

    public void setCurrencyCode(String currencyCode) {
        this.currencyCode = currencyCode;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getTransactionId() {
        return transactionId;
    }

    public void setTransactionId(String transactionId) {
        this.transactionId = transactionId;
    }

    public LocalDateTime getDonatedAt() {
        return donatedAt;
    }

    public void setDonatedAt(LocalDateTime donatedAt) {
        this.donatedAt = donatedAt;
    }

    @Override
    public String toString() {
        return "Donation{" +
                "id='" + id + '\'' +
                ", donorId='" + donorId + '\'' +
                ", campaignId='" + campaignId + '\'' +
                ", fundId='" + fundId + '\'' +
                ", amount=" + amount +
                ", currencyCode='" + currencyCode + '\'' +
                ", status='" + status + '\'' +
                ", transactionId='" + transactionId + '\'' +
                ", donatedAt=" + donatedAt +
                '}';
    }

    public Donation(String id, String donorId, String campaignId, String fundId, BigDecimal amount, String currencyCode, String status, String transactionId, LocalDateTime donatedAt) {
        this.id = id;
        this.donorId = donorId;
        this.campaignId = campaignId;
        this.fundId = fundId;
        this.amount = amount;
        this.currencyCode = currencyCode;
        this.status = status;
        this.transactionId = transactionId;
        this.donatedAt = donatedAt;
    }
}
