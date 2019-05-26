package com.example.se_tp;

import android.content.Intent;
import android.app.Activity;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.widget.TextView;

public class waitingPop extends Activity {
    TextView txtText;
    int seatNum;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_waiting_pop);

        txtText = (TextView) findViewById(R.id.txtText);

        //데이터 가져오기
        Intent intent = getIntent();
        seatNum = intent.getIntExtra("seatNum",1);
        String data  = (seatNum) + "번 자리로 복귀하셨습니까?";

        txtText.setText(data);


    }

    public void mOnComback(View v){
        //데이터 전달하기
        Intent intent = new Intent();
        intent.putExtra("seatNum", seatNum);
        setResult(RESULT_OK, intent);

        //액티비티(팝업) 닫기
        finish();
    }
    public void mOnClose(View v){
        //데이터 전달하기
        Intent intent = new Intent();
        intent.putExtra("seatNum", "Close Popup");
        setResult(RESULT_CANCELED, intent);

        //액티비티(팝업) 닫기
        finish();
    }
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        //바깥레이어 클릭시 안닫히게
        if(event.getAction()==MotionEvent.ACTION_OUTSIDE){
            return false;
        }
        return true;
    }

    @Override
    public void onBackPressed() {
        //안드로이드 백버튼 막기
        return;
    }


}