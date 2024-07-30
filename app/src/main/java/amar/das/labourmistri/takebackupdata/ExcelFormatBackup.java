package amar.das.labourmistri.takebackupdata;

import android.app.Activity;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.HorizontalAlignment;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.File;
import java.io.FileOutputStream;

import amar.das.labourmistri.Database;
import amar.das.labourmistri.R;
import amar.das.labourmistri.globalenum.GlobalConstants;
import amar.das.labourmistri.utility.BackupDataUtility;
import amar.das.labourmistri.utility.MyUtility;

public class ExcelFormatBackup{
    private Activity context;
    private XSSFWorkbook workBook;//XSSFWorkbook is newer .xlsx format is recommended due to its advantages:Smaller file sizes,Better compatibility with other applications,Improved security features
    private Sheet sheet;
    private Cell cell;
    int excelRow;//it will change dynamically change,if ot execute sequentially then it will work.in multiple thread will not work

    public ExcelFormatBackup(Activity context){
        this.context=context;
        this.workBook = new XSSFWorkbook();// Creating a New HSSF Workbook (.xls format)
        this.excelRow=0;
    }
    public File singleBackupExcelFile(String backupExcelFileName){//return null if error
        String[] personIds;
        byte numberOfFiles=4;
        for (byte fileIndicator = 0; fileIndicator < numberOfFiles; fileIndicator++){

            createNewExcelSheetAndSetRowAs0(fileIndicator);//Create a new sheet in a Workbook and assign a name to it and set row to 0

            if((personIds=BackupDataUtility.getPersonIdsAccordingToFileIndicator(fileIndicator,context)) == null) return null;//means error

            createFirst2RowOfCreateInfoAndTotalAdvanceAndBalanceForSingleFileBackup(personIds.length,fileIndicator);
            excelRow++;//updating excel for space

            for (String id:personIds){//loop
                if(!createExcelOfId(id)) return null;////it create excel fill whether id is active or inactive
            }
        }

//        File excelFile = createExcelFolderInExternalStorageAndReturnFile(backupExcelFileName,workBook);
       // return (excelFile != null)?excelFile:null;//null means error Occurred
        return  createExcelFolderInExternalStorageAndReturnFile(backupExcelFileName,workBook);//null means error Occurred
    }
    private void createFirst2RowOfCreateInfoAndTotalAdvanceAndBalanceForSingleFileBackup(int numberOfPerson,byte fileIndicator) {
        if(fileIndicator==0){//for active mlg
            createFirst2RowOfCreateInfoAndTotalAdvanceAndBalanceForActiveSkill(numberOfPerson);
        }else if (fileIndicator==1) {//for inactive skill m
            createFirst2RowOfCreateInfoAndTotalAdvanceAndBalanceForInActiveSkill(numberOfPerson,GlobalConstants.M_SKILL.getValue());
        }else if (fileIndicator==2) {//for inactive skill l
            createFirst2RowOfCreateInfoAndTotalAdvanceAndBalanceForInActiveSkill(numberOfPerson,GlobalConstants.L_SKILL.getValue());
        }else if (fileIndicator==3) {//for inactive skill g
            createFirst2RowOfCreateInfoAndTotalAdvanceAndBalanceForInActiveSkill(numberOfPerson,GlobalConstants.G_SKILL.getValue());
        }
    }
    public File backupInActiveMOrLOrGDataInExcelFormat(String skillType,String backupExcelFileName){//return null if error
        Database db=Database.getInstance(context);

        createNewExcelSheetAndSetRowAs0(context.getString(R.string.inactive)+" Skill "+skillType);

        String []personIds = db.getIdOfInActiveMOrLOrG(skillType);
        if (personIds == null) return null;//if error
        createFirst2RowOfCreateInfoAndTotalAdvanceAndBalanceForInActiveSkill(personIds.length,skillType);
        excelRow++;//updating excel row so that person data will be start from third line

        for (String id:personIds){//loop
            if(!createExcelOfId(id)) return null;
        }

//        File excelFile = createExcelFolderInExternalStorageAndReturnFile(backupExcelFileName,workBook);
//        return (excelFile != null)?excelFile:null;//null means error Occurred

        return createExcelFolderInExternalStorageAndReturnFile(backupExcelFileName,workBook);//null means error Occurred
    }
    public File backupActiveMLGDataInExcelFormat(String backupExcelFileName){//return null if error
            Database db=Database.getInstance(context);

            createNewExcelSheetAndSetRowAs0(context.getString(R.string.active_skill_m_l_g));

            String []personIds = db.getIdOfActiveMLG();
            if (personIds == null) return null;//if error
            createFirst2RowOfCreateInfoAndTotalAdvanceAndBalanceForActiveSkill(personIds.length);
            excelRow++;//updating excel row so that person data will be start from third line

            for (String id:personIds){//loop
                if(!createExcelOfId(id)) return null;
            }

//            File excelFile = createExcelFolderInExternalStorageAndReturnFile(backupExcelFileName,workBook);
//            return (excelFile != null)?excelFile:null;//null means error Occurred
        return createExcelFolderInExternalStorageAndReturnFile(backupExcelFileName,workBook);
    }
    private void createNewExcelSheetAndSetRowAs0(byte fileIndicator) {
        switch (fileIndicator){
            case 0:{this.sheet = this.workBook.createSheet(context.getString(R.string.active_skill_m_l_g)); excelRow=0;}break;//Create a new sheet in a Workbook and assign a name to it
            case 1:{this.sheet = this.workBook.createSheet(context.getString(R.string.inactive_skill_m)); excelRow=0;}break;//Create a new sheet in a Workbook and assign a name to it
            case 2:{this.sheet = this.workBook.createSheet(context.getString(R.string.inactive_skill_l)); excelRow=0;}break;//Create a new sheet in a Workbook and assign a name to it
            case 3:{this.sheet = this.workBook.createSheet(context.getString(R.string.inactive_skill_g)); excelRow=0;}break;//Create a new sheet in a Workbook and assign a name to it
        }
    }
    private void createNewExcelSheetAndSetRowAs0(String sheetName) {
        this.sheet = this.workBook.createSheet(sheetName);//Create a new sheet in a Workbook and assign a name to it
        excelRow=0;
    }
    private boolean createExcelOfId(String id) {//it create excel fill whether id is active or inactive
        byte indicator =  MyUtility.get_indicator(context,id);
        boolean[] errorDetection = {false};//when ever exception occur in one place it will be updated to true in method.if no exception array will not be updated. so if any where error occur it will hold value true
        try {
            int[] arrayOfTotalWagesDepositRateAccordingToIndicator = MyUtility.getSumOfTotalWagesDepositRateDaysWorkedBasedOnIndicator(context, id, indicator, errorDetection);//if error cause errorDetection will be set true
            String[] wagesHeaderAccordingToIndicator = MyUtility.getWagesHeadersFromDbBasedOnIndicator(context, id, indicator, errorDetection);//THIS SHOULD BE TOP at arrayOfTotalWagesDepositRateAccordingToIndicator   TO AVOID INDEX EXCEPTION
            String[][] recyclerViewWagesData = MyUtility.getAllWagesDetailsFromDbBasedOnIndicator(context, id, indicator, errorDetection);//it  return null   when no data
            String[][] recyclerViewDepositData = MyUtility.getAllDepositFromDb(context, id, errorDetection);//it return null   when no data

            String []personDetails = MyUtility.getPersonDetailsForRunningPDFInvoice(id, context);//id ,name,invoice number
            Row row =sheet.createRow(excelRow++);
            cell=row.createCell(0);
            cell.setCellValue(personDetails[1] + " , " + personDetails[0] + " , " + personDetails[2]);
            cell.setCellStyle(getBackgroundYellowStyle());

            row = sheet.createRow(excelRow++);
            cell = row.createCell(0);
            cell.setCellValue(BackupDataUtility.getPhoneAccountOtherDetailsIfDataIsNotNull(id, context));//all person details like rate,releigin,location
            cell.setCellStyle(getLongTextCellStyle());

            if (!errorDetection[0]) {

                if (recyclerViewDepositData == null && recyclerViewWagesData == null) {//when no data
                      sheet.createRow(excelRow++).createCell(0).setCellValue(context.getResources().getString(R.string.no_data_present));
                }

                if (recyclerViewDepositData != null) {//null means data not present

                    String[] header = new String[]{"DATE", "DEPOSIT", "REMARKS"};
                    row = sheet.createRow(excelRow++);
                    for (int j = 0; j < header.length; j++) {//each time new cell will be reated
                        cell = row.createCell(j);
                        cell.setCellValue(header[j]);
                        cell.setCellStyle(getTextToCenter());
                    }
                    for(int i = 0; i < recyclerViewDepositData.length; i++) {
                        row = sheet.createRow(excelRow++);  // Create a New Row for every new entry in list

                        for (int j = 0; j < recyclerViewDepositData[i].length; j++) {
                             row.createCell(j).setCellValue(recyclerViewDepositData[i][j]);
                        }
                    }
                    String[] totalDeposit = new String[]{"+", MyUtility.convertToIndianNumberSystem(arrayOfTotalWagesDepositRateAccordingToIndicator[indicator + 1]), context.getResources().getString(R.string.star_total_width_star)};
                    row = sheet.createRow(excelRow++);
                    for (int j = 0; j < totalDeposit.length; j++) {
                        cell = row.createCell(j);
                        cell.setCellValue(totalDeposit[j]);
                        if(j==1){//1 is a column of total deposit
                            cell.setCellStyle(getWrapTextCellStyle());//so that if text long then visivle all letters
                        }
                    }
                    sheet.createRow(excelRow++).createCell(0);//for space 1 line
                }

                if (recyclerViewWagesData != null) {//null means data not present
                    row = sheet.createRow(excelRow++);
                    for (int j = 0; j < wagesHeaderAccordingToIndicator.length; j++) {
                        cell = row.createCell(j);
                        cell.setCellValue(wagesHeaderAccordingToIndicator[j]);
                        cell.setCellStyle(getTextToCenter());
                    }
                    for (int i = 0; i < recyclerViewWagesData.length; i++) {
                        row = sheet.createRow(excelRow++);  // Create a New Row for every new entry in list

                        for (int j = 0; j < recyclerViewWagesData[i].length; j++) {
                            cell = row.createCell(j);
                            cell.setCellValue(recyclerViewWagesData[i][j]);
                        }
                    }
                    String[] totalWages = MyUtility.getTotalOfWagesAndWorkingDaysFromDbBasedOnIndicator(indicator, arrayOfTotalWagesDepositRateAccordingToIndicator, context);
                    row = sheet.createRow(excelRow++);
                    for (int j = 0; j < totalWages.length; j++) {
                        cell = row.createCell(j);
                        cell.setCellValue(totalWages[j]);
                        if(j==1){//1 is a column of total wages
                            cell.setCellStyle(getWrapTextCellStyle());//so that if text long then visible all letters
                        }
                    }
                }
                 sheet.createRow(excelRow++).createCell(0);//for space 1 line

            } else return false; //if error occurred
        }catch (Exception x){
            x.printStackTrace();
            return false;
        }
        return true;
    }
    private void createFirst2RowOfCreateInfoAndTotalAdvanceAndBalanceForInActiveSkill(int numberOfPerson,String skillType){
        Row row1 = sheet.createRow(excelRow++);
        cell = row1.createCell(0);
        cell.setCellValue(getInActiveSkillCreatedInfo(numberOfPerson,skillType));
        cell.setCellStyle(getLongTextCellStyle());

        row1 = sheet.createRow(excelRow++);
        cell = row1.createCell(0);
        cell.setCellValue(getInActiveAdvanceAndBalance(skillType));
        cell.setCellStyle(getLongTextCellStyle());
    }
    private void createFirst2RowOfCreateInfoAndTotalAdvanceAndBalanceForActiveSkill(int numberOfPerson){
        Row row1 = sheet.createRow(excelRow++);
        cell = row1.createCell(0);
        cell.setCellValue(getActiveSkillMLGCreatedInfo(numberOfPerson));
        cell.setCellStyle(getLongTextCellStyle());

        row1 = sheet.createRow(excelRow++);
        cell = row1.createCell(0);
        cell.setCellValue(getActiveSkillMLGAdvanceAndBalance());
        cell.setCellStyle(getLongTextCellStyle());
    }
    private String getInActiveSkillCreatedInfo(int numberOfPerson,String skillType) {
        StringBuilder sb = new StringBuilder();
        String[] array = BackupDataUtility.getInActiveSkillCreatedInfo(numberOfPerson,skillType);
        sb.append(array[0]).append(" ").append(array[1]);
        return sb.toString();
    }
    private String getActiveSkillMLGCreatedInfo(int numberOfPerson) {
        StringBuilder sb = new StringBuilder();
        String[] array = BackupDataUtility.getActiveSkillMLGCreatedInfo(numberOfPerson);
        sb.append(array[0]).append(" ").append(array[1]);
        return sb.toString();
    }
    private String getInActiveAdvanceAndBalance(String skillType){
        StringBuilder sb = new StringBuilder();
        sb.append(BackupDataUtility.getTotalInActiveMOrLOrGAdvanceAndBalanceInfo(context,skillType)[0]);
        return sb.toString();
//        String[] array=BackupDataUtility.getTotalActiveMLGAdvanceAndBalanceInfo(context);
//        for (String str:array) {
//            sb.append(str);
//        }
    }
    private String getActiveSkillMLGAdvanceAndBalance(){
        StringBuilder sb = new StringBuilder();
        sb.append(BackupDataUtility.getTotalActiveMLGAdvanceAndBalanceInfo(context)[0]);
        return sb.toString();
//        String[] array=BackupDataUtility.getTotalActiveMLGAdvanceAndBalanceInfo(context);
//        for (String str:array) {
//            sb.append(str);
//        }
    }
    private File createExcelFolderInExternalStorageAndReturnFile(String uniqueFileName, XSSFWorkbook workBook) {//return null when exception
        try {//externalFileDir is passed as string because this class is not extended with AppCompatActivity

            if (MyUtility.isFolderExistIfNotExistCreateIt(context.getExternalFilesDir(null).toString(),GlobalConstants.EXCEL_FOLDER_NAME.getValue())) {
                File filePath = new File(context.getExternalFilesDir(null).toString() + File.separator + GlobalConstants.EXCEL_FOLDER_NAME.getValue() + File.separator  + uniqueFileName + ".xlsx");//.xlsx is new format.File.separator works consistently across different operating systems.On Windows systems, it's typically a backslash (\). On Unix-based systems (including macOS and Linux), it's a forward slash (/).Correct Path Construction: By using File.separator, you avoid hardcoding path separators in your code, making it more adaptable and less prone to errors when running on different platforms.
                 workBook.write(new FileOutputStream(filePath.getAbsolutePath()));//if FileOutputStream cannot find file then it will create automatically
                // return filePath.getAbsolutePath();//returning created file absolute path
//                return (filePath != null)?filePath:null;//null means error Occurred
                return  filePath;//null means error Occurred
            }else return null;//error

        }catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
    private Font getCellFront(){
      Font font = workBook.createFont();
      font.setFontHeightInPoints((short) 8); // Adjust font size as needed
      font.setFontName("Arial"); // Set desired font name (optional)
      return font;
    }
    private CellStyle getLongTextCellStyle(){
        CellStyle cellStyle = workBook.createCellStyle();
        cellStyle.setFont(getCellFront());//if you want to apply front to cell
        return cellStyle;
    }
    private CellStyle getWrapTextCellStyle(){
        CellStyle cellStyle = workBook.createCellStyle();
        cellStyle.setWrapText(true);//sets the WRAP_TEXT attribute to true,SO that long text will automatically break into multiple lines within the same cell boundaries.
        return cellStyle;
    }
    private CellStyle getTextToCenter() {// Cell style for header row
        CellStyle style = workBook.createCellStyle();
        style.setAlignment(HorizontalAlignment.CENTER);
        return style;
    }
    private CellStyle getBackgroundYellowStyle() {// Cell style for header row
        CellStyle style = workBook.createCellStyle();
        style.setFillForegroundColor(IndexedColors.YELLOW.getIndex());//set background color
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);//setFillPattern: This is a method of the cell style object that defines the pattern used to fill the background of the cell. Apache POI provides various pre-defined patterns like FillPatternType.SOLID_FOREGROUND, FillPatternType.NO_FILL, FillPatternType.FINE_DOTS, etc.
        return style;
    }
}
