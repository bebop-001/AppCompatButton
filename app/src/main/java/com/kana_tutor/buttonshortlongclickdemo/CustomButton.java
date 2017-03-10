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
import android.content.ContextWrapper;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
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


/*
 * these pages guided me in developing this code.
 * http://stackoverflow.com/questions/5706038/long-press-definition-at-xml-layout-like-androidonclick-does
 * http://kevindion.com/2011/01/custom-xml-attributes-for-android-widgets/
 */

public class CustomButton extends AppCompatButton {
    private static final String TAG = "KanaButton";
    private static AudioManager audioManager;

    private OnClickListener onClickListener(final Method method, final String name) {
        return new OnClickListener() {
            @Override
            public void onClick(View v) {
                Object receiver = v.getContext();
                Class declaringClass = method.getDeclaringClass();
                Class thisClass = CustomButton.this.getClass();
                if (declaringClass.equals(thisClass)) {
                    receiver = CustomButton.this;
                }
                else if (receiver instanceof ContextWrapper)
                    receiver = ((ContextWrapper)receiver).getBaseContext();
                try {
                    method.invoke(receiver, v);
                }
                catch (Exception e) {
                    Log.d(TAG, "Invocation of " + name + " FAILED\nError = \""
                            + e.getMessage() + "\"");
                }
            }
        };
    }
    private OnLongClickListener onLongClickListener(final Method method, final String name) {
        return new OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                boolean rv = false;
                Object receiver = v.getContext();
                Class declaringClass = method.getDeclaringClass();
                Class thisClass = CustomButton.this.getClass();
                if (declaringClass.equals(thisClass)) {
                    receiver = CustomButton.this;
                }
                else if (receiver instanceof ContextWrapper)
                    receiver = ((ContextWrapper)receiver).getBaseContext();
                try {
                    rv = (boolean) method.invoke(receiver, v);
                }
                catch (Exception e) {
                    Log.d(TAG, "Invocation of " + name + " FAILED\nError = \""
                            + e.getMessage() + "\"");
                }
                return rv;
            }
        };
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
        Class [] actClass = new Class[2];
        try {
            String packageName = context.getApplicationContext().getPackageName();
            PackageManager pm = context.getPackageManager();
            ActivityInfo[] actInfo = pm.getPackageInfo(packageName, PackageManager.GET_ACTIVITIES).activities;
            String[] actNames = new String[actInfo.length];
            for (int i = 0; i < actNames.length; i++)
                actNames[i] = actInfo[i].name;
            if (actNames.length > 1)
                Log.d(TAG, "WARNING! Expected 1 activity name. Found " + actNames.length);
            actClass[0] = Class.forName(actNames[0]);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        actClass[1] = this.getClass();
        String methodName;
        for (int i = 0; i < n; i++) {
            int attr = a.getIndex(i);
            Log.w(TAG, String.format("button id:0x%04x", attr));
            methodName = a.getString(attr);
            Method method = null;
            if (null != methodName) {
                // Find a method by the name of "methodName" that expects a view.
                try {
                    for (int j = 0; j < actClass.length; j++) {
                        try {
                            method = actClass[j].getMethod(methodName, View.class);
                            break;
                        }
                        catch (NoSuchMethodException e) {
                            if (actClass.length - 1 == j) {
                                String mess = "Failed to find method match for " + methodName
                                        + "\n" + e.getMessage();
                                throw new NoSuchMethodException(mess);
                            }
                        }
                    }
                    Type rType = method.getGenericReturnType();
                    if (methodName.equals("onClick") && ! rType.equals(void.class))
                        throw new NoSuchMethodException(
                            "CustomButton: methodName = \""
                                + methodName + "\":expected return type void: Found "
                                + rType.toString());
                    else if (methodName.equals("onLongClick") && ! rType.equals(boolean.class))
                        throw new NoSuchMethodException(
                            "CustomButton: methodName = \""
                                + methodName + "\":expected return type boolean: Found "
                                + rType.toString());
                }
                catch (Exception e) {
                    Log.d(TAG, "Find method " + methodName
                            + " in " + actClass.toString() + " FAILED:\n"
                            + e.getMessage());
                    throw e;
                }
            }
            if (null != method) {
                if (attr == R.styleable.CustomButton_onClick) {
                    setOnClickListener(onClickListener(method, methodName));
                    Log.d(TAG, "button:" + methodName);
                }
                else if (attr == R.styleable.CustomButton_onLongClick) {
                    setOnLongClickListener(onLongClickListener(method, methodName));
                    Log.d(TAG, "button:" + methodName);
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
