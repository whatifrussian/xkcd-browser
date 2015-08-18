package ru.yegorf1.xkcdviewer;

import android.app.ListActivity;
import android.os.AsyncTask;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

public class ComicsListActivity extends ListActivity {
    private ComicsAdapter comicsAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_comics_list);

        comicsAdapter = new ComicsAdapter(getApplicationContext(), new ArrayList<XkcdAPI.BaseComicsInfo>());

        setListAdapter(comicsAdapter);

        new LoadComicsListTask(this).execute();
    }

    @Override
    protected void onListItemClick(ListView list, View view, int position, long id) {
        super.onListItemClick(list, view, position, id);

        String selectedItem = ((XkcdAPI.BaseComicsInfo) getListAdapter().getItem(position)).title;

        if (selectedItem != null) {
            Toast.makeText(getApplicationContext(), "You clicked " + selectedItem + " at position " + position, Toast.LENGTH_SHORT).show();
        }
    }

    private void onComicsLoaded(List<XkcdAPI.BaseComicsInfo> result) {
        comicsAdapter.clear();

        if (result != null) {
            for (XkcdAPI.BaseComicsInfo info : result) {
                comicsAdapter.insert(info, comicsAdapter.getCount());
            }
        }

        setListAdapter(new ComicsAdapter(this, result));
    }

    private class LoadComicsListTask extends AsyncTask<Void, Void, List<XkcdAPI.BaseComicsInfo>> {
        private ComicsListActivity activity;

        public LoadComicsListTask(ComicsListActivity activity) {
            this.activity = activity;
        }

        @Override
        protected List<XkcdAPI.BaseComicsInfo> doInBackground(Void... v) {
            return XkcdAPI.getComicsList();
        }

        protected void onPostExecute(List<XkcdAPI.BaseComicsInfo> result) {
            activity.onComicsLoaded(result);
        }
    }
}
