package com.example.administrator.customview;

import android.content.Context;
import android.view.Window;
import android.view.WindowManager;

/**
 * Created by Administrator on 2017/3/12 0012.
 */

public class WindowUtils {
    public float getWindowDensity(Context context){
        float density = context.getResources().getDisplayMetrics().density;
        if (density < 0){
            return 1;
        }
        return density;
    }
}
