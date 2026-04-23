package model;
import java.time.LocalDateTime;

public class DonationStatusLog {

        private String id;
        private String donationId;
        private String changedBy; // Can be null (System or deleted user)
        private DonationStatus oldStatus; // Nullable for the first status entry
        private DonationStatus newStatus;
        private String notes;
        private LocalDateTime changedAt;


        public enum DonationStatus {
            pending, completed, failed, refunded, cancelled
        }
        public DonationStatusLog() {}

    @Override
    public String toString() {
        return "DonationStatusLog{" +
                "id=" + id +
                ", donationId=" + donationId +
                ", changedBy=" + changedBy +
                ", oldStatus=" + oldStatus +
                ", newStatus=" + newStatus +
                ", notes='" + notes + '\'' +
                ", changedAt=" + changedAt +
                '}';
    }

    public DonationStatusLog(String id, String donationId, String changedBy, DonationStatus oldStatus, DonationStatus newStatus, String notes, LocalDateTime changedAt) {
        this.id = id;
        this.donationId = donationId;
        this.changedBy = changedBy;
        this.oldStatus = oldStatus;
        this.newStatus = newStatus;
        this.notes = notes;
        this.changedAt = changedAt;
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

    public String getChangedBy() {
        return changedBy;
    }

    public void setChangedBy(String changedBy) {
        this.changedBy = changedBy;
    }

    public DonationStatus getOldStatus() {
        return oldStatus;
    }

    public void setOldStatus(DonationStatus oldStatus) {
        this.oldStatus = oldStatus;
    }

    public DonationStatus getNewStatus() {
        return newStatus;
    }

    public void setNewStatus(DonationStatus newStatus) {
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
}

