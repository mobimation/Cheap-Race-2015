package tv.laidback.cheaprace2015.enteties;

import java.sql.Timestamp;
import java.util.Date;

public class Trip {

	private int _id;
	private long time;
	private int tripId;
	private int updateposted;
	private double distance;
	private Timestamp startdate;
	private String title;
	private int ended;
	private int type;
	private int compared; // New for Laddbil
	private long timer;

	public int getId() {
		return _id;
	}

	public void setId(int _id) {
		this._id = _id;
	}

	public long getTimeMs() {
		return time;
	}

	public long getTimeS() {
		return time / 1000;
	}

	public void setTime(long time) {
		this.time = time;
	}

	public int getTripId() {
		return tripId;
	}

	public void setTripId(int tripId) {
		this.tripId = tripId;
	}

	public int getUpdateposted() {
		return updateposted;
	}

	public void setUpdateposted(int updateposted) {
		this.updateposted = updateposted;
	}

	public Date getStartdate() {
		return startdate;
	}

	public void setStartdate(Timestamp startdate) {
		this.startdate = startdate;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public double getDistance() {
		return distance;
	}

	public void setDistance(double distance) {
		this.distance = distance;
	}

	public int isEnded() {
		return ended;
	}

	public void setEnded(int ended) {
		this.ended = ended;
	}

	public int getType() {
		return type;
	}

	public void setType(int type) {
		this.type = type;
	}

	public int getCompared() {
		return compared;
	}

	public void setCompared(int compared) {
		this.compared = compared;
	}
	
	public long getTimer() {
		return timer;
	}

	public void setTimer(long timer) {
		this.timer = timer;
	}
}
