package org.tensorflow.demo;
// CameraInfoActivity.java

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;

import java.util.HashMap;
import java.util.Locale;

public class CameraInfoActivity extends Activity {
    TTSSTT ttsstt;
    TextView textView;
    LinearLayout linear_menu_tutorial;
    LinearLayout linear_menu_map;
    LinearLayout linear_menu_camera;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_info_camera);

        ttsstt = new TTSSTT(this);

        linear_menu_tutorial = (LinearLayout)findViewById(R.id.linear_menu_tutorial);
        linear_menu_map = (LinearLayout)findViewById(R.id.linear_menu_map);
        linear_menu_camera = (LinearLayout) findViewById(R.id.linear_menu_camera);

        textView = (TextView)findViewById(R.id.textView_info_camera_content);

        textView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                ttsstt.tts.setSpeechRate(0.9f);
                ttsstt.tts.speak(textView.getText().toString(),TextToSpeech.QUEUE_FLUSH, null,null);
            }
        });

        // 메뉴 - 도움말 클릭
        linear_menu_tutorial.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ttsstt.TimeToClick("도움말", HelpActivity.class);
            }
        });

        // 메뉴 - 지도 클릭
        linear_menu_map.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ttsstt.TimeToClick("지도", MapActivity.class);
            }
        });

        // 메뉴 - 카메라 클릭
        linear_menu_camera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ttsstt.TimeToClick("카메라", DetectorActivity.class);
            }
        });
    }

    protected void onDestroy() {
        super.onDestroy();
        // TTS 객체가 남아있다면 실행을 중지하고 메모리에서 제거한다.
        if(ttsstt.tts != null){
            ttsstt.tts.stop();
            ttsstt.tts.shutdown();
            ttsstt.tts = null;
        }
    }
}