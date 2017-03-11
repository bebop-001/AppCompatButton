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
import android.app.Activity;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.media.AudioManager;
import android.support.v7.widget.AppCompatButton;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.ArrayList;


/*
 * these pages guided me in developing this code.
 * http://stackoverflow.com/questions/5706038/long-press-definition-at-xml-layout-like-androidonclick-does
 * http://kevindion.com/2011/01/custom-xml-attributes-for-android-widgets/
 */

public class CustomButton extends AppCompatButton
        implements View.OnClickListener, View.OnLongClickListener {
    private static final String TAG = "KanaButton";
    private static AudioManager audioManager;

    static class ClickHandler {
        static final ArrayList<ClickHandler> handlers = new ArrayList<>();
        String type, activityName; int buttonId; Method method;
        ClickHandler (String type, String activityName, int buttonId, Method method) {
            this.type = type; this.activityName = activityName;
            this.buttonId = buttonId; this.method = method;
            handlers.add(this);
        }
        static Method getHandler(String type, String activityName, int buttonId) {
            Method rv = null;
            for (ClickHandler c : handlers) {
                if (c.activityName.equals(activityName) &&c.type.equals(type)
                        && c.buttonId == buttonId) {
                    rv = c.method;
                    break;
                }
            }
            return rv;
        }
        public String toString() {
            return String.format("ClickHandler:type=\"%s\", id=0x%04x,activity=%s",
                    this.type, this.buttonId, this.activityName);
        }
    }
    boolean clickHandler( String type, View v) {
        boolean rv = false;
        int buttonId = v.getId();
        Object receiver = v.getContext();
        if (receiver instanceof ContextWrapper) {
            receiver = ((ContextWrapper)receiver).getBaseContext();
        }
        String actName = ((Activity)receiver).getClass().getSimpleName();
        Method handler = ClickHandler.getHandler(type, actName, buttonId);
        if (handler == null) {
            throw new RuntimeException (String.format(
                    "CustomButton:%s:activity=\"%s\", no handler found for 0x%04x"
                    , type, actName, buttonId));
        }
        try {
            if (type.equals("onClick"))
                handler.invoke(receiver, v);
            else
                rv = (boolean) handler.invoke(receiver, v);
        }
        catch (Exception e) {
            throw new RuntimeException(String.format(
                    "CustomButton:%s: id=%04x, activity=\"%s\" invoke FAILED:\n+%s"
                    , type, buttonId, actName, receiver.toString(), e.getMessage())
            );
        }
        return rv;
    }
    @Override
    public void onClick(View v) {
        clickHandler("onClick", v);
    }
    @Override
    public boolean onLongClick(View v) {
        boolean rv = clickHandler("onLongClick", v);
        audioManager.playSoundEffect(AudioManager.FX_KEY_CLICK);
        return rv;
    }
    public CustomButton(Context context) {
        super(context);
        Log.d(TAG, "got here 0.");
    }
    public CustomButton(Context context, AttributeSet attrs) throws NoSuchMethodException, ClassNotFoundException {
        super(context, attrs);
        audioManager = (AudioManager)context.getSystemService(Context.AUDIO_SERVICE);
        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.CustomButton);
        final int n = a.getIndexCount();
        int buttonId = this.getId();
        Class [] actClass = new Class[n];
        String[] actNames = new String[n];
        try {
            String packageName = context.getApplicationContext().getPackageName();
            PackageManager pm = context.getPackageManager();
            ActivityInfo[] actInfo = pm.getPackageInfo(
                    packageName, PackageManager.GET_ACTIVITIES).activities;
            for (int i = 0; i < n; i++) {
                actClass[i] = Class.forName(actInfo[i].name);
                actNames[i] = actClass[i].getSimpleName();
            }
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        for (int i = 0; i < n; i++) {
            int attr = a.getIndex(i);
            Log.w(TAG, String.format("button id:0x%04x", attr));
            String methodType = ((R.styleable.CustomButton_onClick == attr)
                ? "onClick"
                : ((R.styleable.CustomButton_onLongClick == attr)
                    ? "onLongClick"
                    : null
                )
            );
            if (null != methodType) {
                String methodName = a.getString(attr);
                int id = a.getResourceId(i, 666);
                Resources r = a.getResources();
                Method method = null;
                // Find a method by the name of "methodName" that expects a view.
                try {
                    for (int j = 0; j < actClass.length; j++) {
                        try {
                            method = actClass[j].getMethod(methodName, View.class);
                        }
                        catch (NoSuchMethodException e) {
                            if (actClass.length - 1 == j) {
                                String mess = "Failed to find method match for " + methodName
                                        + "\n" + e.getMessage();
                                throw new NoSuchMethodException(mess);
                            }
                        }
                        Type rType = method.getGenericReturnType();
                        // Make sure the return on the caller supplied
                        // value is correct.
                        if (methodType.equals("onClick") && ! rType.equals(void.class))
                            throw new NoSuchMethodException(
                                "CustomButton: methodName = \""
                                    + methodName + "\":expected return type void: Found "
                                    + rType.toString());
                        else if (methodType.equals("onLongClick") && ! rType.equals(boolean.class))
                            throw new NoSuchMethodException(
                                "CustomButton: methodName = \""
                                    + methodName + "\":expected return type boolean: Found "
                                    + rType.toString());
                        // We overoad OnClickListener and OnLongClickListener.
                        // setOnClick associates our OnClickListener's with our
                        // class so our onClick listners will get called.
                        if (methodType.equals("onClick"))
                            setOnClickListener(this);
                        else
                            setOnLongClickListener(this);
                        Log.d(TAG, new ClickHandler(methodType, actNames[j], buttonId, method).toString());
                    }
                }
                catch (Exception e) {
                    Log.d(TAG, "Find method " + methodName
                            + " in " + actClass.toString() + " FAILED:\n"
                            + e.getMessage());
                    throw e;
                }
            }
        }
        a.recycle();
    }
    public CustomButton(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        Log.d(TAG, "got here 2.");
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
}
