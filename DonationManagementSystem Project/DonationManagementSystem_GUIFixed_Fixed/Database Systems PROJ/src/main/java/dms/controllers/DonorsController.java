package dms.controllers;

import dao.CommunicationDAO;
import dao.CountryDAO;
import dao.DonorDAO;
import dao.DonorTagDAO;
import model.Communication;
import model.Country;
import model.Donor;
import dms.exceptions.DatabaseOperationException;

import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;
import java.util.regex.Pattern;

/**
 * DonorsController — manages the Donors screen.
 *
 * Fixes applied:
 *  #16 — Pagination: page through donors PAGE_SIZE rows at a time.
 *  #17 — Empty state: meaningful placeholder with call-to-action label.
 *  #18 — "Send Mail" renamed "Log Comm." and dialog title clarified.
 *  #19 — Toolbar: delete button separated; actions grouped logically.
 *  #20 — Empty state message improved (FXML placeholder).
 */
public class DonorsController implements Initializable {

    // ── Fix #16: Pagination constants ─────────────────────────────────────────
    private static final int PAGE_SIZE = 20;
    private int currentPage = 0;
    private List<Donor> allDonors = new ArrayList<>();

    @FXML private TextField searchField;
    @FXML private TableView<Donor> donorTable;
    @FXML private TableColumn<Donor, String> colName;
    @FXML private TableColumn<Donor, String> colEmail;
    @FXML private TableColumn<Donor, String> colPhone;
    @FXML private TableColumn<Donor, String> colType;

    // Fix #16: pagination controls
    @FXML private Label  lblPageInfo;
    @FXML private Button btnPrevPage;
    @FXML private Button btnNextPage;

    private static final Pattern EMAIL_PATTERN =
        Pattern.compile("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");
    private static final Pattern PHONE_PATTERN =
        Pattern.compile("^[+]?[0-9 \\-().]{7,20}$");

    private final ObservableList<Donor> pageList = FXCollections.observableArrayList();
    private final DonorDAO donorDAO    = new DonorDAO();
    private final CountryDAO countryDAO = new CountryDAO();
    private List<Country> countryCache;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        colName.setCellValueFactory(cd -> {
            Donor d = cd.getValue();
            String full = ((d.getFirstName() != null ? d.getFirstName() : "")
                         + (d.getLastName() != null ? " " + d.getLastName() : "")).trim();
            if (!full.isEmpty()) return new SimpleStringProperty(full);
            if (d.getOrganizationName() != null && !d.getOrganizationName().trim().isEmpty())
                return new SimpleStringProperty(d.getOrganizationName().trim());
            if (d.getEmail() != null && !d.getEmail().isEmpty())
                return new SimpleStringProperty(d.getEmail());
            return new SimpleStringProperty("Unknown Donor");
        });
        colEmail.setCellValueFactory(new PropertyValueFactory<>("email"));
        colPhone.setCellValueFactory(new PropertyValueFactory<>("phone"));
        if (colType != null) colType.setCellValueFactory(new PropertyValueFactory<>("type"));

        donorTable.setItems(pageList);

        // Fix #20: rich empty-state placeholder
        donorTable.setPlaceholder(buildEmptyState(
            "No donors found.",
            "Click  ＋ Add Donor  to add your first donor record."
        ));

        countryCache = countryDAO.getActiveCountries();
        loadDonors();
    }

    // ── Fix #16: load all donors, then show page 0 ────────────────────────────
    private void loadDonors() {
        allDonors = donorDAO.getAllDonors();
        currentPage = 0;
        refreshPage();
    }

    // Fix #16: apply search filter over the full list, then re-paginate
    @FXML
    public void searchByName() {
        String keyword = searchField.getText().trim().toLowerCase();
        if (keyword.isEmpty()) {
            allDonors = donorDAO.getAllDonors();
        } else {
            allDonors = donorDAO.getAllDonors()
                .stream()
                .filter(d -> {
                    String name = (d.getFirstName() != null ? d.getFirstName() : "")
                                + " " + (d.getLastName() != null ? d.getLastName() : "");
                    String email = d.getEmail() != null ? d.getEmail() : "";
                    String phone = d.getPhone() != null ? d.getPhone() : "";
                    String org   = d.getOrganizationName() != null ? d.getOrganizationName() : "";
                    return name.toLowerCase().contains(keyword)
                        || email.toLowerCase().contains(keyword)
                        || phone.contains(keyword)
                        || org.toLowerCase().contains(keyword);
                })
                .collect(java.util.stream.Collectors.toList());
        }
        currentPage = 0;
        refreshPage();
    }

    // Fix #16: show current page slice and update pagination controls
    private void refreshPage() {
        int total = allDonors.size();
        int totalPages = Math.max(1, (int) Math.ceil((double) total / PAGE_SIZE));
        currentPage = Math.max(0, Math.min(currentPage, totalPages - 1));

        int from = currentPage * PAGE_SIZE;
        int to   = Math.min(from + PAGE_SIZE, total);

        pageList.setAll(allDonors.subList(from, to));

        if (lblPageInfo != null) {
            lblPageInfo.setText(total == 0
                ? "No donors found"
                : String.format("Showing %d–%d of %d donors  |  Page %d / %d",
                    from + 1, to, total, currentPage + 1, totalPages));
        }
        if (btnPrevPage != null) btnPrevPage.setDisable(currentPage == 0);
        if (btnNextPage != null) btnNextPage.setDisable(currentPage >= totalPages - 1);
    }

    @FXML public void prevPage() { currentPage--; refreshPage(); }
    @FXML public void nextPage() { currentPage++; refreshPage(); }

    // ── CRUD ──────────────────────────────────────────────────────────────────

    @FXML
    public void openAddDialog() {
        Donor newDonor = showDonorDialog("Add New Donor", null);
        if (newDonor != null) {
            boolean success = donorDAO.addDonor(newDonor);
            if (success) {
                new Alert(Alert.AlertType.INFORMATION, "Donor added successfully!", ButtonType.OK).showAndWait();
                loadDonors();
            } else {
                new Alert(Alert.AlertType.ERROR, "Failed to add donor. Verify database connection.", ButtonType.OK).showAndWait();
            }
        }
    }

    @FXML
    public void openEditDialog() {
        Donor selected = donorTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            new Alert(Alert.AlertType.WARNING, "Please select a donor to edit.", ButtonType.OK).showAndWait();
            return;
        }
        Donor updatedDonor = showDonorDialog("Edit Donor", selected);
        if (updatedDonor != null) {
            boolean success = donorDAO.updateDonor(updatedDonor);
            if (success) {
                new Alert(Alert.AlertType.INFORMATION, "Donor updated successfully!", ButtonType.OK).showAndWait();
                loadDonors();
            } else {
                new Alert(Alert.AlertType.ERROR, "Failed to update donor.", ButtonType.OK).showAndWait();
            }
        }
    }

    @FXML
    public void deleteDonor() {
        Donor selected = donorTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            new Alert(Alert.AlertType.WARNING, "Please select a donor to delete.", ButtonType.OK).showAndWait();
            return;
        }
        String name = resolveName(selected);
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION,
            "Are you sure you want to delete donor: " + name + "?\n\nThis action cannot be undone.",
            ButtonType.YES, ButtonType.NO);
        confirm.setTitle("Confirm Deletion");
        if (confirm.showAndWait().orElse(ButtonType.NO) == ButtonType.YES) {
            boolean success = donorDAO.deleteDonor(selected.getId());
            if (success) {
                new Alert(Alert.AlertType.INFORMATION, "Donor deleted.", ButtonType.OK).showAndWait();
                loadDonors();
            } else {
                new Alert(Alert.AlertType.ERROR,
                    "Failed to delete donor. They may be linked to existing donations.",
                    ButtonType.OK).showAndWait();
            }
        }
    }

    /**
     * Fix #18: renamed from "sendCommunication" but method name kept for FXML binding.
     * Button label in FXML is now "Log Comm." — no false impression of actual email sending.
     */
    @FXML
    public void sendCommunication() {
        Donor selected = donorTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            new Alert(Alert.AlertType.WARNING, "Please select a donor first.", ButtonType.OK).showAndWait();
            return;
        }
        if (selected.getEmail() == null || selected.getEmail().trim().isEmpty()) {
            new Alert(Alert.AlertType.WARNING, "This donor has no email address on record.", ButtonType.OK).showAndWait();
            return;
        }

        TextInputDialog dialog = new TextInputDialog("Thank you for your generous support!");
        dialog.setTitle("Log Communication");                                       // Fix #18
        dialog.setHeaderText("Logging a communication record for: "                 // Fix #18
                + resolveName(selected) + "\n(Email: " + selected.getEmail() + ")");
        dialog.setContentText("Message / Notes:");

        String message = dialog.showAndWait().orElse(null);
        if (message != null && !message.trim().isEmpty()) {
            try {
                Communication comm = new Communication();
                comm.setDonorId(selected.getId());
                comm.setSubject("Outreach from DMS");
                comm.setBody(message);
                comm.setChannel(Communication.Channel.email);
                comm.setSentAt(java.time.LocalDateTime.now());
                comm.setStatus(Communication.CommStatus.sent);
                new CommunicationDAO().addCommunication(comm);
                new Alert(Alert.AlertType.INFORMATION,
                    "Communication logged for " + resolveName(selected) + ".",   // Fix #18: honest wording
                    ButtonType.OK).showAndWait();
            } catch (Exception e) {
                new Alert(Alert.AlertType.ERROR, "Failed to log communication: " + e.getMessage(), ButtonType.OK).showAndWait();
            }
        }
    }

    @FXML
    public void assignTag() {
        Donor selected = donorTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            new Alert(Alert.AlertType.WARNING, "Please select a donor to assign a tag.", ButtonType.OK).showAndWait();
            return;
        }
        List<model.Tag> tags = new dao.TagDAO().getAllTags();
        if (tags.isEmpty()) {
            new Alert(Alert.AlertType.INFORMATION, "No tags available. Add tags in the database first.", ButtonType.OK).showAndWait();
            return;
        }
        ChoiceDialog<model.Tag> dialog = new ChoiceDialog<>(tags.get(0), tags);
        dialog.setTitle("Assign Tag");
        dialog.setHeaderText("Select a tag for " + resolveName(selected));
        dialog.setContentText("Tag:");
        model.Tag tag = dialog.showAndWait().orElse(null);
        if (tag != null) {
            try {
                boolean success = new DonorTagDAO().addDonorTag(selected.getId(), tag.getId());
                if (success) {
                    new Alert(Alert.AlertType.INFORMATION, "Tag assigned successfully.", ButtonType.OK).showAndWait();
                } else {
                    new Alert(Alert.AlertType.WARNING, "Donor already has this tag.", ButtonType.OK).showAndWait();
                }
            } catch (Exception e) {
                new Alert(Alert.AlertType.ERROR, "Database error: " + e.getMessage(), ButtonType.OK).showAndWait();
            }
        }
    }

    @FXML
    public void viewDetails() {
        Donor selected = donorTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            new Alert(Alert.AlertType.WARNING, "Select a donor first.", ButtonType.OK).showAndWait();
            return;
        }
        Dialog<Void> details = new Dialog<>();
        details.setTitle("Donor Profile: " + resolveName(selected));
        details.getDialogPane().getButtonTypes().add(ButtonType.CLOSE);

        VBox root = new VBox(12);
        root.setPadding(new Insets(20));
        root.setPrefWidth(450);

        String countryDisplay = selected.getCountryCode() != null ? selected.getCountryCode() : "Not Set";
        if (selected.getCountryCode() != null) {
            for (Country c : countryCache) {
                if (c.getCode().equals(selected.getCountryCode())) {
                    countryDisplay = c.getName() + " (" + c.getCode() + ")";
                    break;
                }
            }
        }

        String fullName = (selected.getFirstName() != null ? selected.getFirstName() : "")
                        + (selected.getLastName() != null ? " " + selected.getLastName() : "");
        root.getChildren().addAll(
            new Label("Full Name: " + fullName.trim()),
            new Label("Org Name: " + (selected.getOrganizationName() != null ? selected.getOrganizationName() : "N/A")),
            new Label("Entity Type: " + (selected.getType() != null ? selected.getType() : "N/A")),
            new Label("Email: " + (selected.getEmail() != null ? selected.getEmail() : "N/A")),
            new Label("Phone: " + (selected.getPhone() != null ? selected.getPhone() : "N/A")),
            new Label("City: " + (selected.getCity() != null ? selected.getCity() : "N/A")),
            new Label("Country: " + countryDisplay),
            new Separator(),
            new Label("Relational Audit Log:"),
            new Label("- Communications: " + new CommunicationDAO().getByDonor(selected.getId()).size() + " entries"),
            new Label("- Tags assigned: " + new DonorTagDAO().getByDonor(selected.getId()).size())
        );
        details.getDialogPane().setContent(root);
        details.showAndWait();
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    /** Fix #20: builds a VBox empty-state placeholder with title + hint. */
    private javafx.scene.Node buildEmptyState(String title, String hint) {
        VBox box = new VBox(8);
        box.setAlignment(javafx.geometry.Pos.CENTER);
        Label lbl1 = new Label(title);
        lbl1.setStyle("-fx-font-size:15px; -fx-text-fill:#64748b; -fx-font-weight:bold;");
        Label lbl2 = new Label(hint);
        lbl2.setStyle("-fx-font-size:12px; -fx-text-fill:#94a3b8;");
        box.getChildren().addAll(lbl1, lbl2);
        return box;
    }

    private String resolveName(Donor d) {
        String full = (d.getFirstName() != null ? d.getFirstName() : "")
                    + (d.getLastName() != null ? " " + d.getLastName() : "");
        full = full.trim();
        if (!full.isEmpty()) return full;
        if (d.getOrganizationName() != null && !d.getOrganizationName().trim().isEmpty())
            return d.getOrganizationName().trim();
        return d.getEmail() != null ? d.getEmail() : "Unknown Donor";
    }

    private String countryLabel(Country c) { return c.getName() + " (" + c.getCode() + ")"; }

    private Donor showDonorDialog(String title, Donor existing) {
        Dialog<Donor> dialog = new Dialog<>();
        dialog.setTitle(title);
        dialog.setHeaderText(null);

        ButtonType saveBtn = new ButtonType("Save", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(saveBtn, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.setHgap(15); grid.setVgap(12);
        grid.setPadding(new Insets(25));

        TextField tfFirst  = new TextField(); tfFirst.setPromptText("First Name");
        TextField tfLast   = new TextField(); tfLast.setPromptText("Last Name");
        TextField tfOrg    = new TextField(); tfOrg.setPromptText("Organization Name (if applicable)");
        TextField tfEmail  = new TextField(); tfEmail.setPromptText("email@example.com");
        TextField tfPhone  = new TextField(); tfPhone.setPromptText("+92 300 1234567");
        TextField tfCity   = new TextField(); tfCity.setPromptText("City");
        ComboBox<String> cbType = new ComboBox<>(FXCollections.observableArrayList("individual", "organization"));

        List<String> countryLabels = new ArrayList<>();
        for (Country c : countryCache) countryLabels.add(countryLabel(c));
        ComboBox<String> cbCountry = new ComboBox<>(FXCollections.observableArrayList(countryLabels));
        cbCountry.setPromptText("Select Country");

        if (existing != null) {
            tfFirst.setText(existing.getFirstName() != null ? existing.getFirstName() : "");
            tfLast.setText(existing.getLastName() != null ? existing.getLastName() : "");
            tfOrg.setText(existing.getOrganizationName() != null ? existing.getOrganizationName() : "");
            tfEmail.setText(existing.getEmail() != null ? existing.getEmail() : "");
            tfPhone.setText(existing.getPhone() != null ? existing.getPhone() : "");
            tfCity.setText(existing.getCity() != null ? existing.getCity() : "");
            cbType.setValue(existing.getType() != null ? existing.getType() : "individual");
            if (existing.getCountryCode() != null) {
                for (Country c : countryCache) {
                    if (c.getCode().equals(existing.getCountryCode())) {
                        cbCountry.setValue(countryLabel(c)); break;
                    }
                }
            }
        } else {
            cbType.setValue("individual");
        }

        Label lblOrg = new Label("Organization:");
        tfOrg.setVisible("organization".equals(cbType.getValue()));
        lblOrg.setVisible("organization".equals(cbType.getValue()));
        cbType.setOnAction(e -> {
            boolean isOrg = "organization".equals(cbType.getValue());
            tfOrg.setVisible(isOrg); lblOrg.setVisible(isOrg);
        });

        int row = 0;
        grid.add(new Label("First Name:*"), 0, row); grid.add(tfFirst,   1, row++);
        grid.add(new Label("Last Name:"),   0, row); grid.add(tfLast,    1, row++);
        grid.add(new Label("Type:*"),       0, row); grid.add(cbType,    1, row++);
        grid.add(lblOrg,                    0, row); grid.add(tfOrg,     1, row++);
        grid.add(new Label("Email:*"),      0, row); grid.add(tfEmail,   1, row++);
        grid.add(new Label("Phone:"),       0, row); grid.add(tfPhone,   1, row++);
        grid.add(new Label("City:"),        0, row); grid.add(tfCity,    1, row++);
        grid.add(new Label("Country:*"),    0, row); grid.add(cbCountry, 1, row++);

        dialog.getDialogPane().setContent(grid);

        javafx.scene.Node saveButton = dialog.getDialogPane().lookupButton(saveBtn);
        saveButton.setDisable(true);
        Runnable checkValid = () -> {
            boolean ok = !tfFirst.getText().trim().isEmpty()
                && cbType.getValue() != null
                && !tfEmail.getText().trim().isEmpty()
                && EMAIL_PATTERN.matcher(tfEmail.getText().trim()).matches()
                && cbCountry.getValue() != null;
            saveButton.setDisable(!ok);
        };
        tfFirst.textProperty().addListener((o, ov, nv) -> checkValid.run());
        tfEmail.textProperty().addListener((o, ov, nv) -> checkValid.run());
        cbType.valueProperty().addListener((o, ov, nv) -> checkValid.run());
        cbCountry.valueProperty().addListener((o, ov, nv) -> checkValid.run());
        checkValid.run();

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == saveBtn) {
                String firstName = tfFirst.getText().trim();
                String email     = tfEmail.getText().trim();
                String phone     = tfPhone.getText().trim();

                if (firstName.isEmpty()) {
                    new Alert(Alert.AlertType.WARNING, "First name is required.", ButtonType.OK).showAndWait();
                    return null;
                }
                if (!EMAIL_PATTERN.matcher(email).matches()) {
                    new Alert(Alert.AlertType.WARNING, "Please enter a valid email address.", ButtonType.OK).showAndWait();
                    return null;
                }
                if (!phone.isEmpty() && !PHONE_PATTERN.matcher(phone).matches()) {
                    new Alert(Alert.AlertType.WARNING, "Phone number format is invalid.", ButtonType.OK).showAndWait();
                    return null;
                }
                if (cbCountry.getValue() == null) {
                    new Alert(Alert.AlertType.WARNING, "Please select a country.", ButtonType.OK).showAndWait();
                    return null;
                }

                Donor d = (existing == null) ? new Donor() : existing;
                d.setFirstName(firstName);
                d.setLastName(tfLast.getText().trim());
                d.setEmail(email);
                d.setPhone(phone.isEmpty() ? null : phone);
                d.setCity(tfCity.getText().trim().isEmpty() ? null : tfCity.getText().trim());
                d.setType(cbType.getValue());
                if ("organization".equals(cbType.getValue())) {
                    d.setOrganizationName(tfOrg.getText().trim().isEmpty() ? firstName : tfOrg.getText().trim());
                } else {
                    d.setOrganizationName(null);
                }
                String code = cbCountry.getValue().replaceAll(".*\\((.+)\\)$", "$1");
                d.setCountryCode(code);
                d.setActive(true);
                return d;
            }
            return null;
        });

        return dialog.showAndWait().orElse(null);
    }
}
