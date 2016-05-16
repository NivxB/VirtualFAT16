/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package virtualfat16;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
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
    static String actualDir = "/";
    static DirectoryEntry actualDirEntry = null;
    static FileSystem FS;

    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);
        String command = "";
        String[] comando = {};
        int num_command = 0;
        try {
            FS = new FileSystem();
            while (true) {
                System.out.print("[root@localhost " + actualDir + "] $ ");
                command = sc.nextLine();
                comando = splitter(command);
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
        for (int i = 0; i < comandos.length; i++) {
            comandos[i] = comandos[i].replaceAll(" ", "");
        }
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
        /*
         Si estamos en el root las operaciones se hacen sobre el root, no sobre un cluster.
         Si estamos en otro directorio las operaciones se han en el cluster de ese directorio
         */
        if (command.length == 2) {
            if (actualDirEntry == null) {//dentro de root
                String[] filename = command[1].split("\\.");
                try {
                    boolean existe = false;//para saber si se ha usado el nombre del directorio anteriormente
                    //el root no necesita el clusterHead, ya que solamente escribe en su propia onda. 
                    List<DirectoryEntry> lista = FS.readDirEntryRoot();
                    //como estamos en root tenemos que leer las dirEntry en root no en el clusterHead
                    //el clusterHead apunta a los directorios hijos. 
                    for (int i = 0; i < lista.size(); i++) {
                        if (lista.get(i).getFileName().replaceAll(" ", "").equals(command[1])) {
                            existe = true;
                            break;
                        }
                    }
                    if (filename.length == 1) {//directorio
                        if (!existe) {
                            DirectoryEntry dirEntry = new DirectoryEntry(command[1], DirectoryEntry.DIRECTORY, new java.util.Date().getTime(), FS.findFreeCluster(), 0);
                            FS.writeDirEntry(dirEntry, '0', true);
                        } else {
                            System.out.println("Ya existe un directorio con ese nombre!");
                        }
                    } else if (filename.length == 2) {//archivo
                        if (!existe) {
                            DirectoryEntry dirEntry = new DirectoryEntry(command[1], DirectoryEntry.FILE, new java.util.Date().getTime(), FS.findFreeCluster(), 0);
                            FS.writeDirEntry(dirEntry, '0', true);
                        } else {
                            System.out.println("Ya existe un archivo con ese nombre!");
                        }
                    } else {
                        System.out.println("Error, operación no válida!");
                    }
                } catch (Exception ex) {
                    System.out.println(ex);
                }
            } else {//fuera de root
                String[] filename = command[1].split("\\.");
                try {
                    boolean existe = false;//para saber si se ha usado el nombre del directorio anteriormente
                    //el root no necesita el clusterHead, ya que solamente escribe en su propia onda. 
                    List<DirectoryEntry> lista = FS.readDirEntry(actualDirEntry.getClusterHead());
                    //como estamos en root tenemos que leer las dirEntry en root no en el clusterHead
                    //el clusterHead apunta a los directorios hijos. 
                    for (int i = 0; i < lista.size(); i++) {
                        if (lista.get(i).getFileName().replaceAll(" ", "").equals(command[1])) {
                            existe = true;
                            break;
                        }
                    }
                    if (filename.length == 1) {//directorio
                        if (!existe) {
                            DirectoryEntry dirEntry = new DirectoryEntry(command[1], DirectoryEntry.DIRECTORY, new java.util.Date().getTime(), FS.findFreeCluster(), 0);
                            FS.writeDirEntry(dirEntry, actualDirEntry.getClusterHead(), false);
                        } else {
                            System.out.println("Ya existe un directorio con ese nombre!");
                        }
                    } else if (filename.length == 2) {//archivo
                        if (!existe) {
                            DirectoryEntry dirEntry = new DirectoryEntry(command[1], DirectoryEntry.FILE, new java.util.Date().getTime(), FS.findFreeCluster(), 0);
                            FS.writeDirEntry(dirEntry, actualDirEntry.getClusterHead(), false);
                        } else {
                            System.out.println("Ya existe un archivo con ese nombre!");
                        }
                    } else {
                        System.out.println("Error, operación no válida!");
                    }
                } catch (Exception ex) {
                    System.out.println(ex);
                }
            }
        } else {
            System.out.println("Valor invalido!");
        }
    }

    public static void cd(String[] command) {
        boolean movingToRoot = false;
        if (command.length == 2) {

            String[] path = command[1].split("/");

            try {
                DirectoryEntry tmpActual = actualDirEntry;
                String tmpStringActual = actualDir;
                if (command[1].charAt(0) == '/'){
                    DirectoryEntry nextDir = FS.getDirectoryEntryPath(command[1]);
                    if (nextDir != null){
                        actualDir = command[1];
                        actualDirEntry = nextDir;
                    }
                    return;
                }

                for (int i = 0; i < path.length; i++) {
                    String movingToDirName = path[i];
                    DirectoryEntry nextDir = null;
                    List<DirectoryEntry> lista = null;
                    if (movingToDirName.trim().equals("..")) {
                        String checkVal = tmpStringActual;
                        if (checkVal.split("/").length == 0) {
                            System.err.println("No se puede ir antes de root");
                            break;
                        } else {
                            String rutaActual = "";
                            for (int j = 0; j < checkVal.split("/").length - 1; j++) {
                                if (j == 1) {
                                    rutaActual = "";
                                }
                                rutaActual += "/" + checkVal.split("/")[j];
                            }
                            if (rutaActual.split("/").length == 0) {
                                nextDir = null;
                                tmpActual = null;
                                actualDir = "/";
                                tmpStringActual = rutaActual;
                                movingToRoot = true;
                                break;
                            }
                            nextDir = FS.getDirectoryEntryPath(rutaActual);
                            tmpStringActual = "/";
                            movingToDirName = rutaActual;
                        }
                    } else {
                        if (tmpActual == null) {
                            lista = FS.readDirEntryRoot();
                        } else {
                            lista = FS.readDirEntry(tmpActual.getClusterHead());
                        }
                        nextDir = FS.compareFileName(lista, movingToDirName);

                    }
                    if (nextDir == null) {
                        tmpActual = null;
                        break;
                    } else {
                        if (tmpStringActual.split("/").length == 0) {
                            if (movingToDirName.charAt(0) == '/') {
                                tmpStringActual = movingToDirName;
                            } else {
                                tmpStringActual = "/" + movingToDirName;
                            }
                        } else {
                            tmpStringActual += "/" + movingToDirName;
                        }
                        if (nextDir.getFileType() == DirectoryEntry.FILE) {
                            tmpActual = null;
                            System.err.println(movingToDirName + " es un archivo");
                            break;
                        }
                        tmpActual = nextDir;

                    }
                }
                if (tmpActual != null || movingToRoot) {
                    actualDirEntry = tmpActual;
                    actualDir = tmpStringActual;

                } else {
                    System.err.println("El directorio no existe");
                }

            } catch (Exception ex) {
                System.out.println(ex);
            }

        } else {
            System.out.println("Valor invalido!");
        }
    }

    public static void rmdir(String[] command) {
        if (command.length == 2) {
            //si es un directorio entonces
            //si existe ese archivo en el directorio actual entonces

            //si no, entonces tirar error
            //si no es un directorio tirar error
        } else {
            System.out.println("Valor invalido!");
        }
    }

    public static void rm(String[] command) {
        if (command.length == 2) {
            //si existe ese archivo en el directorio actual entonces

            //si no, entonces tirar error
        } else {
            System.out.println("Valor invalido!");
        }
    }

    public static void cat(String[] command) {
        if (command[1].equals(">")) {
            if (command.length == 3) {//cat >
                //si existe ese archivo en el directorio actual entonces

                //si no, hay que crearlo primero
            } else {
                System.out.println("Falta el nombre del archivo!");
            }
        } else if (command.length == 2) {//cat normal
            //si existe ese archivo en el directorio actual entonces

            //si no, entonces tirar error
        } else {
            System.out.println("Falta el nombre del archivo!");
        }
    }

    public static void ls(String[] command) {
        if (command.length == 2) {
            if (command[1].equals("-l")) {
                SimpleDateFormat df2 = new SimpleDateFormat("EEE, d MMM yyyy HH:mm:ss");

                try {
                    List<DirectoryEntry> lista;
                    if (actualDirEntry == null) {//dentro de root
                        lista = FS.readDirEntryRoot();
                    } else {
                        lista = FS.readDirEntry(actualDirEntry.getClusterHead());
                    }
                    System.out.printf("%-30.30s %-30.30s %-30.30s %-30.30s%n", "Filename", "FileType", "DateCreated", "FileSize");
                    for (int i = 0; i < lista.size(); i++) {
                        String type = "Directory";
                        if (lista.get(i).getFileType() == DirectoryEntry.FILE) {
                            type = "File";
                        }
                        System.out.printf("%-30.30s %-30.30s %-30.30s %-30.30s%n", lista.get(i).getFileName(), type, df2.format(new Date(lista.get(i).getCreatedOn())), lista.get(i).getFileSize());
                    }
                } catch (Exception ex) {

                }

            } else {
                System.out.println("Pruebe ls -l");
            }
        } else {
            System.out.println("Valor invalido!");
        }

    }
}
