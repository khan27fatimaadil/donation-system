package dao;

import db.DBConnection;
import model.CampaignStatusLog;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class CampaignStatusLogDAO {

    // ── HELPER: MAP ROW ──────────────────────────────────────────────────────
    private CampaignStatusLog mapRow(ResultSet rs) throws SQLException {
        CampaignStatusLog log = new CampaignStatusLog();
        log.setId(rs.getString("id"));
        log.setCampaignId(rs.getString("campaign_id"));
        String changedBy = rs.getString("changed_by");
        if (changedBy != null) log.setChangedBy(changedBy);
        String oldStatus = rs.getString("old_status");
        if (oldStatus != null)
            log.setOldStatus(CampaignStatusLog.CampaignStatus.valueOf(oldStatus));
        String newStatus = rs.getString("new_status");
        if (newStatus != null)
            log.setNewStatus(CampaignStatusLog.CampaignStatus.valueOf(newStatus));
        log.setNotes(rs.getString("notes"));
        log.setChangedAt(rs.getTimestamp("changed_at") != null
                ? rs.getTimestamp("changed_at").toLocalDateTime() : null);
        return log;
    }

    // ── GET ALL ──────────────────────────────────────────────────────────────
    public List<CampaignStatusLog> getAllLogs() {
        List<CampaignStatusLog> list = new ArrayList<>();
        String sql = "SELECT * FROM campaign_status_logs ORDER BY changed_at DESC";
        try (Connection conn = DBConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) list.add(mapRow(rs));
        } catch (SQLException e) { e.printStackTrace(); }
        return list;
    }

    // ── GET BY CAMPAIGN ──────────────────────────────────────────────────────
    public List<CampaignStatusLog> getByCampaign(String campaignId) {
        List<CampaignStatusLog> list = new ArrayList<>();
        String sql = "SELECT * FROM campaign_status_logs WHERE campaign_id = ? ORDER BY changed_at ASC";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, campaignId.toString());
            ResultSet rs = ps.executeQuery();
            while (rs.next()) list.add(mapRow(rs));
        } catch (SQLException e) { e.printStackTrace(); }
        return list;
    }

    // ── GET LATEST STATUS FOR CAMPAIGN ───────────────────────────────────────
    public CampaignStatusLog getLatest(String campaignId) {
        String sql = "SELECT * FROM campaign_status_logs WHERE campaign_id = ? " +
                     "ORDER BY changed_at DESC LIMIT 1";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, campaignId.toString());
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return mapRow(rs);
        } catch (SQLException e) { e.printStackTrace(); }
        return null;
    }

    // ── ADD ──────────────────────────────────────────────────────────────────
    public boolean addLog(CampaignStatusLog log) {
        String sql = "INSERT INTO campaign_status_logs " +
                     "(id, campaign_id, changed_by, old_status, new_status, notes, changed_at) " +
                     "VALUES (UUID(), ?, ?, ?, ?, ?, NOW())";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, log.getCampaignId().toString());
            ps.setString(2, log.getChangedBy() != null ? log.getChangedBy().toString() : null);
            ps.setString(3, log.getOldStatus() != null ? log.getOldStatus().name() : null);
            ps.setString(4, log.getNewStatus() != null ? log.getNewStatus().name() : null);
            ps.setString(5, log.getNotes());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) { e.printStackTrace(); return false; }
    }

    // ── DELETE BY CAMPAIGN ───────────────────────────────────────────────────
    public boolean deleteByCampaign(String campaignId) {
        String sql = "DELETE FROM campaign_status_logs WHERE campaign_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, campaignId.toString());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) { e.printStackTrace(); return false; }
    }
}
