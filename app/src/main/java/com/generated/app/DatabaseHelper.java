package com.generated.app;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;
import java.util.List;

public class DatabaseHelper extends SQLiteOpenHelper {
    private static final String DB_NAME = "offline_yt.db";
    private static final int DB_VERSION = 1;

    public DatabaseHelper(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE history (video_id INTEGER PRIMARY KEY, title TEXT, view_count INTEGER, last_viewed INTEGER, folder TEXT)");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldV, int newV) {}

    public void recordView(VideoModel video) {
        SQLiteDatabase db = getWritableDatabase();
        Cursor cursor = db.rawQuery("SELECT view_count FROM history WHERE video_id = ?", new String[]{String.valueOf(video.id)});
        if (cursor.moveToFirst()) {
            int count = cursor.getInt(0) + 1;
            ContentValues cv = new ContentValues();
            cv.put("view_count", count);
            cv.put("last_viewed", System.currentTimeMillis());
            db.update("history", cv, "video_id = ?", new String[]{String.valueOf(video.id)});
        } else {
            ContentValues cv = new ContentValues();
            cv.put("video_id", video.id);
            cv.put("title", video.title);
            cv.put("folder", video.folder);
            cv.put("view_count", 1);
            cv.put("last_viewed", System.currentTimeMillis());
            db.insert("history", null, cv);
        }
        cursor.close();
    }

    public List<Long> getRecommendedIds() {
        List<Long> ids = new ArrayList<>();
        SQLiteDatabase db = getReadableDatabase();
        // Simple logic: most viewed and recently viewed
        Cursor cursor = db.rawQuery("SELECT video_id FROM history ORDER BY view_count DESC, last_viewed DESC LIMIT 10", null);
        while (cursor.moveToNext()) {
            ids.add(cursor.getLong(0));
        }
        cursor.close();
        return ids;
    }
}
