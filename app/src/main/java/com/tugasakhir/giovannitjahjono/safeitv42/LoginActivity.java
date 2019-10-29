package com.tugasakhir.giovannitjahjono.safeitv42;

import android.app.Fragment;
import android.app.FragmentTransaction;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.support.constraint.ConstraintLayout;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Text;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;

public class LoginActivity extends AppCompatActivity {
    //Prepare variable
    ListView listView;
    TextView email;
    TextView password;
    ProgressBar progressBar;
    public SharedPreferences sp;
    public SharedPreferences.Editor spe;
    ConstraintLayout constraintLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        constraintLayout = (ConstraintLayout) findViewById(R.id.constraintLayoutLogin);
        //Set the window fullscreen without status bar
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        //Get share preferences
        sp = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        email = (TextView) findViewById(R.id.txtEmail);
        password = (TextView) findViewById(R.id.txtPassword);
        progressBar = (ProgressBar) findViewById(R.id.progressBar);
        progressBar.setVisibility(View.INVISIBLE);
        Button login = (Button) findViewById(R.id.btnLogin);
        Button createAccount = (Button) findViewById(R.id.btnCreateAccount);
        String checkEmail = "";
        String checkUsername = "";
        String checkIdUser ="";
        //set the pattern for email
        final String emailPattern = "[a-zA-Z0-9._-]+@[a-z]+\\.+[a-z]+";
        //Try to get the data from share preference
        try {
            checkEmail = sp.getString("email_user", "");
            checkUsername = sp.getString("username", "");
            checkIdUser = sp.getString("id_user", "");

        }
        catch (Exception e) {

        }

        //check data, if it's exist means user has login before and rhe windows will redirect to main activity
        if(!checkEmail.equals("") && !checkUsername.equals("") && !checkIdUser.equals("")){
            Intent androidsolved_intent = new Intent(getApplicationContext(), MainActivity.class);
            startActivity(androidsolved_intent);
            finish();
        }

        login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //Set the progress bar visible that indicate process is running
                progressBar.setVisibility(View.VISIBLE);
                //close the keyboard
                InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
                final String emailUser = email.getText().toString();
                final String passwordUser = password.getText().toString();
                //check the form it's filled or not yet
                if(emailUser.equals("") && passwordUser.equals("")){
                    progressBar.setVisibility(View.INVISIBLE);
                    Snackbar.make(constraintLayout, "Please fill the form", Snackbar.LENGTH_LONG).show();
                }
                else if(emailUser.matches(emailPattern) && !passwordUser.equals("")){
                    //communicate with web service via getJSON method
                    try {
                        String url = "https://safeitdatabase.000webhostapp.com/login.php?email_user=" + emailUser +"&password_user=" +passwordUser;
                        getJSON(url);
                    } catch (Exception e){

                    }
                } else if(!emailUser.matches(emailPattern)){
                    progressBar.setVisibility(View.INVISIBLE);
                    Snackbar.make(constraintLayout, "Type your email correctly", Snackbar.LENGTH_LONG).show();
                } else if(emailUser.equals("")){
                    progressBar.setVisibility(View.INVISIBLE);
                    Snackbar.make(constraintLayout, "Type your email", Snackbar.LENGTH_LONG).show();
                } else {
                    progressBar.setVisibility(View.INVISIBLE);
                    Snackbar.make(constraintLayout, "Type your password", Snackbar.LENGTH_LONG).show();
                }
            }
        });
        createAccount.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent androidsolved_intent = new Intent(getApplicationContext(), CreateAccountActivity.class);
                startActivity(androidsolved_intent);

            }
        });

    }
    public void getJSON(final String urlWebService) {
        class GetJSON extends AsyncTask<Void, Void, String> {
            @Override
            protected void onPreExecute() {
                super.onPreExecute();
            }
            @Override
            protected void onPostExecute(String s) {
                super.onPostExecute(s);
                if(!s.equals("false")){
                    try {
                        StoreDataToSP(s);
                        if(!sp.getString("email_user", "").equals("")) {
                            progressBar.setVisibility(View.INVISIBLE);
                            Intent androidsolved_intent = new Intent(getApplicationContext(), MainActivity.class);
                            //androidsolved_intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                            startActivity(androidsolved_intent);
                            finish();
                        }
                    } catch (JSONException e) {

                    }
                } else {
                    progressBar.setVisibility(View.INVISIBLE);
                    Snackbar.make(constraintLayout, "Email or password wrong", Snackbar.LENGTH_LONG).show();
                }
            }
            @Override
            protected String doInBackground(Void... voids) {
                try {
                    URL url = new URL(urlWebService);
                    HttpURLConnection con = (HttpURLConnection) url.openConnection();
                    StringBuilder sb = new StringBuilder();
                    BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(con.getInputStream()));
                    String json;
                    while ((json = bufferedReader.readLine()) != null) {
                        sb.append(json + "\n");
                    }
                    return sb.toString().trim();

                } catch (Exception e) {
                    progressBar.setVisibility(View.INVISIBLE);
                    Snackbar.make(constraintLayout, "Email or password wrong", Snackbar.LENGTH_LONG);
                    return e.getMessage();
                }
            }
        }
        GetJSON getJSON = new GetJSON();
        getJSON.execute();
    }
    //Method to store the return value from web service into share reference
    private void StoreDataToSP(String json) throws JSONException {
        try{
            JSONArray jsonArray = new JSONArray(json);
            for (int i = 0; i < json.length(); i++) {
                JSONObject obj = jsonArray.getJSONObject(i);
                spe = sp.edit();
                spe.putString("id_user", obj.getString("id_user"));
                spe.putString("username",obj.getString("username"));
                spe.putString("email_user", obj.getString("email_user"));
                spe.putString("password_user", obj.getString("password_user"));
                spe.apply();
            }

        } catch (Exception e){}
    }
}
