package dao;

import db.DBConnection;
import model.PaymentTransaction;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class PaymentTransactionDAO {

    // ── HELPER: MAP ROW ──────────────────────────────────────────────────────
    private PaymentTransaction mapRow(ResultSet rs) throws SQLException {
        PaymentTransaction pt = new PaymentTransaction();
        pt.setId(rs.getString("id"));
        pt.setDonationId(rs.getString("donation_id"));
        pt.setGateway(rs.getString("gateway"));
        pt.setGatewayTxId(rs.getString("gateway_tx_id"));
        String methodStr = rs.getString("method");
        if (methodStr != null) pt.setMethod(PaymentTransaction.PaymentMethod.valueOf(methodStr));
        String statusStr = rs.getString("status");
        if (statusStr != null) pt.setStatus(PaymentTransaction.TransactionStatus.valueOf(statusStr));
        pt.setAmount(rs.getBigDecimal("amount"));
        pt.setCurrencyCode(rs.getString("currency_code"));
        pt.setFeeAmount(rs.getBigDecimal("fee_amount"));
        pt.setErrorMessage(rs.getString("error_message"));
        pt.setMetadata(rs.getString("metadata"));
        pt.setProcessedAt(rs.getTimestamp("processed_at") != null
                ? rs.getTimestamp("processed_at").toLocalDateTime() : null);
        pt.setCreatedAt(rs.getTimestamp("created_at") != null
                ? rs.getTimestamp("created_at").toLocalDateTime() : null);
        pt.setUpdatedAt(rs.getTimestamp("updated_at") != null
                ? rs.getTimestamp("updated_at").toLocalDateTime() : null);
        return pt;
    }

    // ── GET ALL ──────────────────────────────────────────────────────────────
    public List<PaymentTransaction> getAllTransactions() {
        List<PaymentTransaction> list = new ArrayList<>();
        String sql = "SELECT * FROM payment_transactions ORDER BY created_at DESC";
        try (Connection conn = DBConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) list.add(mapRow(rs));
        } catch (SQLException e) { e.printStackTrace(); }
        return list;
    }

    // ── GET BY ID ────────────────────────────────────────────────────────────
    public PaymentTransaction getById(String id) {
        String sql = "SELECT * FROM payment_transactions WHERE id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, id.toString());
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return mapRow(rs);
        } catch (SQLException e) { e.printStackTrace(); }
        return null;
    }

    // ── GET BY DONATION ──────────────────────────────────────────────────────
    public List<PaymentTransaction> getByDonation(String donationId) {
        List<PaymentTransaction> list = new ArrayList<>();
        String sql = "SELECT * FROM payment_transactions WHERE donation_id = ? ORDER BY created_at DESC";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, donationId.toString());
            ResultSet rs = ps.executeQuery();
            while (rs.next()) list.add(mapRow(rs));
        } catch (SQLException e) { e.printStackTrace(); }
        return list;
    }

    // ── GET BY STATUS ────────────────────────────────────────────────────────
    public List<PaymentTransaction> getByStatus(PaymentTransaction.TransactionStatus status) {
        List<PaymentTransaction> list = new ArrayList<>();
        String sql = "SELECT * FROM payment_transactions WHERE status = ? ORDER BY created_at DESC";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, status.name());
            ResultSet rs = ps.executeQuery();
            while (rs.next()) list.add(mapRow(rs));
        } catch (SQLException e) { e.printStackTrace(); }
        return list;
    }

    // ── ADD ──────────────────────────────────────────────────────────────────
    public boolean addTransaction(PaymentTransaction pt) {
        String sql = "INSERT INTO payment_transactions (id, donation_id, gateway, gateway_tx_id, " +
                     "method, status, amount, currency_code, fee_amount, error_message, metadata, processed_at) " +
                     "VALUES (UUID(), ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, pt.getDonationId().toString());
            ps.setString(2, pt.getGateway());
            ps.setString(3, pt.getGatewayTxId());
            ps.setString(4, pt.getMethod() != null ? pt.getMethod().name() : null);
            ps.setString(5, pt.getStatus() != null ? pt.getStatus().name() : "pending");
            ps.setBigDecimal(6, pt.getAmount());
            ps.setString(7, pt.getCurrencyCode());
            ps.setBigDecimal(8, pt.getFeeAmount());
            ps.setString(9, pt.getErrorMessage());
            ps.setString(10, pt.getMetadata());
            ps.setTimestamp(11, pt.getProcessedAt() != null
                    ? Timestamp.valueOf(pt.getProcessedAt()) : null);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) { e.printStackTrace(); return false; }
    }

    // ── UPDATE STATUS ────────────────────────────────────────────────────────
    public boolean updateStatus(String id, PaymentTransaction.TransactionStatus newStatus) {
        String sql = "UPDATE payment_transactions SET status = ?, updated_at = NOW() WHERE id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, newStatus.name());
            ps.setString(2, id.toString());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) { e.printStackTrace(); return false; }
    }

    // ── DELETE ───────────────────────────────────────────────────────────────
    public boolean deleteTransaction(String id) {
        String sql = "DELETE FROM payment_transactions WHERE id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, id.toString());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) { e.printStackTrace(); return false; }
    }
}
