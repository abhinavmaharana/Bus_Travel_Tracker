package com.example.bus_travel_tracker;

import android.content.Context;
import android.widget.ArrayAdapter;
class MyAdapter extends ArrayAdapter<String> {

    Context context;
    String rTitle[];
    String rDescription[];
    int rImgs[];

    MyAdapter (Context c, String title[], String description[], int imgs[]) {
        super(c, R.layout.row, R.id.textView1, title);
        this.context = c;
        this.rTitle = title;
        this.rDescription = description;
        this.rImgs = imgs;

    }

}
