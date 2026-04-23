package dao;

import db.DBConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * ImpactReportDAO — campaign impact summaries for the Impact Reports screen.
 * Uses direct table queries against campaigns + donations + allocations.
 * No dependency on views or stored procedures — those are optional enhancements
 * available in schema_enhancements.sql.
 */
public class ImpactReportDAO {

    // ── GET ALL REPORTS ───────────────────────────────────────────────────────
    public List<Map<String, Object>> getAllReports() {
        List<Map<String, Object>> result = new ArrayList<>();
        String sql =
            "SELECT " +
            "    c.id                                                    AS campaignId, " +
            "    COALESCE(c.title, '(Untitled)')                         AS campaignTitle, " +
            "    COALESCE(c.goal_amount, 0)                              AS goalAmount, " +
            "    COALESCE(c.currency_code, 'USD')                        AS currencyCode, " +
            "    c.start_date                                            AS startDate, " +
            "    c.end_date                                              AS endDate, " +
            "    c.status                                                AS status, " +
            "    COALESCE(SUM(d.amount), 0)                              AS raisedAmount, " +
            "    COUNT(d.id)                                             AS donationCount, " +
            "    COALESCE(SUM(a.amount), 0)                              AS fundsUsed, " +
            "    ROUND((COALESCE(SUM(d.amount),0) / " +
            "           NULLIF(c.goal_amount,0)) * 100, 1)               AS progressPct " +
            "FROM campaigns c " +
            "LEFT JOIN donations  d ON d.campaign_id = c.id " +
            "LEFT JOIN allocations a ON a.donation_id = d.id " +
            "GROUP BY c.id, c.title, c.goal_amount, c.currency_code, " +
            "         c.start_date, c.end_date, c.status " +
            "ORDER BY c.start_date DESC";
        try (Connection conn = DBConnection.getConnection();
             Statement stmt  = conn.createStatement();
             ResultSet rs    = stmt.executeQuery(sql)) {
            while (rs.next()) {
                result.add(buildRow(rs));
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return result;
    }

    // ── GET REPORT FOR ONE CAMPAIGN ───────────────────────────────────────────
    public Map<String, Object> getReportByCampaign(String campaignId) {
        String sql =
            "SELECT " +
            "    c.id                                                    AS campaignId, " +
            "    COALESCE(c.title, '(Untitled)')                         AS campaignTitle, " +
            "    COALESCE(c.goal_amount, 0)                              AS goalAmount, " +
            "    COALESCE(c.currency_code, 'USD')                        AS currencyCode, " +
            "    c.start_date                                            AS startDate, " +
            "    c.end_date                                              AS endDate, " +
            "    c.status                                                AS status, " +
            "    COALESCE(SUM(d.amount), 0)                              AS raisedAmount, " +
            "    COUNT(d.id)                                             AS donationCount, " +
            "    COALESCE(SUM(a.amount), 0)                              AS fundsUsed, " +
            "    ROUND((COALESCE(SUM(d.amount),0) / " +
            "           NULLIF(c.goal_amount,0)) * 100, 1)               AS progressPct " +
            "FROM campaigns c " +
            "LEFT JOIN donations  d ON d.campaign_id = c.id " +
            "LEFT JOIN allocations a ON a.donation_id = d.id " +
            "WHERE c.id = ? " +
            "GROUP BY c.id, c.title, c.goal_amount, c.currency_code, " +
            "         c.start_date, c.end_date, c.status";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, campaignId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return buildRow(rs);
        } catch (SQLException e) { e.printStackTrace(); }
        return null;
    }

    // ── GET SUBMITTED NARRATIVE REPORTS ──────────────────────────────────────
    // Returns reports from the campaign_summaries table if it exists.
    // Falls back to getAllReports() if the table hasn't been created yet.
    public List<Map<String, Object>> getSubmittedReports() {
        // Check if campaign_summaries table exists first
        String checkSql =
            "SELECT COUNT(*) FROM information_schema.tables " +
            "WHERE table_schema = DATABASE() AND table_name = 'campaign_summaries'";
        try (Connection conn = DBConnection.getConnection();
             Statement stmt  = conn.createStatement();
             ResultSet rs    = stmt.executeQuery(checkSql)) {
            if (rs.next() && rs.getInt(1) == 0) {
                // Table doesn't exist — fall back to campaign summary data
                return getAllReports();
            }
        } catch (SQLException e) {
            return getAllReports();
        }

        // Table exists — query it
        List<Map<String, Object>> result = new ArrayList<>();
        String sql =
            "SELECT " +
            "    cs.id                                                    AS reportId, " +
            "    COALESCE(c.title, '(Untitled)')                          AS campaignTitle, " +
            "    COALESCE(cs.summary, '')                                 AS summary, " +
            "    COALESCE(cs.funds_used, 0)                               AS fundsUsed, " +
            "    COALESCE(NULLIF(TRIM(CONCAT(" +
            "        COALESCE(u.first_name,''), ' ', COALESCE(u.last_name,''))), ''), " +
            "        'System')                                            AS reportedBy, " +
            "    cs.reported_at                                           AS reportedAt " +
            "FROM campaign_summaries cs " +
            "JOIN  campaigns c ON cs.campaign_id = c.id " +
            "LEFT  JOIN users u ON cs.reported_by = u.id " +
            "ORDER BY cs.reported_at DESC";
        try (Connection conn = DBConnection.getConnection();
             Statement stmt  = conn.createStatement();
             ResultSet rs    = stmt.executeQuery(sql)) {
            while (rs.next()) {
                Map<String, Object> row = new LinkedHashMap<>();
                row.put("reportId",      rs.getString("reportId"));
                row.put("campaignTitle", safe(rs.getString("campaignTitle"), "(Untitled)"));
                row.put("summary",       safe(rs.getString("summary"), ""));
                row.put("fundsUsed",     rs.getBigDecimal("fundsUsed") != null
                    ? rs.getBigDecimal("fundsUsed") : java.math.BigDecimal.ZERO);
                row.put("reportedBy",    safe(rs.getString("reportedBy"), "System"));
                row.put("reportedAt",    rs.getTimestamp("reportedAt") != null
                    ? rs.getTimestamp("reportedAt").toLocalDateTime() : null);
                result.add(row);
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return getAllReports(); // graceful fallback
        }
        return result.isEmpty() ? getAllReports() : result;
    }

    // ── PRIVATE HELPERS ───────────────────────────────────────────────────────
    private Map<String, Object> buildRow(ResultSet rs) throws SQLException {
        Map<String, Object> row = new LinkedHashMap<>();
        row.put("campaignId",    safe(rs.getString("campaignId"),    ""));
        row.put("campaignTitle", safe(rs.getString("campaignTitle"), "(Untitled)"));
        row.put("goalAmount",    rs.getBigDecimal("goalAmount") != null
            ? rs.getBigDecimal("goalAmount") : java.math.BigDecimal.ZERO);
        row.put("currencyCode",  safe(rs.getString("currencyCode"), "USD"));
        row.put("startDate",     rs.getDate("startDate") != null
            ? rs.getDate("startDate").toLocalDate() : null);
        row.put("endDate",       rs.getDate("endDate") != null
            ? rs.getDate("endDate").toLocalDate() : null);
        row.put("status",        safe(rs.getString("status"), "unknown"));
        row.put("raisedAmount",  rs.getBigDecimal("raisedAmount") != null
            ? rs.getBigDecimal("raisedAmount") : java.math.BigDecimal.ZERO);
        row.put("donationCount", rs.getInt("donationCount"));
        row.put("fundsUsed",     rs.getBigDecimal("fundsUsed") != null
            ? rs.getBigDecimal("fundsUsed") : java.math.BigDecimal.ZERO);
        double pct = rs.getDouble("progressPct");
        row.put("progressPct",   rs.wasNull() ? 0.0 : pct);
        return row;
    }

    private String safe(String val, String fallback) {
        return (val != null && !val.trim().isEmpty()) ? val.trim() : fallback;
    }

	public boolean submitReport(Map<String, Object> data) {
		// TODO Auto-generated method stub
		return false;
	}
}
