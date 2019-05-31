package com.example.se_tp;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.widget.TextView;

public class outPop extends Activity {
    TextView txtText;
    int seatNum;
    //description : When view 'activity_out_pop' is start, this cord is start
    // input : Bundle of before state
    // output : null
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_out_pop);

        txtText = (TextView) findViewById(R.id.txtText);

        // intent get data of seat number
        Intent intent = getIntent();
        seatNum = intent.getIntExtra("seatNum",1);
        String data  = (seatNum) + "번 자리에서 퇴실하시겠습니까?";

        txtText.setText(data);


    }
    //description : When user click out,it send message to MainActivity
    // input : ReportPop view
    // output : null
    public void mOnOut(View v){
        Intent intent = new Intent();
        intent.putExtra("seatNum", seatNum);
        setResult(RESULT_OK, intent);

        finish();
    }
    //description : When user click cancel,it send message to MainActivity
    // input : ReportPop view
    // output : null
    public void mOnClose(View v){
        Intent intent = new Intent();
        intent.putExtra("seatNum", "Close Popup");
        setResult(RESULT_CANCELED, intent);
        finish();
    }
    //description : When user click area of out of popup, it prevent touch.
    // input : touch input
    // output : null
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if(event.getAction()==MotionEvent.ACTION_OUTSIDE){
            return false;
        }
        return true;
    }
    //description : it prevent back button
    // input : null
    // output : null
    @Override
    public void onBackPressed() {
        return;
    }


}