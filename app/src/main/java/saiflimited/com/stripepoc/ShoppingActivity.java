package saiflimited.com.stripepoc;

import android.annotation.TargetApi;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;

import com.google.gson.Gson;
import com.stripe.exception.APIConnectionException;
import com.stripe.exception.APIException;
import com.stripe.exception.AuthenticationException;
import com.stripe.exception.CardException;
import com.stripe.exception.InvalidRequestException;
import com.stripe.model.Charge;
import com.stripe.model.Customer;
import com.stripe.net.RequestOptions;

import java.util.HashMap;
import java.util.Map;

import saiflimited.com.stripepoc.saiflimited.com.stripepoc.bean.User;
import saiflimited.com.stripepoc.saiflimited.com.stripepoc.utility.Constants;

public class ShoppingActivity extends AppCompatActivity {
    SharedPreferences prefs;
    SharedPreferences.Editor editor;
    ProgressDialog progressDialog;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_shopping);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);

        toolbar.setTitle("PRODUCTS");
        toolbar.setTitleTextColor(Color.BLACK);
        setSupportActionBar(toolbar);
        progressDialog = new ProgressDialog(this);
        progressDialog.setTitle("Processing Request");
        progressDialog.setMessage("Please wait");
        progressDialog.setCancelable(false);
        progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        prefs = getSharedPreferences(Constants.SHARED_PREFERENCES_NAME,MODE_PRIVATE);
        editor=prefs.edit();

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                new CallStripeService().execute();
            }
        });
    }


    class CallStripeService extends AsyncTask<String, Void, Charge> {


        @TargetApi(Build.VERSION_CODES.GINGERBREAD)
        @Override
        protected Charge doInBackground(String... values) {

            com.stripe.Stripe.apiKey = Constants.PLATFORM_SECRET_KEY;
            {
                String customerId = getIntent().getStringExtra("Customer");
                User user = new Gson().fromJson(prefs.getString(customerId,null),User.class);

                RequestOptions requestOptions = RequestOptions.builder().setApiKey(user.getAccountId()).build();
                try {
                    Customer customer = Customer.retrieve(user.getCustomerId(),requestOptions);
                    if(customer.getDefaultSource() != null)
                    {
                        Map<String, Object> chargeParams = new HashMap<String, Object>();
                        chargeParams.put("amount", 1000); // amount in cents, again
                        chargeParams.put("currency", "usd");
                        chargeParams.put("customer", customer.getId());
                        Charge charge = Charge.create(chargeParams,requestOptions);
                    }
                    else
                    {
                        Intent intent = new Intent(getApplicationContext(),CreditCardActivity.class);
                        intent.putExtra("Customer",customerId);

                        startActivity(intent);
                    }
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


            }

            return null;


        }

        @Override
        protected void onPostExecute(Charge result) {

            if(result!=null)
            {
                System.out.print(result.getId());
            }


        }
    }



}
