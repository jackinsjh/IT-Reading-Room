package com.example.se_tp;

import android.bluetooth.BluetoothAdapter;
import android.content.Intent;
import android.os.Handler;
import android.os.Message;
import android.app.Activity;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import pl.polidea.view.ZoomView;

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


/*
사용자 UI와 XML, 코드, DB에서 자리 번호는 모두 1부터 시작함. (0부터 시작이 아닌)
 */


public class MainActivity extends Activity implements View.OnClickListener{

    public int mySeat=-1;
    public int myState=0;
    public String myID="rjsj19951";
    public Button[] seatBtn = new Button[93]; // 인덱스 0 미사용
    Handler handler = new Handler();
    TextView txtview;
    public int[] seatState = new int[93];// 0 = 초기화 , 1 = 공석 , 2= 자리있음,3 = 공석 대기 , 인덱스 0 미사용
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


    public FirebaseDatabase database; // 데이터베이스 객체
    public DatabaseReference[] seatRef = new DatabaseReference[93]; // 각각의 seat 속성에 대한 DB 링크, 인덱스 0 미사용




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        int target;
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        /*Intent intent=getIntent();
        Bundle bundle1=intent.getExtras();
        myID=bundle1.getString("userid");*/
        Log.d("confirm1","here1");
         if(!mBluetoothAdapter.isEnabled()){
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
        }else {}

        //server
        String macAddress = getMACAddress("wlan0");
        getmac = new StringTokenizer(macAddress, ":");
        Log.d("confirm1","here1");
        for (int i = 0; i < 5; i++) {
            unit = getmac.nextToken();
            bluemac = bluemac + unit + ":";
        }
        /*char makemac[]=getmac.nextToken().toCharArray();
        makemac[1]=(char)(int)(makemac[1]+1);
        String make=""+makemac;*/
        String t=getmac.nextToken();
        int k=Integer.parseInt(t);
        k=k-1;
        bluemac=bluemac+k;
        Log.d("confirm1",bluemac);
        Log.d("confirm1","here1-2");
        Log.d("confirm1","here1-2");
        //bluemac = bluemac + make;
        //Log.d("confirm1 bluemac",bluemac);
        client C = new client();
        C.start();

        // 테스트용

        msgNum = new ArrayList<String>();

        for(int i=1; i <= 92; i++){
            seatState[i] = 1;// 초기화
        }
        createButton();
        txtview = (TextView)findViewById(R.id.mySeatView);

        msgNum.add("안쓰는 0번 인덱스"); // msgNum 의 0번 인덱스 무효화 (1번부터 시작하도록)
        for(int i=1; i <= 92; i++){
            seatBtn[i].setTag(i);
            seatBtn[i].setOnClickListener(this);
            msgNum.add((i) + "번 자리입니다.");
        }
        Log.d("confirm1","here1-2wadadwaq");







        database = FirebaseDatabase.getInstance(); // 파이어베이스 인스턴스 얻기


        // 각각 파이어베이스의 시트들마다
        for (int i = 1; i <= 92; i++) {
            // 시트 속성 링크 얻기
            seatRef[i] = database.getReference("seat" + Integer.toString(i));

            Log.d("FBListener", seatRef[i].getKey());


            // 값 변경시의 리스너 설정
            seatRef[i].addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    // This method is called once with the initial value and again
                    // whenever data at this location is updated.
                    String value = dataSnapshot.getValue(String.class); // 시트 내의 String 값
                    String key = dataSnapshot.getKey(); // 시트 명 (seat5 이런거)
                    Log.d("FBListener", "from FB snapshot key : " + key);
                    Log.d("FBListener", "from FB snapshot value : " + value);


                    int thisSeatNum = Integer.valueOf(key.substring(4)); // 현재 작업중인 시트의 번호
                    // 시트 버튼 객체 얻어오기
                    String buttonID = "seat" + thisSeatNum;
                    int resID = getResources().getIdentifier(buttonID, "id", getPackageName());
                    seatBtn[thisSeatNum] = ((Button) findViewById((resID)));

                    // 만약 공석이라면 공석으로 버튼 색 변경
                    if (value.substring(0, 1).equals("1"))
                    {
                        seatBtn[thisSeatNum].setBackgroundResource(R.drawable.imgbtn1);
                        seatState[thisSeatNum] = 1;
                    }
                    // 만약 사용중이라면 사용중으로 버튼 색 변경
                    else if (value.substring(0, 1).equals("2"))
                    {
                        seatBtn[thisSeatNum].setBackgroundResource(R.drawable.imgbtn2);
                        seatState[thisSeatNum] = 2;
                    }
                    // 만약 공석대기중이라면 공석대기중으로 버튼 색 변경
                    else if (value.substring(0, 1).equals("3"))
                    {
                        seatBtn[thisSeatNum].setBackgroundResource(R.drawable.imgbtn3);
                        seatState[thisSeatNum] = 3;
                    }
                    else
                    {
                        Log.e("FBListener", "unknown state number");
                    }

                    Log.d("FBListener", "thisSeatNum : " + thisSeatNum + " resID : "
                            + resID + " buttonID : " + buttonID);



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

                        txtview.setText("사용자의 자리 : "+ (mySeat) + "번 ");
                    }

                }

                // 정보 읽기 실패 시
                @Override
                public void onCancelled(DatabaseError databaseError) {
                    // Failed to read value
                    Log.e("error on reading DB", "Failed to read value.",
                            databaseError.toException());
                }
            });

        }
    }
    //client thread
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
    //server
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

    @Override
    public void onClick(View v)
    {

        // 클릭된 뷰를 버튼으로 받아옴
        Button newButton = (Button) v;

        // 향상된 for문을 사용, 클릭된 버튼을 찾아냄
        for(Button tempButton : seatBtn)
        {
            // 클릭된 버튼을 찾았으면
            if(tempButton == newButton)
            {
                // 위에서 저장한 버튼의 포지션을 태그로 가져옴
                int position = (Integer)v.getTag();
                Message msg = handler.obtainMessage();
                msg.arg1 = position;// 숫자
                selBtn = position;
                handler.sendMessage(msg);// send message to handleMessage
                // 태그로 가져온 포지션을 이용해 리스트에서 출력할 데이터를 꺼내서 토스트 메시지 출력
                Toast.makeText(this, msgNum.get(position), Toast.LENGTH_SHORT).show();
            }
        }
    }
    public class Handler extends android.os.Handler {
        public void handleMessage(Message msg){// 상태에 따라 팝업 or 선택 표시로 이미지 변경
            if(beforeBtn == msg.arg1){// 더블 클릭 했을때 팝업 띄우기
                if(seatState[msg.arg1] == 1){// 공석
                    Intent intent = new Intent(MainActivity.this, requestPop.class);
                    intent.putExtra("seatNum", msg.arg1);
                    startActivityForResult(intent, 1);
                }
                else if((seatState[msg.arg1] == 2) && (mySeat == msg.arg1)){// 자리있음 ,퇴실
                    Intent intent = new Intent(MainActivity.this, outPop.class);
                    intent.putExtra("seatNum", msg.arg1);
                    startActivityForResult(intent, 4);
                }
                else if((seatState[msg.arg1] == 2) && (mySeat != msg.arg1)){// 자리있음 ,신고
                    Intent intent = new Intent(MainActivity.this, reportPop.class);
                    intent.putExtra("seatNum", msg.arg1);
                    startActivityForResult(intent, 2);
                }
                else if((seatState[msg.arg1] == 3) && (mySeat == msg.arg1)){// 공석대기
                    Intent intent = new Intent(MainActivity.this, waitingPop.class);
                    intent.putExtra("seatNum", msg.arg1);
                    startActivityForResult(intent, 3);
                }

            }
            else if(beforeBtn != -1) {//이전에 클릭했던 것이 있을때 이전꺼를 초기화 시킨다.
                if(seatState[beforeBtn] == 1){ // 공석
                    seatBtn[beforeBtn].setBackgroundResource(R.drawable.imgbtn1);

                }
                else if(seatState[beforeBtn] == 2){ // 자리있음
                    seatBtn[beforeBtn].setBackgroundResource(R.drawable.imgbtn2);

                }
                else if(seatState[beforeBtn] == 3){ // 공석 대기중
                    seatBtn[beforeBtn].setBackgroundResource(R.drawable.imgbtn3);

                }
                if(seatState[msg.arg1] == 1){ // 공석
                    seatBtn[msg.arg1].setBackgroundResource(R.drawable.secbtn1);

                }
                else if(seatState[msg.arg1] == 2){ // 자리있음
                    seatBtn[msg.arg1].setBackgroundResource(R.drawable.setbtn2);

                }
                else if(seatState[msg.arg1] == 3){ // 공석 대기중
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

    // seatBtn array 에 각 버튼들의 객체 얻어오기
    public void createButton(){
        for(int i = 1; i <= 92; i++) {
            String buttonID = "seat" + i;
            int resID = getResources().getIdentifier(buttonID, "id", getPackageName());
            seatBtn[i] = ((Button) findViewById((resID)));
        }

    }// align by findVIewByID

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        String res="";
        if(requestCode==1){// 신청
            if(resultCode==RESULT_OK){
                //데이터 받기
                //result=seatnumber
                //신청이 들어옴
                int result = data.getIntExtra("seatNum",1);
                getseat seat=new getseat(result);
                Log.d("confirm1",""+result);
                seat.start();
                try {
                    seat.join();
                }
                catch(Exception e){}
                Log.d("confirm1","sueccess finish get seat");
            }
        }
        else if(requestCode==2){// 신고
            if(resultCode==RESULT_OK){
                //데이터 받기
                int result = data.getIntExtra("seatNum",1);
                putreport report=new putreport(result);
                report.start();
                try{
                    report.join();
                }
                catch(Exception e){}
            }
        } else if(requestCode==3){// 복귀신고
            if(resultCode==RESULT_OK){
                //데이터 받기
                int result = data.getIntExtra("seatNum",1);
                returnseat returns = new returnseat(result);
                returns.start();
                try{
                    returns.join();
                }
                catch(Exception e){}
            }
            /* 의미없음??
            if(resultCode==RESULT_CANCELED){
                //데이터 받기

                int result = data.getIntExtra("seatNum",1);
                seatBtn[result].setBackgroundResource(R.drawable.setbtn3);
                seatState[result] = 3;

            }*/
        }
        else if(requestCode==4){// 퇴실
            if(resultCode==RESULT_OK){
                //데이터 받기
                int result = data.getIntExtra("seatNum",1);
                seatRef[result].setValue("1/");
                seatBtn[result].setBackgroundResource(R.drawable.secbtn1);
                seatState[result] = 1;
                mySeat = -1;
                txtview.setText("사용자의 자리 : ");
            }
        }
    }
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
            out.println("getseat/"+result);
            try {
                res = in.readLine();
                Log.d("confirm1",res);
            }
            catch(Exception e){}
            if(res.compareTo("success")==1)
            {
                seatBtn[result].setBackgroundResource(R.drawable.setbtn2);
                seatState[result] = 2;
            }
        }
    }
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
            if(res.compareTo("success")==1)
            {
                seatBtn[result].setBackgroundResource(R.drawable.setbtn3);
                seatState[result] = 3;
            }
        }
    }
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
            out.println("report/" + result);
            try {
                res = in.readLine();
            } catch (Exception e) {
            }
            if (res.compareTo("success") == 1) {
                seatBtn[result].setBackgroundResource(R.drawable.setbtn2);
                seatState[result] = 2;
            }
        }
    }
    /*
    public void setSeatUI(){// seat State에 따라서 전체 gui 최신화

    }
    */


}

