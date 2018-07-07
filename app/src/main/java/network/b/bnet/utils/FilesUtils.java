package network.b.bnet.utils;

import android.graphics.Bitmap;
import android.os.Environment;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import network.b.bnet.config.Constants;

/**
 * Created by jack.ma on 2017/8/10.
 */

public class FilesUtils {
    /**
     * get files long
     *
     * @param filePath
     * @return
     */
    public static long getFileSize(String filePath) {
        long size = 0;

        File file = new File(filePath);
        if (file != null && file.exists()) {
            size = file.length();
        }
        return size;
    }

    public static String getAPPBasePath() {
        String basePath = Environment.getExternalStorageDirectory().getPath() + File.separator + Constants.BaseAppFilesName;

        return basePath;
    }
    public static String getAPPAudioPath() {
        String basePath = Environment.getExternalStorageDirectory().getPath() + File.separator + Constants.BaseAppFilesName+ File.separator+Constants.AudioFilesName+ File.separator;

        return basePath;
    }

    /**
     * Bitmap transfer to bytes
     *
     * @param bm
     * @return
     */
    public static byte[] bitmapToBytes(Bitmap bm) {
        byte[] bytes = null;
        if (bm != null) {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            bm.compress(Bitmap.CompressFormat.PNG, 100, baos);
            bytes = baos.toByteArray();
        }
        return bytes;
    }


    public static File writeFile(byte[] buffer, String path) {
        boolean writeSucc = false;


        File file = new File(path);
        FileOutputStream out = null;
        try {
            out = new FileOutputStream(file);
            out.write(buffer);
            writeSucc = true;
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (out != null)
                    out.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        if (writeSucc) {
            return file;
        } else {
            return null;
        }
    }
}
