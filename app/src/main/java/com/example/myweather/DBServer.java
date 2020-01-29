package com.example.myweather;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.example.myweather.location.Location;
import com.example.myweather.server.Server;

import java.util.ArrayList;
import java.util.Locale;

//интерфейс табличного представления
interface Tabulated {
    String getTableName();
}

//класс работы с бд
public class DBServer {
    private static final String DATABASE_NAME = "data.db";
    private static final int DATABASE_VERSION = 1;

    private SQLiteDatabase mDataBase;

    public DBServer(Context context) {
        OpenHelper mOpenHelper = new OpenHelper(context);
        mDataBase = mOpenHelper.getWritableDatabase();
    }

    //обобщенный метод получения последнего индекса
    public <T extends Tabulated> int getLastInd(T table) {
        Cursor mCursor = mDataBase.rawQuery(
                String.format("select max(id) from %s;", table.getTableName()),
                null);
        mCursor.moveToFirst();
        int FIRST_COLUMN = 0, lastInd = mCursor.getInt(FIRST_COLUMN);
        mCursor.close();
        return lastInd;
    }

    //таблица серверов
    public class ServerTable implements Tabulated {
        private static final String TABLE_NAME = "server";

        private static final String COLUMN_ID = "id";
        private static final String SERVER_TYPE = "ServerType";
        private static final String SERVER_NAME = "ServerName";
        private static final String SERVER_URL = "ServerUrl";
        private static final String LOCATION = "LocationId";

        private static final int NUM_COLUMN_ID = 0;
        private static final int NUM_COLUMN_SERVER_TYPE = 1;
        private static final int NUM_COLUMN_SERVER_NAME = 2;
        private static final int NUM_COLUMN_SERVER_URL = 3;
        private static final int NUM_COLUMN_LOCATION = 4;

        public String getTableName() {
            return TABLE_NAME;
        }

        public long insert(String serverType, String serverName, String serverUrl, long location) {
            ContentValues cv = new ContentValues();
            cv.put(SERVER_TYPE, serverType);
            cv.put(SERVER_NAME, serverName);
            cv.put(SERVER_URL, serverUrl);
            cv.put(LOCATION, location);
            return mDataBase.insert(TABLE_NAME, null, cv);
        }

        public int update(Server srv) {
            ContentValues cv = new ContentValues();
            cv.put(SERVER_TYPE, srv.getServerType());
            cv.put(SERVER_NAME, srv.getName());
            cv.put(SERVER_URL, srv.getUrl());
            cv.put(LOCATION, srv.getIdLocation());
            return mDataBase.update(TABLE_NAME, cv, COLUMN_ID + " = ?", new String[]{String.valueOf(srv.getId())});
        }

        public void deleteAll() {
            mDataBase.delete(TABLE_NAME, null, null);
        }

        public void delete(long id) {
            mDataBase.delete(TABLE_NAME, COLUMN_ID + " = ?", new String[]{String.valueOf(id)});
        }

        //выбор сервера по местности и типу сервера
        public Server selectServerByLocationAndType(Location location, String type){
            Cursor mCursor = null;
            if (location != null && type != null) {
                mCursor = mDataBase.rawQuery(
                        String.format(
                                Locale.US,
                                "select * from server where LocationId = %d and ServerType = '%s';",
                                location.getId(), type),
                        null);
                if (mCursor.getCount() > 0) {
                    mCursor.moveToFirst();
                    long id = mCursor.getLong(NUM_COLUMN_ID);
                    String serverType = mCursor.getString(NUM_COLUMN_SERVER_TYPE);
                    String serverName = mCursor.getString(NUM_COLUMN_SERVER_NAME);
                    String serverUrl = mCursor.getString(NUM_COLUMN_SERVER_URL);
                    long serverLocation = mCursor.getLong(NUM_COLUMN_LOCATION);
                    mCursor.close();
                    return new Server(id, serverType, serverName, serverUrl, serverLocation);
                }
                mCursor.close();
            }
            return null;
        }

        //получение сервера по id
        public Server select(int id) {
            Cursor mCursor = mDataBase.query(TABLE_NAME, null, COLUMN_ID + " = ?", new String[]{String.valueOf(id)}, null, null, null);

            mCursor.moveToFirst();
            String serverType = mCursor.getString(NUM_COLUMN_SERVER_TYPE);
            String serverName = mCursor.getString(NUM_COLUMN_SERVER_NAME);
            String serverUrl = mCursor.getString(NUM_COLUMN_SERVER_URL);
            long serverLocation = mCursor.getLong(NUM_COLUMN_LOCATION);

            return new Server(id, serverType, serverName, serverUrl, serverLocation);
        }

        public ArrayList<Server> selectAll() {
            Cursor mCursor = mDataBase.query(TABLE_NAME, null, null, null, null, null, null);

            ArrayList<Server> arr = new ArrayList<>();
            mCursor.moveToFirst();
            if (!mCursor.isAfterLast()) {
                do {
                    int id = mCursor.getInt(NUM_COLUMN_ID);
                    String serverType = mCursor.getString(NUM_COLUMN_SERVER_TYPE);
                    String serverName = mCursor.getString(NUM_COLUMN_SERVER_NAME);
                    String serverUrl = mCursor.getString(NUM_COLUMN_SERVER_URL);
                    long serverLocation = mCursor.getLong(NUM_COLUMN_LOCATION);
                    arr.add(new Server(id, serverType, serverName, serverUrl, serverLocation));
                } while (mCursor.moveToNext());
            }
            return arr;
        }
    }

    //таблица локаций
    public class LocationTable implements Tabulated {
        private static final String TABLE_NAME = "location";

        private static final String COLUMN_ID = "ID";
        private static final String COLUMN_NAME = "Name";
        private static final String COLUMN_FAVORITE = "Favorite";

        private static final int NUM_COLUMN_ID = 0;
        private static final int NUM_COLUMN_NAME = 1;
        private static final int NUM_COLUMN_FAVORITE = 2;

        //убрать параметр главные у всех
        public void removeFavorites(){
            mDataBase.execSQL(
                    "update location set Favorite = 0 where Favorite = 1;");
        }

        //получить только главную локцию
        public Location selectFavorite(){
            Cursor mCursor = mDataBase.rawQuery(
                    "select * from location where Favorite = 1;",
                    null);
            if (mCursor.getCount() > 0) {
                mCursor.moveToFirst();
                int id = mCursor.getInt(NUM_COLUMN_ID);
                String name = mCursor.getString(NUM_COLUMN_NAME);
                boolean favorite = mCursor.getInt(NUM_COLUMN_FAVORITE) == 1;
                mCursor.close();
                return new Location(id, name, favorite);
            }
            mCursor.close();
            return null;
        }

        public long insert(String name, boolean favorite) {
            if (favorite)
                removeFavorites();
            ContentValues cv = new ContentValues();
            cv.put(COLUMN_NAME, name);
            cv.put(COLUMN_FAVORITE, favorite ? 1 : 0);
            return mDataBase.insert(TABLE_NAME, null, cv);
        }

        public int update(Location lctn) {
            if (lctn.isFavorite())
                removeFavorites();
            ContentValues cv = new ContentValues();
            cv.put(COLUMN_NAME, lctn.getName());
            cv.put(COLUMN_FAVORITE, lctn.isFavorite() ? 1 : 0);
            return mDataBase.update(TABLE_NAME, cv, COLUMN_ID + " = ?", new String[]{String.valueOf(lctn.getId())});
        }

        public void deleteAll() {
            mDataBase.delete(TABLE_NAME, null, null);
        }

        //проверка на пустоту таблицы
        public boolean isEmpty(){
            Cursor mCursor = mDataBase.rawQuery(
                    "select * from location;",
                    null);
            boolean empty = true;
            if (mCursor.getCount() != 0)
                empty = false;
            mCursor.close();
            return empty;
        }

        //полное удаление местности и связанных серверов
        public void completeDelete(long id){
            mDataBase.delete(TABLE_NAME,
                    COLUMN_ID + " = ?",
                    new String[]{String.valueOf(id)});

            mDataBase.delete(ServerTable.TABLE_NAME,
                    ServerTable.LOCATION + " = ?",
                    new String[]{String.valueOf(id)});
        }

        public void delete(long id) {
            mDataBase.delete(TABLE_NAME, COLUMN_ID + " = ?", new String[]{String.valueOf(id)});
        }

        public Location select(long id) {
            Cursor mCursor = mDataBase.query(TABLE_NAME, null, COLUMN_ID + " = ?", new String[]{String.valueOf(id)}, null, null, null);

            mCursor.moveToFirst();
            String name = mCursor.getString(NUM_COLUMN_NAME);
            boolean favorite = mCursor.getInt(NUM_COLUMN_FAVORITE) == 1;
            mCursor.close();
            return new Location(id, name, favorite);
        }

        public ArrayList<Location> selectAll() {
            Cursor mCursor = mDataBase.query(TABLE_NAME, null, null, null, null, null, null);

            ArrayList<Location> arr = new ArrayList<>();
            mCursor.moveToFirst();
            if (!mCursor.isAfterLast()) {
                do {
                    int id = mCursor.getInt(NUM_COLUMN_ID);
                    String name = mCursor.getString(NUM_COLUMN_NAME);
                    boolean favorite = mCursor.getInt(NUM_COLUMN_FAVORITE) == 1;
                    arr.add(new Location(id, name, favorite));
                } while (mCursor.moveToNext());
            }
            mCursor.close();
            return arr;
        }

        @Override
        public String getTableName() {
            return TABLE_NAME;
        }
    }

    private class OpenHelper extends SQLiteOpenHelper {

        OpenHelper(Context context) {
            super(context, DATABASE_NAME, null, DATABASE_VERSION);
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
            String serverQuery = "CREATE TABLE " + ServerTable.TABLE_NAME + " (" +
                    ServerTable.COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    ServerTable.SERVER_TYPE + " TEXT, " +
                    ServerTable.SERVER_NAME + " TEXT, " +
                    ServerTable.SERVER_URL + " TEXT, " +
                    ServerTable.LOCATION + " INT);";
            db.execSQL(serverQuery);

            String locationQuery = "CREATE TABLE " + LocationTable.TABLE_NAME + " (" +
                    LocationTable.COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    LocationTable.COLUMN_NAME + " TEXT UNIQUE, " +
                    LocationTable.COLUMN_FAVORITE + " INT);";
            db.execSQL(locationQuery);
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            db.execSQL("DROP TABLE IF EXISTS " + ServerTable.TABLE_NAME);
            db.execSQL("DROP TABLE IF EXISTS " + LocationTable.TABLE_NAME);
            onCreate(db);
        }
    }
}
