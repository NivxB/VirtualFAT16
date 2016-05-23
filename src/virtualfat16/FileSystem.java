/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package virtualfat16;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.LinkedList;
import java.util.List;

/**
 *
 * @author kbarahona,elco45
 */
public class FileSystem {

    public static final int FILE_SYSTEM_SIZE = 256 * 1024 * 1024;//bytes (256MB)

    public static final int FAT_ENTRY_MAX_AMOUNT = 65536;//entradas en tabla FAT
    public static final int CLUSTER_MAX_AMOUNT = 65536;

    public static final int DIR_ENTRY_SIZE = 32;//bytes
    public static final int FAT_ENTRY_SIZE = 2;//bytes

    public static final int FAT_TABLE_SIZE = 128 * 1024;//bytes - 128KB
    public static final int CLUSTER_SIZE = 4 * 1024;//bytes - 4KB
    public static final int ROOT_SIZE = 16 * 1024;//bytes - 16KB

    public static final int DIR_ENTRY_MAX_FILES = CLUSTER_SIZE / DIR_ENTRY_SIZE; // (4*1024)/32 = 128
    public static final int ROOT_ENTRY_MAX_FILES = ROOT_SIZE / DIR_ENTRY_SIZE;

    public static final int ROOT_REGION_START = 0; //comienza en el byte 0
    public static final int FAT_REGION_START = ROOT_REGION_START + ROOT_SIZE; //comienza en ese byte (ROOT_SIZE)
    public static final int COPY_FAT_REGION_START = FAT_REGION_START + FAT_TABLE_SIZE; //FAT_REGION + FAT_SIZE
    public static final int DATA_REGION_START = COPY_FAT_REGION_START + FAT_TABLE_SIZE;

    public static final char EOF = 0xFFFF;

    public static RandomAccessFile root;

    public FileSystem() throws FileNotFoundException, IOException {
        root = new RandomAccessFile("./VFAT.bin", "rw");
        if (root.length() == 0) {
            root.seek((FILE_SYSTEM_SIZE) - 1);
            root.write(1);
        }
    }

    public int findFreeEntryRoot() throws IOException {
        root.seek(ROOT_REGION_START);
        for (int i = 0; i < ROOT_ENTRY_MAX_FILES; i++) {
            int checkPos = root.read();
            if (checkPos == 0) {
                return i;
            } else {
                root.seek((i + 1) * DIR_ENTRY_SIZE);
            }
        }
        return -1;
    }

    public char findFreeCluster() throws IOException {
        root.seek(FAT_REGION_START);
        for (int i = 0; i < FAT_ENTRY_MAX_AMOUNT; i++) {
            byte[] onFAT = new byte[FAT_ENTRY_SIZE];
            // en la FAT hay 2 bytes
            root.read(onFAT);
            // tal vez cambiar el encoding? 
            // al correr vamos a ver que ondas
            String decodedPosition = new String(onFAT);
            int positionCheck = (int) decodedPosition.charAt(0);
            //Cuando no hay nada escrito en esa posicion el string es vacio
            //y el int es 0
            if (positionCheck == 0) {
                root.seek(root.getFilePointer() - FAT_ENTRY_SIZE);
                //retornamos los 2 bytes
                root.write(EOF);
                //reservamos en primera FAT
                root.seek(COPY_FAT_REGION_START + (i * FAT_ENTRY_SIZE));
                //reservamos copy fat
                root.write(EOF);
                //System.out.println(i);
                return (char) i;
            }
        }
        return (char) -1;
    }

    public char findFreeCluster(char previousCluster) throws IOException {
        root.seek(FAT_REGION_START);
        for (int i = 0; i < FAT_ENTRY_MAX_AMOUNT; i++) {
            byte[] onFAT = new byte[FAT_ENTRY_SIZE];
            // en la FAT hay 2 bytes
            root.read(onFAT);
            // tal vez cambiar el encoding? 
            // al correr vamos a ver que ondas
            String decodedPosition = new String(onFAT);
            int positionCheck = (int) decodedPosition.charAt(0);
            //Cuando no hay nada escrito en esa posicion el string es vacio
            //y el int es 0
            if (positionCheck == 0) {
                root.seek(root.getFilePointer() - FAT_ENTRY_SIZE);
                //retornamos los 2 bytes
                root.write(EOF);
                //vamos a enlazar chaval
                // para que la fat anterior apunte a la siguiente fat
                root.seek(FAT_REGION_START + (((int) previousCluster) * FAT_ENTRY_SIZE));
                root.write(decodedPosition.charAt(0));
                //reservamos en primera FAT
                root.seek(COPY_FAT_REGION_START + (i * FAT_ENTRY_SIZE));
                //reservamos copy fat
                root.write(EOF);
                root.seek(COPY_FAT_REGION_START + (((int) previousCluster) * FAT_ENTRY_SIZE));
                root.write(decodedPosition.charAt(0));
                return (char) i;
            }
        }
        return (char) -1;
    }

    public int findFreeEntryOnCluster(char cluster) throws IOException {
        int initialPosition = DATA_REGION_START + (((int) cluster) * CLUSTER_SIZE);
        root.seek(initialPosition);
        for (int i = 0; i < DIR_ENTRY_MAX_FILES; i++) {
            int check = root.read();
            if (check == 0) {
                return i;
            } else {
                root.seek(initialPosition + ((i + 1) * DIR_ENTRY_SIZE));
            }
        }
        return -1;
    }

    public char getNextClusterPosition(char cluster) throws IOException {
        root.seek(FAT_REGION_START + (((int) cluster) * CLUSTER_SIZE));
        byte[] nextPosition = new byte[2];
        root.read(nextPosition);
        String decodedPosition = new String(nextPosition);
        char nextCluster = decodedPosition.charAt(0);
        if (nextCluster == EOF || nextCluster == 0) {
            //??????
            return EOF;
        }
        return nextCluster;
    }

    public List<DirectoryEntry> readDirEntry(char cluster) throws IOException {
        List<DirectoryEntry> listEntry = new LinkedList<DirectoryEntry>();
        int initialPosition = DATA_REGION_START + (((int) cluster) * CLUSTER_SIZE);
        root.seek(initialPosition);
        byte[] readData = new byte[32];
        byte[] fecha = new byte[8];
        byte[] clusterHead = new byte[2];
        byte[] fileSize = new byte[4];
        int cont = 0;
        for (int i = 0; i < DIR_ENTRY_MAX_FILES; i++) {
            root.read(readData);
            String decodedPosition = new String(readData);
            int positionCheck = (int) decodedPosition.charAt(0);
            if (positionCheck != 0) {
                try {
                    cont = 0;
                    for (int j = 12; j <= 19; j++) {
                        fecha[cont] = readData[j];
                        cont++;
                    }
                    clusterHead[0] = readData[20];
                    clusterHead[1] = readData[21];
                    cont = 0;
                    for (int k = 22; k <= 25; k++) {
                        fileSize[cont] = readData[k];
                        cont++;
                    }
                    DirectoryEntry dirEntry = new DirectoryEntry(new String(readData, 1, 10), readData[11], byteToLong(fecha), byteToChar(clusterHead), byteToInt(fileSize));
                    dirEntry.setCurrentFilePosition(initialPosition + (i * DIR_ENTRY_SIZE));
                    listEntry.add(dirEntry);
                } catch (Exception ex) {
                    System.out.println(ex);
                }
                root.seek(initialPosition + ((i + 1) * DIR_ENTRY_SIZE));
            }
        }
        return listEntry;
    }

    public List<DirectoryEntry> readDirEntryRoot() throws IOException {
        List<DirectoryEntry> listEntry = new LinkedList<DirectoryEntry>();
        int initialPosition = ROOT_REGION_START;
        root.seek(initialPosition);
        byte[] readData = new byte[32];
        byte[] fecha = new byte[8];
        byte[] clusterHead = new byte[2];
        byte[] fileSize = new byte[4];
        int cont = 0;
        for (int i = 0; i < ROOT_ENTRY_MAX_FILES; i++) {
            root.read(readData);
            String decodedPosition = new String(readData);
            int positionCheck = (int) decodedPosition.charAt(0);
            if (positionCheck != 0) {
                try {
                    cont = 0;
                    for (int j = 12; j <= 19; j++) {
                        fecha[cont] = readData[j];
                        cont++;
                    }
                    clusterHead[0] = readData[20];
                    clusterHead[1] = readData[21];
                    cont = 0;
                    for (int k = 22; k <= 25; k++) {
                        fileSize[cont] = readData[k];
                        cont++;
                    }
                    DirectoryEntry dirEntry = new DirectoryEntry(new String(readData, 1, 10), readData[11], byteToLong(fecha), byteToChar(clusterHead), byteToInt(fileSize));
                    dirEntry.setCurrentFilePosition(initialPosition + (i * DIR_ENTRY_SIZE));
                    //System.out.println(dirEntry.getCurrentFilePosition());
                    listEntry.add(dirEntry);
                } catch (Exception ex) {
                    System.out.println(ex);
                }
                root.seek(initialPosition + ((i + 1) * DIR_ENTRY_SIZE));
            }
        }
        return listEntry;
    }

    public DirectoryEntry compareFileName(List<DirectoryEntry> listEntry, String filename) throws IOException {
        for (int i = 0; i < listEntry.size(); i++) {
            if (listEntry.get(i).getFileName().trim().equals(filename.trim())) {
                return listEntry.get(i);
            }
        }
        return null;
    }

    public String getData(DirectoryEntry dirEntry) throws IOException {
        StringBuilder retVal = new StringBuilder();
        if (dirEntry.getFileType() != DirectoryEntry.FILE) {
            return "ERROR";
        }
        char currentPosition = dirEntry.getClusterHead();
        do {
            byte[] readData = new byte[CLUSTER_SIZE];
            root.seek(DATA_REGION_START + (((int) currentPosition) * CLUSTER_SIZE));
            root.read(readData);
            retVal.append(new String(readData));
            //System.out.println(retVal.toString());
            currentPosition = getNextClusterPosition(currentPosition);
            //System.out.println(currentPosition);
        } while (currentPosition != EOF);

        return retVal.toString().trim();
    }

    public boolean writeData(DirectoryEntry dirEntry, String toWrite) throws IOException {
        if (dirEntry.getFileType() != DirectoryEntry.FILE) {
            return false;
        }
        int clustersNeeded = (int) Math.ceil((double) toWrite.length() / (double) CLUSTER_SIZE);
        int fileSize = toWrite.length();
        dirEntry.setFileSize(fileSize);
        root.seek(dirEntry.getCurrentFilePosition());
        root.write(dirEntry.getByteRepresentation());
        char clusterPosition = dirEntry.getClusterHead();
        int dataWrote = 0;
        byte[] bytesToWrite = toWrite.getBytes();
        for (int i = 0; i < clustersNeeded; i++) {
            root.seek(DATA_REGION_START + (((int) clusterPosition) * CLUSTER_SIZE));
            int pos = bytesToWrite.length - dataWrote;
            if (pos >= CLUSTER_SIZE) {
                root.write(bytesToWrite, dataWrote, CLUSTER_SIZE);
                dataWrote += CLUSTER_SIZE;
            } else {
                root.write(bytesToWrite, dataWrote, bytesToWrite.length - dataWrote);
                dataWrote += bytesToWrite.length - dataWrote;
            }
            char tmpNextCluster = getNextClusterPosition(clusterPosition);
            if (tmpNextCluster == EOF && (i + 1) < clustersNeeded) {
                tmpNextCluster = findFreeCluster(clusterPosition);
            }
            clusterPosition = tmpNextCluster;
        }
        return true;
    }

    public boolean writeDirEntry(DirectoryEntry dirEntry, char myCluster, boolean isRoot) throws IOException {
        if (isRoot) {
            int freeDirEntry = findFreeEntryRoot();
            if (freeDirEntry == -1) {
                return false;
            }
            root.seek(ROOT_REGION_START + (freeDirEntry * DIR_ENTRY_SIZE));
            root.write(dirEntry.getByteRepresentation());
        } else {
            int freeDirEntry = findFreeEntryOnCluster(myCluster);
            if (freeDirEntry == -1) {
                return false;
            }
           // System.out.println((int)myCluster + " parent Cluster");
           // System.out.println((int)dirEntry.getClusterHead() + " chid cluster");
            root.seek(DATA_REGION_START + (((int) myCluster) * CLUSTER_SIZE) + (freeDirEntry * DIR_ENTRY_SIZE));
            root.write(dirEntry.getByteRepresentation());
        }
        return true;
    }

    public DirectoryEntry getDirectoryEntryPath(String fullPath) throws IOException {
        String[] fileNames = fullPath.split("/");
        if (fileNames.length == 0) {
            return null;
        }
        DirectoryEntry retVal = null;
        for (int i = 1; i < fileNames.length; i++) {
            String fileName = fileNames[i];
            List<DirectoryEntry> searchList = null;
            if (i == 1) {
                searchList = readDirEntryRoot();
            } else {
                searchList = readDirEntry(retVal.getClusterHead());
            }
            if (searchList == null) {
                return null;
            }
            retVal = compareFileName(searchList, fileName);
            if (retVal == null) {
                return null;
            }
        }
        return retVal;
    }

    public void deleteDirEntry(DirectoryEntry dirEntry) throws IOException {
        /*if (dirEntry.getFileType() == DirectoryEntry.DIRECTORY) {
         List<DirectoryEntry> list = readDirEntry(dirEntry.getClusterHead());
         if (!list.isEmpty()) {
         System.err.println("Directory is not empty");
         return false;
         }
         }
         root.seek(dirEntry.getCurrentFilePosition());
         root.write((char) 0);*/
         byte[] zero = {0, 0};
        if (dirEntry.getFileType() == DirectoryEntry.FILE) {
            root.seek(dirEntry.getCurrentFilePosition());
            root.write(0);
            root.seek(DATA_REGION_START + ((int) dirEntry.getClusterHead() * CLUSTER_SIZE));
            root.write(0);
            char nextCluster = getNextClusterPosition(dirEntry.getClusterHead());
            while (nextCluster != EOF) {
                char tmpNext = getNextClusterPosition(nextCluster);
                root.seek(FAT_REGION_START + ((int) nextCluster * FAT_ENTRY_SIZE));
                root.write(0);
                root.seek(DATA_REGION_START + ((int) nextCluster * CLUSTER_SIZE));
            root.write(0);
                nextCluster = tmpNext;
            }
          //  System.out.println("deleting FAT " + (int) dirEntry.getClusterHead());
            root.seek(FAT_REGION_START + ((int) dirEntry.getClusterHead() * FAT_ENTRY_SIZE));
            root.write(zero);
        }
    }

    public void deleteOnlyDirEntry(DirectoryEntry dirEntry) throws IOException {
        if (dirEntry.getFileType() == DirectoryEntry.DIRECTORY) {
            List<DirectoryEntry> list = readDirEntry(dirEntry.getClusterHead());
            if (!list.isEmpty()) {
                System.err.println("Directory is not empty");
                return;
            }
            root.seek(dirEntry.getCurrentFilePosition());
            byte[] zero = {0, 0};
            root.write(zero);
            root.seek(DATA_REGION_START + ((int) dirEntry.getClusterHead() * CLUSTER_SIZE));
            root.write(0);
        }

    }

    public int byteToInt(byte[] byteBarray) {
        return ByteBuffer.wrap(byteBarray).order(ByteOrder.BIG_ENDIAN).getInt();
    }

    public long byteToLong(byte[] byteBarray) {
        return ByteBuffer.wrap(byteBarray).order(ByteOrder.BIG_ENDIAN).getLong();
    }

    public char byteToChar(byte[] byteBarray) {
        return ByteBuffer.wrap(byteBarray).order(ByteOrder.BIG_ENDIAN).getChar();
    }
}
