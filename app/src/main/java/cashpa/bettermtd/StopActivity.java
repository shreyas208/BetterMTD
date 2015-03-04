package cashpa.bettermtd;

import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class StopActivity extends ActionBarActivity {

    private static String baseURL = "https://developer.cumtd.com/api/v2.2/json/GetDeparturesByStop";
    public List<NameValuePair> params;

    // JSON Node names
    private static final String TAG_TIME = "time";
    private static final String TAG_DEPARTURES = "departures";
    private static final String TAG_STOPID = "stop_id";
    private static final String TAG_HEADSIGN = "headsign";
    private static final String TAG_ROUTE = "route";
    private static final String TAG_ROUTECOLOR = "route_color";
    private static final String TAG_ROUTEID = "route_id";
    private static final String TAG_ROUTELONGNAME = "route_long_name";
    private static final String TAG_ROUTESHORTNAME = "route_short_name";
    private static final String TAG_ROUTETEXTCOLOR = "route_text_color";
    private static final String TAG_VEHICLEID = "vehicle_id";
    private static final String TAG_ISISTOP = "is_istop";
    private static final String TAG_EXPECTEDMINS = "expected_mins";

    public ListView list;
    JSONArray departures = null;
    ArrayList<HashMap<String, String>> departureList;
    public RecyclerView recyclerView;
    public Context context;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Intent intent = getIntent();
        setContentView(R.layout.activity_stop);
        String stop = intent.getStringExtra(MainActivity.ARG_STOPID);
        context = getApplicationContext();
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);

        toolbar.setTitle(stop);
        toolbar.setTitleTextColor(-1);
        setSupportActionBar(toolbar);

        departureList = new ArrayList<HashMap<String, String>>();

        //list = (ListView) this.findViewById(R.id.list);

        recyclerView = (RecyclerView) findViewById(R.id.recyclerView);

        // Uses linear layout manager for simplicity
        final LinearLayoutManager layoutManager = new LinearLayoutManager(context);
        layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        recyclerView.setLayoutManager(layoutManager);

        // Sets up base URL and populates parameters to load
        params = new ArrayList<NameValuePair>();
        params.add(new BasicNameValuePair("key","***REMOVED***"));
        params.add(new BasicNameValuePair("stop_id", stop));

        new HTTPStopRequest().execute();

    }

    private class HTTPStopRequest extends AsyncTask<Void, Void, Void>
    {
        protected void onPreExecute()
        {
            super.onPreExecute();
            // TODO perhaps add some UI element to indicate task is in progress?
        }

        protected Void doInBackground(Void ... arg0) {
            ServiceHandler sh = new ServiceHandler();
            String jsonStr = sh.makeServiceCall(baseURL, params);

            Log.d("Response: ", "> " + jsonStr);

            try {
                if (jsonStr != null) {
                    // Get JSON Object from string in ServiceHandler response
                    JSONObject jsonObj = new JSONObject(jsonStr);

                    departures = jsonObj.getJSONArray(TAG_DEPARTURES);

                    for (int i = 0; i < departures.length(); i++) {
                        JSONObject c = departures.getJSONObject(i);

                        String stopID = c.getString(TAG_STOPID);
                        String headSign = c.getString(TAG_HEADSIGN);

                        String vehicleID = c.getString(TAG_VEHICLEID);
                        String expectedMins = c.getString(TAG_EXPECTEDMINS);

                        HashMap<String, String> departure = new HashMap<String, String>();

                        departure.put(TAG_STOPID, stopID);
                        departure.put(TAG_HEADSIGN, headSign);
                        departure.put(TAG_VEHICLEID, vehicleID);
                        departure.put(TAG_EXPECTEDMINS, expectedMins);

                        departureList.add(departure);
                    }
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }

            return null;
        }

        protected void onPostExecute(Void result) {
            // TODO populate UI with data when done
            super.onPostExecute(result);

            /**
             * Updating parsed JSON data into ListView
             *
            ListAdapter adapter = new SimpleAdapter(
                    getApplicationContext(), departureList,
                    R.layout.list_item, new String[] { TAG_HEADSIGN, TAG_EXPECTEDMINS
            }, new int[] { R.id.headsign,
                    R.id.expectedmins });

            list.setAdapter(adapter);
            */

            // Passes data to adapter to set content
            RecyclerView.Adapter adapter = new RecyclerViewAdapter(context, departureList);
            recyclerView.setAdapter(adapter);

        }

    }

}