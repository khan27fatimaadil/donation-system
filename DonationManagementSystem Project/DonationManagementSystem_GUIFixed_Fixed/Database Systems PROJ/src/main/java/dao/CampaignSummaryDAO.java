package dao;

import db.DBConnection;
import model.CampaignSummary;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class CampaignSummaryDAO {

    // ── HELPER: MAP ROW ──────────────────────────────────────────────────────
    private CampaignSummary mapRow(ResultSet rs) throws SQLException {
        CampaignSummary cs = new CampaignSummary();
        cs.setCampaignId(rs.getString("campaign_id"));
        String title = rs.getString("title");
        cs.setTitle(title != null ? title : "(Untitled)");
        java.math.BigDecimal goal = rs.getBigDecimal("goal_amount");
        cs.setGoalAmount(goal != null ? goal : java.math.BigDecimal.ZERO);
        String currency = rs.getString("currency_code");
        cs.setCurrencyCode(currency != null ? currency : "PKR");
        java.math.BigDecimal raised = rs.getBigDecimal("raised_amount");
        cs.setRaisedAmount(raised != null ? raised : java.math.BigDecimal.ZERO);
        cs.setDonationCount(rs.getInt("donation_count"));
        double pct = rs.getDouble("progress_pct");
        cs.setProgressPct(rs.wasNull() ? 0.0 : pct);
        return cs;
    }

    // ── GET ALL SUMMARIES ─────────────────────────────────────────────────────
    public List<CampaignSummary> getAllSummaries() {
        List<CampaignSummary> list = new ArrayList<>();
        // This query computes the summary live from donations + campaigns
        String sql = "SELECT c.id AS campaign_id, c.title, c.goal_amount, c.currency_code, " +
                     "COALESCE(SUM(d.amount), 0) AS raised_amount, " +
                     "COUNT(d.id) AS donation_count, " +
                     "ROUND((COALESCE(SUM(d.amount), 0) / NULLIF(c.goal_amount, 0)) * 100, 2) AS progress_pct " +
                     "FROM campaigns c " +
                     "LEFT JOIN donations d ON c.id = d.campaign_id AND d.status = 'completed' " +
                     "GROUP BY c.id, c.title, c.goal_amount, c.currency_code " +
                     "ORDER BY progress_pct DESC";
        try (Connection conn = DBConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) list.add(mapRow(rs));
        } catch (SQLException e) { e.printStackTrace(); }
        return list;
    }

    // ── GET BY CAMPAIGN ID ────────────────────────────────────────────────────
    public CampaignSummary getByCampaign(String campaignId) {
        String sql = "SELECT c.id AS campaign_id, c.title, c.goal_amount, c.currency_code, " +
                     "COALESCE(SUM(d.amount), 0) AS raised_amount, " +
                     "COUNT(d.id) AS donation_count, " +
                     "ROUND((COALESCE(SUM(d.amount), 0) / NULLIF(c.goal_amount, 0)) * 100, 2) AS progress_pct " +
                     "FROM campaigns c " +
                     "LEFT JOIN donations d ON c.id = d.campaign_id AND d.status = 'completed' " +
                     "WHERE c.id = ? " +
                     "GROUP BY c.id, c.title, c.goal_amount, c.currency_code";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, campaignId.toString());
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return mapRow(rs);
        } catch (SQLException e) { e.printStackTrace(); }
        return null;
    }

    // ── GET TOP CAMPAIGNS BY RAISED AMOUNT ────────────────────────────────────
    public List<CampaignSummary> getTopCampaigns(int limit) {
        List<CampaignSummary> list = new ArrayList<>();
        String sql = "SELECT c.id AS campaign_id, c.title, c.goal_amount, c.currency_code, " +
                     "COALESCE(SUM(d.amount), 0) AS raised_amount, " +
                     "COUNT(d.id) AS donation_count, " +
                     "ROUND((COALESCE(SUM(d.amount), 0) / NULLIF(c.goal_amount, 0)) * 100, 2) AS progress_pct " +
                     "FROM campaigns c " +
                     "LEFT JOIN donations d ON c.id = d.campaign_id AND d.status = 'completed' " +
                     "GROUP BY c.id, c.title, c.goal_amount, c.currency_code " +
                     "ORDER BY raised_amount DESC LIMIT ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, limit);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) list.add(mapRow(rs));
        } catch (SQLException e) { e.printStackTrace(); }
        return list;
    }
}
