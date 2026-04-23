package model;
import java.math.BigDecimal;
import java.time.LocalDateTime;

public class PaymentTransaction {
    public PaymentTransaction() {}
    private String id;
    private String donationId;
    private String gateway;
    private String gatewayTxId;
    private PaymentMethod method;
    private TransactionStatus status;
    private BigDecimal amount;
    private String currencyCode;
    private BigDecimal feeAmount;
    private String errorMessage;
    private String metadata; // Handled as a String (JSON) in Java
    private LocalDateTime processedAt;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // Enums for database types
    public enum PaymentMethod {
        credit_card, debit_card, bank_transfer, paypal, stripe, cash, check, crypto
    }

    public enum TransactionStatus {
        pending, processing, succeeded, failed, refunded, disputed
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

    public String getGateway() {
        return gateway;
    }

    public void setGateway(String gateway) {
        this.gateway = gateway;
    }

    public String getGatewayTxId() {
        return gatewayTxId;
    }

    public void setGatewayTxId(String gatewayTxId) {
        this.gatewayTxId = gatewayTxId;
    }

    public PaymentMethod getMethod() {
        return method;
    }

    public void setMethod(PaymentMethod method) {
        this.method = method;
    }

    public TransactionStatus getStatus() {
        return status;
    }

    public void setStatus(TransactionStatus status) {
        this.status = status;
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

    public BigDecimal getFeeAmount() {
        return feeAmount;
    }

    public void setFeeAmount(BigDecimal feeAmount) {
        this.feeAmount = feeAmount;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    public String getMetadata() {
        return metadata;
    }

    public void setMetadata(String metadata) {
        this.metadata = metadata;
    }

    public LocalDateTime getProcessedAt() {
        return processedAt;
    }

    public void setProcessedAt(LocalDateTime processedAt) {
        this.processedAt = processedAt;
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

    public PaymentTransaction(String id, String donationId, String gateway, String gatewayTxId, PaymentMethod method, TransactionStatus status, BigDecimal amount, String currencyCode, BigDecimal feeAmount, String errorMessage, String metadata, LocalDateTime processedAt, LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.id = id;
        this.donationId = donationId;
        this.gateway = gateway;
        this.gatewayTxId = gatewayTxId;
        this.method = method;
        this.status = status;
        this.amount = amount;
        this.currencyCode = currencyCode;
        this.feeAmount = feeAmount;
        this.errorMessage = errorMessage;
        this.metadata = metadata;
        this.processedAt = processedAt;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    @Override
    public String toString() {
        return "PaymentTransaction{" +
                "id=" + id +
                ", donationId=" + donationId +
                ", gateway='" + gateway + '\'' +
                ", gatewayTxId='" + gatewayTxId + '\'' +
                ", method=" + method +
                ", status=" + status +
                ", amount=" + amount +
                ", currencyCode='" + currencyCode + '\'' +
                ", feeAmount=" + feeAmount +
                ", errorMessage='" + errorMessage + '\'' +
                ", metadata='" + metadata + '\'' +
                ", processedAt=" + processedAt +
                ", createdAt=" + createdAt +
                ", updatedAt=" + updatedAt +
                '}';
    }
}
