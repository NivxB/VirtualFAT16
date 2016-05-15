/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package virtualfat16;

import java.io.IOException;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author kbarahona,elco45
 */
public class VirtualFAT16 {

    /**
     * @param args the command line arguments
     */
    static final String[] commands = {"mkdir", "cd", "rmdir", "rm", "cat", "ls", "exit"};

    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);
        String command = "";
        String[] comando={};
        int num_command = 0;
        try {
            FileSystem FS = new FileSystem();
            while (true) {
                System.out.print("mi_sh>>");
                command = sc.nextLine();
                comando=splitter(command);
                num_command = verifyCommands(comando);
                if (num_command == -1) {//error
                    System.out.println("Comando invalido!");
                } else if (num_command == 0) {//mkdir
                    mkdir(comando);
                } else if (num_command == 1) {//cd
                    cd(comando);
                } else if (num_command == 2) {//rmdir
                    rmdir(comando);
                } else if (num_command == 3) {//rm
                    rm(comando);
                } else if (num_command == 4) {//cat
                    cat(comando);
                } else if (num_command == 5) {//ls
                    ls(comando);
                } else {//exit
                    break;
                }
            }
        } catch (IOException ex) {
            Logger.getLogger(VirtualFAT16.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public static String[] splitter(String command) {
        String[] comandos = command.split(" ");
        return comandos;
    }

    public static int verifyCommands(String[] command) {
        for (int i = 0; i < commands.length; i++) {
            if (command[0].equals(commands[i])) {
                return i;
            }
        }
        return -1;
    }

    public static void mkdir(String[] command) {
        if (command.length==2) {
            //crear pinche archivo/directorio
        }else{
            System.out.println("Valor invalido!");
        }
    }

    public static void cd(String[] command) {
        if (command.length==2) {
            //si es un directorio entonces
                //si existe ese archivo en el directorio actual entonces

                //si no, entonces tirar error
            
            //si no es un directorio tirar error
        }else{
            System.out.println("Valor invalido!");
        }
    }

    public static void rmdir(String[] command) {
        if (command.length==2) {
            //si es un directorio entonces
                //si existe ese archivo en el directorio actual entonces

                //si no, entonces tirar error
            
            //si no es un directorio tirar error
        }else{
            System.out.println("Valor invalido!");
        }
    }

    public static void rm(String[] command) {
        if (command.length==2) {
            //si existe ese archivo en el directorio actual entonces
                
            //si no, entonces tirar error
        }else{
            System.out.println("Valor invalido!");
        }
    }

    public static void cat(String[] command) {
        if (command[1].equals(">")) {
            if (command.length==3) {//cat >
                //si existe ese archivo en el directorio actual entonces
                
                //si no, hay que crearlo primero
            }else{
                System.out.println("Falta el nombre del archivo!");
            }
        }else{
            if (command.length==2) {//cat normal
                //si existe ese archivo en el directorio actual entonces
                
                //si no, entonces tirar error
            }else{
                System.out.println("Falta el nombre del archivo!");
            }
        }
    }

    public static void ls(String[] command) {
        if (command.length==2) {
            if (command[1].equals("-l")) {
            
            }else{
                System.out.println("Pruebe ls -l");
            }
        }else{
            System.out.println("Valor invalido!");
        }
        
    }
}
