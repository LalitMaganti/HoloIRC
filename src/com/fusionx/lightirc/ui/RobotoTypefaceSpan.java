package com.fusionx.lightirc.ui;

import android.content.Context;
import android.graphics.Paint;
import android.text.TextPaint;
import android.text.style.TypefaceSpan;

public class RobotoTypefaceSpan extends TypefaceSpan {
    private final Context mContext;

    public RobotoTypefaceSpan(String family, final Context context) {
        super(family);
        mContext = context;
    }

    @Override
    public void updateDrawState(TextPaint ds) {
        applyCustomTypeFace(ds);
    }

    @Override
    public void updateMeasureState(TextPaint paint) {
        applyCustomTypeFace(paint);
    }

    private void applyCustomTypeFace(Paint paint) {
        paint.setTypeface(RobotoTypeface.getTypeface(mContext));
    }
}
