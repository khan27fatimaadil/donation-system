package dao;

import db.DBConnection;
import model.Currency;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class CurrencyDAO {

    // ── HELPER: MAP ROW ──────────────────────────────────────────────────────
    private Currency mapRow(ResultSet rs) throws SQLException {
        Currency c = new Currency();
        c.setCode(rs.getString("code"));
        c.setName(rs.getString("name"));
        c.setSymbol(rs.getString("symbol"));
        c.setActive(rs.getBoolean("is_active"));
        return c;
    }

    // ── GET ALL ──────────────────────────────────────────────────────────────
    public List<Currency> getAllCurrencies() {
        List<Currency> list = new ArrayList<>();
        String sql = "SELECT * FROM currencies ORDER BY code";
        try (Connection conn = DBConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) list.add(mapRow(rs));
        } catch (SQLException e) { e.printStackTrace(); }
        return list;
    }

    // ── GET ACTIVE ONLY ──────────────────────────────────────────────────────
    public List<Currency> getActiveCurrencies() {
        List<Currency> list = new ArrayList<>();
        String sql = "SELECT * FROM currencies WHERE is_active = true ORDER BY code";
        try (Connection conn = DBConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) list.add(mapRow(rs));
        } catch (SQLException e) { e.printStackTrace(); }
        return list;
    }

    // ── GET BY CODE ──────────────────────────────────────────────────────────
    public Currency getByCode(String code) {
        String sql = "SELECT * FROM currencies WHERE code = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, code);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return mapRow(rs);
        } catch (SQLException e) { e.printStackTrace(); }
        return null;
    }

    // ── ADD ──────────────────────────────────────────────────────────────────
    public boolean addCurrency(Currency c) {
        String sql = "INSERT INTO currencies (code, name, symbol, is_active) VALUES (?, ?, ?, ?)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, c.getCode());
            ps.setString(2, c.getName());
            ps.setString(3, c.getSymbol());
            ps.setBoolean(4, c.isActive());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) { e.printStackTrace(); return false; }
    }

    // ── UPDATE ───────────────────────────────────────────────────────────────
    public boolean updateCurrency(Currency c) {
        String sql = "UPDATE currencies SET name = ?, symbol = ?, is_active = ? WHERE code = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, c.getName());
            ps.setString(2, c.getSymbol());
            ps.setBoolean(3, c.isActive());
            ps.setString(4, c.getCode());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) { e.printStackTrace(); return false; }
    }

    // ── DELETE ───────────────────────────────────────────────────────────────
    public boolean deleteCurrency(String code) {
        String sql = "DELETE FROM currencies WHERE code = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, code);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) { e.printStackTrace(); return false; }
    }
}
