package tv.laidback.cheaprace2015.enteties;

import java.sql.Timestamp;

import android.location.Location;

import com.google.android.gms.maps.model.LatLng;

public class RaceLocation {

	private int _id;
	private int locationId;
	private int tripId;
	private float accuracy;
	private double altitude;
	private double latitude;
	private double longitude;
	private float course;
	private float speed;
	private Timestamp timestamp;

	public RaceLocation() {
	}

	public RaceLocation(int tripId, Location location) {
		setTripId(tripId);
		setAccuracy(location.getAccuracy());
		setLatitude(location.getLatitude());
		setLongitude(location.getLongitude());
		setAltitude(location.getAltitude());
		setCourse(location.getBearing());
		setSpeed(location.getSpeed());
		setTimestamp(new Timestamp(location.getTime()));
	}

	public int getId() {
		return _id;
	}

	public void setId(int _id) {
		this._id = _id;
	}

	public int getLocationId() {
		return locationId;
	}

	public void setLocationId(int locationId) {
		this.locationId = locationId;
	}

	public int getTripId() {
		return tripId;
	}

	public void setTripId(int tripId) {
		this.tripId = tripId;
	}

	public float getAccuracy() {
		return accuracy;
	}

	public void setAccuracy(float accuracy) {
		this.accuracy = accuracy;
	}

	public double getAltitude() {
		return altitude;
	}

	public void setAltitude(double altitude) {
		this.altitude = altitude;
	}

	public double getLatitude() {
		return latitude;
	}

	public void setLatitude(double latitude) {
		this.latitude = latitude;
	}

	public double getLongitude() {
		return longitude;
	}

	public void setLongitude(double longitude) {
		this.longitude = longitude;
	}

	public float getCourse() {
		return course;
	}

	public void setCourse(float course) {
		this.course = course;
	}

	public float getSpeed() {
		return speed;
	}

	public void setSpeed(float speed) {
		this.speed = speed;
	}

	public Timestamp getTimestamp() {
		return timestamp;
	}

	public void setTimestamp(Timestamp timestamp) {
		this.timestamp = timestamp;
	}

	public LatLng getLatLong() {
		return new LatLng(getLatitude(), getLongitude());
	}

}
