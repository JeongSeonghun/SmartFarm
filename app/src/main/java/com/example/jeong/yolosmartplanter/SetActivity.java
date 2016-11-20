package com.example.jeong.yolosmartplanter;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Switch;

import org.json.JSONArray;
import org.json.JSONObject;

public class SetActivity extends AppCompatActivity {

    private Switch set_auto;
    private Switch al_set, al_temp, al_hum, al_soil, al_wat;

    private EditText iptext;
    private EditText al_soilup, al_soildown, al_watdown, al_tempup, al_tempdown, al_humdown, al_humup;
    private EditText set_soilup, set_soildown, set_watdown;

    String tempup, tempdown, humup, humdown, soilup, soildown, watdown;
    boolean seting;
    boolean alram, altemp, alhum, alsoil, alwat;

    String ipAddress="192.168.240.1", setAddress, alAddress, setAdd, alAdd;

    Button savebt, savebt2, ipSaveBt;

    Handler shandler, shandler2;

    URLConnectorSend con;
    URLConnector con1, con2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_set);


        SharedPreferences myPref= getSharedPreferences("IpAdd", MODE_PRIVATE);
        //프리퍼런스에 'ipAdd키가 있으면 이값을 읽어 출력
        if(myPref.contains("ipAdd")){
            ipAddress= myPref.getString("ipAdd", "");
        }
        //System.out.println("seting receive: " +ipAddress);
        setAddress= "http://"+ipAddress+"/insertauto.php";
        alAddress= "http://"+ipAddress+"/insertalram.php";
        setAdd= "http://"+ipAddress+"/smartfarmset.php";
        alAdd= "http://"+ipAddress+"/smartfarmal.php";

        con1 = new URLConnector(setAdd, false, 1000, shandler2);
        con2 = new URLConnector(alAdd, false, 1000, shandler2);
        savebt= (Button)findViewById(R.id.save1);
        savebt2= (Button)findViewById(R.id.save2);

        set_auto= (Switch)findViewById(R.id.setbt);

        al_set= (Switch)findViewById(R.id.albt);
        al_temp= (Switch)findViewById(R.id.altemp);
        al_hum= (Switch)findViewById(R.id.alhum);
        al_soil= (Switch)findViewById(R.id.alsoil);
        al_wat= (Switch)findViewById(R.id.alwat);

        set_soilup= (EditText)findViewById(R.id.stsoil);
        set_soildown= (EditText)findViewById(R.id.spsoil);
        set_watdown= (EditText)findViewById(R.id.spwat);

        al_tempup= (EditText)findViewById(R.id.utemp);
        al_tempdown= (EditText)findViewById(R.id.dtemp);
        al_humup= (EditText)findViewById(R.id.uhum);
        al_humdown= (EditText)findViewById(R.id.dhum);
        al_soilup= (EditText)findViewById(R.id.usoil);
        al_soildown= (EditText)findViewById(R.id.dsoil);
        al_watdown= (EditText)findViewById(R.id.dwat);

        iptext= (EditText)findViewById(R.id.iptext);

        con1.start();    //Thread 시작

        try{
            con1.join(); //서브 쓰레드가 끝날때까지 메인 쓰레드는 대기
            //System.out.println("waiting... for result");
        }
        catch(InterruptedException e){
        }

        String result = con1.getResult();

        //System.out.println(result);
        ParseJSON(result, "auto");

        con2.start();    //Thread 시작

        try{
            con2.join(); //서브 쓰레드가 끝날때까지 메인 쓰레드는 대기
            //System.out.println("waiting... for result");
        }
        catch(InterruptedException e){
        }

        String result2 = con2.getResult();

        //System.out.println(result2);
        ParseJSON(result2, "alram");
    }

    @Override
    protected void onResume() {
        super.onResume();
        SharedPreferences myPref= getSharedPreferences("Seting", MODE_PRIVATE);
        //프리퍼런스에 키가 있으면 이값을 읽어 스위치 상태 표시
        if(myPref.contains("AL_SET")){
            al_set.setChecked(myPref.getBoolean("AL_SET", false));
        }
        if(myPref.contains("AL_TEMP")){
            al_temp.setChecked(myPref.getBoolean("AL_TEMP", false));
        }
        if(myPref.contains("AL_HUM")){
            al_hum.setChecked(myPref.getBoolean("AL_HUM", false));
        }
        if(myPref.contains("AL_SOIL")){
            al_soil.setChecked(myPref.getBoolean("AL_SOIL", false));
        }
        if(myPref.contains("AL_WAT")){
            al_wat.setChecked(myPref.getBoolean("AL_WAT", false));
        }
        SharedPreferences myPref2= getSharedPreferences("IpAdd", MODE_PRIVATE);
        //프리퍼런스에 'ipAdd키가 있으면 이값을 읽어 에디트에 출력
        /*
        System.out.println("resume start");
        System.out.println("recieve ipAddress");
        System.out.println(myPref2.getString("ipAdd",""));
        */
        if(myPref2.contains("ipAdd")){
            iptext.setText(myPref2.getString("ipAdd", ""));
        }
    }

    public void ipSaveBtClicked(View v){
        String ipAddress;
        ipAddress=iptext.getText().toString();
        //System.out.println("save button start");

        //프리퍼런스 private 모드로 오픈(현제 어플리케이션에서만 작동)
        SharedPreferences myPref=getSharedPreferences("IpAdd", MODE_PRIVATE);
        //프리퍼런스에 쓰기를 하기 위해 editor 생성
        SharedPreferences.Editor editor= myPref.edit();
        //프리퍼런스에 'ipAdd'키로 저장
        editor.putString("ipAdd", ipAddress);
        //프리퍼런스 변경 커밋
        editor.commit();
    }


    public void saveclick(View v){
        int watdown_int;
        float watdown_fl;
        seting= set_auto.isChecked();
        //System.out.println(seting);

        soilup= set_soilup.getText().toString();
        soildown= set_soildown.getText().toString();
        watdown= set_watdown.getText().toString();
        /*
        watdown_int=Integer.valueOf(set_watdown.getText().toString());
        //50ml-8s, 1ml-8/50
        if(watdown_int==0)
            watdown="1";
        else
            watdown=String.valueOf(watdown_int*0.16);
        */
        //watdown_int=Integer.valueOf(set_watdown.getText().toString());

        con= new URLConnectorSend(setAddress, seting, soilup, soildown, watdown, shandler);

        con.start();
        try{
            con.join(); //서브 쓰레드가 끝날때까지 메인 쓰레드는 대기
            System.out.println("waiting... for result");
        }
        catch(InterruptedException e){
        }

    }

    public void save2click(View v){
        tempup= al_tempup.getText().toString();
        tempdown= al_tempdown.getText().toString();
        humup= al_humup.getText().toString();
        humdown= al_humdown.getText().toString();
        soilup= al_soilup.getText().toString();
        soildown= al_soildown.getText().toString();
        watdown= al_watdown.getText().toString();

        alram= al_set.isChecked();
        altemp= al_temp.isChecked();
        alhum= al_hum.isChecked();
        alsoil= al_soil.isChecked();
        alwat= al_wat.isChecked();

        con= new URLConnectorSend(alAddress, tempup, tempdown,
                humup, humdown, soilup, soildown, watdown, shandler);

        //프리퍼런스 private 모드로 오픈(현제 어플리케이션에서만 작동)
        SharedPreferences myPref=getSharedPreferences("Seting", MODE_PRIVATE);
        //프리퍼런스에 쓰기를 하기 위해 editor 생성
        SharedPreferences.Editor editor= myPref.edit();
        //프리퍼런스에 저장
        editor.putBoolean("AL_SET", alram);
        editor.putBoolean("AL_TEMP", altemp);
        editor.putBoolean("AL_HUM", alhum);
        editor.putBoolean("AL_SOIL", alsoil);
        editor.putBoolean("AL_WAT", alwat);
        //프리퍼런스 변경 커밋
        editor.commit();

        //서비스 활성화에 사용할 인텐트 생성
        Intent intent= new Intent(getApplicationContext(),MyService.class);

        if(alram) {
            startService(intent);   //서비스 시작
        }else{
            stopService(intent);    //서비스 정지
        }

        con.start();
        try{
            con.join(); //서브 쓰레드가 끝날때까지 메인 쓰레드는 대기
            //System.out.println("waiting... for result");
        }
        catch(InterruptedException e){
        }
    }

    public String switchCheck (Switch sw){
        String set;
        if(sw.isClickable())
            set="on";
        else
            set="off";
        return set;
    }

    public boolean chboolean (String check){
        boolean chbool;
        if(check.equals("on"))
            chbool=true;
        else
            chbool=false;
        return chbool;
    }

    /// json값 메서드
    public String ParseJSON(String target, String mode){
        String tempup, tempdown, humup, humdown, soilup, soildown, watdown;
        boolean set;

        try {
            JSONObject json = new JSONObject(target);

            JSONArray arr = json.getJSONArray("result");
            if(mode.equals("auto")) {
                for (int i = 0; i < arr.length(); i++) {
                    JSONObject json2 = arr.getJSONObject(i);
                    soilup = json2.getString("soilup");
                    soildown = json2.getString("soildown");
                    watdown = json2.getString("watdown");
                    set= chboolean(json2.getString("seting"));
                    /*값확인
                    System.out.println(json2.getString("seting"));
                    System.out.println(json2.getString("setsoil"));
                    System.out.println(json2.getString("setwat"));
                    System.out.println(json2.getString("soilup"));
                    System.out.println(json2.getString("soildown"));
                    System.out.println(json2.getString("watdown"));
                    */
                    set_soilup.setText(soilup);
                    set_soildown.setText(soildown);
                    set_watdown.setText(watdown);

                    set_auto.setChecked(set);
                }

                return "";
            }else if(mode.equals("alram")){
                for (int i = 0; i < arr.length(); i++) {
                    JSONObject json2 = arr.getJSONObject(i);
                    tempup = json2.getString("tempup");
                    tempdown= json2.getString("tempdown");
                    humup = json2.getString("humup");
                    humdown = json2.getString("humdown");
                    soilup = json2.getString("soilup");
                    soildown = json2.getString("soildown");
                    watdown = json2.getString("watdown");
                    /*값 확인
                    System.out.println(json2.getString("tempup"));
                    System.out.println(json2.getString("tempdown"));
                    System.out.println(json2.getString("humup"));
                    System.out.println(json2.getString("humdown"));
                    System.out.println(json2.getString("soilup"));
                    System.out.println(json2.getString("soildown"));
                    System.out.println(json2.getString("watdown"));
                    */
                    al_tempup.setText(tempup);
                    al_tempdown.setText(tempdown);
                    al_humup.setText(humup);
                    al_humdown.setText(humdown);
                    al_soilup.setText(soilup);
                    al_soildown.setText(soildown);
                    al_watdown.setText(watdown);

                }

                return "";
            }
        }

        catch(Exception e){
            e.printStackTrace();
        }

        return null;
    }

}
