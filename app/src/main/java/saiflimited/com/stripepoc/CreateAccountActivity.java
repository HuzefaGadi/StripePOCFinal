package saiflimited.com.stripepoc;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Message;
import android.provider.SyncStateContract;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.gson.Gson;
import com.stripe.Stripe;
import com.stripe.exception.APIConnectionException;
import com.stripe.exception.APIException;
import com.stripe.exception.AuthenticationException;
import com.stripe.exception.CardException;
import com.stripe.exception.InvalidRequestException;
import com.stripe.model.Account;
import com.stripe.model.Customer;
import com.stripe.net.RequestOptions;

import java.util.HashMap;
import java.util.Map;
import java.util.Timer;
import java.util.logging.Handler;

import saiflimited.com.stripepoc.saiflimited.com.stripepoc.bean.User;
import saiflimited.com.stripepoc.saiflimited.com.stripepoc.utility.Constants;

public class CreateAccountActivity extends AppCompatActivity {


    SharedPreferences prefs;
    SharedPreferences.Editor editor;
    EditText username,email,pasword;
    Button next;
    ProgressDialog progressDialog;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_account);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setTitle("CREATE ACCOUNT");

        toolbar.setTitleTextColor(Color.BLACK);
        progressDialog = new ProgressDialog(this);
        progressDialog.setTitle("Creating Account");
        progressDialog.setMessage("Please wait");
        progressDialog.setCancelable(false);
        progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        prefs = getSharedPreferences(Constants.SHARED_PREFERENCES_NAME,MODE_PRIVATE);
        editor=prefs.edit();
        username = (EditText) findViewById(R.id.username);
        email = (EditText) findViewById(R.id.emailAddress);
        pasword = (EditText) findViewById(R.id.password);
        next = (Button) findViewById(R.id.button);
        next.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String usernameText = username.getText().toString();
                String emailText = email.getText().toString();
                String passwordText = pasword.getText().toString();
                if(usernameText.trim().isEmpty() || emailText.trim().isEmpty() || passwordText.trim().isEmpty())
                {
                    Toast.makeText(getApplicationContext(),"All fields are mandatory",Toast.LENGTH_LONG).show();
                }
                else
                {
                    progressDialog.show();
                    new CallWebService().execute(emailText);
                }
            }
        });

    }


    class CallWebService extends AsyncTask<String, Void, Account> {

     User user;
        @Override
        protected Account doInBackground(String... values) {

            Stripe.apiKey = Constants.PLATFORM_SECRET_KEY;
            user = new User();
            Map<String, Object> accountParams = new HashMap<String, Object>();
            accountParams.put("country", "US");
            accountParams.put("managed", true);
            try {
                Account account = Account.create(accountParams);
                RequestOptions requestOptions = RequestOptions.builder().setApiKey(account.getKeys().getSecret()).build();
                Map<String, Object> customerParams = new HashMap<String, Object>();
                customerParams.put("description", values[0]);
                Customer customer = Customer.create(customerParams, requestOptions);
                user.setCustomerId(customer.getId());
                return account;

            } catch (AuthenticationException e) {
                e.printStackTrace();
            } catch (InvalidRequestException e) {
                e.printStackTrace();
            } catch (APIConnectionException e) {
                e.printStackTrace();
            } catch (CardException e) {
                e.printStackTrace();
            } catch (APIException e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(final Account account) {
            if (account != null) {

                user.setAccountId(account.getKeys().getSecret());
                user.setEmailId(email.getText().toString());
                user.setPassword(pasword.getText().toString());
                user.setId(account.getId());

                user.setUsername(username.getText().toString());

                editor.putString(user.getEmailId(),new Gson().toJson(user));
                editor.apply();

                findViewById(R.id.successLayout).setVisibility(View.VISIBLE);
                android.os.Handler handlerTimer = new android.os.Handler();
                handlerTimer.postDelayed(new Runnable(){
                    public void run() {
                        Intent intent = new Intent(getApplicationContext(),MainActivity.class);
                        intent.putExtra("EmailId",user.getEmailId());
                        startActivity(intent);
                    }}, 5000);
            }
            try
            {
                progressDialog.cancel();
            }
            catch (Exception e)
            {

            }

        }
    }
}
