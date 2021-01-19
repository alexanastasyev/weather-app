package com.example.weather;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;

import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.concurrent.ExecutionException;

public class MainActivity extends AppCompatActivity {

    private EditText editTextLocation;
    private TextView textViewCity;
    private TextView textViewTemp;
    private TextView textViewWeather;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        ActionBar actionBar = getSupportActionBar();
        actionBar.hide();

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        textViewCity = findViewById(R.id.textViewCity);
        textViewTemp = findViewById(R.id.textViewTemperature);
        textViewWeather = findViewById(R.id.textViewWeather);

    }

    public void onClickGetWeather(View view) {

        final double ABSOLUTE_ZERO = 273.15;
        final String CITY_NOT_FOUND = getString(R.string.city_not_found);

        final int EXECUTION_CODE_SUCCESS = 200;
        final int EXECUTION_CODE_FAILURE = 404;

        editTextLocation = findViewById(R.id.editTextLocation);
        String location = editTextLocation.getText().toString().trim();


        if (!location.isEmpty()) {

            GetWeatherTask getWeatherTask = new GetWeatherTask();
            try {
                String jsonString = getWeatherTask.execute(location).get();
                if (jsonString != null) {
                    JSONObject json = new JSONObject(jsonString);

                    int executionCode = json.getInt("cod");

                    if (executionCode == EXECUTION_CODE_SUCCESS) {

                        clearTexViews();

                        String city = json.getString("name");
                        textViewCity.setText(getString(R.string.city) + " " + city);

                        JSONObject jsonMain = json.getJSONObject("main");
                        String temp = jsonMain.getString("temp");
                        textViewTemp.setText(getString(R.string.temp) + " " + String.valueOf((int) (Double.parseDouble(temp) - ABSOLUTE_ZERO)));

                        JSONArray jsonArray = json.getJSONArray("weather");
                        JSONObject jsonWeather = jsonArray.getJSONObject(0);
                        String weather = jsonWeather.getString("description");
                        textViewWeather.setText(getString(R.string.outwhere) + " " + weather);

                    } else if (executionCode == EXECUTION_CODE_FAILURE) {
                        clearTexViews();
                        textViewCity.setText(CITY_NOT_FOUND);
                    } else {
                        clearTexViews();
                        textViewCity.setText(R.string.unhaldled_error);
                    }
                } else {
                    clearTexViews();
                    textViewCity.setText(CITY_NOT_FOUND);
                }

            } catch (ExecutionException e) {
                e.printStackTrace();
            } catch (InterruptedException e) {
                e.printStackTrace();
            } catch (JSONException e) {
                e.printStackTrace();
            }

        } else {
            Toast.makeText(getApplicationContext(), "Введите название города или индекс", Toast.LENGTH_SHORT).show();
        }
    }

    private static class GetWeatherTask extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... strings) {
            URL url = null;
            HttpURLConnection urlConnection = null;

            try {
                url = new URL("https://api.openweathermap.org/data/2.5/weather?q=" + strings[0] + "&lang=ru&appid=8be4772a1a8c776e0191beb09b502d14");
                urlConnection = (HttpURLConnection) url.openConnection();
                InputStream inputStream = urlConnection.getInputStream();
                InputStreamReader reader = new InputStreamReader(inputStream);
                BufferedReader bufferedReader = new BufferedReader(reader);
                StringBuilder stringBuilder = new StringBuilder();
                String line = bufferedReader.readLine();
                while (line != null) {
                    stringBuilder.append(line);
                    line = bufferedReader.readLine();
                }
                String json = stringBuilder.toString();
                return json;
            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                if (urlConnection != null) {
                    urlConnection.disconnect();
                }
            }

            return null;
        }

    }
    void clearTexViews() {
        textViewCity.setText("");
        textViewTemp.setText("");
        textViewWeather.setText("");
    }
}