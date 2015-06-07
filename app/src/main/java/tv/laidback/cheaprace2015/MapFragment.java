package tv.laidback.cheaprace2015;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

/**
 * A fragment that launches a Google Maps view
 */
public class MapFragment extends Fragment {

    private static View view;

    GoogleMap gMap;
    Double latitude, longitude;

    public MapFragment() {
        // Required empty public constructor
    }

    /**
     * The fragment argument representing the section number for this
     * fragment.
     */
    private static final String ARG_SECTION_NUMBER = "section_number";
    private static final String TAG = MapFragment.class.getSimpleName();
    /**
     * Returns a new instance of this fragment for the given section
     * number.
     */
    public static MapFragment newInstance(int sectionNumber) {
        MapFragment fragment = new MapFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_SECTION_NUMBER, sectionNumber);
        fragment.setArguments(args);
        return fragment;
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        Log.d(TAG,"onCreateView()");
        // inflate and return the layout
        view = inflater.inflate(R.layout.fragment_map, container, false);

        // Eastbourne XJ Restorations
        latitude = 50.780186;
        longitude = 0.287187;

        setUpMapIfNeeded();

        return view;
    }

    /***** Sets up the map if it is possible to do so *****/
    public void setUpMapIfNeeded() {
        // Do a null check to confirm that we have not already instantiated the map.
        if (gMap == null) {
            // Try to obtain the map from the SupportMapFragment
            android.support.v4.app.FragmentManager fm = getChildFragmentManager();
            SupportMapFragment smf = ((SupportMapFragment) fm.findFragmentById(R.id.location_map));

            // gMap = smf.getMap();
            smf.getMapAsync(
                    new OnMapReadyCallback() {
                        @Override
                        public void onMapReady(GoogleMap googleMap) {
                            Log.d(TAG,"onMapReady() #1");
                            setUpMap(googleMap);
                        }
                    });
       /*     // Check if we were successful in obtaining the map.
            if (gMap != null)
                setUpMap();
        } */
        }
    }

    /**
     * This is where we can add markers or lines, add listeners or move the
     * camera.
     * <p>
     * This should only be called once and when we are sure that {@link #gMap}
     * is not null.
     */
    private void setUpMap(GoogleMap map) {
        gMap=map;
        // For showing a move to my loction button
        gMap.setMyLocationEnabled(true);
        // For dropping a marker at a point on the Map
        gMap.addMarker(new MarkerOptions().position(new LatLng(latitude, longitude)).title("My Home").snippet("Home Address"));
        // For zooming automatically to the Dropped PIN Location
        gMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(latitude,
                longitude), 7.64f));
        gMap.setMapType(GoogleMap.MAP_TYPE_SATELLITE);
        gMap.setOnCameraChangeListener(new GoogleMap.OnCameraChangeListener() {
            private float currentZoom = -1;

            @Override
            public void onCameraChange(CameraPosition pos) {
                if (pos.zoom != currentZoom) {
                    currentZoom = pos.zoom;
                    Log.d(TAG, "Zoom changed to " + currentZoom);
                }
            }
        });
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        Log.d(TAG,"onViewCreated()");
        // TODO Auto-generated method stub
        if (gMap != null)
            setUpMap(gMap);

        if (gMap == null) {
            // Try to obtain the map from the SupportMapFragment.
            ((SupportMapFragment) getChildFragmentManager()
                    .findFragmentById(R.id.location_map)).getMapAsync(new OnMapReadyCallback() {
                @Override
                public void onMapReady(GoogleMap googleMap) {
                    Log.d(TAG,"onMapReady() #2");
                    setUpMap(googleMap);
                }
            }); // getMap is deprecated
 /*           // Check if we were successful in obtaining the map.
            if (gMap != null)
                setUpMap(gMap);
                */
        }
    }

    /**** The mapfragment's id must be removed from the FragmentManager
     **** or else if the same it is passed on the next time then
     **** app will crash ****/

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        Log.d(TAG, "onDestroyView()");
        if (gMap != null) {
            getChildFragmentManager().beginTransaction()
                    .remove(getChildFragmentManager().findFragmentById(R.id.location_map)).commit();
            gMap = null;
        }
    }



}



