package org.tensorflow.demo;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Log;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.gson.JsonArray;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;


import java.io.BufferedReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.sql.*;
import java.sql.DriverManager;
import java.sql.Statement;
import java.util.HashMap;
import java.util.Map;

public class UserActivity extends AppCompatActivity {

    JSONArray myArray = new JSONArray();

    RequestQueue requestQueue;
    @Override
        protected void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            setContentView(R.layout.activity_user);

//        String restURL = "http://193.219.91.103:9560/atvaizdavimas";
//
          //RequestQueue requestQueue = Volley.newRequestQueue(this);
//
//        JsonArrayRequest arrayRequest = new JsonArrayRequest(
//                Request.Method.GET,
//                restURL,
//                null,
//                new Response.Listener<JSONArray>(){
//                    @Override
//                    public void onResponse(JSONArray response){
//                        Log.e("Rest response", response.toString());
//                        myArray = response;
//                        }
//
//                        },
//
//                new Response.ErrorListener() {
//                    @Override
//                    public void onErrorResponse(VolleyError error) {
//                        Log.e("Rest Response", error.toString());
//                    }
//
//
//                });
//        requestQueue.add(arrayRequest);
//        System.out.println(myArray.toString());

        //String url = "http://193.219.91.103:9560/kuku";

//        StringRequest putRequest = new StringRequest(Request.Method.PUT, url,
//                new Response.Listener<String>()
//                {
//                    @Override
//                    public void onResponse(String response) {
//                        // response
//                        Log.d("Response", response);
//                    }
//                },
//                new Response.ErrorListener()
//                {
//                    @Override
//                    public void onErrorResponse(VolleyError error) {
//                        // error
//                        Log.d("Error.Response", error.toString());
//                    }
//                }
//        ) {
//
//            @Override
//            protected Map<String, String> getParams()
//            {
//                Map<String, String>  params = new HashMap<String, String>();
//                params.put("pute", "100");
//
//                return params;
//            }
//
//        };
//
//        requestQueue.add(putRequest);


        //URL of the request we are sending
        String url = "http://193.219.91.103:9560/aslohas";
/*
JsonObjectRequest takes in five paramaters
Request Type - This specifies the type of the request eg: GET,POST
URL - This String param specifies the Request URL
JSONObject - This parameter takes in the POST parameters.Sending this parameters
makes this a POST request
Listener -This parameter takes in a implementation of Response.Listener()
interface which is invoked if the request is successful
Listener -This parameter takes in a implementation of Error.Listener()
interface which is invoked if any error is encountered while processing
the request
*/
        JSONObject postparams = new JSONObject();
        try{
            postparams.put("kat_id", "5");
            postparams.put("zen_id", "203");
        }catch(JSONException j){
            Log.e("blabla", j.toString());
        }

        JsonObjectRequest jsonObjReq = new JsonObjectRequest(Request.Method.POST,
                url, postparams, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {

            }
        },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {

                    }
                });
// Adding the request to the queue along with a unique string tag
        addToRequestQueue(jsonObjReq, "postRequest");
    }

    public RequestQueue getRequestQueue() {
        if (requestQueue == null)
            requestQueue = Volley.newRequestQueue(getApplicationContext());
        return requestQueue;
    }

    public void addToRequestQueue(Request request, String tag) {
        request.setTag(tag);
        getRequestQueue().add(request);
    }

    public void cancelAllRequests(String tag) {
        getRequestQueue().cancelAll(tag);
    }
}