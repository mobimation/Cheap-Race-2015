package tv.laidback.cheaprace2015.compatibility;

import java.util.List;

import tv.laidback.cheaprace2015.MainActivity;
import tv.laidback.cheaprace2015.enteties.RaceLocation;

import android.location.Location;
import android.util.Log;

public class TripCalculator {
	private static String TAG = TripCalculator.class.getName();
	
	public static double calculateAveragespeed(double currentTripLengthInMeters, double timeInMilli) {

		double km = currentTripLengthInMeters / 1000;
		double hours = (timeInMilli / 1000) / 3600;
		if (km > 0 && hours > 0) {
			return (km / hours);
		}
		return 0;
	}
	
	// Return CO2 emission in kilograms per distance for a vehicle type
	public static double calculateCO2(int vehicleType, double metres) {
		double co2=0;
		switch (vehicleType) {
		case MainActivity.ELECTRIC_BIKE:
			co2=(0.006*metres)/1000;
			Log.e (TAG, "Electric bike CO2 calculation not supposed to occur in Laddbil");
			break;
		case MainActivity.ELECTRIC:
			co2=(0.0139*metres)/1000;
			break;
		case MainActivity.HYBRID:
			co2=(0.0564*metres)/1000;
			break;
		case MainActivity.PETROL:
			co2=(0.1918*metres)/1000;
			break;
		case MainActivity.DIESEL:
			co2=(0.1641*metres)/1000;
			break;
		default: 
			Log.e (TAG, "*** Contact programmer at once ***; vehicleType ="+vehicleType+" !!");
			break;
				}
		return co2;
	}
	
	// Return Fuel usage in litres for given distance for a vehicle type
	public static double calculateFuelVol(int vehicleType, double metres) {
		double lpk=0;  // Litres per kilometre
		switch (vehicleType) {
		case MainActivity.ELECTRIC_BIKE:
			// Nothing to do - go Electric !
			Log.e (TAG, "Electric bike fuel volume calculation not supposed to occur in Laddbil");
			break;
		case MainActivity.ELECTRIC:
			// Nothing to do - go Electric !
			break;
		case MainActivity.HYBRID:
			lpk=(0.0193*metres)/1000;
			break;
		case MainActivity.PETROL:
			lpk=(0.0825*metres)/1000;
			break;
		case MainActivity.DIESEL:
			lpk=(0.0631*metres)/1000;
			break;
		default: Log.e (TAG, "*** Contact programmer at once ***; vehicleType ="+vehicleType+" !!");
			break;
				}
		return lpk;
	}
	
	// Return Fuel cost for given distance for a vehicle type
	public static double calculateCost(int vehicleType, double metres) {
		double cpkm=0;  // Cost per kilometre
		switch (vehicleType) {
		case MainActivity.ELECTRIC_BIKE:
			cpkm=(0.006*metres)/1000;
			Log.e (TAG, "Electric bike fuel volume calculation not supposed to occur in Laddbil");
			break;
		case MainActivity.ELECTRIC:
			cpkm=(0.139*metres)/1000;
			break;
		case MainActivity.HYBRID:
			cpkm=(0.412*metres)/1000;
			break;
		case MainActivity.PETROL:
			cpkm=(1.244*metres)/1000;
			break;
		case MainActivity.DIESEL:
			cpkm=(0.901*metres)/1000;
			break;
		default: Log.e (TAG, "*** Contact programmer at once ***; vehicleType ="+vehicleType+" !!");
			break;
				}
		return cpkm;
	}
	
	/*  Was used in Elcykel
	public static double calculateCalUsageForBike(double meters) {
		return meters * 32 / 1000;
	}

	public static double calculateCalUsageForCar(long seconds) {
		return seconds / 4 / 60;
	}

	public static double calculateCo2UsageForBike(double meters) {
		return meters * 0.172; // Co2 in gram
	}

	public static double calculateCo2UsageForCar(double meters) {
		return meters / 4.48;
	}
*/
	public static float calculateDistanceOfLocations(List<RaceLocation> locations) {
		Location previusLocation = null;
		float meters = 0f;
		for (RaceLocation location : locations) {

			Location currentLocation = new Location("");
			currentLocation.setLatitude(location.getLatitude());
			currentLocation.setLongitude(location.getLongitude());

			if (previusLocation != null) {

				meters += previusLocation.distanceTo(currentLocation);

			}

			previusLocation = currentLocation;

		}
		return meters;
	}

	public static double calculateDistanceOfLocations(RaceLocation a, RaceLocation locationInfo) {
		float meters;

		Location aLocation = new Location("");
		aLocation.setLatitude(a.getLatitude());
		aLocation.setLongitude(a.getLongitude());

		Location bLocation = new Location("");
		bLocation.setLatitude(locationInfo.getLatitude());
		bLocation.setLongitude(locationInfo.getLongitude());

		meters = aLocation.distanceTo(bLocation);

		return meters;
	}
/*
	public static double calculateSekUsageForBike(double meters) {
		return meters * 0.1 / 1000;
	}

	public static double calculateSekUsageForCar(double meters) {
		return meters * 1.85 / 1000;
	}
*/
	public static float calculateTimeInMilliseconds(List<RaceLocation> locations) {
		if (locations.size() > 1) {
			RaceLocation first = locations.get(0);
			RaceLocation last = locations.get(locations.size() - 1);

			return last.getTimestamp().getTime() - first.getTimestamp().getTime();
		}
		return 0;
	}

	public static TripResult calculate(tv.laidback.cheaprace2015.enteties.Trip trip, RaceLocation first, RaceLocation last, int vs, boolean doNetworkRequests) {
		TripResult result = new TripResult();

		result.time = trip.getTimeS();
		result.speed = calculateAveragespeed(trip.getDistance(), trip.getTimeMs());
		result.length = trip.getDistance();

		result.co2_a=calculateCO2(trip.getType(),trip.getDistance());
		result.co2_b=calculateCO2(vs,trip.getDistance());
		
		result.cost_a=calculateCost(trip.getType(),trip.getDistance());
		result.cost_b=calculateCost(vs,trip.getDistance());
	
		if ((first!=null) && (last!=null)) {
			
		// Looks wierd to make a http request to Google every five seconds
		// to calculate elapsed time and distance. 
		// We can calculate that from position data. 
			
		/* So skip the following for now	
			
		  String url = String.format(Locale.ENGLISH, "http://maps.googleapis.com/maps/api/directions/json?origin=%f,%f&destination=%f,%f&sensor=false", first.getLatitude(),
					first.getLongitude(), last.getLatitude(), last.getLongitude());

		  // Create a new RestTemplate instance
		  RestTemplate restTemplate = new RestTemplate();

		  // Add the Simple XML message converter
		  restTemplate.getMessageConverters().add(new MappingJacksonHttpMessageConverter());
		  restTemplate.getMessageConverters().add(new StringHttpMessageConverter());

		  // Make the HTTP GET request, marshaling the response from
		  // XML to an EventList object
		  DirectionsResponse eventList = restTemplate.getForObject(url, DirectionsResponse.class);

		  if (eventList != null) { // TODO ??? Check validity of this data
		   int carDistanceInMeters = eventList.getRoutes().get(0).getLegs().get(0).getDistance().getValue().intValue();
		   int carTravelTimeInSeconds = eventList.getRoutes().get(0).getLegs().get(0).getDuration().getValue().intValue();
*/
		   // Instead do the calculation from location data
		   int carTravelTimeInSeconds=new Long(trip.getTimeMs()/1000).intValue();
		   double d=trip.getDistance();
		   long lo=(long)Math.floor(d + 0.5d);
		   int carDistanceInMeters=new Long(lo).intValue();
			
		   // Assemble the result
  		   result.time = carTravelTimeInSeconds;
		   result.speed = calculateAveragespeed(carDistanceInMeters, carTravelTimeInSeconds * 1000);
		   result.length = carDistanceInMeters;
		   result.fuel_a = calculateFuelVol(trip.getType(),trip.getDistance());
		   result.fuel_b = calculateFuelVol(trip.getCompared(),trip.getDistance());
		   result.saved_fuel = result.fuel_b-result.fuel_a;
		   result.saved_CO2 = result.co2_b-result.co2_a;
		   result.saved_cost = result.cost_b-result.cost_a;
		  }
		  else
	        Log.w(TAG, "no location info returns zero values for time,speed,length,fuel,co2,cost..");
		return result;
	}
}
