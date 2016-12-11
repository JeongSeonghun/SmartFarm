package com.example.jeong.yolosmartplanter;

import android.os.Handler;
import android.util.Log;

import java.net.URL;
import java.net.URLEncoder;

/**
 * Created by jeong on 2016-04-27.
 */
public class URLConnectorSend extends Thread{
    String address, mode;
    boolean seting;
    boolean state;
    String soilup, soildown, watdown, tempup, tempdown, humup, humdown;

    Handler handler;

    public URLConnectorSend(String url, boolean seting, String soilup, String soildown, String watdown,
                            Handler handler){
        this.address=url;
        this.seting=seting;
        this.soilup= soilup;
        this.soildown= soildown;
        this.watdown= watdown;
        this.mode="auto";
        this.handler = handler;
    }

    public URLConnectorSend(String url, String tempup, String tempdown, String humup, String humdown,
                            String soilup, String soildown, String watdown, Handler handler){
        this.address=url;
        this.tempup=tempup;
        this.tempdown=tempdown;
        this.humup=humup;
        this.humdown=humdown;
        this.soilup= soilup;
        this.soildown= soildown;
        this.watdown= watdown;
        this.mode="alram";
        this.handler = handler;
    }

    public URLConnectorSend(String url, String mode, boolean state){
        this.address=url;
        this.state=state;
        this.mode=mode;
    }

    @Override
    public void run() {
        if(mode.equals("auto"))
            send();
        else if(mode.equals("alram"))
            send2();
        else if(mode.equals("led")||mode.equals("pump"))
            send3();
    }
    public void send(){
        try {

            URL url = new URL(address +
                    "?seting=" + URLEncoder.encode(checkstate(seting), "UTF-8")
                    + "&soilup=" + URLEncoder.encode(soilup, "UTF-8")
                    + "&soildown=" + URLEncoder.encode(soildown, "UTF-8")
                    + "&watdown=" + URLEncoder.encode(watdown, "UTF-8")
            ); //변수값을 UTF-8로 인코딩하기 위해 URLEncoder를 이용하여 인코딩함

            url.openStream(); //서버의 DB에 입력하기 위해 웹서버의 insert.php파일에 입력된 이름과 가격을 넘김


        } catch(Exception ex) {
            Log.e("SampleHTTP", "Exception in processing response.", ex);
            ex.printStackTrace();
        }

    }

    public void send2(){
        try {
            URL url = new URL(address +
                    "?tempup=" + URLEncoder.encode(tempup, "UTF-8")
                    + "&tempdown=" + URLEncoder.encode(tempdown, "UTF-8")
                    + "&humup=" + URLEncoder.encode(humup, "UTF-8")
                    + "&humdown=" + URLEncoder.encode(humdown, "UTF-8")
                    + "&soilup=" + URLEncoder.encode(soilup, "UTF-8")
                    + "&soildown=" + URLEncoder.encode(soildown, "UTF-8")
                    + "&watdown=" + URLEncoder.encode(watdown, "UTF-8")
            ); //변수값을 UTF-8로 인코딩하기 위해 URLEncoder를 이용하여 인코딩함
            url.openStream(); //서버의 DB에 입력하기 위해 웹서버의 insert.php파일에 입력된 이름과 가격을 넘김


        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public void send3(){
        try {
            URL url = new URL(address +
                    "?mode=" + URLEncoder.encode(mode, "UTF-8")
                    + "&state=" + URLEncoder.encode(checkstate(state), "UTF-8")
            ); //변수값을 UTF-8로 인코딩하기 위해 URLEncoder를 이용하여 인코딩함
            url.openStream(); //서버의 DB에 입력하기 위해 웹서버의 php파일에 입력된 값을 넘김

        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public String checkstate(boolean val){
        String check;
        if(val)
            check="on";
        else
            check="off";
        return check;
    }
}
