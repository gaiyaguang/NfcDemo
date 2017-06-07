package com.gyg.nfc;

import android.content.Intent;
import android.nfc.NfcAdapter;
import android.nfc.tech.MifareClassic;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import java.io.IOException;

/**
 * Created by gyg on 2017/6/5.
 */
public class MifareClassicActivity extends BaseNfcActivity implements View.OnClickListener{

    private EditText sectorNum;//扇区
    private EditText blockNum;//块
    private EditText writeData;//写入数据
    private EditText readData;//读出的数据

    private boolean haveMifareClissic=false;//标签是否支持MifareClassic

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_write_mifareclassic);
        sectorNum= (EditText) findViewById(R.id.sector_num);
        blockNum= (EditText) findViewById(R.id.block_num);
        writeData= (EditText) findViewById(R.id.write_data);
        readData= (EditText) findViewById(R.id.read_data);
        findViewById(R.id.write_bn).setOnClickListener(this);
        findViewById(R.id.read_bn).setOnClickListener(this);
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        mTag=intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);
        String[] techList=mTag.getTechList();
        System.out.println("支持的technology类型：");
        for (String tech:techList){
            System.out.println(tech);
            if (tech.indexOf("MifareClassic")>0){
                haveMifareClissic=true;
            }
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.write_bn://写块
                writeBlock();
                break;
            case R.id.read_bn://读块
                readBlock();
                break;
        }
    }

    //写块
    private void writeBlock(){
        if (mTag==null){
            Toast.makeText(this,"无法识别的标签！",Toast.LENGTH_SHORT);
            finish();
            return;
        }
        if (!haveMifareClissic){
            Toast.makeText(this,"不支持MifareClassic",Toast.LENGTH_SHORT);
           finish();
            return;
        }
        MifareClassic mfc=MifareClassic.get(mTag);
        try {
            mfc.connect();//打开连接
            boolean auth;
            int sector=Integer.parseInt(sectorNum.getText().toString().trim());//写入的扇区
            int block=Integer.parseInt(blockNum.getText().toString().trim());//写入的块区
            auth=mfc.authenticateSectorWithKeyA(sector,MifareClassic.KEY_DEFAULT);//keyA验证扇区
            if (auth){
                mfc.writeBlock(block,"0123456789012345".getBytes());//写入数据
                Toast.makeText(this,"写入成功!",Toast.LENGTH_SHORT);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }finally {
            try {
                mfc.close();//关闭连接
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    //读取块
    private void readBlock(){
        if (mTag==null){
            Toast.makeText(this,"无法识别的标签！",Toast.LENGTH_SHORT);
            finish();
            return;
        }
        if (!haveMifareClissic){
            Toast.makeText(this,"不支持MifareClassic",Toast.LENGTH_SHORT);
            finish();
            return;
        }
        MifareClassic mfc=MifareClassic.get(mTag);
        try {
            mfc.connect();//打开连接
            boolean auth;
            int sector=Integer.parseInt(sectorNum.getText().toString().trim());//写入的扇区
            int block=Integer.parseInt(blockNum.getText().toString().trim());//写入的块区
            auth=mfc.authenticateSectorWithKeyA(sector,MifareClassic.KEY_DEFAULT);//keyA验证扇区
            if (auth){
                readData.setText(bytesToHexString(mfc.readBlock(block)));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }finally {
            try {
                mfc.close();//关闭连接
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    //字符序列转换为16进制字符串
    private String bytesToHexString(byte[] src) {
        StringBuilder stringBuilder = new StringBuilder("0x");
        if (src == null || src.length <= 0) {
            return null;
        }
        char[] buffer = new char[2];
        for (int i = 0; i < src.length; i++) {
            buffer[0] = Character.forDigit((src[i] >>> 4) & 0x0F, 16);
            buffer[1] = Character.forDigit(src[i] & 0x0F, 16);
            System.out.println(buffer);
            stringBuilder.append(buffer);
        }
        return stringBuilder.toString();
    }
}
