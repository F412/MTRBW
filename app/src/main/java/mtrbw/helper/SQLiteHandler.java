package mtrbw.helper;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class SQLiteHandler extends SQLiteOpenHelper {

	private static final String TAG = SQLiteHandler.class.getSimpleName();

	// All Static variables
	// Database Version
	private static final int DATABASE_VERSION = 1;

	// Database Name
	private static final String DATABASE_NAME = "android_api";

	// Login table name
	private static final String TABLE_LOGIN = "login";

	// Login Table Columns names
	private static final String KEY_ID = "id";
	private static final String KEY_EMAIL = "email";
    private static final String KEY_GENDER = "gender";
    private static final String KEY_YEAR = "year";
    private static final String KEY_LOCTABLE = "loctable";
	private static final String KEY_UID = "uid";
	private static final String KEY_CREATED_AT = "created_at";

	public SQLiteHandler(Context context) {
		super(context, DATABASE_NAME, null, DATABASE_VERSION);
	}

	// Creating Tables
	@Override
	public void onCreate(SQLiteDatabase db) {

        db.execSQL("CREATE TABLE locations (id INTEGER PRIMARY KEY, lat DOUBLE, lon DOUBLE, bearing FLOAT, speed FLOAT, time LONG)");

		String CREATE_LOGIN_TABLE = "CREATE TABLE " + TABLE_LOGIN + "("
				+ KEY_ID + " INTEGER PRIMARY KEY," + KEY_EMAIL + " TEXT UNIQUE," + KEY_GENDER + " TEXT," + KEY_YEAR + " INT," + KEY_LOCTABLE + " TEXT," + KEY_UID + " TEXT,"
				+ KEY_CREATED_AT + " TEXT" + ")";
		db.execSQL(CREATE_LOGIN_TABLE);
	}

	// Upgrading database
	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		// Drop older table if existed
        db.execSQL("DROP TABLE IF EXISTS locations");
		db.execSQL("DROP TABLE IF EXISTS " + TABLE_LOGIN);

		// Create tables again
		onCreate(db);
	}

	/**
	 * Storing user details in database
	 * */
	public void addUser(String email, String gender, String year, String loctable, String uid, String created_at) {
		SQLiteDatabase db = this.getWritableDatabase();

		ContentValues values = new ContentValues();
		values.put(KEY_EMAIL, email); // Email
        values.put(KEY_GENDER, gender); //Gender
        values.put(KEY_YEAR, year); //Year
        values.put(KEY_LOCTABLE, loctable); //Loctable
		values.put(KEY_UID, uid); // UID
		values.put(KEY_CREATED_AT, created_at); // Created At

		// Inserting Row
		long id = db.insert(TABLE_LOGIN, null, values);
		db.close(); // Closing database connection

		Log.d(TAG, "User saved in local database: " + id);
	}

	/**
	 * Getting user data from database
	 * */
	public HashMap<String, String> getUserDetails() {
		HashMap<String, String> user = new HashMap<String, String>();
		String selectQuery = "SELECT  * FROM " + TABLE_LOGIN;

		SQLiteDatabase db = this.getReadableDatabase();
		Cursor cursor = db.rawQuery(selectQuery, null);
		// Move to first row
		cursor.moveToFirst();
		if (cursor.getCount() > 0) {
			user.put("email", cursor.getString(1));
            user.put("gender", cursor.getString(2));
            user.put("year", cursor.getString(3));
            user.put("loctable", cursor.getString(4));
            user.put("uid", cursor.getString(5));
			user.put("created_at", cursor.getString(6));
		}
		cursor.close();
		db.close();

		return user;
	}

	/**
	 * Getting user login status return true if rows are there in table
	 * */
	public int getRowCount() {
		String countQuery = "SELECT  * FROM " + TABLE_LOGIN;
		SQLiteDatabase db = this.getReadableDatabase();
		Cursor cursor = db.rawQuery(countQuery, null);
		int rowCount = cursor.getCount();
		db.close();
		cursor.close();

		// return row count
		return rowCount;
	}

	/**
	 * Re crate database Delete all tables and create them again
	 * */
	public void deleteUsers() {
		SQLiteDatabase db = this.getWritableDatabase();
		// Delete All Rows
		db.delete(TABLE_LOGIN, null, null);
		db.close();

		Log.d(TAG, "Deleted all user info from sqlite");
	}

    public void addLocation(double lat, double lon, float bearing, float speed, long time){
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("lat", lat);
        values.put("lon", lon);
        values.put("bearing", bearing);
        values.put("speed", speed);
        values.put("time", time);
        db.insert("locations", null, values);
        db.close();
    }

    public void truncateLocations(){
        SQLiteDatabase db = this.getWritableDatabase();
        db.execSQL("delete from  locations");
        db.close();
    }



    public ArrayList<String> getAllLocations(){

        //Conversion factor m/s to km/h
        double kmh = 3.6;

        //Date format
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss z");

        ArrayList<String> locations = new ArrayList<String>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM locations", null);
        cursor.moveToFirst();
        for(int i = 0; i < cursor.getCount(); i++){
            locations.add(String.valueOf(cursor.getDouble(1)) + "\r\n" + String.valueOf(cursor.getDouble(2)) + "\r\n" + String.valueOf(cursor.getFloat(3)) + "°\r\n" + String.valueOf(cursor.getFloat(4) * kmh) + " km/h\r\n" + format.format(cursor.getLong(5)));
            cursor.moveToNext();
        }
        db.close();
        return locations;
    }

    public String getValues(){

        //Conversion factor m/s to km/h
        double kmh = 3.6;

        //Date format
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss z");

        String values = "";
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM locations", null);
        cursor.moveToFirst();
        for(int i = 0; i < cursor.getCount(); i++){
            values = values + "(" + String.valueOf(cursor.getDouble(1)) + ", " + String.valueOf(cursor.getDouble(2)) + ", " + String.valueOf(cursor.getFloat(3)) + ", " + String.valueOf(cursor.getFloat(4) * kmh) + ", '" + format.format(cursor.getLong(5)) + "'), ";
            cursor.moveToNext();
        }
        db.close();
        if(values.length()>2){
            values = values.substring(0, values.length()-2);
            values = values + ";";
        }
        return values;
    }

    public int numberOfLocations(){
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM locations", null);
        return cursor.getCount();
    }


}
