package bdagent.util;


import java.io.*;

public class Version {
    //public static final String VERSION = "V 0.9.00";
    public static String getVersion(){
        String path = ProgramPathHelp.getProgramPath() + File.separator + "VERSION";
        System.out.println("Version file: " + path);
        try {
            BufferedReader reader = new BufferedReader(new FileReader(path));
            String version = reader.readLine();
            reader.close();
            return version;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return "UNKNOWN";


    }
}
