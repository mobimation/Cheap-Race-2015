package tv.laidback.cheaprace2015;

import java.util.Locale;

import android.content.ComponentName;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.support.v4.app.FragmentActivity;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.app.ActionBar;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.app.FragmentPagerAdapter;
import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;


public class MainActivity extends FragmentActivity implements ViewPager.OnPageChangeListener{
    /**
     * The {@link android.support.v4.view.PagerAdapter} that will provide
     * fragments for each of the sections. We use a
     * {@link FragmentPagerAdapter} derivative, which will keep every
     * loaded fragment in memory. If this becomes too memory intensive, it
     * may be best to switch to a
     * {@link android.support.v4.app.FragmentStatePagerAdapter}.
     */
    SectionsPagerAdapter mSectionsPagerAdapter;
    private static final String TAG = MainActivity.class.getSimpleName();
    /**
     * The {@link ViewPager} that will host the section contents.
     */
    ViewPager mViewPager;

    /**
     * Manage service interaction when UI rotates.
     * Rotation causes the activity to die. So it is relaunched.
     * When that happens it needs to reconnect to a running service.
     * @return
     */
    private ServiceConnection sc() {
        Log.i(TAG,"Instantiating ServiceConnection object");
        ServiceConnection sconn = new ServiceConnection() {
            public void onServiceConnected(ComponentName className, IBinder service) {
                // This is called when the connection with the service has been
                // established, giving us the service object we can use to
                // interact with the service. Because we have bound to a explicit
                // service that we know is running in our own process, we can
                // cast its IBinder to a concrete class and directly access it.
                Log.i(TAG,"onServiceConnected()");

                mBoundService = ((LocalisationService.LocalBinder) service)
                        .getService();
                mIsBound = true;
                doBindService();
                setupGui();  // Setup GUI that displays service progress
                refreshDisplay(); // Added in version 40
                btnStartRide.setVisibility(View.GONE);
            }

            public void onServiceDisconnected(ComponentName className) {
                // This is called when the service stops running
                Log.i(TAG,"onServiceDisconnected()");
                mBoundService = null;

                // Toast.makeText(MainActivity.this,
                // R.string.local_service_disconnected, Toast.LENGTH_SHORT).show();
                doUnbindService();
                mIsBound=false;
                setupGui();
                // wait for a new connection
                //doBindService();
            }
        };
        return sconn;
    }

    void doBindService() {
        // Care to bind with the service only if it´s running.
        // That way we avoid ServiceConnection leak errors in the event log..
        // Call this one from onResume or onServiceConnected...
        if (LocalisationService.isInstanceCreated()) {  // If service running
            // In case the system kills the service we are in trouble but for Laddbil
            // we can afford to gamble on it not happening. The trouble being that
            // onDestroy of the service is not called in that special case and
            // our little isInstanceCreated solution will be indicating things are still running.
            if (mConnection==null)
                mConnection=sc();
            Log.i(TAG,"binds LocalisationService");
            bindService(new Intent(MainActivity.this, LocalisationService.class),
                    mConnection, 0);
        }
    }

    void doUnbindService() {
        if (mIsBound) {
            // Unbind service, call this method upon onDestroyed, onPause, onStop
            //
            Log.i(TAG,"unbinds LocalisationService");
            unbindService(mConnection);
            mIsBound = false;
            setupGui();
        }
    }

    private LocalisationService mBoundService;
    private boolean mIsBound = false;
    /**
     * Constants that define the possible vehicle types (1..4)
     */
    public static final int ELECTRIC_BIKE=0;
    public static final int ELECTRIC = 2;
    public static final int HYBRID = 3;
    public static final int PETROL = 1;
    public static final int DIESEL = 4;


    public static final String[] vehicleNames={
            "Elcykel",
            "Bensinbil",
            "Elbil",
            "Laddhybrid",
            "Dieselbil"
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Request fullscreen
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_main);

        // Create the adapter that will return a fragment for each of the
        // primary sections of the activity.
        mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());
        // Set up the ViewPager with the sections adapter.
        mViewPager = (ViewPager) findViewById(R.id.pager);
        mViewPager.setAdapter(mSectionsPagerAdapter);
        mViewPager.setOnPageChangeListener(this);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    /**
     * This method will be invoked when the current page is scrolled, either as part
     * of a programmatically initiated smooth scroll or a user initiated touch scroll.
     *
     * @param position             Position index of the first page currently being displayed.
     *                             Page position+1 will be visible if positionOffset is nonzero.
     * @param positionOffset       Value from [0, 1) indicating the offset from the page at position.
     * @param positionOffsetPixels Value in pixels indicating the offset from position.
     */
    @Override
    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
        Log.d(TAG, "onPageScrolled()");
    }

    /**
     * This method will be invoked when a new page becomes selected. Animation is not
     * necessarily complete.
     *
     * @param position Position index of the new selected page.
     */
    @Override
    public void onPageSelected(int position) {

    }

    /**
     * Called when the scroll state changes. Useful for discovering when the user
     * begins dragging, when the pager is automatically settling to the current page,
     * or when it is fully stopped/idle.
     *
     * @param i The new scroll state.
     * @see ViewPager#SCROLL_STATE_IDLE
     * @see ViewPager#SCROLL_STATE_DRAGGING
     * @see ViewPager#SCROLL_STATE_SETTLING
     */
    @Override
    public void onPageScrollStateChanged(int i) {
        Log.d(TAG,"onPageScrollStateChanged(), i="+i);
    }


    /**
     * A {@link FragmentPagerAdapter} that returns a fragment corresponding to
     * one of the sections/tabs/pages.
     */
    public class SectionsPagerAdapter extends FragmentPagerAdapter {

        public SectionsPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            // getItem is called to instantiate the fragment for the given page.
            // Return a PlaceholderFragment (defined as a static inner class below).
            Log.d("SectionsPagerAdapter", "getItem() - position=" + position+1);
            // The sliding pages and their positions:
            switch (position) {
                case 0:
                    return TeamsFragment.newInstance(position+1);
                case 1:
                    return MapFragment.newInstance(position+1);
                case 2:
                     return WalkieTalkieFragment.newInstance(position+1);
                case 3:
                    return ShareFragment.newInstance(position+1);
            }
            return TeamsFragment.newInstance(position+1);
        }

        @Override
        public int getCount() {
            // Total pages.
            return 4;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            Locale l = Locale.getDefault();
            switch (position) {
                case 0:
                    return "TEAMS";
                case 1:
                    return "MAP";
                case 2:
                    return "Walkie Talkie";
                case 3:
                    return "Share";
            }
            return null;
        }
    }

    /**
     * A placeholder fragment containing a simple view.
     */
 //   public static class PlaceholderFragment extends Fragment {
        /**
         * The fragment argument representing the section number for this
         * fragment.
         */
  //      private static final String ARG_SECTION_NUMBER = "section_number";

        /**
         * Returns a new instance of this fragment for the given section
         * number.
         */
   /*     public static PlaceholderFragment newInstance(int sectionNumber) {
            PlaceholderFragment fragment = new PlaceholderFragment();
            Bundle args = new Bundle();
            args.putInt(ARG_SECTION_NUMBER, sectionNumber);
            fragment.setArguments(args);
            return fragment;
        }

        public PlaceholderFragment() {
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.fragment_main, container, false);
            return rootView;
        }
    } */

}
