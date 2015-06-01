package ru.yegorf1.xkcdviewer;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

public class XkcdAPI {
    public static class ComicsInfo {
        public String title;
        public String imageUrl;
        public String thumbnailUrl;
        public String text;
        public String comment;
        public String originalUrl;
        public String url;
        public String next;
        public String previous;
        public String first;
        public String last;
        public String random;

        public ComicsInfo() { }
    }

    public static ComicsInfo getComicsByID(int id) {
        return  getComicsByURL("http://xkcd.ru/" + id + "/");
    }

    public static ComicsInfo getComicsByURL(String url) {
        String jsonUrl = url + "?json=1";
        String json;

        try {
            json = getPageSource(jsonUrl);
        } catch (IOException ex) {
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

            return res;
        } catch (JSONException ignored) { }

        return new ComicsInfo();
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
