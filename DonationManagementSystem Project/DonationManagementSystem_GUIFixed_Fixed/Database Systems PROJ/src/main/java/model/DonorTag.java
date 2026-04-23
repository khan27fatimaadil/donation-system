package model;
import java.time.LocalDateTime;

public class DonorTag {
    private String donorId;
    private String tagId;
    private LocalDateTime assignedAt;


    public DonorTag() {}


    public DonorTag(String donorId, String tagId, LocalDateTime assignedAt) {
        this.donorId = donorId;
        this.tagId = tagId;
        this.assignedAt = assignedAt;
    }

    @Override
    public String toString() {
        return "DonorTag{" +
                "donorId=" + donorId +
                ", tagId=" + tagId +
                ", assignedAt=" + assignedAt +
                '}';
    }

    public String getDonorId() { return donorId; }
    public void setDonorId(String donorId) { this.donorId = donorId; }

    public String getTagId() { return tagId; }
    public void setTagId(String tagId) { this.tagId = tagId; }

    public LocalDateTime getAssignedAt() { return assignedAt; }
    public void setAssignedAt(LocalDateTime assignedAt) { this.assignedAt = assignedAt; }
}