package model;
import java.math.BigDecimal;
import java.time.LocalDateTime;

public class Allocation {
    public Allocation(){}
    private String id;
    private String donationId;
    private String fundId;
    private BigDecimal amount;
    private LocalDateTime createdAt;

    public Allocation(String id, String donationId, String fundId, BigDecimal amount, LocalDateTime createdAt) {
        this.id = id;
        this.donationId = donationId;
        this.fundId = fundId;
        this.amount = amount;
        this.createdAt = createdAt;
    }

    @Override
    public String toString() {
        return "Allocation{" +
                "id=" + id +
                ", donationId=" + donationId +
                ", fundId=" + fundId +
                ", amount=" + amount +
                ", createdAt=" + createdAt +
                '}';
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getDonationId() {
        return donationId;
    }

    public void setDonationId(String donationId) {
        this.donationId = donationId;
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

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
