package com.example.se_tp;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.content.Intent;
import android.os.Bundle;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.NetworkInterface;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.StringTokenizer;


public class MainActivity extends Activity implements View.OnClickListener{

    public int mySeat=-1;
    public int myState=1;
    public String myID="rjsj19951";
    public Button[] seatBtn = new Button[93]; // Total Seat is 92, and it is 1 ~ 92 , 0 is not used
    Handler handler = new Handler();
    TextView txtview;
    public int[] seatState = new int[93];// state 1 : empty , 2 : Occupy 3: Waiting empty
    public ArrayList<String> msgNum;
    public int selBtn=-1;
    public int beforeBtn=-1;

    private static final int REQUEST_ENABLE_BT = 0;
    BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
    StringTokenizer getmac;
    String unit;
    String bluemac = "";
    BufferedReader in;
    PrintWriter out;


    public FirebaseDatabase database; // Firebase object
    public DatabaseReference[] seatRef = new DatabaseReference[93]; // it link DB form seat data simultaneously, index 0 isn't used


    //description : When view 'activity_main' is start, this cord is start
    // input : Bundle of before state
    // output : null
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        int target;
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Intent home = getIntent();
        myID = home.getExtras().getString("userid");
        txtview = (TextView)findViewById(R.id.mySeatView);
        txtview.setText("사용자의 자리 : "+ "    번 " +"\n 아이디 : " + myID);

        // it check bluetooth adapter is enable
         if(!mBluetoothAdapter.isEnabled()){
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
        }else {}

        //it connect server
        String macAddress = getMACAddress("wlan0");
        getmac = new StringTokenizer(macAddress, ":");
        Log.d("confirm1","here1");
        for (int i = 0; i < 5; i++) {
            unit = getmac.nextToken();
            bluemac = bluemac + unit + ":";
        }

        String t=getmac.nextToken();
        int k=Integer.parseInt(t);
        k=k-1;
        bluemac=bluemac+k;

        client C = new client();
        C.start();



        msgNum = new ArrayList<String>();
        // initialize the seat State
        for(int i=1; i <= 92; i++){
            seatState[i] = 1;// all is empty at start
        }
        createButton();
        txtview = (TextView)findViewById(R.id.mySeatView);

        msgNum.add("안쓰는 0번 인덱스");
        // it allocate the realtime DB listener
        for(int i=1; i <= 92; i++){
            seatBtn[i].setTag(i);
            seatBtn[i].setOnClickListener(this);
            msgNum.add((i) + "번 자리입니다.");
        }
        // obtain Firebase instance
        database = FirebaseDatabase.getInstance();


        // 1~92 seat data receive
        for (int i = 1; i <= 92; i++) {
            // get seat reference link
            seatRef[i] = database.getReference("seat" + Integer.toString(i));

            // LIstener
            seatRef[i].addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    // This method is called once with the initial value and again
                    // whenever data at this location is updated.
                    String value = dataSnapshot.getValue(String.class); // String value is data form DB
                    String key = dataSnapshot.getKey(); // get Key form DB

                    int thisSeatNum = Integer.valueOf(key.substring(4)); // Current Seat Number
                    // Obtain object seat
                    String buttonID = "seat" + thisSeatNum;
                    int resID = getResources().getIdentifier(buttonID, "id", getPackageName());
                    seatBtn[thisSeatNum] = ((Button) findViewById((resID)));

                    // if seat state is empty, it set button image color1
                    if (value.substring(0, 1).equals("1"))
                    {
                        seatBtn[thisSeatNum].setBackgroundResource(R.drawable.imgbtn1);
                        seatState[thisSeatNum] = 1;
                    }
                    // if seat state is occupy, it set button image color2
                    else if (value.substring(0, 1).equals("2"))
                    {
                        seatBtn[thisSeatNum].setBackgroundResource(R.drawable.imgbtn2);
                        seatState[thisSeatNum] = 2;
                    }
                    // if seat state is waiting empty, it set button image color3
                    else if (value.substring(0, 1).equals("3"))
                    {
                        seatBtn[thisSeatNum].setBackgroundResource(R.drawable.imgbtn3);
                        seatState[thisSeatNum] = 3;
                    }
                    else
                    {
                    }




                    // It check seat owner's id
                    if (value.substring((value.indexOf("/") + 1)).equals(myID))
                    {
                        mySeat = thisSeatNum;
                        myState = Integer.valueOf(value.substring(0, 1));
                        if (myState == 2) {
                            seatBtn[thisSeatNum].setBackgroundResource(R.drawable.imgbtn2);
                        }
                        else if (myState == 3) {
                            seatBtn[thisSeatNum].setBackgroundResource(R.drawable.imgbtn3);
                        }
                        else {
                            Log.e("initializing", "invalid state");
                        }

                        txtview.setText("사용자의 자리 : "+ (mySeat) + "번 " +"\n 아이디 : " + myID);
                    }
                    else{

                    }

                }

                // If it link is fail, it start
                @Override
                public void onCancelled(DatabaseError databaseError) {
                    // Failed to read value
                    Log.e("error on reading DB", "Failed to read value.",
                            databaseError.toException());
                }
            });

        }
    }

    //description : it is client Thread to check bluetooth
    // input : null
    // output : null
    private class client extends Thread {
        public void run() {
            try {
                Log.d("confirm1","here3");
                Socket socket = new Socket("211.114.129.97",9003);
                Log.d("confirm1","here3");
                in = new BufferedReader(new InputStreamReader(
                    socket.getInputStream()));
                out = new PrintWriter(socket.getOutputStream(), true);
                out.println(myID + "/" + bluemac);
                while(true){
                    int q=1;
                }
            } catch (Exception e) {
                Log.d("confirm1", "error!???!???!");
            }
        }
    }

    //description : it get MAC address to check bluetooth
    // input : null
    // output : null
    public static String getMACAddress(String interfaceName) {
        try {
            List<NetworkInterface> interfaces = Collections.list(NetworkInterface.getNetworkInterfaces());
            for (NetworkInterface intf : interfaces) {
                if (interfaceName != null) {
                    if (!intf.getName().equalsIgnoreCase(interfaceName)) continue;
                }
                byte[] mac = intf.getHardwareAddress();
                if (mac == null) return "";
                StringBuilder buf = new StringBuilder();
                for (byte aMac : mac) buf.append(String.format("%02X:", aMac));
                if (buf.length() > 0) buf.deleteCharAt(buf.length() - 1);
                return buf.toString();
            }
        } catch (Exception ignored) {
        } // for now eat exceptions
        return "";
    }


    //description : it is onClick method to all button
    // input : clicked button view
    // output : null
    @Override
    public void onClick(View v)
    {

        Button newButton = (Button) v;
        // this 'for' find clicked button
        for(Button tempButton : seatBtn)
        {
            // Find clicked button
            if(tempButton == newButton)
            {
                int position = (Integer)v.getTag();
                Message msg = handler.obtainMessage();
                msg.arg1 = position;// number of seat
                selBtn = position;
                handler.sendMessage(msg);// send message to handleMessage
                Toast.makeText(this, msgNum.get(position), Toast.LENGTH_SHORT).show();
            }
        }
    }

    //description : Handler class is used to install popUp
    // input : null
    // output : null
    public class Handler extends android.os.Handler {
        // Receive messages
        public void handleMessage(Message msg){
            // it check button is double click
            if(beforeBtn == msg.arg1){
                // it check seat state is empty, and user's state is empty
                if(seatState[msg.arg1] == 1 && myState ==1){
                    Intent intent = new Intent(MainActivity.this, requestPop.class);
                    intent.putExtra("seatNum", msg.arg1);
                    startActivityForResult(intent, 1);
                }
                else if((seatState[msg.arg1] == 2) && (mySeat == msg.arg1)){// it check seat state is occupy, and it check that seat is  user's seat
                    Intent intent = new Intent(MainActivity.this, outPop.class);
                    intent.putExtra("seatNum", msg.arg1);
                    startActivityForResult(intent, 4);
                }
                else if((seatState[msg.arg1] == 2) && (mySeat != msg.arg1)){//it check other occupied seat
                    Intent intent = new Intent(MainActivity.this, reportPop.class);
                    intent.putExtra("seatNum", msg.arg1);
                    startActivityForResult(intent, 2);
                }
                else if((seatState[msg.arg1] == 3) && (mySeat == msg.arg1)){// when waiting empty state button is clicked, it start
                    Intent intent = new Intent(MainActivity.this, waitingPop.class);
                    intent.putExtra("seatNum", msg.arg1);
                    startActivityForResult(intent, 3);
                }

            }
            else if(beforeBtn != -1) {//befeorBtn isn't empty , it start
                if(seatState[beforeBtn] == 1){
                    seatBtn[beforeBtn].setBackgroundResource(R.drawable.imgbtn1);

                }
                else if(seatState[beforeBtn] == 2){
                    seatBtn[beforeBtn].setBackgroundResource(R.drawable.imgbtn2);

                }
                else if(seatState[beforeBtn] == 3){
                    seatBtn[beforeBtn].setBackgroundResource(R.drawable.imgbtn3);

                }
                if(seatState[msg.arg1] == 1){
                    seatBtn[msg.arg1].setBackgroundResource(R.drawable.secbtn1);

                }
                else if(seatState[msg.arg1] == 2){
                    seatBtn[msg.arg1].setBackgroundResource(R.drawable.setbtn2);

                }
                else if(seatState[msg.arg1] == 3){
                    seatBtn[msg.arg1].setBackgroundResource(R.drawable.setbtn3);

                }
                beforeBtn = msg.arg1;
            }
            else{
                beforeBtn = msg.arg1;
                seatBtn[msg.arg1].setBackgroundResource(R.drawable.secbtn1);

            }

        }
    }

    //description : it allocate btn id to array
    // input : null
    // output : null
    public void createButton(){
        for(int i = 1; i <= 92; i++) {
            String buttonID = "seat" + i;
            int resID = getResources().getIdentifier(buttonID, "id", getPackageName());
            seatBtn[i] = ((Button) findViewById((resID)));
        }

    }// align by findVIewByID

    //description : it receive message from pop up
    // input : requsertcode separate popup, resultcode is ok and cancel, Intent data is message
    // output : null
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        String res="";
        if(requestCode==1){// it is request pop up
            if(resultCode==RESULT_OK){

                int result = data.getIntExtra("seatNum",1);
                getseat seat=new getseat(result);
                seat.start();
                try {
                    seat.join();
                }
                catch(Exception e){}
            }
        }
        else if(requestCode==2){//it is report pop up
            if(resultCode==RESULT_OK){
                int result = data.getIntExtra("seatNum",1);
                putreport report=new putreport(result);
                report.start();
                try{
                    report.join();
                }
                catch(Exception e){}
            }
        } else if(requestCode==3){//it is comeback pop up
            if(resultCode==RESULT_OK){
                int result = data.getIntExtra("seatNum",1);
                returnseat returns = new returnseat(result);
                returns.start();
                try{
                    returns.join();
                }
                catch(Exception e){}
            }

        }
        else if(requestCode==4){//it is out pop up
            if(resultCode==RESULT_OK){
                int result = data.getIntExtra("seatNum",1);
                seatRef[result].setValue("1/");
                seatBtn[result].setBackgroundResource(R.drawable.secbtn1);
                seatState[result] = 1;
                mySeat = -1;
                myState=1;

                txtview.setText("사용자의 자리 : "+ (mySeat) + "번 " +"\n 아이디 : " + myID);
            }
        }
    }

    //description : send message that get seat
    // input : null
    // output : null

    private class getseat extends Thread {
        int result;
        public getseat(int k)
        {
            this.result=k;
        }
        public void run() {
            String res="";
            try{
                Thread.sleep(1000);
            }
            catch(Exception e){}
            //send message server
            out.println("getseat/"+result);
            try {
                res = in.readLine();
                Log.d("confirm1",res);
            }
            catch(Exception e){}
            //if request is successed gui and state is changed
            if(res.compareTo("success")==1)
            {
                seatBtn[result].setBackgroundResource(R.drawable.setbtn2);
                seatState[result] =2;
            }
        }
    }

    //description : send message that report empty seat
    // input : null
    // output : null
    private class putreport extends Thread {
        int result;
        public putreport(int k)
        {
            this.result=k;
        }
        public void run() {
            String res="";
            try{
                Thread.sleep(1000);
            }
            catch(Exception e){}
            out.println("report/"+result);
            try {
                res = in.readLine();
            }
            catch(Exception e){}
            //if request is successed gui and state is changed
            if(res.compareTo("success")==1)
            {
                seatBtn[result].setBackgroundResource(R.drawable.setbtn3);
                seatState[result] = 3;
            }
        }
    }
    //description : send message that return the seat
    // input : null
    // output : null
    private class returnseat extends Thread {
        int result;

        public returnseat(int k) {
            this.result = k;
        }

        public void run() {
            String res = "";
            try {
                Thread.sleep(1000);
            } catch (Exception e) {
            }
            seatBtn[result].setBackgroundResource(R.drawable.setbtn3);
            seatState[result] = 3;
            out.println("return/" + result);
            try {
                res = in.readLine();
            } catch (Exception e) {
            }
            //if request is successed gui and state is changed
            if (res.compareTo("success") == 1) {
                seatBtn[result].setBackgroundResource(R.drawable.setbtn2);
                seatState[result] = 2;
            }
        }
    }



}

