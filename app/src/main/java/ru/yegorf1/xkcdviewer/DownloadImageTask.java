package ru.yegorf1.xkcdviewer;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Environment;
import android.util.Log;
import android.widget.ImageView;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;

import uk.co.senab.photoview.PhotoViewAttacher;

public class DownloadImageTask extends AsyncTask<String, Void, Bitmap> {
    private ImageView bmImage;
    private PhotoViewAttacher attacher;
    private String path;

    public DownloadImageTask(String path) {
        this.path = path;
    }

    public DownloadImageTask(ImageView bmImage) {
        this.bmImage = bmImage;
    }

    public DownloadImageTask(ImageView bmImage, PhotoViewAttacher attacher) {
        this.bmImage = bmImage;
        this.attacher = attacher;
    }

    protected Bitmap doInBackground(String... urls) {
        Bitmap mIcon11;
        InputStream in;
        URL url;

        try {
            url = new URL(urls[0]);
        } catch (MalformedURLException e) {
            e.printStackTrace();
            return null;
        }
        String path;

        if (this.path != null) {
            path = this.path;
        } else {
            path = XkcdAPI.getWorkingDir() + url.getPath();
        }

        if (XkcdAPI.useStorage()) {
            File imageFile = new File(path);
            if (imageFile.exists()) {
                BitmapFactory.Options options = new BitmapFactory.Options();
                options.inPreferredConfig = Bitmap.Config.ARGB_8888;
                Bitmap bitmap = BitmapFactory.decodeFile(path, options);

                return bitmap;
            }
        }

        try {
            in = url.openStream();
            mIcon11 = BitmapFactory.decodeStream(in);
            in.close();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }

        if (!XkcdAPI.useStorage() || this.path != null) {
            saveBitmap(path, mIcon11);
        }

        return mIcon11;
    }

    public static void saveBitmap(String filename, Bitmap bitmap) {
        File imageFile = new File(filename);
        File parent = imageFile.getParentFile();

        if(!parent.exists() && !parent.mkdirs()){ }

        FileOutputStream out = null;
        try {
            out = new FileOutputStream(filename);
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, out);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (out != null) {
                    out.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    protected void onPostExecute(Bitmap result) {
        if (bmImage != null) {
            bmImage.setImageBitmap(result);
            if (attacher != null) {
                attacher.update();
            }
        }
    }
}