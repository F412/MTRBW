package mtrbw;

import com.mtrbw.mtrbw.R;

import mtrbw.app.AppConfig;
import mtrbw.app.AppController;
import mtrbw.helper.SQLiteHandler;
import mtrbw.helper.SessionManager;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import com.android.volley.Request.Method;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;

public class RegisterActivity extends Activity {
	private static final String TAG = RegisterActivity.class.getSimpleName();
	private Button btnRegister;
	private Button btnLinkToLogin;
	private EditText inputEmail;
	private EditText inputPassword;
    private EditText inputPassword2;
	private ProgressDialog pDialog;
	private SessionManager session;
	private SQLiteHandler db;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_register);

		inputEmail = (EditText) findViewById(R.id.email);
		inputPassword = (EditText) findViewById(R.id.password);
        inputPassword2 = (EditText) findViewById(R.id.password2);
		btnRegister = (Button) findViewById(R.id.btnRegister);
		btnLinkToLogin = (Button) findViewById(R.id.btnLinkToLoginScreen);

        //Setting up the gender spinner
        ArrayList<String> gender = new ArrayList<String>();
        gender.add("Gender");
        gender.add("female");
        gender.add("male");
        ArrayAdapter<String> genderAdapter = new ArrayAdapter<String>(this, R.layout.spinner_item, gender);
        final Spinner spinGender = (Spinner)findViewById(R.id.genderSpinner);
        spinGender.setAdapter(genderAdapter);

        //Setting up the year spinner
        ArrayList<String> years = new ArrayList<String>();
        int thisYear = Calendar.getInstance().get(Calendar.YEAR);
        years.add("Year of birth");
        for (int i = 0; i < 120; i++) {
            years.add(Integer.toString(thisYear - i));
        }
        ArrayAdapter<String> yearAdapter = new ArrayAdapter<String>(this, R.layout.spinner_item, years);
        final Spinner spinYear = (Spinner)findViewById(R.id.yearSpinner);
        spinYear.setAdapter(yearAdapter);

		// Progress dialog
		pDialog = new ProgressDialog(this);
		pDialog.setCancelable(false);

		// Session manager
		session = new SessionManager(getApplicationContext());

		// SQLite database handler
		db = new SQLiteHandler(getApplicationContext());

		// Check if user is already logged in or not
		if (session.isLoggedIn()) {
			// User is already logged in. Take him to main activity
			Intent intent = new Intent(RegisterActivity.this,
					MainActivity.class);
			startActivity(intent);
			finish();
		}

		// Register Button Click event
		btnRegister.setOnClickListener(new View.OnClickListener() {
			public void onClick(View view) {
				String email = inputEmail.getText().toString();
				String password = inputPassword.getText().toString();
                String password2 = inputPassword2.getText().toString();
                String gender = "";
                String year = "0";

                if (spinGender.getSelectedItem().toString().equals("female")){
                    gender = "f";
                }

                if (spinGender.getSelectedItem().toString().equals("male")){
                    gender = "m";
                }

                if (!spinYear.getSelectedItem().toString().equals("Year of birth")){
                    year = spinYear.getSelectedItem().toString();
                }


				if (!email.equals("") && !password.equals("") && !password2.equals("")) {
                    if (password.equals(password2)){
                        registerUser(email, password, gender, year);
                    }
                    else {
                        Toast.makeText(getApplicationContext(),
                                "Passwords don't match.", Toast.LENGTH_LONG)
                                .show();
                    }

				} else {
					Toast.makeText(getApplicationContext(),
							"Please fill in at least the first three fields.", Toast.LENGTH_LONG)
							.show();
				}

			}
		});

		// Link to Login Screen
		btnLinkToLogin.setOnClickListener(new View.OnClickListener() {

			public void onClick(View view) {
				Intent i = new Intent(getApplicationContext(),
						LoginActivity.class);
				startActivity(i);
				finish();
			}
		});

	}

	/**
	 * Function to store user in MySQL database will post params(tag,
	 * email, password) to register url
	 * */
	private void registerUser(final String email,
			final String password, final String gender, final String year) {
		// Tag used to cancel the request
		String tag_string_req = "req_register";

		pDialog.setMessage("Registering ...");
		showDialog();

		StringRequest strReq = new StringRequest(Method.POST,
				AppConfig.URL_REGISTER, new Response.Listener<String>() {

					@Override
					public void onResponse(String response) {
						Log.d(TAG, "Response: " + response.toString());
						hideDialog();

						try {
							JSONObject jObj = new JSONObject(response);
							boolean error = jObj.getBoolean("error");
							if (!error) {
								// User successfully stored in MySQL

								// Launch login activity
								Intent intent = new Intent(
										RegisterActivity.this,
										LoginActivity.class);
								startActivity(intent);
								finish();
							} else {

								// Error occurred in registration. Get the error
								// message
								String errorMsg = jObj.getString("error_msg");
								Toast.makeText(getApplicationContext(),
										errorMsg, Toast.LENGTH_LONG).show();
							}
						} catch (JSONException e) {
							e.printStackTrace();
						}

					}
				}, new Response.ErrorListener() {

					@Override
					public void onErrorResponse(VolleyError error) {
						Log.e(TAG, "Registration error: " + error.getMessage());
						Toast.makeText(getApplicationContext(),
								error.getMessage(), Toast.LENGTH_LONG).show();
						hideDialog();
					}
				}) {

			@Override
			protected Map<String, String> getParams() {
				// Posting params to register url
				Map<String, String> params = new HashMap<String, String>();
				params.put("tag", "register");
				params.put("email", email);
                params.put("gender", gender);
                params.put("year", year);
				params.put("password", password);

				return params;
			}

		};

		// Adding request to request queue
		AppController.getInstance().addToRequestQueue(strReq, tag_string_req);
	}

	private void showDialog() {
		if (!pDialog.isShowing())
			pDialog.show();
	}

	private void hideDialog() {
		if (pDialog.isShowing())
			pDialog.dismiss();
	}
}
