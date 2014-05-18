package android.preference;

import com.fusionx.lightirc.R;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Parcelable;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;

public class DatabasePreference extends Preference {

    private final Preference mPreference;

    public DatabasePreference(final Context context, final AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);

        final TypedArray a = context.obtainStyledAttributes(attrs,
                R.styleable.DatabasePreference, 0, 0);
        final String classString = a.getString(R.styleable.DatabasePreference_preference);
        a.recycle();

        try {
            mPreference = (Preference) Class.forName(classString).newInstance();
        } catch (ClassNotFoundException | InstantiationException | IllegalAccessException e) {
            throw new IllegalArgumentException(e);
        }
    }

    public DatabasePreference(final Context context, final AttributeSet attrs) {
        super(context, attrs);

        final TypedArray a = context.obtainStyledAttributes(attrs,
                R.styleable.DatabasePreference, 0, 0);
        final String classString = a.getString(R.styleable.DatabasePreference_preference);
        a.recycle();

        try {
            mPreference = (Preference) Class.forName(classString).newInstance();
        } catch (ClassNotFoundException | InstantiationException | IllegalAccessException e) {
            throw new IllegalArgumentException(e);
        }
    }

    public Intent getIntent() {
        return mPreference.getIntent();
    }

    public void setIntent(Intent intent) {
        mPreference.setIntent(intent);
    }

    public String getFragment() {
        return mPreference.getFragment();
    }

    public void setFragment(String fragment) {
        mPreference.setFragment(fragment);
    }

    public Bundle getExtras() {
        return mPreference.getExtras();
    }

    public Bundle peekExtras() {
        return mPreference.peekExtras();
    }

    public int getLayoutResource() {
        return mPreference.getLayoutResource();
    }

    public void setLayoutResource(int layoutResId) {
        mPreference.setLayoutResource(layoutResId);
    }

    public int getWidgetLayoutResource() {
        return mPreference.getWidgetLayoutResource();
    }

    public void setWidgetLayoutResource(int widgetLayoutResId) {
        mPreference.setWidgetLayoutResource(widgetLayoutResId);
    }

    public View getView(View convertView, ViewGroup parent) {
        return mPreference.getView(convertView, parent);
    }

    public int getOrder() {
        return mPreference.getOrder();
    }

    public void setOrder(int order) {
        mPreference.setOrder(order);
    }

    public void setTitle(CharSequence title) {
        mPreference.setTitle(title);
    }

    public int getTitleRes() {
        return mPreference.getTitleRes();
    }

    public CharSequence getTitle() {
        return mPreference.getTitle();
    }

    public void setTitle(int titleResId) {
        mPreference.setTitle(titleResId);
    }

    public void setIcon(Drawable icon) {
        mPreference.setIcon(icon);
    }

    public Drawable getIcon() {
        return mPreference.getIcon();
    }

    public void setIcon(int iconResId) {
        mPreference.setIcon(iconResId);
    }

    public CharSequence getSummary() {
        return mPreference.getSummary();
    }

    public void setSummary(int summaryResId) {
        mPreference.setSummary(summaryResId);
    }

    public void setSummary(CharSequence summary) {
        mPreference.setSummary(summary);
    }

    public boolean isEnabled() {
        return mPreference.isEnabled();
    }

    public void setEnabled(boolean enabled) {
        mPreference.setEnabled(enabled);
    }

    public boolean isSelectable() {
        return mPreference.isSelectable();
    }

    public void setSelectable(boolean selectable) {
        mPreference.setSelectable(selectable);
    }

    public boolean getShouldDisableView() {
        return mPreference.getShouldDisableView();
    }

    public void setShouldDisableView(boolean shouldDisableView) {
        mPreference.setShouldDisableView(shouldDisableView);
    }

    public String getKey() {
        return mPreference.getKey();
    }

    public void setKey(String key) {
        mPreference.setKey(key);
    }

    public boolean hasKey() {
        return mPreference.hasKey();
    }

    public boolean isPersistent() {
        return mPreference.isPersistent();
    }

    public void setPersistent(boolean persistent) {
        mPreference.setPersistent(persistent);
    }

    public OnPreferenceChangeListener getOnPreferenceChangeListener() {
        return mPreference.getOnPreferenceChangeListener();
    }

    public void setOnPreferenceChangeListener(
            OnPreferenceChangeListener onPreferenceChangeListener) {
        mPreference.setOnPreferenceChangeListener(onPreferenceChangeListener);
    }

    public OnPreferenceClickListener getOnPreferenceClickListener() {
        return mPreference.getOnPreferenceClickListener();
    }

    public void setOnPreferenceClickListener(
            OnPreferenceClickListener onPreferenceClickListener) {
        mPreference.setOnPreferenceClickListener(onPreferenceClickListener);
    }

    public Context getContext() {
        return mPreference.getContext();
    }

    public SharedPreferences getSharedPreferences() {
        return mPreference.getSharedPreferences();
    }

    public SharedPreferences.Editor getEditor() {
        return mPreference.getEditor();
    }

    public boolean shouldCommit() {
        return mPreference.shouldCommit();
    }

    public int compareTo(Preference another) {
        return mPreference.compareTo(another);
    }

    public PreferenceManager getPreferenceManager() {
        return mPreference.getPreferenceManager();
    }

    public void notifyDependencyChange(boolean disableDependents) {
        mPreference.notifyDependencyChange(disableDependents);
    }

    public void onDependencyChanged(Preference dependency,
            boolean disableDependent) {
        mPreference.onDependencyChanged(dependency, disableDependent);
    }

    public void onParentChanged(Preference parent, boolean disableChild) {
        mPreference.onParentChanged(parent, disableChild);
    }

    public boolean shouldDisableDependents() {
        return mPreference.shouldDisableDependents();
    }

    public String getDependency() {
        return mPreference.getDependency();
    }

    public void setDependency(String dependencyKey) {
        mPreference.setDependency(dependencyKey);
    }

    public void setDefaultValue(Object defaultValue) {
        mPreference.setDefaultValue(defaultValue);
    }

    public String toString() {
        return mPreference.toString();
    }

    public void saveHierarchyState(Bundle container) {
        mPreference.saveHierarchyState(container);
    }

    public void restoreHierarchyState(Bundle container) {
        mPreference.restoreHierarchyState(container);
    }

    protected Object onGetDefaultValue(TypedArray a, int index) {
        return mPreference.onGetDefaultValue(a, index);
    }

    protected View onCreateView(ViewGroup parent) {
        return mPreference.onCreateView(parent);
    }

    protected void onBindView(final View view) {
        mPreference.onBindView(view);
    }

    protected void onClick() {
        mPreference.onClick();
    }

    protected boolean shouldPersist() {
        return mPreference.shouldPersist();
    }

    protected boolean callChangeListener(Object newValue) {
        return mPreference.callChangeListener(newValue);
    }

    protected void notifyChanged() {
        mPreference.notifyChanged();
    }

    protected void notifyHierarchyChanged() {
        mPreference.notifyHierarchyChanged();
    }

    protected void onAttachedToHierarchy(PreferenceManager preferenceManager) {
        mPreference.onAttachedToHierarchy(preferenceManager);
    }

    protected void onAttachedToActivity() {
        mPreference.onAttachedToActivity();
    }

    protected Preference findPreferenceInHierarchy(String key) {
        return mPreference.findPreferenceInHierarchy(key);
    }

    protected void onPrepareForRemoval() {
        mPreference.onPrepareForRemoval();
    }

    protected void onSetInitialValue(boolean restorePersistedValue, Object defaultValue) {
        mPreference.onSetInitialValue(restorePersistedValue, defaultValue);
    }

    protected boolean persistString(String value) {
        return mPreference.persistString(value);
    }

    protected String getPersistedString(String defaultReturnValue) {
        return mPreference.getPersistedString(defaultReturnValue);
    }

    protected boolean persistInt(int value) {
        return mPreference.persistInt(value);
    }

    protected int getPersistedInt(int defaultReturnValue) {
        return mPreference.getPersistedInt(defaultReturnValue);
    }

    protected boolean persistFloat(float value) {
        return mPreference.persistFloat(value);
    }

    protected float getPersistedFloat(float defaultReturnValue) {
        return mPreference.getPersistedFloat(defaultReturnValue);
    }

    protected boolean persistLong(long value) {
        return mPreference.persistLong(value);
    }

    protected long getPersistedLong(long defaultReturnValue) {
        return mPreference.getPersistedLong(defaultReturnValue);
    }

    protected boolean persistBoolean(boolean value) {
        return mPreference.persistBoolean(value);
    }

    protected boolean getPersistedBoolean(boolean defaultReturnValue) {
        return mPreference.getPersistedBoolean(defaultReturnValue);
    }

    protected Parcelable onSaveInstanceState() {
        return mPreference.onSaveInstanceState();
    }

    protected void onRestoreInstanceState(Parcelable state) {
        mPreference.onRestoreInstanceState(state);
    }
}