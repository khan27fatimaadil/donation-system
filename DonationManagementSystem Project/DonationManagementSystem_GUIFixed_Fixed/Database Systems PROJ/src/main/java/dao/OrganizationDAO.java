package dao;

import db.DBConnection;
import model.Organization;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class OrganizationDAO {

    // ── HELPER: MAP ROW ──────────────────────────────────────────────────────
    private Organization mapRow(ResultSet rs) throws SQLException {
        Organization o = new Organization();
        o.setId(rs.getString("id"));
        o.setName(rs.getString("name"));
        o.setTaxId(rs.getString("tax_id"));
        o.setContactEmail(rs.getString("contact_email"));
        o.setAddress(rs.getString("address"));
        o.setPhone(rs.getString("phone"));
        o.setWebsite(rs.getString("website"));
        o.setCreatedAt(rs.getTimestamp("created_at") != null
                ? rs.getTimestamp("created_at").toLocalDateTime() : null);
        o.setUpdatedAt(rs.getTimestamp("updated_at") != null
                ? rs.getTimestamp("updated_at").toLocalDateTime() : null);
        return o;
    }

    // ── GET ALL ──────────────────────────────────────────────────────────────
    public List<Organization> getAllOrganizations() {
        List<Organization> list = new ArrayList<>();
        String sql = "SELECT * FROM organizations ORDER BY name";
        try (Connection conn = DBConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) list.add(mapRow(rs));
        } catch (SQLException e) { e.printStackTrace(); }
        return list;
    }

    // ── GET BY ID ────────────────────────────────────────────────────────────
    public Organization getById(String id) {
        String sql = "SELECT * FROM organizations WHERE id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, id.toString());
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return mapRow(rs);
        } catch (SQLException e) { e.printStackTrace(); }
        return null;
    }

    // ── ADD ──────────────────────────────────────────────────────────────────
    public boolean addOrganization(Organization o) {
        String sql = "INSERT INTO organizations (id, name, tax_id, contact_email, address, phone, website) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            String newId = o.getId() != null ? o.getId().toString() : java.util.UUID.randomUUID().toString();
            ps.setString(1, newId);
            ps.setString(2, o.getName());
            ps.setString(3, o.getTaxId());
            ps.setString(4, o.getContactEmail());
            ps.setString(5, o.getAddress());
            ps.setString(6, o.getPhone());
            ps.setString(7, o.getWebsite());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) { e.printStackTrace(); return false; }
    }

    // ── UPDATE ───────────────────────────────────────────────────────────────
    public boolean updateOrganization(Organization o) {
        String sql = "UPDATE organizations SET name = ?, tax_id = ?, contact_email = ?, " +
                     "address = ?, phone = ?, website = ?, updated_at = NOW() WHERE id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, o.getName());
            ps.setString(2, o.getTaxId());
            ps.setString(3, o.getContactEmail());
            ps.setString(4, o.getAddress());
            ps.setString(5, o.getPhone());
            ps.setString(6, o.getWebsite());
            ps.setString(7, o.getId().toString());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) { e.printStackTrace(); return false; }
    }

    // ── DELETE ───────────────────────────────────────────────────────────────
    public boolean deleteOrganization(String id) {
        String sql = "DELETE FROM organizations WHERE id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, id.toString());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) { e.printStackTrace(); return false; }
    }
}
