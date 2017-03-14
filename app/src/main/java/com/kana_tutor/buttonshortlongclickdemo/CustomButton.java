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

import android.content.Context;
import android.content.ContextWrapper;
import android.content.res.TypedArray;
import android.media.AudioManager;
import android.support.v7.widget.AppCompatButton;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;

import java.lang.reflect.InvocationTargetException;
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

    private OnClickListener getOnClickListener (final Method method) {
        return new OnClickListener() {
            @Override
            public void onClick(View v) {
                Object receiver = v.getContext();
                if (receiver instanceof ContextWrapper) {
                    receiver = ((ContextWrapper)receiver)
                        .getBaseContext();
                }
                //noinspection TryWithIdenticalCatches
                try {
                    method.invoke(receiver, v);
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                } catch (InvocationTargetException e) {
                    e.printStackTrace();
                }
            }
        };
    }

    private OnLongClickListener getOnLongClickListener (final Method method) {
        return new OnLongClickListener() {
            @SuppressWarnings("TryWithIdenticalCatches")
            @Override
            public boolean onLongClick(View v) {
                boolean rv = false;
                Object receiver = v.getContext();
                if (receiver instanceof ContextWrapper) {
                    receiver = ((ContextWrapper)receiver)
                        .getBaseContext();
                }
                try {
                    audioManager.playSoundEffect(AudioManager.FX_KEY_CLICK);
                    rv = (boolean) method.invoke(receiver, v);
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                } catch (InvocationTargetException e) {
                    e.printStackTrace();
                }
                return rv;
            }
        };
    }

    /******************************************************************
     * Class Constructors.
     ******************************************************************/
    public CustomButton(Context context) {
        super(context);
        Log.d(TAG, "got here 0.");
    }
    public CustomButton(Context context, AttributeSet attrs)
            throws NoSuchMethodException, ClassNotFoundException {
        super(context, attrs);
        Class actClass = context.getClass();
        String actName = actClass.getSimpleName();
        // Id of the button we're configuring.  This is the same as
        // the view id passed into the button callback and is used
        // to tie things together.
        int buttonId = this.getId();

        Log.d(TAG, String.format("constructor: activity: %s, id = 0x%08x"
                , actName, buttonId));
        // used so LongClick has key clicks just like onClick.
        audioManager = (AudioManager)context
            .getSystemService(Context.AUDIO_SERVICE);
        TypedArray customButtonAttrs = context
            .obtainStyledAttributes(attrs, R.styleable.CustomButton);

        // when building our handler, we need it's class.  This is
        // actually part of the handler when its constructed.  Activity
        // short name is used when the callback is called so we
        final int buttonAttrCount = customButtonAttrs.getIndexCount();
        for (int i = 0; i < buttonAttrCount; i++) {
            int attrId = customButtonAttrs.getIndex(i);
            Log.w(TAG, String.format("button id:0x%04x", buttonId));
            String methodType;
            if (R.styleable.CustomButton_onClick == attrId)
                methodType = "onClick";
            else if (R.styleable.CustomButton_onLongClick == attrId)
                methodType = "onLongClick";
            else throw new NoSuchMethodException(
                String.format(
                    "Unrecognized CustomButton attribute. id=0x%08x"
                    , attrId
                )
            );
            String methodName = customButtonAttrs.getString(attrId);
            Method method;
            try {
                //noinspection unchecked
                method = actClass
                    .getMethod(methodName, View.class);
            }
            catch (NoSuchMethodException e) {
                Log.d(TAG, String.format(
                    "Failed to find method \"%s\" "
                    + "in activity \"%s\"\nError:%s",
                    methodName, actName, e.getMessage()));
                continue;
            }
            Type rType = method.getGenericReturnType();
            // Make sure the return on the caller supplied
            // value is correct.
            if (methodType.equals("onClick")
                    && ! rType.equals(void.class))
                throw new NoSuchMethodException(
                    "CustomButton: methodName = \"" + methodName
                    + "\":expected return type void: Found "
                    + rType.toString());
            else if (methodType.equals("onLongClick")
                    && ! rType.equals(boolean.class))
                throw new NoSuchMethodException(
                    "CustomButton: methodName = \"" + methodName
                    + "\":expected return type boolean: Found "
                    + rType.toString());
            // We overload OnClickListener and OnLongClickListener.
            // setOnClick associates our OnClickListener's with our
            // class so our onClick listeners will get called.
            if (methodType.equals("onClick")) {
                Log.w(TAG, String.format("onClick id:0x%04x", buttonId));
                setOnClickListener(getOnClickListener(method));
            }
            else  {
                Log.w(TAG, String.format("onLongClick id:0x%04x", buttonId));
                setOnLongClickListener(getOnLongClickListener(method));
            }
        }
        customButtonAttrs.recycle();
    }
    public CustomButton(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        Log.d(TAG, "got here 2.");
    }
}
