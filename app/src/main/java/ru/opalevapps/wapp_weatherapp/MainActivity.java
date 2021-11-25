package ru.opalevapps.wapp_weatherapp;

import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;

import javax.net.ssl.HttpsURLConnection;


public class MainActivity extends AppCompatActivity implements View.OnClickListener {
    private Button btnFindCity;
    private EditText etCity;
    private TextView tvResultWeather;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // find views by id
        btnFindCity = findViewById(R.id.btnFindCity);
        tvResultWeather = findViewById(R.id.tvResultWeather);
        etCity = findViewById(R.id.etCity);
        btnFindCity.setOnClickListener(this);

    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btnFindCity:
                if (etCity.getText().toString().equals("")) {
                    Toast.makeText(this, getText(R.string.enter_city_name), Toast.LENGTH_SHORT).show();
                } else {
                    String city = etCity.getText().toString();
                    String key = "b5eef74414264d2494d125546212211";
                    String url = String.format("https://api.weatherapi.com/v1/current.json?key=%s&q=%s&aqi=no&lang=ru", key, city);

                    new getURLData().execute(url);
                }
                break;
            default:
                break;
        }
    }

    // get weather data from JSON by URL
    private class getURLData extends AsyncTask<String, String, String> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            tvResultWeather.setText("Данные о погоде загружаются...");
        }

        // get JSON string from URL
        @Override
        protected String doInBackground(String... strings) {
            HttpsURLConnection connection = null;
            BufferedReader reader = null;
            try {
                URL url = new URL(strings[0]);
                connection = (HttpsURLConnection) url.openConnection();
                connection.connect();

                InputStream stream;
                // check error state
                if (connection.getResponseCode() == 400)  // error code - bad request
                    stream = connection.getErrorStream(); // read error stream
                else
                    stream = connection.getInputStream(); // read normal state stream

                reader = new BufferedReader(new InputStreamReader(stream));
                StringBuffer buffer = new StringBuffer();
                String line;

                while ((line = reader.readLine()) != null) {
                    buffer.append(line).append("\n");
                }

                return buffer.toString();
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                // close all opened connections and streams
                if (connection != null) connection.disconnect();
                try {
                    if (reader != null) reader.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            return null;
        }

        // get weather data from JSON and set it to text view
        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);

            try {
                // for result text
                StringBuilder result = new StringBuilder();

                // get json object from
                JSONObject jsonObject = new JSONObject(s);

                // handle error codes from JSON object
                if (jsonObject.has("error")) {
                    // get error field from json object
                    jsonObject = jsonObject.getJSONObject("error");
                    // process error codes
                    switch (jsonObject.getInt("code")) {
                        case 1006:
                            result.append("Не найдено соответствующее местоположение").append("\n");
                            break;

                        default:
                            result.append("Ошибка загрузки данных: ").append("\n");
                            result.append("Код ошибки: ").append(jsonObject.getInt("code"));
                            result.append("Текст ошибки: ").append(jsonObject.getString("message"));
                    }

                } else { // build result string with weather data
                    // get JSON object with weather data
                    result.append("Город: ").append(jsonObject.getJSONObject("location").getString("name")).append("\n");

                    // for use current JSON Object with weather data
                    jsonObject = jsonObject.getJSONObject("current");

                    // build string with weather data
                    result.append("Температура: ").append(jsonObject.getDouble("temp_c")).append("°C\n");
                    result.append("Ощущается как: ").append(jsonObject.getDouble("feelslike_c")).append("°C\n");
                    result.append("Условие: ").append(jsonObject.getJSONObject("condition").getString("text")).append("\n");
                    result.append("Влажность: ").append(jsonObject.getInt("humidity")).append("%\n");
                }

                // set result text to view
                tvResultWeather.setText(result);
            } catch (JSONException e) {
                e.printStackTrace();
            }

        }
    }
}