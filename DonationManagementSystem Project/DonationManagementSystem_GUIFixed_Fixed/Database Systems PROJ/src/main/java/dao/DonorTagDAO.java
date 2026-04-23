package dao;

import db.DBConnection;
import model.DonorTag;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class DonorTagDAO {

    // ── HELPER: MAP ROW ──────────────────────────────────────────────────────
    private DonorTag mapRow(ResultSet rs) throws SQLException {
        DonorTag dt = new DonorTag();
        dt.setDonorId(rs.getString("donor_id"));
        dt.setTagId(rs.getString("tag_id"));
        dt.setAssignedAt(rs.getTimestamp("assigned_at") != null
                ? rs.getTimestamp("assigned_at").toLocalDateTime() : null);
        return dt;
    }

    // ── GET ALL TAGS FOR A DONOR ─────────────────────────────────────────────
    public List<DonorTag> getByDonor(String donorId) {
        List<DonorTag> list = new ArrayList<>();
        String sql = "SELECT * FROM donor_tags WHERE donor_id = ? ORDER BY assigned_at DESC";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, donorId.toString());
            ResultSet rs = ps.executeQuery();
            while (rs.next()) list.add(mapRow(rs));
        } catch (SQLException e) { e.printStackTrace(); }
        return list;
    }

    // ── GET ALL DONORS WITH A TAG ────────────────────────────────────────────
    public List<DonorTag> getByTag(String tagId) {
        List<DonorTag> list = new ArrayList<>();
        String sql = "SELECT * FROM donor_tags WHERE tag_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, tagId.toString());
            ResultSet rs = ps.executeQuery();
            while (rs.next()) list.add(mapRow(rs));
        } catch (SQLException e) { e.printStackTrace(); }
        return list;
    }

    // ── GET ALL ──────────────────────────────────────────────────────────────
    public List<DonorTag> getAllDonorTags() {
        List<DonorTag> list = new ArrayList<>();
        String sql = "SELECT * FROM donor_tags ORDER BY assigned_at DESC";
        try (Connection conn = DBConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) list.add(mapRow(rs));
        } catch (SQLException e) { e.printStackTrace(); }
        return list;
    }

    // ── CHECK IF EXISTS ──────────────────────────────────────────────────────
    public boolean exists(String donorId, String tagId) {
        String sql = "SELECT 1 FROM donor_tags WHERE donor_id = ? AND tag_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, donorId.toString());
            ps.setString(2, tagId.toString());
            ResultSet rs = ps.executeQuery();
            return rs.next();
        } catch (SQLException e) { e.printStackTrace(); return false; }
    }

    // ── ADD ──────────────────────────────────────────────────────────────────
    public boolean addDonorTag(String donorId, String tagId) {
        String sql = "INSERT INTO donor_tags (donor_id, tag_id, assigned_at) VALUES (?, ?, NOW())";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, donorId.toString());
            ps.setString(2, tagId.toString());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) { e.printStackTrace(); return false; }
    }

    // ── DELETE ONE TAG FROM DONOR ────────────────────────────────────────────
    public boolean deleteDonorTag(String donorId, String tagId) {
        String sql = "DELETE FROM donor_tags WHERE donor_id = ? AND tag_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, donorId.toString());
            ps.setString(2, tagId.toString());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) { e.printStackTrace(); return false; }
    }

    // ── DELETE ALL TAGS FROM A DONOR ─────────────────────────────────────────
    public boolean deleteAllTagsForDonor(String donorId) {
        String sql = "DELETE FROM donor_tags WHERE donor_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, donorId.toString());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) { e.printStackTrace(); return false; }
    }
}
