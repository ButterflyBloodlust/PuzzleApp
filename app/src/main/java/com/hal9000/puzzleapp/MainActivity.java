package com.hal9000.puzzleapp;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.AdapterView;
import android.widget.GridView;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    GridView gridView;
    PicGalleryAdapter adapter;

    private final static int[] imageResourceIDs = new int[]{
            R.drawable.pic01,
            R.drawable.pic02,
            R.drawable.pic03,
            R.drawable.pic04,
            R.drawable.pic05,
            R.drawable.pic06,
            R.drawable.pic07,
            R.drawable.pic08,
            R.drawable.pic09,
            R.drawable.pic10,
            R.drawable.pic01,
            R.drawable.pic02,
            R.drawable.pic03,
            R.drawable.pic04,
            R.drawable.pic05,
            R.drawable.pic06,
            R.drawable.pic07,
            R.drawable.pic08,
            R.drawable.pic09,
            R.drawable.pic10
    };
    private ArrayList<Integer> imageIDs = new ArrayList<>(imageResourceIDs.length);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initToolbar();
        setImagesData();

        gridView = findViewById(R.id.pic_gallery_gridview);
        // Getting adapter and passing data reference
        adapter = new PicGalleryAdapter(this, imageIDs);
        gridView.setAdapter(adapter);
        gridView.setOnScrollListener(new GalleryScrollListener(gridView));
        gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                //Log.d("ViewPictureActivity", "before start");
                ViewPictureActivity.start(MainActivity.this, imageIDs, position);
            }
        });

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.toolbar_menu, menu);
        return true;
    }

    private void initToolbar() {
        Toolbar displayActToolbar = findViewById(R.id.main_toolbar);
        setSupportActionBar(displayActToolbar);
    }

    private void setImagesData() {
        for (int resourceID : imageResourceIDs) {
            imageIDs.add(resourceID);
        }
    }
}
