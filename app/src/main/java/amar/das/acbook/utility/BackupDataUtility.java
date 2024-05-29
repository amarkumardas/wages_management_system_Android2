package amar.das.acbook.utility;

import android.content.Context;
import android.database.Cursor;
import android.text.TextUtils;

import amar.das.acbook.Database;
import amar.das.acbook.R;
import amar.das.acbook.globalenum.GlobalConstants;

public class BackupDataUtility {
    public static String[] getTotalActiveAdvanceAndBalanceInfo(Context context){
        String[] ratesInfo = new String[1];
        Database db=Database.getInstance(context);
        String noRateIds=db.getIdsAndReturnNullIfRateIsProvidedOfActiveMLG();
        if(noRateIds!=null){//means no rate ids are there
            ratesInfo[0] ="FOR SEEING TOTAL  ADVANCE  AND  BALANCE  PLEASE  SET  RATE  TO  IDs: "+noRateIds;
            return ratesInfo;
        }
        StringBuilder sb = new StringBuilder();
        try(Cursor cursor=db.getData("SELECT SUM("+Database.COL_13_ADVANCE+") , SUM("+Database.COL_14_BALANCE+") FROM "+Database.TABLE_NAME1+" WHERE ("+Database.COL_8_MAINSKILL1 +"='"+context.getResources().getString(R.string.mestre)+"' OR "+Database.COL_8_MAINSKILL1 +"='"+context.getResources().getString(R.string.laber)+"' OR "+Database.COL_8_MAINSKILL1 +"='"+context.getResources().getString(R.string.women_laber)+"') AND "+Database.COL_12_ACTIVE+"='"+ GlobalConstants.ACTIVE.getValue()+"'")){
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
    public static String[] getActiveSkillCreatedInfo(int numberOfPerson, Context context) {
        String[] backupInfo = new String[2];
        StringBuilder sb1 = new StringBuilder();
        sb1.append("CREATED ON: ").append(MyUtility.get12hrCurrentTimeAndDate());
        backupInfo[0] = sb1.toString();

        StringBuilder sb2 = new StringBuilder();
        sb2.append("BACKUP OF ").append(numberOfPerson)
                .append(" ACTIVE  PEOPLE  SKILLED  IN ( ")
                .append(context.getResources().getString(R.string.mestre)).append(" ")
                .append(context.getResources().getString(R.string.laber)).append(" ")
                .append(context.getResources().getString(R.string.women_laber)).append(" ")
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
             Cursor cursor = db.getData("SELECT " + Database.COL_3_BANKAC + ", " + Database.COL_4_IFSCCODE + ", " + Database.COL_5_BANKNAME + ", " + Database.COL_9_ACCOUNT_HOLDER_NAME + " FROM " + Database.TABLE_NAME1 + " WHERE " + Database.COL_1_ID + "='" + id + "'")) {

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
}
