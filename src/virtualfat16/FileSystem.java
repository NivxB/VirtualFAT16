/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package virtualfat16;

import java.io.FileNotFoundException;
import java.io.IOException;
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

    public static final int FAT_TABLE_SIZE = 128 * 1024;//bytes
    public static final int CLUSTER_SIZE = 4 * 1024;//bytes - 4KB
    public static final int ROOT_SIZE = 16 * 1024;//bytes

    public static final int DIR_ENTRY_MAX_FILES = CLUSTER_SIZE / DIR_ENTRY_SIZE; // (4*1024)/32
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

    public int findFreeCluster() throws IOException {
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
                return i;
            }
        }
        return -1;
    }

    public int findFreeCluster(int previousCluster) throws IOException {
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
                root.seek(FAT_REGION_START +(previousCluster * FAT_ENTRY_SIZE));
                root.write(i);
                //reservamos en primera FAT
                root.seek(COPY_FAT_REGION_START + (i * FAT_ENTRY_SIZE));
                //reservamos copy fat
                root.write(EOF);
                root.seek(COPY_FAT_REGION_START +(previousCluster * FAT_ENTRY_SIZE));
                root.write(i);
                return i;
            }
        }
        return -1;
    }

    public int findFreeEntryOnCluster(int cluster) throws IOException{
        int initialPosition = DATA_REGION_START + (cluster * CLUSTER_SIZE);
        root.seek(initialPosition);
        for (int i = 0 ; i < DIR_ENTRY_MAX_FILES ; i++){
            int check = root.read();
            if (check == 0){
                return i;
            }else{
                root.seek(initialPosition + ((i+1) * DIR_ENTRY_SIZE));
            }
        }
        return -1;
    }
}
