/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package virtualfat16;

import java.io.Serializable;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

/**
 *
 * @author kbarahona,elco45
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
        this.fileSize = fileSize;
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

    public byte[] intToByte(int myInteger) {
        return ByteBuffer.allocate(4).order(ByteOrder.BIG_ENDIAN).putInt(myInteger).array();
    }

    public int byteToInt(byte[] byteBarray) {
        return ByteBuffer.wrap(byteBarray).order(ByteOrder.BIG_ENDIAN).getInt();
    }
    
    public byte[] longToByte(long myLong) { //dick
        return ByteBuffer.allocate(8).order(ByteOrder.BIG_ENDIAN).putLong(myLong).array();
    }

    public long byteToLong(byte[] byteBarray) {
        return ByteBuffer.wrap(byteBarray).order(ByteOrder.BIG_ENDIAN).getLong();
    }
    
    public byte[] charToByte(char myChar) {
        return ByteBuffer.allocate(2).order(ByteOrder.BIG_ENDIAN).putChar(myChar).array();
    }

    public char byteToChar(byte[] byteBarray) {
        return ByteBuffer.wrap(byteBarray).order(ByteOrder.BIG_ENDIAN).getChar();
    }
}
