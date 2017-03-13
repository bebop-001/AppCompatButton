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

import android.app.Activity;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.media.AudioManager;
import android.support.v7.widget.AppCompatButton;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;

import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.ArrayList;


/*
 * these pages guided me in developing this code.
 * http://stackoverflow.com/questions/5706038/long-press-definition-at-xml-layout-like-androidonclick-does
 * http://kevindion.com/2011/01/custom-xml-attributes-for-android-widgets/
 */

@SuppressWarnings("ALL")
public class CustomButton extends AppCompatButton
        implements View.OnClickListener, View.OnLongClickListener {
    private static final String TAG = "KanaButton";
    private static AudioManager audioManager;

    // This is used to store data for the callback reflection handlers.
    // To make the handler work, we need three pieces of information:
    // 1)  The handler itself
    // 2)  The activity it's defined for.
    // 3)  its type i.e. onClick or onLongClick.
    static class HandlerData {
        //cache the handler info here.
        static final ArrayList<HandlerData> handlers = new ArrayList<>();
        // "this" variables.
        String type, activityName; int buttonId; Method method;
        // our constructor.
        HandlerData (String type, String activityName, int buttonId
                , Method method) {
            this.type = type; this.activityName = activityName;
            this.buttonId = buttonId; this.method = method;
            handlers.add(this);
        }
        // if the handler defined by the type, activity and id exist,
        // return it.  Otherwise, return null.
        static Method getHandler(String type, String activityName, int buttonId) {
            Method rv = null;
            for (HandlerData c : handlers) {
                if (c.activityName.equals(activityName) &&c.type.equals(type)
                        && c.buttonId == buttonId) {
                    rv = c.method;
                    break;
                }
            }
            return rv;
        }
        // to String for debugging.
        public String toString() {
            return String.format("HandlerData:type=\"%s\", "
                + "id=0x%04x,activity=%s"
                , this.type, this.buttonId, this.activityName);
        }
    }
    // the generic click handler.  So much of the code was the same
    // between onClick and onLongClick I just combined them.
    boolean clickHandler( String type, View v) {
        boolean rv = false;
        int buttonId = v.getId();
        Object receiver = v.getContext();
        if (receiver instanceof ContextWrapper) {
            receiver = ((ContextWrapper)receiver).getBaseContext();
        }
        @SuppressWarnings("ConstantConditions") String actName = ((Activity)receiver).getClass().getSimpleName();
        Method handler = HandlerData.getHandler(type, actName, buttonId);
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
            Log.d(TAG, String.format("button click:%s:%s:0x%04x",
                    type, actName, buttonId));
        }
        catch (Exception e) {
            throw new RuntimeException(String.format(
                    "CustomButton:%s: id=%04x, activity=\"%s\" invoke FAILED:\n+%s"
                    , type, buttonId, actName, e.getMessage())
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
        Activity act = (Activity)context;
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
            Log.w(TAG, String.format("button id:0x%04x", attrId));
            String methodType = ((R.styleable.CustomButton_onClick == attrId)
                ? "onClick"
                : ((R.styleable.CustomButton_onLongClick == attrId)
                    ? "onLongClick"
                    : null
                )
            );
            if (null != methodType) {
                String methodName = customButtonAttrs.getString(attrId);
                Resources r = customButtonAttrs.getResources();
                // Find a method by the name of "methodName" that expects a view.
                try {
                    Method method = null;
                    // search for the method in all the possible
                    // classes.  It will exist in at least one but
                    // may not be in all.  If it's not found,
                    // print a warning and move on.
                    try {
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
                    if (methodType.equals("onClick"))
                        setOnClickListener(this);
                    else
                        setOnLongClickListener(this);
                    Log.d(TAG,
                        new HandlerData(
                            methodType, actName, buttonId, method
                        ).toString());
                }
                catch (Exception e) {
                    Log.d(TAG, "Find method " + methodName
                            + " in " + actName + " FAILED:\n"
                            + e.getMessage());
                    throw e;
                }
            }
        }
        customButtonAttrs.recycle();
    }
    public CustomButton(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        Log.d(TAG, "got here 2.");
    }
}
