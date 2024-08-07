package amar.das.labourmistri.utility;

import android.content.Context;
import android.database.Cursor;
import android.os.Environment;
import android.os.StatFs;
import android.text.TextUtils;
import android.text.format.Formatter;

import java.io.File;

import amar.das.labourmistri.Database;
import amar.das.labourmistri.R;
import amar.das.labourmistri.globalenum.GlobalConstants;
import amar.das.labourmistri.sharedpreferences.SharedPreferencesHelper;

public class BackupDataUtility {
    public static String[] getTotalInActiveMOrLOrGAdvanceAndBalanceInfo(Context context, String skillType){//only 1 size of array
        String[] ratesInfo = new String[1];
        Database db=Database.getInstance(context);
        String noRateIds=db.getIdOfSpecificSkillAndReturnNullIfRateIsProvidedOfActiveOrInactiveMLG(skillType,false);
        if(noRateIds!=null){//means no rate ids are there
            ratesInfo[0] ="TO KNOW TOTAL  ADVANCE  AND  BALANCE  PLEASE  SET  RATE  TO  IDs: "+noRateIds;
            return ratesInfo;
        }
        StringBuilder sb = new StringBuilder();
        try(Cursor cursor=db.getData("SELECT SUM("+Database.COL_13_ADVANCE+") , SUM("+Database.COL_14_BALANCE+") FROM "+Database.PERSON_REGISTERED_TABLE +" WHERE "+Database.COL_8_MAINSKILL1 +"='"+skillType+"' AND "+Database.COL_12_ACTIVE+"='"+GlobalConstants.INACTIVE_PEOPLE.getValue()+"'")){
            cursor.moveToFirst();
            sb.append("BASED ON PREVIOUS CALCULATED RATE. TOTAL ADVANCE  Rs: ")
                    .append(MyUtility.convertToIndianNumberSystem(cursor.getLong(0)))
                    .append(" , TOTAL BALANCE  Rs: ")
                    .append(MyUtility.convertToIndianNumberSystem(cursor.getLong(1)));

            ratesInfo[0] = sb.toString();
            return ratesInfo;
        }catch (Exception x){
            x.printStackTrace();
            return new String[]{"error"};
        }
    }
    public static String[] getTotalActiveMLGAdvanceAndBalanceInfo(Context context){//array of one size only
        String[] ratesInfo = new String[1];
        Database db=Database.getInstance(context);
        String noRateIds=db.getIdsAndReturnNullIfRateIsProvidedOfActiveMLG();
        if(noRateIds!=null){//means no rate ids are there
            ratesInfo[0] ="TO KNOW TOTAL  ADVANCE  AND  BALANCE  PLEASE  SET  RATE  TO  IDs: "+noRateIds;
            return ratesInfo;
        }
        StringBuilder sb = new StringBuilder();
        try(Cursor cursor=db.getData("SELECT SUM("+Database.COL_13_ADVANCE+") , SUM("+Database.COL_14_BALANCE+") FROM "+Database.PERSON_REGISTERED_TABLE +" WHERE ("+Database.COL_8_MAINSKILL1 +"='"+GlobalConstants.M_SKILL.getValue()+"' OR "+Database.COL_8_MAINSKILL1 +"='"+GlobalConstants.L_SKILL.getValue()+"' OR "+Database.COL_8_MAINSKILL1 +"='"+GlobalConstants.G_SKILL.getValue()+"') AND "+Database.COL_12_ACTIVE+"='"+ GlobalConstants.ACTIVE_PEOPLE.getValue()+"'")){
            cursor.moveToFirst();
            sb.append("BASED ON PREVIOUS CALCULATED RATE. TOTAL ADVANCE  Rs: ")
                    .append(MyUtility.convertToIndianNumberSystem(cursor.getLong(0)))
                    .append(" , TOTAL BALANCE  Rs: ")
                    .append(MyUtility.convertToIndianNumberSystem(cursor.getLong(1)));

            ratesInfo[0] = sb.toString();
            return ratesInfo;
        }catch (Exception x){
            x.printStackTrace();
            return new String[]{"error"};
        }
    }
    public static String[] getInActiveSkillCreatedInfo(int numberOfPerson,String skillType) {//only 2 size
        String[] backupInfo = new String[2];
        StringBuilder sb1 = new StringBuilder();
        sb1.append("CREATED ON: ").append(MyUtility.get12hrCurrentTimeAndDate());
        backupInfo[0] = sb1.toString();

        StringBuilder sb2 = new StringBuilder();
        sb2.append("BACKUP OF ").append(numberOfPerson)
                .append(" INACTIVE  PEOPLE  SKILLED  IN ( ")
                .append(skillType).append(" ")
                .append("). SORTED ACCORDING TO  ID");
        backupInfo[1] = sb2.toString();
        return backupInfo;
    }
    public static String[] getActiveSkillMLGCreatedInfo(int numberOfPerson) {//ONLY two size
        String[] backupInfo = new String[2];
        StringBuilder sb1 = new StringBuilder();
        sb1.append("CREATED ON: ").append(MyUtility.get12hrCurrentTimeAndDate());
        backupInfo[0] = sb1.toString();

        StringBuilder sb2 = new StringBuilder();
        sb2.append("BACKUP OF ").append(numberOfPerson)
                .append(" ACTIVE  PEOPLE  SKILLED  IN ( ")
                .append(GlobalConstants.M_SKILL.getValue()).append(" ")
                .append(GlobalConstants.L_SKILL.getValue()).append(" ")
                .append(GlobalConstants.G_SKILL.getValue()).append(" ")
                .append("). SORTED ACCORDING TO  ID");
        backupInfo[1] = sb2.toString();
        return backupInfo;
    }
    public static String getPhoneAccountOtherDetailsIfDataIsNotNull(String id,Context context) {
        StringBuilder sb=new StringBuilder();
        String phoneNumber=MyUtility.getActiveOrBothPhoneNumber(id,context,false);
        if(!TextUtils.isEmpty(phoneNumber)){//checks for null or ""
            sb.append("PHONE: ").append(phoneNumber).append("\n");//phone number
        }
        String accountDetails= getAccountDetailsIfDataIsNotNull(id,context);
        if(accountDetails!=null){
            sb.append(accountDetails).append("\n\n");//account details
        }
        sb.append(MyUtility.getOtherDetails(id,context));//other details like aadhaar,location,religion,total worked days etc
        return sb.toString();
    }
    public static String getAccountDetailsIfDataIsNotNull(String id,Context context) {//if error return null
        // try (Database db = new Database(context);
        try (Database db =Database.getInstance(context);
             Cursor cursor = db.getData("SELECT " + Database.COL_3_BANKAC + ", " + Database.COL_4_IFSCCODE + ", " + Database.COL_5_BANKNAME + ", " + Database.COL_9_ACCOUNT_HOLDER_NAME + " FROM " + Database.PERSON_REGISTERED_TABLE + " WHERE " + Database.COL_1_ID + "='" + id + "'")) {

            StringBuilder sb = new StringBuilder("\n");
            if (cursor != null && cursor.moveToFirst()) {
                //appear text if no data
//                sb.append("\nBANK NAME: ").append(cursor.getString(2) != null ? cursor.getString(2) : "").append("\n");
//                sb.append("A/C HOLDER NAME: ").append(cursor.getString(3) != null ? cursor.getString(3) : "").append("\n");
//                sb.append("A/C: ").append(cursor.getString(0) != null ? convertToReadableNumber(cursor.getString(0)) : "").append("\n");
//                sb.append("IFSC CODE: ").append(cursor.getString(1) != null ? cursor.getString(1) : "");

//                sb.append((cursor.getString(2) != null) ? "\nBANK NAME: "+cursor.getString(2) : "").append("\n");
//                sb.append((cursor.getString(3) != null) ? "A/C HOLDER NAME: "+cursor.getString(3) : "").append("\n");
//                sb.append((cursor.getString(0) != null) ? "A/C: "+convertToReadableNumber(cursor.getString(0)) : "").append("\n");
//                sb.append((cursor.getString(1) != null) ? "IFSC CODE: "+cursor.getString(1) : "");
//or
                if (!TextUtils.isEmpty(cursor.getString(2))){//TextUtils.isEmpty() checks for "" and null
                    sb.append("BANK NAME: ").append(cursor.getString(2)).append("\n");
                }
                if (!TextUtils.isEmpty(cursor.getString(3))) {
                    sb.append("A/C HOLDER NAME: ").append(cursor.getString(3)).append("\n");
                }
                if (!TextUtils.isEmpty(cursor.getString(0))) {
                    sb.append("A/C: ").append(MyUtility.convertToReadableNumber(cursor.getString(0))).append("\n");
                }
                if (!TextUtils.isEmpty(cursor.getString(1))) {
                    sb.append("IFSC CODE: ").append(cursor.getString(1));
                }
            }
            return sb.toString().trim();
        } catch (Exception ex) {
            ex.printStackTrace();
            return null;
        }
    }
    public static String[] getPersonIdsAccordingToFileIndicator(byte fileIndicator,Context context){//if error return null
        Database db = Database.getInstance(context);
        switch (fileIndicator) {
            case 0: return db.getIdOfActiveMLG();
            case 1: return db.getIdOfInActiveMOrLOrG(GlobalConstants.M_SKILL.getValue());
            case 2: return db.getIdOfInActiveMOrLOrG(GlobalConstants.L_SKILL.getValue());
            case 3: return db.getIdOfInActiveMOrLOrG(GlobalConstants.G_SKILL.getValue());
            default:return null;
        }
    }
    public static long checkDeviceInternalStorageAvailabilityInMB(Context context){
        try {
            File path = Environment.getDataDirectory();//Return the user data directory.return type FILE and Environment class Provides access to environment variables.
            StatFs stat = new StatFs(path.getPath());//Construct a new StatFs for looking at the stats of the filesystem at path.
            long blockSize = stat.getBlockSizeLong();//The size, in bytes, of a block on the file system. This corresponds to the Unix statvfs.f_frsize field.
            long availableBlocks = stat.getAvailableBlocksLong();//The number of bytes that are free on the file system and available to applications.
            String format = Formatter.formatFileSize(context, availableBlocks * blockSize);//return available internal storage memory like 9.66 GB
            format = format.trim();//for safer side

            StringBuilder stringBuilder = new StringBuilder();
            for (int i = 0; i < format.length(); i++) {
                if (format.charAt(i) == ' ' || Character.isAlphabetic(format.charAt(i)))
                    break;
                stringBuilder.append(format.charAt(i));
            }
            return (long) (Float.parseFloat(stringBuilder.toString())*1024);//converted to mb
        }catch (Exception x){
            x.printStackTrace();
        }
        return 0;
    }
    public static String convertDaysToPeriod(int days,Context context) { //return 2 YEARS 12 MONTHS 20 DAYS
        StringBuilder period = new StringBuilder();

        int years = days / 365;
        days %= 365; // Remaining days after calculating years

        if (years > 0) {
            period.append(years).append(" ").append(context.getString(R.string.year)).append(years > 1 ? context.getString(R.string.s)+" " : " "); // Add "s" for plural years
        }

        if (days >= 30) {
            int months = days / 30;
            days %= 30; // Remaining days after calculating months
            //            if (period.length() > 0) {
//                period.append(", "); //if there is year then Add comma and space if there's already a period
//            }
            period.append(months).append(" ").append(context.getString(R.string.month)).append(months > 1 ?  context.getString(R.string.s)+" " : " ");
        }

        if (days >= 7 && period.length() == 0) { // Only add weeks if no years or months
            int weeks = days / 7;
            days %= 7;
            period.append(weeks).append(" ").append(context.getString(R.string.week)).append(weeks > 1 ?  context.getString(R.string.s)+" " : " ");
        }

        if (days > 0) {// Handle remaining days (optional)
            period.append(days).append(" ").append(context.getString(R.string.day)).append(days > 1 ?  context.getString(R.string.s)+" " : "");
        }

        return period.toString().isEmpty() ? "0 "+context.getString(R.string.day) : period.toString(); // Handle 0 days case
    }
    public static boolean didUserBackupDataTodayOrNeverBackedUp(Context context, boolean forCheckingUserBackupDataTodayTrueAndForUserNeverBackupItsDataFalse) {// Check if any backup date is today's date
        String dateDatabaseBackup= SharedPreferencesHelper.getString(context,SharedPreferencesHelper.Keys.DATE_DATABASE_BACKUP.name(),null);//these are the variables which get updated when user backup data
        String dateBackupEachFile=SharedPreferencesHelper.getString(context,SharedPreferencesHelper.Keys.DATE_BACKUP_EACH_FILE.name(),null);
        String dateSingleBackupFile=SharedPreferencesHelper.getString(context,SharedPreferencesHelper.Keys.DATE_SINGLE_BACKUP_FILE.name(),null);
        if(forCheckingUserBackupDataTodayTrueAndForUserNeverBackupItsDataFalse) {
            return ((!TextUtils.isEmpty(dateDatabaseBackup) && isBackupDataDateIsTodayDate(dateDatabaseBackup)) || (!TextUtils.isEmpty(dateBackupEachFile) && isBackupDataDateIsTodayDate(dateBackupEachFile)) || (!TextUtils.isEmpty(dateSingleBackupFile) && isBackupDataDateIsTodayDate(dateSingleBackupFile)));//if any three ways user has backed up data then return true
        }else{
            return (TextUtils.isEmpty(dateDatabaseBackup) && TextUtils.isEmpty(dateBackupEachFile) && TextUtils.isEmpty(dateSingleBackupFile));//initially when user download app then this will execute because all three variable would be null means user has not backed up data single time or just now user has download this app
        }
    }
//    public static  boolean isUserNeverBackupItsData(Context context) {//if three backup variable is null that means user has download the app just now or user has never backup its data
//        String dateDatabaseBackup = SharedPreferencesHelper.getString(context, SharedPreferencesHelper.Keys.DATE_DATABASE_BACKUP.name(), null);//these are the variables which get updated when user backup data
//        String dateBackupEachFile = SharedPreferencesHelper.getString(context, SharedPreferencesHelper.Keys.DATE_BACKUP_EACH_FILE.name(), null);
//        String dateSingleBackupFile = SharedPreferencesHelper.getString(context, SharedPreferencesHelper.Keys.DATE_SINGLE_BACKUP_FILE.name(), null);
//
//        return (TextUtils.isEmpty(dateDatabaseBackup) && TextUtils.isEmpty(dateBackupEachFile) && TextUtils.isEmpty(dateSingleBackupFile));//initially when user download app then this will execute because all three variable would be null means user has not backed up data single time or just now user has download this app
//    }
    private static boolean isBackupDataDateIsTodayDate(String backupDate){
        if(TextUtils.isEmpty(backupDate)){return false;}

        String extractDate=MyUtility.getDateFromLastBackupDate(backupDate);
        if(TextUtils.isEmpty(extractDate)){return false;}

        return extractDate.equals(MyUtility.getDateFromLastBackupDate(MyUtility.get12hrCurrentTimeAndDateForLastBackup()));//checking user backup date by taking today date
    }

}
