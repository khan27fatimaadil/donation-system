package model;
import java.time.LocalDateTime;

public class Communication {
    public Communication(){};

    private String id;
    private String donorId;
    private String donationId; // Optional: Link to a specific gift
    private String sentBy;     // User who triggered the message
    private Channel channel;
    private String subject;
    private String body;
    private CommStatus status;
    private LocalDateTime sentAt;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    public enum Channel {
        email, sms, letter, phone, in_app
    }

    public enum CommStatus {
        draft, queued, sent, delivered, failed, bounced
    }

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

    public String getDonationId() {
        return donationId;
    }

    public void setDonationId(String donationId) {
        this.donationId = donationId;
    }

    public String getSentBy() {
        return sentBy;
    }

    public void setSentBy(String sentBy) {
        this.sentBy = sentBy;
    }

    public Channel getChannel() {
        return channel;
    }

    public void setChannel(Channel channel) {
        this.channel = channel;
    }

    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public String getBody() {
        return body;
    }

    public void setBody(String body) {
        this.body = body;
    }

    public CommStatus getStatus() {
        return status;
    }

    public void setStatus(CommStatus status) {
        this.status = status;
    }

    public LocalDateTime getSentAt() {
        return sentAt;
    }

    public void setSentAt(LocalDateTime sentAt) {
        this.sentAt = sentAt;
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
        return "Communication{" +
                "id=" + id +
                ", donorId=" + donorId +
                ", donationId=" + donationId +
                ", sentBy=" + sentBy +
                ", channel=" + channel +
                ", subject='" + subject + '\'' +
                ", body='" + body + '\'' +
                ", status=" + status +
                ", sentAt=" + sentAt +
                ", createdAt=" + createdAt +
                ", updatedAt=" + updatedAt +
                '}';
    }

    public Communication(String id, String donorId, String donationId, String sentBy, Channel channel, String subject, String body, CommStatus status, LocalDateTime sentAt, LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.id = id;
        this.donorId = donorId;
        this.donationId = donationId;
        this.sentBy = sentBy;
        this.channel = channel;
        this.subject = subject;
        this.body = body;
        this.status = status;
        this.sentAt = sentAt;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }
}
