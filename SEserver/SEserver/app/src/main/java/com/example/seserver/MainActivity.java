package com.example.seserver;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Handler;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.StringTokenizer;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;


public class MainActivity extends AppCompatActivity {
  private static HashMap<String, PrintWriter> ids=new HashMap<String,PrintWriter>();
  private static HashMap<String,String>macset=new HashMap<String,String>();
  private static final int PORT=9003;
  int check=0;
  int timer_check=0;
  String timer_id="";
  String timer_seat="";
  String[] targetseat=new String[93];
  int count=0;
  BluetoothAdapter mBluetoothAdapter;
  ArrayList<String> arr=new ArrayList<String>();

  FirebaseDatabase database;
  DatabaseReference[] seatRef;

  @Override
  protected void onCreate(Bundle savedInstanceState) {

    database = FirebaseDatabase.getInstance(); // 파이어베이스 인스턴스 얻기

    seatRef = new DatabaseReference[93];

    for(int i=1;i<92;i++){
      targetseat[i]="";
    }
    for (int i  = 1; i <= 92; i++) {
      // 시트 속성 링크 얻기
      seatRef[i] = database.getReference("seat" + Integer.toString(i));
      seatRef[i].addValueEventListener(new ValueEventListener() {
        @Override
        public void onDataChange(DataSnapshot dataSnapshot) {
          String value = dataSnapshot.getValue(String.class);
          String key=dataSnapshot.getKey();
          StringTokenizer token=new StringTokenizer(key,"t");
          String seatnum=token.nextToken();
          seatnum=token.nextToken();
          int num=Integer.parseInt(seatnum);
          targetseat[num]=value;
        }
        @Override
        public void onCancelled(DatabaseError databaseError) {
          // Failed to read value
          Log.e("error on reading DB", "Failed to read value.",
              databaseError.toException());
        }
      });
    }
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);
    ActivityCompat.requestPermissions(this,
        new String[]{Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION}, 10);
    mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
    IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
    registerReceiver(mReceiver, filter);
    new findbluetooth().start();
    new servermain().start();
    new timerstart().start();
  }
  private final BroadcastReceiver mReceiver = new BroadcastReceiver() { //각각의 디바이스로부터 정보를 받으려면 만들어야함
    @Override
    public void onReceive(Context context, Intent intent) {
      String action = intent.getAction();
      if(BluetoothDevice.ACTION_FOUND.equals(action)){
        BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
        arr.add(device.getAddress());
        Log.d("confirm1",device.getAddress());
      }
    }
  };
  private class timerstart extends Thread{
    public void run(){
      while(true){
      if(timer_check==1)
      {
        new timer(timer_seat,timer_id).start();
        timer_check=0;
      }
    }
    }
  }
  private class server extends Thread{
    private Socket socket;
    private String id;
    private BufferedReader in;
    private PrintWriter out;
    private  StringTokenizer st;
    private String putid;
    private String blueid;
    public server(Socket socket){
      this.socket=socket;
    }
    public void run(){
      try {
        in = new BufferedReader(new InputStreamReader(
            socket.getInputStream()));
        out=new PrintWriter(socket.getOutputStream(),true);
        Log.d("confirm1","server");
        id=in.readLine();
        Log.d("confirm1",id);
        if(id==null)
          return;
        st=new StringTokenizer(id,"/");
        putid=st.nextToken();
        blueid=st.nextToken();
        synchronized(ids){
          if(!ids.containsKey(putid)){
            ids.put(putid,out);
          }
        }
        synchronized(macset){
          if(!macset.containsKey(blueid)){
            macset.put(putid,blueid);
          }
        }
        while(true)
        {
          String msg;
          int success=0;
          msg=in.readLine();
          Log.d("confirm1",msg);
          if(msg.startsWith("getseat"))
          {
            ArrayList<String> arr_check=new ArrayList<String>();
            st = new StringTokenizer(msg, "/");
            String a = st.nextToken();
            String seat = st.nextToken();
            /*while(true){
              if(check==1){
                for(int i=0;i<arr.size();i++){
                  arr_check.add(arr.get(i));
                }
                Log.d("confirm1","check");
                break;
              }
            }*/
            /*for(int i=0;i<arr_check.size();i++){
              Log.d("confirm1 bluetooth",arr_check.get(i));
            }*/
            Log.d("confirm1",blueid);
            //if(arr_check.contains(blueid)) {
              Log.d("confirm1","check1");
              DatabaseReference myRef = database.getReference("seat" + seat);
              myRef.setValue("2/" + putid);
              out.println("success");
            //}
            /*else{
              out.println("fail");
            }*/
          }
          if(msg.startsWith("report")){
            ArrayList<String> arr_check=new ArrayList<String>();
            st = new StringTokenizer(msg, "/");
            String a = st.nextToken();
            String seat = st.nextToken();
            DatabaseReference myRef = database.getReference("seat" + seat);
            String targetvalue=targetseat[Integer.parseInt(seat)];
            st = new StringTokenizer(targetvalue, "/");
            a=st.nextToken();
            String targetid=st.nextToken();
            /*while(true){
              if(check==1){
                for(int i=0;i<arr.size();i++){
                  arr_check.add(arr.get(i));
                }
                break;
              }
            }*/
            //if(arr_check.contains(blueid)){
              if(arr_check.contains(macset.get(targetid))){
                success=1;
              }
            //}
            if(success==1)
            {
              myRef.setValue("3/" + targetid);
              timer_seat=seat;
              timer_id=targetid;
              out.println("success");
              timer_check=1;
            }
            else{
              out.println("fail");
            }
          }
          if(msg.startsWith("return")){
            ArrayList<String> arr_check=new ArrayList<String>();
            /*while(true){
              if(check==1){
                for(int i=0;i<arr.size();i++){
                  arr_check.add(arr.get(i));
                }
                break;
              }
            }*/
            //if(arr_check.contains(blueid)) {
              Log.d("confrim1","check2");
              st = new StringTokenizer(msg, "/");
              String a = st.nextToken();
              String seat = st.nextToken();
              DatabaseReference myRef = database.getReference("seat" + seat);
              myRef.setValue("2/" + putid);
              out.println("success");
            //}
            //else{
              //out.println("fail");
            //}
          }
        }
      }
      catch(Exception e){}
    }
  }
  private class servermain extends Thread{
    public void run(){
      try {
        Thread.sleep(3000);
        Log.d("confirm1","servermaain");
        ServerSocket listener = new ServerSocket(PORT);
        while(true){
        new server(listener.accept()).start();
        }
      }
      catch(Exception e){
        Log.d("confirm1","error!!!");
      }
    }
  }
  private class timer extends Thread{
    String seat="";
    String id="";
    public timer(String inputseat,String inputid)
    {
      seat=inputseat;
      id=inputid;
    }
    public void run(){
      try {
        Thread.sleep(1800000);
        DatabaseReference myRef = database.getReference("seat" + seat);
        String targetvalue=targetseat[Integer.parseInt(seat)];
        StringTokenizer st = new StringTokenizer(targetvalue,"/");
        String a=st.nextToken();
        String targetid=st.nextToken();
        if(targetid.compareTo(id)==1&&a.compareTo("3")==1){
          myRef.setValue('1');
        }
      }
      catch(Exception e){
        Log.d("confirm1","error!!!");
      }
    }
  }
  private class findbluetooth extends Thread{
    public void run(){
      while(true) {
        try {
          Log.d("confirm1","afjalsjfkajsf");
          mBluetoothAdapter.startDiscovery();
          Thread.sleep(10000);
          mBluetoothAdapter.cancelDiscovery();
          check=1;
          Thread.sleep(100);
          check=0;
          arr.clear();
        } catch (Exception e) {
          Log.d("confirm1", "error!!!");
        }
      }
    }

  }
}