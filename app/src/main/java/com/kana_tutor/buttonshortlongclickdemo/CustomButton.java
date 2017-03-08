package com.kana_tutor.buttonshortlongclickdemo;

import android.annotation.SuppressLint;
import android.content.Context;
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


/**
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
                try {
                    method.invoke(CustomButton.this, v);
                } catch (Exception e) {
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
                try {
                    rv = (boolean) method.invoke(CustomButton.this, v);
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
    public CustomButton(Context context, AttributeSet attrs) throws NoSuchMethodException {
        super(context, attrs);
        audioManager = (AudioManager)context.getSystemService(Context.AUDIO_SERVICE);
        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.CustomButton);
        final int n = a.getIndexCount();
        String method;
        for (int i = 0; i < n; i++) {
            int attr = a.getIndex(i);
            Log.w(TAG, String.format("button id:0x%04x", attr));
            method = a.getString(attr);
            Method handler = null;
            if (null != method) {
                // Find a method by the name of "method" that expects a view.
                handler = getClass().getMethod(method, View.class);
                Type rType = handler.getGenericReturnType();
                if (method.equals("onClick") && ! rType.equals(void.class))
                    throw new NoSuchMethodException(
                        "CustomButton: method = \""
                            + method + "\":expected return type void: Found "
                            + rType.toString());
                else if (method.equals("onLongClick") && ! rType.equals(boolean.class))
                    throw new NoSuchMethodException(
                        "CustomButton: method = \""
                            + method + "\":expected return type boolean: Found "
                            + rType.toString());
            }
            if (null != handler) {
                Type rType = handler.getGenericReturnType();
                if (attr == R.styleable.CustomButton_onClick) {
                    if (! rType.equals(void.class))
                        throw new IllegalArgumentException(
                            "CustomButton: method = \""
                            + method + "\":expected return type void: Found "
                            + rType.toString());
                    setOnClickListener(onClickListener(handler, method));
                    Log.d(TAG, "button:" + method);
                }
                else if (attr == R.styleable.CustomButton_onLongClick) {
                    if (! rType.equals(boolean.class))
                        throw new IllegalArgumentException(
                            "CustomButton: method = \""
                            + method + "\":expected return type boolean: Found "
                            + rType.toString());
                    setOnLongClickListener(onLongClickListener(handler, method));
                    Log.d(TAG, "button:" + method);
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
