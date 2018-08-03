package com.dylan.common.observer;

import android.content.Context;
import android.database.ContentObserver;
import android.database.Cursor;
import android.net.Uri;
import android.os.Handler;
import android.text.TextUtils;
import android.util.Log;
import android.widget.EditText;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
<uses-permission android:name="android.permission.RECEIVE_SMS" />
<uses-permission android:name="android.permission.READ_SMS" />
<uses-permission android:name="android.permission.SEND_SMS" />
<uses-permission android:name="android.permission.WRITE_SMS" />
**/
public class ObserveSms extends ContentObserver {
    public interface OnSmsReceived {
        void onSms(String from, String body);
    }

    private Context context = null;
    private EditText editText = null;
    private OnSmsReceived smsReceived = null;
    private String[] whereValue = null;
    private String whereCause = null;

    public static ObserveSms beginObserve(Context context, EditText edittext, String fromPhone, String filter) {
        ObserveSms observer = new ObserveSms(context, new Handler(), edittext, fromPhone, filter);
        context.getContentResolver().registerContentObserver(Uri.parse("content://sms/"), true, observer);
        return observer;
    }
    public static ObserveSms beginObserve(Context context, OnSmsReceived smsReceived, String fromPhone, String filter) {
        ObserveSms observer = new ObserveSms(context, new Handler(), smsReceived, fromPhone, filter);
        context.getContentResolver().registerContentObserver(Uri.parse("content://sms/"), true, observer);
        return observer;
    }
    public static void endObserve(Context context, ObserveSms observer) {
        if (observer == null) return;
        context.getContentResolver().unregisterContentObserver(observer);
    }

    protected ObserveSms(Context context, Handler handler, EditText edittext, String fromPhone, String filter) {
        super(handler);
        this.context = context;
        this.editText = edittext;
        buildWhereCause(fromPhone, filter);
    }
    protected ObserveSms(Context context, Handler handler, OnSmsReceived smsReceived, String fromPhone, String filter) {
        super(handler);
        this.context = context;
        this.smsReceived = smsReceived;
        buildWhereCause(fromPhone, filter);
    }
    private void buildWhereCause(String fromPhone, String filter) {
        List<String> value = new ArrayList<String>() {{
            add("0");
        }};
        String where = "read=?";
        if (!TextUtils.isEmpty(fromPhone)) {
            where += " and address=?";
            value.add(fromPhone);
        }
        if (!TextUtils.isEmpty(filter)) {
            where += " and body like ?";
            value.add(filter);
        }
        this.whereCause = where;
        this.whereValue = value.toArray(new String[value.size()]);
        Log.e("dylan", "cause=" + where);
        Log.e("dylan", "value=" + this.whereValue.toString());
    }

    @Override
    public void onChange(boolean selfChange) {
        super.onChange(selfChange);
        try {
            Cursor cursor = context.getContentResolver().query(Uri.parse("content://sms/inbox"),
                    new String[]{"_id", "address", "read", "body"}, "body like '%[新津]%'",
                    null, "_id desc");
            if (cursor != null && cursor.getCount() > 0) {
                if (cursor.moveToFirst()) {
                    String body = cursor.getString(cursor.getColumnIndex("body"));
                    String from = cursor.getString(cursor.getColumnIndex("address"));
                    if (this.editText != null) {
                        String regEx = "(?<![0-9])([0-9]{" + 6 + "})(?![0-9])";
                        Pattern p = Pattern.compile(regEx);
                        Matcher m = p.matcher(body);
                        while (m.find()) {
                            String code = m.group();
                            editText.setText(code);
                        }
                    } else if (smsReceived != null){
                        smsReceived.onSms(from, body);
                    }
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
}
