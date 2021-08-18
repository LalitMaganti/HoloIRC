package app.holoirc.misc;

public enum Theme {
    LIGHT(100),
    DARK(350);

    private final int mTextColourOffset;

    private Theme(final int i) {
        mTextColourOffset = i;
    }

    public int getTextColourOffset() {
        return mTextColourOffset;
    }
}