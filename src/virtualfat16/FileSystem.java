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
    public static final int DIR_ENTRY_MAX_AMOUNT = 512;//entradas
    
    public static final int DIR_ENTRY_SIZE = 32;//bytes
    public static final int FAT_ENTRY_SIZE = 2;//bytes
    
    public static final int FAT_TABLE_SIZE = 128 * 1024;//bytes
    public static final int CLUSTER_SIZE = 4 * 1024;//bytes - 4KB
    public static final int ROOT_SIZE = 16 * 1024;//bytes
    
    public static RandomAccessFile root;
        public FileSystem() throws FileNotFoundException, IOException {
        root = new RandomAccessFile("./VFAT.bin", "rw");
        if(root.length()==0){
            root.seek((FILE_SYSTEM_SIZE) - 1);
            root.write(1);
        }
    }
    
  }
