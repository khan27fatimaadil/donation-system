package dao;

import db.DBConnection;
import model.Country;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class CountryDAO {

    // ── HELPER: MAP ROW ──────────────────────────────────────────────────────
    private Country mapRow(ResultSet rs) throws SQLException {
        Country c = new Country();
        c.setCode(rs.getString("code"));
        c.setName(rs.getString("name"));
        c.setRegion(rs.getString("region"));
        c.setActive(rs.getBoolean("is_active"));
        return c;
    }

    // ── GET ALL ──────────────────────────────────────────────────────────────
    public List<Country> getAllCountries() {
        List<Country> list = new ArrayList<>();
        String sql = "SELECT * FROM countries ORDER BY name";
        try (Connection conn = DBConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) list.add(mapRow(rs));
        } catch (SQLException e) { e.printStackTrace(); }
        return list;
    }

    // ── GET ACTIVE ONLY ──────────────────────────────────────────────────────
    public List<Country> getActiveCountries() {
        List<Country> list = new ArrayList<>();
        String sql = "SELECT * FROM countries WHERE is_active = true ORDER BY name";
        try (Connection conn = DBConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) list.add(mapRow(rs));
        } catch (SQLException e) { e.printStackTrace(); }
        return list;
    }

    // ── GET BY CODE ──────────────────────────────────────────────────────────
    public Country getByCode(String code) {
        String sql = "SELECT * FROM countries WHERE code = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, code);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return mapRow(rs);
        } catch (SQLException e) { e.printStackTrace(); }
        return null;
    }

    // ── ADD ──────────────────────────────────────────────────────────────────
    public boolean addCountry(Country c) {
        String sql = "INSERT INTO countries (code, name, region, is_active) VALUES (?, ?, ?, ?)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, c.getCode());
            ps.setString(2, c.getName());
            ps.setString(3, c.getRegion());
            ps.setBoolean(4, c.isActive());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) { e.printStackTrace(); return false; }
    }

    // ── UPDATE ───────────────────────────────────────────────────────────────
    public boolean updateCountry(Country c) {
        String sql = "UPDATE countries SET name = ?, region = ?, is_active = ? WHERE code = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, c.getName());
            ps.setString(2, c.getRegion());
            ps.setBoolean(3, c.isActive());
            ps.setString(4, c.getCode());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) { e.printStackTrace(); return false; }
    }

    // ── DELETE ───────────────────────────────────────────────────────────────
    public boolean deleteCountry(String code) {
        String sql = "DELETE FROM countries WHERE code = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, code);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) { e.printStackTrace(); return false; }
    }
}
