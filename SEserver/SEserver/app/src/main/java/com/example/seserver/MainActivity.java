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
  //bluetooth setting
  BluetoothAdapter mBluetoothAdapter;
  ArrayList<String> arr=new ArrayList<String>();

  FirebaseDatabase database;
  DatabaseReference[] seatRef;
  //description : when program created, it start
  //input : bundle of before state
  //output : null
  @Override
  protected void onCreate(Bundle savedInstanceState) {

    database = FirebaseDatabase.getInstance(); // firebase instance

    seatRef = new DatabaseReference[93];

    for(int i=1;i<92;i++){
      targetseat[i]="";
    }
    for (int i  = 1; i <= 92; i++) {
      // acquire link
      seatRef[i] = database.getReference("seat" + Integer.toString(i));
      seatRef[i].addValueEventListener(new ValueEventListener() {
        //real time database setting
        //input: data
        //output: null
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
        //description: announce database fail
        //input : databaseerror
        //output: null
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
    //gain GPS authority
    ActivityCompat.requestPermissions(this,
        new String[]{Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION}, 10);
    //bluetooth adapter declare
    mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
    IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
    registerReceiver(mReceiver, filter);
    new findbluetooth().start();
    new servermain().start();
    new timerstart().start();
  }
  //description : get bluetooth of each device
  //input : null
  //output : null
  private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
    @Override
    public void onReceive(Context context, Intent intent) {
      String action = intent.getAction();
      if(BluetoothDevice.ACTION_FOUND.equals(action)){
        BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
        arr.add(device.getAddress());
      }
    }
  };
  // description : make timer thread
  // input : null
  // output : null
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
  // description : server trhead
  // input : null
  // output : null
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
      //setting the output,input stream
      //setting user's id and mac address
      try {
        in = new BufferedReader(new InputStreamReader(
            socket.getInputStream()));
        out=new PrintWriter(socket.getOutputStream(),true);
        Log.d("confirm1","server");
        id=in.readLine();
        Log.d("confirm1 ididid",id);
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
        //handle each client's message
        while(true)
        {
          String msg;
          int success=0;
          //recognize client's message
          msg=in.readLine();
          Log.d("confirm1",msg);
          //getseat message is arrive
          if(msg.startsWith("getseat"))
          {
            ArrayList<String> arr_check=new ArrayList<String>();
            st = new StringTokenizer(msg, "/");
            String a = st.nextToken();
            String seat = st.nextToken();
            //wait bluetooth array is updated
            while(true){
              if(check==1){
                for(int i=0;i<arr.size();i++){
                  arr_check.add(arr.get(i));
                }
                Log.d("confirm1","check");
                break;
              }
            }
            //confirm user is near here by blutooth
            if(arr_check.contains(blueid)) {
              DatabaseReference myRef = database.getReference("seat" + seat);
              myRef.setValue("2/" + putid);
              out.println("success");
            }
            else{
              out.println("fail");
            }
          }
          //report messae is arrive
          if(msg.startsWith("report")){
            ArrayList<String> arr_check=new ArrayList<String>();
            st = new StringTokenizer(msg, "/");
            String a = st.nextToken();
            String seat = st.nextToken();
            DatabaseReference myRef = database.getReference("seat" + seat);
            String targetvalue=targetseat[Integer.parseInt(seat)];
            st = new StringTokenizer(targetvalue, "/");
            Log.d("confirm1",targetvalue);
            a=st.nextToken();
            String targetid=st.nextToken();
            while(true){
              if(check==1){
                for(int i=0;i<arr.size();i++){
                  arr_check.add(arr.get(i));
                }
                break;
              }
            }
            //confirm reporter is near here and reported user isn't near here by bluetooth
            if(arr_check.contains(blueid)){
              try{
                if(arr_check.contains(macset.get(targetid))){
                  success=1;
                }}
              catch(Exception e){
              }
            }
            //if report is successed
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
          //return message is arrived
          if(msg.startsWith("return")){
            ArrayList<String> arr_check=new ArrayList<String>();
            while(true){
              if(check==1){
                for(int i=0;i<arr.size();i++){
                  arr_check.add(arr.get(i));
                }
                break;
              }
            }
            if(arr_check.contains(blueid)) {
              st = new StringTokenizer(msg, "/");
              String a = st.nextToken();
              String seat = st.nextToken();
              DatabaseReference myRef = database.getReference("seat" + seat);
              myRef.setValue("2/" + putid);
              out.println("success");
            }
            else{
              out.println("fail");
            }
          }
        }
      }
      catch(Exception e){}
    }
  }
  //definition : make each socket's thread
  // input : null
  // output : null
  private class servermain extends Thread{
    public void run(){
      try {
        Thread.sleep(3000);
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
  // description : timer thread
  // input : null
  // output : null
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
        //timer setting
        Thread.sleep(1800000);
        //when timer is expired confirm the seat that other person get it
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
  // description : find near device by bluetooth ( unpdates every 3 second)
  // input : null
  // output : null
  private class findbluetooth extends Thread{
    public void run(){
      while(true) {
        try {
          //start to find device near here by bluetooth
          mBluetoothAdapter.startDiscovery();
          Thread.sleep(3000);
          //stop to find device
          mBluetoothAdapter.cancelDiscovery();
          check=1;
          //it is time that program compare list of device and requested device
          Thread.sleep(300);
          check=0;
          //delete all device's macaddress in arr
          arr.clear();
        } catch (Exception e) {
          Log.d("confirm1", "error!!!");
        }
      }
    }

  }
}