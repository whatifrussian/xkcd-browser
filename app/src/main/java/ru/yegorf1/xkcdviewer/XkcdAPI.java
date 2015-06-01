package ru.yegorf1.xkcdviewer;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

public class XkcdAPI {
    public static class BaseComicsInfo {
        public int id;
        public String title;
        public String thumbnailUrl;
        public String url;
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

        public ComicsInfo() { }
    }

    private static ComicsInfo lastLoaded;

    public static ComicsInfo getComicsByID(int id) {
        ComicsInfo info = getComicsByURL("http://xkcd.ru/" + id + "/");
        info.id = id;

        return info;
    }

    public static ComicsInfo getComicsByURL(String url) {
        String jsonUrl = url + "?json=1";
        String json;

        try {
            json = getPageSource(jsonUrl);
        } catch (IOException ex) {
            lastLoaded = new ComicsInfo();
            return new ComicsInfo();
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
        } catch (JSONException ignored) { }

        lastLoaded = new ComicsInfo();

        return new ComicsInfo();
    }

    public static String getLastComicsURL() {
        if (lastLoaded == null || lastLoaded.equals(new ComicsInfo())) {
            List<BaseComicsInfo> infos = getComicsList();
            return getComicsByURL(infos.get(infos.size() - 1).url).url;
        } else {
            return lastLoaded.last;
        }
    }

    public static int getLastComicsId() {
        return urlToId(getLastComicsURL());
    }

    public static int getLastComicsId(boolean canUseInternet) {
        if (canUseInternet) {
            return  getLastComicsId();
        } else if (lastLoaded != null) {
            return urlToId(lastLoaded.last);
        } else {
            return 0;
        }
    }


    public static int urlToId(String url) {
        return Integer.parseInt(url.replaceAll("\\D+",""));
    }

    public static List<BaseComicsInfo> getComicsList() {
        List<BaseComicsInfo> res = new ArrayList<>();

        String jsonUrl = "http://xkcd.ru/num/?json=1";
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

                    BaseComicsInfo info = new BaseComicsInfo();

                    info.id = jsonInfo.getInt("id");
                    info.title = jsonInfo.getString("title");
                    info.thumbnailUrl = jsonInfo.getString("thumbnail");
                    info.url = jsonInfo.getString("url");

                    res.add(info);
                } catch (JSONException ignored) { }
            }

        } catch (JSONException ignored) { }

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
        while((line = reader.readLine()) != null)
        {
            str.append(line);
        }
        in.close();

        return str.toString();
    }
}
