/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package virtualfat16;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectInputStream;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;
import java.io.RandomAccessFile;

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

    public static final char EOF = 0x1;

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
                root.writeChar(EOF);
                //vamos a enlazar chaval
                // para que la fat anterior apunte a la siguiente fat
                root.seek(FAT_REGION_START + (((int) previousCluster) * FAT_ENTRY_SIZE));
                root.write(decodedPosition.charAt(0));
                //reservamos en primera FAT
                root.seek(COPY_FAT_REGION_START + (i * FAT_ENTRY_SIZE));
                //reservamos copy fat
                root.writeChar(EOF);
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
        int nextCluster = (int) decodedPosition.charAt(0);
        if (nextCluster == EOF) {
            //??????
            return EOF;
        }
        return decodedPosition.charAt(0);
    }

    public void ReadDirEntry(int cluster, char clusterhead) throws IOException {

        int initialPosition = DATA_REGION_START + (cluster * CLUSTER_SIZE);
        root.seek(initialPosition);
        byte[] readData = new byte[32];
        for (int i = 0; i < DIR_ENTRY_MAX_FILES; i++) {
            int check = root.read(readData);
            if (check != 0) {
                try {
                    DirectoryEntry dirEntry = (DirectoryEntry) (convertFromBytes(readData));
                    if (dirEntry.getClusterHead() == clusterhead) {
                        System.out.println("Yay?!");
                    }
                } catch (Exception ex) {
                    System.out.println(ex);
                }
                root.seek(initialPosition + ((i + 1) * DIR_ENTRY_SIZE));
            } else {
                break;
            }
        }
    }

    private byte[] convertToBytes(Object object) throws IOException {
        try (ByteArrayOutputStream bos = new ByteArrayOutputStream();
                ObjectOutput out = new ObjectOutputStream(bos)) {
            out.writeObject(object);
            return bos.toByteArray();
        }
    }

    private Object convertFromBytes(byte[] bytes) throws IOException, ClassNotFoundException {
        try (ByteArrayInputStream bis = new ByteArrayInputStream(bytes);
                ObjectInput in = new ObjectInputStream(bis)) {
            return in.readObject();
        }
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
            currentPosition = getNextClusterPosition(currentPosition);
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
                root.write(bytesToWrite, pos, CLUSTER_SIZE);
                pos += CLUSTER_SIZE;
            } else {
                root.write(bytesToWrite, pos, bytesToWrite.length - pos);
                pos += bytesToWrite.length - pos;
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
            root.seek(DATA_REGION_START + (((int) myCluster) * CLUSTER_SIZE) + (freeDirEntry * DIR_ENTRY_SIZE));
            root.write(dirEntry.getByteRepresentation());
        }
        return true;
    }
}
