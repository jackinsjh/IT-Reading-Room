package com.example.se_tp;

import android.bluetooth.BluetoothAdapter;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class LoginActivity extends AppCompatActivity {
    public int check=0;
    public int login_finish=0;
    String userid;
    //description : When view 'activity_login' is start, this cord is start. And it is start Activity
    // input : Bundle of before state
    // output : null
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        Intent dIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
        dIntent.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION,3590);
        startActivity(dIntent);
        Button button = (Button)findViewById(R.id.login);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Login();

                if(check==1)
                {
                    Log.d("confirm1","success");

                    Intent home = new Intent(getApplicationContext(), com.example.se_tp.MainActivity.class);
                    home.putExtra("userid",userid);
                    startActivity(home);
                }
                else{
                    Log.d("confirm1","fail");
                }
            }
        });
    }
    //description : When user click 'log-in' button, it starts. this check id and pw with Gachon Cyber Campus.
    // input : null
    // output : null
    private void Login() {
        //final ProgressDialog progressDialog = ProgressDialog.show(LoginActivity.this, "", "로그인을 시도하는 중입니다.");
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                //get user's input
                EditText edt_userid = (EditText)findViewById(R.id.userid);
                EditText edt_password = (EditText)findViewById(R.id.password);
                userid = edt_userid.getText().toString();
                String passowrd = edt_password.getText().toString();
                try {
                    //send post message to gachon university server
                    URL url = new URL("https://cyber.gachon.ac.kr/login.php");
                    HttpURLConnection huc = (HttpURLConnection)url.openConnection();
                    huc.setRequestMethod("POST");
                    huc.setDoInput(true);
                    huc.setDoOutput(true);
                    huc.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
                    Log.d("what1","asd");
                    OutputStream os = huc.getOutputStream();
                    //setting data to proper form
                    String body = "username="+userid+"&password="+passowrd;
                    os.write(body.getBytes("euc-kr"));
                    os.flush();
                    os.close();
                    //get reply from gachon server
                    BufferedReader br = new BufferedReader( new InputStreamReader( huc.getInputStream(), "UTF-8" ));
                    String buf;
                    Thread.sleep(1000);
                    // confirm login is successed
                    while((buf=br.readLine())!=null) {
                        Log.d("confirm1", buf);
                        if(buf.contains("리다이"))
                        {
                            check=1;
                            Log.d("confirm1","check change");
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
                Log.d("confrim1","check1");
                //progressDialog.dismiss();
            }
        });thread.start();
        try {
            //wait until reply is arrived and confirm the reply data
            thread.join();
        }
        catch(Exception e){}
    }
}


