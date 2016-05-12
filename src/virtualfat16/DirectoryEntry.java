/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package virtualfat16;

/**
 *
 * @author kbarahona
 */
public class DirectoryEntry {
    public static final byte DIRECTORY = 0x0010;
    public static final byte FILE = 0x0020;
    public byte deleted;
    private String fileName;
    private byte fileType;
    private long createdOn;
    private char clusterHead;
    private int fileSize;
    private long currentFilePosition;

    public DirectoryEntry(String fileName, byte fileType, long createdOn, char clusterHead, int fileSize) {
        this.fileName = fileName;
        this.fileType = fileType;
        this.createdOn = createdOn;
        this.clusterHead = clusterHead;
        this.fileSize = fileSize;
        this.currentFilePosition = 0;
        this.deleted = 0x1;
    }
    
    public DirectoryEntry(String fileName, byte fileType, long createdOn, char clusterHead, int fileSize, byte deleted) {
        this.fileName = fileName;
        this.fileType = fileType;
        this.createdOn = createdOn;
        this.clusterHead = clusterHead;
        this.fileSize = fileSize;
        this.currentFilePosition = 0;
        this.deleted = deleted;
    }
    
    public int getFileSize() {
        return fileSize;
    }
    
    public void setFileSize(int fileSize) {
        this.fileSize =  fileSize;
    }

    public char getClusterHead() {
        return clusterHead;
    }

    public void setClusterHead(char clusterHead) {
        this.clusterHead = clusterHead;
    }

    public long getCreatedOn() {
        return createdOn;
    }

    public byte getFileType() {
        return fileType;
    }

    public String getFileName() {
        return fileName;
    }

    public long getCurrentFilePosition() {
        return currentFilePosition;
    }

    public void setCurrentFilePosition(long currentFilePosition) {
        this.currentFilePosition = currentFilePosition;
    }
}
