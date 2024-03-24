package amar.das.acbook.globalenum;


public enum GlobalConstants {
     ACTIVE("1"),
     INACTIVE("0"),

     BACKUP_ACTIVE_MLG_PDF_NAME("active_mlg_backup"),

    //text file name
    ALL_DETAILS_TEXT_FILE_NAME("_all_data_"),
    TEXT_FILE_FOLDER_NAME("acBookBackupInvoice"),
    //pdf file name
    PDF_FOLDER_NAME("acBookPDF"),
    RUNNING_INVOICE_FILE_NAME("_running_invoice"),
    CALCULATED_INVOICE_FILE_NAME("_calculated_invoice");
    private final String value;

    GlobalConstants(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }
}
