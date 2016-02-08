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
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.gson.Gson;
import com.stripe.android.Stripe;
import com.stripe.android.TokenCallback;
import com.stripe.android.model.Card;
import com.stripe.android.model.Token;
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

import saiflimited.com.stripepoc.R;
import saiflimited.com.stripepoc.saiflimited.com.stripepoc.bean.CardForm;
import saiflimited.com.stripepoc.saiflimited.com.stripepoc.bean.User;
import saiflimited.com.stripepoc.saiflimited.com.stripepoc.utility.Constants;

public class CreditCardActivity extends AppCompatActivity {

    EditText cardNumber,cvc,month,year;
     Button next;
    String customerEmail;
    SharedPreferences prefs;
    SharedPreferences.Editor editor;
    private ProgressDialog progressDialog;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_credit_card);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setTitle("PAYMENT");
        toolbar.setTitleTextColor(Color.BLACK);
        prefs = getSharedPreferences(Constants.SHARED_PREFERENCES_NAME, MODE_PRIVATE);
        editor = prefs.edit();
        progressDialog = new ProgressDialog(this);
        progressDialog.setTitle("Processing Payment");
        progressDialog.setMessage("Please wait");
        progressDialog.setCancelable(false);
        progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        customerEmail = getIntent().getStringExtra("Customer");
        cardNumber = (EditText) findViewById(R.id.cardNumber);
        cvc = (EditText) findViewById(R.id.cvc);
        month = (EditText) findViewById(R.id.month);
        year = (EditText) findViewById(R.id.year);
        next = (Button) findViewById(R.id.next);
        next.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try
                {
                    String cvcText = cvc.getText().toString();
                    String cardNumberText = cardNumber.getText().toString();
                    Integer monthInt = Integer.valueOf(month.getText().toString());
                    Integer yearInt = Integer.valueOf(year.getText().toString());
                    CardForm card = new CardForm();
                    card.setCardNumber(cardNumberText);
                    card.setCvc(cvcText);
                    card.setCurrency("usd");
                    card.setExpiryMonth(monthInt);
                    card.setExpiryYear(yearInt);
                    saveCreditCard(card);
                } catch(Exception e)
                {
                    Toast.makeText(getApplicationContext(),"Error in Fields",Toast.LENGTH_LONG).show();
                }

            }
        });
    }


    public void saveCreditCard(CardForm form) {

        Card card = new Card(
                form.getCardNumber(),
                form.getExpiryMonth(),
                form.getExpiryYear(),
                form.getCvc());
        card.setCurrency(form.getCurrency());

        boolean validation = card.validateCard();
        if (validation) {

            if(card!=null)
            {
                new Stripe().createToken(
                        card,
                        Constants.PUBLISHABLE_KEY,
                        new TokenCallback() {
                            public void onSuccess(Token token) {

                                new CallStripeService().execute(token.getId());
                            }
                            public void onError(Exception error) {

                            }
                        });



            }


        } else if (!card.validateNumber()) {
            handleError("The card number that you entered is invalid");
        } else if (!card.validateExpiryDate()) {
            handleError("The expiration date that you entered is invalid");
        } else if (!card.validateCVC()) {
            handleError("The CVC code that you entered is invalid");
        } else {
            handleError("The card details that you entered are invalid");
        }
    }

    private void handleError(String s) {

        Toast.makeText(getApplicationContext(),s,Toast.LENGTH_LONG).show();
    }

    class CallStripeService extends AsyncTask<String, Void, Charge> {


        @TargetApi(Build.VERSION_CODES.GINGERBREAD)
        @Override
        protected Charge doInBackground(String... values) {

            com.stripe.Stripe.apiKey = Constants.PLATFORM_SECRET_KEY;

// Get the credit card details submitted by the form
            String token = values[0];

// Create the charge on Stripe's servers - this will charge the user's card
            try {
                User user = new Gson().fromJson(prefs.getString(customerEmail,null),User.class);
                RequestOptions requestOptions = RequestOptions.builder().setApiKey(user.getAccountId()).build();
                Customer customer = Customer.retrieve(user.getCustomerId(),requestOptions);
                /*Map<String, Object> updateparams = new HashMap<String, Object>();
                updateparams.put("source", token);
                customer.update(updateparams,requestOptions);*/

                Map<String, Object> params = new HashMap<String, Object>();
                params.put("source", token);
                customer.createCard(params,requestOptions);

                Map<String, Object> chargeParams = new HashMap<String, Object>();
                chargeParams.put("amount", 1000); // amount in cents, again
                chargeParams.put("currency", "usd");
                chargeParams.put("customer", customer.getId());

                Charge charge = Charge.create(chargeParams,requestOptions);

                return charge;
            } catch (CardException e) {
                e.printStackTrace();
            } catch (APIException e) {
                e.printStackTrace();
            } catch (InvalidRequestException e) {
                e.printStackTrace();
            } catch (APIConnectionException e) {
                e.printStackTrace();
            } catch (AuthenticationException e) {
                e.printStackTrace();
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
