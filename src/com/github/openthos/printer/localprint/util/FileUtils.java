package com.github.openthos.printer.localprint.util;

import android.os.ParcelFileDescriptor;

import com.github.openthos.printer.localprint.APP;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

/**
 * Created by bboxh on 2016/4/12.
 */
public class FileUtils {


    private static final String TAG = "FileUtils";

    public static boolean copyFile(String docu_file_path,ParcelFileDescriptor data){

        boolean flag = false;

        /*if (!printJob.isQueued()) {
            return;
        }*/

        File outfile = new File(docu_file_path);
        LogUtils.d(TAG, "copyfile ->" + docu_file_path);
        outfile.delete();

        FileInputStream file = new ParcelFileDescriptor.AutoCloseInputStream(data);
        byte[] bbuf = new byte[1024];
        int hasRead = 0;

        try {

            FileOutputStream outStream = new FileOutputStream(outfile);

            while ((hasRead = file.read(bbuf)) > 0) {
                outStream.write(bbuf);
            }

            flag = true;
            LogUtils.d(TAG, "copyfile finished");
            outStream.close();

        } catch (IOException e) {
            e.printStackTrace();
            flag = false;
        } finally {
            try {
                file.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        return flag;
    }

    public static String getDocuFilePath(String s) {
        return getComponentPath() + getDocuFileName(s) ;
    }

    public static String getDocuFileName(String s) {
        return  "/" + s + "_" + APP.DOCU_FILE;
    }

    public static String getComponentPath() {
        return getFilePath() + APP.COMPONENT_PATH;
    }

    public static String getFilePath() {
        return APP.getApplicatioContext().getFilesDir().getAbsolutePath();
    }
}
