package mtrbw;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.mtrbw.mtrbw.R;

import mtrbw.app.AppConfig;
import mtrbw.app.AppController;
import mtrbw.helper.SQLiteHandler;
import mtrbw.helper.SessionManager;

import java.util.HashMap;
import java.util.Map;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

public class MainActivity extends Activity {

    private static final String TAG = MainActivity.class.getSimpleName();
    private TextView txtEmail;
	private Button btnLogout;
    private Button btnShowValues;
    private Button btnSendValues;
    private ProgressDialog pDialog;
    private float lastSpeed = 0;
    private float speed = 0;
    private Location centerPoint = new Location("");

	private SQLiteHandler db;
	private SessionManager session;

    private boolean WiFiIsConnected() {
        ConnectivityManager manager = (ConnectivityManager) getSystemService(CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = manager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
        return netInfo.isConnected();
    }

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		txtEmail = (TextView) findViewById(R.id.email);
		btnLogout = (Button) findViewById(R.id.btnLogout);
        btnShowValues = (Button) findViewById(R.id.btnShowValues);
        btnSendValues = (Button) findViewById(R.id.btnSendValues);

        // Progress dialog
        pDialog = new ProgressDialog(this);
        pDialog.setCancelable(false);

		// SqLite database handler
		db = new SQLiteHandler(getApplicationContext());

		// Session manager
		session = new SessionManager(getApplicationContext());

		if (!session.isLoggedIn()) {
			logoutUser();
		}

		// Fetching user details from sqlite
		HashMap<String, String> user = db.getUserDetails();
		String email = user.get("email");
        String uid = user.get("uid");

		// Displaying the user details on the screen
        txtEmail.setText(email + ".");

		// Logout button click event
		btnLogout.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                logoutUser();
            }
        });

        //Button Show Values click event
        btnShowValues.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                showValues();
            } });

        //Button Send Values click event
        btnSendValues.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                sendValues();
            }
        });

        //Setting up the tracker
        LocationManager locManager = (LocationManager)getSystemService(Context.LOCATION_SERVICE);
        LocationListener locListener = new MyLocationListener();

        //Update frequency is set to 200ms.
        locManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 200, 0, locListener);

        //Registering WiFiReceiver
        WiFiReceiver wifi =  new WiFiReceiver();
        IntentFilter intentFilter = new IntentFilter("android.net.wifi.STATE_CHANGE");
        registerReceiver(wifi, intentFilter);

        //If selected, the center point of the circular area to monitor is needed as a location object.
        if (AppConfig.limitedArea){
            centerPoint.setLatitude(AppConfig.centerLat);
            centerPoint.setLongitude(AppConfig.centerLon);
        }


    }


	/**
	 * Logging out the user. Will set isLoggedIn flag to false in shared
	 * preferences Clears the user data from sqlite users table
	 * */
	private void logoutUser() {
		session.setLogin(false);

		db.deleteUsers();

		// Launching the login activity
		Intent intent = new Intent(MainActivity.this, LoginActivity.class);
		startActivity(intent);
		finish();
	}

    private void showValues() {

            ListView listViewCoords = (ListView) findViewById(R.id.list_view_locations);

            ArrayAdapter<String> coordsAdapter = new ArrayAdapter<String>(this,
                    android.R.layout.simple_list_item_1, android.R.id.text1, db.getAllLocations());
            listViewCoords.setAdapter(coordsAdapter);

        if(db.numberOfLocations() == 0) {
            Toast.makeText(getApplicationContext(), "No values to show.", Toast.LENGTH_SHORT).show();
        }
    }

    public void sendValues() {

        if(db.numberOfLocations()>0) {

            // Tag used to cancel the request
            String tag_string_req = "req_login";

            pDialog.setMessage("Sending values ...");
            showDialog();

            StringRequest strReq = new StringRequest(Request.Method.POST,
                    AppConfig.URL_REGISTER, new Response.Listener<String>() {

                @Override
                public void onResponse(String response) {
                    Log.d(TAG, "Response: " + response.toString());
                    hideDialog();

                    try {
                        JSONObject jObj = new JSONObject(response);
                        boolean error = jObj.getBoolean("error");

                        // Check for error node in json
                        if (!error) {
                            // Werte erfolgreich übertragen, lokal gespeicherte Locations löschen
                            db.truncateLocations();

                            Toast.makeText(getApplicationContext(),
                                    "Values sent.", Toast.LENGTH_LONG).show();


                        } else {
                            // Transmission error
                            String errorMsg = jObj.getString("error_msg");
                            Toast.makeText(getApplicationContext(),
                                    errorMsg, Toast.LENGTH_LONG).show();
                        }
                    } catch (JSONException e) {
                        // JSON error
                        e.printStackTrace();
                    }

                }
            }, new Response.ErrorListener() {

                @Override
                public void onErrorResponse(VolleyError error) {
                    Log.e(TAG, "Transmission error: " + error.getMessage());
                    Toast.makeText(getApplicationContext(),
                            error.getMessage(), Toast.LENGTH_LONG).show();
                    hideDialog();
                }
            }) {

                @Override
                protected Map<String, String> getParams() {
                    // Posting parameters to login url
                    HashMap<String, String> user = db.getUserDetails();
                    String loctable = user.get("loctable");

                    Map<String, String> params = new HashMap<String, String>();
                    params.put("tag", "locations");
                    params.put("loctable", loctable);
                    params.put("values", db.getValues());

                    return params;
                }


            };

            // Adding request to request queue
            AppController.getInstance().addToRequestQueue(strReq, tag_string_req);
        }
        else{
            Toast.makeText(getApplicationContext(), "No values to send.", Toast.LENGTH_SHORT).show();
        }
    }

    private void showDialog() {
        if (!pDialog.isShowing())
            pDialog.show();
    }

    private void hideDialog() {
        if (pDialog.isShowing())
            pDialog.dismiss();
    }

    private class MyLocationListener implements LocationListener {

        @Override
        public void onLocationChanged(Location loc){

            speed = loc.getSpeed();

            //Locations are only saved, when the device is moving. Incorrect measurements (speed > 0 when the device is in fact not moving) are filtered.
            if (speed > 0 && lastSpeed > 0) {

                //If selected, only locations within a certain area are saved.
                if (!AppConfig.limitedArea | loc.distanceTo(centerPoint) <= AppConfig.radius) {

                    double latitude = loc.getLatitude();
                    double longitude = loc.getLongitude();
                    float bearing = loc.getBearing();
                    long time = loc.getTime();

                    //Saving to the local database.
                    db.addLocation(latitude, longitude, bearing, speed, time);
                }

            }

            lastSpeed = speed;

        }

        @Override
        public void onProviderDisabled(String provider){
            Toast.makeText(getApplicationContext(), "GPS deactivated", Toast.LENGTH_SHORT).show();
        }

        @Override
        public void onProviderEnabled(String provider){
            Toast.makeText(getApplicationContext(), "GPS activated", Toast.LENGTH_SHORT).show();
        }

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras){

        }
    }

    private class WiFiReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            if (WiFiIsConnected() && db.numberOfLocations() > 100) {
                sendValues();
            }
        }
    }
}
