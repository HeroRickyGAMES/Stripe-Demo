package com.herorickystudios.sripeintegrationandroidstudio;

import android.os.Bundle;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.stripe.android.PaymentConfiguration;
import com.stripe.android.paymentsheet.PaymentSheet;
import com.stripe.android.paymentsheet.PaymentSheetResult;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class paymentActivityInteiro extends AppCompatActivity {

    String PUBLIC_KEY = "pk_test_51M8uinEu3aGVFmBvJ9n5qZjwwyiK1cnbgvLnFUqC7xDJjxt3CpVCTIksE89QGfo3aPZOWlnYqeEElwgC7ahLgs8u00CGuvGXRe";
    String SECRET_KEY = "sk_test_51M8uinEu3aGVFmBvklARKwzQ0zW1SQKQPilassvznj6eDfrpsMd8BFuc7y9kSl45YFaVxst4Zg4zfnqXIIeOetjC005DQGvAtk";
    PaymentSheet paymentSheet;
    String customerID;
    String EphericalKey;
    String ClientSecret;
    String valor;
    EditText editPaymentValue;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_payment_inteiro);

        PaymentConfiguration.init(this, PUBLIC_KEY);

        valor = getIntent().getExtras().getString("ValorInteiro");

        System.out.println(valor);

        paymentSheet = new PaymentSheet(this, paymentSheetResult -> {
            onPaymentResult(paymentSheetResult);
        });

        pagarMetodo();

    }
    private void onPaymentResult(PaymentSheetResult paymentSheetResult) {

        if(paymentSheetResult instanceof PaymentSheetResult.Completed){

            Toast.makeText(this, "Pagamento feito com sucesso!", Toast.LENGTH_SHORT).show();


        }else{
            Toast.makeText(this, "Pagamento cancelado ...", Toast.LENGTH_SHORT).show();
        }
    }
    private void getEphericalKey(String customerID) {

        StringRequest stringRequest = new StringRequest(Request.Method.POST, "https://api.stripe.com/v1/ephemeral_keys", new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {

                try {
                    JSONObject object = new JSONObject(response);
                    EphericalKey = object.getString("id");

                    Toast.makeText(paymentActivityInteiro.this, EphericalKey, Toast.LENGTH_SHORT).show();

                    System.out.println( "EphericalKey " + EphericalKey);
                    getClientSecret(customerID, EphericalKey);

                } catch (JSONException e) {
                    e.printStackTrace();
                }

            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {

            }
        }){
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> header = new HashMap<>();
                header.put("Authorization", "Bearer " + SECRET_KEY);
                header.put("Stripe-Version", "2022-11-15");
                return header;
            }


            @Override
            protected Map<String, String> getParams() throws AuthFailureError {

                Map<String, String> params = new HashMap<>();
                params.put("customer", customerID);

                return params;
            }

        };

        RequestQueue requestQueue = Volley.newRequestQueue(this);
        requestQueue.add(stringRequest);


    }
    private void getClientSecret(String customerID, String ephericalKey) {

        StringRequest stringRequest = new StringRequest(Request.Method.POST, "https://api.stripe.com/v1/payment_intents", new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {

                try {
                    JSONObject object = new JSONObject(response);
                    ClientSecret = object.getString("client_secret");

                    Toast.makeText(paymentActivityInteiro.this, ClientSecret, Toast.LENGTH_SHORT).show();

                    System.out.println("Client secret " + ClientSecret);

                    PaymentFlow();


                } catch (JSONException e) {
                    e.printStackTrace();
                }

            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {

            }
        }){
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> header = new HashMap<>();
                header.put("Authorization", "Bearer " + SECRET_KEY);
                header.put("Stripe-Version", "2022-11-15");
                return header;
            }
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {

                Map<String, String> params = new HashMap<>();
                params.put("amount", valor + "0");
                params.put("currency", "brl");
                params.put("automatic_payment_methods[enabled]", "true");
                return params;
            }

        };

        RequestQueue requestQueue = Volley.newRequestQueue(this);
        requestQueue.add(stringRequest);

    }
    private void PaymentFlow() {

        paymentSheet.presentWithPaymentIntent(
                ClientSecret,
                new PaymentSheet.Configuration("Taxa completa", new PaymentSheet.CustomerConfiguration(
                        customerID,
                        EphericalKey
                ))
        );
    }

    public void pagarMetodo(){

        StringRequest stringRequest = new StringRequest(Request.Method.POST, "https://api.stripe.com/v1/customers", new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {



                try {
                    JSONObject object = new JSONObject(response);
                    customerID = object.getString("id");

                    Toast.makeText(paymentActivityInteiro.this, customerID, Toast.LENGTH_SHORT).show();

                    System.out.println("CustomerID " + customerID);

                    getEphericalKey(customerID);

                } catch (JSONException e) {
                    e.printStackTrace();
                }

            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                System.out.println(error.networkResponse.toString());

                Toast.makeText(paymentActivityInteiro.this, error.networkResponse.toString(), Toast.LENGTH_SHORT).show();

            }
        }){
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> header = new HashMap<>();
                header.put("Authorization", "Bearer " + SECRET_KEY);
                header.put("Stripe-Version", "2022-11-15");
                return header;
            }

            @Nullable
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {

                Map<String, String> params = new HashMap<>();
                params.put("customer", customerID);

                return super.getParams();
            }
        };

        RequestQueue requestQueue = Volley.newRequestQueue(this);
        requestQueue.add(stringRequest);
    }
}