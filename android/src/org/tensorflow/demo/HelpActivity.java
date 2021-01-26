package org.tensorflow.demo;
// HelpActivity.java
// HelpActivity.java

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.speech.tts.TextToSpeech;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;

import java.util.HashMap;
import java.util.Locale;

public class HelpActivity extends Activity {
    TextView textView_tutorial;
    TextView textView_info_func;
    TextView textView_menu_tutorial;
    Context mContext;

    TTSSTT ttsstt;
    String text;

    RelativeLayout relativehelp;
    RelativeLayout relativetutorial;
    LinearLayout linear_menu_map;
    LinearLayout linear_menu_camera;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_help);

        ttsstt = new TTSSTT(this);

        textView_tutorial = (TextView)findViewById(R.id.textView_tutorial_simple);

        relativehelp = (RelativeLayout) findViewById(R.id.RelativeLayout_info_func_simple);
        relativetutorial = (RelativeLayout) findViewById(R.id.RelativeLayout_tutorial_simple);
        linear_menu_map = (LinearLayout) findViewById(R.id.linear_menu_map);
        linear_menu_camera = (LinearLayout) findViewById(R.id.linear_menu_camera);



        // 튜토리얼에 관한 설명
        textView_tutorial = (TextView)findViewById(R.id.textView_tutorial_simple);
        relativetutorial.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ttsstt.TimeToClick(textView_tutorial.getText().toString(), TutorialActivity.class);
            }
        });

        // 기능에 관한 설명
        textView_info_func = (TextView)findViewById(R.id.textView_info_func_simple);
        relativehelp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ttsstt.TimeToClick(textView_info_func.getText().toString(), FuncActivity.class);
            }
        });


        //하단바
        // 메뉴 - 지도 클릭
        linear_menu_map.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ttsstt.TimeToClick("지도", MapActivity.class);
            }
        });

        // 메뉴 - 카메라  클릭
        linear_menu_camera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ttsstt.TimeToClick("카메라", DetectorActivity.class);
            }
        });

    }

}