package dao;

import db.DBConnection;
import model.Fund;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class FundDAO {

    // ── HELPER: MAP ROW ──────────────────────────────────────────────────────
    private Fund mapRow(ResultSet rs) throws SQLException {
        Fund f = new Fund();
        f.setId(rs.getString("id"));
        f.setName(rs.getString("name"));
        f.setDescription(rs.getString("description"));
        String typeStr = rs.getString("type");
        if (typeStr != null) f.setType(Fund.FundType.valueOf(typeStr));
        f.setActive(rs.getBoolean("is_active"));
        f.setCreatedAt(rs.getTimestamp("created_at") != null
                ? rs.getTimestamp("created_at").toLocalDateTime() : null);
        f.setUpdatedAt(rs.getTimestamp("updated_at") != null
                ? rs.getTimestamp("updated_at").toLocalDateTime() : null);
        return f;
    }

    // ── GET ALL ──────────────────────────────────────────────────────────────
    public List<Fund> getAllFunds() {
        List<Fund> list = new ArrayList<>();
        String sql = "SELECT * FROM funds ORDER BY name";
        try (Connection conn = DBConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) list.add(mapRow(rs));
        } catch (SQLException e) { e.printStackTrace(); }
        return list;
    }

    // ── GET ACTIVE ONLY ──────────────────────────────────────────────────────
    public List<Fund> getActiveFunds() {
        List<Fund> list = new ArrayList<>();
        String sql = "SELECT * FROM funds WHERE is_active = true ORDER BY name";
        try (Connection conn = DBConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) list.add(mapRow(rs));
        } catch (SQLException e) { e.printStackTrace(); }
        return list;
    }

    // ── GET BY ID ────────────────────────────────────────────────────────────
    public Fund getById(String id) {
        String sql = "SELECT * FROM funds WHERE id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, id);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return mapRow(rs);
        } catch (SQLException e) { e.printStackTrace(); }
        return null;
    }

    // ── ADD ──────────────────────────────────────────────────────────────────
    public boolean addFund(Fund f) {
        String sql = "INSERT INTO funds (id, name, description, type, is_active) VALUES (?, ?, ?, ?, ?)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            String newId = f.getId() != null ? f.getId().toString() : java.util.UUID.randomUUID().toString();
            ps.setString(1, newId);
            ps.setString(2, f.getName());
            ps.setString(3, f.getDescription());
            ps.setString(4, f.getType() != null ? f.getType().name() : null);
            ps.setBoolean(5, f.isActive());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) { e.printStackTrace(); return false; }
    }

    // ── UPDATE ───────────────────────────────────────────────────────────────
    public boolean updateFund(Fund f) {
        String sql = "UPDATE funds SET name = ?, description = ?, type = ?, is_active = ?, " +
                     "updated_at = NOW() WHERE id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, f.getName());
            ps.setString(2, f.getDescription());
            ps.setString(3, f.getType() != null ? f.getType().name() : null);
            ps.setBoolean(4, f.isActive());
            ps.setString(5, f.getId());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) { e.printStackTrace(); return false; }
    }

    // ── DELETE ───────────────────────────────────────────────────────────────
    public boolean deleteFund(String id) {
        String sql = "DELETE FROM funds WHERE id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, id);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) { e.printStackTrace(); return false; }
    }
}
