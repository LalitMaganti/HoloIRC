package com.fusionx.lightirc.views;

import android.content.Context;
import android.preference.Preference;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.fusionx.common.Utils;
import com.fusionx.lightirc.R;

/**
 * Hacky way to add a view to a settings fragment
 *
 * @author Lalit Maganti
 */
public class MustBeCompleteView extends Preference {
    private TextView mTextView;
    private String mText;

    public MustBeCompleteView(final Context context, final AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    public View getView(final View convertView, final ViewGroup parent) {
        if (convertView == null) {
            mTextView = (TextView) LayoutInflater.from(getContext()).inflate(R.layout
                    .must_be_complete_textview, null);
            mTextView.setTypeface(Utils.getRobotoLightTypeface(getContext()));
            mTextView.setText(mText);
            return mTextView;
        }
        return convertView;
    }

    public void setInitialText(String text) {
        mText = String.format(getContext().getString(R.string.server_settings_non_empty), text);
        if (mTextView != null) {
            mTextView.setText(mText);
        }
    }

    public void setVisibility(int visibility) {
        mTextView.setVisibility(visibility);
    }
}