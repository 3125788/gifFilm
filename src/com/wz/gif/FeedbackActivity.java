package com.wz.gif;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;

public class FeedbackActivity extends Activity{

    private EditText feedback_edit;
    private Button btn_send;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.feedback);
        
        setUpViews();
        setUpListener();
    }

    private void setUpViews() {
        feedback_edit = (EditText)this.findViewById(R.id.feedback_edit);
        btn_send = (Button)this.findViewById(R.id.feedback_btn_send);
    }

    private void setUpListener() {
        btn_send.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                
            }
        });
    }

}
