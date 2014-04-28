package com.fusionx.lightirc.ui.preferences;

import com.fusionx.lightirc.R;
import com.fusionx.lightirc.util.UIUtils;

import android.content.Context;
import android.preference.Preference;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

/**
 * Hacky way to add a view to a settings fragment
 *
 * @author Lalit Maganti
 */
public class ViewPreference extends Preference {

    private TextView mTextView;

    private String mText;

    public ViewPreference(final Context context, final AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    public View getView(final View convertView, final ViewGroup parent) {
        if (convertView == null) {
            mTextView = (TextView) LayoutInflater.from(getContext()).inflate(R.layout
                    .must_be_complete_textview, null);
            UIUtils.setRobotoLight(getContext(), mTextView);
            mTextView.setText(mText);
            return mTextView;
        }
        return convertView;
    }

    public void setInitialText(final CharSequence text) {
        mText = String.format(getContext().getString(R.string.server_settings_non_empty), text);
        if (mTextView != null) {
            mTextView.setText(mText);
        }
    }
}