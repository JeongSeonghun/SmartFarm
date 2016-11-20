package com.example.jeong.yolosmartplanter;
// url 내용 읽기 class
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Created by jeong on 2016-04-18.
 */
public class URLConnector extends Thread{
    private String result;
    private String URL;
    private int delay;
    private boolean operateval;
    Handler handler;
    boolean remode;

    public URLConnector(String url, boolean repeatseting, int delaymilisec, Handler handler){
        URL = url;// url주소 URL에 저장
        delay=delaymilisec;
        this.handler = handler;
        this.remode=repeatseting;
    }

    public void operate(boolean val){
        operateval= val;
    }

    @Override
    public void run() {
        operateval=true;
        if(remode) {
            try {
                while (true) {
                    if(operateval) {
                        String output = request(URL);
                        result = output;
                        Message msg = new Message();
                        msg.what = 0;
                        msg.obj = result;
                        handler.sendMessage(msg);
                        Thread.sleep(delay);
                    }else
                        break;
                }

            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }//if end
        else{
            String output=request(URL);
            result=output;
        }

    }

    public String getResult(){
        return result;
    }


    private String request(String urlStr) {
        StringBuilder output = new StringBuilder();
        try {
            URL url = new URL(urlStr);  //url형으로
            HttpURLConnection conn = (HttpURLConnection)url.openConnection(); //httpUrl연결
            if (conn != null) {
                conn.setConnectTimeout(15000);
                conn.setRequestMethod("GET");
                conn.setDoInput(true);
                conn.setDoOutput(true);

                int resCode = conn.getResponseCode();
                if (resCode == HttpURLConnection.HTTP_OK) {
                    BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream())) ;
                    String line = null;
                    while(true) {
                        line = reader.readLine();   //한줄씩 읽음
                        if (line == null) {
                            break;
                        }
                        output.append(line + "\n");
                    }

                    reader.close();
                    conn.disconnect();
                }
            }
        } catch(Exception ex) {
            Log.e("SampleHTTP", "Exception in processing response.", ex);
            ex.printStackTrace();
        }


        return output.toString();
    }


}
