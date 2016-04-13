package com.retrospectivecreations.wfc;

/**
 * Created by Aditya on 24-02-2015.
 */
public class AudioProperties {

    String fileName;
    String LastModified;
    String fileSize;
    String filePath;

    public AudioProperties(String fileName, String lastModified, String fileSize, String filePath) {
        this.fileName = fileName;
        LastModified = lastModified;
        this.fileSize = fileSize;
        this.filePath = filePath;
    }

    public String getFileName() {
        return fileName;
    }

    public String getLastModified() {
        return LastModified;
    }

    public String getFileSize() {
        return fileSize;
    }

    public String getFilePath() {
        return filePath;
    }


}
