package com.elimak.chap12horizontaliconview;

import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.app.Activity;
import android.view.Menu;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        final Resources res = getResources();
        final List<Drawable> list = new ArrayList<Drawable>();

        list.add(res.getDrawable(R.drawable.icon_00));
        list.add(res.getDrawable(R.drawable.icon_01));
        list.add(res.getDrawable(R.drawable.icon_02));
        list.add(res.getDrawable(R.drawable.icon_03));
        list.add(res.getDrawable(R.drawable.icon_04));
        list.add(res.getDrawable(R.drawable.icon_05));
        list.add(res.getDrawable(R.drawable.icon_06));
        list.add(res.getDrawable(R.drawable.icon_07));
        list.add(res.getDrawable(R.drawable.icon_08));
        list.add(res.getDrawable(R.drawable.icon_09));
        list.add(res.getDrawable(R.drawable.icon_10));
        list.add(res.getDrawable(R.drawable.icon_11));
        list.add(res.getDrawable(R.drawable.icon_12));
        list.add(res.getDrawable(R.drawable.icon_13));
        list.add(res.getDrawable(R.drawable.icon_14));
        list.add(res.getDrawable(R.drawable.icon_15));
        list.add(res.getDrawable(R.drawable.icon_16));
        list.add(res.getDrawable(R.drawable.icon_17));
        list.add(res.getDrawable(R.drawable.icon_18));
        list.add(res.getDrawable(R.drawable.icon_19));

        setContentView(R.layout.activity_main);
        final HorizontalIconView view = (HorizontalIconView) findViewById(R.id.horizontal_icon_view);
        view.setDrawables(list);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }
    
}
