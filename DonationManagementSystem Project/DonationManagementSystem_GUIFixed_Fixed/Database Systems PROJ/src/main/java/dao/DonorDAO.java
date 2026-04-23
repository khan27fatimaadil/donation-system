package dao;

import db.DBConnection;
import model.Donor;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class DonorDAO {

    // ── HELPER: MAP ROW ──────────────────────────────────────────────────────
    private Donor mapRow(ResultSet rs) throws SQLException {
        Donor d = new Donor();
        d.setId(rs.getString("id"));
        String firstName = rs.getString("first_name");
        d.setFirstName(firstName != null ? firstName : "");
        d.setLastName(rs.getString("last_name"));  // null is fine — handled in display
        d.setOrganizationName(rs.getString("organization_name"));
        String email = rs.getString("email");
        d.setEmail(email != null ? email : "");
        d.setPhone(rs.getString("phone"));
        String type = rs.getString("type");
        d.setType(type != null ? type : "individual");
        d.setAddress(rs.getString("address"));
        d.setCity(rs.getString("city"));
        d.setCountryCode(rs.getString("country_code"));
        d.setActive(rs.getBoolean("is_active"));
        d.setCreatedAt(rs.getTimestamp("created_at") != null
                ? rs.getTimestamp("created_at").toLocalDateTime() : null);
        d.setUpdatedAt(rs.getTimestamp("updated_at") != null
                ? rs.getTimestamp("updated_at").toLocalDateTime() : null);
        return d;
    }

    // ── GET ALL ──────────────────────────────────────────────────────────────
    public List<Donor> getAllDonors() {
        List<Donor> list = new ArrayList<>();
        String sql = "SELECT * FROM donors ORDER BY last_name, first_name";
        try (Connection conn = DBConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) list.add(mapRow(rs));
        } catch (SQLException e) { e.printStackTrace(); }
        return list;
    }

    // ── GET ACTIVE ONLY ──────────────────────────────────────────────────────
    public List<Donor> getActiveDonors() {
        List<Donor> list = new ArrayList<>();
        String sql = "SELECT * FROM donors WHERE is_active = true ORDER BY last_name, first_name";
        try (Connection conn = DBConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) list.add(mapRow(rs));
        } catch (SQLException e) { e.printStackTrace(); }
        return list;
    }

    // ── GET BY ID ────────────────────────────────────────────────────────────
    public Donor getById(String id) {
        String sql = "SELECT * FROM donors WHERE id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, id);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return mapRow(rs);
        } catch (SQLException e) { e.printStackTrace(); }
        return null;
    }

    // ── SEARCH BY NAME ───────────────────────────────────────────────────────
    public List<Donor> searchByName(String keyword) {
        List<Donor> list = new ArrayList<>();
        String sql = "SELECT * FROM donors WHERE first_name LIKE ? OR last_name LIKE ? " +
                     "OR organization_name LIKE ? ORDER BY last_name, first_name";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            String kw = "%" + keyword + "%";
            ps.setString(1, kw);
            ps.setString(2, kw);
            ps.setString(3, kw);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) list.add(mapRow(rs));
        } catch (SQLException e) { e.printStackTrace(); }
        return list;
    }

    // ── SEARCH BY EMAIL ──────────────────────────────────────────────────────
    public Donor getByEmail(String email) {
        String sql = "SELECT * FROM donors WHERE email = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, email);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return mapRow(rs);
        } catch (SQLException e) { e.printStackTrace(); }
        return null;
    }

    // ── ADD ──────────────────────────────────────────────────────────────────
    public boolean addDonor(Donor d) {
        String sql = "INSERT INTO donors (id, first_name, last_name, organization_name, email, " +
                     "phone, type, address, city, country_code, is_active) " +
                     "VALUES (UUID(), ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, d.getFirstName());
            ps.setString(2, d.getLastName());
            ps.setString(3, d.getOrganizationName());
            ps.setString(4, d.getEmail());
            ps.setString(5, d.getPhone());
            ps.setString(6, d.getType());
            ps.setString(7, d.getAddress());
            ps.setString(8, d.getCity());
            ps.setString(9, d.getCountryCode());
            ps.setBoolean(10, d.isActive());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) { e.printStackTrace(); return false; }
    }

    // ── UPDATE ───────────────────────────────────────────────────────────────
    public boolean updateDonor(Donor d) {
        String sql = "UPDATE donors SET first_name = ?, last_name = ?, organization_name = ?, " +
                     "email = ?, phone = ?, type = ?, address = ?, city = ?, " +
                     "country_code = ?, is_active = ?, updated_at = NOW() WHERE id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, d.getFirstName());
            ps.setString(2, d.getLastName());
            ps.setString(3, d.getOrganizationName());
            ps.setString(4, d.getEmail());
            ps.setString(5, d.getPhone());
            ps.setString(6, d.getType());
            ps.setString(7, d.getAddress());
            ps.setString(8, d.getCity());
            ps.setString(9, d.getCountryCode());
            ps.setBoolean(10, d.isActive());
            ps.setString(11, d.getId());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) { e.printStackTrace(); return false; }
    }

    // ── DELETE ──────────────────────────────────────────────────────────────
    public boolean deleteDonor(String id) {
        String sql = "DELETE FROM donors WHERE id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, id);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) { e.printStackTrace(); return false; }
    }
}
