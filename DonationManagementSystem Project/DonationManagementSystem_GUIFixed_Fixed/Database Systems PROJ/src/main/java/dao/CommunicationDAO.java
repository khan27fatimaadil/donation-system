package dao;

import db.DBConnection;
import model.Communication;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class CommunicationDAO {

    // ── HELPER: MAP ROW ──────────────────────────────────────────────────────
    private Communication mapRow(ResultSet rs) throws SQLException {
        Communication c = new Communication();
        c.setId(rs.getString("id"));
        c.setDonorId(rs.getString("donor_id"));
        String donationId = rs.getString("donation_id");
        if (donationId != null) c.setDonationId(donationId);
        String sentBy = rs.getString("sent_by");
        if (sentBy != null) c.setSentBy(sentBy);
        String channelStr = rs.getString("channel");
        if (channelStr != null) c.setChannel(Communication.Channel.valueOf(channelStr));
        c.setSubject(rs.getString("subject"));
        c.setBody(rs.getString("body"));
        String statusStr = rs.getString("status");
        if (statusStr != null) c.setStatus(Communication.CommStatus.valueOf(statusStr));
        c.setSentAt(rs.getTimestamp("sent_at") != null
                ? rs.getTimestamp("sent_at").toLocalDateTime() : null);
        c.setCreatedAt(rs.getTimestamp("created_at") != null
                ? rs.getTimestamp("created_at").toLocalDateTime() : null);
        c.setUpdatedAt(rs.getTimestamp("updated_at") != null
                ? rs.getTimestamp("updated_at").toLocalDateTime() : null);
        return c;
    }

    // ── GET ALL ──────────────────────────────────────────────────────────────
    public List<Communication> getAllCommunications() {
        List<Communication> list = new ArrayList<>();
        String sql = "SELECT * FROM communications ORDER BY created_at DESC";
        try (Connection conn = DBConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) list.add(mapRow(rs));
        } catch (SQLException e) { e.printStackTrace(); }
        return list;
    }

    // ── GET BY ID ────────────────────────────────────────────────────────────
    public Communication getById(String id) {
        String sql = "SELECT * FROM communications WHERE id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, id.toString());
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return mapRow(rs);
        } catch (SQLException e) { e.printStackTrace(); }
        return null;
    }

    // ── GET BY DONOR ─────────────────────────────────────────────────────────
    public List<Communication> getByDonor(String donorId) {
        List<Communication> list = new ArrayList<>();
        String sql = "SELECT * FROM communications WHERE donor_id = ? ORDER BY created_at DESC";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, donorId.toString());
            ResultSet rs = ps.executeQuery();
            while (rs.next()) list.add(mapRow(rs));
        } catch (SQLException e) { e.printStackTrace(); }
        return list;
    }

    // ── GET BY DONATION ──────────────────────────────────────────────────────
    public List<Communication> getByDonation(String donationId) {
        List<Communication> list = new ArrayList<>();
        String sql = "SELECT * FROM communications WHERE donation_id = ? ORDER BY created_at DESC";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, donationId.toString());
            ResultSet rs = ps.executeQuery();
            while (rs.next()) list.add(mapRow(rs));
        } catch (SQLException e) { e.printStackTrace(); }
        return list;
    }

    // ── GET BY STATUS ────────────────────────────────────────────────────────
    public List<Communication> getByStatus(Communication.CommStatus status) {
        List<Communication> list = new ArrayList<>();
        String sql = "SELECT * FROM communications WHERE status = ? ORDER BY created_at DESC";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, status.name());
            ResultSet rs = ps.executeQuery();
            while (rs.next()) list.add(mapRow(rs));
        } catch (SQLException e) { e.printStackTrace(); }
        return list;
    }

    // ── ADD ──────────────────────────────────────────────────────────────────
    public boolean addCommunication(Communication c) {
        String sql = "INSERT INTO communications " +
                     "(id, donor_id, donation_id, sent_by, channel, subject, body, status, sent_at) " +
                     "VALUES (UUID(), ?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, c.getDonorId().toString());
            ps.setString(2, c.getDonationId() != null ? c.getDonationId().toString() : null);
            ps.setString(3, c.getSentBy() != null ? c.getSentBy().toString() : null);
            ps.setString(4, c.getChannel() != null ? c.getChannel().name() : null);
            ps.setString(5, c.getSubject());
            ps.setString(6, c.getBody());
            ps.setString(7, c.getStatus() != null ? c.getStatus().name() : "draft");
            ps.setTimestamp(8, c.getSentAt() != null ? Timestamp.valueOf(c.getSentAt()) : null);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) { e.printStackTrace(); return false; }
    }

    // ── UPDATE STATUS ────────────────────────────────────────────────────────
    public boolean updateStatus(String id, Communication.CommStatus newStatus) {
        String sql = "UPDATE communications SET status = ?, updated_at = NOW() WHERE id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, newStatus.name());
            ps.setString(2, id.toString());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) { e.printStackTrace(); return false; }
    }

    // ── UPDATE ───────────────────────────────────────────────────────────────
    public boolean updateCommunication(Communication c) {
        String sql = "UPDATE communications SET subject = ?, body = ?, channel = ?, " +
                     "status = ?, sent_at = ?, updated_at = NOW() WHERE id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, c.getSubject());
            ps.setString(2, c.getBody());
            ps.setString(3, c.getChannel() != null ? c.getChannel().name() : null);
            ps.setString(4, c.getStatus() != null ? c.getStatus().name() : null);
            ps.setTimestamp(5, c.getSentAt() != null ? Timestamp.valueOf(c.getSentAt()) : null);
            ps.setString(6, c.getId().toString());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) { e.printStackTrace(); return false; }
    }

    // ── DELETE ───────────────────────────────────────────────────────────────
    public boolean deleteCommunication(String id) {
        String sql = "DELETE FROM communications WHERE id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, id.toString());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) { e.printStackTrace(); return false; }
    }
}
