package dao;

import db.DBConnection;
import model.Receipt;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ReceiptDAO {

    // ── HELPER: MAP ROW ──────────────────────────────────────────────────────
    private Receipt mapRow(ResultSet rs) throws SQLException {
        Receipt r = new Receipt();
        r.setId(rs.getString("id"));
        r.setDonationId(rs.getString("donation_id"));
        String issuedBy = rs.getString("issued_by");
        if (issuedBy != null) r.setIssuedBy(issuedBy);
        r.setReceiptNumber(rs.getString("receipt_number"));
        r.setIssuedAt(rs.getTimestamp("issued_at") != null
                ? rs.getTimestamp("issued_at").toLocalDateTime() : null);
        r.setFileUrl(rs.getString("file_url"));
        r.setSentAt(rs.getTimestamp("sent_at") != null
                ? rs.getTimestamp("sent_at").toLocalDateTime() : null);
        r.setCreatedAt(rs.getTimestamp("created_at") != null
                ? rs.getTimestamp("created_at").toLocalDateTime() : null);
        return r;
    }

    // ── GET ALL ──────────────────────────────────────────────────────────────
    public List<Receipt> getAllReceipts() {
        List<Receipt> list = new ArrayList<>();
        String sql = "SELECT * FROM receipts ORDER BY issued_at DESC";
        try (Connection conn = DBConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) list.add(mapRow(rs));
        } catch (SQLException e) { e.printStackTrace(); }
        return list;
    }

    // ── GET BY ID ────────────────────────────────────────────────────────────
    public Receipt getById(String id) {
        String sql = "SELECT * FROM receipts WHERE id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, id.toString());
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return mapRow(rs);
        } catch (SQLException e) { e.printStackTrace(); }
        return null;
    }

    // ── GET BY DONATION ──────────────────────────────────────────────────────
    public Receipt getByDonation(String donationId) {
        String sql = "SELECT * FROM receipts WHERE donation_id = ? ORDER BY issued_at DESC LIMIT 1";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, donationId.toString());
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return mapRow(rs);
        } catch (SQLException e) { e.printStackTrace(); }
        return null;
    }

    // ── GET BY RECEIPT NUMBER ────────────────────────────────────────────────
    public Receipt getByReceiptNumber(String receiptNumber) {
        String sql = "SELECT * FROM receipts WHERE receipt_number = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, receiptNumber);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return mapRow(rs);
        } catch (SQLException e) { e.printStackTrace(); }
        return null;
    }

    // ── GET UNSENT RECEIPTS ──────────────────────────────────────────────────
    public List<Receipt> getUnsentReceipts() {
        List<Receipt> list = new ArrayList<>();
        String sql = "SELECT * FROM receipts WHERE sent_at IS NULL ORDER BY issued_at ASC";
        try (Connection conn = DBConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) list.add(mapRow(rs));
        } catch (SQLException e) { e.printStackTrace(); }
        return list;
    }

    // ── ADD ──────────────────────────────────────────────────────────────────
    public boolean addReceipt(Receipt r) {
        String sql = "INSERT INTO receipts " +
                     "(id, donation_id, issued_by, receipt_number, issued_at, file_url, sent_at) " +
                     "VALUES (UUID(), ?, ?, ?, NOW(), ?, ?)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, r.getDonationId().toString());
            ps.setString(2, r.getIssuedBy() != null ? r.getIssuedBy().toString() : null);
            ps.setString(3, r.getReceiptNumber());
            ps.setString(4, r.getFileUrl());
            ps.setTimestamp(5, r.getSentAt() != null ? Timestamp.valueOf(r.getSentAt()) : null);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) { e.printStackTrace(); return false; }
    }

    // ── MARK AS SENT ─────────────────────────────────────────────────────────
    public boolean markAsSent(String id) {
        String sql = "UPDATE receipts SET sent_at = NOW() WHERE id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, id.toString());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) { e.printStackTrace(); return false; }
    }

    // ── UPDATE FILE URL ──────────────────────────────────────────────────────
    public boolean updateFileUrl(String id, String fileUrl) {
        String sql = "UPDATE receipts SET file_url = ? WHERE id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, fileUrl);
            ps.setString(2, id.toString());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) { e.printStackTrace(); return false; }
    }

    // ── DELETE ───────────────────────────────────────────────────────────────
    public boolean deleteReceipt(String id) {
        String sql = "DELETE FROM receipts WHERE id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, id.toString());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) { e.printStackTrace(); return false; }
    }
}
