package model;
import java.math.BigDecimal;

public class CampaignSummary {
    public CampaignSummary() {}
    private String campaignId;
    private String title;
    private BigDecimal goalAmount;
    private String currencyCode;
    private BigDecimal raisedAmount;
    private int donationCount;
    private double progressPct;

    public CampaignSummary(String campaignId, String title, BigDecimal goalAmount, String currencyCode, BigDecimal raisedAmount, int donationCount, double progressPct) {
        this.campaignId = campaignId;
        this.title = title;
        this.goalAmount = goalAmount;
        this.currencyCode = currencyCode;
        this.raisedAmount = raisedAmount;
        this.donationCount = donationCount;
        this.progressPct = progressPct;
    }

    public String getCampaignId() {
        return campaignId;
    }

    public void setCampaignId(String campaignId) {
        this.campaignId = campaignId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
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

    public BigDecimal getRaisedAmount() {
        return raisedAmount;
    }

    public void setRaisedAmount(BigDecimal raisedAmount) {
        this.raisedAmount = raisedAmount;
    }

    public int getDonationCount() {
        return donationCount;
    }

    public void setDonationCount(int donationCount) {
        this.donationCount = donationCount;
    }

    public double getProgressPct() {
        return progressPct;
    }

    public void setProgressPct(double progressPct) {
        this.progressPct = progressPct;
    }

    @Override
    public String toString() {
        return "CampaignSummary{" +
                "campaignId=" + campaignId +
                ", title='" + title + '\'' +
                ", goalAmount=" + goalAmount +
                ", currencyCode='" + currencyCode + '\'' +
                ", raisedAmount=" + raisedAmount +
                ", donationCount=" + donationCount +
                ", progressPct=" + progressPct +
                '}';
    }
}
