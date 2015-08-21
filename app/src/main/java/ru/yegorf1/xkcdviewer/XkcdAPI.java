package ru.yegorf1.xkcdviewer;

import android.os.Environment;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class XkcdAPI {

    public static class BaseComicsInfo implements Comparable<BaseComicsInfo> {
        public int id;
        public String title;
        public String thumbnailUrl;
        public String url;

        public BaseComicsInfo() {
        }

        public BaseComicsInfo(JSONObject jsonInfo) throws JSONException {
            this.title = jsonInfo.getString("title");
            this.thumbnailUrl = jsonInfo.getString("thumbnail");
            this.url = jsonInfo.getString("url");
            this.id = urlToId(this.url);
        }

        @Override
        public int compareTo(BaseComicsInfo b) {
            return id - b.id;
        }
    }

    public static class ComicsInfo extends BaseComicsInfo {
        public String imageUrl;
        public String text;
        public String comment;
        public String originalUrl;
        public String next;
        public String previous;
        public String first;
        public String last;
        public String random;

        public ComicsInfo() {
        }
    }

    private static ComicsInfo lastLoaded;

    public static ComicsInfo getComicsByID(int id) {
        ComicsInfo info = getComicsByURL("http://xkcd.ru/" + id + "/");
        info.id = id;

        return info;
    }

    public static boolean useStorage() {
        String state = Environment.getExternalStorageState();

        return Environment.MEDIA_MOUNTED.equals(state) && MainActivity.useStorage();
    }

    public static String getWorkingDir() {
        return Environment.getExternalStorageDirectory().toString() + "/xkcd";
    }

    public static ComicsInfo getComicsByURL(String url) {
        String jsonUrl = url + "?json=1";
        String json = "";

        String filename = getWorkingDir() + "/j/" + urlToId(url) + ".json";

        if (useStorage()) {
            File jsonFile = new File(filename);
            File jsonsDir = jsonFile.getParentFile();
            jsonsDir.mkdirs();

            if (jsonFile.exists()) {
                json = readFileToEnd(jsonFile);
            }
        }

        if (json.equals("")) {
            try {
                json = getPageSource(jsonUrl);
            } catch (IOException ex) {
                lastLoaded = new ComicsInfo();
                return new ComicsInfo();
            }

            if (useStorage()) {
                FileOutputStream outputStream;

                try {
                    outputStream = new FileOutputStream(filename);
                    outputStream.write(json.getBytes());
                    outputStream.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }

        JSONObject jsonObj;

        try {
            jsonObj = new JSONObject(json);

            ComicsInfo res = new ComicsInfo();

            res.title = jsonObj.getString("title");
            res.imageUrl = jsonObj.getString("image");
            res.thumbnailUrl = jsonObj.getString("thumbnail");
            res.text = jsonObj.getString("text");
            res.comment = jsonObj.getString("comment");
            res.originalUrl = jsonObj.getString("original_url");
            res.url = jsonObj.getString("url");
            res.next = jsonObj.getString("next");
            res.previous = jsonObj.getString("previous");
            res.first = jsonObj.getString("first");
            res.last = jsonObj.getString("last");
            res.random = jsonObj.getString("random");

            lastLoaded = res;

            return res;
        } catch (JSONException ignored) {
        }

        lastLoaded = new ComicsInfo();

        return new ComicsInfo();
    }

    private static String readFileToEnd(File file) {
        StringBuilder text = new StringBuilder();

        try {
            BufferedReader br = new BufferedReader(new FileReader(file));
            String line;

            while ((line = br.readLine()) != null) {
                text.append(line);
                text.append('\n');
            }
            br.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return text.toString();
    }

    public static String getLastComicsURL() {
        if (MainActivity.isOffline() || lastLoaded == null || lastLoaded.equals(new ComicsInfo())) {
            List<BaseComicsInfo> infos = getComicsList();
            return getComicsByURL(infos.get(infos.size() - 1).url).url;
        } else {
            return lastLoaded.last;
        }
    }

    private static String getFirstComicsURL() {
        if (MainActivity.isOffline() || lastLoaded == null || lastLoaded.equals(new ComicsInfo())) {
            List<BaseComicsInfo> infos = getComicsList();
            return getComicsByURL(infos.get(0).url).url;
        } else {
            return lastLoaded.first;
        }
    }

    public static int getLastLoadedComicsId() {
        if (lastLoaded == null) {
            return getLastComicsId();
        } else {
            return lastLoaded.id;
        }
    }

    public static int getLastComicsId() {
        return urlToId(getLastComicsURL());
    }

    public static int getFirstComicsId() {
        return urlToId(getFirstComicsURL());
    }

    public static int getNextComics(ComicsInfo info) {
        if (MainActivity.isOffline()) {
            int n = info.id + 1;

            while (!saved(n) && n < getLastComicsId()) {
                n++;
            }

            return n;
        } else {
            return urlToId(info.next);
        }
    }

    public static int getPrevComics(ComicsInfo info) {
        if (MainActivity.isOffline()) {
            int n = info.id - 1;

            while (!saved(n) && n > 0) {
                n--;
            }

            return n;
        } else {
            return urlToId(info.previous);
        }
    }

    private static boolean saved(int id) {
        String filename = getWorkingDir() + "/j/" + id + ".json";

        return new File(filename).exists();
    }

    public static int urlToId(String url) {
        return Integer.parseInt(url.replaceAll("\\D+", ""));
    }

    public static List<BaseComicsInfo> getComicsList() {
        List<BaseComicsInfo> res = new ArrayList<>();

        String jsonUrl = "http://xkcd.ru/num/?json=1";

        if (MainActivity.isOffline()) {
            File jsonDir = new File(getWorkingDir() + "/j/");
            File[] jsons = jsonDir.listFiles();

            for (File j : jsons) {
                String json = readFileToEnd(j);

                try {
                    JSONObject jsonInfo = new JSONObject(json);
                    BaseComicsInfo info = new BaseComicsInfo(jsonInfo);

                    res.add(info);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            Collections.sort(res);
        } else {
            String json;

            try {
                json = getPageSource(jsonUrl);
            } catch (IOException ex) {
                return res;
            }

            try {
                JSONObject jsonObj = new JSONObject(json);
                JSONArray list = jsonObj.getJSONArray("list");

                for (int i = 0; i < list.length(); i++) {
                    try {
                        JSONObject jsonInfo = list.getJSONObject(i);

                        BaseComicsInfo info = new BaseComicsInfo(jsonInfo);

                        res.add(info);
                    } catch (JSONException ignored) {
                    }
                }

            } catch (JSONException ignored) {
            }
        }

        return res;
    }

    private static String getPageSource(String url) throws IOException {
        HttpClient client = new DefaultHttpClient();
        HttpGet request = new HttpGet(url);
        HttpResponse response = client.execute(request);

        InputStream in = response.getEntity().getContent();
        BufferedReader reader = new BufferedReader(new InputStreamReader(in));
        StringBuilder str = new StringBuilder();
        String line;
        while ((line = reader.readLine()) != null) {
            str.append(line);
        }
        in.close();

        return str.toString();
    }
}
