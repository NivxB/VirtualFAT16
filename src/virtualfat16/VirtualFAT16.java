/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package virtualfat16;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author kbarahona
 */
public class VirtualFAT16 {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        try {
            FileSystem FS = new FileSystem();
        } catch (IOException ex) {
            Logger.getLogger(VirtualFAT16.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
}
