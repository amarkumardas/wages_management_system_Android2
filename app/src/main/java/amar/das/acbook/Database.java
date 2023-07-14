package amar.das.acbook;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;


public class Database extends SQLiteOpenHelper {
    public final static int Database_Version=1;//5to update db version just increase the value by 1.when this value is increase then constructor is called
    SQLiteDatabase db;
    public final static String DATABASE_NAME="person_db";

    //table 1---------------------------------------------------------------------------------------
    public final static String TABLE_NAME1="person_details_table";
    public final static String COL_1_ID ="ID";
    public final static String COL_2_NAME ="NAME";
    public final static String COL_3_BANKAC ="BANKACCOUNT";
    public final static String COL_4_IFSCCODE ="IFSCCODE";
    public final static String COL_5_BANKNAME ="BANKNAME";
    public final static String COL_6_AADHAAR_NUMBER ="AADHARCARD";
    public final static String COL_7_ACTIVE_PHONE1 ="PHONE";
    public final static String COL_8_SKILL1 ="SKILL1";//skill1
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

    //table 2---------------------------------------------------------------------------------------
    public final static String TABLE_NAME2="wages_table";
    public final static String COL_21_ID ="ID";
    public final static String COL_22_DATE ="DATE";//here date and time and id is acting like primary key
    public final static String COL_23_TIME ="TIME";
    public final static String COL_24_MICPATH ="MICPATH";
    public final static String COL_25_DESCRIPTION ="DESCRIPTION";
    public final static String COL_26_WAGES ="WAGES";
    public final static String COL_27_DEPOSIT ="DEPOSIT";
    public final static String COL_28_P1 ="P1";
    public final static String COL_29_P2 ="P2";
    public final static String COL_291_P3 ="P3";
    public final static String COL_292_P4 ="P4";
    public final static String COL_293_ISDEPOSITED ="ISDEPOSITED";

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
    public Database(Context context){
        super(context,DATABASE_NAME,null,Database_Version);//The reason of passing null is you want the standard SQLiteCursor behaviour. If you want to implement a specialized Cursor you can get it by by extending the Cursor class( this is for doing additional operations on the query results). And in these cases, you can use the CursorFactory class to return an instance of your Cursor implementation. Here is the document for that https://stackoverflow.com/questions/11643294/what-is-the-use-of-sqlitedatabase-cursorfactory-in-android
        System.out.println("constructor db*****************************");
    }
    //If we explicitly insert default NULL into the column then in database blank will be shown instead of NULL
    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {//it will execute only once        //NOT NULL OR DEFAULT NOT WORKING AND VARCHAR GIVEN VALUE NOT WORKING HOLDING MORE THAN GIVEN VALUE
     try {//if some error occur it will handle
         sqLiteDatabase.execSQL("CREATE TABLE " + TABLE_NAME1 + " ("+COL_1_ID+" INTEGER PRIMARY KEY AUTOINCREMENT , "+COL_2_NAME+" VARCHAR(100) DEFAULT NULL,"+COL_3_BANKAC+" VARCHAR(20) DEFAULT NULL,"+COL_4_IFSCCODE+" VARCHAR(11) DEFAULT NULL,"+COL_5_BANKNAME+" VARCHAR(38) DEFAULT NULL,"+COL_6_AADHAAR_NUMBER+" VARCHAR(12) DEFAULT NULL,"+COL_7_ACTIVE_PHONE1+" VARCHAR(10) DEFAULT NULL, "+ COL_8_SKILL1 +" CHAR(1) DEFAULT NULL,"+COL_9_ACCOUNT_HOLDER_NAME+" VARCHAR(100) DEFAULT NULL, "+COL_11_ACTIVE_PHONE2+" VARCHAR(100) DEFAULT NULL,"+COL_12_ACTIVE+" CHAR(1) DEFAULT 1,"+COL_13_ADVANCE+" NUMERIC DEFAULT NULL,"+COL_14_BALANCE+" NUMERIC DEFAULT NULL,"+COL_15_LATESTDATE+" TEXT DEFAULT NULL,TIME TEXT DEFAULT '0' , "+COL_17_LOCATION+" VARCHAR(30) DEFAULT NULL, "+COL_18_RELIGION+" VARCHAR(20) DEFAULT NULL, "+COL_10_IMAGE+" BLOB DEFAULT NULL);");
         sqLiteDatabase.execSQL("CREATE TABLE " + TABLE_NAME2 + " ("+COL_21_ID+" INTEGER ,"+COL_22_DATE+" TEXT DEFAULT NULL,"+COL_23_TIME+" TEXT DEFAULT NULL,"+COL_24_MICPATH+" TEXT DEFAULT NULL,"+COL_25_DESCRIPTION+" TEXT DEFAULT NULL,"+COL_26_WAGES+" NUMERIC DEFAULT NULL,"+COL_27_DEPOSIT+" NUMERIC DEFAULT NULL,"+COL_28_P1+" INTEGER DEFAULT NULL,"+COL_29_P2+" INTEGER DEFAULT NULL,"+COL_291_P3+" INTEGER DEFAULT NULL,"+COL_292_P4+" INTEGER DEFAULT NULL,"+COL_293_ISDEPOSITED+" CHAR(1) DEFAULT NULL);");
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
    //insert data TO table 1
    public boolean insertDataTable1(String name, String bankAccount, String ifscCode, String bankName, String aadhaarCard, String phoneNumber, String skill, String fatherName, byte[] image, String acHolder,String location,String religion) {
        try {
            db = this.getWritableDatabase();//getting permission
            ContentValues cv = new ContentValues();//to enter data at once it is like hash map
            cv.put(COL_2_NAME, name);
            cv.put(COL_3_BANKAC, bankAccount);
            cv.put(COL_4_IFSCCODE, ifscCode);
            cv.put(COL_5_BANKNAME, bankName);
            cv.put(COL_6_AADHAAR_NUMBER, aadhaarCard);
            cv.put(COL_7_ACTIVE_PHONE1, phoneNumber);
            cv.put(COL_8_SKILL1, skill);
            cv.put(COL_9_ACCOUNT_HOLDER_NAME, fatherName);
            cv.put(COL_10_IMAGE, image);
            cv.put(COL_11_ACTIVE_PHONE2, acHolder);
            cv.put(COL_17_LOCATION, location);
            cv.put(COL_18_RELIGION, religion);
            cv.put(COL_12_ACTIVE,"1");//when new user added then it will be active
            //-1 is returned if error occurred. .insert(...) returns the row id of the new inserted record
            long rowId = db.insert(TABLE_NAME1, null, cv);
           // db.close();//closing db after operation performed
            if (rowId == -1)//data not inserted
                return false;
            else
                return true;
        }catch (Exception e){
            e.printStackTrace();
            return false;
        }finally {
            if(db != null)
                db.close();
        }
    }
     public  Cursor getId(String name, String bankAccount, String ifscCode, String bankName, String aadhaarCard, String phoneNumber, String type, String fatherName,String acHolder,String location,String religion){
            db = this.getWritableDatabase();//error when closing db or cursor
            String query = "SELECT "+Database.COL_1_ID+" FROM " + TABLE_NAME1 + " WHERE "+Database.COL_2_NAME+"='" + name + "' AND "+Database.COL_9_ACCOUNT_HOLDER_NAME+"='" + fatherName + "' AND "+Database.COL_3_BANKAC+"='" + bankAccount + "' AND "+Database.COL_7_ACTIVE_PHONE1+"='" + phoneNumber + "' AND "+Database.COL_4_IFSCCODE+"='" + ifscCode + "' AND "+Database.COL_6_AADHAAR_NUMBER+"='" + aadhaarCard + "' AND "+Database.COL_8_SKILL1 +"='" + type + "' AND "+Database.COL_5_BANKNAME+"='" + bankName + "' AND "+Database.COL_11_ACTIVE_PHONE2+"='" + acHolder + "' AND "+Database.COL_17_LOCATION+"='"+location+"' AND "+Database.COL_18_RELIGION+"='"+religion+"'";
            Cursor cursor = db.rawQuery(query, null);
            return cursor;
     }

    public Cursor getData(String query){//error when closing db or cursor so don't close cursor
            db = this.getReadableDatabase();
            Cursor cursor = db.rawQuery(query, null);
            return cursor;
    }
    //update to Table 1
    public boolean updateDataTable1(String name, String bankAccount, String ifscCode, String bankName, String aadhaarCard, String phoneNumber, String skill, String fatherName, byte[] image, String acHolder, String Id,String location,String religion ) {
        try {
            db = this.getWritableDatabase();//getting permission
            ContentValues cv = new ContentValues();//to enter data at once it is like hash map
            cv.put(COL_2_NAME, name);
            cv.put(COL_3_BANKAC, bankAccount);
            cv.put(COL_4_IFSCCODE, ifscCode);
            cv.put(COL_5_BANKNAME, bankName);
            cv.put(COL_6_AADHAAR_NUMBER, aadhaarCard);
            cv.put(COL_7_ACTIVE_PHONE1, phoneNumber);
            cv.put(COL_8_SKILL1, skill);
            cv.put(COL_9_ACCOUNT_HOLDER_NAME, fatherName);
            cv.put(COL_10_IMAGE, image);
            cv.put(COL_11_ACTIVE_PHONE2, acHolder);
            cv.put(COL_17_LOCATION, location);
            cv.put(COL_18_RELIGION, religion);
           // cv.put(COL_12, "1");//when ever user update that user will become active
            //0 is returned if no record updated and it return number of rows updated
            int rowId = db.update(TABLE_NAME1, cv, Database.COL_1_ID+"=?", new String[]{Id});
            //db.close();//closing db after operation performed
            if(rowId!=1) {//if update return 1 then data is updated else not updated
                return false;
            }

                return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }finally {
            if(db != null)
                db.close();
        }
    }
    //insert data TO table 3
    public boolean insertDataTable3(String id,int r1,int r2,int r3,int r4,String skill1,String skill2,String skill3,String indicator ) {
        try {
            db = this.getWritableDatabase();//getting permission
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
            //-1 is returned if error occurred. .insert(...) returns the row id of the new inserted record
            long rowId = db.insert(TABLE_NAME3, null, cv);
           // db.close();//closing db after operation performed
            if (rowId == -1)//data not inserted
                return false;
            else
                return true;
        }catch (Exception e){
            e.printStackTrace();
            return false;
        }finally {
            if(db != null)
                db.close();
        }
    }
    //insert data TO table 2
    public boolean insert_1_Person_WithWagesTable2(String id, String date,String time, String micPath, String description, int wages, int p1, String isDeposited) {
        try {
            db = this.getWritableDatabase();//getting permission
            ContentValues cv = new ContentValues();//to enter data at once it is like hash map
            cv.put(COL_21_ID, id);
            cv.put(COL_22_DATE, date);
            cv.put(COL_23_TIME, time);
            cv.put(COL_24_MICPATH, micPath);
            cv.put(COL_25_DESCRIPTION, description);
            cv.put(COL_26_WAGES, wages);
            cv.put(COL_28_P1, p1);
           cv.put(COL_293_ISDEPOSITED, isDeposited);
            //-1 is returned if error occurred. .insert(...) returns the row id of the new inserted record
            long rowId = db.insert(TABLE_NAME2, null, cv);
           // db.close();//closing db after operation performed
            if (rowId == -1)//data not inserted
                return false;
            else
                return true;
        }catch (Exception e){
            e.printStackTrace();
            return false;
        }finally {
            if(db != null)
                db.close();
        }
    }
    public boolean insert_2_Person_WithWagesTable2(String id, String date,String time, String micPath, String description, int wages, int p1,int p2, String isDeposited) {
        try {
            db = this.getWritableDatabase();//getting permission
            ContentValues cv = new ContentValues();//to enter data at once it is like hash map
            cv.put(COL_21_ID, id);
            cv.put(COL_22_DATE, date);
            cv.put(COL_23_TIME, time);
            cv.put(COL_24_MICPATH, micPath);
            cv.put(COL_25_DESCRIPTION, description);
            cv.put(COL_26_WAGES, wages);
            cv.put(COL_28_P1, p1);
            cv.put(COL_29_P2, p2);
            cv.put(COL_293_ISDEPOSITED, isDeposited);
            //-1 is returned if error occurred. .insert(...) returns the row id of the new inserted record
            long rowId = db.insert(TABLE_NAME2, null, cv);
           // db.close();//closing db after operation performed
            if (rowId == -1)//data not inserted
                return false;
            else
                return true;
        }catch (Exception e){
            e.printStackTrace();
            return false;
        }finally {
            if(db != null)
                db.close();
        }
    }
    public boolean insert_3_Person_WithWagesTable2(String id, String date,String time, String micPath, String description, int wages, int p1,int p2,int p3, String isDeposited) {
        try {
            db = this.getWritableDatabase();//getting permission
            ContentValues cv = new ContentValues();//to enter data at once it is like hash map
            cv.put(COL_21_ID, id);
            cv.put(COL_22_DATE, date);
            cv.put(COL_23_TIME, time);
            cv.put(COL_24_MICPATH, micPath);
            cv.put(COL_25_DESCRIPTION, description);
            cv.put(COL_26_WAGES, wages);
            cv.put(COL_28_P1, p1);
            cv.put(COL_29_P2, p2);
            cv.put(COL_291_P3, p3);
            cv.put(COL_293_ISDEPOSITED, isDeposited);
            //-1 is returned if error occurred. .insert(...) returns the row id of the new inserted record
            long rowId = db.insert(TABLE_NAME2, null, cv);
           // db.close();//closing db after operation performed
            if (rowId == -1)//data not inserted
                return false;
            else
                return true;
        }catch (Exception e){
            e.printStackTrace();
            return false;
        }finally {
            if(db != null)
                db.close();
        }
    }
    public boolean insert_4_Person_WithWagesTable2(String id, String date,String time, String micPath, String description, int wages, int p1,int p2,int p3,int p4, String isDeposited) {
        try {
            db = this.getWritableDatabase();//getting permission
            ContentValues cv = new ContentValues();//to enter data at once it is like hash map
            cv.put(COL_21_ID, id);
            cv.put(COL_22_DATE, date);
            cv.put(COL_23_TIME, time);
            cv.put(COL_24_MICPATH, micPath);
            cv.put(COL_25_DESCRIPTION, description);
            cv.put(COL_26_WAGES, wages);
            cv.put(COL_28_P1, p1);
            cv.put(COL_29_P2, p2);
            cv.put(COL_291_P3, p3);
            cv.put(COL_292_P4, p4);
            cv.put(COL_293_ISDEPOSITED, isDeposited);
            //-1 is returned if error occurred. .insert(...) returns the row id of the new inserted record
            long rowId = db.insert(TABLE_NAME2, null, cv);
           // db.close();//closing db after operation performed
            if (rowId == -1)//data not inserted
                return false;
            else
                return true;
        }catch (Exception e){
            e.printStackTrace();
            return false;
        }finally {
            if(db != null)
                db.close();
        }
    }
    public boolean insert_Deposit_Table2(String id, String date,String time, String micPath, String description,int deposit,String isDeposited) {
        try {
            db = this.getWritableDatabase();//getting permission
            ContentValues cv = new ContentValues();//to enter data at once it is like hash map
            cv.put(COL_21_ID, id);
            cv.put(COL_22_DATE, date);
            cv.put(COL_23_TIME, time);
            cv.put(COL_24_MICPATH, micPath);
            cv.put(COL_25_DESCRIPTION, description);
            cv.put(COL_27_DEPOSIT, deposit);
            cv.put(COL_293_ISDEPOSITED, isDeposited);
            //-1 is returned if error occurred. .insert(...) returns the row id of the new inserted record
            long rowId = db.insert(TABLE_NAME2, null, cv);
           // db.close();//closing db after operation performed
            if (rowId == -1)//data not inserted
                return false;
            else
                return true;
        }catch (Exception e){
            e.printStackTrace();
            return false;
        }finally {
            if(db != null)
                db.close();
        }
    }
    public boolean updateTable(String query){
        try{
            db=this.getWritableDatabase();
            db.execSQL(query);
            return true;
        }catch(Exception e){
            e.printStackTrace();
            return false;
        }finally {
            if(db != null)
                db.close();
        }
    }
    public boolean update_1_TABLE_NAME2(String date, String time, String remarks, String micPath, int wages , int p1 , String id , String date2, String time2){
        try {
            db = this.getWritableDatabase();//getting permission
            ContentValues cv = new ContentValues();//to enter data at once it is like hash map
            cv.put(COL_22_DATE, date);
            cv.put(COL_23_TIME, time);
            cv.put(COL_24_MICPATH, micPath);
            cv.put(COL_25_DESCRIPTION, remarks);
            cv.put(COL_26_WAGES, wages);
            cv.put(COL_28_P1, p1);
            //100 % it will update
            int rowId = db.update(TABLE_NAME2,cv,Database.COL_21_ID+"= '"+id+"'"+" AND "+Database.COL_22_DATE+"= '"+date2+"'"+" AND "+Database.COL_23_TIME+"= '"+time2+"'",null);
           // db.close();//closing db after operation performed
            if(rowId!=1)//if update return 1 then data is updated else not updated
                return false;
             return  true;
        }catch (Exception e){
            e.printStackTrace();
            return false;
        }finally {
            if(db != null)
                db.close();
        }
    }
    public boolean update_2_TABLE_NAME2(String date, String time, String remarks, String micPath, int wages , int p1, int p2 , String id , String date2, String time2){
        try {
            db = this.getWritableDatabase();//getting permission
            ContentValues cv = new ContentValues();//to enter data at once it is like hash map
            cv.put(COL_22_DATE, date);
            cv.put(COL_23_TIME, time);
            cv.put(COL_24_MICPATH, micPath);
            cv.put(COL_25_DESCRIPTION, remarks);
            cv.put(COL_26_WAGES, wages);
            cv.put(COL_28_P1, p1);
            cv.put(COL_29_P2, p2);
            //100 % it will update
            int rowId = db.update(TABLE_NAME2,cv,Database.COL_21_ID+"= '"+id+"'"+" AND "+Database.COL_22_DATE+"= '"+date2+"'"+" AND "+Database.COL_23_TIME+"= '"+time2+"'",null);
           // db.close();//closing db after operation performed
            if(rowId!=1)//if update return 1 then data is updated else not updated
                return false;
            return  true;
        }catch (Exception e){
            e.printStackTrace();
            return false;
        }finally {
            if(db != null)
                db.close();
        }
    }
    public boolean update_3_TABLE_NAME2(String date, String time, String remarks, String micPath, int wages , int p1, int p2, int p3 , String id , String date2, String time2){
        try {
            db = this.getWritableDatabase();//getting permission
            ContentValues cv = new ContentValues();//to enter data at once it is like hash map
            cv.put(COL_22_DATE, date);
            cv.put(COL_23_TIME, time);
            cv.put(COL_24_MICPATH, micPath);
            cv.put(COL_25_DESCRIPTION, remarks);
            cv.put(COL_26_WAGES, wages);
            cv.put(COL_28_P1, p1);
            cv.put(COL_29_P2, p2);
            cv.put(COL_291_P3, p3);
            //100 % it will update
            int rowId = db.update(TABLE_NAME2,cv,Database.COL_21_ID+"= '"+id+"'"+" AND "+Database.COL_22_DATE+"= '"+date2+"'"+" AND "+Database.COL_23_TIME+"= '"+time2+"'",null);
            //db.close();//closing db after operation performed
            if(rowId!=1)//if update return 1 then data is updated else not updated
                return false;

            return  true;
        }catch (Exception e){
            e.printStackTrace();
            return false;
        }finally {
            if(db != null)
                db.close();
        }
    }
    public boolean update_4_TABLE_NAME2(String date, String time, String remarks, String micPath, int wages , int p1 , int p2, int p3, int p4, String id , String date2, String time2){
        try {
            db = this.getWritableDatabase();//getting permission
            ContentValues cv = new ContentValues();//to enter data at once it is like hash map
            cv.put(COL_22_DATE, date);
            cv.put(COL_23_TIME, time);
            cv.put(COL_24_MICPATH, micPath);
            cv.put(COL_25_DESCRIPTION, remarks);
            cv.put(COL_26_WAGES, wages);
            cv.put(COL_28_P1, p1);
            cv.put(COL_29_P2, p2);
            cv.put(COL_291_P3, p3);
            cv.put(COL_292_P4, p4);
            //100 % it will update
            int rowId = db.update(TABLE_NAME2,cv,Database.COL_21_ID+"= '"+id+"'"+" AND "+Database.COL_22_DATE+"= '"+date2+"'"+" AND "+Database.COL_23_TIME+"= '"+time2+"'",null);
           // db.close();//closing db after operation performed
            if(rowId!=1)//if update return 1 then data is updated else not updated
                return false;

            return  true;
        }catch (Exception e){
            e.printStackTrace();
            return false;
        }finally {
            if(db != null)
                db.close();
        }
    }
    public boolean update_Deposit_TABLE_NAME2(String date,String time, String micPath, String description,int deposit,String id , String date2, String time2) {
        try {
            db = this.getWritableDatabase();//getting permission
            ContentValues cv = new ContentValues();//to enter data at once it is like hash map
            cv.put(COL_22_DATE, date);
            cv.put(COL_23_TIME, time);
            cv.put(COL_24_MICPATH, micPath);
            cv.put(COL_25_DESCRIPTION, description);
            cv.put(COL_27_DEPOSIT, deposit);

             //100 % it will update
            int rowId = db.update(TABLE_NAME2,cv,Database.COL_21_ID+"= '"+id+"'"+" AND "+Database.COL_22_DATE+"= '"+date2+"'"+" AND "+Database.COL_23_TIME+"= '"+time2+"'",null);
           // db.close();//closing db after operation performed
            if(rowId!=1)//if update return 1 then data is updated else not updated
                return false;

            return  true;
        }catch (Exception e){
            e.printStackTrace();
            return false;
        }finally {
            if(db != null)
                db.close();
        }
    }
    public boolean update_Rating_TABLE_NAME3(String star,String remarks,String leavingDate,String returningDate,int r1,int r2,int r3,int r4,String id,int indicator){
        try {
            db = this.getWritableDatabase();//getting permission
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
            //100 % it will update
            int rowId = db.update(TABLE_NAME3,cv,Database.COL_31_ID+"= '"+id+"'",null);
            if(rowId!=1)//if update return 1 then data is updated else not updated
                return false;

            return  true;
        }catch (Exception e){
            e.printStackTrace();
            return false;
        }finally {
            if(db != null)
                db.close();
        }
    }
    public boolean insertPdf(String id,byte [] pdf,int whichPdf1or2){
        try {
            db = this.getWritableDatabase();//getting permission
            ContentValues cv = new ContentValues();//to enter data at once it is like hash map
            if(whichPdf1or2==1) {
                cv.put(COL_394_INVOICE1, pdf);//pdf1
            }else {//if whichPdf1or2==2
                cv.put(COL_395_INVOICE2, pdf);//pdf2
            }
            //100 % it will update return 1 if updated
             int rowId = db.update(TABLE_NAME3,cv,Database.COL_31_ID+"= '"+id+"'",null);
            // db.close();//closing db after operation performed
            if(rowId==1)//if update return 1 then data is updated else not updated
                return true;

            return  false;
        }catch (Exception e){
            e.printStackTrace();
            return false;
        }finally {
            if(db != null)
                db.close();
        }
    }
//    @SuppressLint("SuspiciousIndentation")
//    public boolean deleteRows(String id, String tableName){
//        try {
//            db = this.getWritableDatabase();//getting permission
//            int row = db.delete(tableName, "ID= '" + id + "'", null);
//            if (row > 0){//delete method return number of record deleted
//                return true;
//            }
//
//            return false;
//        }catch (Exception e){
//            e.printStackTrace();
//            return false;
//        }finally {
//            if(db != null)
//            db.close();
//        }
//    }
    public boolean transactionDeleteRows(String id, String tableName){
        SQLiteDatabase db = null;
        boolean success = false;
        try {
            db = this.getWritableDatabase();
            db.beginTransaction();

            int row = db.delete(tableName, "ID= '" + id + "'", null);
            if (row > 0){//delete method return number of record deleted
                success = true;
            }
        } catch (Exception e) {
            e.printStackTrace();
            success = false;
        }finally{
            if (db != null){
                if(success){//if success then only commit
                    db.setTransactionSuccessful();//If you want to commit the transaction there is a method setTransactionSuccessful() which will commit the values in the database.If you want to rollback your transaction then you need to endTransaction() without committing the transaction by setTransactionSuccessful().
                }
                db.endTransaction(); //Commit or rollback the transaction
                db.close();
            }
        }
        return success;
    }
}
