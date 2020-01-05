package com.example.newsapp;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteStatement;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
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
    ArrayList<String> contents = new ArrayList<>();
    ArrayAdapter arrayAdapter;
    SQLiteDatabase articles;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        articles = this.openOrCreateDatabase("Artiles",MODE_PRIVATE,null);
        articles.execSQL("CREATE TABLE IF NOT EXISTS articles(id INTEGER PRIMARY KEY,articleId INETGER,title VARCHAR,content VARCHAR)");

        arrayAdapter = new ArrayAdapter(this, android.R.layout.simple_list_item_1, titles);
        ListView listView = findViewById(R.id.listView);
        listView.setAdapter(arrayAdapter);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent intent = new Intent(getApplicationContext(),ArticleActivity.class);
                intent.putExtra("content",contents.get(position));
                startActivity(intent);
            }
        });

        UpdateListView();
        DownloadTask task = new DownloadTask();
        try {
            task.execute("https://hacker-news.firebaseio.com/v0/topstories.json?print=pretty");
        }catch (Exception e){
            e.printStackTrace();
        }
    }
    public void UpdateListView(){
        Cursor c = articles.rawQuery("SELECT * FROM articles",null);
        int titleIndex = c.getColumnIndex("title");
        int contentIndex = c.getColumnIndex("content");
        if(c.moveToFirst()){
            titles.clear();
            contents.clear();
            do{
                titles.add(c.getString(titleIndex));
                contents.add(c.getString(contentIndex));
            }while(c.moveToNext());
        }

        arrayAdapter.notifyDataSetChanged();
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
                        // add the details to the database
                        String sql = "INSERT INTO articles (articleId,title,content) VALUES(?, ?, ?)";
                        SQLiteStatement statement = articles.compileStatement(sql);
                        statement.bindString(1,articleId); // index of column
                        statement.bindString(2,title);
                        statement.bindString(3,content);
                        statement.execute();

                    }
                }

                Log.i("REsult====",result);
            }catch(Exception e){
                e.printStackTrace();
            }


            return null;
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            UpdateListView();
        }
    }
}
