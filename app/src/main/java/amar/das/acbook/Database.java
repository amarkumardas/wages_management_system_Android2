package amar.das.acbook;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteBlobTooBigException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.graphics.Color;
import android.util.Log;
import android.widget.Toast;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import amar.das.acbook.pdfgenerator.MakePdf;
import amar.das.acbook.textfilegenerator.TextFile;
import amar.das.acbook.utility.MyUtility;
public class Database extends SQLiteOpenHelper {
    public final static int Database_Version=1;//5to update db version just increase the value by 1.when this value is increase then constructor is called
    SQLiteDatabase db;
    Context context;
    public final static String DATABASE_NAME="person_db";
    private static Database instance; // Step 1: Private static instance

    //table 1---------------------------------------------------------------------------------------
    public final static String TABLE_NAME1="person_details_table";
    public final static String COL_1_ID ="ID";
    public final static String COL_2_NAME ="NAME";
    public final static String COL_3_BANKAC ="BANKACCOUNT";
    public final static String COL_4_IFSCCODE ="IFSCCODE";
    public final static String COL_5_BANKNAME ="BANKNAME";
    public final static String COL_6_AADHAAR_NUMBER ="AADHARCARD";
    public final static String COL_7_ACTIVE_PHONE1 ="PHONE";
    public final static String COL_8_MAINSKILL1 ="MAINSKILL1";//skill1
    public final static String COL_9_ACCOUNT_HOLDER_NAME ="FATHERNAME";
    public final static String COL_10_IMAGE ="IMAGE";
    public final static String COL_11_ACTIVE_PHONE2 ="ACHOLDER";
    public final static String COL_12_ACTIVE ="ACTIVE";
    public final static String COL_13_ADVANCE ="ADVANCE";
    public final static String COL_14_BALANCE ="BALANCE";
    public final static String COL_15_LATESTDATE ="LATESTDATE";
    public final static String COL_16_TIME ="TIME";//To arrange today's enter data to show on top
    public final static String COL_17_LOCATION ="LOCATION";
    public final static String COL_18_RELIGION="RELIGION";

//----------------------------------TABLE_ACTIVE_MESTRE--------------------------------------------------
    public final static String TABLE0_ACTIVE_MESTRE ="active_mestre_wages_table";
    public final static String COL_1_ID_AM ="ID";//here SYSTEM_DATE and time and id is acting like primary key
    public final static String COL_13_SYSTEM_DATETIME_AM ="SYSTEM_DATETIME";//this is added for making unique record,there is also again another date column taken because if user don't enter current date and enter previous date then there may be very rare chance that it matches previous date,time and id,so this can make row duplicate.so this system date is taken to avoid
    public final static String COL_2_DATE_AM ="USER_DATE";
    public final static String COL_3_TIME_AM ="TIME";//here date and time and id is acting like primary key thats why time column is taken
    public final static String COL_4_MICPATH_AM ="MICPATH";
    public final static String COL_5_REMARKS_AM ="REMARKS";
    public final static String COL_6_WAGES_AM ="WAGES";
    public final static String COL_7_DEPOSIT_AM ="DEPOSIT";
    public final static String COL_8_P1_AM ="P1";
    public final static String COL_9_P2_AM ="P2";
    public final static String COL_10_P3_AM ="P3";
    public final static String COL_11_P4_AM ="P4";
    public final static String COL_12_ISDEPOSITED_AM ="ISDEPOSITED";

//--------------------------------------TABLE_ACTIVE_LG------------------------------------------------
    public final static String TABLE1_ACTIVE_LG ="active_l_g_wages_table";
    public final static String COL_1_ID_ALG ="ID";//here SYSTEM_DATE and time and id is acting like primary key
    public final static String COL_13_SYSTEM_DATETIME_ALG ="SYSTEM_DATETIME";
    public final static String COL_2_DATE_ALG ="USER_DATE";
    public final static String COL_3_TIME_ALG ="TIME";//here SYSTEM_DATE and time and id is acting like primary key
    public final static String COL_4_MICPATH_ALG ="MICPATH";
    public final static String COL_5_REMARKS_ALG ="REMARKS";
    public final static String COL_6_WAGES_ALG ="WAGES";
    public final static String COL_7_DEPOSIT_ALG ="DEPOSIT";
    public final static String COL_8_P1_ALG ="P1";
    public final static String COL_9_P2_ALG ="P2";
    public final static String COL_10_P3_ALG ="P3";
    public final static String COL_11_P4_ALG ="P4";
    public final static String COL_12_ISDEPOSITED_ALG ="ISDEPOSITED";

    //---------------------------------TABLE_IN_ACTIVE_MESTRE----------------------------------------------------
    public final static String TABLE2_IN_ACTIVE_MESTRE ="in_active_mestre_wages_table";
    public final static String COL_1_ID_IAM ="ID";//here SYSTEM_DATE and time and id is acting like primary key
    public final static String COL_13_SYSTEM_DATETIME_IAM ="SYSTEM_DATETIME";
    public final static String COL_2_DATE_IAM ="USER_DATE";
    public final static String COL_3_TIME_IAM ="TIME";//here SYSTEM_DATE and time and id is acting like primary key
    public final static String COL_4_MICPATH_IAM ="MICPATH";
    public final static String COL_5_REMARKS_IAM ="REMARKS";
    public final static String COL_6_WAGES_IAM ="WAGES";
    public final static String COL_7_DEPOSIT_IAM ="DEPOSIT";
    public final static String COL_8_P1_IAM ="P1";
    public final static String COL_9_P2_IAM ="P2";
    public final static String COL_10_P3_IAM ="P3";
    public final static String COL_11_P4_IAM ="P4";
    public final static String COL_12_ISDEPOSITED_IAM ="ISDEPOSITED";

    //-------------------------------------TABLE_ACTIVE_IN_LG------------------------------------------
    public final static String TABLE3_IN_ACTIVE_LG ="in_active_l_g_wages_table";
    public final static String COL_1_ID_IALG ="ID";//here SYSTEM_DATE and time and id is acting like primary key
    public final static String COL_13_SYSTEM_DATETIME_IALG ="SYSTEM_DATETIME";
    public final static String COL_2_DATE_IALG ="USER_DATE";
    public final static String COL_3_TIME_IALG ="TIME";//here SYSTEM_DATE and time and id is acting like primary key
    public final static String COL_4_MICPATH_IALG ="MICPATH";
    public final static String COL_5_REMARKS_IALG ="REMARKS";
    public final static String COL_6_WAGES_IALG ="WAGES";
    public final static String COL_7_DEPOSIT_IALG ="DEPOSIT";
    public final static String COL_8_P1_IALG ="P1";
    public final static String COL_9_P2_IALG ="P2";
    public final static String COL_10_P3_IALG ="P3";
    public final static String COL_11_P4_IALG ="P4";
    public final static String COL_12_ISDEPOSITED_IALG ="ISDEPOSITED";

    //table 3---------------------------------------------------------------------------------------
    public final static String TABLE_NAME3="rate_skills_indicator_table";
    public final static String COL_31_ID ="ID";
    public final static String COL_32_R1 ="R1";
    public final static String COL_33_R2 ="R2";
    public final static String COL_34_R3 ="R3";
    public final static String COL_35_R4 ="R4";
    public final static String COL_36_SKILL2 ="SKILL2";
    public final static String COL_37_SKILL3 ="SKILL3";
    public final static String COL_38_SKILL4 ="SKILL4";
    public final static String COL_39_INDICATOR ="INDICATOR";
    public final static String COL_391_STAR ="STAR";
    public final static String COL_392_LEAVINGDATE ="LEAVINGDATE";
    public final static String COL_393_REFFERAL_REMARKS ="REFFERAL";
    public final static String COL_394_INVOICE1 ="PDF1";//or invoice
    public final static String COL_395_INVOICE2 ="PDF2";
    public final static String COL_396_PDFSEQUENCE ="PDFSEQUENCE";//invoice sequence
    public final static String COL_397_TOTAL_WORKED_DAYS="TOTAL_WORKED_DAYS";
    public final static String COL_398_RETURNINGDATE ="RETURNDATE";
    //table 4---------------------------------------------------------------------------------------
    public final static String TABLE_NAME4="location_table";
    public final static String COL_41_LOCATION ="LOCATION";

    //table 5---------------------------------------------------------------------------------------
    public final static String TABLE_NAME5="religion_table";
    public final static String COL_51_RELIGION="RELIGION";

    //history table
//    public final static String TABLE_HISTORY ="history_table";
//    public final static String COL_13_SYSTEM_DATE_H ="SYSTEM_DATE";
//    public final static String COL_1_ID_H ="ID";//here date and time and id is acting like primary key
//    public final static String COL_2_DATE_H ="DATE";//here date is user given date and time and id is acting like primary key
//    public final static String COL_3_TIME_H ="TIME";//here date and time and id is acting like primary key
//    public final static String COL_4_MICPATH_H ="MICPATH";
//    public final static String COL_5_DESCRIPTION_H ="REMARKS";
//    public final static String COL_6_WAGES_H ="WAGES";
//    public final static String COL_7_DEPOSIT_H ="DEPOSIT";
//    public final static String COL_8_P1_H ="P1";
//    public final static String COL_9_P2_H ="P2";
//    public final static String COL_10_P3_H ="P3";
//    public final static String COL_11_P4_H ="P4";
//    public final static String COL_12_ISDEPOSITED_H ="ISDEPOSITED";

    public Database(Context context){
        super(context,DATABASE_NAME,null,Database_Version);//The reason of passing null is you want the standard SQLiteCursor behaviour. If you want to implement a specialized Cursor you can get it by by extending the Cursor class( this is for doing additional operations on the query results). And in these cases, you can use the CursorFactory class to return an instance of your Cursor implementation. Here is the document for that https://stackoverflow.com/questions/11643294/what-is-the-use-of-sqlitedatabase-cursorfactory-in-android
        this.context=context;
        System.out.println("constructor db*****************************");
    }

    public static synchronized Database getInstance(Context context) { //Create a public static method to get only one instance
        if (instance == null) {
            instance = new Database(context.getApplicationContext());
        }
        return instance;
    }
    public static void closeDatabase() {//In the closeDatabase() method, you can use either getWritableDatabase() or getReadableDatabase() to close the database connection. Both methods essentially return the same underlying database connection, and closing the connection obtained from either method will close the database properly.
        if (instance != null) {
            SQLiteDatabase db = instance.getWritableDatabase(); // or getReadableDatabase()
            if (db != null && db.isOpen()) {
                db.close();
            }
            instance = null; // Set the instance to null to indicate it's closed
        }
    }
    //If we explicitly insert default NULL into the column then in database blank will be shown instead of NULL
    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {//it will execute only once        //NOT NULL OR DEFAULT NOT WORKING AND VARCHAR GIVEN VALUE NOT WORKING HOLDING MORE THAN GIVEN VALUE
     try {//if some error occur it will handle
         sqLiteDatabase.execSQL("CREATE TABLE " + TABLE_NAME1 + " ("+COL_1_ID+" INTEGER PRIMARY KEY AUTOINCREMENT , "+COL_2_NAME+" VARCHAR(100) DEFAULT NULL,"+COL_3_BANKAC+" VARCHAR(20) DEFAULT NULL,"+COL_4_IFSCCODE+" VARCHAR(11) DEFAULT NULL,"+COL_5_BANKNAME+" VARCHAR(38) DEFAULT NULL,"+COL_6_AADHAAR_NUMBER+" VARCHAR(12) DEFAULT NULL,"+COL_7_ACTIVE_PHONE1+" VARCHAR(10) DEFAULT NULL, "+ COL_8_MAINSKILL1 +" CHAR(1) DEFAULT NULL,"+COL_9_ACCOUNT_HOLDER_NAME+" VARCHAR(100) DEFAULT NULL, "+COL_11_ACTIVE_PHONE2+" VARCHAR(100) DEFAULT NULL,"+COL_12_ACTIVE+" CHAR(1) DEFAULT 1,"+COL_13_ADVANCE+" NUMERIC DEFAULT NULL,"+COL_14_BALANCE+" NUMERIC DEFAULT NULL,"+COL_15_LATESTDATE+" TEXT DEFAULT NULL,TIME TEXT DEFAULT '0' , "+COL_17_LOCATION+" VARCHAR(30) DEFAULT NULL, "+COL_18_RELIGION+" VARCHAR(20) DEFAULT NULL, "+COL_10_IMAGE+" BLOB DEFAULT NULL);");
         sqLiteDatabase.execSQL("CREATE TABLE " + TABLE0_ACTIVE_MESTRE + " ("+ COL_1_ID_AM +" INTEGER ,"+COL_13_SYSTEM_DATETIME_AM+" TEXT NOT NULL,"+COL_2_DATE_AM +" TEXT DEFAULT NULL,"+ COL_3_TIME_AM +" TEXT DEFAULT NULL,"+ COL_4_MICPATH_AM +" TEXT DEFAULT NULL,"+ COL_5_REMARKS_AM +" TEXT DEFAULT NULL,"+ COL_6_WAGES_AM +" NUMERIC DEFAULT NULL,"+ COL_7_DEPOSIT_AM +" NUMERIC DEFAULT NULL,"+ COL_8_P1_AM +" INTEGER DEFAULT NULL,"+ COL_9_P2_AM +" INTEGER DEFAULT NULL,"+ COL_10_P3_AM +" INTEGER DEFAULT NULL,"+ COL_11_P4_AM +" INTEGER DEFAULT NULL,"+ COL_12_ISDEPOSITED_AM +" CHAR(1) DEFAULT NULL);");
         sqLiteDatabase.execSQL("CREATE TABLE " + TABLE1_ACTIVE_LG + " ("+ COL_1_ID_ALG +" INTEGER ,"+COL_13_SYSTEM_DATETIME_ALG+" TEXT NOT NULL,"+COL_2_DATE_ALG +" TEXT DEFAULT NULL,"+ COL_3_TIME_ALG +" TEXT DEFAULT NULL,"+ COL_4_MICPATH_ALG +" TEXT DEFAULT NULL,"+ COL_5_REMARKS_ALG +" TEXT DEFAULT NULL,"+ COL_6_WAGES_ALG +" NUMERIC DEFAULT NULL,"+ COL_7_DEPOSIT_ALG +" NUMERIC DEFAULT NULL,"+ COL_8_P1_ALG +" INTEGER DEFAULT NULL,"+ COL_9_P2_ALG +" INTEGER DEFAULT NULL,"+ COL_10_P3_ALG +" INTEGER DEFAULT NULL,"+ COL_11_P4_ALG +" INTEGER DEFAULT NULL,"+ COL_12_ISDEPOSITED_ALG +" CHAR(1) DEFAULT NULL);");
         sqLiteDatabase.execSQL("CREATE TABLE " + TABLE2_IN_ACTIVE_MESTRE + " ("+ COL_1_ID_IAM +" INTEGER ,"+COL_13_SYSTEM_DATETIME_IAM+" TEXT NOT NULL,"+ COL_2_DATE_IAM +" TEXT DEFAULT NULL,"+COL_3_TIME_IAM +" TEXT DEFAULT NULL,"+ COL_4_MICPATH_IAM +" TEXT DEFAULT NULL,"+ COL_5_REMARKS_IAM +" TEXT DEFAULT NULL,"+ COL_6_WAGES_IAM +" NUMERIC DEFAULT NULL,"+ COL_7_DEPOSIT_IAM +" NUMERIC DEFAULT NULL,"+ COL_8_P1_IAM +" INTEGER DEFAULT NULL,"+ COL_9_P2_IAM +" INTEGER DEFAULT NULL,"+ COL_10_P3_IAM +" INTEGER DEFAULT NULL,"+ COL_11_P4_IAM +" INTEGER DEFAULT NULL,"+ COL_12_ISDEPOSITED_IAM +" CHAR(1) DEFAULT NULL);");
         sqLiteDatabase.execSQL("CREATE TABLE " + TABLE3_IN_ACTIVE_LG + " ("+ COL_1_ID_IALG +" INTEGER ,"+COL_13_SYSTEM_DATETIME_IALG+" TEXT NOT NULL,"+ COL_2_DATE_IALG +" TEXT DEFAULT NULL,"+COL_3_TIME_IALG +" TEXT DEFAULT NULL,"+ COL_4_MICPATH_IALG +" TEXT DEFAULT NULL,"+ COL_5_REMARKS_IALG +" TEXT DEFAULT NULL,"+ COL_6_WAGES_IALG +" NUMERIC DEFAULT NULL,"+ COL_7_DEPOSIT_IALG +" NUMERIC DEFAULT NULL,"+ COL_8_P1_IALG +" INTEGER DEFAULT NULL,"+ COL_9_P2_IALG +" INTEGER DEFAULT NULL,"+ COL_10_P3_IALG +" INTEGER DEFAULT NULL,"+ COL_11_P4_IALG +" INTEGER DEFAULT NULL,"+ COL_12_ISDEPOSITED_IALG +" CHAR(1) DEFAULT NULL);");
         //sqLiteDatabase.execSQL("CREATE TABLE " + TABLE_HISTORY + " ("+ COL_1_ID_H +" INTEGER ,"+ COL_2_DATE_H +" TEXT DEFAULT NULL,"+ COL_3_TIME_H +" TEXT DEFAULT NULL,"+ COL_4_MICPATH_H +" TEXT DEFAULT NULL,"+ COL_5_DESCRIPTION_H +" TEXT DEFAULT NULL,"+ COL_6_WAGES_H +" NUMERIC DEFAULT NULL,"+ COL_7_DEPOSIT_H +" NUMERIC DEFAULT NULL,"+ COL_8_P1_H +" INTEGER DEFAULT NULL,"+ COL_9_P2_H +" INTEGER DEFAULT NULL,"+ COL_10_P3_H +" INTEGER DEFAULT NULL,"+ COL_11_P4_IALG +" INTEGER DEFAULT NULL,"+ COL_12_ISDEPOSITED_IALG +" CHAR(1) DEFAULT NULL);");


         sqLiteDatabase.execSQL("CREATE TABLE " + TABLE_NAME3 + " ("+COL_31_ID+" INTEGER PRIMARY KEY NOT NULL ,"+COL_32_R1+" INTEGER DEFAULT NULL,"+COL_33_R2+" INTEGER DEFAULT NULL,"+COL_34_R3+" INTEGER DEFAULT NULL,"+COL_35_R4+" INTEGER DEFAULT NULL,"+ COL_36_SKILL2 +" CHAR(1) DEFAULT NULL,"+ COL_37_SKILL3 +" CHAR(1) DEFAULT NULL,"+ COL_38_SKILL4 +" CHAR(1) DEFAULT NULL,"+COL_39_INDICATOR+" CHAR(1) DEFAULT NULL,"+ COL_391_STAR +" CHAR(1) DEFAULT NULL,"+COL_392_LEAVINGDATE+" VARCHAR(10) DEFAULT NULL,"+COL_393_REFFERAL_REMARKS+" TEXT DEFAULT NULL,"+COL_394_INVOICE1+" BLOB DEFAULT NULL,"+COL_395_INVOICE2+" BLOB DEFAULT NULL,"+COL_396_PDFSEQUENCE+" INTEGER DEFAULT 0 , "+COL_397_TOTAL_WORKED_DAYS+" INTEGER DEFAULT 0 , "+COL_398_RETURNINGDATE+" TEXT DEFAULT NULL);");//id is primary key because according to id only data is stored in table 3 so no duplicate
         sqLiteDatabase.execSQL("CREATE TABLE " + TABLE_NAME4 + " ("+COL_41_LOCATION+" TEXT DEFAULT NULL);");
         sqLiteDatabase.execSQL("CREATE TABLE " + TABLE_NAME5 + " ("+COL_51_RELIGION+" TEXT DEFAULT NULL);");
     }catch(Exception e){
         e.printStackTrace();
     }
        //onUpgrade(sqLiteDatabase,0,Database_Version);//IT is needed when we update database
    }
    @Override    //i is old version and i1 is new version.When we change version then this method is called
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int oldVersion, int newVersion) {
        System.out.println("on upgrade*******************************************");
       // if(oldVersion < 5){//if version is less then 5 then will execute
            //System.out.println("old****************5"+oldVersion);
         //   try {//if some error occur it will handle
//                sqLiteDatabase.execSQL("CREATE TABLE " + TABLE_NAME1 + " ("+COL_1_ID+" INTEGER PRIMARY KEY AUTOINCREMENT , "+COL_2_NAME+" VARCHAR(100) DEFAULT NULL,"+COL_3_BANKAC+" VARCHAR(20) DEFAULT NULL,"+COL_4_IFSCCODE+" VARCHAR(11) DEFAULT NULL,"+COL_5_BANKNAME+" VARCHAR(38) DEFAULT NULL,"+COL_6_AADHAAR_NUMBER+" VARCHAR(12) DEFAULT NULL,"+COL_7_ACTIVE_PHONE1+" VARCHAR(10) DEFAULT NULL, "+COL_8_SKILL+" CHAR(1) DEFAULT NULL,"+COL_9_ACCOUNT_HOLDER_NAME+" VARCHAR(100) DEFAULT NULL,"+COL_10_IMAGE+" BLOB DEFAULT NULL,"+COL_11_ACTIVE_PHONE2+" VARCHAR(100) DEFAULT NULL,"+COL_12_ACTIVE+" CHAR(1) DEFAULT 1,"+COL_13_ADVANCE+" NUMERIC DEFAULT NULL,"+COL_14_BALANCE+" NUMERIC DEFAULT NULL,"+COL_15_LATESTDATE+" TEXT DEFAULT NULL,TIME TEXT DEFAULT '0' , "+COL_17_LOCATION+" VARCHAR(30) DEFAULT NULL, "+COL_18_RELIGION+" VARCHAR(20) DEFAULT NULL);");
//                sqLiteDatabase.execSQL("CREATE TABLE " + TABLE_NAME2 + " ("+COL_21_ID+" INTEGER ,"+COL_22_DATE+" TEXT DEFAULT NULL,"+COL_23_TIME+" TEXT DEFAULT NULL,"+COL_24_MICPATH+" TEXT DEFAULT NULL,"+COL_25_DESCRIPTION+" TEXT DEFAULT NULL,"+COL_26_WAGES+" NUMERIC DEFAULT NULL,"+COL_27_DEPOSIT+" NUMERIC DEFAULT NULL,"+COL_28_P1+" INTEGER DEFAULT NULL,"+COL_29_P2+" INTEGER DEFAULT NULL,"+COL_291_P3+" INTEGER DEFAULT NULL,"+COL_292_P4+" INTEGER DEFAULT NULL,"+COL_293_ISDEPOSITED+" CHAR(1) DEFAULT NULL);");
//                sqLiteDatabase.execSQL("CREATE TABLE " + TABLE_NAME3 + " ("+COL_31_ID+" INTEGER PRIMARY KEY NOT NULL ,"+COL_32_R1+" INTEGER DEFAULT NULL,"+COL_33_R2+" INTEGER DEFAULT NULL,"+COL_34_R3+" INTEGER DEFAULT NULL,"+COL_35_R4+" INTEGER DEFAULT NULL,"+COL_36_SKILL1+" CHAR(1) DEFAULT NULL,"+COL_37_SKILL2+" CHAR(1) DEFAULT NULL,"+COL_38_SKILL3+" CHAR(1) DEFAULT NULL,"+COL_39_INDICATOR+" CHAR(1) DEFAULT NULL,"+COL_391_RATING+" CHAR(1) DEFAULT NULL,"+COL_392_LEAVINGDATE+" VARCHAR(10) DEFAULT NULL,"+COL_393_REFFERAL_REMARKS+" TEXT DEFAULT NULL,"+COL_394_INVOICE1+" BLOB DEFAULT NULL,"+COL_395_INVOICE2+" BLOB DEFAULT NULL,"+COL_396_PDFSEQUENCE+" INTEGER DEFAULT 0 , "+COL_397_TOTAL_WORKED_DAYS+" INTEGER DEFAULT 0);");//id is primary key because according to id only data is stored in table 3 so no duplicate
//            }catch(Exception e){
//                e.printStackTrace();
//            }
//        }
//      sqLiteDatabase.execSQL("DROP TABLE IF EXISTS "+ TABLE_NAME1);
//      sqLiteDatabase.execSQL("DROP TABLE IF EXISTS "+ TABLE_NAME2);
//      sqLiteDatabase.execSQL("DROP TABLE IF EXISTS "+ TABLE_NAME3);
      //  else if(oldVersion < 6) { //if version is less then 6 then will execute
//            System.out.println("old****************6"+oldVersion);
//            sqLiteDatabase.execSQL("ALTER TABLE " + TABLE_NAME1 + " ADD COLUMN TIME TEXT DEFAULT '0'");//make this column integerADDED NEW COLUMN TO TABLE3 AND VERSION IS 4
//            sqLiteDatabase.execSQL("ALTER TABLE " + TABLE_NAME3 + " ADD COLUMN PDFSEQUENCE INTEGER DEFAULT 0");
        //}
     // Log.d("DATABASE","ON UPGRADE DROP 3 TABLES");
      //onCreate(sqLiteDatabase);
    }
    public boolean insertDataTable1(String name, String bankAccount, String ifscCode, String bankName, String aadhaarCard, String phoneNumber, String skill, String fatherName, byte[] image, String acHolder,String location,String religion) {
        boolean success=false;
        SQLiteDatabase dB=null;
        try {
            dB = this.getWritableDatabase();//getting permission
            dB.beginTransaction();//transaction start
            ContentValues cv = new ContentValues();//to enter data at once it is like hash map
            cv.put(COL_2_NAME, name);
            cv.put(COL_3_BANKAC, bankAccount);
            cv.put(COL_4_IFSCCODE, ifscCode);
            cv.put(COL_5_BANKNAME, bankName);
            cv.put(COL_6_AADHAAR_NUMBER, aadhaarCard);
            cv.put(COL_7_ACTIVE_PHONE1, phoneNumber);
            cv.put(COL_8_MAINSKILL1, skill);
            cv.put(COL_9_ACCOUNT_HOLDER_NAME, fatherName);
            cv.put(COL_10_IMAGE, image);
            cv.put(COL_11_ACTIVE_PHONE2, acHolder);
            cv.put(COL_17_LOCATION, location);
            cv.put(COL_18_RELIGION, religion);
            cv.put(COL_12_ACTIVE,"1");//when new user added then it will be active
            success=(dB.insert(TABLE_NAME1, null, cv) == -1)? false: true;// -1 is returned if error occurred
        }catch (Exception e){
            e.printStackTrace();
            return false;
        }finally{
            if (dB != null){
                if(success){//if success then only commit
                    dB.setTransactionSuccessful();//If you want to commit the transaction there is a method setTransactionSuccessful() which will commit the values in the database.If you want to rollback your transaction then you need to endTransaction() without committing the transaction by setTransactionSuccessful().
                }
                dB.endTransaction(); //Commit or rollback the transaction.it will rollback when some error occur
                dB.close(); // db.close();//closing db after operation performed
            }
        }
        return success;
    }
    public  Cursor getId(String name, String bankAccount, String ifscCode, String bankName, String aadhaarCard, String phoneNumber, String type, String fatherName,String acHolder,String location,String religion){
            db = this.getReadableDatabase();//error when closing db or cursor
            String query = "SELECT "+Database.COL_1_ID+" FROM " + TABLE_NAME1 + " WHERE "+Database.COL_2_NAME+"='" + name + "' AND "+Database.COL_9_ACCOUNT_HOLDER_NAME+"='" + fatherName + "' AND "+Database.COL_3_BANKAC+"='" + bankAccount + "' AND "+Database.COL_7_ACTIVE_PHONE1+"='" + phoneNumber + "' AND "+Database.COL_4_IFSCCODE+"='" + ifscCode + "' AND "+Database.COL_6_AADHAAR_NUMBER+"='" + aadhaarCard + "' AND "+Database.COL_8_MAINSKILL1 +"='" + type + "' AND "+Database.COL_5_BANKNAME+"='" + bankName + "' AND "+Database.COL_11_ACTIVE_PHONE2+"='" + acHolder + "' AND "+Database.COL_17_LOCATION+"='"+location+"' AND "+Database.COL_18_RELIGION+"='"+religion+"'";
            return db.rawQuery(query, null);
     }
    public Cursor getData(String query){//error when closing db or cursor so don't close cursor
            db = this.getReadableDatabase();
             return db.rawQuery(query, null);
    }
    public boolean updateDataTable1(String name, String bankAccount, String ifscCode, String bankName, String aadhaarCard, String phoneNumber, String skill, String fatherName, byte[] image, String acHolder, String Id,String location,String religion ) {
        boolean success=false;
        SQLiteDatabase dB=null;
        try {
            dB = this.getWritableDatabase();//getting permission
            dB.beginTransaction();//transaction start
            ContentValues cv = new ContentValues();//to enter data at once it is like hash map
            cv.put(COL_2_NAME, name);
            cv.put(COL_3_BANKAC, bankAccount);
            cv.put(COL_4_IFSCCODE, ifscCode);
            cv.put(COL_5_BANKNAME, bankName);
            cv.put(COL_6_AADHAAR_NUMBER, aadhaarCard);
            cv.put(COL_7_ACTIVE_PHONE1, phoneNumber);
            cv.put(COL_8_MAINSKILL1, skill);
            cv.put(COL_9_ACCOUNT_HOLDER_NAME, fatherName);
            cv.put(COL_10_IMAGE, image);
            cv.put(COL_11_ACTIVE_PHONE2, acHolder);
            cv.put(COL_17_LOCATION, location);
            cv.put(COL_18_RELIGION, religion);
            success=(dB.update(TABLE_NAME1, cv, Database.COL_1_ID+"=?", new String[]{Id})!=1)? false :true;//if update return 1 then data is updated else not updated.//0 is returned if no record updated and it return number of rows updated
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }finally{
            if (dB != null){
                if(success){//if success then only commit
                    dB.setTransactionSuccessful();//If you want to commit the transaction there is a method setTransactionSuccessful() which will commit the values in the database.If you want to rollback your transaction then you need to endTransaction() without committing the transaction by setTransactionSuccessful().
                }
                dB.endTransaction(); //Commit or rollback the transaction.it will rollback when some error occur
                dB.close(); // db.close();//closing db after operation performed
            }
        }
        return success;
    }
    public boolean insertDataTable3(String id,int r1,int r2,int r3,int r4,String skill1,String skill2,String skill3,String indicator ) {
        boolean success=false;
        SQLiteDatabase dB=null;
        try {
            dB = this.getWritableDatabase();//getting permission
            dB.beginTransaction();//transaction start
            ContentValues cv = new ContentValues();//to enter data at once it is like hash map
            cv.put(COL_31_ID, id);
            cv.put(COL_32_R1, r1);
            cv.put(COL_33_R2, r2);
            cv.put(COL_34_R3, r3);
            cv.put(COL_35_R4, r4);
            cv.put(COL_36_SKILL2, skill1);
            cv.put(COL_37_SKILL3, skill2);
            cv.put(COL_38_SKILL4, skill3);
            cv.put(COL_39_INDICATOR, indicator);
            success=(dB.insert(TABLE_NAME3, null, cv) == -1)? false :true;//-1 data not inserted. -1 is returned if error occurred.
        }catch (Exception e){
            e.printStackTrace();
            return false;
        }finally{
            if (dB != null){
                if(success){//if success then only commit
                    dB.setTransactionSuccessful();//If you want to commit the transaction there is a method setTransactionSuccessful() which will commit the values in the database.If you want to rollback your transaction then you need to endTransaction() without committing the transaction by setTransactionSuccessful().
                }
                dB.endTransaction(); //Commit or rollback the transaction.it will rollback when some error occur
                dB.close(); // db.close();//closing db after operation performed
            }
        }
        return success;
    }
    public boolean updatePersonSkillAndShiftData(String changedSkill,String id) {
        try{
            String[] previousActiveOrInactiveAndSkill =getActiveOrInactiveAndSkill(id);
            /**if previous  and changedSkill
             *      M  =  M=return true =previousActiveOrInactiveAndSkill[1].equals(changedSkill)
             *      L  =  L=return true =previousActiveOrInactiveAndSkill[1].equals(changedSkill)
             *      G  =  G=return true =previousActiveOrInactiveAndSkill[1].equals(changedSkill)
             *      G  =  L=return true =(previousActiveOrInactiveAndSkill[1].equals(context.getResources().getString(R.string.women_laber)) && changedSkill.equals(context.getResources().getString(R.string.laber))))
             *      L  =  G=return true =(previousActiveOrInactiveAndSkill[1].equals(context.getResources().getString(R.string.laber)) && changedSkill.equals(context.getResources().getString(R.string.women_laber)))
             *      G or L = M changed to M
             *      M  =  G or L changed to G or L*/
            if(previousActiveOrInactiveAndSkill[1].equals(changedSkill) ||
              (previousActiveOrInactiveAndSkill[1].equals(context.getResources().getString(R.string.laber)) && changedSkill.equals(context.getResources().getString(R.string.women_laber))) ||
              (previousActiveOrInactiveAndSkill[1].equals(context.getResources().getString(R.string.women_laber)) && changedSkill.equals(context.getResources().getString(R.string.laber)))) return true;

            if(previousActiveOrInactiveAndSkill[0].equals("1")){//active
//                if(previousActiveOrInactiveAndSkill[1].equals(context.getResources().getString(R.string.laber)) || previousActiveOrInactiveAndSkill[1].equals(context.getResources().getString(R.string.women_laber))){//if previous is G or L then definitely change to mestre because previous all condition is checked
//                    return shiftDataFromActiveTableToUserChangedSkillActiveTable(id,changedSkill);
//                }
//                if(previousActiveOrInactiveAndSkill[1].equals(context.getResources().getString(R.string.mestre))){//if previous is mestre then definitely change to G or L  because previous all condition is checked
//                    return shiftDataFromActiveTableToUserChangedSkillActiveTable(id,changedSkill);
//                }
                return shiftDataFromActiveTableToUserChangedSkillActiveTable(id,changedSkill);//if previous is G or L then definitely change to mestre because previous all condition is checked AND if previous is mestre then definitely change to G or L  because previous all condition is checked
            }else if (previousActiveOrInactiveAndSkill[0].equals("0")) {//inactive
//                if(previousActiveOrInactiveAndSkill[1].equals(context.getResources().getString(R.string.laber)) || previousActiveOrInactiveAndSkill[1].equals(context.getResources().getString(R.string.women_laber))){//if previous is G or L then definitely change to mestre because previous all condition is checked
//                   // return shiftDataFromInActiveTableLOrGToMestre(id);
//                }
//                if(previousActiveOrInactiveAndSkill[1].equals(context.getResources().getString(R.string.mestre))){//if previous is mestre then definitely change to G or L  because previous all condition is checked
//                    //return shiftDataFromInActiveTableMestreToLOrG(id);
//                }
                return shiftDataFromInActiveTableToUserChangedSkillInActiveTable(id,changedSkill);//if previous is G or L then definitely change to mestre because previous all condition is checked AND if previous is mestre then definitely change to G or L  because previous all condition is checked
            }
        }catch (Exception x){
            x.printStackTrace();
            return false;
        }
        return false;//if condition not meet then return false
    }
    private boolean shiftDataFromActiveTableToUserChangedSkillActiveTable(String id, String updatedSkill){
        boolean success=false;//first get data from table then update skill to insert to particular table
        Database  database=Database.getInstance(context);
        String[] previousActiveOrInactiveAndSkill=null;
        try {
            Cursor dataFromActiveTableCursor = getWagesDepositDataForRecyclerView(id);//getting data from previous table

            previousActiveOrInactiveAndSkill=getActiveOrInactiveAndSkill(id);//this statement should be here to get previous data like skill

            if(database.updateTable("UPDATE " + Database.TABLE_NAME1+ " SET " + Database.COL_8_MAINSKILL1 + "='" + updatedSkill + "' WHERE ID='" + id + "'")){//if it is updated then only perform shiftData operation
                success=shiftDataToActiveTable(dataFromActiveTableCursor,id,getTableName(getTableNumber(previousActiveOrInactiveAndSkill)),false, getMainSkill(id));//getSkill(id) gives updated skill
            }

        }catch (Exception e){
            e.printStackTrace();
            return false;
        }finally{
            //in sqlite database we cant use nested transaction so updating manually using database object because in this method shiftDataToActiveTable() already transaction is implemented
            if(!success){//if success is false the revert changes
                database.updateTable("UPDATE " + Database.TABLE_NAME1+ " SET " + Database.COL_8_MAINSKILL1 + "='" + previousActiveOrInactiveAndSkill[1] +"' WHERE ID='" + id + "'");
            }
            database.close();
        }
        return success;
    }
    private boolean shiftDataFromInActiveTableToUserChangedSkillInActiveTable(String id, String updatedSkill){
        boolean success=false;//first get data from table then update skill to insert to particular table
        Database  database=Database.getInstance(context);
        String[] previousActiveOrInactiveAndSkill=null;
        try {
            Cursor dataFromActiveTableCursor = getWagesDepositDataForRecyclerView(id);//getting data from previous table

            previousActiveOrInactiveAndSkill=getActiveOrInactiveAndSkill(id);//this statement should be here to get previous data like skill

            if(database.updateTable("UPDATE " + Database.TABLE_NAME1+ " SET " + Database.COL_8_MAINSKILL1 + "='" + updatedSkill + "' WHERE ID='" + id + "'")){//if it is updated then only perform shiftData operation
                success=shiftDataToInActiveTable(dataFromActiveTableCursor,id,getTableName(getTableNumber(previousActiveOrInactiveAndSkill)),false, getMainSkill(id));//getSkill(id) gives updated skill
            }
        }catch (Exception e){
            e.printStackTrace();
            return false;
        }finally{
            //in sqlite database we cant use nested transaction so updating manually because in this method shiftDataToActiveTable() already transaction is implemented
            if(!success){//if success is false the revert changes
                database.updateTable("UPDATE " + Database.TABLE_NAME1+ " SET " + Database.COL_8_MAINSKILL1 + "='" + previousActiveOrInactiveAndSkill[1] +"' WHERE ID='" + id + "'");
            }
            database.close();
        }
        return success;
    }
    private boolean insertWagesOrDepositToActiveTableDirectly(SQLiteDatabase db,String systemDateTime,String skill, String id, String date, String time, String micPath, String remarks, int wages, int p1, int p2, int p3, int p4, int deposit, String isDeposited) {
                ContentValues cv = new ContentValues();//to enter data at once it is like hash map
                if(skill.equals(context.getResources().getString(R.string.laber)) || skill.equals(context.getResources().getString(R.string.women_laber))){//check for M OR lG
                    if (id != null) {
                        cv.put(COL_1_ID_ALG, id);
                    }
                    if (date != null) {
                        cv.put(COL_2_DATE_ALG, date);
                    }
                    if (time != null) {
                        cv.put(COL_3_TIME_ALG, time);
                    }
                    if (micPath != null) {
                        cv.put(COL_4_MICPATH_ALG, micPath);
                    }
                    if (remarks != null) {
                        cv.put(COL_5_REMARKS_ALG, remarks);
                    }
                    if (wages != 0) {
                        cv.put(COL_6_WAGES_ALG, wages);
                    }
                    if (deposit != 0) {
                        cv.put(COL_7_DEPOSIT_ALG, deposit);
                    }
                    if (p1 != 0) {
                        cv.put(COL_8_P1_ALG, p1);
                    }
                    if (p2 != 0) {
                        cv.put(COL_9_P2_ALG, p2);
                    }
                    if (p3 != 0) {
                        cv.put(COL_10_P3_ALG, p3);
                    }
                    if (p4 != 0) {
                        cv.put(COL_11_P4_ALG, p4);
                    }
                    if(systemDateTime!=null){
                        cv.put(COL_13_SYSTEM_DATETIME_ALG, systemDateTime);
                    }else return false;

                    if (isDeposited != null && (isDeposited.equals("0") || isDeposited.equals("1"))) {//its very important to have value user have to pass either 0 for not deposit or 1 for deposit because when editing this value is used to decide TO edit.But here validation not required because this method will receive data directly from table
                        cv.put(COL_12_ISDEPOSITED_ALG, isDeposited);
                    }else return false;

                    return (db.insert(TABLE1_ACTIVE_LG, null,cv) == -1)? false :true;//(rowId == -1) means data not inserted

                }else if(skill.equals(context.getResources().getString(R.string.mestre))){//check for mestre
                    if (id != null) {
                        cv.put(COL_1_ID_AM, id);
                    }
                    if (date != null) {
                        cv.put(COL_2_DATE_AM, date);
                    }
                    if (time != null) {
                        cv.put(COL_3_TIME_AM, time);
                    }
                    if (micPath != null) {
                        cv.put(COL_4_MICPATH_AM, micPath);
                    }
                    if (remarks != null) {
                        cv.put(COL_5_REMARKS_AM, remarks);
                    }
                    if (wages != 0) {
                        cv.put(COL_6_WAGES_AM, wages);
                    }
                    if (deposit != 0) {
                        cv.put(COL_7_DEPOSIT_AM, deposit);
                    }
                    if (p1 != 0) {
                        cv.put(COL_8_P1_AM, p1);
                    }
                    if (p2 != 0) {
                        cv.put(COL_9_P2_AM, p2);
                    }
                    if (p3 != 0) {
                        cv.put(COL_10_P3_AM, p3);
                    }
                    if (p4 != 0) {
                        cv.put(COL_11_P4_AM, p4);
                    }
                    if(systemDateTime!=null){
                        cv.put(COL_13_SYSTEM_DATETIME_AM, systemDateTime);
                    }else return false;

                    if (isDeposited != null && (isDeposited.equals("0") || isDeposited.equals("1"))) {//its very important to have value user have to pass either 0 for not deposit or 1 for deposit because when editing this value is used to decide TO edit.But here validation not required because this method will receive data directly from table
                        cv.put(COL_12_ISDEPOSITED_AM, isDeposited);
                    }else return false;
                    return (db.insert(TABLE0_ACTIVE_MESTRE, null,cv) == -1)? false :true;//(rowId == -1) means data not inserted
                }
          return false;//any error return false
    }
    private boolean insertWagesOrDepositToInActiveTableDirectly(SQLiteDatabase db,String systemDateTime, String skill, String id, String date, String time, String micPath, String remarks, int wages, int p1, int p2, int p3, int p4, int deposit, String isDeposited) {
        ContentValues cv = new ContentValues();//to enter data at once it is like hash map
        if(skill.equals(context.getResources().getString(R.string.laber)) || skill.equals(context.getResources().getString(R.string.women_laber))){//check for M OR lG
            if (id != null) {
                cv.put(COL_1_ID_ALG, id);
            }
            if (date != null) {
                cv.put(COL_2_DATE_ALG, date);
            }
            if (time != null) {
                cv.put(COL_3_TIME_ALG, time);
            }
            if (micPath != null) {
                cv.put(COL_4_MICPATH_ALG, micPath);
            }
            if (remarks != null) {
                cv.put(COL_5_REMARKS_ALG, remarks);
            }
            if (wages != 0) {
                cv.put(COL_6_WAGES_ALG, wages);
            }
            if (deposit != 0) {
                cv.put(COL_7_DEPOSIT_ALG, deposit);
            }
            if (p1 != 0) {
                cv.put(COL_8_P1_ALG, p1);
            }
            if (p2 != 0) {
                cv.put(COL_9_P2_ALG, p2);
            }
            if (p3 != 0) {
                cv.put(COL_10_P3_ALG, p3);
            }
            if (p4 != 0) {
                cv.put(COL_11_P4_ALG, p4);
            }

            if(systemDateTime!=null){
                cv.put(COL_13_SYSTEM_DATETIME_ALG, systemDateTime);
            }else return false;

            if (isDeposited != null && (isDeposited.equals("0") || isDeposited.equals("1"))) {//its very important to have value user have to pass either 0 for not deposit or 1 for deposit because when editing this value is used to decide TO edit.But here validation not required because this method will receive data directly from table
                cv.put(COL_12_ISDEPOSITED_ALG, isDeposited);
            }else return false;

            return (db.insert(TABLE3_IN_ACTIVE_LG, null,cv) == -1)? false : true;//(rowId == -1) means data not inserted
        }else if(skill.equals(context.getResources().getString(R.string.mestre))){//check for mestre
            if (id != null) {
                cv.put(COL_1_ID_AM, id);
            }
            if (date != null) {
                cv.put(COL_2_DATE_AM, date);
            }
            if (time != null) {
                cv.put(COL_3_TIME_AM, time);
            }
            if (micPath != null) {
                cv.put(COL_4_MICPATH_AM, micPath);
            }
            if (remarks != null) {
                cv.put(COL_5_REMARKS_AM, remarks);
            }
            if (wages != 0) {
                cv.put(COL_6_WAGES_AM, wages);
            }
            if (deposit != 0) {
                cv.put(COL_7_DEPOSIT_AM, deposit);
            }
            if (p1 != 0) {
                cv.put(COL_8_P1_AM, p1);
            }
            if (p2 != 0) {
                cv.put(COL_9_P2_AM, p2);
            }
            if (p3 != 0) {
                cv.put(COL_10_P3_AM, p3);
            }
            if (p4 != 0) {
                cv.put(COL_11_P4_AM, p4);
            }

            if(systemDateTime!=null){
                cv.put(COL_13_SYSTEM_DATETIME_AM, systemDateTime);
            }else return false;

            if (isDeposited != null && (isDeposited.equals("0") || isDeposited.equals("1"))) {//its very important to have value user have to pass either 0 for not deposit or 1 for deposit because when editing this value is used to decide TO edit.But here validation not required because this method will receive data directly from table
                cv.put(COL_12_ISDEPOSITED_AM, isDeposited);
            }else return false;
            return (db.insert(TABLE2_IN_ACTIVE_MESTRE, null,cv) == -1)? false :true;//(rowId == -1) means data not inserted
        }
        return false;
    }
    public boolean insertWagesOrDepositOnlyToActiveTableTransaction(String id,String systemCurrentDate24hrTime, String date, String time, String micPath, String remarks, int wages, int p1, int p2, int p3, int p4, int deposit, String isDeposited) {
      /**Before inserting first 1. make id active because only(INSERTION,UPDATION,AND PRESSING ACTIVE RADIO BUTTON) would make id active so since it is insertion method so make it active
       * 2. check for duplicate data then
       * 3.insert into active table*/

        if(!activateIdWithLatestDate(id,time)){
            Toast.makeText(context, context.getResources().getString(R.string.failed_to_make_id_active), Toast.LENGTH_LONG).show();
            return false;
        }
        if(isRedundantDataPresentInOtherTable(id)){//if redundant data is present then it will delete duplicate data from other table and that data will be inserted in current table as remarks so that user would know about redundant data.but this thing would not happen just for tight checking
            Toast.makeText(context,context.getResources().getString(R.string.check_last_remarks), Toast.LENGTH_LONG).show();
            return false;
        }

        boolean success=false;
        SQLiteDatabase dB=null;
        try{
            String activeInactiveSkill[]=getActiveOrInactiveAndSkill(id);
            dB = this.getWritableDatabase();//getting permission
            dB.beginTransaction();//transaction start
          ContentValues cv = new ContentValues();//to enter data at once it is like hash map

        if(activeInactiveSkill[0].equals("1")){//if person is active at 0th position and 1st position is person skill

           if(activeInactiveSkill[1].equals(context.getResources().getString(R.string.laber)) || activeInactiveSkill[1].equals(context.getResources().getString(R.string.women_laber))){//check for M OR lG
                    if (id != null) {
                        cv.put(COL_1_ID_ALG, id);
                    }
                    if (date != null) {
                        cv.put(COL_2_DATE_ALG, date);
                    }
                    if (time != null) {
                        cv.put(COL_3_TIME_ALG, time);
                    }
                    if (micPath != null) {
                        cv.put(COL_4_MICPATH_ALG, micPath);
                    }
                    if (remarks != null) {
                        cv.put(COL_5_REMARKS_ALG, remarks);
                    }
                    if (wages != 0) {
                        cv.put(COL_6_WAGES_ALG, wages);
                    }
                    if (deposit != 0) {
                        cv.put(COL_7_DEPOSIT_ALG, deposit);
                    }
                    if (p1 != 0) {
                        cv.put(COL_8_P1_ALG, p1);
                    }
                    if (p2 != 0) {
                        cv.put(COL_9_P2_ALG, p2);
                    }
                    if (p3 != 0) {
                        cv.put(COL_10_P3_ALG, p3);
                    }
                    if (p4 != 0) {
                        cv.put(COL_11_P4_ALG, p4);
                    }

                    if(systemCurrentDate24hrTime != null){
                        cv.put(COL_13_SYSTEM_DATETIME_ALG,systemCurrentDate24hrTime);
                    } else {
                        Toast.makeText(context, "SYSTEM DATE TIME  value cannot be null ", Toast.LENGTH_LONG).show();
                        return false;
                    }


                    if (isDeposited != null){//its very important to have value user have to pass either 0 for not deposit or 1 for deposit because when editing this value is used to decide edit
                        cv.put(COL_12_ISDEPOSITED_ALG, isDeposited);
                    } else {
                        Toast.makeText(context, "isDeposited value cannot be null ", Toast.LENGTH_LONG).show();
                        return false;
                    }
                     success=(dB.insert(TABLE1_ACTIVE_LG, null, cv) == -1)? false :true; //if data not inserted return -1
                //testing success=(dB.insert(TABLE2_IN_ACTIVE_MESTRE, null,cv) == -1)? false :true; //if data not inserted return -1
            }else if(activeInactiveSkill[1].equals(context.getResources().getString(R.string.mestre))){//check for mestre
                   if (id != null) {
                       cv.put(COL_1_ID_AM, id);
                   }
                   if (date != null) {
                       cv.put(COL_2_DATE_AM, date);
                   }
                   if (time != null) {
                       cv.put(COL_3_TIME_AM, time);
                   }
                   if (micPath != null) {
                       cv.put(COL_4_MICPATH_AM, micPath);
                   }
                   if (remarks != null) {
                       cv.put(COL_5_REMARKS_AM, remarks);
                   }
                   if (wages != 0) {
                       cv.put(COL_6_WAGES_AM, wages);
                   }
                   if (deposit != 0) {
                       cv.put(COL_7_DEPOSIT_AM, deposit);
                   }
                   if (p1 != 0) {
                       cv.put(COL_8_P1_AM, p1);
                   }
                   if (p2 != 0) {
                       cv.put(COL_9_P2_AM, p2);
                   }
                   if (p3 != 0) {
                       cv.put(COL_10_P3_AM, p3);
                   }
                   if (p4 != 0) {
                       cv.put(COL_11_P4_AM, p4);
                   }

               if(systemCurrentDate24hrTime !=null){
                   cv.put(COL_13_SYSTEM_DATETIME_AM,systemCurrentDate24hrTime);
               } else {
                   Toast.makeText(context, "SYSTEM DATE TIME  value cannot be null ", Toast.LENGTH_LONG).show();
                   return false;
               }

               if (isDeposited != null) {//its very important to have value user have to pass either 0 for not deposit or 1 for deposit because when editing this value is used to decide edit
                       cv.put(COL_12_ISDEPOSITED_AM, isDeposited);
                   } else {
                       Toast.makeText(context, "isDeposited value cannot be null ", Toast.LENGTH_LONG).show();
                       return false;
                   }
                  success=(dB.insert(TABLE0_ACTIVE_MESTRE, null,cv) == -1)? false :true; //if data not inserted return -1
               //testing success=(dB.insert(TABLE2_IN_ACTIVE_MESTRE, null,cv) == -1)? false :true; //if data not inserted return -1

           }
        }else {
            Toast.makeText(context, "insertion only happens in active table but its inactive", Toast.LENGTH_LONG).show();
         }
        }catch (Exception x){
        x.printStackTrace();
         return false;
        }finally{
            if (dB != null){
            if(success){//if success then only commit
                dB.setTransactionSuccessful();//If you want to commit the transaction there is a method setTransactionSuccessful() which will commit the values in the database.If you want to rollback your transaction then you need to endTransaction() without committing the transaction by setTransactionSuccessful().
            }
                dB.endTransaction(); //Commit or rollback the transaction.it will rollback when some error occur
                dB.close(); // db.close();//closing db after operation performed
        }
        }
        return success;
    }
    public boolean updateWagesOrDepositOnlyToActiveTable(String date,String newSystemTimeDate,String time, String remarks, String micPath, int wages, int deposit, int p1 , int p2, int p3, int p4, String id , String previousSystemTimeDate){
        /**Before updating first make id active because only(INSERTION,UPDATION,AND PRESSING ACTIVE RADIO BUTTON) would make id active so since it is updation method so make it active
         *  second check for duplicate data then update into active table*/

        if(!activateIdWithLatestDate(id,time)){
            Toast.makeText(context, context.getResources().getString(R.string.failed_to_make_id_active), Toast.LENGTH_LONG).show();
            return false;
        }
        if(isRedundantDataPresentInOtherTable(id)){//if redundant data is present then it will delete duplicate data from other table and that data will be inserted in current table as remarks so that user would know about redundant data.but this thing would not happen just for tight checking
            Toast.makeText(context,context.getResources().getString(R.string.check_last_remarks), Toast.LENGTH_LONG).show();
            return false;
        }

        if(newSystemTimeDate == null || newSystemTimeDate ==null) return false;
        boolean success=false;
        SQLiteDatabase dB=null;
        try{
            String activeInactiveSkill[]=getActiveOrInactiveAndSkill(id);

            dB = this.getWritableDatabase();//getting permission
            dB.beginTransaction();//transaction start
            ContentValues cv = new ContentValues();//to enter data at once it is like hash map

            if(activeInactiveSkill[0].equals("1")){//if person is active at 0th position and 1st position is person skill

                if(activeInactiveSkill[1].equals(context.getResources().getString(R.string.laber)) || activeInactiveSkill[1].equals(context.getResources().getString(R.string.women_laber))){//check for M OR lG
                    if (date != null) {
                        cv.put(COL_2_DATE_ALG, date);
                    }
                    if (time != null) {
                        cv.put(COL_3_TIME_ALG, time);
                    }
                    if (micPath != null) {
                        cv.put(COL_4_MICPATH_ALG, micPath);
                    }
                    if (remarks != null) {
                        cv.put(COL_5_REMARKS_ALG, remarks);
                    }
                    if (wages != 0) {
                        cv.put(COL_6_WAGES_ALG, wages);
                    }else{
                        cv.put(COL_6_WAGES_ALG, (String) null);
                    }
                    if (deposit != 0) {
                        cv.put(COL_7_DEPOSIT_ALG, deposit);
                    }else{
                        cv.put(COL_7_DEPOSIT_ALG, (String) null);
                    }
                    if (p1 != 0) {
                        cv.put(COL_8_P1_ALG, p1);
                    }else{
                        cv.put(COL_8_P1_ALG, (String) null);
                    }
                    if (p2 != 0) {
                        cv.put(COL_9_P2_ALG, p2);
                    }else{
                        cv.put(COL_9_P2_ALG, (String) null);
                    }
                    if (p3 != 0) {
                        cv.put(COL_10_P3_ALG, p3);
                    }else{
                        cv.put(COL_10_P3_ALG, (String) null);
                    }
                    if (p4 != 0) {
                        cv.put(COL_11_P4_ALG, p4);
                    }else{
                        cv.put(COL_11_P4_ALG, (String) null);
                    }

                    if(newSystemTimeDate != null){//compulsory data
                        cv.put(COL_13_SYSTEM_DATETIME_ALG,newSystemTimeDate);
                    }else{
                        return false;
                    }

                    success=(dB.update(TABLE1_ACTIVE_LG,cv,Database.COL_1_ID_ALG +"= '"+id+"'"+" AND "+Database.COL_13_SYSTEM_DATETIME_ALG +"= '"+previousSystemTimeDate+"'",null) !=1 )? false :true;//at a time only 1 row should be updated so if 1 row is updated then update method will return 1. and if update method return value is more than 1 then there is duplicate row so checking with value 1
                }else if(activeInactiveSkill[1].equals(context.getResources().getString(R.string.mestre))){//check for mestre
                    if (date != null) {
                        cv.put(COL_2_DATE_AM, date);
                    }
                    if (time != null) {
                        cv.put(COL_3_TIME_AM, time);
                    }
                    if (micPath != null) {
                        cv.put(COL_4_MICPATH_AM, micPath);
                    }
                    if (remarks != null) {
                        cv.put(COL_5_REMARKS_AM, remarks);
                    }
                    if (wages != 0) {
                        cv.put(COL_6_WAGES_AM, wages);
                    }else{
                        cv.put(COL_6_WAGES_AM, (String) null);
                    }
                    if (deposit != 0) {
                        cv.put(COL_7_DEPOSIT_AM, deposit);
                    }else{
                        cv.put(COL_7_DEPOSIT_AM, (String) null);
                    }
                    if (p1 != 0) {
                        cv.put(COL_8_P1_AM, p1);
                    }else{
                        cv.put(COL_8_P1_AM, (String) null);
                    }
                    if (p2 != 0) {
                        cv.put(COL_9_P2_AM, p2);
                    }else{
                        cv.put(COL_9_P2_AM, (String) null);
                    }
                    if (p3 != 0) {
                        cv.put(COL_10_P3_AM, p3);
                    }else{
                        cv.put(COL_10_P3_AM, (String) null);
                    }
                    if (p4 != 0) {
                        cv.put(COL_11_P4_AM, p4);
                    }else{
                        cv.put(COL_11_P4_AM, (String) null);
                    }
                    if(newSystemTimeDate != null){//compulsory data
                        cv.put(COL_13_SYSTEM_DATETIME_AM,newSystemTimeDate);
                    }else{
                        return false;
                    }
                    success=(dB.update(TABLE0_ACTIVE_MESTRE,cv,Database.COL_1_ID_AM +"= '"+id+"'"+" AND "+Database.COL_13_SYSTEM_DATETIME_AM +"= '"+previousSystemTimeDate+"'",null)!=1) ? false :true;
                }
            }else {
                Toast.makeText(context, "updation only happens in active table but its inactive", Toast.LENGTH_LONG).show();
            }
        }catch (Exception x){
            x.printStackTrace();
            return false;
        }finally{
            if (dB != null){
                if(success){//if success then only commit
                    dB.setTransactionSuccessful();//If you want to commit the transaction there is a method setTransactionSuccessful() which will commit the values in the database.If you want to rollback your transaction then you need to endTransaction() without committing the transaction by setTransactionSuccessful().
                }
                dB.endTransaction(); //Commit or rollback the transaction.it will rollback when some error occur
                dB.close(); // db.close();//closing db after operation performed
            }
        }
        return success;
    }
    public boolean insertWagesOrDepositOnlyToInActiveTableTransaction(String id, String date, String time, String micPath, String remarks, int wages, int p1, int p2, int p3, int p4, int deposit, String isDeposited) {
        boolean success=false;
        SQLiteDatabase dB=null;
        try{
            String activeInactiveSkill[]=getActiveOrInactiveAndSkill(id);

            dB = this.getWritableDatabase();//getting permission
            dB.beginTransaction();//transaction start
            ContentValues cv = new ContentValues();//to enter data at once it is like hash map

            if(activeInactiveSkill[0].equals("0")){//if person is inactive at 0th position and 1st position is person skill

                if(activeInactiveSkill[1].equals(context.getResources().getString(R.string.laber)) || activeInactiveSkill[1].equals(context.getResources().getString(R.string.women_laber))){//check for M OR lG
                    if (id != null) {
                        cv.put(COL_1_ID_IALG, id);
                    }
                    if (date != null) {
                        cv.put(COL_2_DATE_IALG, date);
                    }
                    if (time != null) {
                        cv.put(COL_3_TIME_IALG, time);
                    }
                    if (micPath != null) {
                        cv.put(COL_4_MICPATH_IALG, micPath);
                    }
                    if (remarks != null) {
                        cv.put(COL_5_REMARKS_IALG, remarks);
                    }
                    if (wages != 0) {
                        cv.put(COL_6_WAGES_IALG, wages);
                    }
                    if (deposit != 0) {
                        cv.put(COL_7_DEPOSIT_IALG, deposit);
                    }
                    if (p1 != 0) {
                        cv.put(COL_8_P1_IALG, p1);
                    }
                    if (p2 != 0) {
                        cv.put(COL_9_P2_IALG, p2);
                    }
                    if (p3 != 0) {
                        cv.put(COL_10_P3_IALG, p3);
                    }
                    if (p4 != 0) {
                        cv.put(COL_11_P4_IALG, p4);
                    }
                    if (isDeposited != null) {//its very important to have value user have to pass either 0 for not deposit or 1 for deposit because when editing this value is used to decide edit
                        cv.put(COL_12_ISDEPOSITED_IALG, isDeposited);
                    } else {
                        Log.d(this.getClass().getSimpleName(),"isDeposited value cannot be null "+Thread.currentThread().getStackTrace()[2].getMethodName());
                        return false;
                    }
                    success=(dB.insert(TABLE3_IN_ACTIVE_LG, null, cv) == -1)? false :true; //if data not inserted return -1
                }else if(activeInactiveSkill[1].equals(context.getResources().getString(R.string.mestre))){//check for mestre
                    if (id != null) {
                        cv.put(COL_1_ID_IAM, id);
                    }
                    if (date != null) {
                        cv.put(COL_2_DATE_IAM, date);
                    }
                    if (time != null) {
                        cv.put(COL_3_TIME_IAM, time);
                    }
                    if (micPath != null) {
                        cv.put(COL_4_MICPATH_IAM, micPath);
                    }
                    if (remarks != null) {
                        cv.put(COL_5_REMARKS_IAM, remarks);
                    }
                    if (wages != 0) {
                        cv.put(COL_6_WAGES_IAM, wages);
                    }
                    if (deposit != 0) {
                        cv.put(COL_7_DEPOSIT_IAM, deposit);
                    }
                    if (p1 != 0) {
                        cv.put(COL_8_P1_IAM, p1);
                    }
                    if (p2 != 0) {
                        cv.put(COL_9_P2_IAM, p2);
                    }
                    if (p3 != 0) {
                        cv.put(COL_10_P3_IAM, p3);
                    }
                    if (p4 != 0) {
                        cv.put(COL_11_P4_IAM, p4);
                    }
                    if (isDeposited != null) {//its very important to have value user have to pass either 0 for not deposit or 1 for deposit because when editing this value is used to decide edit
                        cv.put(COL_12_ISDEPOSITED_IAM, isDeposited);
                    } else {
                        Toast.makeText(context, "isDeposited value cannot be null ", Toast.LENGTH_LONG).show();
                        return false;
                    }
                    success=(dB.insert(TABLE2_IN_ACTIVE_MESTRE, null,cv) == -1)? false :true; //if data not inserted return -1
                }
            }else {
                Toast.makeText(context, "insertion SHOULD happens in Inactive table", Toast.LENGTH_LONG).show();
             }
        }catch (Exception x){
            x.printStackTrace();
            return false;
        }finally{
            if (dB != null){
                if(success){//if success then only commit
                    dB.setTransactionSuccessful();//If you want to commit the transaction there is a method setTransactionSuccessful() which will commit the values in the database.If you want to rollback your transaction then you need to endTransaction() without committing the transaction by setTransactionSuccessful().
                }
                dB.endTransaction(); //Commit or rollback the transaction.it will rollback when some error occur
                dB.close(); // db.close();//closing db after operation performed
            }
        }
        return success;
    }
    public Cursor getWagesDepositDataForRecyclerView(String id){
        try{
            String activeInactiveSkill[]=getActiveOrInactiveAndSkill(id);

            if(activeInactiveSkill[0].equals("1")){//if person is active

                if(activeInactiveSkill[1].equals(context.getResources().getString(R.string.laber)) || activeInactiveSkill[1].equals(context.getResources().getString(R.string.women_laber))){//check for l OR G

                    return  getData("SELECT "+Database.COL_2_DATE_ALG +","+Database.COL_4_MICPATH_ALG +","+Database.COL_5_REMARKS_ALG +","+Database.COL_6_WAGES_ALG +","+Database.COL_7_DEPOSIT_ALG +","+Database.COL_8_P1_ALG +","+Database.COL_9_P2_ALG +","+Database.COL_10_P3_ALG +","+Database.COL_11_P4_ALG +","+Database.COL_1_ID_ALG +","+Database.COL_3_TIME_ALG +","+Database.COL_12_ISDEPOSITED_ALG +","+Database.COL_13_SYSTEM_DATETIME_ALG+" FROM "+Database.TABLE1_ACTIVE_LG +" WHERE "+Database.COL_1_ID_ALG +"='"+id+"'");

                }else if(activeInactiveSkill[1].equals(context.getResources().getString(R.string.mestre))){//check for mestre

                    return  getData("SELECT "+Database.COL_2_DATE_AM +","+Database.COL_4_MICPATH_AM +","+Database.COL_5_REMARKS_AM +","+Database.COL_6_WAGES_AM +","+Database.COL_7_DEPOSIT_AM +","+Database.COL_8_P1_AM +","+Database.COL_9_P2_AM +","+Database.COL_10_P3_AM +","+Database.COL_11_P4_AM +","+Database.COL_1_ID_AM +","+Database.COL_3_TIME_AM +","+Database.COL_12_ISDEPOSITED_AM +","+Database.COL_13_SYSTEM_DATETIME_AM+" FROM "+Database.TABLE0_ACTIVE_MESTRE +" WHERE "+Database.COL_1_ID_AM +"='"+id+"'");

                }
            }else if(activeInactiveSkill[0].equals("0")){//if person is inactive

                if(activeInactiveSkill[1].equals(context.getResources().getString(R.string.laber)) || activeInactiveSkill[1].equals(context.getResources().getString(R.string.women_laber))){//check for l or G

                    return  getData("SELECT "+Database.COL_2_DATE_IALG +","+Database.COL_4_MICPATH_IALG +","+Database.COL_5_REMARKS_IALG +","+Database.COL_6_WAGES_IALG +","+Database.COL_7_DEPOSIT_IALG +","+Database.COL_8_P1_IALG +","+Database.COL_9_P2_IALG +","+Database.COL_10_P3_IALG +","+Database.COL_11_P4_IALG +","+Database.COL_1_ID_IALG +","+Database.COL_3_TIME_IALG +","+Database.COL_12_ISDEPOSITED_IALG+","+Database.COL_13_SYSTEM_DATETIME_IALG+" FROM "+Database.TABLE3_IN_ACTIVE_LG +" WHERE "+Database.COL_1_ID_IALG +"='"+id+"'");

                }else if(activeInactiveSkill[1].equals(context.getResources().getString(R.string.mestre))){//check for mestre

                    return  getData("SELECT "+Database.COL_2_DATE_IAM +","+Database.COL_4_MICPATH_IAM +","+Database.COL_5_REMARKS_IAM +","+Database.COL_6_WAGES_IAM +","+Database.COL_7_DEPOSIT_IAM +","+Database.COL_8_P1_IAM +","+Database.COL_9_P2_IAM +","+Database.COL_10_P3_IAM +","+Database.COL_11_P4_IAM +","+Database.COL_1_ID_IAM +","+Database.COL_3_TIME_IAM +","+Database.COL_12_ISDEPOSITED_IAM +","+Database.COL_13_SYSTEM_DATETIME_IAM+" FROM "+Database.TABLE2_IN_ACTIVE_MESTRE +" WHERE "+Database.COL_1_ID_IAM +"='"+id+"'");

                }
            }
        }catch (Exception x){
            x.printStackTrace();
           return null;
        }
        return null;
    }
    public Cursor getSumOfWagesP1P2P3P4Deposit(String id){
        try{
            String activeInactiveSkill[]=getActiveOrInactiveAndSkill(id);

            if(activeInactiveSkill[0].equals("1")){//if person is active

                if(activeInactiveSkill[1].equals(context.getResources().getString(R.string.laber)) || activeInactiveSkill[1].equals(context.getResources().getString(R.string.women_laber))){//check for l OR G

                    return  getData("SELECT SUM("+Database.COL_6_WAGES_ALG +"),SUM("+Database.COL_8_P1_ALG +"),SUM("+Database.COL_9_P2_ALG +"),SUM("+Database.COL_10_P3_ALG +"),SUM("+Database.COL_11_P4_ALG +"),SUM("+Database.COL_7_DEPOSIT_ALG +") FROM "+Database.TABLE1_ACTIVE_LG+" WHERE "+Database.COL_1_ID_ALG +"= '"+id +"'");

                }else if(activeInactiveSkill[1].equals(context.getResources().getString(R.string.mestre))){//check for mestre

                    return getData("SELECT SUM("+Database.COL_6_WAGES_AM +"),SUM("+Database.COL_8_P1_AM +"),SUM("+Database.COL_9_P2_AM +"),SUM("+Database.COL_10_P3_AM +"),SUM("+Database.COL_11_P4_AM +"),SUM("+Database.COL_7_DEPOSIT_AM +") FROM "+Database.TABLE0_ACTIVE_MESTRE+" WHERE "+Database.COL_1_ID_AM +"= '"+id +"'");

                }
            }else if(activeInactiveSkill[0].equals("0")){//if person is inactive

                if(activeInactiveSkill[1].equals(context.getResources().getString(R.string.laber)) || activeInactiveSkill[1].equals(context.getResources().getString(R.string.women_laber))){//check for l or G

                    return getData("SELECT SUM("+Database.COL_6_WAGES_IALG +"),SUM("+Database.COL_8_P1_IALG +"),SUM("+Database.COL_9_P2_IALG +"),SUM("+Database.COL_10_P3_IALG +"),SUM("+Database.COL_11_P4_IALG +"),SUM("+Database.COL_7_DEPOSIT_IALG +") FROM "+Database.TABLE3_IN_ACTIVE_LG+" WHERE "+Database.COL_1_ID_IALG +"= '"+id +"'");

                }else if(activeInactiveSkill[1].equals(context.getResources().getString(R.string.mestre))){//check for mestre

                    return  getData("SELECT SUM("+Database.COL_6_WAGES_IAM +"),SUM("+Database.COL_8_P1_IAM +"),SUM("+Database.COL_9_P2_IAM +"),SUM("+Database.COL_10_P3_IAM +"),SUM("+Database.COL_11_P4_IAM +"),SUM("+Database.COL_7_DEPOSIT_IAM +") FROM "+Database.TABLE2_IN_ACTIVE_MESTRE+" WHERE "+Database.COL_1_ID_IAM +"= '"+id +"'");

                }
            }
        }catch (Exception x){
            x.printStackTrace();
            return null;
        }
        return null;
    }
    public Cursor getWagesForUpdate(String id, String previousSystemDateTime){
        try{
            String activeInactiveSkill[]=getActiveOrInactiveAndSkill(id);

            if(activeInactiveSkill[0].equals("1")){//if person is active

                if(activeInactiveSkill[1].equals(context.getResources().getString(R.string.laber)) || activeInactiveSkill[1].equals(context.getResources().getString(R.string.women_laber))){//check for l OR G

                   return getData("SELECT "+Database.COL_1_ID_ALG +","+Database.COL_2_DATE_ALG +","+Database.COL_3_TIME_ALG +","+Database.COL_5_REMARKS_ALG +","+Database.COL_6_WAGES_ALG +","+Database.COL_8_P1_ALG +","+Database.COL_9_P2_ALG +","+Database.COL_10_P3_ALG +","+Database.COL_11_P4_ALG +","+Database.COL_4_MICPATH_ALG +","+Database.COL_13_SYSTEM_DATETIME_ALG +" FROM " + Database.TABLE1_ACTIVE_LG + " WHERE "+Database.COL_1_ID_ALG +"= '" + id + "'" + " AND "+Database.COL_13_SYSTEM_DATETIME_ALG +"= '"+previousSystemDateTime + "'");

                }else if(activeInactiveSkill[1].equals(context.getResources().getString(R.string.mestre))){//check for mestre

                    return getData("SELECT "+Database.COL_1_ID_AM +","+Database.COL_2_DATE_AM +","+Database.COL_3_TIME_AM +","+Database.COL_5_REMARKS_AM +","+Database.COL_6_WAGES_AM +","+Database.COL_8_P1_AM +","+Database.COL_9_P2_AM +","+Database.COL_10_P3_AM +","+Database.COL_11_P4_AM +","+Database.COL_4_MICPATH_AM +","+Database.COL_13_SYSTEM_DATETIME_AM +" FROM " + Database.TABLE0_ACTIVE_MESTRE + " WHERE "+Database.COL_1_ID_AM +"= '" + id + "'" + " AND "+Database.COL_13_SYSTEM_DATETIME_AM +"= '"+previousSystemDateTime+ "'");

                }
            }else if(activeInactiveSkill[0].equals("0")){//if person is inactive

                if(activeInactiveSkill[1].equals(context.getResources().getString(R.string.laber)) || activeInactiveSkill[1].equals(context.getResources().getString(R.string.women_laber))){//check for l or G

                    return getData("SELECT "+Database.COL_1_ID_IALG +","+Database.COL_2_DATE_IALG +","+Database.COL_3_TIME_IALG +","+Database.COL_5_REMARKS_IALG +","+Database.COL_6_WAGES_IALG +","+Database.COL_8_P1_IALG +","+Database.COL_9_P2_IALG +","+Database.COL_10_P3_IALG +","+Database.COL_11_P4_IALG +","+Database.COL_4_MICPATH_IALG +","+Database.COL_13_SYSTEM_DATETIME_IALG +" FROM " + Database.TABLE3_IN_ACTIVE_LG + " WHERE "+Database.COL_1_ID_IALG +"= '" + id + "'" + " AND "+Database.COL_13_SYSTEM_DATETIME_IALG +"= '" + previousSystemDateTime +"'");

                }else if(activeInactiveSkill[1].equals(context.getResources().getString(R.string.mestre))){//check for mestre

                    return getData("SELECT "+Database.COL_1_ID_IAM +","+Database.COL_2_DATE_IAM +","+Database.COL_3_TIME_IAM +","+Database.COL_5_REMARKS_IAM +","+Database.COL_6_WAGES_IAM +","+Database.COL_8_P1_IAM +","+Database.COL_9_P2_IAM +","+Database.COL_10_P3_IAM +","+Database.COL_11_P4_IAM +","+Database.COL_4_MICPATH_IAM +","+Database.COL_13_SYSTEM_DATETIME_IAM +" FROM " + Database.TABLE2_IN_ACTIVE_MESTRE + " WHERE "+Database.COL_1_ID_IAM +"= '" + id + "'" + " AND "+Database.COL_13_SYSTEM_DATETIME_IAM +"= '" + previousSystemDateTime+ "'");

                }
            }
        }catch (Exception x){
            x.printStackTrace();
            return null;
        }
        return null;
    }
    public Cursor getDepositForUpdate(String id, String systemDateTime){
        try{
            String activeInactiveSkill[]=getActiveOrInactiveAndSkill(id);

            if(activeInactiveSkill[0].equals("1")){//if person is active

                if(activeInactiveSkill[1].equals(context.getResources().getString(R.string.laber)) || activeInactiveSkill[1].equals(context.getResources().getString(R.string.women_laber))){//check for l OR G

                     return getData("SELECT  "+Database.COL_5_REMARKS_ALG +","+Database.COL_7_DEPOSIT_ALG +","+Database.COL_4_MICPATH_ALG +","+Database.COL_2_DATE_ALG +" FROM " + Database.TABLE1_ACTIVE_LG + " WHERE "+Database.COL_1_ID_ALG +"= '" + id + "' AND "+Database.COL_13_SYSTEM_DATETIME_ALG +"= '" + systemDateTime+ "'");

                }else if(activeInactiveSkill[1].equals(context.getResources().getString(R.string.mestre))){//check for mestre

                     return getData("SELECT  "+Database.COL_5_REMARKS_AM +","+Database.COL_7_DEPOSIT_AM +","+Database.COL_4_MICPATH_AM +","+Database.COL_2_DATE_AM +" FROM " + Database.TABLE0_ACTIVE_MESTRE + " WHERE "+Database.COL_1_ID_AM +"= '" + id + "' AND "+Database.COL_13_SYSTEM_DATETIME_AM +"= '" + systemDateTime+ "'");

                }
            }else if(activeInactiveSkill[0].equals("0")){//if person is inactive

                if(activeInactiveSkill[1].equals(context.getResources().getString(R.string.laber)) || activeInactiveSkill[1].equals(context.getResources().getString(R.string.women_laber))){//check for l or G

                     return getData("SELECT  "+Database.COL_5_REMARKS_IALG +","+Database.COL_7_DEPOSIT_IALG +","+Database.COL_4_MICPATH_IALG +","+Database.COL_2_DATE_IALG +" FROM " + Database.TABLE3_IN_ACTIVE_LG + " WHERE "+Database.COL_1_ID_IALG +"= '" + id + "' AND "+Database.COL_13_SYSTEM_DATETIME_IALG +"= '" + systemDateTime+ "'");

                }else if(activeInactiveSkill[1].equals(context.getResources().getString(R.string.mestre))){//check for mestre

                     return getData("SELECT  "+Database.COL_5_REMARKS_IAM +","+Database.COL_7_DEPOSIT_IAM +","+Database.COL_4_MICPATH_IAM +","+Database.COL_2_DATE_IAM +" FROM " + Database.TABLE2_IN_ACTIVE_MESTRE + " WHERE "+Database.COL_1_ID_IAM +"= '" + id + "' AND "+Database.COL_13_SYSTEM_DATETIME_IAM +"= '" + systemDateTime +"'");

                }
            }
        }catch (Exception x){
            x.printStackTrace();
            return null;
        }
        return null;
    }
    public boolean makeIdActive(String id) {//to make it active updating latestDate is compulsory
        /**if table is inactive then only data is shifted from inactive to active table and make id active.If this method return false then
         * other method cannot insert or update in active table due to id inactive*/
        try {
            String activeInactiveSkill[] = getActiveOrInactiveAndSkill(id);
            if (activeInactiveSkill[0].equals("0")){//if person is inactive then insert data from inactive table to active table
                 if(isRedundantDataPresentInOtherTable(id)){
                     Toast.makeText(context,context.getResources().getString(R.string.check_last_remarks), Toast.LENGTH_LONG).show();
                     return false;
                 }
                Cursor dataFromInActiveTableCursor = getWagesDepositDataForRecyclerView(id);

                if (activeInactiveSkill[1].equals(context.getResources().getString(R.string.laber)) || activeInactiveSkill[1].equals(context.getResources().getString(R.string.women_laber))) {//check for l OR G

                    return shiftDataToActiveTable(dataFromInActiveTableCursor,id,Database.TABLE3_IN_ACTIVE_LG,true,activeInactiveSkill[1]);

                }else if (activeInactiveSkill[1].equals(context.getResources().getString(R.string.mestre))) {//check for mestre

                    return shiftDataToActiveTable(dataFromInActiveTableCursor,id,Database.TABLE2_IN_ACTIVE_MESTRE,true,activeInactiveSkill[1]);
                }
            }else{//optional
                Toast.makeText(context, "id is already active no need to do anything ", Toast.LENGTH_LONG).show();
            }
        }catch(Exception x) {
            x.printStackTrace();
            return false;
        }
        return false;
    }
    public boolean makeIdInActive(String id) {//to make it active updating latestDate is compulsory
        /**if table is active then only data is shifted from active to inactive table and make id inactive.latest date is not updated because it will be automatically updated during time*/
        try {
            String activeInactiveSkill[] = getActiveOrInactiveAndSkill(id);
            if (activeInactiveSkill[0].equals("1")){//if person is active then insert data from active table to inactive table
                if(isRedundantDataPresentInOtherTable(id)){
                    Toast.makeText(context,context.getResources().getString(R.string.check_last_remarks), Toast.LENGTH_LONG).show();
                    return false;
                }
                Cursor dataFromActiveTableCursor = getWagesDepositDataForRecyclerView(id);

                if (activeInactiveSkill[1].equals(context.getResources().getString(R.string.laber)) || activeInactiveSkill[1].equals(context.getResources().getString(R.string.women_laber))) {//check for l OR G

                    return shiftDataToInActiveTable(dataFromActiveTableCursor,id,Database.TABLE1_ACTIVE_LG,true,activeInactiveSkill[1]);

                }else if (activeInactiveSkill[1].equals(context.getResources().getString(R.string.mestre))) {//check for mestre

                    return shiftDataToInActiveTable(dataFromActiveTableCursor,id,Database.TABLE0_ACTIVE_MESTRE,true,activeInactiveSkill[1]);
                }
            }else{//optional
                Toast.makeText(context, "id is already inactive no need to do anything ", Toast.LENGTH_LONG).show();
            }
        }catch(Exception x) {
            x.printStackTrace();
            return false;
        }
        return false;
    }
    private boolean isRedundantDataPresentInOtherTable(String id){//if redundant return true else false
    /**if redundant data is present then this method delete duplicate data from other table and add all data remarks to current table and return true.
     * if there is no redundant data then this method return false*/
        boolean isRedundantDataPresent=false;
        try{
            String activeAndSkill[]=getActiveOrInactiveAndSkill(id);
            byte currentTableNumber=getTableNumber(activeAndSkill);
            boolean tables[]=getInWhichTableRedundantDataIsPresent(id,currentTableNumber);//return 5 size of table and last index indicate duplicate data is present or not

            if(tables[4]){//if true then execute
                for (byte otherTable = 0; otherTable < 4; otherTable++){//since only 4 otherTable so 4
                    if (tables[otherTable] && otherTable != currentTableNumber){//otherTable != currentTableNumber because in current table data will be present so stoping that.
                        isRedundantDataPresent = true;//means duplicate data is present

                        if (!deleteFromOtherTableAndAddRemarksInCurrentTableTransactional(otherTable, id, activeAndSkill)){
                            return true;//if failed to delete and add remarks
                        }
                    }
                }
            }
            if (isRedundantDataPresent) return true;
        }catch (Exception x){
            x.printStackTrace();
            return true;//if exception raised that means all table is not checked so return true which indicate that may be redundant data present in table
        }
        return false;
    }
    private boolean deleteFromOtherTableAndAddRemarksInCurrentTableTransactional(byte duplicateDataPresentTableNumber, String id, String activeAndSkill[]) {//this method should be called only when id is inactive because data will be inserted directly to inactive table without checking
        /**in this method we can either insert only message or insert all entries individually by taking cursor data directly.for now this method only inserting message as entries having all entries of other table*/
        boolean success=false;
        SQLiteDatabase dB =null;
        try {
            dB = this.getWritableDatabase();//getting permission it should be here to rollback
            dB.beginTransaction();//transaction start
            Cursor otherTableDataCursor= getDataFromOtherTable(duplicateDataPresentTableNumber,id);
            //StringBuilder remarks=new StringBuilder(formatCursorDataToText(otherTableDataCursor)).append("\n\nALL "+otherTableDataCursor.getCount()+" ENTRIES TOTAL SUM").append(MyUtility.getMessageOnlyTotalWagesAndDeposit(id,context));
            String  remarks= formatCursorDataToText(otherTableDataCursor);

            if(activeAndSkill[0].equals("1")){//insert remarks in current active table
                if(!insertWagesOrDepositToActiveTableDirectly(dB,MyUtility.systemCurrentDate24hrTime(), activeAndSkill[1], id, MyUtility.getOnlyCurrentDate(), MyUtility.getOnlyTime(), null, remarks, 0, 0, 0, 0, 0, 0, "0")){
                    return false;
                }
            }else if (activeAndSkill[0].equals("0")){//insert remarks in current inactive table
               if(!insertWagesOrDepositToInActiveTableDirectly(dB,MyUtility.systemCurrentDate24hrTime(),activeAndSkill[1], id, MyUtility.getOnlyCurrentDate(), MyUtility.getOnlyTime(), null, remarks, 0, 0, 0, 0, 0, 0, "0")){
                   return false;
               }
            }

            ArrayList<String> allAudioList=getAllAudioPathFromDb(id,getTableName(duplicateDataPresentTableNumber));//getting audio from other table

            dB.delete(getTableName(duplicateDataPresentTableNumber), "ID= '" + id + "'", null);//deleting data from other table table

            if(!deleteAudioOrPdf(allAudioList)){//deleting audio from other table.if above delete operation fail then this operation should not work
                return false;
            }

            success=true;
        }catch (Exception x){
            x.printStackTrace();
            return false;
        }finally {
            if (dB != null){
                if(success){//if success then only commit
                    dB.setTransactionSuccessful();//If you want to commit the transaction there is a method setTransactionSuccessful() which will commit the values in the database.If you want to rollback your transaction then you need to endTransaction() without committing the transaction by setTransactionSuccessful().
                }
                dB.endTransaction(); //Commit or rollback the transaction
                dB.close();
            }
        }
        return success;
    }
    private String formatCursorDataToText(Cursor otherTableDataCursor){//first deposit then wages data will be displayed
        if(otherTableDataCursor == null) return null;
        StringBuilder wagesSb=new StringBuilder();
        StringBuilder depositSb=new StringBuilder();

        depositSb.append("["+MyUtility.getOnlyTime()+context.getResources().getString(R.string.hyphen_automatic_entered))
//                .append("\n\nDATA FOUND IN OTHER TABLE. ")
//                .append("\nDATA DELETED FROM OTHER TABLE. ")
//                .append("\nTOTAL NUMBER OF DELETED DATA: ").append(otherTableDataCursor.getCount());
                                                        //or
//                .append("\n\n").append(otherTableDataCursor.getCount()).append(" ENTRIES FOUND IN OTHER TABLE.")
//                .append("\n").append(otherTableDataCursor.getCount()).append(" ENTRIES DELETED FROM OTHER TABLE.")
//                .append("\n").append(otherTableDataCursor.getCount()).append(" DELETED ENTRIES ARE: ");

                .append("\n\n").append(otherTableDataCursor.getCount()).append(" ENTRIES FOUND IN OTHER TABLE.")
                .append("\nENTRIES ARE: ");

        while(otherTableDataCursor.moveToNext()){//sequence number cant be used because it will not be in sequence order
            
            if(otherTableDataCursor.getString(8).equals("0")) {//IF wages
               wagesSb.append("\n\n").append("-> ").append(otherTableDataCursor.getString(0)).append(" WAGES=").append(otherTableDataCursor.getString(2)).append("  ");
               
               if(otherTableDataCursor.getString(4) != null) {
                   wagesSb.append(otherTableDataCursor.getString(4)).append(" ");
               }
                if(otherTableDataCursor.getString(5) != null) {
                    wagesSb.append(otherTableDataCursor.getString(5)).append(" ");
                }
                if(otherTableDataCursor.getString(6) != null) {
                    wagesSb.append(otherTableDataCursor.getString(6)).append(" ");
                }
                if(otherTableDataCursor.getString(7) != null) {
                    wagesSb.append(otherTableDataCursor.getString(7)).append(" ");
                }
                wagesSb.append("\n").append(otherTableDataCursor.getString(1));//remarks
            }else if (otherTableDataCursor.getString(8).equals("1")) {//if deposit
                depositSb.append("\n\n").append("-> ").append(otherTableDataCursor.getString(0)).append(" DEPOSITED=").append(otherTableDataCursor.getString(3)).append("\n").append(otherTableDataCursor.getString(1));
            }
         }
        return depositSb.append(wagesSb).append("\n\n----------FINISH----------").toString();
    }
    private Cursor getDataFromOtherTable(byte tableNumber, String id) {
        if(tableNumber == -1) return null;//if incorrect table number
        try{
            switch (tableNumber){
                case 0: return getData("SELECT "+Database.COL_2_DATE_AM +","+Database.COL_5_REMARKS_AM +","+Database.COL_6_WAGES_AM +","+Database.COL_7_DEPOSIT_AM +","+Database.COL_8_P1_AM +","+Database.COL_9_P2_AM +","+Database.COL_10_P3_AM +","+Database.COL_11_P4_AM +","+Database.COL_12_ISDEPOSITED_AM+","+Database.COL_13_SYSTEM_DATETIME_AM +" FROM "+getTableName(tableNumber)+" WHERE "+Database.COL_1_ID_AM +"='"+id+"'");
                case 1: return getData("SELECT "+Database.COL_2_DATE_ALG +","+Database.COL_5_REMARKS_ALG +","+Database.COL_6_WAGES_ALG +","+Database.COL_7_DEPOSIT_ALG +","+Database.COL_8_P1_ALG +","+Database.COL_9_P2_ALG +","+Database.COL_10_P3_ALG +","+Database.COL_11_P4_ALG +","+Database.COL_12_ISDEPOSITED_ALG +","+Database.COL_13_SYSTEM_DATETIME_ALG +" FROM "+getTableName(tableNumber)+" WHERE "+Database.COL_1_ID_ALG +"='"+id+"'");
                case 2: return getData("SELECT "+Database.COL_2_DATE_IAM +","+Database.COL_5_REMARKS_IAM +","+Database.COL_6_WAGES_IAM +","+Database.COL_7_DEPOSIT_IAM +","+Database.COL_8_P1_IAM +","+Database.COL_9_P2_IAM +","+Database.COL_10_P3_IAM +","+Database.COL_11_P4_IAM +","+Database.COL_12_ISDEPOSITED_IAM +","+Database.COL_13_SYSTEM_DATETIME_IAM +" FROM "+getTableName(tableNumber)+" WHERE "+Database.COL_1_ID_IAM +"='"+id+"'");
                case 3: return getData("SELECT "+Database.COL_2_DATE_IALG +","+Database.COL_5_REMARKS_IALG +","+Database.COL_6_WAGES_IALG +","+Database.COL_7_DEPOSIT_IALG +","+Database.COL_8_P1_IALG +","+Database.COL_9_P2_IALG +","+Database.COL_10_P3_IALG +","+Database.COL_11_P4_IALG +","+Database.COL_12_ISDEPOSITED_IALG +","+Database.COL_13_SYSTEM_DATETIME_IALG +" FROM "+getTableName(tableNumber)+" WHERE "+Database.COL_1_ID_IALG +"='"+id+"'");
            }
        }catch (Exception x){
            x.printStackTrace();
            return null;
        }
        return null;
    }
    private boolean[] getInWhichTableRedundantDataIsPresent(String id, byte tableNumber) {
        if(tableNumber == -1) return null;//if incorrect table number
        boolean table[]=new boolean[5];//default value false and last position indicate in array duplicate data is present or not if present then value will be true or else false
              try {
                  Cursor cursor;
                  for(byte otherTable=0 ;  otherTable < 4 ; otherTable++){//traversing four table
                      if (tableNumber != otherTable){// SELECT EXISTS (SELECT 1 FROM active_l_g_wages_table WHERE ID = '9'); This query only checks for the existence of a row with the specified condition. It doesn't need to retrieve any actual data; it just needs to determine if any matching row exists
                        cursor=getData("SELECT EXISTS (SELECT 1 FROM " + getTableName(otherTable) + " WHERE ID = '" + id + "')");
                        cursor.moveToFirst();
                          if (cursor.getShort(0) == 1){//if getShort(0) is 1 that means data is present in table
                              table[otherTable] = true;//if data is present then make it true
                              table[4]=true;//storing at last index true to indicate duplicate data is present
                          }
                      }
                  }
              }catch (Exception x){
                  x.printStackTrace();
                  return null;
              }
          return table;
    }
    private boolean shiftDataToActiveTable(Cursor dataFromTableCursor, String id,String tableNameToDelete,boolean updateLatestDate,String insertAccordingToSkillIntoActiveTable) {//this method should be called only when id is active because data will be inserted directly to active table without checking
        boolean success=false;
        SQLiteDatabase dB =null;
       try {
            dB = this.getWritableDatabase();//getting permission it should be here to rollback
            dB.beginTransaction();//transaction start

            if(updateLatestDate){
                dB.execSQL("UPDATE " + Database.TABLE_NAME1+ " SET " + Database.COL_12_ACTIVE + "='" + 1 + "' , "+ Database.COL_15_LATESTDATE + "='" + MyUtility.getOnlyCurrentDate() + "' , " + Database.COL_16_TIME + "='" + MyUtility.getOnlyTime() + "' WHERE " + Database.COL_1_ID + "='" + id + "'");//Making it active to shift data in active table
            }

            if (dataFromTableCursor != null) {
                while (dataFromTableCursor.moveToNext()){
                   if(!insertWagesOrDepositToActiveTableDirectly(dB,dataFromTableCursor.getString(12),insertAccordingToSkillIntoActiveTable,dataFromTableCursor.getString(9), dataFromTableCursor.getString(0), dataFromTableCursor.getString(10), dataFromTableCursor.getString(1), dataFromTableCursor.getString(2), dataFromTableCursor.getInt(3), dataFromTableCursor.getInt(5), dataFromTableCursor.getInt(6), dataFromTableCursor.getInt(7), dataFromTableCursor.getInt(8), dataFromTableCursor.getInt(4), dataFromTableCursor.getString(11))){
                       return false;
                   }
                }
                    dB.delete(tableNameToDelete, "ID= '" + id + "'", null);
            }
            success=true;
        }catch (Exception x){
            x.printStackTrace();
            return false;
        }finally {
            if (dB != null){
                if(success){//if success then only commit
                    dB.setTransactionSuccessful();//If you want to commit the transaction there is a method setTransactionSuccessful() which will commit the values in the database.If you want to rollback your transaction then you need to endTransaction() without committing the transaction by setTransactionSuccessful().
                }
                dB.endTransaction(); //Commit or rollback the transaction
                dB.close();
            }
        }
        return success;
    }
    private boolean shiftDataToInActiveTable(Cursor dataFromTableCursor, String id,String tableNameToDelete,boolean updateActive,String skill) {//this method should be called only when id is inactive because data will be inserted directly to inactive table without checking
        boolean success=false;
        SQLiteDatabase dB=null;
        try {
            dB = this.getWritableDatabase();//getting permission it should be here
            dB.beginTransaction();//transaction start
            if(updateActive) {
                dB.execSQL("UPDATE " + Database.TABLE_NAME1 + " SET " + Database.COL_12_ACTIVE + "='" + 0 + "' WHERE " + Database.COL_1_ID + "='" + id + "'");//Making it INactive to shift data in inactive table.latest date is not updated due to automaticallly updated
            }

            if (dataFromTableCursor != null) {
                while (dataFromTableCursor.moveToNext()) {
                    if(!insertWagesOrDepositToInActiveTableDirectly(dB,dataFromTableCursor.getString(12),skill, dataFromTableCursor.getString(9), dataFromTableCursor.getString(0), dataFromTableCursor.getString(10), dataFromTableCursor.getString(1), dataFromTableCursor.getString(2), dataFromTableCursor.getInt(3), dataFromTableCursor.getInt(5), dataFromTableCursor.getInt(6), dataFromTableCursor.getInt(7), dataFromTableCursor.getInt(8), dataFromTableCursor.getInt(4), dataFromTableCursor.getString(11))){
                        return false;
                    }
                }
                dB.delete(tableNameToDelete, "ID= '" + id + "'", null);
            }
            success=true;
        }catch (Exception x){
            x.printStackTrace();
            return false;
        }finally {
            if (dB != null){
                if(success){//if success then only commit
                    dB.setTransactionSuccessful();//If you want to commit the transaction there is a method setTransactionSuccessful() which will commit the values in the database.If you want to rollback your transaction then you need to endTransaction() without committing the transaction by setTransactionSuccessful().
                }
                dB.endTransaction(); //Commit or rollback the transaction
                dB.close();
            }
        }
        return success;
    }
    private boolean activateIdWithLatestDate(String id, String onlyTime) {
        if(id == null || onlyTime == null) return false;

        if (isActiveOrInactive(id)) {//if active then update active and latest date
           return updateTable("UPDATE " + Database.TABLE_NAME1 + " SET " + Database.COL_12_ACTIVE + "='" + 1 + "' , "+ Database.COL_15_LATESTDATE + "='" + MyUtility.getOnlyCurrentDate() + "' , " + Database.COL_16_TIME + "= '" + onlyTime + "' WHERE " + Database.COL_1_ID + "='" + id + "'");
        }else {
             return makeIdActive(id);
        }
    }
    public boolean updateTable(String query){
        boolean success=false;
        SQLiteDatabase dB=null;
        try{
            dB=this.getWritableDatabase();
            dB.beginTransaction();//transaction start
            dB.execSQL(query);
            success=true;
        }catch(Exception e){
            e.printStackTrace();
            return false;
        }finally{
            if (dB != null){
                if(success){//if success then only commit
                    dB.setTransactionSuccessful();//If you want to commit the transaction there is a method setTransactionSuccessful() which will commit the values in the database.If you want to rollback your transaction then you need to endTransaction() without committing the transaction by setTransactionSuccessful().
                }
                dB.endTransaction(); //Commit or rollback the transaction.it will rollback when some error occur
                dB.close();
            }
        }
        return success;
    }
    public boolean update_Rating_TABLE_NAME3(String star,String remarks,String leavingDate,String returningDate,int r1,int r2,int r3,int r4,String id,int indicator){
        boolean success=false;
        SQLiteDatabase dB=null;
        try {
            dB = this.getWritableDatabase();//getting permission it should be here
            dB.beginTransaction();//transaction start
            ContentValues cv = new ContentValues();//to enter data at once it is like hash map
            cv.put(COL_391_STAR, star);
            cv.put(COL_393_REFFERAL_REMARKS, remarks);
            cv.put(COL_392_LEAVINGDATE, leavingDate);
            cv.put(COL_398_RETURNINGDATE,returningDate);
            switch (indicator){//to avoid unnecessary update
                case 1:{
                    cv.put(COL_32_R1,r1);
                }break;
                case 2:{
                    cv.put(COL_32_R1,r1);
                    cv.put(COL_33_R2,r2);
                }break;
                case 3:{
                    cv.put(COL_32_R1,r1);
                    cv.put(COL_33_R2,r2);
                    cv.put(COL_34_R3,r3);
                }break;
                case 4:{
                    cv.put(COL_32_R1,r1);
                    cv.put(COL_33_R2,r2);
                    cv.put(COL_34_R3,r3);
                    cv.put(COL_35_R4,r4);
                }break;
            }
             success=(dB.update(TABLE_NAME3,cv,Database.COL_31_ID+"= '"+id+"'",null)!=1)? false :true;//if update return 1 then data is updated else not updated
        }catch (Exception e){
            e.printStackTrace();
            return false;
        }finally {
            if (dB != null){
                if(success){//if success then only commit
                    dB.setTransactionSuccessful();//If you want to commit the transaction there is a method setTransactionSuccessful() which will commit the values in the database.If you want to rollback your transaction then you need to endTransaction() without committing the transaction by setTransactionSuccessful().
                }
                dB.endTransaction(); //Commit or rollback the transaction
                dB.close();
            }
        }
        return success;
    }
    private boolean insertPdf(String id,byte [] pdf,byte whichPdf1or2,SQLiteDatabase dB){
        try {
            ContentValues cv = new ContentValues();//to enter data at once it is like hash map
            if(whichPdf1or2==1) {
                cv.put(COL_394_INVOICE1, pdf);//pdf1
            }else if(whichPdf1or2==2){
                cv.put(COL_395_INVOICE2, pdf);//pdf2
            }
            return (dB.update(TABLE_NAME3,cv,Database.COL_31_ID+"= '"+id+"'",null) == 1)?true:false;//if update return 1 then data is updated else not updated
        }catch (Exception e){
            e.printStackTrace();
            return false;
        }
    }
    public String columnNameOutOf4Table(String id, byte columnIndex){//this method will take more time
        try{
             return getColumnName(getTableNumber(getActiveOrInactiveAndSkill(id)),columnIndex);
        }catch (Exception x){
            x.printStackTrace();
            return null;
        }
    }
    public String tableNameOutOf4Table(String id){//this method will take more time
        try{
            return getTableName(getTableNumber(getActiveOrInactiveAndSkill(id)));
        }catch (Exception x){
            x.printStackTrace();
            return null;
        }
    }
    private String getColumnName(byte tableNumber, byte columnIndex) {
        switch (tableNumber){//hardcoded
            case 0: return columnNameOfTableActiveMestre(columnIndex);
            case 1: return columnNameOfTableActiveLaberAndG(columnIndex);
            case 2: return columnNameOfTableInActiveMestre(columnIndex);
            case 3: return columnNameOfTableInActiveLaberAndG(columnIndex);
        }
        return null;
    }
    private String columnNameOfTableActiveMestre(byte columnIndex){
        switch (columnIndex){
            case 1: return Database.COL_1_ID_AM;
            case 2: return Database.COL_2_DATE_AM;
            case 3: return Database.COL_3_TIME_AM;
            case 4: return Database.COL_4_MICPATH_AM;
            case 5: return Database.COL_5_REMARKS_AM;
            case 6: return Database.COL_6_WAGES_AM;
            case 7: return Database.COL_7_DEPOSIT_AM;
            case 8: return Database.COL_8_P1_AM;
            case 9: return Database.COL_9_P2_AM;
            case 10: return Database.COL_10_P3_AM;
            case 11: return Database.COL_11_P4_AM;
            case 12: return Database.COL_12_ISDEPOSITED_AM;
            case 13: return Database.COL_13_SYSTEM_DATETIME_AM;
        }
        return null;
    }
    private String columnNameOfTableActiveLaberAndG(byte columnIndex){
        switch (columnIndex){
            case 1: return Database.COL_1_ID_ALG;
            case 2: return Database.COL_2_DATE_ALG;
            case 3: return Database.COL_3_TIME_ALG;
            case 4: return Database.COL_4_MICPATH_ALG;
            case 5: return Database.COL_5_REMARKS_ALG;
            case 6: return Database.COL_6_WAGES_ALG;
            case 7: return Database.COL_7_DEPOSIT_ALG;
            case 8: return Database.COL_8_P1_ALG;
            case 9: return Database.COL_9_P2_ALG;
            case 10: return Database.COL_10_P3_ALG;
            case 11: return Database.COL_11_P4_ALG;
            case 12: return Database.COL_12_ISDEPOSITED_ALG;
            case 13: return Database.COL_13_SYSTEM_DATETIME_ALG;
        }
        return null;
    }
    private String columnNameOfTableInActiveMestre(byte columnIndex){
        switch (columnIndex){
            case 1: return Database.COL_1_ID_IAM;
            case 2: return Database.COL_2_DATE_IAM;
            case 3: return Database.COL_3_TIME_IAM;
            case 4: return Database.COL_4_MICPATH_IAM;
            case 5: return Database.COL_5_REMARKS_IAM;
            case 6: return Database.COL_6_WAGES_IAM;
            case 7: return Database.COL_7_DEPOSIT_IAM;
            case 8: return Database.COL_8_P1_IAM;
            case 9: return Database.COL_9_P2_IAM;
            case 10: return Database.COL_10_P3_IAM;
            case 11: return Database.COL_11_P4_IAM;
            case 12: return Database.COL_12_ISDEPOSITED_IAM;
            case 13: return Database.COL_13_SYSTEM_DATETIME_IAM;
        }
        return null;
    }
    private String columnNameOfTableInActiveLaberAndG(byte columnIndex){
        switch (columnIndex){
            case 1: return Database.COL_1_ID_IALG;
            case 2: return Database.COL_2_DATE_IALG;
            case 3: return Database.COL_3_TIME_IALG;
            case 4: return Database.COL_4_MICPATH_IALG;
            case 5: return Database.COL_5_REMARKS_IALG;
            case 6: return Database.COL_6_WAGES_IALG;
            case 7: return Database.COL_7_DEPOSIT_IALG;
            case 8: return Database.COL_8_P1_IALG;
            case 9: return Database.COL_9_P2_IALG;
            case 10: return Database.COL_10_P3_IALG;
            case 11: return Database.COL_11_P4_IALG;
            case 12: return Database.COL_12_ISDEPOSITED_IALG;
            case 13: return Database.COL_12_ISDEPOSITED_IALG;
        }
        return null;
    }
    private String[] getActiveOrInactiveAndSkill(String id){
        db = this.getReadableDatabase();
        Cursor cursor=null;
        try {
            cursor=db.rawQuery("SELECT  "+Database.COL_12_ACTIVE+" ," +Database.COL_8_MAINSKILL1 +" FROM " + Database.TABLE_NAME1 + " WHERE "+Database.COL_1_ID+"= '" + id +"'",null);
            cursor.moveToFirst();
            return new String[]{cursor.getString(0),cursor.getString(1)};
        }catch (Exception x){
            x.printStackTrace();
            return null;
        }finally{
            if(cursor != null) {
                    cursor.close();
                }
        }
    }
    private String getTableName(byte tableNumber){
        switch (tableNumber){//hardcoded
            case 0: return Database.TABLE0_ACTIVE_MESTRE;
            case 1: return Database.TABLE1_ACTIVE_LG;
            case 2: return Database.TABLE2_IN_ACTIVE_MESTRE;
            case 3: return Database.TABLE3_IN_ACTIVE_LG;
        }
        return null;
    }
    private byte getTableNumber(String activeOrInactiveAndSkill[]){
        if (activeOrInactiveAndSkill[0].equals("1")) {//active
            if (activeOrInactiveAndSkill[1].equals(context.getResources().getString(R.string.mestre))) {
                return 0;
            } else if (activeOrInactiveAndSkill[1].equals(context.getResources().getString(R.string.laber)) || activeOrInactiveAndSkill[1].equals(context.getResources().getString(R.string.women_laber))){
                return 1;
            }
        } else if (activeOrInactiveAndSkill[0].equals("0")) {//inactive
            if (activeOrInactiveAndSkill[1].equals(context.getResources().getString(R.string.mestre))) {
                return 2;
            } else if (activeOrInactiveAndSkill[1].equals(context.getResources().getString(R.string.laber)) || activeOrInactiveAndSkill[1].equals(context.getResources().getString(R.string.women_laber))){
                return 3;
            }
        }
        return -1;//means incorrect table number
    }
    public String getMainSkill(String id){
        db = this.getReadableDatabase();
        Cursor cursor=null;
        try {
            cursor=db.rawQuery("SELECT  "+Database.COL_8_MAINSKILL1 +" FROM " + Database.TABLE_NAME1 + " WHERE "+Database.COL_1_ID+"= '" + id +"'",null);
            cursor.moveToFirst();
            return cursor.getString(0);
        }catch (Exception x){
            x.printStackTrace();
            return null;
        }finally{
            if(cursor != null) {
                    cursor.close();
                }
        }
    }
    public boolean isActiveOrInactive(String id){
        Database db=Database.getInstance(context);
        Cursor cursor=null;
        try{
            cursor=db.getData("SELECT  "+Database.COL_12_ACTIVE+" FROM "+ Database.TABLE_NAME1 + " WHERE "+Database.COL_1_ID+"= '" + id +"'");
            cursor.moveToFirst();
            return cursor.getString(0).equals("1")? true :false;
        }catch (Exception x){
            x.printStackTrace();
            return false;
        }finally{
                if(cursor != null) {
                    cursor.close();
                }
        }
    }
//    public boolean deleteAllRowsTransaction(String id, String tableName){//delete all rows
//        boolean success = false;
//        SQLiteDatabase dB=null;
//        try {
//            dB = this.getWritableDatabase();
//            dB.beginTransaction();
//
//            int row = dB.delete(tableName, "ID= '" + id + "'", null);//if the delete method fails to delete rows due to an error or any other reason, it will return -1. A return value of -1 indicates that the delete operation encountered an error or failed to execute for some reason
//            if (row >= 0){//delete method return number of record deleted.if there is no record then return 0 so = is used
//                success = true;
//            }
//        }catch (Exception e){
//            e.printStackTrace();
//            return false;
//        }finally{
//            if (dB != null){
//                if(success){//if success then only commit
//                    dB.setTransactionSuccessful();//If you want to commit the transaction there is a method setTransactionSuccessful() which will commit the values in the database.If you want to rollback your transaction then you need to endTransaction() without committing the transaction by setTransactionSuccessful().
//                }
//                dB.endTransaction(); //Commit or rollback the transaction
//                dB.close();
//            }
//        }
//        return success;
//    }
    public boolean deleteAudioFirstThenWagesAndDepositThenAddFinalMessageThenUpdatePdfSequence(String id,int totalDeposit,int totalWages,int p1,int p2,int p3,int p4,int r1,int r2,int r3,int r4,byte indicate,int []innerArray){
    /**should be follow in sequence strictly because to call this method id should be active otherwise data will Be inserted directly to other table
     * 1.Check for redundant data present in other table if present then delete those data and add in recyclerview as remarks so that user would know.Redundant data will be in table when deletion operation delete only half data while making id active or in active but this will not happen due to transaction applied just for 100% confirmation checking
     * 2.check id is active or not.if not active then make active.Its Important otherwise data will be inserted in other table
     * 3.updateRateTotalAdvanceOrBalanceToDatabase() calculate first so that other method would access db and get updated balance or advance
     * 4.get total sum of wages deposit and m l m m to specify as remarks after final calculation.Do this operation otherwise it will be lost after operation perform ie.deletion.after that
     * 5.create pdf and text file to get updated rate data if user changes rate after that
     * 6.save pdf in db then deleted from device
     * 7.get all audio path from db and store in arraylist as backup.if all operation executed successfully then delete all audio from device save option.but its important to delete audio first then all data from table otherwise audio path will be deleted from table
     * 8.delete all wages and deposit
     * 9.update pdf sequence so that while will get updated pdf sequence while add final remarks to recycler view
     * 10.add final message to database or recycle view so that after deletion message would be inserted and visible to user
     * 11.deleted all audio from device which is present in arraylist*/

     /**1.Check for redundant data present in other table if present then delete those data and add in recyclerview as remarks so that user would know.Redundant data will be in table when deletion operation delete only half data while making id active or in active but this will not happen due to transaction applied just for 100% confirmation checking*/
    if(isRedundantDataPresentInOtherTable(id)){//if redundant data is present then it will delete duplicate data from other table and that data will be inserted in current table as remarks so that user would know about redundant data.but this thing would not happen just for tight checking
           // Toast.makeText(context,context.getResources().getString(R.string.check_last_remarks), Toast.LENGTH_LONG).show();//cant use during background task
            return false;
    }
     /**2.check id is active or not.if not active then make active.Its Important otherwise data will be inserted in other table*/
      boolean success=false;//value should be false
      if(!activateIdWithLatestDate(id,MyUtility.getOnlyTime())){
         // Toast.makeText(context, context.getResources().getString(R.string.failed_to_make_id_active), Toast.LENGTH_LONG).show();//cant use during background task
          return false;
      }

      SQLiteDatabase dB =null;
      String pdfAbsolutePath=null;
    try {
        dB = this.getWritableDatabase();//getting permission it should be here to rollback
        dB.beginTransaction();//transaction start

       /**3.updateRateTotalAdvanceOrBalanceToDatabase() calculate first so that other method would access db and get updated balance or advance*/
        success=updateRateTotalDaysWorkedTotalAdvanceOrBalanceToDatabase(dB,id,totalDeposit,  totalWages,  p1,  p2,  p3,  p4,  r1,  r2,  r3,  r4,  indicate,innerArray);//this method updateRateTotalAdvanceOrBalanceToDatabase() calculate first so that other method would access db and get updated balance or advance
        if(!success) return false;

        /**4.get total sum of wages deposit and m l m m to specify as remarks after final calculation.Do this operation otherwise it will be lost after operation perform ie.deletion.after that*/
        String previousInvoiceSummaryTotal=MyUtility.getMessageOnlyTotalWagesAndDeposit(id,context);//getting total sum of wages deposit and m l m m to specify as remarks after final calculation.getting data here otherwise it will be lost after deletion

        /**5.create pdf and text file to get updated rate data if user changes rate after that*/
         pdfAbsolutePath = generatePDFAndReturnFileAbsolutePath(id);//first pdf is created here to get updated rate data if user changes rate
        if(pdfAbsolutePath == null) {//if error in creating pdf pdfAbsolutePath will be null
            success=false;//to rollback transaction
            return false;
        }
        success=MyUtility.createTextFileInvoice(id,context,context.getExternalFilesDir(null).toString());//creating text file for backup in device folder
        if(!success) return false;

        /**6.save pdf in db then deleted from device*/
        success=savePdfToDatabaseAndDeleteFromDevice(pdfAbsolutePath,id,dB);//after saving pdf in db then deleted from device
        if(!success) return false;

        /**7.get all audio path from db and store in arraylist as backup.if all operation executed successfully then delete all audio from device save option.but its important to delete audio first then all data from table otherwise audio path will be deleted from table*/
        ArrayList<String> allAudioList=getAllAudioPathFromDb(id,tableNameOutOf4Table(id));//before deleting all audio getting all audio from db and storing in arraylist

        /**8.delete all wages and deposit*/
         dB.delete(tableNameOutOf4Table(id), "ID= '" + id + "'", null);//if error occur then control goes to catch block there success will be false so if above code is executes successfully that operation should not be committed

        /**9.update pdf sequence*/
        success=updateInvoiceNumberBy1ToDb(id,dB);
        if(!success) return false;

        /**10.add final message to database or recycle view so that after deletion message would be inserted and visible to user*/
         success=addMessageAfterFinalCalculationToRecyclerview(id,dB,previousInvoiceSummaryTotal);
         if(!success)return false;

         //if control reach till here that means all main important operation executed successfully. If this code fail to execute then no need to rollback
        /**11.deleted all audio from device*/
        success=deleteAudioOrPdf(allAudioList);
        if(!success){
//            Toast.makeText(context, "FAILED TO DELETE ALL AUDIOS FROM YOUR DEVICE AUDIO ID: " + id + "\n\n\ncheck remarks in recycler view", Toast.LENGTH_LONG).show();
//            if (!insertWagesOrDepositToActiveTableDirectly(dB,getMainSkill(id),id, MyUtility.getOnlyCurrentDate(), MyUtility.getOnlyTime(), null, "["+ MyUtility.getOnlyTime() +context.getResources().getString(R.string.hyphen_automatic_entered)+"\n\n[FAILED TO DELETE ALL AUDIO FROM YOUR DEVICE.\nPLEASE DELETE ALL AUDIO WITH ID:" + id +" YOURSELF.\nIF NOT DELETED, IT WILL REMAIN IN DEVICE STORAGE WHICH IS NO USE]", 0, 0, 0, 0, 0, 0, "0")) {//this insertion should be perform only when id is active
//                Toast.makeText(context, "OPTIONAL TO DO\nDELETE ALL AUDIO WITH ID: " + id + "\nFROM YOUR DEVICE MANUALLY", Toast.LENGTH_LONG).show();
//            }//toast will not work while doing background task

            insertWagesOrDepositToActiveTableDirectly(dB,MyUtility.systemCurrentDate24hrTime(),getMainSkill(id),id, MyUtility.getOnlyCurrentDate(), MyUtility.getOnlyTime(), null, "["+ MyUtility.getOnlyTime() +context.getResources().getString(R.string.hyphen_automatic_entered)+"\n\n[FAILED TO DELETE ALL AUDIO FROM YOUR DEVICE.\nPLEASE DELETE ALL AUDIO WITH ID:" + id +" YOURSELF.\nIF NOT DELETED, IT WILL REMAIN IN DEVICE STORAGE WHICH IS NO USE]", 0, 0, 0, 0, 0, 0, "0");//this insertion should be perform only when id is active.if this method insertWagesOrDepositToActiveTableDirectly() fails then to delete audio manually message will not be inserted in db or recycler view and user would not see message to delete audio but it will happen very rear
            success=true;//making true to commit all above important operation.If above operation fail then no problem.Only delete audio manually message will not be inserted in db or recycler view and user would not see message to delete audio but it will happen very rear
        }
    }catch (Exception x){
        x.printStackTrace();
        return false;//if delete operation 8 method produce error then this will execute and success will be false from before
    }finally {
        if(!success){//if any operation fail
            MyUtility.deletePdfOrRecordingUsingPathFromDevice(TextFile.textFileAbsolutePathInDevice);//if any operation fail then delete the created text file alse from device
            TextFile.textFileAbsolutePathInDevice=null;//setting null to static variable for save
            MyUtility.deletePdfOrRecordingUsingPathFromDevice(pdfAbsolutePath);//if any operation fail then delete the created pdf from device
        }
        if (dB != null){
            if(success){//if success then only commit
                dB.setTransactionSuccessful();//If you want to commit the transaction there is a method setTransactionSuccessful() which will commit the values in the database.If you want to rollback your transaction then you need to endTransaction() without committing the transaction by setTransactionSuccessful().
            }
            dB.endTransaction(); //Commit or rollback the transaction
            dB.close();
        }
    }
    return success;
  }
    private String generatePDFAndReturnFileAbsolutePath(String id) {//if error return null otherwise file path
        try{
            String fileAbsolutePath;
            MakePdf makePdf = new MakePdf(); //create PDF
            if(!makePdf.createPage1(MakePdf.defaultPageWidth, MakePdf.defaultPageHeight, 1)) return null;//created page 1

            if(!fetchOrganizationDetailsAndWriteToPDF(makePdf)) return null;//org details

            if(!fetchPersonDetailAndWriteToPDF(id,makePdf))return null;//personal detail

            if(!fetchWorkDetailsCalculationAndWriteToPDF(id,makePdf)) return null;//calculation

            if(!makePdf.createdPageFinish2()) return null;

            fileAbsolutePath =makePdf.createFileToSavePdfDocumentAndReturnFile(context.getExternalFilesDir(null).toString(),MyUtility.generateUniqueFileName(context,id)).getAbsolutePath();

            if(!makePdf.closeDocumentLastOperation4())return null;

                return fileAbsolutePath;//fileNameAbsolutePath will be used to get file from device and convert to byteArray and store in db

        }catch (Exception ex){
            Toast.makeText(context, "FAILED TO GENERATE PDF", Toast.LENGTH_LONG).show();
            ex.printStackTrace();
            return null;
        }
    }
    private boolean fetchOrganizationDetailsAndWriteToPDF(MakePdf makePdf) {
        try{
            makePdf.makeTopHeaderOrganizationDetails("RRD Construction Work","GSTIN-123456789123456789", "9436018408", "7005422684", "rrdconstructionbench@gmail.com",false);
            return true;
        }catch(Exception ex){
            ex.printStackTrace();
            return false;
        }
    }
    private boolean fetchPersonDetailAndWriteToPDF(String id, MakePdf makePdf) {
        try (Cursor cursor1 = getData("SELECT " + Database.COL_2_NAME + " , " + Database.COL_3_BANKAC + " , " + Database.COL_6_AADHAAR_NUMBER + " , " + Database.COL_10_IMAGE + " FROM " + Database.TABLE_NAME1 + " WHERE "+Database.COL_1_ID+"='" + id + "'");
            // Cursor cursor2 = getData("SELECT " + Database.COL_396_PDFSEQUENCE + " FROM " + Database.TABLE_NAME3 + " WHERE "+Database.COL_31_ID+"= '" + id + "'")
             ){
            if (cursor1 != null){
                cursor1.moveToFirst();
                String bankAccount, aadhaar;
                int pdfSequenceNo;

                if (cursor1.getString(1).length() > 4) {
                    bankAccount = cursor1.getString(1).substring(cursor1.getString(1).length() - 4);
                } else {
                    bankAccount = "";
                }
                if (cursor1.getString(2).length() > 5) {
                    aadhaar = cursor1.getString(2).substring(cursor1.getString(2).length() - 5);
                } else {
                    aadhaar = "";
                }

//                if (cursor2 != null) {//this make filename unique
//                    cursor2.moveToFirst();
//                    pdfSequenceNo = (cursor2.getInt(0) + 1); /*pdf sequence in db is updated when pdf is generated successfully so for now increasing manually NOT UPDATING in db so that if pdf generation is failed sequence should not be updated in db*/
//                } else {
//                    pdfSequenceNo = -1;//if error
//                }
                pdfSequenceNo=MyUtility.getPdfSequence(id,context);//pdf sequence in db is updated when pdf is generated successfully so for now increasing manually NOT UPDATING in db so that if pdf generation is failed sequence should not be updated in db
                if(pdfSequenceNo != -1){
                    pdfSequenceNo=pdfSequenceNo+1;
                }
//                else {
//                    pdfSequenceNo=-1;//if error
//                }

                String activePhoneNumber=MyUtility.getActivePhoneNumbersFromDb(id,context);
                if(activePhoneNumber != null){
                    activePhoneNumber= activePhoneNumber.substring(activePhoneNumber.length() - 6);//phone number
                }else{
                    activePhoneNumber="";
                }
                makePdf.makePersonImageDetails(cursor1.getString(0), id, bankAccount, aadhaar, cursor1.getBlob(3), String.valueOf(pdfSequenceNo),activePhoneNumber, false);
            }else{
                Toast.makeText(context, "NO DATA IN CURSOR", Toast.LENGTH_LONG).show();
                makePdf.makePersonImageDetails("[NULL NO DATA IN CURSOR]", "[NULL NO DATA IN CURSOR]", "[NULL NO DATA IN CURSOR]", "[NULL NO DATA IN CURSOR]", null, "[NULL]","[NULL NO DATA IN CURSOR]",false);
            }
            return true;
        }catch (Exception ex){
            ex.printStackTrace();
            return false;
        }
    }
    private boolean fetchWorkDetailsCalculationAndWriteToPDF(String id, MakePdf makePdf) {
        try{
            byte indicator= MyUtility.get_indicator(context,id);
            boolean[] errorDetection={false};//when ever exception occur it will be updated to true in method so it indicate error occurred or not
            String[] skillHeader = MyUtility.getWagesHeadersFromDbBasedOnIndicator(context,id, indicator, errorDetection);//THIS SHOULD BE TOP at arrayOfTotalWagesDepositRateAccordingToIndicator   TO AVOID INDEX EXCEPTION
            float[] columnWidth=getColumnWidthBasedOnIndicator(indicator,errorDetection);
            int[] arrayOfTotalWagesDepositRateAccordingToIndicator= MyUtility.getSumOfTotalWagesDepositRateDaysWorkedBasedOnIndicator(context,id,indicator,errorDetection);//if error cause errorDetection will be set true
            String[][] recyclerViewWagesData = MyUtility.getAllWagesDetailsFromDbBasedOnIndicator(context,id, indicator, errorDetection);//it amy return null   when no data
            String[][] recyclerViewDepositData = MyUtility.getAllDepositFromDb(context,id,errorDetection);//it amy return null   when no data
            if(errorDetection[0]==false){
                if(!makeSummaryAndWriteToPDFBasedOnIndicator(context,indicator,id,makePdf,arrayOfTotalWagesDepositRateAccordingToIndicator)) return false;//summary
                if(!makePdf.writeSentenceWithoutLines(new String[]{""},new float[]{100f},true, (byte) 50,(byte)50)) return false;//just for 1 space

                if (recyclerViewWagesData != null){//null means data not present
                    if(makePdf.makeTable(skillHeader, recyclerViewWagesData,columnWidth, 9, false)){
                        //getTotalOfWagesAndWorkingDaysFromDbBasedOnIndicator should be use after all wages displayed
                        if(!makePdf.singleCustomRow(getTotalOfWagesAndWorkingDaysFromDbBasedOnIndicator(indicator, errorDetection, arrayOfTotalWagesDepositRateAccordingToIndicator), columnWidth, 0, Color.rgb(221, 133, 3), 0, 0, true, (byte) 0, (byte) 0)){
                            return false;
                        }
                    }else return false;
                }

                if (recyclerViewDepositData != null) {//if deposit there then draw in pdf
                    if(makePdf.makeTable(new String[]{"DATE", "DEPOSIT", "REMARKS"}, recyclerViewDepositData, new float[]{12f, 12f, 76f}, 9, false)) {//[indicator + 1] is index of deposit
                        if(!makePdf.singleCustomRow(new String[]{"+", MyUtility.convertToIndianNumberSystem(arrayOfTotalWagesDepositRateAccordingToIndicator[indicator + 1]), "****TOTAL DEPOSIT****"}, new float[]{12f, 12f, 76f}, 0, 0, 0, 0, true, (byte) 0, (byte) 0)) {
                            return false;
                        }
                    }else return false;
                }
                if(!addWorkAmountAndDepositBasedOnIndicatorAndWriteToPDF(indicator, arrayOfTotalWagesDepositRateAccordingToIndicator, makePdf, skillHeader)) {return false;}
            }else return false;//means error has occurred

            return true;
        }catch(Exception ex){
            ex.printStackTrace();
            return false;
        }
    }
    private float[] getColumnWidthBasedOnIndicator(byte indicator,boolean[] errorDetection) {
        try{
            switch (indicator) {
                case 1: return new float[]{12f, 12f, 5f, 71f};

                case 2: return new float[]{12f, 12f, 5f, 5f, 66f};

                case 3: return new float[]{12f, 12f, 5f, 5f, 5f, 61f};

                case 4: return new float[]{12f, 12f, 5f, 5f, 5f, 5f, 56f};
            }
            return new float[]{1f,1f,1f};//this code will not execute due to return in switch block just using to avoid error
        }catch (Exception ex){
            ex.printStackTrace();
            errorDetection[0]=true;//indicate error has occur
            return new float[]{1f,1f,1f};//to avoid error
        }
    }
    private boolean makeSummaryAndWriteToPDFBasedOnIndicator(Context context, byte indicator, String id, MakePdf makePdf, int[] arrayOfTotalWagesDepositRateAccordingToIndicator) {
        try(Cursor cursor=getData("SELECT "+Database.COL_13_ADVANCE+" ,"+Database.COL_14_BALANCE+" FROM " + Database.TABLE_NAME1 + " WHERE "+Database.COL_1_ID+"= '" + id + "'")) {
            if(!makePdf.writeSentenceWithoutLines(new String[]{"SUMMARY","",""},new float[]{12f, 50f, 38f},false,(byte)50,(byte)50)) return false;

            cursor.moveToFirst();//means only one row is returned
            if (cursor.getInt(0) != 0 && cursor.getInt(1) == 0) {

                if(!makePdf.singleCustomRow(MyUtility.headersForSummaryBasedOnIndicator(indicator,arrayOfTotalWagesDepositRateAccordingToIndicator,"ADVANCE"),new float[]{25f, 50f, 25f},0,0,0,0,true,(byte)50,(byte)50)) return false;
                //yellow                               green
                if(!makePdf.singleCustomRow(MyUtility.totalWagesWorkAmountDepositAdvanceOrBalanceForSummaryBasedOnIndicator(indicator,arrayOfTotalWagesDepositRateAccordingToIndicator,cursor.getInt(0)),new float[]{25f, 50f, 25f},Color.rgb(221, 133, 3),Color.rgb(26,145,12) ,Color.RED,0,true,(byte)50,(byte)50)) return false;
                if(!makePdf.singleCustomRow(new String[]{ context.getResources().getString(R.string.star_after_calculation_advance_rs_dot) + MyUtility.convertToIndianNumberSystem(cursor.getInt(0))},new float[]{100f},0,0 ,0,0,true,(byte)50,(byte)50)) return false;

            }else if (cursor.getInt(0) == 0 && cursor.getInt(1) != 0) {

                if(!makePdf.singleCustomRow(MyUtility.headersForSummaryBasedOnIndicator(indicator,arrayOfTotalWagesDepositRateAccordingToIndicator,"BALANCE"),new float[]{25f, 50f, 25f},0,0,0,0,true,(byte)50,(byte)50))return false;
                //                                                                                                                                                                                                                      yellow                               green                                    green
                if(!makePdf.singleCustomRow(MyUtility.totalWagesWorkAmountDepositAdvanceOrBalanceForSummaryBasedOnIndicator(indicator,arrayOfTotalWagesDepositRateAccordingToIndicator,cursor.getInt(1)),new float[]{25f, 50f, 25f},Color.rgb(221, 133, 3),Color.rgb(26,145,12) ,Color.rgb(26,145,12),0,true,(byte)50,(byte)50))return false;
                if(!makePdf.singleCustomRow(new String[]{ context.getResources().getString(R.string.star_after_calculation_balance_rs_dot) +MyUtility.convertToIndianNumberSystem(cursor.getInt(1))},new float[]{100f},0,0 ,0,0,true,(byte)50,(byte)50))return false;

            }else if(cursor.getInt(0) == 0 && cursor.getInt(1) == 0){
                if(!makePdf.singleCustomRow(MyUtility.headersForSummaryBasedOnIndicator(indicator,arrayOfTotalWagesDepositRateAccordingToIndicator,"ALL CLEARED"),new float[]{25f, 50f, 25f},0,0,0,0,true,(byte)50,(byte)50))return false;
                // yellow                               green                                green
                if(!makePdf.singleCustomRow(MyUtility.totalWagesWorkAmountDepositAdvanceOrBalanceForSummaryBasedOnIndicator(indicator,arrayOfTotalWagesDepositRateAccordingToIndicator,0),new float[]{25f, 50f, 25f},Color.rgb(221, 133, 3),Color.rgb(26,145,12),Color.rgb(26,145,12),0,true,(byte)50,(byte)50))return false;
                if(!makePdf.singleCustomRow(new String[]{context.getResources().getString(R.string.star_after_calculation_no_dues_dot)},new float[]{100f},0,0 ,0,0,true,(byte)50,(byte)50))return false;

            }
            return true;
        }catch (Exception ex){
            ex.printStackTrace();
            return false;
        }
    }
    private boolean savePdfToDatabaseAndDeleteFromDevice(String pdfAbsolutePath,String id,SQLiteDatabase dB){//storing in device and deleting from device because it is stored in database
        if(pdfAbsolutePath != null) {
            Cursor cursor=null;
            byte[] newPDF=null;
            try {
                newPDF = Files.readAllBytes(Paths.get(pdfAbsolutePath));//CONVERTED pdf file to byte array if path is not found then catch block execute
                cursor = getData("SELECT "+Database.COL_395_INVOICE2+" FROM " + Database.TABLE_NAME3 + " WHERE "+Database.COL_31_ID+"= '" + id + "'"); //so that object close automatically
                cursor.moveToFirst();

                if (cursor.getBlob(0) == null) {//if pdf2 is null then store in pdf2
                    return insertPdf(id, newPDF, (byte) 2, dB);
                }
                //if pdf1 is not null then store in pdf 2
                if (insertPdf(id, cursor.getBlob(0), (byte) 1, dB)) {//store pdf2 in pdf1
                    return insertPdf(id, newPDF, (byte) 2, dB);//store new pdf in pdf2
                }
                return false;
            }catch(SQLiteBlobTooBigException x){//when pdf file is too large.
               //large file is stored in database but cannot be viewed gives error cursor large size while retrieving
               return insertPdf(id, newPDF, (byte) 2, dB);//if large pdf size then store new pdf in pdf2.but in this line pdf sequence will be lost for once because shifting of pdf is not done due to large file.but after again one calculation it will be fine

            }catch (Exception ex){
                ex.printStackTrace();
                return false;
            }finally{
                MyUtility.deletePdfOrRecordingUsingPathFromDevice(pdfAbsolutePath);//after saving created pdf in db and device then delete that pdf from device.not returning true or false because it is not important.but if we return then it will override return value of try or catch block
               if (cursor != null){
                   cursor.close();
               }
            }
        }else return false;
    }
    private String[] getTotalOfWagesAndWorkingDaysFromDbBasedOnIndicator(byte indicator, boolean[] errorDetection,int[] arrayOfTotalWagesDepositRateAccordingToIndicator) {// when no data and if error errorDetection will be set to true
        try{
            switch (indicator) {
                case 1: return new String[]{"+",MyUtility.convertToIndianNumberSystem(arrayOfTotalWagesDepositRateAccordingToIndicator[0]), arrayOfTotalWagesDepositRateAccordingToIndicator[1]+"" ,context.getResources().getString(R.string.star_total_star)};

                case 2: return new String[]{"+", MyUtility.convertToIndianNumberSystem(arrayOfTotalWagesDepositRateAccordingToIndicator[0]), arrayOfTotalWagesDepositRateAccordingToIndicator[1]+"",arrayOfTotalWagesDepositRateAccordingToIndicator[2]+"" ,context.getResources().getString(R.string.star_total_star)};

                case 3: return new String[]{"+", MyUtility.convertToIndianNumberSystem(arrayOfTotalWagesDepositRateAccordingToIndicator[0]), arrayOfTotalWagesDepositRateAccordingToIndicator[1]+"",arrayOfTotalWagesDepositRateAccordingToIndicator[2]+"",arrayOfTotalWagesDepositRateAccordingToIndicator[3]+"" ,context.getResources().getString(R.string.star_total_star)};

                case 4: return new String[]{"+", MyUtility.convertToIndianNumberSystem(arrayOfTotalWagesDepositRateAccordingToIndicator[0]), arrayOfTotalWagesDepositRateAccordingToIndicator[1]+"",arrayOfTotalWagesDepositRateAccordingToIndicator[2]+"",arrayOfTotalWagesDepositRateAccordingToIndicator[3]+"",arrayOfTotalWagesDepositRateAccordingToIndicator[4]+"" ,context.getResources().getString(R.string.star_total_star)};
            }
            return new String[]{"wrong indicator"};//this code will not execute due to return in switch block just using to avoid error
        }catch (Exception ex){
            ex.printStackTrace();
            errorDetection[0]=true;//indicate error has occur
            return new String[]{"error occurred"};//to avoid error
        }
    }
    private boolean addWorkAmountAndDepositBasedOnIndicatorAndWriteToPDF(byte indicator,int[] sumArrayAccordingToIndicator, MakePdf makePdf,String[] skillAccordingToindicator) {
        try{
            switch(indicator){
                case 1: {
                    makePdf.singleCustomRow(new String[]{skillAccordingToindicator[2] + " =", sumArrayAccordingToIndicator[1] + "", context.getResources().getString(R.string.x), context.getResources().getString(R.string.rate), MyUtility.convertToIndianNumberSystem(sumArrayAccordingToIndicator[1] * sumArrayAccordingToIndicator[3])}, new float[]{15f, 20f, 12f, 20f, 33f}, 0, 0, 0, 0, false, (byte) 88, (byte) 88);
                    if (sumArrayAccordingToIndicator[2] == 0) {//DEPOSIT AMOUNT checking there or not or can be use (indicator+1) to get index of deposit
                        makePdf.singleCustomRow(new String[]{context.getResources().getString(R.string.total_work_amount_equal),  MyUtility.convertToIndianNumberSystem(sumArrayAccordingToIndicator[1] * sumArrayAccordingToIndicator[3])}, new float[]{67f, 33f}, 0, Color.rgb(26,145,12), 0, 0, true, (byte) 88, (byte) 88);
                    } else {//when there is deposit then add deposit
                        makePdf.singleCustomRow(new String[]{context.getResources().getString(R.string.total_deposit_equal), MyUtility.convertToIndianNumberSystem(sumArrayAccordingToIndicator[2])}, new float[]{67f, 33f}, 0, 0, 0, 0, true, (byte) 88, (byte) 88);
                        //                                                                                                                                                                                                                                                                           green color
                        makePdf.singleCustomRow(new String[]{context.getResources().getString(R.string.total_work_deposit_plus_work_amount_equal),  MyUtility.convertToIndianNumberSystem((sumArrayAccordingToIndicator[1] * sumArrayAccordingToIndicator[3]) + sumArrayAccordingToIndicator[2])}, new float[]{67f, 33f}, 0, Color.rgb(26,145,12), 0, 0, true, (byte) 88, (byte) 88);
                    }
                }break;
                case 2: {
                    makePdf.singleCustomRow(new String[]{skillAccordingToindicator[2] + " =", sumArrayAccordingToIndicator[1] + "", context.getResources().getString(R.string.x), context.getResources().getString(R.string.rate),MyUtility.convertToIndianNumberSystem(sumArrayAccordingToIndicator[1] * sumArrayAccordingToIndicator[4])}, new float[]{15f, 20f, 12f, 20f, 33f}, 0, 0, 0, 0, false,(byte) 88, (byte) 88);
                    makePdf.singleCustomRow(new String[]{skillAccordingToindicator[3] + " =", sumArrayAccordingToIndicator[2] + "", context.getResources().getString(R.string.x), context.getResources().getString(R.string.rate),MyUtility.convertToIndianNumberSystem(sumArrayAccordingToIndicator[2] * sumArrayAccordingToIndicator[5])}, new float[]{15f, 20f, 12f, 20f, 33f}, 0, 0, 0, 0, true, (byte) 88, (byte) 88);
                    if (sumArrayAccordingToIndicator[3] == 0) {//DEPOSIT AMOUNT checking there or not
                        //                                                                                                                                      P1*R1                              +                             P2*R2
                        makePdf.singleCustomRow(new String[]{context.getResources().getString(R.string.total_work_amount_equal), MyUtility.convertToIndianNumberSystem((sumArrayAccordingToIndicator[1] * sumArrayAccordingToIndicator[4]) + (sumArrayAccordingToIndicator[2] * sumArrayAccordingToIndicator[5]))}, new float[]{67f, 33f}, 0, Color.rgb(26,145,12), 0, 0, true, (byte) 88, (byte) 88);
                    }else{//when there is deposit then add deposit
                        makePdf.singleCustomRow(new String[]{context.getResources().getString(R.string.total_deposit_equal), MyUtility.convertToIndianNumberSystem(sumArrayAccordingToIndicator[3])}, new float[]{67f, 33f}, 0, 0, 0, 0, true, (byte) 88, (byte) 88);
                        //                       P1*R1                             +                             P2*R2                             +  DEPOSIT
                        makePdf.singleCustomRow(new String[]{context.getResources().getString(R.string.total_work_deposit_plus_work_amount_equal),MyUtility.convertToIndianNumberSystem((sumArrayAccordingToIndicator[1] * sumArrayAccordingToIndicator[4]) + (sumArrayAccordingToIndicator[2] * sumArrayAccordingToIndicator[5]) + sumArrayAccordingToIndicator[3])}, new float[]{67f, 33f}, 0, Color.rgb(26,145,12), 0, 0, true, (byte) 88, (byte) 88);
                    }
                }break;
                case 3:{
                    makePdf.singleCustomRow(new String[]{skillAccordingToindicator[2] + " =", sumArrayAccordingToIndicator[1] + "", context.getResources().getString(R.string.x), context.getResources().getString(R.string.rate), MyUtility.convertToIndianNumberSystem(sumArrayAccordingToIndicator[1] * sumArrayAccordingToIndicator[5])}, new float[]{15f, 20f, 12f, 20f, 33f}, 0, 0, 0, 0, false,(byte) 88, (byte) 88);
                    makePdf.singleCustomRow(new String[]{skillAccordingToindicator[3] + " =", sumArrayAccordingToIndicator[2] + "", context.getResources().getString(R.string.x), context.getResources().getString(R.string.rate), MyUtility.convertToIndianNumberSystem(sumArrayAccordingToIndicator[2] * sumArrayAccordingToIndicator[6])}, new float[]{15f, 20f, 12f, 20f, 33f}, 0, 0, 0, 0, true, (byte) 88, (byte) 88);
                    makePdf.singleCustomRow(new String[]{skillAccordingToindicator[4] + " =", sumArrayAccordingToIndicator[3] + "", context.getResources().getString(R.string.x), context.getResources().getString(R.string.rate), MyUtility.convertToIndianNumberSystem(sumArrayAccordingToIndicator[3] * sumArrayAccordingToIndicator[7])}, new float[]{15f, 20f, 12f, 20f, 33f}, 0, 0, 0, 0, true, (byte) 88, (byte) 88);

                    if (sumArrayAccordingToIndicator[4] == 0) {//DEPOSIT AMOUNT checking there or not
                        makePdf.singleCustomRow(new String[]{context.getResources().getString(R.string.total_work_amount_equal),MyUtility.convertToIndianNumberSystem((sumArrayAccordingToIndicator[1] * sumArrayAccordingToIndicator[5]) + (sumArrayAccordingToIndicator[2] * sumArrayAccordingToIndicator[6]) + (sumArrayAccordingToIndicator[3] * sumArrayAccordingToIndicator[7]))}, new float[]{67f, 33f}, 0, Color.rgb(26,145,12), 0, 0, true, (byte) 88, (byte) 88);
                    }else{ //when there is deposit then add deposit
                        makePdf.singleCustomRow(new String[]{context.getResources().getString(R.string.total_deposit_equal), MyUtility.convertToIndianNumberSystem(sumArrayAccordingToIndicator[4])}, new float[]{67f, 33f}, 0, 0, 0, 0, true, (byte) 88, (byte) 88);
                        //                                                                                P1*R1                             +                                P2*R2                                                           P3*R3                                              +  DEPOSIT
                        makePdf.singleCustomRow(new String[]{context.getResources().getString(R.string.total_work_deposit_plus_work_amount_equal),MyUtility.convertToIndianNumberSystem((sumArrayAccordingToIndicator[1] * sumArrayAccordingToIndicator[5]) + (sumArrayAccordingToIndicator[2] * sumArrayAccordingToIndicator[6] + (sumArrayAccordingToIndicator[3] * sumArrayAccordingToIndicator[7])) + sumArrayAccordingToIndicator[4])}, new float[]{67f, 33f}, 0,Color.rgb(26,145,12), 0, 0, true, (byte) 88, (byte) 88);
                    }
                }break;
                case 4:{
                    makePdf.singleCustomRow(new String[]{skillAccordingToindicator[2] + " =", sumArrayAccordingToIndicator[1] + "", context.getResources().getString(R.string.x), context.getResources().getString(R.string.rate),MyUtility.convertToIndianNumberSystem(sumArrayAccordingToIndicator[1] * sumArrayAccordingToIndicator[6])}, new float[]{15f, 20f, 12f, 20f, 33f}, 0, 0, 0, 0, false,(byte) 88, (byte) 88);
                    makePdf.singleCustomRow(new String[]{skillAccordingToindicator[3] + " =", sumArrayAccordingToIndicator[2] + "", context.getResources().getString(R.string.x), context.getResources().getString(R.string.rate),MyUtility.convertToIndianNumberSystem(sumArrayAccordingToIndicator[2] * sumArrayAccordingToIndicator[7])}, new float[]{15f, 20f, 12f, 20f, 33f}, 0, 0, 0, 0, true, (byte) 88, (byte) 88);
                    makePdf.singleCustomRow(new String[]{skillAccordingToindicator[4] + " =", sumArrayAccordingToIndicator[3] + "", context.getResources().getString(R.string.x), context.getResources().getString(R.string.rate),MyUtility.convertToIndianNumberSystem(sumArrayAccordingToIndicator[3] * sumArrayAccordingToIndicator[8])}, new float[]{15f, 20f, 12f, 20f, 33f}, 0, 0, 0, 0, true, (byte) 88, (byte) 88);
                    makePdf.singleCustomRow(new String[]{skillAccordingToindicator[5] + " =", sumArrayAccordingToIndicator[4] + "", context.getResources().getString(R.string.x), context.getResources().getString(R.string.rate),MyUtility.convertToIndianNumberSystem(sumArrayAccordingToIndicator[4] * sumArrayAccordingToIndicator[9])}, new float[]{15f, 20f, 12f, 20f, 33f}, 0, 0, 0, 0, true, (byte) 88, (byte) 88);

                    if(sumArrayAccordingToIndicator[5] == 0) {//DEPOSIT AMOUNT checking there or not
                        makePdf.singleCustomRow(new String[]{context.getResources().getString(R.string.total_work_amount_equal),MyUtility.convertToIndianNumberSystem((sumArrayAccordingToIndicator[1] * sumArrayAccordingToIndicator[6]) + (sumArrayAccordingToIndicator[2] * sumArrayAccordingToIndicator[7]) + (sumArrayAccordingToIndicator[3] * sumArrayAccordingToIndicator[8]) + (sumArrayAccordingToIndicator[4] * sumArrayAccordingToIndicator[9]))}, new float[]{67f, 33f}, 0,Color.rgb(26,145,12), 0, 0, true, (byte) 88, (byte) 88);
                    }else{//when there is deposit then add deposit
                        makePdf.singleCustomRow(new String[]{context.getResources().getString(R.string.total_deposit_equal),MyUtility.convertToIndianNumberSystem(sumArrayAccordingToIndicator[5])}, new float[]{67f, 33f}, 0, 0, 0, 0, true, (byte) 88, (byte) 88 );
                        //                                                                                P1*R1                             +                                P2*R2                                                           P3*R3                                                                           P4*R4                                  +  DEPOSIT
                        makePdf.singleCustomRow(new String[]{context.getResources().getString(R.string.total_work_deposit_plus_work_amount_equal),MyUtility.convertToIndianNumberSystem((sumArrayAccordingToIndicator[1] * sumArrayAccordingToIndicator[6]) + (sumArrayAccordingToIndicator[2] * sumArrayAccordingToIndicator[7] + (sumArrayAccordingToIndicator[3] * sumArrayAccordingToIndicator[8]) + (sumArrayAccordingToIndicator[4] * sumArrayAccordingToIndicator[9])) + sumArrayAccordingToIndicator[5])}, new float[]{67f, 33f}, 0,Color.rgb(26,145,12), 0, 0, true, (byte) 88, (byte) 88);
                    }
                }break;
            }
            return true;
        }catch(Exception ex){
            ex.printStackTrace();
            return false;
        }
    }
    private boolean updateRateTotalDaysWorkedTotalAdvanceOrBalanceToDatabase(SQLiteDatabase dB,String id,int totalDeposit,int totalWages,int p1,int p2,int p3,int p4,int r1,int r2,int r3,int r4,byte indicate,int []innerArray){
       try(Cursor cursor = getData("SELECT "+Database.COL_397_TOTAL_WORKED_DAYS +" FROM " + Database.TABLE_NAME3 + " WHERE "+Database.COL_31_ID+"= '" + id + "'")) {
           cursor.moveToFirst();//means only one row is returned

           //updating rate and total worked days
           dB.execSQL("UPDATE " + Database.TABLE_NAME3 + " SET " + Database.COL_32_R1 + "='" + r1 + "' , " + Database.COL_33_R2 + "='" + r2 + "' , " + Database.COL_34_R3 + "='" + r3 + "' , " + Database.COL_35_R4 + "='" + r4 + "' , " + Database.COL_397_TOTAL_WORKED_DAYS + " ='" + (cursor.getInt(0) + p1) + "' WHERE " + Database.COL_31_ID + "='" + id + "'");

           if (!MyUtility.isEnterDataIsWrong(innerArray)) {//if data is right then only change fields.This condition is already checked but checking again
               if (!MyUtility.isp1p2p3p4PresentAndRateNotPresent(r1, r2, r3, r4, p1, p2, p3, p4, indicate)) {//This condition is already checked but checking again
                   //if both wages and total work amount is less then 0 then don't save.This condition already checked but checking again

                   if (((totalDeposit + ((p1 * r1) + (p2 * r2) + (p3 * r3) + (p4 * r4))) < 0) || (totalWages < 0)) {//user cant enter negative number so when (totalDeposit + (totalr1r2r3r4sum1sum2sum3sum4)) is negative that means int range is exceeds so wrong result will be shown
                       Toast.makeText(context, "FAILED TO SAVE DUE TO WRONG DATA", Toast.LENGTH_LONG).show();
                       return false;
                   }
                   if ((totalDeposit + ((p1 * r1) + (p2 * r2) + (p3 * r3) + (p4 * r4))) < totalWages) {
                       //updating Advance to db
                        dB.execSQL("UPDATE " + Database.TABLE_NAME1 + " SET " + Database.COL_13_ADVANCE + "='" + (totalWages - (totalDeposit + ((p1 * r1) + (p2 * r2) + (p3 * r3) + (p4 * r4)))) + "' WHERE " + Database.COL_1_ID + "='" + id + "'");

                       //if there is advance then balance  column should be 0
                        dB.execSQL("UPDATE " + Database.TABLE_NAME1 + " SET " + Database.COL_14_BALANCE + "='" + 0 + "' WHERE " + Database.COL_1_ID + "='" + id + "'");

                   } else if ((totalDeposit + ((p1 * r1) + (p2 * r2) + (p3 * r3) + (p4 * r4))) >= totalWages) {//>= is given because when totalWages and total work is same then this condition will be executed to set balance 0

                       //updating balance to db if greater then 0
                       dB.execSQL("UPDATE " + Database.TABLE_NAME1 + " SET " + Database.COL_14_BALANCE + "='" + ((totalDeposit + ((p1 * r1) + (p2 * r2) + (p3 * r3) + (p4 * r4))) - totalWages) + "' WHERE " + Database.COL_1_ID + "='" + id + "'");

                       //if there is balance then update advance column should be 0
                        dB.execSQL("UPDATE " + Database.TABLE_NAME1 + " SET " + Database.COL_13_ADVANCE + "='" + 0 + "' WHERE " + Database.COL_1_ID + "='" + id + "'");
                   }
               } else {
                   Toast.makeText(context, "FAILED TO SAVE DUE TO RATE NOT PROVIDED", Toast.LENGTH_LONG).show();
                   return false;
               }
           } else {
               Toast.makeText(context, "FAILED TO SAVE DUE TO WRONG DATA", Toast.LENGTH_LONG).show();
               return false;
           }
       }catch (Exception x){
           x.printStackTrace();
           return false;
       }
       return true;
    }
    private boolean  addMessageAfterFinalCalculationToRecyclerview(String id,SQLiteDatabase dB,String previousSummary) {//to call this method id should be active otherwise data will ne inserted directly to other table
        try(Cursor cursor=getData("SELECT "+Database.COL_13_ADVANCE+","+Database.COL_14_BALANCE+" FROM " + Database.TABLE_NAME1 + " WHERE "+Database.COL_1_ID+"= '" + id + "'")) {
            cursor.moveToFirst();//means only one row is returned

            if (cursor.getInt(0) != 0 && cursor.getInt(1) == 0) {//if advance there

                return insertWagesOrDepositToActiveTableDirectly(dB,MyUtility.systemCurrentDate24hrTime(),getMainSkill(id),id,MyUtility.getOnlyCurrentDate(), MyUtility.getOnlyTime(),null, "[" + MyUtility.getOnlyTime() +context.getResources().getString(R.string.hyphen_automatic_entered)+"\n\n"+context.getResources().getString(R.string.summary_of_previous_invoice_number_dot)+MyUtility.getPdfSequence(id,context)+previousSummary+"\n\n" + "[After calculation advance Rs. " + cursor.getInt(0)+" ]",cursor.getInt(0),0,0,0,0,0,"0");

            }else if (cursor.getInt(0) == 0 && cursor.getInt(1) != 0) {//if balance there

                return insertWagesOrDepositToActiveTableDirectly(dB,MyUtility.systemCurrentDate24hrTime(),getMainSkill(id),id,MyUtility.getOnlyCurrentDate(),MyUtility.getOnlyTime(),null, "[" +MyUtility.getOnlyTime() +context.getResources().getString(R.string.hyphen_automatic_entered)+"\n\n"+context.getResources().getString(R.string.summary_of_previous_invoice_number_dot)+MyUtility.getPdfSequence(id,context)+previousSummary+"\n\n" + "[After calculation balance Rs. " + cursor.getInt(1)+" ]",0,0,0,0,0,cursor.getInt(1),"1");

            }else if(cursor.getInt(0) == 0 && cursor.getInt(1) == 0){//if no advance and balance

                return insertWagesOrDepositToActiveTableDirectly(dB,MyUtility.systemCurrentDate24hrTime(),getMainSkill(id),id,MyUtility.getOnlyCurrentDate(), MyUtility.getOnlyTime(),null, "[" + MyUtility.getOnlyTime() +context.getResources().getString(R.string.hyphen_automatic_entered)+"\n\n"+context.getResources().getString(R.string.summary_of_previous_invoice_number_dot)+MyUtility.getPdfSequence(id,context)+previousSummary+"\n\n" + "[After calculation no dues  Rs. 0 ]",0,0,0,0,0,0,"0");

            }
            return false;
        }catch (Exception ex){
            ex.printStackTrace();
            return false;
        }
    }
    private boolean updateInvoiceNumberBy1ToDb(String id,SQLiteDatabase dB) {
        try(Cursor cursor=getData("SELECT "+Database.COL_396_PDFSEQUENCE +" FROM " + Database.TABLE_NAME3 + " WHERE "+Database.COL_31_ID+"= '" + id + "'")){
            cursor.moveToFirst();//means only one row is returned
            dB.execSQL("UPDATE " + Database.TABLE_NAME3 + " SET  "+Database.COL_396_PDFSEQUENCE +" ='" + (cursor.getInt(0)+1) +"' WHERE "+Database.COL_31_ID+"='" + id + "'");
            return true;
        }catch (Exception ex){
            ex.printStackTrace();
            return false;
        }
    }
    private boolean deleteAudioOrPdf(ArrayList<String> allAudio) {//deleting all audios
        if(allAudio==null) return false;//means error occurred while fetching audio from db
        if(allAudio.size() != 0) {//allAudio.size() will be 0 when no single audio present in db but allAudio reference will not be null
             for (String audio : allAudio) {
                 if (!MyUtility.deletePdfOrRecordingUsingPathFromDevice(audio)) {
                     return false;
                 }
             }
         }
        return true;//if there is no single audio then also return true
    }
    private ArrayList<String> getAllAudioPathFromDb(String id,String tableName){//may return null when exception occur
        ArrayList<String> audiosPathAl=new ArrayList<>();
        if(isSingleRowPresentInTable(id)){//if data present in table then execute
            try (Cursor cursor = getData("SELECT " + columnNameOutOf4Table(id, (byte) 4) + " FROM " + tableName + " WHERE " + columnNameOutOf4Table(id, (byte) 1) + "= '" + id + "'")) {//so that object close automatically
                while (cursor.moveToNext()) {
                    if (cursor.getString(0) != null) {//checking path may be null
                        audiosPathAl.add(cursor.getString(0));
                    }
                }
            }catch (Exception x) {
                x.printStackTrace();
                return null;
            }
        }
        return audiosPathAl;//if no data then array list size will be 0
    }
    private boolean isSingleRowPresentInTable(String id){
        try(Cursor cursor = getData("SELECT EXISTS (SELECT 1 FROM " + tableNameOutOf4Table(id) + " WHERE ID = '" + id + "')")){//SELECT EXISTS (SELECT 1 FROM active_l_g_wages_table WHERE ID = '9'); This query only checks for the existence of a row with the specified condition. It doesn't need to retrieve any actual data; it just needs to determine if any matching row exists
            cursor.moveToFirst();
            return (cursor.getShort(0) == 1)? true:false;//if getShort(0) is 1 that means data is present in table.
        }catch (Exception x){
            x.printStackTrace();
            return false;
        }
    }
}
//    One common cause of database locking is that you are using multiple instances of SQLiteOpenHelper to access the same database file. This can create conflicts and inconsistencies in the database state, and prevent other instances from accessing it. To avoid this, you should use only one instance of SQLiteOpenHelper throughout your application, and make sure to close it when you are done with it
//        ChatGPT
//        Your statement is generally correct, but there's a slight clarification needed. SQLiteOpenHelper is typically used in Android applications to manage database connections and database version management. It's important to use a single instance of SQLiteOpenHelper throughout your application's lifecycle to prevent issues with database locking and maintain a consistent database state. Here's a more detailed explanation:
//
//        Single Instance of SQLiteOpenHelper: Creating multiple instances of SQLiteOpenHelper to access the same database file can lead to problems. Each instance maintains its own database connection pool, and if multiple instances are used, they may not coordinate their access to the database file effectively, leading to locking conflicts.
//
//        Database Locking: When multiple instances of SQLiteOpenHelper or other database access methods are used concurrently, they may try to perform read and write operations on the same database file simultaneously. This can result in database locking issues, where one instance locks the database for a write operation, preventing other instances from reading or writing until the lock is released. This can lead to inconsistencies and data corruption.
//
//        Consistent Database State: To maintain a consistent database state and avoid locking conflicts, it's best practice to use a single instance of SQLiteOpenHelper throughout your application. This ensures that all database operations are synchronized and that the database is accessed in a coordinated manner.
//
//        Closing the Database: Additionally, it's essential to close the database connection when you are done with it. This can be achieved by calling the close() method on your SQLiteOpenHelper instance. Failing to close the database properly can lead to resource leaks and potential issues with your application.
