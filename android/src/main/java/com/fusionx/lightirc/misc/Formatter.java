package com.fusionx.lightirc.misc;

import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.style.CharacterStyle;

import java.util.DuplicateFormatFlagsException;
import java.util.FormatterClosedException;
import java.util.Locale;
import java.util.MissingFormatArgumentException;
import java.util.UnknownFormatConversionException;

public final class Formatter {

    // User-settable parameters.
    private SpannableStringBuilder out;

    private Locale locale;

    public Formatter() {
        out = new SpannableStringBuilder();
        locale = Locale.getDefault();
    }

    /**
     * Returns the output destination of the {@code Formatter}.
     *
     * @return the output destination of the {@code Formatter}.
     * @throws FormatterClosedException if the {@code Formatter} has been closed.
     */
    public Spanned getFormattedString() {
        return out;
    }

    /**
     * Returns the content by calling the {@code toString()} method of the output
     * destination.
     *
     * @return the content by calling the {@code toString()} method of the output
     * destination.
     * @throws FormatterClosedException if the {@code Formatter} has been closed.
     */
    @Override
    public String toString() {
        return out.toString();
    }

    /**
     * Writes a formatted string to the output destination of the {@code Formatter}.
     *
     * @param format a format string.
     * @param args   the arguments list used in the {@code format()} method. If there are
     *               more arguments than those specified by the format string, then
     *               the additional arguments are ignored.
     * @return this {@code Formatter}.
     * @throws java.util.IllegalFormatException if the format string is illegal or incompatible
     *                                          with
     *                                          the
     *                                          arguments, or if fewer arguments are sent than
     *                                          those
     *                                          required by
     *                                          the format string, or any other illegal situation.
     * @throws FormatterClosedException         if the {@code Formatter} has been closed.
     */
    public Formatter format(final String format, final FormattedString... args) {
        return format(this.locale, format, args);
    }

    /**
     * Writes a formatted string to the output destination of the {@code Formatter}.
     *
     * @param l      the {@code Locale} used in the method. If {@code locale} is
     *               {@code null}, then no localization will be applied. This
     *               parameter does not change this Formatter's default {@code Locale}
     *               as specified during construction, and only applies for the
     *               duration of this call.
     * @param format a format string.
     * @param args   the arguments list used in the {@code format()} method. If there are
     *               more arguments than those specified by the format string, then
     *               the additional arguments are ignored.
     * @return this {@code Formatter}.
     * @throws java.util.IllegalFormatException if the format string is illegal or incompatible
     *                                          with
     *                                          the
     *                                          arguments, or if fewer arguments are sent than
     *                                          those
     *                                          required by
     *                                          the format string, or any other illegal situation.
     * @throws FormatterClosedException         if the {@code Formatter} has been closed.
     */
    public Formatter format(Locale l, String format, FormattedString... args) {
        Locale originalLocale = locale;
        try {
            this.locale = (l == null ? Locale.US : l);
            doFormat(format, args);
        } finally {
            this.locale = originalLocale;
        }
        return this;
    }

    private void doFormat(String format, FormattedString... args) {
        FormatSpecifierParser fsp = new FormatSpecifierParser(format);
        int currentObjectIndex = 0;
        FormattedString lastArgument = null;
        boolean hasLastArgumentSet = false;

        int length = format.length();
        int i = 0;
        while (i < length) {
            // Find the maximal plain-text sequence...
            int plainTextStart = i;
            int nextPercent = format.indexOf('%', i);
            int plainTextEnd = (nextPercent == -1) ? length : nextPercent;
            // ...and output it.
            if (plainTextEnd > plainTextStart) {
                outputCharSequence(format, plainTextStart, plainTextEnd);
            }
            i = plainTextEnd;
            // Do we have a format specifier?
            if (i < length) {
                FormatToken token = fsp.parseFormatToken(i + 1);

                FormattedString argument = null;
                if (token.requireArgument()) {
                    int index = token.getArgIndex() == FormatToken.UNSET ? currentObjectIndex++
                            : token.getArgIndex();
                    argument = getArgument(args, index, fsp, lastArgument, hasLastArgumentSet);
                    lastArgument = argument;
                    hasLastArgumentSet = true;
                }

                CharSequence substitution = transform(token, argument);
                // The substitution is null if we called Formattable.formatTo.
                if (substitution != null) {
                    outputCharSequence(substitution, 0, substitution.length());
                }
                i = fsp.i;
            }
        }
    }

    // Fixes http://code.google.com/p/android/issues/detail?id=1767.
    private void outputCharSequence(CharSequence cs, int start, int end) {
        out.append(cs, start, end);
    }

    private FormattedString getArgument(FormattedString[] args, int index,
            FormatSpecifierParser fsp,
            FormattedString lastArgument, boolean hasLastArgumentSet) {
        if (index == FormatToken.LAST_ARGUMENT_INDEX && !hasLastArgumentSet) {
            throw new MissingFormatArgumentException("<");
        }

        if (args == null) {
            return null;
        }

        if (index >= args.length) {
            throw new MissingFormatArgumentException(fsp.getFormatSpecifierText());
        }

        if (index == FormatToken.LAST_ARGUMENT_INDEX) {
            return lastArgument;
        }

        return args[index];
    }

    /*
     * Gets the formatted string according to the format token and the
     * argument.
     */
    private CharSequence transform(FormatToken token, FormattedString argument) {
        // There are only two format specifiers that matter: "%d" and "%s".
        // Nothing else is common in the wild. We fast-path these two to
        // avoid the heavyweight machinery needed to cope with flags, width,
        // and precision.
        if (token.isDefault()) {
            switch (token.getConversionType()) {
                case 's':
                    if (argument == null) {
                        return "null";
                    } else {
                        final CharacterStyle style = argument.getCharacterStyle();
                        final SpannableStringBuilder builder = new SpannableStringBuilder
                                (argument.getString());
                        builder.setSpan(style, 0, argument.getString().length(),
                                Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                        return builder;
                    }
            }
        }
        throw new UnsupportedOperationException();
    }

    /*
     * Complete details of a single format specifier parsed from a format string.
     */
    private static class FormatToken {

        static final int LAST_ARGUMENT_INDEX = -2;

        static final int UNSET = -1;

        private int argIndex = UNSET;

        private char conversionType = (char) UNSET;

        private int precision = UNSET;

        private int width = UNSET;

        // These have package access for performance. They used to be represented by an int bitmask
        // and accessed via methods, but Android's JIT doesn't yet do a good job of such code.
        // Direct field access, on the other hand, is fast.
        boolean flagComma;

        boolean flagMinus;

        boolean flagParenthesis;

        boolean flagPlus;

        boolean flagSharp;

        boolean flagSpace;

        boolean flagZero;

        private StringBuilder strFlags;

        // Tests whether there were no flags, no width, and no precision specified.
        boolean isDefault() {
            return !flagComma && !flagMinus && !flagParenthesis && !flagPlus && !flagSharp &&
                    !flagSpace && !flagZero && width == UNSET && precision == UNSET;
        }

        int getArgIndex() {
            return argIndex;
        }

        void setArgIndex(int index) {
            argIndex = index;
        }

        /*
         * Sets qualified char as one of the flags. If the char is qualified,
         * sets it as a flag and returns true. Or else returns false.
         */
        boolean setFlag(int ch) {
            boolean dupe;
            switch (ch) {
                case ',':
                    dupe = flagComma;
                    flagComma = true;
                    break;
                case '-':
                    dupe = flagMinus;
                    flagMinus = true;
                    break;
                case '(':
                    dupe = flagParenthesis;
                    flagParenthesis = true;
                    break;
                case '+':
                    dupe = flagPlus;
                    flagPlus = true;
                    break;
                case '#':
                    dupe = flagSharp;
                    flagSharp = true;
                    break;
                case ' ':
                    dupe = flagSpace;
                    flagSpace = true;
                    break;
                case '0':
                    dupe = flagZero;
                    flagZero = true;
                    break;
                default:
                    return false;
            }
            if (dupe) {
                // The RI documentation implies we're supposed to report all the flags, not just
                // the first duplicate, but the RI behaves the same as we do.
                throw new DuplicateFormatFlagsException(String.valueOf(ch));
            }
            if (strFlags == null) {
                strFlags = new StringBuilder(7); // There are seven possible flags.
            }
            strFlags.append((char) ch);
            return true;
        }

        char getConversionType() {
            return conversionType;
        }

        void setConversionType(char c) {
            conversionType = c;
        }

        boolean requireArgument() {
            return conversionType != '%' && conversionType != 'n';
        }
    }

    private static class FormatSpecifierParser {

        private String format;

        private int length;

        private int startIndex;

        private int i;

        /**
         * Constructs a new parser for the given format string.
         */
        FormatSpecifierParser(String format) {
            this.format = format;
            this.length = format.length();
        }

        /**
         * Returns a FormatToken representing the format specifier starting at 'offset'.
         *
         * @param offset the first character after the '%'
         */
        FormatToken parseFormatToken(int offset) {
            this.startIndex = offset;
            this.i = offset;
            return parseArgumentIndexAndFlags(new FormatToken());
        }

        /**
         * Returns a string corresponding to the last format specifier that was parsed.
         * Used to construct error messages.
         */
        String getFormatSpecifierText() {
            return format.substring(startIndex, i);
        }

        private int peek() {
            return (i < length) ? format.charAt(i) : -1;
        }

        private char advance() {
            if (i >= length) {
                throw unknownFormatConversionException();
            }
            return format.charAt(i++);
        }

        private UnknownFormatConversionException unknownFormatConversionException() {
            throw new UnknownFormatConversionException(getFormatSpecifierText());
        }

        private FormatToken parseArgumentIndexAndFlags(FormatToken token) {
            // Parse the argument index, if there is one.
            int ch = peek();
            if (Character.isDigit(ch)) {
                int number = nextInt();
                if (peek() == '$') {
                    // The number was an argument index.
                    advance(); // Swallow the '$'.
                    if (number == FormatToken.UNSET) {
                        throw new MissingFormatArgumentException(getFormatSpecifierText());
                    }
                    // k$ stands for the argument whose index is k-1 except that
                    // 0$ and 1$ both stand for the first element.
                    token.setArgIndex(Math.max(0, number - 1));
                }
            } else if (ch == '<') {
                token.setArgIndex(FormatToken.LAST_ARGUMENT_INDEX);
                advance();
            }

            // Parse the flags.
            while (token.setFlag(peek())) {
                advance();
            }
            return parseConversionType(token);
        }

        private FormatToken parseConversionType(FormatToken token) {
            char conversionType = advance(); // A conversion type is mandatory.
            token.setConversionType(conversionType);
            return token;
        }

        // Parses an integer (of arbitrary length, but typically just one digit).
        private int nextInt() {
            long value = 0;
            while (i < length && Character.isDigit(format.charAt(i))) {
                value = 10 * value + (format.charAt(i++) - '0');
                if (value > Integer.MAX_VALUE) {
                    return failNextInt();
                }
            }
            return (int) value;
        }

        // Swallow remaining digits to resync our attempted parse, but return failure.
        private int failNextInt() {
            while (Character.isDigit(peek())) {
                advance();
            }
            return FormatToken.UNSET;
        }
    }
}