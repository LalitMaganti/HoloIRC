package com.fusionx.lightirc.ui.preferences;

import com.fusionx.lightirc.R;

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

    private final LayoutInflater mLayoutInflater;

    private TextView mTextView;

    private String mText;

    public ViewPreference(final Context context, final AttributeSet attrs) {
        super(context, attrs);

        mLayoutInflater = LayoutInflater.from(getContext());
    }

    @Override
    public View getView(final View convertView, final ViewGroup parent) {
        if (convertView == null) {
            mTextView = (TextView) mLayoutInflater.inflate(R.layout.must_be_complete_textview,
                    parent, false);
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