package dao;

import db.DBConnection;
import model.Tag;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class TagDAO {

    // ── HELPER: MAP ROW ──────────────────────────────────────────────────────
    private Tag mapRow(ResultSet rs) throws SQLException {
        Tag t = new Tag();
        t.setId(rs.getString("id"));
        t.setName(rs.getString("name"));
        t.setColor(rs.getString("color"));
        t.setCreatedAt(rs.getTimestamp("created_at") != null
                ? rs.getTimestamp("created_at").toLocalDateTime() : null);
        return t;
    }

    // ── GET ALL ──────────────────────────────────────────────────────────────
    public List<Tag> getAllTags() {
        List<Tag> list = new ArrayList<>();
        String sql = "SELECT * FROM tags ORDER BY name";
        try (Connection conn = DBConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) list.add(mapRow(rs));
        } catch (SQLException e) { e.printStackTrace(); }
        return list;
    }

    // ── GET BY ID ────────────────────────────────────────────────────────────
    public Tag getById(String id) {
        String sql = "SELECT * FROM tags WHERE id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, id.toString());
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return mapRow(rs);
        } catch (SQLException e) { e.printStackTrace(); }
        return null;
    }

    // ── SEARCH BY NAME ───────────────────────────────────────────────────────
    public List<Tag> searchByName(String keyword) {
        List<Tag> list = new ArrayList<>();
        String sql = "SELECT * FROM tags WHERE name LIKE ? ORDER BY name";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, "%" + keyword + "%");
            ResultSet rs = ps.executeQuery();
            while (rs.next()) list.add(mapRow(rs));
        } catch (SQLException e) { e.printStackTrace(); }
        return list;
    }

    // ── ADD ──────────────────────────────────────────────────────────────────
    public boolean addTag(Tag t) {
        String sql = "INSERT INTO tags (id, name, color) VALUES (?, ?, ?)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            String newId = t.getId() != null ? t.getId().toString() : java.util.UUID.randomUUID().toString();
            ps.setString(1, newId);
            ps.setString(2, t.getName());
            ps.setString(3, t.getColor());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) { e.printStackTrace(); return false; }
    }

    // ── UPDATE ───────────────────────────────────────────────────────────────
    public boolean updateTag(Tag t) {
        String sql = "UPDATE tags SET name = ?, color = ? WHERE id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, t.getName());
            ps.setString(2, t.getColor());
            ps.setString(3, t.getId().toString());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) { e.printStackTrace(); return false; }
    }

    // ── DELETE ───────────────────────────────────────────────────────────────
    public boolean deleteTag(String id) {
        String sql = "DELETE FROM tags WHERE id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, id.toString());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) { e.printStackTrace(); return false; }
    }
}
