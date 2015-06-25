package tv.laidback.cheaprace2015.compatibility;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Locale;

import tv.laidback.cheaprace2015.R;
import tv.laidback.cheaprace2015.enteties.Trip;
import tv.laidback.cheaprace2015.sql.TripDataSource;
import android.app.Activity;
import android.content.Context;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

public class TripAdapter extends BaseAdapter {
/**
 * The TripAdapter updates calculated values for a trip based on location data.
 * For Laddbil comparison data needs to be calculated for three vehicle categories
 */
	Context context;
	int layoutResourceId;
	private tv.laidback.cheaprace2015.sql.TripDataSource tripDatasource;
	// private LocationDataSource locationDatasource;
	private Number count = null;
	SparseArray<Trip> trips = new SparseArray<>();

	public TripAdapter(Context context, int layoutResourceId, TripDataSource tripDatasource) {
		super();
		this.layoutResourceId = layoutResourceId;
		this.context = context;
		this.tripDatasource = tripDatasource;
	}
	
	public void setDataSource(TripDataSource tripDatasource) {
		this.tripDatasource = tripDatasource;
		notifyDataSetChanged();
	}

	@Override
	public void notifyDataSetChanged() {
		count = null;
		trips = new SparseArray<>();
		super.notifyDataSetChanged();
	}

	@Override
	public int getCount() {
		if (tripDatasource == null) {
			return 0;
		}
		if (count == null) {
			count = tripDatasource.countTrips();
		}
		return count.intValue();
	}

	@Override
	public Trip getItem(int position) {
		if (tripDatasource == null) {
			return null;
		}
		Trip object = trips.get(position);
		if (object == null) {
			object = tripDatasource.getTripByPos(position);
			trips.append(position, object);
		}
		return object;
	}

	@Override
	public long getItemId(int position) {
		return getItem(position).getId();
	}

	@Override
	public boolean hasStableIds() {
		return true;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		View row = convertView;
		MainObjectHolder holder;

		if (row == null) {
			LayoutInflater inflater = ((Activity) context).getLayoutInflater();
			row = inflater.inflate(layoutResourceId, parent, false);
            // Holder holds references to list item data fields
			holder = new MainObjectHolder();
			holder.imgIcon = (ImageView) row.findViewById(R.id.iconRideType);
			holder.txtSummary= (TextView) row.findViewById(R.id.txtRideSummary);
			holder.txtCo2 = (TextView) row.findViewById(R.id.txtTripCO2);
			holder.txtCost = (TextView) row.findViewById(R.id.txtCost);
			// TODO Calories not applicable to Laddbil
			// holder.txtHelth = null; // (TextView) row.findViewById(R.id.txtHelth); // Crash for now, the ultimate TODO mark

			holder.txtDate = (TextView) row.findViewById(R.id.txtRideDate);

			row.setTag(holder);
		} else {
			holder = (MainObjectHolder) row.getTag();
		}

		Trip trip = getItem(position);

		TripResult result = TripCalculator.calculate(trip, null, null, 0, false);

		Date d=trip.getStartdate();
		// Format date info in swedish
		Locale swedish = new Locale("sv", "SV");
		Calendar calendar = new GregorianCalendar();		
		calendar.setTime(d);
		// Empiric tests showed formatting "%te%tb" crashes, separate conversions work..
		String day = String.format(swedish,"%te",calendar);
		String month = String.format(swedish,"%tb",calendar);	

	    holder.txtDate.setText(day+month);
		// TODO Study data and format for Laddbil needs
		// Get parameters for summary string
		String sTime=context.getString(R.string.time_xxx, formatIntoHHMMSS(result.time));
		String sDist=context.getString(R.string.km, result.length / 1000);
		String sSpeed=context.getString(R.string.avarage_speed_xxx, result.speed);
		String summary=sTime+" min - "+sDist+" km - "+sSpeed+" km/h";

		holder.txtSummary.setText(summary);
		holder.txtCo2.setText(context.getString(R.string.co2_xxx, result.co2_a/1000));
		holder.txtCost.setText(context.getString(R.string.cost_xxx, result.cost_a));

		if (trip.getType() == 2)  // Electric
			holder.imgIcon.setImageResource(R.drawable.el_logo_black);  // Display "electric" logo 
		else
		 holder.imgIcon.setImageResource(R.drawable.bensin_logo_black); // All others indicate fossil

		return row;
	}

	// Holds variables for ListView entry, see "listview_item_row.xml"
	
	// The separate fields for distance, time and average speed is now 
	// formatted into a common TextView "txtSummary" but were separate for elcykel.
	static class MainObjectHolder {
		ImageView imgIcon;
		public TextView txtDate;
		public TextView txtCost;
		public TextView txtCo2;
		public TextView txtSummary;
	}

	public static String formatIntoHHMMSS(double diff) {
		int diffInSec = (int) diff;

		int hours = diffInSec / 3600, remainder = diffInSec % 3600, minutes = remainder / 60, seconds = remainder % 60;

		return ((hours > 0 ? hours + "h " : "") + (minutes > 0 ? minutes + "m " : "") + seconds + "s");

	}

}