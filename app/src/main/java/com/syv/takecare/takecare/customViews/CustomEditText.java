package com.syv.takecare.takecare.customViews;

import android.content.Context;
import android.graphics.drawable.Drawable;
import androidx.appcompat.widget.AppCompatEditText;
import android.util.AttributeSet;

public class CustomEditText extends AppCompatEditText {



    public CustomEditText(Context context){
        super(context);
    }

    public CustomEditText(Context context, AttributeSet attrs) {
        super(context, attrs);
    }
    public CustomEditText(Context context, AttributeSet attrs, int defStyleAttrs) {
        super(context, attrs,defStyleAttrs);
    }

    @Override
    public void setError(CharSequence error, Drawable icon) {
        if (error == null) {
            super.setError(null, icon);
            setCompoundDrawables(null, null, null, null);
        }
        else if (error.toString().equals(""))
            setCompoundDrawables(null, null, icon, null);
        else super.setError(error, icon);
    }
}
