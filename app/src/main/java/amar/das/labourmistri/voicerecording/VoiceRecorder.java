package amar.das.labourmistri.voicerecording;

import android.media.MediaPlayer;
import android.media.MediaRecorder;

import java.io.File;

import amar.das.labourmistri.globalenum.GlobalConstants;
import amar.das.labourmistri.utility.MyUtility;

public class VoiceRecorder{
   private static MediaRecorder mediaRecorder;//it should be static
    private static MediaPlayer mediaPlayer;
    private  String audioAbsolutePath;
  public VoiceRecorder(String id,String getExternalFilesDir){
      audioAbsolutePath=getRecordingAbsolutePath(getExternalFilesDir,id);
      mediaRecorder=new MediaRecorder();
   }
   public boolean startRecording(){
           try{//to start mediaRecorder should be in try catch block
           mediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);//Sets the number of audio channels for recording.
           mediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);//Sets the format of the output file produced during recording
           mediaRecorder.setOutputFile(getAudioAbsolutePath());//giving file path where fill will be stored
           mediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
           mediaRecorder.setAudioChannels(1);//Sets the number of audio channels for recording here setting to 1.
               mediaRecorder.prepare();//first prepare then start
               mediaRecorder.start();
               return true;
           }catch (Exception e){
               e.printStackTrace();
               return false;
           }
   }
    public static void stopRecording() {
      try {
          if (mediaRecorder != null) {
              mediaRecorder.stop();
              mediaRecorder.release();
              mediaRecorder = null;//to know audio is saved successfully which was started
          }
      }catch (Exception x){
          x.printStackTrace();
      }
    }
    public static boolean audioPlayer(String audioAbsolutePath){
      if (audioAbsolutePath==null) {return false;}

           mediaPlayer = new MediaPlayer();
        try {
            mediaPlayer.setDataSource(audioAbsolutePath);//passing the path where this audio is saved to play
            mediaPlayer.prepare();
            mediaPlayer.start();
           return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
    public static void stopAudioPlayer(){
        if (mediaPlayer != null) {
            mediaPlayer.stop();
            mediaPlayer.release(); // release resources
            mediaPlayer = null; // set mediaPlayer to null
        }
    }
    public  String getAudioAbsolutePath(){//may return  null when audioAbsolutePath is null
       return audioAbsolutePath;
    }
    public MediaRecorder getMediaRecorder(){//to know recording is present
        return mediaRecorder;
    }
    private String getRecordingAbsolutePath(String getExternalFilesDir,String id){//return null if error
        if(MyUtility.isFolderExistIfNotExistCreateIt(getExternalFilesDir,File.separator+ GlobalConstants.AUDIO_FOLDER_NAME.getValue())) {//getExternalFilesDir(null) is a method in Android Studio that returns the path of the directory holding application files on external storage
            return new File(getExternalFilesDir+File.separator+GlobalConstants.AUDIO_FOLDER_NAME.getValue()+File.separator+MyUtility.generateUniqueFileNameByTakingDateTime(id,GlobalConstants.AUDIO_FILE_NAME.getValue()) +".mp3").getAbsolutePath();//path of audio where it is saved in device
        }else{
            return null;
        }
    }
}
