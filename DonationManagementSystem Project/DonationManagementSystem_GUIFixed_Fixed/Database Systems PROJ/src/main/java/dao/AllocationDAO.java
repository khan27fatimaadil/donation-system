package dao;

import db.DBConnection;
import model.Allocation;
import model.AllocationCheck;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class AllocationDAO {

    // ── HELPER: MAP ALLOCATION ROW ───────────────────────────────────────────
    private Allocation mapRow(ResultSet rs) throws SQLException {
        Allocation a = new Allocation();
        a.setId(rs.getString("id"));
        a.setDonationId(rs.getString("donation_id"));
        a.setFundId(rs.getString("fund_id"));
        a.setAmount(rs.getBigDecimal("amount"));
        a.setCreatedAt(rs.getTimestamp("created_at") != null
                ? rs.getTimestamp("created_at").toLocalDateTime() : null);
        return a;
    }

    // ── GET ALL ALLOCATIONS ──────────────────────────────────────────────────
    public List<Allocation> getAllAllocations() {
        List<Allocation> list = new ArrayList<>();
        String sql = "SELECT * FROM allocations ORDER BY created_at DESC";
        try (Connection conn = DBConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) list.add(mapRow(rs));
        } catch (SQLException e) { e.printStackTrace(); }
        return list;
    }

    // ── GET BY ID ────────────────────────────────────────────────────────────
    public Allocation getById(String id) {
        String sql = "SELECT * FROM allocations WHERE id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, id);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return mapRow(rs);
        } catch (SQLException e) { e.printStackTrace(); }
        return null;
    }

    // ── GET BY DONATION ──────────────────────────────────────────────────────
    public List<Allocation> getByDonation(String donationId) {
        List<Allocation> list = new ArrayList<>();
        String sql = "SELECT * FROM allocations WHERE donation_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, donationId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) list.add(mapRow(rs));
        } catch (SQLException e) { e.printStackTrace(); }
        return list;
    }

    // ── GET BY FUND ──────────────────────────────────────────────────────────
    public List<Allocation> getByFund(String fundId) {
        List<Allocation> list = new ArrayList<>();
        String sql = "SELECT * FROM allocations WHERE fund_id = ? ORDER BY created_at DESC";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, fundId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) list.add(mapRow(rs));
        } catch (SQLException e) { e.printStackTrace(); }
        return list;
    }

    // ── GET ALLOCATION CHECK (BALANCE VIEW) ──────────────────────────────────
    // Returns how much of a donation has been allocated vs remaining
    public AllocationCheck getAllocationCheck(String donationId) {
        String sql = "SELECT d.id AS donation_id, d.amount AS donation_amount, " +
                     "COALESCE(SUM(a.amount), 0) AS allocated_amount, " +
                     "(d.amount - COALESCE(SUM(a.amount), 0)) AS unallocated_amount " +
                     "FROM donations d " +
                     "LEFT JOIN allocations a ON d.id = a.donation_id " +
                     "WHERE d.id = ? " +
                     "GROUP BY d.id, d.amount";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, donationId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return new AllocationCheck(
                        rs.getString("donation_id"),
                        rs.getBigDecimal("donation_amount"),
                        rs.getBigDecimal("allocated_amount"),
                        rs.getBigDecimal("unallocated_amount")
                );
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return null;
    }

    // ── ADD ──────────────────────────────────────────────────────────────────
    public boolean addAllocation(Allocation a) {
        String sql = "INSERT INTO allocations (id, donation_id, fund_id, amount) VALUES (UUID(), ?, ?, ?)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, a.getDonationId());
            ps.setString(2, a.getFundId());
            ps.setBigDecimal(3, a.getAmount());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) { e.printStackTrace(); return false; }
    }

    // ── UPDATE ───────────────────────────────────────────────────────────────
    public boolean updateAllocation(Allocation a) {
        String sql = "UPDATE allocations SET fund_id = ?, amount = ? WHERE id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, a.getFundId());
            ps.setBigDecimal(2, a.getAmount());
            ps.setString(3, a.getId());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) { e.printStackTrace(); return false; }
    }

    // ── DELETE ───────────────────────────────────────────────────────────────
    public boolean deleteAllocation(String id) {
        String sql = "DELETE FROM allocations WHERE id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, id);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) { e.printStackTrace(); return false; }
    }

    // ── DELETE ALL FOR A DONATION ────────────────────────────────────────────
    public boolean deleteByDonation(String donationId) {
        String sql = "DELETE FROM allocations WHERE donation_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, donationId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) { e.printStackTrace(); return false; }
    }

    // ── ALLOCATE FUND TO CAMPAIGN ─────────────────────────────────────────────
    // Called from the Causes screen "Allocate Fund" button.
    // Links the allocation to the first completed donation for the campaign.
    // If no completed donation exists yet, falls back to the most recent donation
    // of any status. This avoids the FK violation while keeping the record traceable.
    public boolean addCampaignAllocation(String campaignId, String fundId, java.math.BigDecimal amount) {
        // Step 1: find a donation to attach this allocation to
        String findDonation =
            "SELECT id FROM donations WHERE campaign_id = ? AND status = 'completed' " +
            "ORDER BY donated_at DESC LIMIT 1";
        String donationId = null;
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(findDonation)) {
            ps.setString(1, campaignId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) donationId = rs.getString("id");
        } catch (SQLException e) { e.printStackTrace(); }

        // Fallback: any donation for this campaign
        if (donationId == null) {
            String fallback = "SELECT id FROM donations WHERE campaign_id = ? ORDER BY donated_at DESC LIMIT 1";
            try (Connection conn = DBConnection.getConnection();
                 PreparedStatement ps = conn.prepareStatement(fallback)) {
                ps.setString(1, campaignId);
                ResultSet rs = ps.executeQuery();
                if (rs.next()) donationId = rs.getString("id");
            } catch (SQLException e) { e.printStackTrace(); }
        }

        if (donationId == null) {
            System.err.println("addCampaignAllocation: no donations found for campaign " + campaignId
                + ". Cannot allocate — add at least one donation to this cause first.");
            return false;
        }

        // Step 2: insert the allocation
        String sql = "INSERT INTO allocations (id, donation_id, fund_id, amount) VALUES (UUID(), ?, ?, ?)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, donationId);
            ps.setString(2, fundId);
            ps.setBigDecimal(3, amount);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("addCampaignAllocation error: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    // ── GET TOTAL ALLOCATED FOR A CAMPAIGN ────────────────────────────────────
    // Returns how much from funds has been allocated to a campaign via its donations.
    public java.math.BigDecimal getTotalAllocatedForCampaign(String campaignId) {
        String sql =
            "SELECT COALESCE(SUM(a.amount), 0) AS total " +
            "FROM allocations a " +
            "JOIN donations d ON a.donation_id = d.id " +
            "WHERE d.campaign_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, campaignId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return rs.getBigDecimal("total");
        } catch (SQLException e) { e.printStackTrace(); }
        return java.math.BigDecimal.ZERO;
    }
}
