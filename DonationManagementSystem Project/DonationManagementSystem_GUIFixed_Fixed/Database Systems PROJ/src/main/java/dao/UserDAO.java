package dao;

import db.DBConnection;
import model.User;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class UserDAO {

    // ── HELPER: MAP ROW ──────────────────────────────────────────────────────
    private User mapRow(ResultSet rs) throws SQLException {
        User u = new User();
        u.setId(rs.getString("id"));
        String orgId = rs.getString("organization_id");
        if (orgId != null) u.setOrganizationId(orgId);
        u.setFirstName(rs.getString("first_name"));
        u.setLastName(rs.getString("last_name"));
        u.setEmail(rs.getString("email"));
        String roleStr = rs.getString("role");
        if (roleStr != null) u.setRole(User.UserRole.valueOf(roleStr));
        u.setActive(rs.getBoolean("is_active"));
        u.setCreatedAt(rs.getTimestamp("created_at") != null
                ? rs.getTimestamp("created_at").toLocalDateTime() : null);
        u.setUpdatedAt(rs.getTimestamp("updated_at") != null
                ? rs.getTimestamp("updated_at").toLocalDateTime() : null);
        return u;
    }

    // ── GET ALL ──────────────────────────────────────────────────────────────
    public List<User> getAllUsers() {
        List<User> list = new ArrayList<>();
        String sql = "SELECT * FROM users ORDER BY last_name, first_name";
        try (Connection conn = DBConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) list.add(mapRow(rs));
        } catch (SQLException e) { e.printStackTrace(); }
        return list;
    }

    // ── GET BY ID ────────────────────────────────────────────────────────────
    public User getById(String id) {
        String sql = "SELECT * FROM users WHERE id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, id.toString());
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return mapRow(rs);
        } catch (SQLException e) { e.printStackTrace(); }
        return null;
    }

    // ── GET BY EMAIL ─────────────────────────────────────────────────────────
    public User getByEmail(String email) {
        String sql = "SELECT * FROM users WHERE email = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, email);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return mapRow(rs);
        } catch (SQLException e) { e.printStackTrace(); }
        return null;
    }

    // ── LOGIN / AUTHENTICATE ─────────────────────────────────────────────────
    // Used for the login screen: UserDAO.getByCredentials(email, password)
    // NOTE: password_hash column must exist in your users table.
        public User getByCredentials(String email, String passwordHash) {
        String sql = "SELECT * FROM users WHERE email = ? AND password_hash = ? AND is_active = true";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, email);
            ps.setString(2, passwordHash);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return mapRow(rs);
        } catch (SQLException e) { e.printStackTrace(); }
        return null;
    }

    // ── GET BY ORGANIZATION ──────────────────────────────────────────────────
    public List<User> getByOrganization(String organizationId) {
        List<User> list = new ArrayList<>();
        String sql = "SELECT * FROM users WHERE organization_id = ? ORDER BY last_name, first_name";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, organizationId.toString());
            ResultSet rs = ps.executeQuery();
            while (rs.next()) list.add(mapRow(rs));
        } catch (SQLException e) { e.printStackTrace(); }
        return list;
    }

    // ── ADD ──────────────────────────────────────────────────────────────────
    public boolean addUser(User u, String passwordHash) {
        String sql = "INSERT INTO users (id, organization_id, first_name, last_name, email, " +
                     "role, is_active, password_hash) VALUES (UUID(), ?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, u.getOrganizationId() != null ? u.getOrganizationId().toString() : null);
            ps.setString(2, u.getFirstName());
            ps.setString(3, u.getLastName());
            ps.setString(4, u.getEmail());
            ps.setString(5, u.getRole() != null ? u.getRole().name() : "fundraiser");
            ps.setBoolean(6, u.isActive());
            ps.setString(7, passwordHash);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) { e.printStackTrace(); return false; }
    }

    // ── UPDATE ───────────────────────────────────────────────────────────────
    public boolean updateUser(User u) {
        String sql = "UPDATE users SET organization_id = ?, first_name = ?, last_name = ?, " +
                     "email = ?, role = ?, is_active = ?, updated_at = NOW() WHERE id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, u.getOrganizationId() != null ? u.getOrganizationId().toString() : null);
            ps.setString(2, u.getFirstName());
            ps.setString(3, u.getLastName());
            ps.setString(4, u.getEmail());
            ps.setString(5, u.getRole() != null ? u.getRole().name() : null);
            ps.setBoolean(6, u.isActive());
            ps.setString(7, u.getId().toString());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) { e.printStackTrace(); return false; }
    }

    // ── UPDATE PASSWORD ──────────────────────────────────────────────────────
    public boolean updatePassword(String userId, String newPasswordHash) {
        String sql = "UPDATE users SET password_hash = ?, updated_at = NOW() WHERE id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, newPasswordHash);
            ps.setString(2, userId.toString());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) { e.printStackTrace(); return false; }
    }

    // ── DEACTIVATE (SOFT DELETE) ─────────────────────────────────────────────
    public boolean deactivateUser(String id) {
        String sql = "UPDATE users SET is_active = false, updated_at = NOW() WHERE id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, id.toString());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) { e.printStackTrace(); return false; }
    }

    // ── HARD DELETE ──────────────────────────────────────────────────────────
    public boolean deleteUser(String id) {
        String sql = "DELETE FROM users WHERE id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, id.toString());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) { e.printStackTrace(); return false; }
    }
}
