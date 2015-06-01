package ru.yegorf1.xkcdviewer;

import android.app.Activity;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import uk.co.senab.photoview.PhotoViewAttacher;


public class ComicsFragment extends Fragment {
    private static final String ARG_COMICS_NUMBER = "number";

    private int comicsNumber;
    private TextView comicsTitleTextView;
    private ImageView comicsImageView;
    private PhotoViewAttacher comicsImageAttacher;

    public static ComicsFragment newInstance(int comicsNumber) {
        ComicsFragment fragment = new ComicsFragment();

        Bundle args = new Bundle();
        args.putInt(ARG_COMICS_NUMBER, comicsNumber);
        fragment.setArguments(args);

        return fragment;
    }

    public ComicsFragment() { }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getArguments() != null) {
            comicsNumber = getArguments().getInt(ARG_COMICS_NUMBER);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_comics, container, false);

        comicsTitleTextView = (TextView)v.findViewById(R.id.comics_title);
        comicsImageView = (ImageView)v.findViewById(R.id.comics_image);

        new DownloadComicsTask(this).execute(comicsNumber);

        comicsImageAttacher = new PhotoViewAttacher(comicsImageView);

        return v;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
    }

    @Override
    public void onDetach() {
        super.onDetach();
    }

    private void setComics(XkcdAPI.ComicsInfo result) {
        comicsTitleTextView.setText(result.title);
        new DownloadImageTask(comicsImageView).execute(result.imageUrl);
    }

    private class DownloadComicsTask extends AsyncTask<Integer, Void, XkcdAPI.ComicsInfo> {
        private ComicsFragment fragment;

        public DownloadComicsTask(ComicsFragment fragment) {
            this.fragment = fragment;
        }

        @Override
        protected XkcdAPI.ComicsInfo doInBackground(Integer... params) {
            int id = params[0];

            return XkcdAPI.getComicsByID(id);
        }

        protected void onPostExecute(XkcdAPI.ComicsInfo result) {
            fragment.setComics(result);
        }
    }
}

