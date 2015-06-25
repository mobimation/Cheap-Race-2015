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
 * The TeamsFragment class deals with Cheap Race 2015 teams and their members.
 */
public class TeamsFragment extends Fragment {
    /**
     * teams represent an index 0..29 of the competing teams.
     * Each team consists of members which are accessible through the index.
     */
    private class Team {
        String teamName;
        String[] members;
        public Team(String teamName, String[]members) {
            this.teamName=teamName;
            this.members=members;
        }
    }
    private Team[] teams;

    /**
     * Instantiate teams and their members
     */
    public void TeamsFragment() {
        teams[0]=new Team("4GOM",members_4_GOM);
        teams[1]=new Team("Larborn/Svendsen",members_Larborn_Svendsen);
        teams[2]=new Team("Doherty",members_Doherty);
        teams[3]=new Team("Källarmästarna",members_Källarmästarna);
        teams[4]=new Team("Förbifarten",members_Förbifarten);
        teams[5]=new Team("Top Team North",members_Top_Team_North);
        teams[6]=new Team("Generacing",members_Generacing);
        teams[7]=new Team("Katta-Maran",members_Katta_Maran);
        teams[8]=new Team("Kattresan",members_Kattresan);
        teams[9]=new Team("Maja",members_Maja);
        teams[10]=new Team("Villovägens",members_Villovägens);
        teams[11]=new Team("Dalarö",members_Dalarö);
        teams[12]=new Team("Eldflugan",members_Eldflugan);
        teams[13]=new Team("Kattapult",members_Kattapult);
        teams[14]=new Team("SAC Nordic",members_SAC_Nordic);
        teams[15]=new Team("Far o Zon",members_Far_o_Zon);
        teams[16]=new Team("GORB",members_GORB);
        teams[17]=new Team("G Team",members_G_Team);
        teams[18]=new Team("Skrotbilsgänget",members_Skrotbilsgänget);
        teams[19]=new Team("R Green",members_R_Green);
        teams[20]=new Team("Flying Mats",members_Flying_Mats);
        teams[21]=new Team("Bacon",members_Bacon);
        teams[22]=new Team("Frille",members_Frille);
        teams[23]=new Team("El Gato Negro",members_El_Gato_Negro);
        teams[24]=new Team("Fat and Furious",members_Fat_and_Furious);
        teams[25]=new Team("Motorteknik",members_Motorteknik);
        teams[26]=new Team("Teknikens Värld",members_Teknikens_Värld);
    }

    /**
     * Members of each team
     */
    private final static String[] members_4_GOM = {
            "Peter Hällström",
            "Tony Hällström",
            "Pelle Norberg",
            "Patrik Hanberger",
    };

    private final static String[] members_Larborn_Svendsen = {
            "Pontus Larborn",
            "Tom Svendsen",
    };

    private final static String[] members_Doherty = {
            "Mattias Skarelius",
            "Jan Ernefors",
    };

    private final static String[] members_Källarmästarna = {
            "Rickard Fritz",
            "Linda Myrendal",
            "Marcus Fritz",
            "Sara Hadberg",
    };

    private final static String[] members_Förbifarten = {
            "Carina Fredlund",
            "Roger Timelin",
    };

    private final static String[] members_Top_Team_North = {
            "Keith Sivenbring",
            "Kenneth Sandström",
            "John Stenman",
    };

    private final static String[] members_Generacing = {
            "Peter Nordgren",
            "Johan Nordgren",
            "Gustav Nordgren",
    };

    private final static String[] members_Katta_Maran = {
            "Jörgen Dimenäs",
            "Gunn-Britt Häggblad Dimenäs",
            "Heléne Dimenäs",
            "Håkan Dimenäs",
    };

    private final static String[] members_Kattresan = {
            "Gunnar Forsgren",
            "Jan Runesten",
    };

    private final static String[] members_Maja = {
            "Magnus Kwarnmark",
            "Kerstin Kwarnmark",
            "Åsa Kwarnmark",
    };

    private final static String[] members_Villovägens = {
            "Sven Eklund",
            "Matilda Eklund",
    };

    private final static String[] members_Dalarö = {
            "Jonas Eriksson",
            "Kerstin Eriksson",
            "Elias Eriksson",
            "Tilde Eriksson",
    };

    private final static String[] members_Eldflugan = {
            "Kim Bergström",
            "Andreas Wahlberg",
            "Ola Eriksson",
            "Ulf Jesslén",
    };

    private final static String[] members_Kattapult = {
            "Katarina Gramatkovski",
            "lars Dahlin",
            "Rickard Hansson",
            "Johannes Grebelius",
    };

    private final static String[] members_SAC_Nordic = {
            "Bengt Ivar Johansson",
            "Kurt Lundberg",
            "Birgitta Johansson",
            "Kerstin Lundberg",
    };

    private final static String[] members_Far_o_Zon = {
            "Håkan Pekkari",
            "Joakim Pekkari",
            "Jan Thorström",
            "Marcus Thorström",
    };

    private final static String[] members_GORB = {
            "Ove Andersson",
            "Gertrud Andersson",
            "Riita Ripdal",
            "Bengt-Åke Malmgren",
    };

    private final static String[] members_G_Team = {
            "Kjell-Åke Gunnarsson",
            "Anna-Lena Erixon",
            "Caroline Magnevill",
    };

    private final static String[] members_Skrotbilsgänget = {
            "Daniel Pettersson",
            "David Gustavsson",
    };

    private final static String[] members_R_Green = {
            "Martin_Nilsson",
            "Magnus_Törnqvist",
    };

    private final static String[] members_Flying_Mats = {
            "Mats Haglund",
            "Mats Engberg",
    };

    private final static String[] members_Bacon = {
            "Stellan Schultz",
            "Karin Mårtensson",
            "Mikael Karlsson",
            "Camilla Käck",
    };

    private final static String[] members_Frille = {
            "Fredrik Olsson",
            "Emil Erixon",
    };

    private final static String[] members_El_Gato_Negro = {
            "Tina Korkemaa",
            "Juhani Kärkkäinen",
            "Amy Sautter",
    };

    private final static String[] members_Fat_and_Furious = {
            "Johan Emilsson",
            "Tobias Emilsson",
            "Peter Emilsson",
    };

    private final static String[] members_Motorteknik = {
            "Jan Axelsson",
            "Ann-Louise Johansson",
            "Thomas Oscarsson",
            "Gunilla Broström",
    };

    private final static String[] members_Teknikens_Värld = {
            "Mikael Stjerna",
            "Andreas Libell",
    };

    /**
     * Return array of team names
     * @return string array
     */
    public String[] getTeams() {
        String[]teamName=new String[teams.length];
        for (int x=0; x < teams.length; x++) {
            teamName[x]=new String((teams[x].teamName));
        }
        return teamName;
    }

    /**
     * Get members of a team
     * @param team
     * @return Team = String array of member names
     */
    public Team getMembers(String team) {
        for (int x=0; x < teams.length; x++) {
            if (teams[x].teamName.equals(team))
                return teams[x];
        }
        return null;
    }

    /**
     * Get name of team that a member belongs to
     * @param member
     * @return team (null=unknown member)
     */
    public String getTeam(String member) {
        for (int n2=0; n2<teams.length; n2++) {
            String[]members=teams[n2].members;
            for (int n3=0; n3<members.length; n3++)
                if (members[n3].equals(member))
                    return teams[n2].teamName;
        }
        return null;
    }

    TextView serviceMessage=null;
    final LocalBroadcastManager lbm=LocalBroadcastManager.getInstance(getActivity());
    int count=0;

    final BroadcastReceiver updateUIReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals("ping"))
                if (intent.hasExtra("Greeting"))
                    if (serviceMessage!=null)
                      serviceMessage.setText(intent.getStringExtra("Greeting ")+""+count++);
        }
    };

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
    public void onResume() {
        lbm.registerReceiver(updateUIReceiver, new IntentFilter("ping"));
        super.onResume();
    }
    @Override
    public void onPause() {
        lbm.unregisterReceiver(updateUIReceiver);
        super.onPause();
    }
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        view=inflater.inflate(R.layout.fragment_teams, container, false);
        serviceMessage=(TextView)view.findViewById(R.id.serviceMessage);

        // LocalBroadcastManager.getInstance(getActivity().getBaseContext()).registerReceiver(updateUIReceiver, filter);

        return view;
    }
}
