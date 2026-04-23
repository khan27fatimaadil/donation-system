package dao;

import db.DBConnection;

import java.math.BigDecimal;
import java.sql.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * AnalyticsDAO — read-only queries for the Dashboard and Analytics screens.
 * Queries run directly against base tables so they work regardless of whether
 * the optional schema_enhancements.sql views have been created.
 */
public class AnalyticsDAO {

    private static final DateTimeFormatter DATE_FMT =
        DateTimeFormatter.ofPattern("dd MMM yyyy");

    // ── TOP DONORS ───────────────────────────────────────────────────────────
    public List<Map<String, Object>> getTopDonors() {
        List<Map<String, Object>> result = new ArrayList<>();
        String sql =
            "SELECT " +
            "    d.id AS donorId, " +
            "    COALESCE(NULLIF(TRIM(CONCAT(" +
            "        COALESCE(d.first_name,''), ' ', COALESCE(d.last_name,''))), ''), " +
            "        d.organization_name, d.email, 'Unknown Donor') AS name, " +
            "    d.email AS email, " +
            "    COALESCE(SUM(don.amount), 0) AS total, " +
            "    COUNT(don.id)               AS donationCount " +
            "FROM donors d " +
            "LEFT JOIN donations don ON don.donor_id = d.id AND don.status = 'completed' " +
            "GROUP BY d.id, d.first_name, d.last_name, d.organization_name, d.email " +
            "ORDER BY total DESC " +
            "LIMIT 10";
        try (Connection conn = DBConnection.getConnection();
             Statement stmt  = conn.createStatement();
             ResultSet rs    = stmt.executeQuery(sql)) {
            while (rs.next()) {
                Map<String, Object> row = new LinkedHashMap<>();
                row.put("donorId",       rs.getString("donorId"));
                row.put("name",          safe(rs.getString("name"), "Unknown Donor"));
                row.put("email",         safe(rs.getString("email"), "—"));
                row.put("total",         safeDec(rs.getBigDecimal("total")));
                row.put("donationCount", rs.getInt("donationCount"));
                result.add(row);
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return result;
    }

    // ── CAUSE PROGRESS ───────────────────────────────────────────────────────
    public List<Map<String, Object>> getCauseProgress() {
        List<Map<String, Object>> result = new ArrayList<>();
        String sql =
            "SELECT " +
            "    c.id                                              AS campaignId, " +
            "    COALESCE(c.title, '(Untitled)')                  AS title, " +
            "    COALESCE(c.goal_amount, 0)                       AS goal, " +
            "    COALESCE(SUM(d.amount), 0)                       AS raised, " +
            "    ROUND((COALESCE(SUM(d.amount),0) / " +
            "           NULLIF(c.goal_amount,0)) * 100, 1)        AS percent, " +
            "    c.status                                          AS status " +
            "FROM campaigns c " +
            "LEFT JOIN donations d ON d.campaign_id = c.id " +
            "GROUP BY c.id, c.title, c.goal_amount, c.status " +
            "ORDER BY percent DESC";
        try (Connection conn = DBConnection.getConnection();
             Statement stmt  = conn.createStatement();
             ResultSet rs    = stmt.executeQuery(sql)) {
            while (rs.next()) {
                Map<String, Object> row = new LinkedHashMap<>();
                row.put("campaignId", rs.getString("campaignId"));
                row.put("title",      safe(rs.getString("title"), "(Untitled)"));
                row.put("goal",       safeDec(rs.getBigDecimal("goal")));
                row.put("raised",     safeDec(rs.getBigDecimal("raised")));
                double pct = rs.getDouble("percent");
                row.put("percent",    rs.wasNull() ? 0.0 : pct);
                row.put("status",     safe(rs.getString("status"), "unknown"));
                result.add(row);
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return result;
    }

    // ── UPCOMING RECURRING ───────────────────────────────────────────────────
    public List<Map<String, Object>> getUpcomingRecurring() {
        List<Map<String, Object>> result = new ArrayList<>();
        String sql =
            "SELECT " +
            "    COALESCE(NULLIF(TRIM(CONCAT(" +
            "        COALESCE(d.first_name,''), ' ', COALESCE(d.last_name,''))), ''), " +
            "        d.organization_name, 'Unknown Donor')  AS donor, " +
            "    COALESCE(c.title, '(No Cause)')             AS cause, " +
            "    rp.amount                                   AS amount, " +
            "    COALESCE(rp.currency_code, 'USD')           AS currency, " +
            "    rp.frequency                                AS frequency, " +
            "    rp.next_charge_date                         AS nextDue " +
            "FROM recurring_plans rp " +
            "JOIN  donors    d ON rp.donor_id    = d.id " +
            "JOIN  campaigns c ON rp.campaign_id = c.id " +
            "WHERE rp.status = 'active' " +
            "ORDER BY rp.next_charge_date ASC";
        try (Connection conn = DBConnection.getConnection();
             Statement stmt  = conn.createStatement();
             ResultSet rs    = stmt.executeQuery(sql)) {
            while (rs.next()) {
                Map<String, Object> row = new LinkedHashMap<>();
                row.put("donor",     safe(rs.getString("donor"), "Unknown Donor"));
                row.put("cause",     safe(rs.getString("cause"), "(No Cause)"));
                row.put("amount",    safeDec(rs.getBigDecimal("amount")));
                row.put("currency",  safe(rs.getString("currency"), "USD"));
                row.put("frequency", safe(rs.getString("frequency"), "—"));
                row.put("nextDue",   rs.getDate("nextDue") != null
                    ? rs.getDate("nextDue").toLocalDate() : null);
                result.add(row);
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return result;
    }

    // ── OVERALL SUMMARY (Dashboard stat cards) ───────────────────────────────
    public Map<String, Object> getOverallSummary() {
        Map<String, Object> summary = new LinkedHashMap<>();
        String sql =
            "SELECT " +
            "    (SELECT COALESCE(SUM(amount),0)  FROM donations WHERE status='completed') AS totalRaised, " +
            "    (SELECT COUNT(DISTINCT donor_id) FROM donations WHERE status='completed') AS totalDonors, " +
            "    (SELECT COUNT(*) FROM campaigns  WHERE status='active')                   AS totalCampaigns, " +
            "    (SELECT COUNT(*) FROM donations  WHERE status='completed')                AS totalDonations";
        try (Connection conn = DBConnection.getConnection();
             Statement stmt  = conn.createStatement();
             ResultSet rs    = stmt.executeQuery(sql)) {
            if (rs.next()) {
                summary.put("totalRaised",    safeDec(rs.getBigDecimal("totalRaised")));
                summary.put("totalDonors",    rs.getInt("totalDonors"));
                summary.put("totalCampaigns", rs.getInt("totalCampaigns"));
                summary.put("totalDonations", rs.getInt("totalDonations"));
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return summary;
    }

    // ── RECENT DONATIONS (Dashboard table) ───────────────────────────────────
    public List<Map<String, Object>> getRecentDonations(int limit) {
        List<Map<String, Object>> result = new ArrayList<>();
        String sql =
            "SELECT " +
            "    COALESCE(NULLIF(TRIM(CONCAT(" +
            "        COALESCE(d.first_name,''), ' ', COALESCE(d.last_name,''))), ''), " +
            "        d.organization_name, 'Unknown Donor') AS donor, " +
            "    COALESCE(don.amount, 0)                   AS amount, " +
            "    COALESCE(don.currency_code, 'USD')        AS currency, " +
            "    COALESCE(c.title, '(No Cause)')           AS cause, " +
            "    don.donated_at                            AS donatedAt, " +
            "    COALESCE(don.status, 'unknown')           AS status " +
            "FROM donations don " +
            "JOIN  donors    d ON don.donor_id    = d.id " +
            "LEFT  JOIN campaigns c ON don.campaign_id = c.id " +
            "ORDER BY don.donated_at DESC " +
            "LIMIT ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, limit);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                Map<String, Object> row = new LinkedHashMap<>();
                row.put("donor",  safe(rs.getString("donor"), "Unknown Donor"));
                BigDecimal amt = safeDec(rs.getBigDecimal("amount"));
                String     cur = safe(rs.getString("currency"), "USD");
                row.put("amount", cur + " " + String.format("%,.0f", amt));
                row.put("cause",  safe(rs.getString("cause"), "(No Cause)"));
                row.put("date",   rs.getTimestamp("donatedAt") != null
                    ? rs.getTimestamp("donatedAt").toLocalDateTime().format(DATE_FMT) : "—");
                row.put("status", safe(rs.getString("status"), "unknown"));
                result.add(row);
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return result;
    }
    
    public String getTextSummary() {
        Map<String, Object> stats = getOverallSummary();
        StringBuilder sb = new StringBuilder();
        sb.append("DONATION MANAGEMENT SYSTEM - ANALYTICS REPORT\n");
        sb.append("Generated on: ").append(LocalDate.now()).append("\n");
        sb.append("-------------------------------------------\n");
        sb.append("Total Raised: PKR ").append(stats.get("totalRaised")).append("\n");
        sb.append("Total Donors: ").append(stats.get("totalDonors")).append("\n");
        sb.append("Active Campaigns: ").append(stats.get("totalCampaigns")).append("\n");
        return sb.toString();
    }

    // ── DONATIONS BY MONTH ────────────────────────────────────────────────────
    public List<Map<String, Object>> getDonationsByMonth(int year) {
        List<Map<String, Object>> result = new ArrayList<>();
        String sql =
            "SELECT MONTH(donated_at) AS month, COALESCE(SUM(amount), 0) AS total " +
            "FROM donations " +
            "WHERE status = 'completed' AND YEAR(donated_at) = ? " +
            "GROUP BY MONTH(donated_at) ORDER BY month";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, year);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                Map<String, Object> row = new LinkedHashMap<>();
                row.put("month", rs.getInt("month"));
                row.put("total", safeDec(rs.getBigDecimal("total")));
                result.add(row);
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return result;
    }

    // ── HELPERS ───────────────────────────────────────────────────────────────
    private String safe(String val, String fallback) {
        return (val != null && !val.trim().isEmpty()) ? val.trim() : fallback;
    }

    private BigDecimal safeDec(BigDecimal val) {
        return val != null ? val : BigDecimal.ZERO;
    }
}
