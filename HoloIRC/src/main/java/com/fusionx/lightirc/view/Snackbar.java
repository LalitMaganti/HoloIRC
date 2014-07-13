package com.fusionx.lightirc.view;

import com.fusionx.lightirc.R;

import android.content.Context;
import android.graphics.Color;
import android.os.Looper;
import android.util.AttributeSet;
import android.widget.TextView;

import java.util.concurrent.atomic.AtomicInteger;

public class Snackbar extends TextView {

    private final AtomicInteger mAtomicInteger = new AtomicInteger();

    public Snackbar(final Context context, final AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init() {
        setBackgroundColor(getResources().getColor(R.color.snackbar));
        setTextColor(Color.WHITE);
    }

    public void display(final String message) {
        final int value = mAtomicInteger.incrementAndGet();
        getHandler().postDelayed(new Runnable() {
            @Override
            public void run() {
                if (value == mAtomicInteger.get()) {
                    setVisibility(INVISIBLE);
                }
            }
        }, 3000);
        setText(message);
        setVisibility(VISIBLE);
    }
}