package com.kana_tutor.buttonshortlongclickdemo;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

public class FragActivity extends AppCompatActivity {
    private static final String TAG = "FragActivity";
    private static AudioManager audioManager;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.frag_container);
        audioManager = (AudioManager)getSystemService(Context.AUDIO_SERVICE);


        Fragment f = new Frag();

        FragmentManager fm = getSupportFragmentManager();

        fm.beginTransaction()
                .replace(R.id.frag_container, f)
                .commit();

    }

    @Override
    protected void onResume() {
        super.onResume();
        // setup in onResume because frag still isn't visible in onCreate.

        View rootView = findViewById(R.id.root_view);
        rootView.setBackgroundColor(0xffe57373);

        String activityName = getClass().getSimpleName();
        TextView activityTV = (TextView)findViewById(R.id.activity_name_TV);
        activityTV.setText(activityName);
        Button selectActivity = (Button)findViewById(R.id.change_activity);
        selectActivity.setText("Select \"Main\" Activity");
    }
    @SuppressLint("SetTextI18n")
    public void onClickChangeActivity(View v) {
        Intent i = new Intent(this, MainActivity.class);
        startActivity(i);
    }
    @SuppressLint("SetTextI18n")
    public void onClick_1(View v) {
        TextView tv = (TextView)((View)v.getParent()).findViewById(R.id.short_long_TV);
        String buttonText = ((Button)v).getText().toString();
        tv.setText(buttonText + ":short click");
    }
    @SuppressLint("SetTextI18n")
    public void onClick_2(View v) {
        TextView tv = (TextView)((View)v.getParent()).findViewById(R.id.short_long_TV);
        String buttonText = ((Button)v).getText().toString();
        tv.setText(buttonText + ":short click");
    }
    @SuppressLint("SetTextI18n")
    public void onClick_3(View v) {
        TextView tv = (TextView)((View)v.getParent()).findViewById(R.id.short_long_TV);
        String buttonText = ((Button)v).getText().toString();
        tv.setText(buttonText + ":short click");
    }
    @SuppressLint("SetTextI18n")
    @SuppressWarnings("SameReturnValue")
    public boolean longClick_1(View v) {
        audioManager.playSoundEffect(AudioManager.FX_KEY_CLICK);
        TextView tv = (TextView)((View)v.getParent()).findViewById(R.id.short_long_TV);
        String buttonText = ((Button)v).getText().toString();
        tv.setText(buttonText + ":long click");
        return true;
    }
    @SuppressLint("SetTextI18n")
    @SuppressWarnings("SameReturnValue")
    public boolean longClick_2(View v) {
        audioManager.playSoundEffect(AudioManager.FX_KEY_CLICK);
        TextView tv = (TextView)((View)v.getParent()).findViewById(R.id.short_long_TV);
        String buttonText = ((Button)v).getText().toString();
        tv.setText(buttonText + ":long click");
        return true;
    }
    @SuppressLint("SetTextI18n")
    @SuppressWarnings("SameReturnValue")
    public boolean longClick_3(View v) {
        audioManager.playSoundEffect(AudioManager.FX_KEY_CLICK);
        TextView tv = (TextView)((View)v.getParent()).findViewById(R.id.short_long_TV);
        String buttonText = ((Button)v).getText().toString();
        tv.setText(buttonText + ":long click");
        return true;
    }
}
