package ru.yegorf1.xkcdviewer;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import java.io.File;
import java.util.concurrent.ExecutionException;

public class MainActivity extends AppCompatActivity {
    private static Context context;

    private static final int CHOOSE_COMICS = 42;

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

        context = getApplicationContext();

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
                openComics(XkcdAPI.getFirstComicsId());
            }
        });
        prevButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openComics(currentFragment.getPrev());
            }
        });
        comicsListButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openComicsList();
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

    private void openComicsList() {
        Intent chooseComicsIntent = new Intent(getApplicationContext(), ComicsListActivity.class);
        startActivityForResult(chooseComicsIntent, CHOOSE_COMICS);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == CHOOSE_COMICS && resultCode == RESULT_OK && data != null) {
            int comicsId = data.getIntExtra("id", 1);
            openComics(comicsId);
        }
    }


    public void openComics(int id) {
        currentFragment = ComicsFragment.newInstance(id);

        try {
            boolean isLast = id == XkcdAPI.getLastComicsId();
            lastButton.setClickable(!isLast);
            lastButton.setTextColor(isLast ? Color.GRAY : Color.BLACK);
            nextButton.setClickable(!isLast);
            nextButton.setTextColor(isLast ? Color.GRAY : Color.BLACK);

            boolean isFirst = id == XkcdAPI.getFirstComicsId();
            firstButton.setClickable(!isFirst);
            prevButton.setClickable(!isFirst);
            firstButton.setTextColor(isFirst ? Color.GRAY : Color.BLACK);
            prevButton.setTextColor(isFirst ? Color.GRAY : Color.BLACK);

            comicsListButton.setText("#" + id);
        } catch (Exception ignored) {
        }


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
        XkcdAPI.ComicsInfo c = currentFragment.comicsInfo;

        switch (id) {
            case R.id.action_share:
                Bitmap b = currentFragment.getImage();
                if (b == null) {
                    Toast.makeText(getApplicationContext(), "Image still loading", Toast.LENGTH_SHORT).show();
                } else {

                    String folder = XkcdAPI.getWorkingDir();
                    String path = folder + "/share_" + System.currentTimeMillis() + ".png";
                    try {
                        new DownloadImageTask(path).execute(c.imageUrl).get();
                    } catch (ExecutionException | InterruptedException e) { e.printStackTrace(); }
                    Uri bmpUri = Uri.fromFile(new File(path));

                    Intent share = new Intent(Intent.ACTION_SEND);
                    share.setType("image/png");
                    share.putExtra(Intent.EXTRA_TEXT, c.title + "|" + c.url);
                    share.putExtra(Intent.EXTRA_STREAM, bmpUri);
                    startActivity(Intent.createChooser(share, "Share Image"));
                }
                break;
            case R.id.action_copy:
                ClipboardManager clipboard = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
                ClipData clip = ClipData.newPlainText(c.title, c.url);
                clipboard.setPrimaryClip(clip);
                break;
        }

        return id == R.id.action_settings || super.onOptionsItemSelected(item);

    }

    public static boolean isOffline() {
        ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo wifiInfo = connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
        NetworkInfo mobileInfo = connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);

        return !((wifiInfo != null && wifiInfo.isConnected()) || (mobileInfo != null && mobileInfo.isConnected()));
    }
}

