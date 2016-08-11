package model;

/**
 * Created by alexanderweiss on 13.04.16.
 * Helping class for detecting operating system
 */
public class OperatingSystem {

    public static String OS = System.getProperty("os.name").toLowerCase();

    public static boolean isWindows (){
        return (OS.contains("win"));
    }

    public static boolean isMac(){
        return (OS.contains("mac"));
    }



}
