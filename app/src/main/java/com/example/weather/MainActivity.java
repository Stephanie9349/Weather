package com.example.weather;

import androidx.appcompat.app.AppCompatActivity;

import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.concurrent.ExecutionException;

public class MainActivity extends AppCompatActivity {

    private TextView textViewCity;
    private TextView textViewTemp;
    private ImageView weatherImage;
    private EditText editText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        textViewCity = findViewById(R.id.textViewCity);
        textViewTemp=findViewById(R.id.textViewTemp);
        weatherImage=findViewById(R.id.imageView);
        editText=findViewById(R.id.editText);
        String content="https://openweathermap.org/data/2.5/weather?q=CheonAn&appid=439d4b804bc8187953eb36d2a8c26a02";
        callWeatherData(content);

        Button button=findViewById(R.id.button);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String data="https://openweathermap.org/data/2.5/weather?q="+editText.getText().toString()+"&appid=439d4b804bc8187953eb36d2a8c26a02";
                callWeatherData(data);

            }
        });
    }

    static class Weather extends AsyncTask<String,Void,String>{

        @Override
        protected String doInBackground(String... address) {
            try{
                URL url=new URL(address[0]);
                HttpURLConnection connection=(HttpURLConnection) url.openConnection();
                connection.connect();

                InputStream streamIn=connection.getInputStream();
                InputStreamReader streamInReader=new InputStreamReader(streamIn);

                int data=streamInReader.read(); //-1: EOF end of file
                StringBuilder weatherContent=new StringBuilder();

                while(data!=-1){
                    char ch=(char) data;
                    weatherContent.append(ch);
                    data=streamInReader.read();
                }

                return weatherContent.toString();

            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }

            return null;
        }
    }

    private void callWeatherData(String content){
        Weather weather=new Weather();

        try{
            String returnData=weather.execute(content).get();

            JSONObject jsonObject=new JSONObject(returnData);

            String cityInfo=jsonObject.getString("name");
            String weatherInfo=jsonObject.getString("weather");

            JSONArray arrayInfo=new JSONArray(weatherInfo);
            String iconInfo="";

            for(int i=0;i<arrayInfo.length();i++){
                JSONObject dataFromArray=arrayInfo.getJSONObject(i);
                iconInfo=dataFromArray.getString("icon");
            }

            JSONObject mainInfo=jsonObject.getJSONObject("main");
            String tempData=mainInfo.getString("temp");

            setMainInfo(cityInfo,tempData);
            setIconInfo(iconInfo);
            setTrivial(mainInfo);

        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
    private void setMainInfo(String city, String temp){
        textViewCity.setText(city.trim());
        temp+='\u2103'; // 섭씨 기호 나타내는 것
        textViewTemp.setText(temp.trim());
    }
    private void setIconInfo(String iconData){
        String targetIcon="https://openweathermap.org/img/wn/" + iconData + "@2x.png";
        Uri uri= Uri.parse(targetIcon);
        Glide.with(this)
                .load(uri)
                .centerCrop()
                .into(weatherImage);
    }

    private void setTrivial(JSONObject mainObj) throws JSONException{
        String tempMax=mainObj.getString("temp_max");
        String humidity=mainObj.getString("humidity");
        String trivial= tempMax + "/" + humidity + "%";
    }

}