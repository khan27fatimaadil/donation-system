package dao;

import db.DBConnection;
import model.DonationStatusLog;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class DonationStatusLogDAO {

    // ── HELPER: MAP ROW ──────────────────────────────────────────────────────
    private DonationStatusLog mapRow(ResultSet rs) throws SQLException {
        DonationStatusLog log = new DonationStatusLog();
        log.setId(rs.getString("id"));
        log.setDonationId(rs.getString("donation_id"));
        String changedBy = rs.getString("changed_by");
        if (changedBy != null) log.setChangedBy(changedBy);
        String oldStatus = rs.getString("old_status");
        if (oldStatus != null)
            log.setOldStatus(DonationStatusLog.DonationStatus.valueOf(oldStatus));
        String newStatus = rs.getString("new_status");
        if (newStatus != null)
            log.setNewStatus(DonationStatusLog.DonationStatus.valueOf(newStatus));
        log.setNotes(rs.getString("notes"));
        log.setChangedAt(rs.getTimestamp("changed_at") != null
                ? rs.getTimestamp("changed_at").toLocalDateTime() : null);
        return log;
    }

    // ── GET ALL ──────────────────────────────────────────────────────────────
    public List<DonationStatusLog> getAllLogs() {
        List<DonationStatusLog> list = new ArrayList<>();
        String sql = "SELECT * FROM donation_status_logs ORDER BY changed_at DESC";
        try (Connection conn = DBConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) list.add(mapRow(rs));
        } catch (SQLException e) { e.printStackTrace(); }
        return list;
    }

    // ── GET BY DONATION ──────────────────────────────────────────────────────
    public List<DonationStatusLog> getByDonation(String donationId) {
        List<DonationStatusLog> list = new ArrayList<>();
        String sql = "SELECT * FROM donation_status_logs WHERE donation_id = ? ORDER BY changed_at ASC";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, donationId.toString());
            ResultSet rs = ps.executeQuery();
            while (rs.next()) list.add(mapRow(rs));
        } catch (SQLException e) { e.printStackTrace(); }
        return list;
    }

    // ── GET LATEST STATUS FOR DONATION ───────────────────────────────────────
    public DonationStatusLog getLatest(String donationId) {
        String sql = "SELECT * FROM donation_status_logs WHERE donation_id = ? " +
                     "ORDER BY changed_at DESC LIMIT 1";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, donationId.toString());
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return mapRow(rs);
        } catch (SQLException e) { e.printStackTrace(); }
        return null;
    }

    // ── ADD ──────────────────────────────────────────────────────────────────
    public boolean addLog(DonationStatusLog log) {
        String sql = "INSERT INTO donation_status_logs " +
                     "(id, donation_id, changed_by, old_status, new_status, notes, changed_at) " +
                     "VALUES (UUID(), ?, ?, ?, ?, ?, NOW())";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, log.getDonationId().toString());
            ps.setString(2, log.getChangedBy() != null ? log.getChangedBy().toString() : null);
            ps.setString(3, log.getOldStatus() != null ? log.getOldStatus().name() : null);
            ps.setString(4, log.getNewStatus() != null ? log.getNewStatus().name() : null);
            ps.setString(5, log.getNotes());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) { e.printStackTrace(); return false; }
    }

    // ── DELETE BY DONATION ───────────────────────────────────────────────────
    public boolean deleteByDonation(String donationId) {
        String sql = "DELETE FROM donation_status_logs WHERE donation_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, donationId.toString());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) { e.printStackTrace(); return false; }
    }
}
