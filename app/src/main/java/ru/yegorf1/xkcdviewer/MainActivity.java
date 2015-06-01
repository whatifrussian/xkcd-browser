package ru.yegorf1.xkcdviewer;

import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;


public class MainActivity extends AppCompatActivity {
    private Button firstButton;
    private Button prevButton;
    private Button comicsListButton;
    private Button nextButton;
    private Button lastButton;

    private ComicsFragment currentFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        if (savedInstanceState == null) {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    int last = XkcdAPI.getLastComicsId();

                    openComics(last);
                }
            }).start();
        }

        firstButton = (Button) findViewById(R.id.first_comics_button);
        prevButton = (Button) findViewById(R.id.prev_comics_button);
        comicsListButton = (Button) findViewById(R.id.list_comics_button);
        nextButton = (Button) findViewById(R.id.next_comics_button);
        lastButton = (Button) findViewById(R.id.last_comics_button);

        firstButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openComics(1);
            }
        });
        prevButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openComics(currentFragment.getPrev());
            }
        });
        nextButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openComics(currentFragment.getNext());
            }
        });
        lastButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openComics(XkcdAPI.getLastComicsId());
            }
        });
    }

    public void openComics(int id) {
        currentFragment = ComicsFragment.newInstance(id);

        try {
            boolean isLast = id == XkcdAPI.getLastComicsId(false);
            lastButton.setClickable(!isLast);
            lastButton.setTextColor(isLast ? Color.GRAY : Color.BLACK);
            nextButton.setClickable(!isLast);
            nextButton.setTextColor(isLast ? Color.GRAY : Color.BLACK);

            boolean isFirst = id == 1;
            firstButton.setClickable(!isFirst);
            prevButton.setClickable(!isFirst);
            firstButton.setTextColor(isFirst ? Color.GRAY : Color.BLACK);
            prevButton.setTextColor(isFirst ? Color.GRAY : Color.BLACK);

            comicsListButton.setText("#" + id);
        } catch (Exception ignored) {}


        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction transaction = fragmentManager.beginTransaction();

        transaction.replace(R.id.container, currentFragment);
        transaction.addToBackStack(null);

        transaction.commitAllowingStateLoss();
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        return id == R.id.action_settings || super.onOptionsItemSelected(item);

    }
}
