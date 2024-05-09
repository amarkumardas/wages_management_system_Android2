package amar.das.acbook.globalenum;


public enum GlobalConstants {
     ACTIVE("1"),
     INACTIVE("0"),

     BACKUP_ACTIVE_MLG_PDF_FILE_NAME("ACTIVE_SKILL_MLG_BACKUP"),
     BACKUP_INACTIVE_M_TEXT_FILE_NAME("INACTIVE_SKILL_M_BACKUP"),
    BACKUP_INACTIVE_L_TEXT_FILE_NAME("INACTIVE_SKILL_L_BACKUP"),
    BACKUP_INACTIVE_G_TEXT_FILE_NAME("INACTIVE_SKILL_G_BACKUP"),
    //text file name
    ALL_DETAILS_TEXT_FILE_NAME("_ALL_DATA_"),
    TEXT_FILE_FOLDER_NAME("acBookBackupInvoice"),
    //pdf file name
    PDF_FOLDER_NAME("acBookPDF"),
    RUNNING_INVOICE_FILE_NAME("_RUNNING_INVOICE"),
    CALCULATED_INVOICE_FILE_NAME("_CALCULATED_INVOICE"),
    DEFAULT_BUSINESS_NAME("CONSTRUCTION WORK"),
    DEPOSIT_CODE("1"),//to indicate amount is deposited
    WAGES_CODE("0");//to indicate amount is wages
    private final String value;

    GlobalConstants(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }
}
