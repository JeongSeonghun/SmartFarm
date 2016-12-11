package com.example.jeong.yolosmartplanter;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.ToggleButton;

import org.json.JSONArray;
import org.json.JSONObject;

public class MainActivity extends AppCompatActivity {

    private TextView ttext, htext, wtext, stext;
    private ToggleButton ledtog;

    String sensor;
    String ipAddress="192.168.240.1";
    URLConnector con;
    URLConnectorSend con_led, con_pump;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        SharedPreferences myPref= getSharedPreferences("IpAdd", MODE_PRIVATE);
        //프리퍼런스에 'ipAdd키가 있으면 이값을 읽어 출력
        if(myPref.contains("ipAdd")){
           ipAddress= myPref.getString("ipAdd", "");
        }

        ttext= (TextView)findViewById(R.id.temp);       //온도
        htext= (TextView)findViewById(R.id.hum);        //습도
        stext= (TextView)findViewById(R.id.soil);       //토양 습도
        wtext= (TextView)findViewById(R.id.wat);        //수위값

        ledtog= (ToggleButton)findViewById(R.id.ledtog);

        sensor = "http://"+ipAddress+"/smartfarm.php";

        con = new URLConnector(sensor, true, 1000, handler);
        con.start();    //Thread 시작
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        con.operate(false);
    }

    public void setActivity(View view){
        Intent intent= new Intent(getApplicationContext(),SetActivity.class);
        intent.putExtra("IP_KEY",ipAddress); //키 - 보낼 값
        startActivity(intent);
    }

    // '메인스레드' 에서 Handler 객체를 생성한다.
    // Handler 객체를 생성한 스레드 만이 다른 스레드가 전송하는 Message나 Runnable 객체를 수신
    // 아래 생성된 Handler 객체는 handlerMessage() 를 오버라이딩 하여
    // Message 를 수신.
    Handler handler = new Handler() { // 메인에서 생성한 핸들러

        @Override
        public void handleMessage(Message msg) {
            if(msg.what == 0){
                String result=(String)msg.obj;
                ParseJSON(result);

            }
        } // end handleMessage
    };

    /// json값 메서드
    public String ParseJSON(String target){
        String temp, hum, wat="", shum="", pumpstate, ledstate;

        try {
            JSONObject json = new JSONObject(target);
            JSONArray arr = json.getJSONArray("result");

            for(int i = 0; i < arr.length(); i++){
                JSONObject json2 = arr.getJSONObject(i);
                temp = json2.getString("temp");
                hum = json2.getString("hum");
                shum = json2.getString("soil");
                wat = json2.getString("wat");
                ledstate= json2.getString("ledstate");

                ttext.setText(temp);
                htext.setText(hum);
                stext.setText(shum);
                wtext.setText(wat);

                ledtog.setChecked(chboolean(ledstate));
            }

            /* soil humidity
            530~430 dry soil
            430~350 humid soil
            350~260 in water
            */
            return "";
        }

        catch(Exception e){
            e.printStackTrace();
        }

        return null;
    }

    public void ledToggClick(View view){
        String dataAdd;

        //프리퍼런스 private 모드로 오픈(현제 어플리케이션에서만 작동)
        SharedPreferences myPref=getSharedPreferences("IpAdd", MODE_PRIVATE);
        //프리퍼런스에 'ipAdd키가 있으면 이값을 읽어옴
        if(myPref.contains("ipAdd")){
            ipAddress=myPref.getString("ipAdd","");
        }
        dataAdd= "http://"+ipAddress+"/insertstate.php";

        con_pump = new URLConnectorSend(dataAdd, "led", ledtog.isChecked());
        con_pump.start();    //Thread 시작

        try{
            con_pump.join(); //서브 쓰레드가 끝날때까지 메인 쓰레드는 대기
            System.out.println("waiting... for result");
        }
        catch(InterruptedException e){
        }

    }

    public void pumpClick(View view){
        String dataAdd;
        //System.out.println("Toggel Button state : "+ledtog.isChecked());
        //System.out.println("ledToggle Button Click");
        //프리퍼런스 private 모드로 오픈(현제 어플리케이션에서만 작동)
        SharedPreferences myPref=getSharedPreferences("IpAdd", MODE_PRIVATE);
        //프리퍼런스에 'ipAdd키가 있으면 이값을 읽어옴
        if(myPref.contains("ipAdd")){
            ipAddress=myPref.getString("ipAdd","");
        }
        dataAdd= "http://"+ipAddress+"/insertstate.php";

        con_led = new URLConnectorSend(dataAdd, "pump", true);
        con_led.start();    //Thread 시작

        try{
            con_led.join(); //서브 쓰레드가 끝날때까지 메인 쓰레드는 대기
            //System.out.println("waiting... for result");
        }
        catch(InterruptedException e){
        }

    }

    public boolean chboolean (String check){
        boolean chbool;
        if(check.equals("on"))
            chbool=true;
        else
            chbool=false;
        return chbool;
    }
}
