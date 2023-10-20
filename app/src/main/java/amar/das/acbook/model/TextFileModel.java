package amar.das.acbook.model;
import java.io.Serializable;

public class TextFileModel implements Serializable {
    public String getAbsolutePath() {
        return absolutePath;
    }
    public void setAbsolutePath(String absolutePath){
        this.absolutePath = absolutePath;
    }
    private String absolutePath;
    private String fileName;

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }
}
