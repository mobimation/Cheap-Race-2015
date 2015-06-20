package tv.laidback.cheaprace2015;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.content.LocalBroadcastManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;


/**
 * A simple {@link Fragment} subclass.
 */
public class TeamsFragment extends Fragment {
    private static View view;
    /**
     * The fragment argument representing the section number for this
     * fragment.
     */
    static String ARG_SECTION_NUMBER = "section_number";

    /**
     * Returns a new instance of this fragment for the given section
     * number.
     */
    public static TeamsFragment newInstance(int sectionNumber) {
        TeamsFragment fragment = new TeamsFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_SECTION_NUMBER, sectionNumber);
        fragment.setArguments(args);
        return fragment;
    }

    public TeamsFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        view=inflater.inflate(R.layout.fragment_teams, container, false);
        IntentFilter filter=new IntentFilter();
        filter.addAction("ping");
        final TextView serviceMessage=(TextView)view.findViewById(R.id.serviceMessage);

        BroadcastReceiver updateUIReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if (intent.equals("ping"))
                    if (intent.hasExtra("greeting")) {
                        serviceMessage.setText(intent.getStringExtra("greeting"));
                    }
            }
        };
        LocalBroadcastManager.getInstance(getActivity().getBaseContext()).registerReceiver(updateUIReceiver, filter);
        return view;
    }
}
