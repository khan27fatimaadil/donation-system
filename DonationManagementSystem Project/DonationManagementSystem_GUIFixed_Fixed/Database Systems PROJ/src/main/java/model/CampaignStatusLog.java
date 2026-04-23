package model;
import java.time.LocalDateTime;

public class CampaignStatusLog {
    private String id;
    private String campaignId;
    private String changedBy; // Nullable if system-generated or user deleted
    private CampaignStatus oldStatus; // Nullable for initial creation
    private CampaignStatus newStatus;
    private String notes;
    private LocalDateTime changedAt;
    public enum CampaignStatus {
        draft, active, paused, completed, cancelled
    }
    public CampaignStatusLog() {}

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

    public String getChangedBy() {
        return changedBy;
    }

    public void setChangedBy(String changedBy) {
        this.changedBy = changedBy;
    }

    public CampaignStatus getOldStatus() {
        return oldStatus;
    }

    public void setOldStatus(CampaignStatus oldStatus) {
        this.oldStatus = oldStatus;
    }

    public CampaignStatus getNewStatus() {
        return newStatus;
    }

    public void setNewStatus(CampaignStatus newStatus) {
        this.newStatus = newStatus;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public LocalDateTime getChangedAt() {
        return changedAt;
    }

    public void setChangedAt(LocalDateTime changedAt) {
        this.changedAt = changedAt;
    }

    @Override
    public String toString() {
        return "CampaignStatusLog{" +
                "id=" + id +
                ", campaignId=" + campaignId +
                ", changedBy=" + changedBy +
                ", oldStatus=" + oldStatus +
                ", newStatus=" + newStatus +
                ", notes='" + notes + '\'' +
                ", changedAt=" + changedAt +
                '}';
    }

    public CampaignStatusLog(String id, String campaignId, String changedBy, CampaignStatus oldStatus, CampaignStatus newStatus, String notes, LocalDateTime changedAt) {
        this.id = id;
        this.campaignId = campaignId;
        this.changedBy = changedBy;
        this.oldStatus = oldStatus;
        this.newStatus = newStatus;
        this.notes = notes;
        this.changedAt = changedAt;
    }
}
