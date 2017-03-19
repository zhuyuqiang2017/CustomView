package com.example.administrator.customview;

import android.os.CountDownTimer;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;

public class MainActivity extends AppCompatActivity {

    private CustomView cv;
    private int progressValue = 0;
    private boolean mProgressIsRunning = false;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        cv = (CustomView) findViewById(R.id.pb);
    }

    public void startProgressAnimation(View view){
        if (mProgressIsRunning){
            timer.cancel();
            timer.onFinish();
        }else{
            mProgressIsRunning = true;
            if (progressValue>=100){
                progressValue = 0;
            }
            timer.start();
        }
    }

    private CountDownTimer timer = new CountDownTimer(Integer.MAX_VALUE,100) {
        @Override
        public void onTick(long millisUntilFinished) {
            cv.setProgressValue(progressValue);
            progressValue ++;
        }

        @Override
        public void onFinish() {
            mProgressIsRunning = false;
        }
    };
}
