package com.example.sharinglists.utils;

public class TimeFormat {
    private static final int SECOND_MILLIS = 1000;
    private static final int MINUTE_MILLIS = 60 * SECOND_MILLIS;
    private static final int HOUR_MILLIS = 60 * MINUTE_MILLIS;
    private static final int DAY_MILLIS = 24 * HOUR_MILLIS;

    public static String getTimeAgo(long timestamp){

        if (timestamp < 1000000000000L) {
            timestamp *= SECOND_MILLIS;
        }

        long currentTime = System.currentTimeMillis();

        final long timeDiff = currentTime - timestamp;

        if (timeDiff < MINUTE_MILLIS){
            return "just now";
        } else if (timeDiff < 2 * MINUTE_MILLIS) {
            return "a minute ago";
        } else if (timeDiff < 50 * MINUTE_MILLIS){
            return (timeDiff / MINUTE_MILLIS + " minutes ago");
        } else if (timeDiff < 90 * MINUTE_MILLIS) {
            return "an hour ago";
        } else if (timeDiff < 24 * HOUR_MILLIS){
            return (timeDiff / HOUR_MILLIS + " hours ago");
        } else if (timeDiff < 48 * HOUR_MILLIS){
            return "yesterday";
        } else {
            return timeDiff / DAY_MILLIS + " days ago";
        }

    }
}
