package ru.yegorf1.xkcdviewer;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;


public class ComicsFragment extends Fragment {
    private static final String ARG_COMICS_NUMBER = "number";

    private int comicsNumber;

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
        return inflater.inflate(R.layout.fragment_comics, container, false);
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
    }

    @Override
    public void onDetach() {
        super.onDetach();
    }
}
