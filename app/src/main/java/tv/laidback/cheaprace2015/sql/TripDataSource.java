package tv.laidback.cheaprace2015.sql;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import tv.laidback.cheaprace2015.enteties.Trip;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteStatement;
import android.util.Log;

// The data base field definitions in effect for Laddbil
public class TripDataSource {
	private static String TAG = TripDataSource.class.getSimpleName();
	// Database fields
	private SQLiteDatabase database;
	private MySQLiteHelper dbHelper;
  //private String[] allColumns = { "_id", "time", "tripId", "updateposted", "distance", "startdate", "title", "ended", "type", "timer" };
	private String[] allNewColumns = { "_id", "time", "tripId", "updateposted", "distance", "startdate", "title", "ended", "type", "compared", "timer" };
	private Context context;

	public TripDataSource(Context context) {
		this.context = context;
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

	public Trip createTrip(int type, int compare, String title) {

		Timestamp timestamp = new Timestamp(new Date().getTime());

		ContentValues values = new ContentValues();
		values.put("title", title);
		values.put("type", type);
		values.put("compared", compare);
		values.put("startdate", timestamp.toString());
		open();
		long insertId = database.insert(MySQLiteHelper.TABLE_TRIPS, null, values);
		Cursor cursor = database.query(MySQLiteHelper.TABLE_TRIPS, allNewColumns, "_id = " + insertId, null, null, null, null);
		Trip newTrip = null;
		try {
			cursor.moveToFirst();
			newTrip = cursorToTrip(cursor);
		} catch (Exception e) {
			Log.e(TAG, "database operation failed:", e);
		} finally {
			// Make sure to close the cursor
			cursor.close();
		}
		return newTrip;
	}

	public void deleteTrip(Trip trip) {
		long id = trip.getId();
		Log.i(TAG,"Trip deleted with id: " + id);
		open();
		database.delete(MySQLiteHelper.TABLE_TRIPS, "_id" + " = " + id, null);
	}

	public void deleteAll() {
		open();
		database.delete(MySQLiteHelper.TABLE_TRIPS, null, null);
		Log.i(TAG,"All trips deleted");
	}

	public void deleteTrip(int tripId) {
		Log.i(TAG,"Trip deleted with id: " + tripId);
		open();
		database.delete(MySQLiteHelper.TABLE_TRIPS, "_id" + " = " + tripId, null);
	}

	public void updateTrip(int tripId, double distance, double timeInMillisecond) {
		Log.i(TAG,"Trip updated with id: " + tripId + " distance: " + distance + " time: " + timeInMillisecond);

		String where = "_id=?";
		String[] whereArgs = { String.valueOf(tripId) };

		ContentValues args = new ContentValues();
		args.put("distance", distance);
		args.put("time", timeInMillisecond);
        open(); // added
		database.update(MySQLiteHelper.TABLE_TRIPS, args, where, whereArgs);

		// updateposted integer,
		// averagespeed float,
		// distance float,
		// startdate timestamp,
		// title varchar
	}

	public void updateTrip(int localTripId, int serverTripId) {
		Log.i(TAG,"Associating trip " + localTripId+" with server trip id "+serverTripId);

		String where = "_id=?";
		String[] whereArgs = { String.valueOf(localTripId) };

		ContentValues args = new ContentValues();
		args.put("tripId", serverTripId);
		open();
		database.update(MySQLiteHelper.TABLE_TRIPS, args, where, whereArgs);
	}

	public void setEnded(int localTripId, long timer) {
		Log.i(TAG,"Ending trip with id: " + localTripId);

		String where = "_id=?";
		String[] whereArgs = { String.valueOf(localTripId) };

		ContentValues args = new ContentValues();
		args.put("ended", 1);
		args.put("timer", timer);
        open();
		database.update(MySQLiteHelper.TABLE_TRIPS, args, where, whereArgs);
	}

	public void setUpdated(int localTripId) {
		Log.i(TAG,"Ending trip with id: " + localTripId);

		String where = "_id=?";
		String[] whereArgs = { String.valueOf(localTripId) };

		ContentValues args = new ContentValues();
		args.put("ended", 2);
        open();
		database.update(MySQLiteHelper.TABLE_TRIPS, args, where, whereArgs);
	}

	public List<Trip> getAllTrips() {
		List<Trip> trips = new ArrayList<Trip>();
        open();
		Cursor cursor = database.query(MySQLiteHelper.TABLE_TRIPS, allNewColumns, null, null, null, null, "_id desc");
		try {
			cursor.moveToFirst();
			while (!cursor.isAfterLast()) {
				Trip trip = cursorToTrip(cursor);
				trips.add(trip);
				cursor.moveToNext();
			}
		} catch (Exception e) {
			Log.e(TAG, "database operation failed:", e);
		} finally {
			// Make sure to close the cursor
			cursor.close();
		}
		return trips;
	}

	public Trip getTrip(int tripId) {
		List<Trip> trips = new ArrayList<Trip>();

		String where = "_id = ?";
		String[] whereArgs = { String.valueOf(tripId) };
        open();
		Cursor cursor = database.query(MySQLiteHelper.TABLE_TRIPS, allNewColumns, where, whereArgs, null, null, "_id desc");
		try {
			cursor.moveToFirst();
			while (!cursor.isAfterLast()) {
				Trip trip = cursorToTrip(cursor);
				trips.add(trip);
				cursor.moveToNext();
			}
		} catch (Exception e) {
			Log.e(TAG, "database operation failed:", e);
		} finally {
			// Make sure to close the cursor
			cursor.close();
		}
		if (trips.size() > 0) {
			return trips.get(0);
		}
		else
		  return null;
	}

	public List<Trip> getUnsyncedTrips(int limit) {
		List<Trip> trips = new ArrayList<Trip>();

		String where = "tripId is null";
		String[] whereArgs = {};
        open();
		Cursor cursor = database.query(MySQLiteHelper.TABLE_TRIPS, allNewColumns, where, whereArgs, null, null, "_id", String.valueOf(limit));
		try {
			cursor.moveToFirst();
			while (!cursor.isAfterLast()) {
				Trip trip = cursorToTrip(cursor);
				trips.add(trip);
				cursor.moveToNext();
			}
		} catch (Exception e) {
			Log.e(TAG, "database operation failed:", e);
		} finally {
			// Make sure to close the cursor
			cursor.close();
		}
		return trips;
	}

	public List<Trip> getUnupdatedTrips(int limit) {
		List<Trip> trips = new ArrayList<Trip>();

		String where = "ended = ?";
		String[] whereArgs = { String.valueOf(1) };
        open();
		Cursor cursor = database.query(MySQLiteHelper.TABLE_TRIPS, allNewColumns, where, whereArgs, null, null, "_id", String.valueOf(limit));
		try {
			cursor.moveToFirst();
			while (!cursor.isAfterLast()) {
				Trip trip = cursorToTrip(cursor);
				trips.add(trip);
				cursor.moveToNext();
			}
		} catch (Exception e) {
			Log.e(TAG, "database operation failed:", e);
		} finally {
			// Make sure to close the cursor
			cursor.close();
		}
		return trips;
	}

    // Loads database column into Trip object
	private Trip cursorToTrip(Cursor cursor) {
		Trip trip = new Trip();
		trip.setId(cursor.getInt(0)); // _id
		trip.setTime(cursor.getInt(1)); // time
		trip.setTripId(cursor.getInt(2)); // tripId
		trip.setUpdateposted(cursor.getInt(3)); // updateposted
		trip.setDistance(cursor.getInt(4)); // averagespeed
		trip.setStartdate(Timestamp.valueOf(cursor.getString(5))); // startdate
		trip.setTitle(cursor.getString(6)); // title
		trip.setEnded(cursor.getInt(7)); // ended
		trip.setType(cursor.getInt(8)); // type
		trip.setCompared(cursor.getInt(9)); // compared
		trip.setTimer(cursor.getInt(10)); // timer
		return trip;
	}

	public int getServerTripId(int tripId) {
		Trip trip = getTrip(tripId);
		if (trip != null && trip.getTripId() > 0) {
			return trip.getTripId();
		}
		return -1;
	}

	public int countTrips() {

		Log.i(TAG, "counting rows");
		open(); // added
		SQLiteStatement s = database.compileStatement("select count(*) from " + MySQLiteHelper.TABLE_TRIPS + " WHERE ended >= 1;");
		Number count = s.simpleQueryForLong();
		return count.intValue();
	}

	public Trip getTripByPos(int tripPos) {
		List<Trip> trips = new ArrayList<Trip>();

		String where = "ended >= 1";
		String[] whereArgs = {};
        open();
		Cursor cursor = database.query(MySQLiteHelper.TABLE_TRIPS, allNewColumns, where, whereArgs, null, null, "_id desc", tripPos + ", 1");
		try {
			cursor.moveToFirst();
			while (!cursor.isAfterLast()) {
				Trip trip = cursorToTrip(cursor);
				trips.add(trip);
				cursor.moveToNext();
			}
		} catch (Exception e) {
			Log.e(TAG, "database operation failed:", e);
		} finally {
			// Make sure to close the cursor
			cursor.close();
		}
		if (trips.size() > 0) {
			return trips.get(0);
		}
		return null;
	}
}
