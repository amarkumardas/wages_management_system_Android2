package amar.das.acbook.globalenum;


public enum GlobalConstants {
     ACTIVE_PEOPLE("1"),
     INACTIVE_PEOPLE("0"),
     DEPOSIT_CODE("1"),//to indicate amount is deposited
     WAGES_CODE("0"),//to indicate amount is wages
     DATABASE_BACKUP_FILE_NAME("DATABASE_BACKUP"),
     SINGLE_BACKUP_PDF_FILE_NAME("ALL_DATA_SINGLE_FILE_BACKUP"),
     SINGLE_BACKUP_TEXT_FILE_NAME("ALL_DATA_SINGLE_FILE_BACKUP"),
     SINGLE_BACKUP_EXCEL_FILE_NAME("ALL_DATA_SINGLE_FILE_BACKUP"),
     BACKUP_ACTIVE_MLG_PDF_FILE_NAME("ACTIVE_SKILL_MLG_BACKUP"),
     BACKUP_ACTIVE_MLG_EXCEL_FILE_NAME("ACTIVE_SKILL_MLG_BACKUP"),
     BACKUP_ACTIVE_MLG_TEXT_FILE_NAME("ACTIVE_SKILL_MLG_BACKUP"),
     BACKUP_INACTIVE_M_TEXT_FILE_NAME("INACTIVE_SKILL_M_BACKUP"),
     BACKUP_INACTIVE_M_PDF_FILE_NAME("INACTIVE_SKILL_M_BACKUP"),
     BACKUP_INACTIVE_M_EXCEL_FILE_NAME("INACTIVE_SKILL_M_BACKUP"),
    BACKUP_INACTIVE_L_TEXT_FILE_NAME("INACTIVE_SKILL_L_BACKUP"),
    BACKUP_INACTIVE_L_PDF_FILE_NAME("INACTIVE_SKILL_L_BACKUP"),
    BACKUP_INACTIVE_L_EXCEL_FILE_NAME("INACTIVE_SKILL_L_BACKUP"),
    BACKUP_INACTIVE_G_TEXT_FILE_NAME("INACTIVE_SKILL_G_BACKUP"),
    BACKUP_INACTIVE_G_PDF_FILE_NAME("INACTIVE_SKILL_G_BACKUP"),
    BACKUP_INACTIVE_G_EXCEL_FILE_NAME("INACTIVE_SKILL_G_BACKUP"),

    //text file name
    ALL_DETAILS_TEXT_FILE_NAME("_ALL_DATA_"),

    //folders
    BACKUP_CALCULATED_INVOICE_TEXT_FOLDER_NAME("Backup_Calculated_Invoice_Text_Folder"),
    AUDIO_FOLDER_NAME("Audio_Folder"),
    REGISTERED_IMAGE_FOLDER_NAME("Image_Folder"),
    PDF_FOLDER_NAME("Pdf_Folder_Empty"),
    EXCEL_FOLDER_NAME("Excel_Folder_Empty"),
    DATABASE_FOLDER_NAME("Database_Folder_Empty"),

    AUDIO_FILE_NAME("AUDIO"),
    REGISTERED_IMAGE_FILE_NAME("IMG"),
    USER_SELECTED_PDF_FORMAT("1"), USER_SELECTED_TEXT_FORMAT("2"), USER_SELECTED_EXCEL_FORMAT("3"),
    RUNNING_INVOICE_FILE_NAME("_RUNNING_INVOICE"),
    CALCULATED_INVOICE_FILE_NAME("_CALCULATED_INVOICE"),
    DEFAULT_BUSINESS_NAME("CONSTRUCTION WORK"),
    //inactive days
    TWO_WEEKS_DEFAULT("15"),
    THREE_WEEKS("22"),
    ONE_MONTH("30");
    private final String value;
    GlobalConstants(String value) {
        this.value = value;
    }
    public String getValue() {
        return value;
    }
}
