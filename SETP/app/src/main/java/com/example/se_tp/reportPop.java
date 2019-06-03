package com.example.se_tp;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.widget.TextView;

/**
 * @brief -ReportPop Activity Java class
 * @details - this class include Popup GUI Logic
 *
 */
public class reportPop extends Activity {
    TextView txtText;
    int seatNum;
    /**
     * @brief - reportPop Activity Create method
     * @details -When view 'activity_report_pop' is start, this cord is start
     * @input -Bundle of before state
     * @output- null
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_report_pop);

        txtText = (TextView) findViewById(R.id.txtText);

        // intent get data of seat number
        Intent intent = getIntent();
        seatNum = intent.getIntExtra("seatNum",1);
        String data  = (seatNum) + "번 자리를 신고하시겠습니까?";

        txtText.setText(data);


    }

    /**
     * @brief - click report botton
     * @details -When user click report,it send message to MainActivity
     * @input -ReportPop view
     * @output- null
     */
    public void mOnReport(View v){
        //데이터 전달하기
        Intent intent = new Intent();
        intent.putExtra("seatNum", seatNum);
        setResult(RESULT_OK, intent);

        //액티비티(팝업) 닫기
        finish();
    }

    /**
     * @brief - click cancle button
     * @details -When user click cancel,it send message to MainActivity
     * @input -ReportPop view
     * @output- null
     */
    public void mOnClose(View v){
        //데이터 전달하기
        Intent intent = new Intent();
        intent.putExtra("seatNum", "Close Popup");
        setResult(RESULT_CANCELED, intent);

        //액티비티(팝업) 닫기
        finish();
    }

    /**
     * @brief - prevent click to out of popup
     * @details -When user click area of out of popup, it prevent touch.
     * @input -touch input
     * @output- null
     */
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        //바깥레이어 클릭시 안닫히게
        if(event.getAction()==MotionEvent.ACTION_OUTSIDE){
            return false;
        }
        return true;
    }
    //description : it prevent back button
    // input : null
    // output : null
    /**
     * @brief - on Back pressed
     * @details -it prevent back button
     * @input -null
     * @output- null
     */
    @Override
    public void onBackPressed() {
        //안드로이드 백버튼 막기
        return;
    }


}