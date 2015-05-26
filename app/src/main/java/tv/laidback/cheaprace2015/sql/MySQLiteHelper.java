/*
 * Copyright (C) 2011 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package tv.laidback.cheaprace2015.sql;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

/**
 * 
 * Basic database creation operations
 *
 */
public class MySQLiteHelper extends SQLiteOpenHelper {

	public static final String TABLE_LOCATIONS = "locations";
	public static final String TABLE_TRIPS = "trips";

	public static final String LADDBIL_DATABASE_NAME = "testladdbil1.db";
	private static final int DATABASE_VERSION = 12;

	/**
	 * Addition for static access to ONE database helper instance.
	 * 
	 * @credits http://touchlabblog.tumblr.com/post/24474750219/single-sqlite-connection
	 */
	private static MySQLiteHelper instance;
	
	public static synchronized MySQLiteHelper getHelper(Context context) {
		if (instance == null)
			instance = new MySQLiteHelper(context);
		return instance;
	}
	
	/**
	 * Regarding the database tables below; if you change the table you need to
	 * increment the database version so that the database will be recreated
	 * in the new format. All previous database content will be erased if so.
	 * A primitive way to preserve any old data can be to rename the database and
	 * later on add some merge code that copies in any precious data from the old db.
	 */
	
	// The location database remains the same as with Elcykel 
	private static final String LOCATION_TABLE_CREATE = "create table locations ( _id integer primary key autoincrement, locationId integer, tripId integer, accuracy float, altitude float, course float, latitude float, longitude float, speed float, timestamp timestamp );";
	// For reference; the original Elcykel trip database format
    //	private static final String TRIP_TABLE_CREATE = "create table trips ( _id integer primary key autoincrement, time integer, timer integer, tripId integer, updateposted integer, distance float, startdate timestamp, title varchar, ended integer, type integer);";
	// Laddbil format; one more parameter added; "compared" = vehicle type to compare with
	private static final String TRIP_TABLE_CREATE_LADDBIL = "create table trips ( _id integer primary key autoincrement, time integer, timer integer, tripId integer, updateposted integer, distance float, startdate timestamp, title varchar, ended integer, type integer, compared integer);";


	public MySQLiteHelper(Context context) {
		super(context, LADDBIL_DATABASE_NAME, null, DATABASE_VERSION);
	}

	@Override
	public void onCreate(SQLiteDatabase database) {
		database.execSQL(LOCATION_TABLE_CREATE);
		database.execSQL(TRIP_TABLE_CREATE_LADDBIL);  // Laddbil format now in effect
	}

	// When it is sensed that the database version has been updated 
	// the old database tables are erased and new ones written.
	// Currently no code exists to preserve/migrate earlier content
	// on same database name or to migrate over from another database.
	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		Log.w(MySQLiteHelper.class.getName(), "CAUTION: Upgrading database "+ 
				LADDBIL_DATABASE_NAME + " from version " + oldVersion +
				" to " + newVersion + ", which destroys all old data..");
		db.execSQL("DROP TABLE IF EXISTS " + TABLE_LOCATIONS);
		db.execSQL("DROP TABLE IF EXISTS " + TABLE_TRIPS);
		onCreate(db);
	}
}