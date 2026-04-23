package dao;

import db.DBConnection;
import model.Campaign;
import model.Organization;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class CampaignDAO {

    // ── HELPER: MAP ROW ──────────────────────────────────────────────────────
    private Campaign mapRow(ResultSet rs) throws SQLException {
        Campaign c = new Campaign();
        c.setId(rs.getString("id"));
        c.setOrganizationId(rs.getString("organization_id"));
        c.setTitle(rs.getString("title"));
        c.setDescription(rs.getString("description"));
        c.setGoalAmount(rs.getBigDecimal("goal_amount"));
        c.setCurrencyCode(rs.getString("currency_code"));
        c.setStartDate(rs.getDate("start_date") != null
                ? rs.getDate("start_date").toLocalDate() : null);
        c.setEndDate(rs.getDate("end_date") != null
                ? rs.getDate("end_date").toLocalDate() : null);
        String statusStr = rs.getString("status");
        if (statusStr != null && !statusStr.trim().isEmpty()) {
            try { c.setStatus(Campaign.Status.valueOf(statusStr)); }
            catch (IllegalArgumentException ex) { /* leave null if unrecognised value */ }
        }
        c.setImageUrl(rs.getString("image_url"));
        c.setCreatedAt(rs.getTimestamp("created_at") != null
                ? rs.getTimestamp("created_at").toLocalDateTime() : null);
        c.setUpdatedAt(rs.getTimestamp("updated_at") != null
                ? rs.getTimestamp("updated_at").toLocalDateTime() : null);
        return c;
    }

    // ── GET ALL ──────────────────────────────────────────────────────────────
    public List<Campaign> getAllCampaigns() {
        List<Campaign> list = new ArrayList<>();
        String sql = "SELECT * FROM campaigns ORDER BY title";
        try (Connection conn = DBConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) list.add(mapRow(rs));
        } catch (SQLException e) { e.printStackTrace(); }
        return list;
    }

    // ── GET ACTIVE ───────────────────────────────────────────────────────────
    public List<Campaign> getActiveCampaigns() {
        List<Campaign> list = new ArrayList<>();
        String sql = "SELECT * FROM campaigns WHERE status = 'active' ORDER BY title";
        try (Connection conn = DBConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) list.add(mapRow(rs));
        } catch (SQLException e) { e.printStackTrace(); }
        return list;
    }

    // ── GET BY ID ────────────────────────────────────────────────────────────
    public Campaign getById(String id) {
        String sql = "SELECT * FROM campaigns WHERE id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, id);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return mapRow(rs);
        } catch (SQLException e) { e.printStackTrace(); }
        return null;
    }

    // ── GET BY ORGANIZATION ──────────────────────────────────────────────────
    public List<Campaign> getByOrganization(String organizationId) {
        List<Campaign> list = new ArrayList<>();
        String sql = "SELECT * FROM campaigns WHERE organization_id = ? ORDER BY title";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, organizationId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) list.add(mapRow(rs));
        } catch (SQLException e) { e.printStackTrace(); }
        return list;
    }

    // ── SEARCH BY TITLE ──────────────────────────────────────────────────────
    public List<Campaign> searchByTitle(String keyword) {
        List<Campaign> list = new ArrayList<>();
        String sql = "SELECT * FROM campaigns WHERE title LIKE ? ORDER BY title";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, "%" + keyword + "%");
            ResultSet rs = ps.executeQuery();
            while (rs.next()) list.add(mapRow(rs));
        } catch (SQLException e) { e.printStackTrace(); }
        return list;
    }

    // ── ADD ──────────────────────────────────────────────────────────────────
    public boolean addCampaign(Campaign c) {
        String sql = "INSERT INTO campaigns (id, organization_id, title, description, goal_amount, " +
                     "currency_code, start_date, end_date, status, image_url) " +
                     "VALUES (UUID(), ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        // Ensure mandatory organization_id exists
        if (c.getOrganizationId() == null || c.getOrganizationId().isEmpty()) {
             List<Organization> orgs = new OrganizationDAO().getAllOrganizations();
             if (!orgs.isEmpty()) c.setOrganizationId(orgs.get(0).getId());
        }
        // Ensure mandatory currency
        if (c.getCurrencyCode() == null || c.getCurrencyCode().isEmpty()) {
            c.setCurrencyCode("PKR");
        }

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, c.getOrganizationId());
            ps.setString(2, c.getTitle());
            ps.setString(3, c.getDescription());
            ps.setBigDecimal(4, c.getGoalAmount());
            ps.setString(5, c.getCurrencyCode());
            ps.setDate(6, c.getStartDate() != null ? Date.valueOf(c.getStartDate()) : null);
            ps.setDate(7, c.getEndDate() != null ? Date.valueOf(c.getEndDate()) : null);
            ps.setString(8, c.getStatus() != null ? c.getStatus().name() : "draft");
            ps.setString(9, c.getImageUrl());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) { e.printStackTrace(); return false; }
    }

    // ── UPDATE ───────────────────────────────────────────────────────────────
    public boolean updateCampaign(Campaign c) {
        String sql = "UPDATE campaigns SET organization_id = ?, title = ?, description = ?, " +
                     "goal_amount = ?, currency_code = ?, start_date = ?, end_date = ?, " +
                     "status = ?, image_url = ?, updated_at = NOW() WHERE id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, c.getOrganizationId());
            ps.setString(2, c.getTitle());
            ps.setString(3, c.getDescription());
            ps.setBigDecimal(4, c.getGoalAmount());
            ps.setString(5, c.getCurrencyCode());
            ps.setDate(6, c.getStartDate() != null ? Date.valueOf(c.getStartDate()) : null);
            ps.setDate(7, c.getEndDate() != null ? Date.valueOf(c.getEndDate()) : null);
            ps.setString(8, c.getStatus() != null ? c.getStatus().name() : null);
            ps.setString(9, c.getImageUrl());
            ps.setString(10, c.getId());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) { e.printStackTrace(); return false; }
    }

    // ── UPDATE STATUS ONLY ───────────────────────────────────────────────────
    public boolean updateStatus(String id, Campaign.Status newStatus) {
        String sql = "UPDATE campaigns SET status = ?, updated_at = NOW() WHERE id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, newStatus.name());
            ps.setString(2, id);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) { e.printStackTrace(); return false; }
    }

    // ── DELETE ──────────────────────────────────────────────────────────────
    public boolean deleteCampaign(String id) {
        String sql = "DELETE FROM campaigns WHERE id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, id);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) { e.printStackTrace(); return false; }
    }
}
