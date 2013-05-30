package com.fusionx.lightirc.misc;


import java.util.Comparator;

public class UserComparator implements Comparator<String> {
    @Override
    public int compare(String s, String s2) {
        if (s.startsWith(s2.substring(0, 1)) && (s.substring(0, 1).equals("@")
                || s.substring(0, 1).equals("+"))) {
            return 0;
        } else if (s.startsWith("@")) {
            return 1;
        } else if (s2.startsWith("@")) {
            return -1;
        } else if (s.startsWith("+")) {
            return 1;
        } else if (s2.startsWith("+")) {
            return -1;
        }
        return s.compareTo(s2);
    }
}