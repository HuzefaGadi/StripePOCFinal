package saiflimited.com.stripepoc;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
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

import saiflimited.com.stripepoc.saiflimited.com.stripepoc.bean.User;
import saiflimited.com.stripepoc.saiflimited.com.stripepoc.utility.Constants;

public class MainActivity extends AppCompatActivity {

    Button signIn, createAccount;
    EditText username, password;
    private SharedPreferences prefs;
    private SharedPreferences.Editor editor;
    private ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        prefs = getSharedPreferences(Constants.SHARED_PREFERENCES_NAME, MODE_PRIVATE);
        editor = prefs.edit();
        progressDialog = new ProgressDialog(this);
        progressDialog.setTitle("Signing In");
        progressDialog.setMessage("Please wait");
        progressDialog.setCancelable(false);
        progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);


        toolbar.setTitle("SIGN IN");
        toolbar.setTitleTextColor(Color.BLACK);
        setSupportActionBar(toolbar);
        username = (EditText) findViewById(R.id.username);
        password = (EditText) findViewById(R.id.password);
        if (getIntent().getStringExtra("EmailId") != null) {
            username.setText(getIntent().getStringExtra("EmailId"));
        }

        signIn = (Button) findViewById(R.id.signIn);
        createAccount = (Button) findViewById(R.id.createAccount);
        createAccount.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(getApplicationContext(), CreateAccountActivity.class));
                finish();
            }
        });
        signIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                String usernameText = username.getText().toString();
                if (prefs.getString(usernameText, null) != null) {
                    User user = new Gson().fromJson(prefs.getString(usernameText, null), User.class);
                    progressDialog.show();
                    new CallWebService().execute(user.getCustomerId(),user.getAccountId());
                } else {

                    Toast.makeText(getApplicationContext(), "User Doesn't exists", Toast.LENGTH_LONG).show();
                }
            }
        });

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

    class CallWebService extends AsyncTask<String, Void, Customer> {

        @Override
        protected Customer doInBackground(String... values) {

            Stripe.apiKey = Constants.PLATFORM_SECRET_KEY;
            try {

                RequestOptions requestOptions = RequestOptions.builder().setApiKey(values[1]).build();
                Customer customer = Customer.retrieve(values[0],requestOptions);
                String defaultcard= customer.getDefaultCard();
                return customer;
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
        protected void onPostExecute(final Customer account) {
            if (account != null) {
                Intent intent = new Intent(getApplicationContext(), ShoppingActivity.class);
                intent.putExtra("Customer", account.getDescription());
                startActivity(intent);
                finish();

                try {
                    progressDialog.cancel();
                } catch (Exception e) {

                }

            }
        }
    }
}
