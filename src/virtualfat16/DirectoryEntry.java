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
    private char clusterHead=0xFFFF;
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

    public byte[] getByteRepresentation() {
        byte[] retVal = new byte[FileSystem.DIR_ENTRY_SIZE];
        retVal[0] = 1;
        for (int i = 0; i < 10; i++) {
            if (i < this.fileName.getBytes().length) {
                retVal[i + 1] = (this.fileName.getBytes())[i];
            } else {
                retVal[i + 1] = ' ';
            }
        }
        retVal[11] = fileType;
        byte[] tmpLong = longToByte(this.createdOn);
        System.arraycopy(tmpLong, 0, retVal, 12, 8);
        byte[] tmpCluster = charToByte(this.clusterHead);
        System.arraycopy(tmpCluster, 0, retVal, 20, 2);
        byte[] tmpInteger = intToByte(this.fileSize);
        System.arraycopy(tmpInteger, 0, retVal, 22, 4);
        return retVal;
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
    
    public byte [] charToByteArray( char c )
   {
      byte [] twoBytes = { (byte)(c & 0xff), (byte)(c >> 8 & 0xff) };
      return twoBytes;
   }
    
    

    @Override
    public String toString() {
        return "DirectoryEntry{" + "deleted=" + deleted + ", fileName=" + fileName + ", fileType=" + fileType + ", createdOn=" + createdOn + ", clusterHead=" + clusterHead + ", fileSize=" + fileSize + ", currentFilePosition=" + currentFilePosition + '}';
    }
    
    
}
