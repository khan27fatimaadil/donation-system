package dao;

import db.DBConnection;
import model.Donation;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class DonationDAO {

    // ── HELPER: MAP ROW ──────────────────────────────────────────────────────
    private Donation mapRow(ResultSet rs) throws SQLException {
        Donation d = new Donation();
        d.setId(rs.getString("id"));
        d.setDonorId(rs.getString("donor_id"));
        d.setCampaignId(rs.getString("campaign_id"));
        d.setFundId(rs.getString("fund_id"));
        java.math.BigDecimal amt = rs.getBigDecimal("amount");
        d.setAmount(amt != null ? amt : java.math.BigDecimal.ZERO);
        String currency = rs.getString("currency_code");
        d.setCurrencyCode(currency != null ? currency : "PKR");
        String status = rs.getString("status");
        d.setStatus(status != null ? status : "unknown");
        d.setTransactionId(rs.getString("transaction_id"));
        d.setDonatedAt(rs.getTimestamp("donated_at") != null
                ? rs.getTimestamp("donated_at").toLocalDateTime() : null);
        return d;
    }

    // ── GET ALL ──────────────────────────────────────────────────────────────
    public List<Donation> getAllDonations() {
        List<Donation> list = new ArrayList<>();
        String sql = "SELECT * FROM donations ORDER BY donated_at DESC";
        try (Connection conn = DBConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) list.add(mapRow(rs));
        } catch (SQLException e) { e.printStackTrace(); }
        return list;
    }

    // ── GET BY ID ────────────────────────────────────────────────────────────
    public Donation getById(String id) {
        String sql = "SELECT * FROM donations WHERE id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, id);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return mapRow(rs);
        } catch (SQLException e) { e.printStackTrace(); }
        return null;
    }

    // ── GET BY DONOR ─────────────────────────────────────────────────────────
    public List<Donation> getByDonor(String donorId) {
        List<Donation> list = new ArrayList<>();
        String sql = "SELECT * FROM donations WHERE donor_id = ? ORDER BY donated_at DESC";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, donorId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) list.add(mapRow(rs));
        } catch (SQLException e) { e.printStackTrace(); }
        return list;
    }

    // ── GET BY CAMPAIGN ──────────────────────────────────────────────────────
    public List<Donation> getByCampaign(String campaignId) {
        List<Donation> list = new ArrayList<>();
        String sql = "SELECT * FROM donations WHERE campaign_id = ? ORDER BY donated_at DESC";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, campaignId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) list.add(mapRow(rs));
        } catch (SQLException e) { e.printStackTrace(); }
        return list;
    }

    // ── GET BY STATUS ────────────────────────────────────────────────────────
    public List<Donation> getByStatus(String status) {
        List<Donation> list = new ArrayList<>();
        String sql = "SELECT * FROM donations WHERE status = ? ORDER BY donated_at DESC";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, status);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) list.add(mapRow(rs));
        } catch (SQLException e) { e.printStackTrace(); }
        return list;
    }

    // ── ADD ──────────────────────────────────────────────────────────────────
    public boolean addDonation(Donation d) {
        String sql = "INSERT INTO donations (id, donor_id, campaign_id, fund_id, amount, " +
                     "currency_code, status, transaction_id, donated_at) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, d.getId());
            ps.setString(2, d.getDonorId());
            ps.setString(3, d.getCampaignId());
            ps.setString(4, d.getFundId());
            ps.setBigDecimal(5, d.getAmount());
            ps.setString(6, d.getCurrencyCode());
            ps.setString(7, d.getStatus() != null ? d.getStatus() : "completed");
            ps.setString(8, d.getTransactionId());
            
            if (d.getDonatedAt() != null) {
                ps.setTimestamp(9, Timestamp.valueOf(d.getDonatedAt()));
            } else {
                ps.setTimestamp(9, new Timestamp(System.currentTimeMillis()));
            }

            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("DB ERROR in addDonation: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    // ── UPDATE STATUS ────────────────────────────────────────────────────────
    public boolean updateStatus(String id, String newStatus) {
        String sql = "UPDATE donations SET status = ? WHERE id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, newStatus);
            ps.setString(2, id);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) { e.printStackTrace(); return false; }
    }

    // ── DELETE ───────────────────────────────────────────────────────────────
    public boolean deleteDonation(String id) {
        String sql = "DELETE FROM donations WHERE id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, id);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) { e.printStackTrace(); return false; }
    }
}
