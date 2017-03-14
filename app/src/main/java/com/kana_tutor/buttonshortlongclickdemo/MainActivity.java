/*
 *
 *    Copyright 2017 Steven Smith, sjs@kana-tutor.com
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing,
 *    software distributed under the License is distributed on an
 *    "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 *    either express or implied. See the License for the specific
 *    language governing permissions and limitations under the License.
 */
package com.kana_tutor.buttonshortlongclickdemo;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

@SuppressWarnings("unused")
public class MainActivity extends AppCompatActivity {
    @SuppressWarnings("unused")
    private static final String TAG = "MainActivity";
    private static AudioManager audioManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        audioManager = (AudioManager)getSystemService(Context.AUDIO_SERVICE);
    }

    @SuppressLint("SetTextI18n")
    @Override
    protected void onResume() {
        super.onResume();
        View rootView = findViewById(R.id.root_view);
        rootView.setBackgroundColor(0xffb3e5fc);
        String activityName = this.getClass().getSimpleName();
        TextView activityTV = (TextView)findViewById(R.id.activity_name_TV);
        activityTV.setText(activityName);
        Button selectActivity = (Button)findViewById(R.id.change_activity);
        selectActivity.setText("Select \"Other\" Activity");
    }

    @SuppressLint("SetTextI18n")
    public void onClickChangeActivity(View v) {
        Intent i = new Intent(this, OtherActivity.class);
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
