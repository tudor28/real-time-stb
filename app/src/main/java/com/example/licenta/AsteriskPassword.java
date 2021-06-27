package com.example.licenta;

public class AsteriskPassword {
    private CharSequence mSource;

    public AsteriskPassword(CharSequence source) {
        mSource = source; // Store char sequence
    }

    public char charAt(int index) {
        return '*'; // This is the important part
    }

    public int length() {
        return mSource.length(); // Return default
    }

    public CharSequence subSequence(int start, int end) {
        return mSource.subSequence(start, end); // Return default
    }
}
