package amar.das.acbook.globalenum;


public enum GlobalConstants {
     ACTIVE("1"),
     INACTIVE("0"),

     BACKUP_PDF_NAME("Backup_Active_MLG_");
    private final String value;

    GlobalConstants(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }
}
