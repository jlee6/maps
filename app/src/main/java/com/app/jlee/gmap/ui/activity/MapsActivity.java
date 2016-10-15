package com.app.jlee.gmap.ui.activity;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import com.app.jlee.gmap.R;
import com.app.jlee.gmap.ui.fragment.MapsFragment;

public class MapsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);

        getSupportFragmentManager().beginTransaction()
                .replace(R.id.map_placeholder, new MapsFragment())
                .commit();
    }
}
