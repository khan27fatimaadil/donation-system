package model;

import java.time.LocalDateTime;

public class Receipt {
    private String id;
    private String donationId;
    private String issuedBy;
    private String receiptNumber;
    private LocalDateTime issuedAt;
    private String fileUrl;
    private LocalDateTime sentAt;
    private LocalDateTime createdAt;

    public Receipt() {}

    public String getId()                        { return id; }
    public void   setId(String id)               { this.id = id; }
    public String getDonationId()                { return donationId; }
    public void   setDonationId(String v)        { this.donationId = v; }
    public String getIssuedBy()                  { return issuedBy; }
    public void   setIssuedBy(String v)          { this.issuedBy = v; }
    public String getReceiptNumber()             { return receiptNumber; }
    public void   setReceiptNumber(String v)     { this.receiptNumber = v; }
    public LocalDateTime getIssuedAt()           { return issuedAt; }
    public void   setIssuedAt(LocalDateTime v)   { this.issuedAt = v; }
    public String getFileUrl()                   { return fileUrl; }
    public void   setFileUrl(String v)           { this.fileUrl = v; }
    public LocalDateTime getSentAt()             { return sentAt; }
    public void   setSentAt(LocalDateTime v)     { this.sentAt = v; }
    public LocalDateTime getCreatedAt()          { return createdAt; }
    public void   setCreatedAt(LocalDateTime v)  { this.createdAt = v; }
}
