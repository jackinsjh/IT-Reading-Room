package com.example.se_tp;

import android.app.Activity;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.webkit.CookieManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpCookie;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;
import java.util.Map;

public class LoginActivity extends AppCompatActivity {
    public int check=0;
    public int login_finish=0;
    String userid;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        /*Intent dIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
        dIntent.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION,3590);
        startActivity(dIntent);*/
        Button button = (Button)findViewById(R.id.login);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Login();

                if(check==1)
                {
                    Log.d("confirm1","success");
                    /*Intent putintent = new Intent(LoginActivity.this,MainActivity.class);
                    Bundle mybundle=new Bundle();
                    mybundle.putString("userid",userid);
                    putintent.putExtras(mybundle);*/
                    Intent home = new Intent(getApplicationContext(), com.example.se_tp.MainActivity.class);
                    startActivity(home);
                }
                else{
                    Log.d("confirm1","fail");
                }
            }
        });
    }

    private void Login() {
        //final ProgressDialog progressDialog = ProgressDialog.show(LoginActivity.this, "", "로그인을 시도하는 중입니다.");
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                EditText edt_userid = (EditText)findViewById(R.id.userid);
                EditText edt_password = (EditText)findViewById(R.id.password);
                userid = edt_userid.getText().toString();
                String passowrd = edt_password.getText().toString();
                try {
                    Log.d("confirm1","what");
                    URL url = new URL("https://cyber.gachon.ac.kr/login.php");
                    HttpURLConnection huc = (HttpURLConnection)url.openConnection();
                    huc.setRequestMethod("POST");
                    huc.setDoInput(true);
                    huc.setDoOutput(true);
                    huc.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
                    Log.d("what1","asd");
                    OutputStream os = huc.getOutputStream();
                    String body = "username="+userid+"&password="+passowrd;
                    os.write(body.getBytes("euc-kr"));
                    os.flush();
                    os.close();
                    BufferedReader br = new BufferedReader( new InputStreamReader( huc.getInputStream(), "UTF-8" ));
                    String buf;
                    Thread.sleep(1000);
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
            thread.join();
        }
        catch(Exception e){}
    }
}


