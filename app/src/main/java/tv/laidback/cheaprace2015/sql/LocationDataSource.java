package tv.laidback.cheaprace2015.sql;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteStatement;
import android.util.Log;

import tv.laidback.cheaprace2015.enteties.RaceLocation;

public class LocationDataSource {

	private static String TAG = LocationDataSource.class.getSimpleName();

	// Database fields
	private SQLiteDatabase database;
	private MySQLiteHelper dbHelper;
	private String[] allColumns = { "_id", "locationId", "tripId", "accuracy", "altitude", "latitude", "longitude", "course", "speed", "timestamp" };

	public LocationDataSource(Context context) {
		// Updated for access to one common helper instance
		// (in an attempt to avoid database locked situations)
		// see http://touchlabblog.tumblr.com/post/24474750219/single-sqlite-connection
		//
		dbHelper = MySQLiteHelper.getHelper(context.getApplicationContext());
		// dbHelper = new MySQLiteHelper(context);
	}

	public void open() throws SQLException {
		database = dbHelper.getWritableDatabase();
	}

	public void close() {
		dbHelper.close();
	}

	public RaceLocation createLocation(RaceLocation location) {
		ContentValues values = new ContentValues();
		values.put("tripId", location.getTripId());
		values.put("accuracy", location.getAccuracy());
		values.put("latitude", location.getLatitude());
		values.put("longitude", location.getLongitude());
		values.put("timestamp", location.getTimestamp().toString());
        open();  // added
		long insertId = database.insert(MySQLiteHelper.TABLE_LOCATIONS, null, values);
		Cursor cursor = database.query(MySQLiteHelper.TABLE_LOCATIONS, allColumns, "_id = " + insertId, null, null, null, null);
		RaceLocation newLocation = null;
		try {
			cursor.moveToFirst();
			newLocation = cursorToLocation(cursor);
		} catch (Exception e) {
			Log.e(TAG, "database operation failed:", e);
		} finally {
			cursor.close();
		}
		return newLocation;
	}

	public void deleteLocation(RaceLocation location) {
		long id = location.getId();
		Log.i(TAG,"Location deleted with id: " + id);
		open();
		database.delete(MySQLiteHelper.TABLE_LOCATIONS, "_id" + " = " + id, null);
	}

	public void deleteTrip(int tripId) {
		Log.i(TAG,"Location deleted with id: " + tripId);
		open();
		database.delete(MySQLiteHelper.TABLE_LOCATIONS, "tripId" + " = " + tripId, null);
	}

	public List<RaceLocation> getAllLocations() {
		List<RaceLocation> locations = new ArrayList<RaceLocation>();
		open();
		Cursor cursor = database.query(MySQLiteHelper.TABLE_LOCATIONS, allColumns, null, null, null, null, null);
		try {
			cursor.moveToFirst();
			while (!cursor.isAfterLast()) {
				RaceLocation location = cursorToLocation(cursor);
				locations.add(location);
				cursor.moveToNext();
			}
		} catch (Exception e) {
			Log.e(TAG, "database operation failed:", e);
		} finally {
			// Make sure to close the cursor
			cursor.close();
		}
		return locations;
	}

	public List<RaceLocation> getUnsyncedLocations(int limit) {
		List<RaceLocation> locations = new ArrayList<RaceLocation>();

		String where = "locationId is null";
		String[] whereArgs = {};
		open();
		Cursor cursor = database.query(MySQLiteHelper.TABLE_LOCATIONS, allColumns, where, whereArgs, null, null, null, String.valueOf(limit));
		try {
			cursor.moveToFirst();
			while (!cursor.isAfterLast()) {
				RaceLocation location = cursorToLocation(cursor);
				locations.add(location);
				cursor.moveToNext();
			}
		} catch (Exception e) {
			Log.e(TAG, "database operation failed:", e);
		} finally {
			// Make sure to close the cursor
			cursor.close();
		}
		return locations;
	}

	public List<RaceLocation> getAllLocations(int tripId) {
		List<RaceLocation> locations = new ArrayList<RaceLocation>();

		String selection = "tripId=?";
		String[] selectionArgs = { String.valueOf(tripId) };
		open();
		Cursor cursor = database.query(MySQLiteHelper.TABLE_LOCATIONS, allColumns, selection, selectionArgs, null, null, null);
		try {
			cursor.moveToFirst();
			while (!cursor.isAfterLast()) {
				RaceLocation location = cursorToLocation(cursor);
				locations.add(location);
				cursor.moveToNext();
			}
		} catch (Exception e) {
			Log.e(TAG, "database operation failed:", e);
		} finally {
			// Make sure to close the cursor
			cursor.close();
		}
		return locations;
	}

	private RaceLocation cursorToLocation(Cursor cursor) {
		RaceLocation location = new RaceLocation();
		location.setId(cursor.getInt(0));
		location.setLocationId(cursor.getInt(1));
		location.setTripId(cursor.getInt(2));
		location.setAccuracy(cursor.getFloat(3));
		// location.setAltitude(4)
		location.setLatitude(cursor.getFloat(5));
		location.setLongitude(cursor.getFloat(6));
		// location.setCourse(7);
		// location.setSpeed(8);
		location.setTimestamp(Timestamp.valueOf(cursor.getString(9)));

		// private String[] allColumns = { "_id", "locationId", "tripId",
		// "accuracy",
		// "altitude", "latitude", "longitude", "course", "speed", "timestamp"
		// };

		return location;
	}

	public void updateLocation(int localLocationId, int serverLocationId) {
		Log.i(TAG,"Updating Trip with id: " + localLocationId);

		String where = "_id=?";
		String[] whereArgs = { String.valueOf(localLocationId) };

		ContentValues args = new ContentValues();
		args.put("locationId", serverLocationId);
		open();
		database.update(MySQLiteHelper.TABLE_LOCATIONS, args, where, whereArgs);
	}

	public RaceLocation getLastLocation(int activeTripId) {

		List<RaceLocation> locations = new ArrayList<RaceLocation>();
		String where = "tripId=?";
		String[] whereArgs = { String.valueOf(activeTripId) };
		open();
		Cursor cursor = database.query(MySQLiteHelper.TABLE_LOCATIONS, allColumns, where, whereArgs, null, null, "_id desc", "1");
		try {
			cursor.moveToFirst();
			while (!cursor.isAfterLast()) {
				RaceLocation location = cursorToLocation(cursor);
				locations.add(location);
				cursor.moveToNext();
			}
		} catch (Exception e) {
			Log.e(TAG, "database operation failed:", e);
		} finally {
			// Make sure to close the cursor
			cursor.close();
		}
		if (locations.size() > 0) {
			return locations.get(0);
		}
		return null;
	}

	public RaceLocation getFirstLocation(int activeTripId) {

		List<RaceLocation> locations = new ArrayList<RaceLocation>();
		String where = "tripId=?";
		String[] whereArgs = { String.valueOf(activeTripId) };
		open();
		Cursor cursor = database.query(MySQLiteHelper.TABLE_LOCATIONS, allColumns, where, whereArgs, null, null, "_id", "1");
		try {
			cursor.moveToFirst();
			while (!cursor.isAfterLast()) {
				RaceLocation location = cursorToLocation(cursor);
				locations.add(location);
				cursor.moveToNext();
			}
		} catch (Exception e) {
			Log.e(TAG, "database operation failed:", e);
		} finally {
			// Make sure to close the cursor
			cursor.close();
		}
		if (locations.size() > 0) {
			return locations.get(0);
		}
		return null;
	}

	public void deleteAll() {
		open();
		database.delete(MySQLiteHelper.TABLE_LOCATIONS, null, null);
		Log.i(TAG,"All trips deleted");
	}

	public int countLocations() {
		Log.i(TAG, "counting rows");
		open();
		SQLiteStatement s = database.compileStatement("select count(*) from " + MySQLiteHelper.TABLE_LOCATIONS + ";");
		Number count = s.simpleQueryForLong();
		return count.intValue();
	}

	public RaceLocation getLocationByPos(int position) {
		List<RaceLocation> locations = new ArrayList<RaceLocation>();

		String where = "";
		String[] whereArgs = {};
		open();
		Cursor cursor = database.query(MySQLiteHelper.TABLE_TRIPS, allColumns, where, whereArgs, null, null, "_id desc", position + ", 1");
		try {
			cursor.moveToFirst();
			while (!cursor.isAfterLast()) {
				RaceLocation location = cursorToLocation(cursor);
				locations.add(location);
				cursor.moveToNext();
			}
		} catch (Exception e) {
			Log.e(TAG, "database operation failed:", e);
		} finally {
			// Make sure to close the cursor
			cursor.close();
		}
		if (locations.size() > 0) {
			return locations.get(0);
		}
		return null;
	}
}