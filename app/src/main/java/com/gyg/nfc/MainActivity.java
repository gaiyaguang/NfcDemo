package com.gyg.nfc;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

public class MainActivity extends BaseNfcActivity implements View.OnClickListener{

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        findViewById(R.id.button).setOnClickListener(this);
        findViewById(R.id.button2).setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.button://读写NDEF格式
                startActivity(new Intent(this,NdefActivity.class));
                break;
            case R.id.button2://读写MifareClassic格式
                startActivity(new Intent(this,MifareClassicActivity.class));
                break;
        }
    }
}
