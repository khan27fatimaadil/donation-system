package dms.controllers;

import dao.CampaignDAO;
import dao.CurrencyDAO;
import dao.DonationStatusLogDAO;
import dao.DonationDAO;
import dao.DonorDAO;
import dao.FundDAO;
import dao.PaymentTransactionDAO;
import dao.ReceiptDAO;
import db.DBConnection;
import model.Campaign;
import model.Currency;
import model.Donation;
import model.DonationStatusLog;
import model.Donor;
import model.Fund;
import model.PaymentTransaction;
import model.Receipt;
import dms.exceptions.DatabaseOperationException;
import dms.exceptions.ValidationException;

import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;

import java.math.BigDecimal;
import java.net.URL;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;
import java.util.ResourceBundle;
import java.util.UUID;

public class DonationEntryController implements Initializable {

    @FXML private ComboBox<Donor>    donorCombo;
    @FXML private ComboBox<Campaign> causeCombo;
    @FXML private ComboBox<Fund>     fundCombo;
    @FXML private ComboBox<Currency> currencyCombo;
    @FXML private TextField          amountField;
    @FXML private TextField          methodField;
    @FXML private DatePicker         datePicker;

    private final DonorDAO             donorDAO     = new DonorDAO();
    private final CampaignDAO          campaignDAO  = new CampaignDAO();
    private final FundDAO              fundDAO      = new FundDAO();
    private final CurrencyDAO          currencyDAO  = new CurrencyDAO();

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        loadDropdowns();
    }

    private void loadDropdowns() {
        try {
            // Donors — use organizationName for org-type donors
            List<Donor> donors = donorDAO.getAllDonors();
            donorCombo.setItems(FXCollections.observableArrayList(donors));
            donorCombo.setConverter(new javafx.util.StringConverter<Donor>() {
                public String toString(Donor d) {
                    if (d == null) return "";
                    return resolveDonorName(d);
                }
                public Donor fromString(String s) { return null; }
            });

            List<Campaign> campaigns = campaignDAO.getAllCampaigns();
            causeCombo.setItems(FXCollections.observableArrayList(campaigns));
            causeCombo.setConverter(new javafx.util.StringConverter<Campaign>() {
                public String toString(Campaign c) {
                    return c == null ? "" : (c.getTitle() != null ? c.getTitle() : "(Untitled)");
                }
                public Campaign fromString(String s) { return null; }
            });

            List<Fund> funds = fundDAO.getAllFunds();
            fundCombo.setItems(FXCollections.observableArrayList(funds));
            fundCombo.setConverter(new javafx.util.StringConverter<Fund>() {
                public String toString(Fund f) {
                    return f == null ? "" : (f.getName() != null ? f.getName() : "(Unnamed)");
                }
                public Fund fromString(String s) { return null; }
            });

            List<Currency> currencies = currencyDAO.getActiveCurrencies();
            currencyCombo.setItems(FXCollections.observableArrayList(currencies));
            currencyCombo.setConverter(new javafx.util.StringConverter<Currency>() {
                public String toString(Currency c) {
                    return c == null ? "" : c.getCode() + " - " + c.getName() + " (" + c.getSymbol() + ")";
                }
                public Currency fromString(String s) { return null; }
            });
            currencies.stream().filter(c -> "PKR".equals(c.getCode())).findFirst()
                .ifPresentOrElse(currencyCombo::setValue,
                    () -> { if (!currencies.isEmpty()) currencyCombo.setValue(currencies.get(0)); });

            if (datePicker != null) datePicker.setValue(java.time.LocalDate.now());

        } catch (Exception e) {
            System.err.println("Dropdown load error: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    public void submitForm() {
        try {
            validateInput();

            Donor    selectedDonor    = donorCombo.getValue();
            Campaign selectedCampaign = causeCombo.getValue();
            Fund     selectedFund     = fundCombo.getValue();
            Currency selectedCurrency = currencyCombo.getValue();

            String     amountStr    = amountField.getText().trim();
            String     methodStr    = methodField.getText().trim();
            BigDecimal amount       = new BigDecimal(amountStr);
            String     donationId   = UUID.randomUUID().toString();
            String     receiptId    = UUID.randomUUID().toString();
            String     txnId        = UUID.randomUUID().toString();
            String     receiptNum   = "REC-" + System.currentTimeMillis();
            LocalDateTime donatedAt = (datePicker != null && datePicker.getValue() != null)
                                      ? datePicker.getValue().atStartOfDay()
                                      : LocalDateTime.now();

            // ── TRANSACTION: all-or-nothing insert ───────────────────────
            try (Connection conn = DBConnection.getConnection()) {
                conn.setAutoCommit(false);
                try {
                    // 1. donations
                    String sqlDon = "INSERT INTO donations " +
                        "(id, donor_id, campaign_id, amount, currency_code, type, status, donated_at) " +
                        "VALUES (?, ?, ?, ?, ?, 'one_time', 'completed', ?)";
                    try (PreparedStatement ps = conn.prepareStatement(sqlDon)) {
                        ps.setString(1, donationId);
                        ps.setString(2, selectedDonor.getId());
                        ps.setString(3, selectedCampaign.getId());
                        ps.setBigDecimal(4, amount);
                        ps.setString(5, selectedCurrency.getCode());
                        ps.setTimestamp(6, Timestamp.valueOf(donatedAt));
                        ps.executeUpdate();
                    }

                    // 2. receipts
                    String sqlRec = "INSERT INTO receipts " +
                        "(id, donation_id, receipt_number, issued_at) VALUES (?, ?, ?, NOW())";
                    try (PreparedStatement ps = conn.prepareStatement(sqlRec)) {
                        ps.setString(1, receiptId);
                        ps.setString(2, donationId);
                        ps.setString(3, receiptNum);
                        ps.executeUpdate();
                    }

                    // 3. payment_transactions
                    String gateway = methodStr.isEmpty() ? "Cash" : methodStr;
                    String sqlTxn = "INSERT INTO payment_transactions " +
                        "(id, donation_id, gateway, method, status, amount, currency_code, processed_at) " +
                        "VALUES (?, ?, ?, 'cash', 'succeeded', ?, ?, NOW())";
                    try (PreparedStatement ps = conn.prepareStatement(sqlTxn)) {
                        ps.setString(1, txnId);
                        ps.setString(2, donationId);
                        ps.setString(3, gateway);
                        ps.setBigDecimal(4, amount);
                        ps.setString(5, selectedCurrency.getCode());
                        ps.executeUpdate();
                    }

                    // 4. donation_status_log
                    String sqlLog = "INSERT INTO donation_status_log " +
                        "(donation_id, old_status, new_status, notes) " +
                        "VALUES (?, 'pending', 'completed', 'Recorded via Donation Entry form')";
                    try (PreparedStatement ps = conn.prepareStatement(sqlLog)) {
                        ps.setString(1, donationId);
                        ps.executeUpdate();
                    }

                    // 5. allocations (if a fund was selected)
                    if (selectedFund != null) {
                        String sqlAlloc = "INSERT INTO allocations (donation_id, fund_id, amount) " +
                            "VALUES (?, ?, ?)";
                        try (PreparedStatement ps = conn.prepareStatement(sqlAlloc)) {
                            ps.setString(1, donationId);
                            ps.setString(2, selectedFund.getId());
                            ps.setBigDecimal(3, amount);
                            ps.executeUpdate();
                        }
                    }

                    conn.commit();

                } catch (Exception ex) {
                    conn.rollback();
                    throw new DatabaseOperationException(
                        "Transaction rolled back — no data was saved. Cause: " + ex.getMessage(), ex);
                }
            }

            Alert ok = new Alert(Alert.AlertType.INFORMATION,
                "Donation recorded successfully!\n" +
                "Donor:   " + resolveDonorName(selectedDonor) + "\n" +
                "Amount:  " + selectedCurrency.getSymbol() + " " + amountStr + "\n" +
                "Receipt: " + receiptNum,
                ButtonType.OK);
            ok.setHeaderText("Donation Saved");
            ok.showAndWait();
            resetForm();

        } catch (ValidationException ve) {
            new Alert(Alert.AlertType.WARNING, ve.getMessage(), ButtonType.OK).showAndWait();
        } catch (DatabaseOperationException dbEx) {
            new Alert(Alert.AlertType.ERROR, "Database Error: " + dbEx.getMessage(), ButtonType.OK).showAndWait();
        } catch (Exception e) {
            new Alert(Alert.AlertType.ERROR, "Unexpected Error: " + e.getMessage(), ButtonType.OK).showAndWait();
        }
    }

    private void validateInput() throws ValidationException {
        if (donorCombo.getValue() == null)
            throw new ValidationException("Please select a donor.");
        if (causeCombo.getValue() == null)
            throw new ValidationException("Please select a cause/campaign.");
        if (currencyCombo.getValue() == null)
            throw new ValidationException("Please select a currency.");
        String amountStr = amountField.getText().trim();
        if (amountStr.isEmpty())
            throw new ValidationException("Please enter a donation amount.");
        try {
            BigDecimal amount = new BigDecimal(amountStr);
            if (amount.compareTo(BigDecimal.ZERO) <= 0)
                throw new ValidationException("Amount must be greater than zero.");
        } catch (NumberFormatException e) {
            throw new ValidationException("Invalid amount — enter a number e.g. 5000 or 1500.50.");
        }
    }

    @FXML
    public void resetForm() {
        donorCombo.setValue(null);
        causeCombo.setValue(null);
        fundCombo.setValue(null);
        amountField.clear();
        methodField.clear();
        currencyCombo.getItems().stream().filter(c -> "PKR".equals(c.getCode())).findFirst()
            .ifPresentOrElse(currencyCombo::setValue,
                () -> { if (!currencyCombo.getItems().isEmpty())
                            currencyCombo.setValue(currencyCombo.getItems().get(0)); });
        if (datePicker != null) datePicker.setValue(java.time.LocalDate.now());
    }

    /** Resolve a display name for any donor type — never returns blank. */
    private String resolveDonorName(Donor d) {
        if (d == null) return "Unknown Donor";
        String full = ((d.getFirstName() != null ? d.getFirstName() : "")
                     + (d.getLastName()  != null ? " " + d.getLastName() : "")).trim();
        if (!full.isEmpty()) return full;
        if (d.getOrganizationName() != null && !d.getOrganizationName().trim().isEmpty())
            return d.getOrganizationName().trim();
        return d.getEmail() != null && !d.getEmail().isEmpty() ? d.getEmail() : "Unknown Donor";
    }
}
