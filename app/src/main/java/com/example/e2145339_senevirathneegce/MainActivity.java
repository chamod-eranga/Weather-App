package com.example.e2145339_senevirathneegce;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import android.location.Address;
import android.location.Geocoder;
import android.os.AsyncTask;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    private TextView textLatitude, textLongitude, textAddress, textTime, textTemperature, textHumidity, textWeatherDescription;
    private Button buttonRefresh;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Set up window insets listener for edge-to-edge display
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Initialize UI elements
        textLatitude = findViewById(R.id.text_latitude);
        textLongitude = findViewById(R.id.text_longitude);
        textAddress = findViewById(R.id.text_address);
        textTime = findViewById(R.id.text_time);
        textTemperature = findViewById(R.id.text_temperature);
        textHumidity = findViewById(R.id.text_humidity);
        textWeatherDescription = findViewById(R.id.text_weather_description);
        buttonRefresh = findViewById(R.id.button_refresh);

        // Set up the refresh button
        buttonRefresh.setOnClickListener(v -> refreshData());

        // Initial data fetch
        refreshData();
    }

    private void refreshData() {
        double lat = 6.877009;
        double lng = 80.013498;
        String openWeatherMapsAPIKeY = "8d295e98f81b4b6742a879b9b9f0447e";
        textLatitude.setText("Latitude: " + lat);
        textLongitude.setText("Longitude: " + lng);

        String urlForWeatherAPI = "https://api.openweathermap.org/data/2.5/weather?lat=" + lat + "&lon=" + lng + "&appid=" + openWeatherMapsAPIKeY;
        new ReadJSONFeedTask().execute(urlForWeatherAPI);

        // Perform reverse geocoding to get address
        getAddress(lat, lng);

        // Update current system time
        Date currentDate = new Date();
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String currentDateTime = dateFormat.format(currentDate);
        textTime.setText(currentDateTime);
    }

    private void getAddress(double latitude, double longitude) {
        Geocoder geocoder = new Geocoder(this, Locale.getDefault());
        try {
            List<Address> addresses = geocoder.getFromLocation(latitude, longitude, 1);
            if (addresses != null && !addresses.isEmpty()) {
                Address address = addresses.get(0);
                StringBuilder addressString = new StringBuilder();
                for (int i = 0; i <= address.getMaxAddressLineIndex(); i++) {
                    addressString.append(address.getAddressLine(i)).append("\n");
                }
                textAddress.setText(addressString.toString());
            } else {
                textAddress.setText("No address found for this location.");
            }
        } catch (IOException e) {
            e.printStackTrace();
            textAddress.setText("Unable to get address. Check your network connection.");
        }
    }


    private class ReadJSONFeedTask extends AsyncTask<String, Void, String> {
        protected String doInBackground(String... urls) {
            return readJSONFeed(urls[0]);
        }

        protected void onPostExecute(String result) {
            try {
                JSONObject jObj = new JSONObject(result);
                JSONObject mainObj = getObject("main", jObj);

                String temperature = "Temperature: " + getFloat("temp", mainObj) + "°C";
                String humidity = "Humidity: " + getInt("humidity", mainObj) + "%";

                textTemperature.setText(temperature);
                textHumidity.setText(humidity);

                // You can extend this to add more weather information
                JSONObject weatherArray = jObj.getJSONArray("weather").getJSONObject(0);
                String weatherDescription = "Weather: " + weatherArray.getString("description");

                textWeatherDescription.setText(weatherDescription);

            } catch (Exception e) {
                e.printStackTrace();
                Toast.makeText(MainActivity.this, "Failed to parse weather data", Toast.LENGTH_SHORT).show();
            }
        }
    }

    public String readJSONFeed(String address) {
        URL url = null;
        try {
            url = new URL(address);
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
        StringBuilder stringBuilder = new StringBuilder();
        HttpURLConnection urlConnection = null;
        try {
            urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setRequestMethod("GET");
            urlConnection.setDoInput(true);
            urlConnection.setDoOutput(true);
            urlConnection.connect();
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            InputStream content = new BufferedInputStream(urlConnection.getInputStream());
            BufferedReader reader = new BufferedReader(new InputStreamReader(content));
            String line;
            while ((line = reader.readLine()) != null) {
                stringBuilder.append(line);
            }

        } catch (IOException e) {
            e.printStackTrace();
            runOnUiThread(() -> Toast.makeText(this, e.toString(), Toast.LENGTH_LONG).show());
        } finally {
            if (urlConnection != null) {
                urlConnection.disconnect();
            }
        }

        return stringBuilder.toString();
    }

    private static JSONObject getObject(String tagName, JSONObject jObj) throws JSONException {
        return jObj.getJSONObject(tagName);
    }

    private static float getFloat(String tagName, JSONObject jObj) throws JSONException {
        return (float) jObj.getDouble(tagName) - 273.15f;  // Convert from Kelvin to Celsius
    }

    private static int getInt(String tagName, JSONObject jObj) throws JSONException {
        return jObj.getInt(tagName);
    }
}