package edu.hm.cs.ig.passbutler.data;

import java.util.Date;

/**
 * Created by dennis on 10.12.17.
 */

public class FileMetaData {

    String filePath;
    Date lastModified;
    String fileHash;

    public FileMetaData(String filePath, Date lastModified, String fileHash) {
        this.filePath = filePath;
        this.lastModified = lastModified;
        this.fileHash = fileHash;
    }

    public String getFilePath() {
        return filePath;
    }

    public Date getLastModified() {
        return lastModified;
    }

    public String getFileHash() {
        return fileHash;
    }
}
