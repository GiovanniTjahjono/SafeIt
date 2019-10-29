package com.tugasakhir.giovannitjahjono.safeitv42;

import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.support.constraint.ConstraintLayout;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class CreateAccountActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_account);
        //Set the app fullscrean whitout status bar
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);

        Button btnCreateAccount = (Button) findViewById(R.id.btnCreateAccountR);
        final TextView fullname = (TextView) findViewById(R.id.txtFullname);
        final TextView email = (TextView) findViewById(R.id.txtEmailR);
        final TextView password = (TextView) findViewById(R.id.txtPasswordR);
        final TextView retypePassword = (TextView) findViewById(R.id.txtRetypePassword);
        final ConstraintLayout constraintLayout = (ConstraintLayout) findViewById(R.id.constraint_create_account);
        fullname.requestFocus();
        final Context context;
        //prepare the email pattern for checking email input
        final String emailPattern = "[a-zA-Z0-9._-]+@[a-z]+\\.+[a-z]+";

        btnCreateAccount.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //Hide the keyboard
                InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(view.getWindowToken(), 0);

                String fullnameUser = String.valueOf(fullname.getText());
                String emailUser = String.valueOf(email.getText());
                String passwordUser = String.valueOf(password.getText());
                String retypePasswordUser = String.valueOf(retypePassword.getText());
                //Check user has fill the form or not yet
                if (!fullnameUser.equals("") && !emailUser.equals("") && !passwordUser.equals("") && !retypePasswordUser.equals("") && emailUser.matches(emailPattern)) {
                    //Check is the user retype the same password
                    if (passwordUser.equals(retypePasswordUser)) {

                        String urlPost = "https://safeitdatabase.000webhostapp.com/signUp.php?username=" + fullnameUser + "&email_user=" + emailUser + "&password_user=" + passwordUser;
                        //Calling PostJSON Method
                        postJSON(urlPost, emailUser);
                    //If user doesn't retype password correctly, it will show snackbar
                    } else {
                        Snackbar snackbar = Snackbar
                                .make(constraintLayout, "Please retype password correctly", Snackbar.LENGTH_LONG);
                        snackbar.show();
                        retypePassword.requestFocus();
                    }
                //If user hasn't fill all form
                } else {
                    //It will show snackbar based on the form that doesn't filled
                    if (retypePasswordUser.equals("") && passwordUser.equals("") && emailUser.equals("") && fullnameUser.equals("")) {
                        Snackbar.make(constraintLayout, "Please fill your the form", Snackbar.LENGTH_LONG).show();
                    } else if (fullnameUser.equals("")) {
                        fullname.requestFocus();
                        Snackbar.make(constraintLayout, "Please fill your fullname", Snackbar.LENGTH_LONG).show();
                    } else if (emailUser.equals("")) {
                        email.requestFocus();
                        Snackbar.make(constraintLayout, "Please fill your email", Snackbar.LENGTH_LONG).show();
                    } else if (!emailUser.matches(emailPattern)) {
                        email.requestFocus();
                        Snackbar.make(constraintLayout, "Please fill your email correctly", Snackbar.LENGTH_LONG).show();
                    } else if (passwordUser.equals("")) {
                        password.requestFocus();
                        Snackbar.make(constraintLayout, "Please fill your password", Snackbar.LENGTH_LONG).show();
                    } else if (retypePasswordUser.equals("")) {
                        retypePassword.requestFocus();
                        Snackbar.make(constraintLayout, "Please retype your password", Snackbar.LENGTH_LONG).show();
                    }

                }
            }

        });
    }
    //Method postJSON to communicate with the web service
    public void postJSON(final String urlWebService, final String email) {
        class PostJSON extends AsyncTask<Void, Void, String> {
            @Override
            protected void onPreExecute() {
                super.onPreExecute();
            }
            @Override
            protected void onPostExecute(String s) {
                super.onPostExecute(s);
                try {
                    ConstraintLayout constraintLayout = (ConstraintLayout) findViewById(R.id.constraint_create_account);
                    //If the parameter filled true, it's mean create account successfully
                    if (s.equals("true")) {
                        Toast.makeText(getApplicationContext(), "Create account successfully", Toast.LENGTH_LONG).show();
                        Intent androidsolved_intent = new Intent(getApplicationContext(), LoginActivity.class);
                        startActivity(androidsolved_intent);
                        finish();
                    //Else snackbar will appear inform that failed to create account
                    } else {
                        TextView email = (TextView) findViewById(R.id.txtEmailR);
                        email.requestFocus();
                        Snackbar.make(constraintLayout, "Registered failed, please please check the email or your connection", Snackbar.LENGTH_LONG).show();
                    }
                } catch (Exception e) {
                    System.out.println("FAILED");
                }
            }
            //Communicate process with web service
            @Override
            protected String doInBackground(Void... voids) {
                try {
                    URL url = new URL(urlWebService);
                    HttpURLConnection con = (HttpURLConnection) url.openConnection();
                    StringBuilder sb = new StringBuilder();
                    BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(con.getInputStream()));
                    String result = bufferedReader.readLine();
                    return result;
                } catch (Exception e) {
                    return null;
                }
            }
        }
        PostJSON post = new PostJSON();
        post.execute();
    }
}
