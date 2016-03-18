package org.nv95.openmanga.providers;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.support.annotation.Nullable;

import org.nv95.openmanga.Constants;
import org.nv95.openmanga.R;
import org.nv95.openmanga.helpers.StorageHelper;
import org.nv95.openmanga.items.MangaInfo;
import org.nv95.openmanga.items.MangaPage;
import org.nv95.openmanga.items.MangaSummary;
import org.nv95.openmanga.lists.MangaList;
import org.nv95.openmanga.utils.MangaChangesObserver;

import java.io.File;
import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Date;

/**
 * Created by nv95 on 05.10.15.
 */
public class HistoryProvider extends MangaProvider {
    private static final String TABLE_NAME = "history";
    protected static boolean features[] = {false, false, true, true, false};
    private static final int sorts[] = {R.string.sort_latest, R.string.sort_alphabetical};
    private static final String sortUrls[] = {"timestamp DESC", "name COLLATE NOCASE"};
    private static WeakReference<HistoryProvider> instanceReference = new WeakReference<HistoryProvider>(null);
    StorageHelper dbHelper;
    private Context context;

    @Deprecated
    public HistoryProvider(Context context) {
        this.context = context;
        dbHelper = new StorageHelper(context);
    }

    public static HistoryProvider getInstacne(Context context) {
        HistoryProvider instance = instanceReference.get();
        if (instance == null) {
            instance = new HistoryProvider(context);
            instanceReference = new WeakReference<HistoryProvider>(instance);
        }
        return instance;
    }

    public static boolean addToHistory(Context context, MangaInfo mangaInfo, int chapter, int page) {
        return HistoryProvider.getInstacne(context).add(mangaInfo, chapter, page);
    }

    public static boolean removeFromHistory(Context context, MangaInfo mangaInfo) {
        return HistoryProvider.getInstacne(context).remove(mangaInfo);
    }

    public static boolean has(Context context, MangaInfo mangaInfo) {
        return HistoryProvider.getInstacne(context).has(mangaInfo);
    }

    public static HistorySummary get(Context context, MangaInfo mangaInfo) {
        return HistoryProvider.getInstacne(context).get(mangaInfo);
    }

    public static MangaInfo GetLast(Context context) {
        try {
            return HistoryProvider.getInstacne(context).getList(0, 0, 0).get(0);
        } catch (Exception e) {
            return null;
        }
    }

    @Override
    protected void finalize() throws Throwable {
        dbHelper.close();
        super.finalize();
    }

    @Override
    public MangaList getList(int page, int sort, int genre) throws IOException {
        if (page > 0)
            return null;
        SQLiteDatabase database = dbHelper.getReadableDatabase();
        MangaList list;
        MangaInfo manga;
        //noinspection TryFinallyCanBeTryWithResources
        try {
            list = new MangaList();
            Cursor cursor = database.query(TABLE_NAME, null, null, null, null, null, sortUrls[sort]);
            if (cursor.moveToFirst()) {
                do {
                    manga = new MangaInfo(cursor);
                    list.add(manga);
                } while (cursor.moveToNext());
            }
            cursor.close();
        } finally {
            database.close();
        }
        return list;
    }

    @Override
    public MangaSummary getDetailedInfo(MangaInfo mangaInfo) {
        return null;
    }

    @Override
    public ArrayList<MangaPage> getPages(String readLink) {
        return null;
    }

    @Override
    public String getPageImage(MangaPage mangaPage) {
        return null;
    }

    @Override
    public String getName() {
        return context.getString(R.string.action_history);
    }

    @Override
    public boolean hasFeature(int feature) {
        return features[feature];
    }

    public boolean add(MangaInfo mangaInfo, int chapter, int page) {
        SQLiteDatabase database = dbHelper.getWritableDatabase();
        ContentValues cv = mangaInfo.toContentValues();
        cv.put("timestamp", new Date().getTime());
        cv.put("chapter", chapter);
        cv.put("page", page);
        int updCount = database.update(TABLE_NAME, cv, "id=" + mangaInfo.path.hashCode(), null);
        if (updCount == 0) {
            database.insert(TABLE_NAME, null, cv);
        }
        database.close();
        MangaChangesObserver.queueChanges(Constants.CATEGORY_HISTORY);
        return true;
    }

    public boolean remove(MangaInfo mangaInfo) {
        SQLiteDatabase database = dbHelper.getWritableDatabase();
        database.delete(TABLE_NAME, "id=" + mangaInfo.path.hashCode(), null);
        database.close();
        MangaChangesObserver.queueChanges(Constants.CATEGORY_HISTORY);
        return true;
    }

    @Override
    public boolean remove(long[] ids) {
        SQLiteDatabase database = dbHelper.getWritableDatabase();
        database.beginTransaction();
        File cacheDir = context.getExternalCacheDir();
        for (long o : ids) {
            database.delete(TABLE_NAME, "id=" + o, null);
        }
        database.setTransactionSuccessful();
        database.endTransaction();
        database.close();
        MangaChangesObserver.queueChanges(Constants.CATEGORY_HISTORY);
        return true;
    }

    public void clear() {
        SQLiteDatabase database = dbHelper.getWritableDatabase();
        database.beginTransaction();
        database.delete(TABLE_NAME, null, null);
        database.setTransactionSuccessful();
        database.endTransaction();
        database.close();
        MangaChangesObserver.queueChanges(Constants.CATEGORY_HISTORY);
    }

    public boolean has(MangaInfo mangaInfo) {
        boolean res;
        SQLiteDatabase database = dbHelper.getReadableDatabase();
        res = database.query(TABLE_NAME, null, "id=" + mangaInfo.path.hashCode(), null, null, null, null).getCount() > 0;
        database.close();
        return res;
    }

    @Nullable
    public HistorySummary get(MangaInfo mangaInfo) {
        HistorySummary res = null;
        SQLiteDatabase database = dbHelper.getReadableDatabase();
        Cursor c = database.query(TABLE_NAME, null, "id=" + mangaInfo.path.hashCode(), null, null, null, null);
        if (c.moveToFirst()) {
            res = new HistorySummary();
            res.time = c.getLong(c.getColumnIndex("timestamp"));
            res.chapter = c.getInt(c.getColumnIndex("chapter"));
            res.page = c.getInt(c.getColumnIndex("page"));
        }
        c.close();
        database.close();
        return res;
    }

    public class HistorySummary {
        protected int chapter;
        protected int page;
        protected long time;

        public int getChapter() {
            return chapter;
        }

        public int getPage() {
            return page;
        }

        public long getTime() {
            return time;
        }
    }

    @Override
    public String[] getSortTitles(Context context) {
        return super.getTitles(context, sorts);
    }
}
