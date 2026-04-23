package dao;

import db.DBConnection;
import model.RecurringPlan;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class RecurringPlanDAO {

    // ── HELPER: MAP ROW ──────────────────────────────────────────────────────
    private RecurringPlan mapRow(ResultSet rs) throws SQLException {
        RecurringPlan rp = new RecurringPlan();
        rp.setId(rs.getString("id"));
        rp.setCampaignId(rs.getString("campaign_id"));
        rp.setCurrencyCode(rs.getString("currency_code"));
        rp.setDonorId(rs.getString("donor_id"));
        String freqStr = rs.getString("frequency");
        if (freqStr != null) rp.setFrequency(RecurringPlan.Frequency.valueOf(freqStr));
        rp.setNextChargeDate(rs.getDate("next_charge_date") != null
                ? rs.getDate("next_charge_date").toLocalDate() : null);
        rp.setStartDate(rs.getDate("start_date") != null
                ? rs.getDate("start_date").toLocalDate() : null);
        rp.setEndDate(rs.getDate("end_date") != null
                ? rs.getDate("end_date").toLocalDate() : null);
        String statusStr = rs.getString("status");
        if (statusStr != null) rp.setStatus(RecurringPlan.PlanStatus.valueOf(statusStr));
        rp.setGatewayPlanId(rs.getString("gateway_plan_id"));
        rp.setCreatedAt(rs.getTimestamp("created_at") != null
                ? rs.getTimestamp("created_at").toLocalDateTime() : null);
        rp.setUpdatedAt(rs.getTimestamp("updated_at") != null
                ? rs.getTimestamp("updated_at").toLocalDateTime() : null);
        return rp;
    }

    // ── GET ALL ──────────────────────────────────────────────────────────────
    public List<RecurringPlan> getAllSchedules() {
        List<RecurringPlan> list = new ArrayList<>();
        String sql = "SELECT * FROM recurring_plans ORDER BY next_charge_date";
        try (Connection conn = DBConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) list.add(mapRow(rs));
        } catch (SQLException e) { e.printStackTrace(); }
        return list;
    }

    // ── GET ACTIVE ONLY ──────────────────────────────────────────────────────
    public List<RecurringPlan> getActiveSchedules() {
        List<RecurringPlan> list = new ArrayList<>();
        String sql = "SELECT * FROM recurring_plans WHERE status = 'active' ORDER BY next_charge_date";
        try (Connection conn = DBConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) list.add(mapRow(rs));
        } catch (SQLException e) { e.printStackTrace(); }
        return list;
    }

    // ── GET BY DONOR ─────────────────────────────────────────────────────────
    public List<RecurringPlan> getByDonor(String donorId) {
        List<RecurringPlan> list = new ArrayList<>();
        String sql = "SELECT * FROM recurring_plans WHERE donor_id = ? ORDER BY next_charge_date";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, donorId.toString());
            ResultSet rs = ps.executeQuery();
            while (rs.next()) list.add(mapRow(rs));
        } catch (SQLException e) { e.printStackTrace(); }
        return list;
    }

    // ── GET BY ID ────────────────────────────────────────────────────────────
    public RecurringPlan getById(String id) {
        String sql = "SELECT * FROM recurring_plans WHERE id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, id.toString());
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return mapRow(rs);
        } catch (SQLException e) { e.printStackTrace(); }
        return null;
    }

    // ── ADD ──────────────────────────────────────────────────────────────────
    public boolean addSchedule(RecurringPlan rp) {
        String sql = "INSERT INTO recurring_plans (id, campaign_id, currency_code, donor_id, " +
                     "frequency, next_charge_date, start_date, end_date, status, gateway_plan_id) " +
                     "VALUES (UUID(), ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, rp.getCampaignId());
            ps.setString(2, rp.getCurrencyCode() != null ? rp.getCurrencyCode() : "PKR");
            ps.setString(3, rp.getDonorId());
            ps.setString(4, rp.getFrequency() != null ? rp.getFrequency().name() : "monthly");
            ps.setDate(5, rp.getNextChargeDate() != null ? Date.valueOf(rp.getNextChargeDate()) : null);
            ps.setDate(6, rp.getStartDate() != null ? Date.valueOf(rp.getStartDate()) : Date.valueOf(java.time.LocalDate.now()));
            ps.setDate(7, rp.getEndDate() != null ? Date.valueOf(rp.getEndDate()) : null);
            ps.setString(8, rp.getStatus() != null ? rp.getStatus().name() : "active");
            ps.setString(9, rp.getGatewayPlanId());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) { e.printStackTrace(); return false; }
    }

    // ── TOGGLE ACTIVE/PAUSED ─────────────────────────────────────────────────
        public boolean toggleActive(String id) {
        String sql = "UPDATE recurring_plans SET " +
                     "status = CASE WHEN status = 'active' THEN 'paused' ELSE 'active' END, " +
                     "updated_at = NOW() WHERE id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, id.toString());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) { e.printStackTrace(); return false; }
    }

    // ── UPDATE ───────────────────────────────────────────────────────────────
    public boolean updateSchedule(RecurringPlan rp) {
        String sql = "UPDATE recurring_plans SET campaign_id = ?, currency_code = ?, donor_id = ?, " +
                     "frequency = ?, next_charge_date = ?, start_date = ?, end_date = ?, " +
                     "status = ?, gateway_plan_id = ?, updated_at = NOW() WHERE id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, rp.getCampaignId());
            ps.setString(2, rp.getCurrencyCode());
            ps.setString(3, rp.getDonorId());
            ps.setString(4, rp.getFrequency() != null ? rp.getFrequency().name() : null);
            ps.setDate(5, rp.getNextChargeDate() != null ? Date.valueOf(rp.getNextChargeDate()) : null);
            ps.setDate(6, rp.getStartDate() != null ? Date.valueOf(rp.getStartDate()) : null);
            ps.setDate(7, rp.getEndDate() != null ? Date.valueOf(rp.getEndDate()) : null);
            ps.setString(8, rp.getStatus() != null ? rp.getStatus().name() : null);
            ps.setString(9, rp.getGatewayPlanId());
            ps.setString(10, rp.getId());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) { e.printStackTrace(); return false; }
    }

    // ── DELETE ───────────────────────────────────────────────────────────────
    public boolean deleteSchedule(String id) {
        String sql = "DELETE FROM recurring_plans WHERE id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, id.toString());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) { e.printStackTrace(); return false; }
    }
}
