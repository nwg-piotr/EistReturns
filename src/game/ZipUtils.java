package game;

import java.io.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

class ZipUtils {

    /**
     * Based on the https://stackoverflow.com/a/16646691/4040598 by Jonas Byström
     */
    static final class ZipFileUtil {
        static void zipDirectory(File dir, File zipFile) throws IOException {
            FileOutputStream fout = new FileOutputStream(zipFile);
            ZipOutputStream zout = new ZipOutputStream(fout);
            zipSubDirectory("", dir, zout);
            zout.close();
        }

        static void zipSubDirectory(String basePath, File dir, ZipOutputStream zout) throws IOException {
            byte[] buffer = new byte[4096];
            File[] files = dir.listFiles();
            if(files != null) {
                for (File file : files) {
                    if (file.isDirectory()) {
                        /*
                         * Skip the 'editor-data' folder and anything else user could have put there
                         */
                        if (isLevel(file)) {
                            String path = basePath + file.getName() + "/";
                            zout.putNextEntry(new ZipEntry(path));
                            zipSubDirectory(path, file, zout);
                            zout.closeEntry();
                        }
                    } else {
                        /*
                         * For some reason this also adds trash files like 'java.io.FileInputStream@1c636e41'
                         */
                        if (!file.getName().startsWith("java.io.FileInputStream")) {
                            FileInputStream fin = new FileInputStream(file);
                            zout.putNextEntry(new ZipEntry(basePath + file.getName()));
                            int length;
                            while ((length = fin.read(buffer)) > 0) {
                                zout.write(buffer, 0, length);
                            }
                            zout.closeEntry();
                            fin.close();
                        }
                    }
                }
            }
        }

        /**
         * @param file - folder name to check
         * @return true if the folder name consists of 2 digits
         */
        private static boolean isLevel(File file){

            boolean result = false;
            String name = file.getName();
            try {
                // just to trigger an exception if the folder name couldn't be converted into int
                int number = Integer.valueOf(name);

                if (name.length() == 2) {
                    result = true;
                }
            } catch (Exception e) {
                result = false;
            }
            return result;
        }
    }

}
