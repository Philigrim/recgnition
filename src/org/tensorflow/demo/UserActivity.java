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


import org.json.JSONArray;
import org.json.JSONObject;


import java.io.FileWriter;
import java.sql.*;
import java.sql.DriverManager;
import java.sql.Statement;
import java.util.HashMap;
import java.util.Map;

public class UserActivity extends AppCompatActivity {

    JSONArray myArray = new JSONArray();

    @Override
        protected void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            setContentView(R.layout.activity_user);

//        String restURL = "http://193.219.91.103:9560/atvaizdavimas";
//
//        RequestQueue requestQueue = Volley.newRequestQueue(this);
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

//        String url = "http://193.219.91.103:9560/nx";
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
//                params.put("kat_id", "100");
//                params.put("zen_id", "548");
//                params.put("zen_pav", "Stotele");
//                params.put("st_astext", "POINT(\"54 25\")");
//
//                return params;
//            }
//
//        };
    }
}