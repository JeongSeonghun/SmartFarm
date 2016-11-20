package com.example.jeong.yolosmartplanter;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Vibrator;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONObject;

public class MyService extends Service {
    private int num=0;
    private int number=0;

    String ipAddress="192.168.240.1", alStateAdd;
    URLConnector con;
    private NotificationManager mNotificationManager;
    private Notification.Builder mBuilder;

    //진동객체
    Vibrator vibrator;


    public MyService() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public void onCreate() {
        super.onCreate();
        //Toast.makeText(getApplicationContext(), "onCreate()", Toast.LENGTH_SHORT).show();
        //진동 객체 생성
        vibrator = (Vibrator)getSystemService(Context.VIBRATOR_SERVICE);

    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        super.onStartCommand(intent, flags, startId);

        //프리퍼런스 private 모드로 오픈(현제 어플리케이션에서만 작동)
        SharedPreferences myPref=getSharedPreferences("IpAdd", MODE_PRIVATE);
        //프리퍼런스에 'ipAdd키가 있으면 이값을 읽어옴
        if(myPref.contains("ipAdd")){
            //System.out.println("Sevice preference test value: "+myPref.getString("ipAdd", ""));
            ipAddress=myPref.getString("ipAdd","");
        }

        alStateAdd= "http://"+ipAddress+"/alramstate.php";
        //System.out.println("service receive : "+alStateAdd);

        //Toast.makeText(getApplicationContext(), "onStartCommand()"+num,Toast.LENGTH_SHORT).show();

            con = new URLConnector(alStateAdd, true, 5000, handler);
            con.start();    //Thread 시작


        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        //Toast.makeText(getApplicationContext(), "onDestroy()",Toast.LENGTH_SHORT).show();
        con.operate(false);
    }

    Handler handler = new Handler() { // 메인에서 생성한 핸들러

        @Override
        public void handleMessage(Message msg) {

            if(msg.what == 0){

                String result=(String)msg.obj;
                //System.out.println(result);

                ParseJSON(result);

            }
        } // end handleMessage
    };

    /// json값 메서드
    public String ParseJSON(String target){
        String temp="", hum="", wat="", soil="";
        boolean[] alcheck=new boolean[4];

        SharedPreferences myPref= getSharedPreferences("Seting", MODE_PRIVATE);
        if(myPref.contains("AL_TEMP")){
            alcheck[0]=myPref.getBoolean("AL_TEMP", false);
        }
        if(myPref.contains("AL_HUM")){
            alcheck[1]=myPref.getBoolean("AL_HUM", false);
        }
        if(myPref.contains("AL_SOIL")){
            alcheck[2]=myPref.getBoolean("AL_SOIL", false);
        }
        if(myPref.contains("AL_WAT")){
            alcheck[3]=myPref.getBoolean("AL_WAT", false);
        }

        try {
            JSONObject json = new JSONObject(target);

            JSONArray arr = json.getJSONArray("result");

            for(int i = 0; i < arr.length(); i++){
                JSONObject json2 = arr.getJSONObject(i);
                temp = json2.getString("altemp");
                hum = json2.getString("alhum");
                soil = json2.getString("alsoil");
                wat = json2.getString("alwat");

                /*
                System.out.println("Service json value ");
                System.out.println("altemp: "+ temp);
                System.out.println("alhum: "+ hum);
                System.out.println("alsoil: "+ soil);
                System.out.println("alwat: "+ wat);
                */

            }
            System.out.println("notificationString : \n"+notificationString(temp, hum, soil, wat));
            if((temp.equals("on")&&alcheck[0])
                    ||(hum.equals("on")&&alcheck[1])
                    ||(soil.equals("on")&&alcheck[2])
                    ||(wat.equals("on")&&alcheck[3]))
            notificationShow(notificationString(temp, hum, soil, wat));

            return "";
        }

        catch(Exception e){
            e.printStackTrace();
        }

        return null;
    }

    public String notificationString(String temp, String hum, String soil, String wat){
        String[] str=new String[]{temp, hum, soil, wat};
        String[] strlist=new String[]{"temp", "hum", "soil", "wat"};
        boolean[] alcheck= new boolean[4];
        String result="";
        int i, num;

        SharedPreferences myPref= getSharedPreferences("Seting", MODE_PRIVATE);
        if(myPref.contains("AL_TEMP")){
            alcheck[0]=myPref.getBoolean("AL_TEMP", false);
        }
        if(myPref.contains("AL_HUM")){
            alcheck[1]=myPref.getBoolean("AL_HUM", false);
        }
        if(myPref.contains("AL_SOIL")){
            alcheck[2]=myPref.getBoolean("AL_SOIL", false);
        }
        if(myPref.contains("AL_WAT")){
            alcheck[3]=myPref.getBoolean("AL_WAT", false);
        }

        //System.out.println("notificationString Result check");
        //System.out.println("receive check:"+str[0]+", "+str[1]+", "+str[2]+", "+str[3]);

        for(i=0; i<4; i++){
            num=notificationNum(alcheck,i);
            if(str[i].equals("on")&&alcheck[i]){
                result = result + strlist[i];
                //System.out.println("result" + i + ": " + result);
                if(num>0)
                    result=result+", ";
            }
        }
        result=result+" Warning!";

        return result;
    }
    public int notificationNum(boolean[] alcheck, int curnum){
        int i, num=0;
        for(i=curnum+1; i<4; i++){
            if(alcheck[i])
                num+=1;
        }
        return num-1;
    }

    //알림 실행
    public void notificationShow(String msg){
        //System.out.println("notification msg : "+msg);

        mNotificationManager= (NotificationManager)getSystemService(Context.NOTIFICATION_SERVICE);

        //notification 알림에 대한 설정 가능한 빌더 객체 생성
        mBuilder= new Notification.Builder(this)
                //알림 발생시 상태바에 띄울 아이콘 설정
                .setSmallIcon(R.drawable.sti1_s)
                //알림 타이틀 설정
                .setContentTitle("Smart Farm Alram")
                //알림 내용 설정
                .setContentText(msg);
        //확장뷰를 클릭 했을 때 이동할 엑티비티를 활성화할 인텐트
        Intent resultIntent= new Intent(this, HomeActivity.class);

        //확장 메세지 클릭시 시작할 엑티비티를 제어할 PendingIntent 생성
        //param1: PendingIntent가 시작 시켜야할 액티비티의 Context
        //param2: sender를 위한 요청 코드
        //param3: 시작될 엑티비티의 Intent;
        //Param4: 플래그

        PendingIntent resultPendingIntent=PendingIntent.getActivity(this,
                0,
                resultIntent,
                PendingIntent.FLAG_UPDATE_CURRENT);


        //생성한 PenddingIntent 설정
        mBuilder.setContentIntent(resultPendingIntent);

        //사용자가 알림 확인하면 초기화
        mBuilder.setAutoCancel(true);

        //알림 횟수 설정
        mBuilder.setNumber(++number);
        //알림 발생
        mNotificationManager.notify(1, mBuilder.build());
        //진동주기(** 퍼미션이 필요함 **)
        vibrator.vibrate(1000); //1초 동안 진동

    }
}