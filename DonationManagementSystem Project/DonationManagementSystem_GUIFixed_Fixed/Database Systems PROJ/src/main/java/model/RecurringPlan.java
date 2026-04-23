package model;
import java.time.LocalDate;
import java.time.LocalDateTime;

public class RecurringPlan {
    public RecurringPlan() {}
    private String id;

    // Foreign Keys
    private String campaignId;
    private String currencyCode;
    private String donorId;

    // Plan Details
    private Frequency frequency;
    private LocalDate nextChargeDate; // Use LocalDate for 'date' type
    private LocalDate startDate;
    private LocalDate endDate;
    private PlanStatus status;
    private String gatewayPlanId;

    // Metadata
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // Enums for MySQL ENUM types
    public enum Frequency {
        weekly, monthly, quarterly, annually
    }

    public enum PlanStatus {
        active, paused, cancelled, completed
    }

    public RecurringPlan(String id, String campaignId, String currencyCode, String donorId, Frequency frequency, LocalDate nextChargeDate, LocalDate startDate, LocalDate endDate, PlanStatus status, String gatewayPlanId, LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.id = id;
        this.campaignId = campaignId;
        this.currencyCode = currencyCode;
        this.donorId = donorId;
        this.frequency = frequency;
        this.nextChargeDate = nextChargeDate;
        this.startDate = startDate;
        this.endDate = endDate;
        this.status = status;
        this.gatewayPlanId = gatewayPlanId;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getCampaignId() {
        return campaignId;
    }

    public void setCampaignId(String campaignId) {
        this.campaignId = campaignId;
    }

    public String getCurrencyCode() {
        return currencyCode;
    }

    public void setCurrencyCode(String currencyCode) {
        this.currencyCode = currencyCode;
    }

    public String getDonorId() {
        return donorId;
    }

    public void setDonorId(String donorId) {
        this.donorId = donorId;
    }

    public Frequency getFrequency() {
        return frequency;
    }

    public void setFrequency(Frequency frequency) {
        this.frequency = frequency;
    }

    public LocalDate getNextChargeDate() {
        return nextChargeDate;
    }

    public void setNextChargeDate(LocalDate nextChargeDate) {
        this.nextChargeDate = nextChargeDate;
    }

    public LocalDate getStartDate() {
        return startDate;
    }

    public void setStartDate(LocalDate startDate) {
        this.startDate = startDate;
    }

    public LocalDate getEndDate() {
        return endDate;
    }

    public void setEndDate(LocalDate endDate) {
        this.endDate = endDate;
    }

    public PlanStatus getStatus() {
        return status;
    }

    public void setStatus(PlanStatus status) {
        this.status = status;
    }

    public String getGatewayPlanId() {
        return gatewayPlanId;
    }

    public void setGatewayPlanId(String gatewayPlanId) {
        this.gatewayPlanId = gatewayPlanId;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    @Override
    public String toString() {
        return "RecurringPlan{" +
                "id=" + id +
                ", campaignId=" + campaignId +
                ", currencyCode='" + currencyCode + '\'' +
                ", donorId=" + donorId +
                ", frequency=" + frequency +
                ", nextChargeDate=" + nextChargeDate +
                ", startDate=" + startDate +
                ", endDate=" + endDate +
                ", status=" + status +
                ", gatewayPlanId='" + gatewayPlanId + '\'' +
                ", createdAt=" + createdAt +
                ", updatedAt=" + updatedAt +
                '}';
    }
}
