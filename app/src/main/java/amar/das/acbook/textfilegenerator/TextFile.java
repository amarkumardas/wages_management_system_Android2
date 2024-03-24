package amar.das.acbook.textfilegenerator;

import java.io.File;
import java.io.FileOutputStream;

import amar.das.acbook.utility.MyUtility;

public class TextFile {
    private StringBuilder text;
    public static String textFileAbsolutePathInDevice;
    //public static String textFileFolderName="acBookBackupInvoice";
//    public static String allDataTextFileName ="_all_data_";

    public TextFile (){
        this.text=new StringBuilder();
    }
    public boolean appendText(String text){
        try{
            this.text.append(text);
         return true;
        }catch (Exception x){
            x.printStackTrace();
            return false;
        }
    }
    public boolean createTextFile(String externalFileDir,String folderNameToSave,String fileName){
        //before calling this method read and write external storage permission should be taken
        try{
           if(MyUtility.isFolderExistIfNotExistCreateIt(externalFileDir,folderNameToSave)) {
               File filePathInDevice = new File(externalFileDir + "/" + folderNameToSave + "/" + fileName + ".txt");//path of text file where it is saved in device.here  folder  name should be same  so that file would be store in this folder name else it will give fileNotFoundException
               FileOutputStream outputStream = new FileOutputStream(filePathInDevice);
               outputStream.write(this.text.toString().getBytes());//writing to the file which is there in device
               outputStream.close();
               textFileAbsolutePathInDevice =filePathInDevice.getAbsolutePath();
               return true;
           }
           return false;
        }catch (Exception x){
            x.printStackTrace();
            return false;
        }
         /*What is getExternalFileDir() in android stdio
          getExternalFilesDir() is a method in Android Studio that returns the path to the directory on the primary external storage device where the our application can place persistent files it owns. These files are private to the application and are removed when the application is uninstalled.
          The method is used to access the external storage of an Android device, which is an area of the device's file system that can be accessed by the user and other applications. The external storage is typically used for storing user data that needs to persist even after the application is closed or the device is restarted.
          The getExternalFilesDir() method requires the WRITE_EXTERNAL_STORAGE permission to be granted in the application's manifest file. This permission allows the application to write to the external storage.
          The path returned by getExternalFilesDir() is specific to the application's package name, so different applications will have different directories on the external storage. The method returns null if the external storage is not currently available, such as when the device is connected to a computer and mounted as a USB storage device, or when the external storage is emulated on the device.*/
    }
}
