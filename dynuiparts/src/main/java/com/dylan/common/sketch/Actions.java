package com.dylan.common.sketch;

import java.io.File;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Fragment;
import android.app.SearchManager;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Parcelable;
import android.provider.Contacts;
import android.provider.DocumentsContract;
import android.provider.MediaStore;

import com.dylan.common.utils.Utility;

@SuppressWarnings("deprecation")
public class Actions {

	public static void openUrl(Context ctx, String url) {
		Intent intent = new Intent(Intent.ACTION_VIEW);
		intent.setData(Uri.parse(url));
		ctx.startActivity(intent);
	}
	public static void dial(Context ctx, String telphone) {
		Uri uri = Uri.parse("tel:" + telphone);   
		Intent intent = new Intent(Intent.ACTION_DIAL, uri);     
		ctx.startActivity(intent);  
	}
	public static void call(Context ctx, String telphone) {
		Uri uri = Uri.parse("tel:" + telphone);   
		Intent intent = new Intent(Intent.ACTION_CALL, uri);     
		ctx.startActivity(intent);  
	}
	public static void sms(Context ctx) {
		Intent intent = new Intent(Intent.ACTION_VIEW);  
        intent.setType("vnd.android-dir/mms-sms");      
		ctx.startActivity(intent);  
	}
	public static void sms(Context ctx, String content) {
		Intent intent = new Intent(Intent.ACTION_VIEW);   
		intent.putExtra("sms_body", content); 
        intent.setType("vnd.android-dir/mms-sms");      
		ctx.startActivity(intent);  
	}
	public static void sms(Context ctx, String content, String phone) {
		Uri uri = Uri.parse("smsto:" + phone);    
        Intent intent = new Intent(Intent.ACTION_SENDTO,uri); 
		intent.putExtra("sms_body", content);   
		ctx.startActivity(intent);  
	}
	public static void contact(Context ctx) {
		Intent intent = new Intent();   
		intent.setAction(Intent.ACTION_VIEW);   
		intent.setData(Contacts.People.CONTENT_URI);   
		ctx.startActivity(intent); 
	}
	public static void google(Context ctx, String keywords) {
		Intent intent = new Intent();   
		intent.setAction(Intent.ACTION_WEB_SEARCH);   
		intent.putExtra(SearchManager.QUERY, keywords);     
		ctx.startActivity(intent);  
	}
	public static void map(Context ctx, String gps) {
		Uri uri = Uri.parse("geo:" + gps);   
		Intent intent = new Intent(Intent.ACTION_VIEW, uri);    
		ctx.startActivity(intent);  
	}
	public static void map(Context ctx, double longitude, double latitude) {
		Uri uri = Uri.parse("geo:" + latitude + "," + longitude);   
		Intent intent = new Intent(Intent.ACTION_VIEW, uri);    
		ctx.startActivity(intent);  
	}
	public static void playMp3(Context ctx, String url) {
		Intent intent = new Intent(Intent.ACTION_VIEW);   
		Uri uri = Uri.parse(url);   
		intent.setDataAndType(uri, "audio/mp3");   
		ctx.startActivity(intent);  
	}
	public static void uninstall(Context ctx, String packageName) {
		Uri uri = Uri.fromParts("package", packageName, null);     
		Intent intent = new Intent(Intent.ACTION_DELETE, uri); 
		ctx.startActivity(intent);  
	}
	public static void install(Context ctx, String packageName) {
		Uri uri = Uri.fromParts("package", packageName, null);     
		Intent intent = new Intent(Intent.ACTION_PACKAGE_ADDED, uri); 
		ctx.startActivity(intent);  
	}
	public static void installAPK(Context context, String path) {
        Intent intent = new Intent();
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.setAction(android.content.Intent.ACTION_VIEW);
        intent.setDataAndType(Uri.fromFile(new File(path)), "application/vnd.android.package-archive");
        context.startActivity(intent);
    }
	public static void email(Context ctx, String email, String title) { 
        Intent myIntent = new Intent(android.content.Intent.ACTION_SEND);  
        myIntent.setType("plain/text");  
        myIntent.putExtra(android.content.Intent.EXTRA_EMAIL, new String[]{email});  
        myIntent.putExtra(android.content.Intent.EXTRA_SUBJECT, title);  
        ctx.startActivity(Intent.createChooser(myIntent, "发送邮件")); 
	}
	public static void startActivity(Context ctx, Class<?> act) {
		Intent intent = new Intent(ctx, act);
		ctx.startActivity(intent);
	}
	public static void startActivityForResult(Activity ctx, int requestCode, Class<?> act) {
		Intent intent = new Intent(ctx, act);
		ctx.startActivityForResult(intent, requestCode);
	}
	public static void startActivityForResult(Context ctx, Fragment frag, int requestCode, Class<?> act) {
		Intent intent = new Intent(ctx, act);
		frag.startActivityForResult(intent, requestCode);
	}
	public static void startActivity(Context ctx, Class<?> act, String... args) {
		Intent intent = new Intent(ctx, act);
		for (int i = 0; i < args.length; i += 2) {
			intent.putExtra(args[i], args[i + 1]);
		}
		ctx.startActivity(intent);
	}
	public static void startActivityForResult(Activity ctx, int requestCode, Class<?> act, String... args) {
		Intent intent = new Intent(ctx, act);
		for (int i = 0; i < args.length; i += 2) {
			intent.putExtra(args[i], args[i + 1]);
		}
		ctx.startActivityForResult(intent, requestCode);
	}
	public static void startActivityForResult(Context ctx, Fragment frag, int requestCode, Class<?> act, String... args) {
		Intent intent = new Intent(ctx, act);
		for (int i = 0; i < args.length; i += 2) {
			intent.putExtra(args[i], args[i + 1]);
		}
		frag.startActivityForResult(intent, requestCode);
	}
	public static void startActivity(Context ctx, Class<?> act, String[] keys, Object[] values) {
		Intent intent = new Intent(ctx, act);
		for (int i = 0; i < keys.length; i++) {
			if (values[i] instanceof String) intent.putExtra(keys[i], (String)values[i]);
			else if (values[i] instanceof Boolean) intent.putExtra(keys[i], (Boolean)values[i]);
			else if (values[i] instanceof Double) intent.putExtra(keys[i], (Double)values[i]);
			else if (values[i] instanceof Float) intent.putExtra(keys[i], (Float)values[i]);
			else if (values[i] instanceof Integer) intent.putExtra(keys[i], (Integer)values[i]);
			else if (values[i] instanceof Long) intent.putExtra(keys[i], (Long)values[i]);
			else if (values[i] instanceof Short) intent.putExtra(keys[i], (Short)values[i]);
			else if (values[i] instanceof Parcelable) intent.putExtra(keys[i], (Parcelable)values[i]);
			else if (values[i] instanceof Bundle) intent.putExtra(keys[i], (Bundle)values[i]);
			else if (values[i] instanceof String[]) intent.putExtra(keys[i], (String[])values[i]);
			else if (values[i] instanceof Boolean[]) intent.putExtra(keys[i], (Boolean[])values[i]);
			else if (values[i] instanceof Double[]) intent.putExtra(keys[i], (Double[])values[i]);
			else if (values[i] instanceof Float[]) intent.putExtra(keys[i], (Float[])values[i]);
			else if (values[i] instanceof Integer[]) intent.putExtra(keys[i], (Integer[])values[i]);
			else if (values[i] instanceof Long[]) intent.putExtra(keys[i], (Long[])values[i]);
			else if (values[i] instanceof Short[]) intent.putExtra(keys[i], (Short[])values[i]);
		}
		ctx.startActivity(intent);
	}
	public static void startActivityForResult(Activity ctx, int requestCode, Class<?> act, String[] keys, Object[] values) {
		Intent intent = new Intent(ctx, act);
		for (int i = 0; i < keys.length; i++) {
			if (values[i] instanceof String) intent.putExtra(keys[i], (String)values[i]);
			else if (values[i] instanceof Boolean) intent.putExtra(keys[i], (Boolean)values[i]);
			else if (values[i] instanceof Double) intent.putExtra(keys[i], (Double)values[i]);
			else if (values[i] instanceof Float) intent.putExtra(keys[i], (Float)values[i]);
			else if (values[i] instanceof Integer) intent.putExtra(keys[i], (Integer)values[i]);
			else if (values[i] instanceof Long) intent.putExtra(keys[i], (Long)values[i]);
			else if (values[i] instanceof Short) intent.putExtra(keys[i], (Short)values[i]);
			else if (values[i] instanceof Parcelable) intent.putExtra(keys[i], (Parcelable)values[i]);
			else if (values[i] instanceof Bundle) intent.putExtra(keys[i], (Bundle)values[i]);
			else if (values[i] instanceof String[]) intent.putExtra(keys[i], (String[])values[i]);
			else if (values[i] instanceof Boolean[]) intent.putExtra(keys[i], (Boolean[])values[i]);
			else if (values[i] instanceof Double[]) intent.putExtra(keys[i], (Double[])values[i]);
			else if (values[i] instanceof Float[]) intent.putExtra(keys[i], (Float[])values[i]);
			else if (values[i] instanceof Integer[]) intent.putExtra(keys[i], (Integer[])values[i]);
			else if (values[i] instanceof Long[]) intent.putExtra(keys[i], (Long[])values[i]);
			else if (values[i] instanceof Short[]) intent.putExtra(keys[i], (Short[])values[i]);
		}
		ctx.startActivityForResult(intent, requestCode);
	}
	public static void startActivityForResult(Context ctx, int requestCode, Fragment frag, Class<?> act, String[] keys, Object[] values) {
		Intent intent = new Intent(ctx, act);
		for (int i = 0; i < keys.length; i++) {
			if (values[i] instanceof String) intent.putExtra(keys[i], (String)values[i]);
			else if (values[i] instanceof Boolean) intent.putExtra(keys[i], (Boolean)values[i]);
			else if (values[i] instanceof Double) intent.putExtra(keys[i], (Double)values[i]);
			else if (values[i] instanceof Float) intent.putExtra(keys[i], (Float)values[i]);
			else if (values[i] instanceof Integer) intent.putExtra(keys[i], (Integer)values[i]);
			else if (values[i] instanceof Long) intent.putExtra(keys[i], (Long)values[i]);
			else if (values[i] instanceof Short) intent.putExtra(keys[i], (Short)values[i]);
			else if (values[i] instanceof Parcelable) intent.putExtra(keys[i], (Parcelable)values[i]);
			else if (values[i] instanceof Bundle) intent.putExtra(keys[i], (Bundle)values[i]);
			else if (values[i] instanceof String[]) intent.putExtra(keys[i], (String[])values[i]);
			else if (values[i] instanceof Boolean[]) intent.putExtra(keys[i], (Boolean[])values[i]);
			else if (values[i] instanceof Double[]) intent.putExtra(keys[i], (Double[])values[i]);
			else if (values[i] instanceof Float[]) intent.putExtra(keys[i], (Float[])values[i]);
			else if (values[i] instanceof Integer[]) intent.putExtra(keys[i], (Integer[])values[i]);
			else if (values[i] instanceof Long[]) intent.putExtra(keys[i], (Long[])values[i]);
			else if (values[i] instanceof Short[]) intent.putExtra(keys[i], (Short[])values[i]);
		}
		frag.startActivityForResult(intent, requestCode);
	}


	private static boolean isExternalStorageDocument(Uri uri) {
		return "com.android.externalstorage.documents".equals(uri.getAuthority());
	}
	private static boolean isDownloadsDocument(Uri uri) {
		return "com.android.providers.downloads.documents".equals(uri.getAuthority());
	}
	private static boolean isMediaDocument(Uri uri) {
		return "com.android.providers.media.documents".equals(uri.getAuthority());
	}
	private static boolean isGooglePhotosUri(Uri uri) {
		return "com.google.android.apps.photos.content".equals(uri.getAuthority());
	}
	private static String getDataColumn(Context context, Uri uri, String selection, String[] selectionArgs) {
		Cursor cursor = null;
		final String column = "_data";
		final String[] projection = { column };
		try {
			cursor = context.getContentResolver().query(uri, projection, selection, selectionArgs, null);
			if (cursor != null && cursor.moveToFirst()) {
				final int index = cursor.getColumnIndexOrThrow(column);
				return cursor.getString(index);
			}
		} finally {
			if (cursor != null)
				cursor.close();
		}
		return null;
	}
	@SuppressLint("NewApi")
	private static String getPath(final Context context, final Uri uri) {
		final boolean isKitKat = Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT;
		if (isKitKat && DocumentsContract.isDocumentUri(context, uri)) {
			if (isExternalStorageDocument(uri)) {
				final String docId = DocumentsContract.getDocumentId(uri);
				final String[] split = docId.split(":");
				final String type = split[0];
				if ("primary".equalsIgnoreCase(type)) {
					return Environment.getExternalStorageDirectory() + "/" + split[1];
				}
			} else if (isDownloadsDocument(uri)) {
				final String id = DocumentsContract.getDocumentId(uri);
				final Uri contentUri = ContentUris.withAppendedId(Uri.parse("content://downloads/public_downloads"), Long.valueOf(id));
				return getDataColumn(context, contentUri, null, null);
			} else if (isMediaDocument(uri)) {
				final String docId = DocumentsContract.getDocumentId(uri);
				final String[] split = docId.split(":");
				final String type = split[0];
				Uri contentUri = null;
				if ("image".equals(type)) {
					contentUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
				} else if ("video".equals(type)) {
					contentUri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
				} else if ("audio".equals(type)) {
					contentUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
				}
				final String selection = "_id=?";
				final String[] selectionArgs = new String[] {split[1]
				};
				return getDataColumn(context, contentUri, selection, selectionArgs);
			}
		} else if ("content".equalsIgnoreCase(uri.getScheme())) {
			if (isGooglePhotosUri(uri)) return uri.getLastPathSegment();
			return getDataColumn(context, uri, null, null);
		} else if ("file".equalsIgnoreCase(uri.getScheme())) {
			return uri.getPath();
		}
		return null;
	}
	private static Intent cropImageIntent(Context context, Uri uri, int outputX, int outputY, Uri output) {
		Intent intent = new Intent("com.android.camera.action.CROP");
		if (Utility.isKitkatOrLater()) {
			String url = getPath(context, uri);
			intent.setDataAndType(Uri.fromFile(new File(url)), "image/*");
		} else {
			intent.setDataAndType(uri, "image/*");
		}
		intent.putExtra("crop", "true");
		if (outputX > 0 && outputY > 0) {
			intent.putExtra("aspectX", outputX);
			intent.putExtra("aspectY", outputY);
		}
		if (outputX > 0) {
			intent.putExtra("outputX", outputX);
		}
		if (outputY > 0) {
			intent.putExtra("outputY", outputY);
		}
		intent.putExtra("noFaceDetection", true);
		intent.putExtra("scale", true);
		intent.putExtra(MediaStore.EXTRA_OUTPUT, output);
		intent.putExtra("outputFormat", Bitmap.CompressFormat.JPEG.toString());
		intent.putExtra("return-data", false);
		return intent;
	}
	public static void cropImage(Activity context, Uri uri, int outputX, int outputY, Uri output, int requestCode) {
		if (uri == null) return;
		context.startActivityForResult(cropImageIntent(context, uri, outputX, outputY, output), requestCode);
	}
	public static void cropImage(Context context, Fragment fragment, Uri uri, int outputX, int outputY, Uri output, int requestCode) {
		if (uri == null) return;
		fragment.startActivityForResult(cropImageIntent(context, uri, outputX, outputY, output), requestCode);
	}
	public static void cropImage(Activity context, Uri uri, int outputX, int outputY, int requestCode) {
		if (uri == null) return;
		Uri output = Uri.fromFile(new File(Environment.getExternalStorageDirectory(), "cropped.jpg"));
		context.startActivityForResult(cropImageIntent(context, uri, outputX, outputY, output), requestCode);
	}
	public static void cropImage(Context context, Fragment fragment, Uri uri, int outputX, int outputY, int requestCode) {
		if (uri == null) return;
		Uri output = Uri.fromFile(new File(Environment.getExternalStorageDirectory(), "cropped.jpg"));
		fragment.startActivityForResult(cropImageIntent(context, uri, outputX, outputY, output), requestCode);
	}
}
