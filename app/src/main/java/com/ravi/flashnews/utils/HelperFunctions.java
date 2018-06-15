package com.ravi.flashnews.utils;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class HelperFunctions {

    public static String getViewableDate(String dateString) {
        try {
            DateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.ENGLISH);
            Date complexDate = df.parse(dateString);
            SimpleDateFormat showFormat = new SimpleDateFormat("dd MMM, yyyy", Locale.ENGLISH);
            return showFormat.format(complexDate);
        } catch (ParseException pex) {
//            pex.printStackTrace();
        }
        return "";
    }

    public static boolean isValidResponse(JSONObject jsonData) throws JSONException {
        return jsonData.getString(JsonKeys.STATUS_KEY).equals(JsonKeys.OK_KEY);
    }
}
