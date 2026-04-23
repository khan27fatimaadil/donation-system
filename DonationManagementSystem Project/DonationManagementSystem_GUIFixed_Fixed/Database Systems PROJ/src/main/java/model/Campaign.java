package model;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
public class Campaign {
    private String id;                 // char(36)
    private String organizationId;     // char(36)
    private String title;              // varchar(255)
    private String description;        // text
    private BigDecimal goalAmount;     // decimal(15,2)
    private String currencyCode;       // char(3)
    private LocalDate startDate;       // date
    private LocalDate endDate;         // date
    private Status status;             // enum
    private String imageUrl;           // varchar(500)
    private LocalDateTime createdAt;   // datetime
    private LocalDateTime updatedAt;   // datetime
    public Campaign(){}

    public Campaign(String id, String organizationId, String title, String description, BigDecimal goalAmount, String currencyCode, LocalDate startDate, LocalDate endDate, Status status, String imageUrl, LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.id = id;
        this.organizationId = organizationId;
        this.title = title;
        this.description = description;
        this.goalAmount = goalAmount;
        this.currencyCode = currencyCode;
        this.startDate = startDate;
        this.endDate = endDate;
        this.status = status;
        this.imageUrl = imageUrl;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    @Override
    public String toString() {
        return "Campaign{" +
                "id='" + id + '\'' +
                ", organizationId='" + organizationId + '\'' +
                ", title='" + title + '\'' +
                ", description='" + description + '\'' +
                ", goalAmount=" + goalAmount +
                ", currencyCode='" + currencyCode + '\'' +
                ", startDate=" + startDate +
                ", endDate=" + endDate +
                ", status=" + status +
                ", imageUrl='" + imageUrl + '\'' +
                ", createdAt=" + createdAt +
                ", updatedAt=" + updatedAt +
                '}';
    }

    public enum Status {
        draft, active, paused, completed, cancelled
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public String getOrganizationId() {
        return organizationId;
    }

    public void setOrganizationId(String organizationId) {
        this.organizationId = organizationId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public BigDecimal getGoalAmount() {
        return goalAmount;
    }

    public void setGoalAmount(BigDecimal goalAmount) {
        this.goalAmount = goalAmount;
    }

    public String getCurrencyCode() {
        return currencyCode;
    }

    public void setCurrencyCode(String currencyCode) {
        this.currencyCode = currencyCode;
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

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
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
}
