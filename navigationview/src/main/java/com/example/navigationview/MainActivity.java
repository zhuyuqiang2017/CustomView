package com.example.navigationview;

import android.graphics.Rect;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import java.io.IOException;
import java.io.InputStream;

public class MainActivity extends AppCompatActivity {

    private DecoderView dv;
    private CustomNavigationView cnv;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        dv = (DecoderView) findViewById(R.id.dv);
        cnv = (CustomNavigationView) findViewById(R.id.cnv);
        InputStream is = null;
        try {
            is = getAssets().open("lol.jpg");
            Log.i("zyq", "(is != null) =" + (is != null));
        } catch (IOException e) {
            Log.i("zyq", "exception = " + e.toString());
            e.printStackTrace();
        }
        dv.setImageResource(is);
        dv.setViewPortChangeListener(new DecoderView.onViewPortPositionChangeListener() {
            @Override
            public void getViewPortRect(Rect mViewPortRect) {
                cnv.setBoundsRect(mViewPortRect);
            }
        });
        cnv.setDrawableId(R.drawable.lol);
        cnv.setOnBoundsRectChangeListener(new CustomNavigationView.onBoundsChangedListener() {
            @Override
            public void deliveryViewAndBoundsRect(int width, int height, Rect mBoundsRect) {
                dv.setViewPortRectPosition(mBoundsRect.left / (float) width, mBoundsRect.top / (float) height);
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        cnv.setBoundsRect(dv.getViewPortRect());
    }
}

