package game;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

class ZipUtils {

    /*
     * From http://www.java2s.com/Code/Java/File-Input-Output/Makingazipfileofdirectoryincludingitssubdirectoriesrecursively.htm
     */
    static void zipUserLevels(String zipFileName) throws Exception {

        File exportFolder = new File(System.getProperty("user.home") + "/.EistReturns/export/");
        File dirObj = new File(System.getProperty("user.home") + "/.EistReturns/levels");
        if(exportFolder.mkdir()){
            System.out.println("Export folder created");
        }

        ZipOutputStream out = new ZipOutputStream(new FileOutputStream(exportFolder.toString() + "/" + zipFileName));
        System.out.println("Creating zip: " + zipFileName);
        addDir(dirObj, out);
        out.close();
    }

    private static void addDir(File dirObj, ZipOutputStream out) throws IOException {

        //File[] files = Utils.userLevelDirs();
        File[] files = dirObj.listFiles();
        byte[] tmpBuf = new byte[1024];

        for (int i = 0; i < files.length; i++) {
            if (files[i].isDirectory()) {
                addDir(files[i], out);
                continue;
            }
            FileInputStream in = new FileInputStream(files[i].getAbsolutePath());
            System.out.println(" Adding: " + files[i].getAbsolutePath());
            out.putNextEntry(new ZipEntry(files[i].getAbsolutePath()));
            int len;
            while ((len = in.read(tmpBuf)) > 0) {
                out.write(tmpBuf, 0, len);
            }
            out.closeEntry();
            in.close();
        }
    }

}
