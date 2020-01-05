package com.example.newsapp;

import androidx.appcompat.app.AppCompatActivity;

import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    ArrayList<String> titles = new ArrayList<>();
    ArrayAdapter arrayAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        arrayAdapter = new ArrayAdapter(this, android.R.layout.simple_list_item_1, titles);
        ListView listView = findViewById(R.id.listView);
        listView.setAdapter(arrayAdapter);

        DownloadTask task = new DownloadTask();
        try {
            task.execute("https://hacker-news.firebaseio.com/v0/topstories.json?print=pretty");
        }catch (Exception e){
            e.printStackTrace();
        }
    }
    public class DownloadTask extends AsyncTask<String,Void,String>{
        @Override
        protected String doInBackground(String... urls) {
            String result = "";
            URL url;
            HttpURLConnection urlConnection = null;
            try{

                url = new URL(urls[0]);
                urlConnection = (HttpURLConnection) url.openConnection();
                InputStream input = urlConnection.getInputStream();
                InputStreamReader reader = new InputStreamReader(input);
                int data = reader.read();
                while(data!=-1){
                    char c =(char)data;
                    result += c;
                    data = reader.read();
                }
                JSONArray idArray = new JSONArray(result);
                int maxArticle = 20;
                if(idArray.length()<maxArticle){
                    maxArticle = idArray.length();
                }
                for(int i=0;i<maxArticle;i++){
                    String articleId = idArray.getString(i);
                    url = new URL("https://hacker-news.firebaseio.com/v0/item/"+articleId+".json?print=pretty");
                    urlConnection = (HttpURLConnection)url.openConnection();
                    input = urlConnection.getInputStream();
                    reader = new InputStreamReader(input);
                    data = reader.read();

                    String articleInfo = "";
                    while(data!=-1){
                        char c = (char)data;
                        articleInfo += c;
                        data = reader.read();
                    }
                    JSONObject object = new JSONObject(articleInfo);

                    if(!object.isNull("title")&& !object.isNull("url")){
                        String title = object.getString("title");
                        String articleUrl = object.getString("url");

                        url = new URL(articleUrl);
                        urlConnection = (HttpURLConnection)url.openConnection();
                        input = urlConnection.getInputStream();
                        reader = new InputStreamReader(input);
                        data = reader.read();

                        String content ="";
                        while(data!=-1){
                            char c = (char)data;
                            content += c;
                            data = reader.read();
                        }
                        Log.i("HTML",content);

                    }
                }

                Log.i("REsult====",result);
            }catch(Exception e){
                e.printStackTrace();
            }


            return null;
        }
    }
}
