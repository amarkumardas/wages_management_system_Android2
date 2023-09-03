package amar.das.acbook.backgroundtask;

import android.content.Context;
import android.database.Cursor;

import amar.das.acbook.Database;
import amar.das.acbook.R;
import amar.das.acbook.utility.MyUtility;

public class MyBackgroundTask implements Runnable{
    private Context context;
    private String id;
    private byte taskIndicator;
    private boolean result;
    public MyBackgroundTask(Context context,String id,byte taskIndicator){
        this.context=context;
        this.id=id;
        this.taskIndicator=taskIndicator;
    }

    @Override
    public void run() {
//        if(taskIndicator==(byte)1){
//            System.out.println("----------------------makeIdActive");
//         result= makeIdActive(id);
//            System.out.println("------------result: "+result);
//        }else if(taskIndicator==(byte)2){
//           result= makeIdInActive(id);
//        }
    }

    public boolean getResult() {
        return result;
    }
//    public boolean makeIdInActive(String id){//here no need to edit latestdate becuase it will be already updated
//        /**if table is active then only data is shifted and make id inactive*/
//        Database db=new Database(context);
//        boolean updateLatestDate = true;
//        String activeInactiveSkill[] = db.getActiveOrInactiveAndSkill(id);
//        try {
//            if (activeInactiveSkill[0].equals("1")){//if person is active then insert data from active table to inactive table then make id inactive
//
//                if (activeInactiveSkill[1].equals(context.getResources().getString(R.string.laber)) || activeInactiveSkill[1].equals(context.getResources().getString(R.string.women_laber))) {//check for l OR G
//
//                    Cursor dataFromActiveTableCursor = db.getWagesDepositDataForRecyclerView(id);//data from active table
//
//                    if (!db.updateTable("UPDATE " + Database.TABLE_NAME1 + " SET " + Database.COL_12_ACTIVE + "='" + 0 + "' WHERE " + Database.COL_1_ID + "='" + id + "'")) {//this line should be before while loop otherwise wrong output.Updating making id inactive so  that from active table data inserted in inactive table because to insert in inactive table id should be inactive
//                        updateLatestDate = false;
//                        return false;
//                    }
//
//                    if (dataFromActiveTableCursor != null) {
//                        while (dataFromActiveTableCursor.moveToNext()) {
//                            if (!db.insertWagesOrDepositOnlyToInActiveTableTransaction(dataFromActiveTableCursor.getString(9), dataFromActiveTableCursor.getString(0), dataFromActiveTableCursor.getString(10), dataFromActiveTableCursor.getString(1), dataFromActiveTableCursor.getString(2), dataFromActiveTableCursor.getInt(3), dataFromActiveTableCursor.getInt(5), dataFromActiveTableCursor.getInt(6), dataFromActiveTableCursor.getInt(7), dataFromActiveTableCursor.getInt(8), dataFromActiveTableCursor.getInt(4), dataFromActiveTableCursor.getString(11))) {
//                                updateLatestDate = false;//revert the update active to inactive
//                                db.deleteAllRowsTransaction(id, Database.TABLE3_IN_ACTIVE_LG); //deleting data from active table. if in half way not inserted then delete all data from INactive table because data is not fully inserted in inactive table
//                                return false;
//                            }
//                        }
//                        if (updateLatestDate && !db.deleteAllRowsTransaction(id, Database.TABLE1_ACTIVE_LG)) {//**after while loop executed successfully then only delete data from active table Because data is shifted in INactive table successfully
//                            updateLatestDate = false;
//                            db.deleteAllRowsTransaction(id, Database.TABLE3_IN_ACTIVE_LG);//since data is inserted in active table then delete data from active table
//                            return false;
//                        }
//                    }
//                }else if (activeInactiveSkill[1].equals(context.getResources().getString(R.string.mestre))) {//check for mestre
//
//                    Cursor dataFromActiveTableCursor = db.getWagesDepositDataForRecyclerView(id);
//
//                    if (!db.updateTable("UPDATE " + Database.TABLE_NAME1 + " SET " + Database.COL_12_ACTIVE + "='" + 0 + "' WHERE " + Database.COL_1_ID + "='" + id + "'")) {//this line should be before while loop otherwise wrong output.Updating making id active so  that from inactive table data inserted in active table because to insert in active table id should be active
//                        updateLatestDate = false;
//                        return false;
//                    }
//                    if (dataFromActiveTableCursor != null) {
//
//                        while (dataFromActiveTableCursor.moveToNext()) {
//                            if (!db.insertWagesOrDepositOnlyToInActiveTableTransaction(dataFromActiveTableCursor.getString(9), dataFromActiveTableCursor.getString(0), dataFromActiveTableCursor.getString(10), dataFromActiveTableCursor.getString(1), dataFromActiveTableCursor.getString(2), dataFromActiveTableCursor.getInt(3), dataFromActiveTableCursor.getInt(5), dataFromActiveTableCursor.getInt(6), dataFromActiveTableCursor.getInt(7), dataFromActiveTableCursor.getInt(8), dataFromActiveTableCursor.getInt(4), dataFromActiveTableCursor.getString(11))) {
//                                updateLatestDate = false;//revert the update active to inactive
//                                db.deleteAllRowsTransaction(id, Database.TABLE2_IN_ACTIVE_MESTRE); //deleting data from active table. if in half way not inserted then delete all data from active table because data is not fully inserted in active table
//                                return false;
//                            }
//                        }
//                        if (updateLatestDate && !db.deleteAllRowsTransaction(id, Database.TABLE0_ACTIVE_MESTRE)) {//**after while loop executed successfully then only delete data from active table Because data is shifted in inactive table successfully
//                            updateLatestDate = false;
//                            db.deleteAllRowsTransaction(id, Database.TABLE2_IN_ACTIVE_MESTRE);//since data is inserted in INactive table then delete data from active table
//                            return false;
//                        }
//                    }
//                }
//            }
//        }catch(Exception x) {
//            x.printStackTrace();
//            return false;
//        } finally {
//            //MANUAlly you cant make id inactive.if want to make then latest date should be taken 1 month back
//            if (!updateLatestDate) {
//                db.updateTable("UPDATE " + Database.TABLE_NAME1 + " SET " + Database.COL_12_ACTIVE + "='" + 1 + "' WHERE " + Database.COL_1_ID + "='" + id + "'");
//
//                // updateTable("UPDATE " + Database.TABLE_NAME1 + " SET " + Database.COL_15_LATESTDATE + "='" + MyUtility.getOnlyCurrentDate() + "' , " + Database.COL_16_TIME + "='" + MyUtility.getOnlyTime() + "' WHERE " + Database.COL_1_ID + "='" + id + "'");
//            } else {
//                //updateTable("UPDATE " + Database.TABLE_NAME1 + " SET " + Database.COL_12_ACTIVE + "='" + 1 + "' WHERE " + Database.COL_1_ID + "='" + id + "'");
//            }
//
//        }
//        return true;
//    }
//    public boolean makeIdActive(String id) {//to make it active updating latestDate is compulsory
//        /**if table is inactive then only data is shifted and make id active*/
//        Database db=new Database(context);
//        boolean updateLatestDate = true;
//        String activeInactiveSkill[] = db.getActiveOrInactiveAndSkill(id);
//        System.out.println(activeInactiveSkill[0]+" ------------------------------ "+activeInactiveSkill[1]);
//        try {
//            if (activeInactiveSkill[0].equals("0")){//if person is inactive then insert data from inactive table to active table
//
//                if (activeInactiveSkill[1].equals(context.getResources().getString(R.string.laber)) || activeInactiveSkill[1].equals(context.getResources().getString(R.string.women_laber))) {//check for l OR G
//
//                    Cursor dataFromInActiveTableCursor = db.getWagesDepositDataForRecyclerView(id);
//
//                    if (!db.updateTable("UPDATE " + Database.TABLE_NAME1 + " SET " + Database.COL_12_ACTIVE + "='" + 1 + "' WHERE " + Database.COL_1_ID + "='" + id + "'")) {//this line should be before while loop otherwise wrong output.Updating making id active so  that from inactive table data inserted in active table because to insert in active table id should be active
//                        updateLatestDate = false;
//                        return false;
//                    }
//
//                    if (dataFromInActiveTableCursor != null) {
//                        while (dataFromInActiveTableCursor.moveToNext()) {
//                            if (!db.insertWagesOrDepositOnlyToActiveTableTransaction(dataFromInActiveTableCursor.getString(9), dataFromInActiveTableCursor.getString(0), dataFromInActiveTableCursor.getString(10), dataFromInActiveTableCursor.getString(1), dataFromInActiveTableCursor.getString(2), dataFromInActiveTableCursor.getInt(3), dataFromInActiveTableCursor.getInt(5), dataFromInActiveTableCursor.getInt(6), dataFromInActiveTableCursor.getInt(7), dataFromInActiveTableCursor.getInt(8), dataFromInActiveTableCursor.getInt(4), dataFromInActiveTableCursor.getString(11))) {
//                                updateLatestDate = false;//revert the update active to inactive
//                                db.deleteAllRowsTransaction(id, Database.TABLE1_ACTIVE_LG); //deleting data from active table. if in half way not inserted then delete all data from active table because data is not fully inserted in active table
//                                return false;
//                            }
//                        }
//                        if (updateLatestDate && !db.deleteAllRowsTransaction(id, Database.TABLE3_IN_ACTIVE_LG)) {//**after while loop executed successfully then only delete data from inactive table Because data is shifted in active table successfully
//                            updateLatestDate = false;
//                            db.deleteAllRowsTransaction(id, Database.TABLE1_ACTIVE_LG);//since data is inserted in active table then delete data from active table
//                            return false;
//                        }
//                    }
//                }else if (activeInactiveSkill[1].equals(context.getResources().getString(R.string.mestre))) {//check for mestre
//
//                    Cursor dataFromInActiveTableCursor = db.getWagesDepositDataForRecyclerView(id);
//                    System.out.println("-----------------data count: "+dataFromInActiveTableCursor.getCount());
//                    if (!db.updateTable("UPDATE " + Database.TABLE_NAME1 + " SET " + Database.COL_12_ACTIVE + "='" + 1 + "' WHERE " + Database.COL_1_ID + "='" + id + "'")) {//this line should be before while loop otherwise wrong output.Updating making id active so  that from inactive table data inserted in active table because to insert in active table id should be active
//                        updateLatestDate = false;
//                        return false;
//                    }
//                    activeInactiveSkill = db.getActiveOrInactiveAndSkill(id);
//                    System.out.println("-------------make id active: "+activeInactiveSkill[0]);
//                    if (dataFromInActiveTableCursor != null) {
//
//                        while (dataFromInActiveTableCursor.moveToNext()) {
//                            if (!db.insertWagesOrDepositOnlyToActiveTableTransaction(dataFromInActiveTableCursor.getString(9), dataFromInActiveTableCursor.getString(0), dataFromInActiveTableCursor.getString(10), dataFromInActiveTableCursor.getString(1), dataFromInActiveTableCursor.getString(2), dataFromInActiveTableCursor.getInt(3), dataFromInActiveTableCursor.getInt(5), dataFromInActiveTableCursor.getInt(6), dataFromInActiveTableCursor.getInt(7), dataFromInActiveTableCursor.getInt(8), dataFromInActiveTableCursor.getInt(4), dataFromInActiveTableCursor.getString(11))) {
//                                updateLatestDate = false;//revert the update active to inactive
//                                db.deleteAllRowsTransaction(id, Database.TABLE0_ACTIVE_MESTRE); //deleting data from active table. if in half way not inserted then delete all data from active table because data is not fully inserted in active table
//                                System.out.println("----------loop cond deleted all active mestre ");
//                                return false;
//                            }
//                        }
//                        if (updateLatestDate && !db.deleteAllRowsTransaction(id, Database.TABLE2_IN_ACTIVE_MESTRE)) {//**after while loop executed successfully then only delete data from inactive table Because data is shifted in active table successfully
//                            updateLatestDate = false;
//                            db.deleteAllRowsTransaction(id, Database.TABLE0_ACTIVE_MESTRE);//since data is inserted in active table then delete data from active table
//                            System.out.println("----------if deleted all active mestre ");
//                            return false;
//                        }
//                    }
//                }
//            }
//        }catch(Exception x) {
//            x.printStackTrace();
//            return false;
//        } finally {
//            // if(activeInactiveSkill[0].equals("0")) {//if inactive then only update
//            if (updateLatestDate) {
//                db.updateTable("UPDATE " + Database.TABLE_NAME1 + " SET " + Database.COL_15_LATESTDATE + "='" + MyUtility.getOnlyCurrentDate() + "' , " + Database.COL_16_TIME + "='" + MyUtility.getOnlyTime() + "' WHERE " + Database.COL_1_ID + "='" + id + "'");
//                System.out.println("----------updated");
//            } else {
//                System.out.println("------------------------revert to 0");
//                db.updateTable("UPDATE " + Database.TABLE_NAME1 + " SET " + Database.COL_12_ACTIVE + "='" + 0 + "' WHERE " + Database.COL_1_ID + "='" + id + "'");
//            }
//            if (db != null){
//                db.close();
//            }
//            // }
//
//        }
//        return true;
//    }
}
//if we access static variable from main thread in new thread then will main thread get blocked in android stdio
// In Android Studio (and generally in Java or Android development), accessing a static variable from a new
// thread will not block the main thread itself. However, you need to be cautious about concurrent access to
// the static variable when multiple threads are involved.
//
// When you access a static variable from a new thread, the main thread will continue its execution independently.
// The main thread will not wait for the new thread to complete its task. This is the essence of multithreading,
// where multiple threads can run concurrently, allowing for better performance and responsiveness in applications.
//
// However, you must be aware of potential concurrency issues that may arise when multiple threads access and
// modify the same static variable simultaneously. If not handled properly, this can lead to data corruption
// or unpredictable behavior.