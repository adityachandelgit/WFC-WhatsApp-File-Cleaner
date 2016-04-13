package com.retrospectivecreations.wfc;

/**
 * Created by Aditya on 25-02-2015.
 */
public class VideoProperties {

    String filePath;
    String cachePath;
    String fileSize;
    boolean isChecked;

    public VideoProperties(String filePath, String cachePath, String fileSize, boolean isChecked) {
        this.filePath = filePath;
        this.cachePath = cachePath;
        this.fileSize = fileSize;
        this.isChecked = isChecked;
    }

    public String getFilePath() {
        return filePath;
    }

    public String getCachePath() {
        return cachePath;
    }

    public String getFileSize() {
        return fileSize;
    }

    public boolean isChecked() {
        return isChecked;
    }

    public void setChecked(boolean isChecked) {
        this.isChecked = isChecked;
    }
}
