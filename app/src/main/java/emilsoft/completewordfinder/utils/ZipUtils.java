package emilsoft.completewordfinder.utils;

import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.ref.WeakReference;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import emilsoft.completewordfinder.MainActivity;

public class ZipUtils {

    private static int DEFAULT_BUFFER_SIZE = 65536;

//    public static void unzip(InputStream zipFile, File targetDirectory) {
//            try (BufferedInputStream bis = new BufferedInputStream(zipFile)) {
//                try (ZipInputStream zis = new ZipInputStream(bis)) {
//                    ZipEntry ze;
//                    int count;
//                    byte[] buffer = new byte[DEFAULT_BUFFER_SIZE];
//                    while ((ze = zis.getNextEntry()) != null) {
//                        File file = new File(targetDirectory, ze.getName());
//                        File dir = ze.isDirectory() ? file : file.getParentFile();
//                        if (!dir.isDirectory() && !dir.mkdirs())
//                            throw new FileNotFoundException("Failed to ensure directory: "+dir.getAbsolutePath());
//                        if (ze.isDirectory())
//                            continue;
//                        try (FileOutputStream fout = new FileOutputStream(file)) {
//                            while ((count = zis.read(buffer)) != -1)
//                                fout.write(buffer, 0, count);
//                        }
//                    }
//                }
//            } catch (IOException | NullPointerException ex) {
//            Log.v(MainActivity.TAG, "Zip not opened.\n"+ex.getMessage());
//            }
//    }

    public static void unzipToInternalStorage(InputStream zipFile, FileOutputStream outputFile) {
        try (BufferedInputStream bis = new BufferedInputStream(zipFile)) {
            try (ZipInputStream zis = new ZipInputStream(bis)) {
                ZipEntry ze;
                int count;
                byte[] buffer = new byte[DEFAULT_BUFFER_SIZE];
                if ((ze = zis.getNextEntry()) != null) {
                    try (BufferedOutputStream bos = new BufferedOutputStream(outputFile)) {
                        while ((count = zis.read(buffer)) != -1)
                            bos.write(buffer, 0, count);
                    }
                }
            }
        } catch (IOException | NullPointerException ex) {
            Log.v(MainActivity.TAG, "Zip not opened.\n"+ex.getMessage());
        }
    }

    public static boolean fileExists(Context context, String filename) {
        WeakReference<Context> ctx = new WeakReference<>(context);
        File file = ctx.get().getFileStreamPath(filename);
        //Log.v(MainActivity.TAG, "File Length = "+file.length());
        return file != null && file.exists();
    }

    public static void copyFromAssetsToInternalStorage(InputStream input, OutputStream output) {
        try {
            copyFile(input, output);
        } catch (IOException ex) {
            Log.v(MainActivity.TAG, "Copy Asset to Internal Storage failed; "+ex.getMessage());
        }
    }

    //Unzip the file after it's copied in internal storage
    public static void unzip(Context context, InputStream zipFile) {
        try (BufferedInputStream bis = new BufferedInputStream(zipFile)) {
            try (ZipInputStream zis = new ZipInputStream(bis)) {
                ZipEntry ze;
                int count;
                byte[] buffer = new byte[DEFAULT_BUFFER_SIZE];
                while ((ze = zis.getNextEntry()) != null) {
                    try (FileOutputStream fout = context.openFileOutput(ze.getName(), Context.MODE_PRIVATE)) {
                        while ((count = zis.read(buffer)) != -1)
                            fout.write(buffer, 0, count);
                    }
                }
            }
        } catch (IOException | NullPointerException ex) {
            Log.v(MainActivity.TAG, "Zip not opened.\n"+ex.getMessage());
        }
    }

    private static void copyFile(InputStream in, OutputStream out) throws IOException{
        byte[] buffer = new byte[DEFAULT_BUFFER_SIZE];
        int count;
        while ((count = in.read(buffer)) != -1)
            out.write(buffer, 0, count);
    }
}
