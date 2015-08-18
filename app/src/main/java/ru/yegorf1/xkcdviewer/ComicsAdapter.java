package ru.yegorf1.xkcdviewer;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;

public class ComicsAdapter extends ArrayAdapter<XkcdAPI.BaseComicsInfo> {
    private final Context context;

    public ComicsAdapter(Context context, List<XkcdAPI.BaseComicsInfo> comics) {
        super(context, R.layout.comics_list_item_layout, comics);

        this.context = context;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View rowView = inflater.inflate(R.layout.comics_list_item_layout, parent, false);
        TextView textView = (TextView) rowView.findViewById(R.id.comics_name);
        ImageView imageView = (ImageView) rowView.findViewById(R.id.comics_thumbnail);

        textView.setText(getItem(position).title);
        new DownloadImageTask(imageView).execute(getItem(position).thumbnailUrl);

        return rowView;
    }
}
