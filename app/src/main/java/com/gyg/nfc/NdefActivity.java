package com.gyg.nfc;

import android.nfc.FormatException;
import android.nfc.NdefMessage;
import android.nfc.NdefRecord;
import android.nfc.tech.Ndef;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import java.io.IOException;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.Locale;

/**
 * Created by gyg on 2017/6/5.
 */
public class NdefActivity extends BaseNfcActivity implements View.OnClickListener{

    EditText writeEdt,readEdt;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ndef);
        writeEdt= (EditText) findViewById(R.id.write_data);
        readEdt= (EditText) findViewById(R.id.read_data);
        findViewById(R.id.write_bn).setOnClickListener(this);
        findViewById(R.id.read_bn).setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.write_bn://写数据
                writeNdef();
                break;
            case R.id.read_bn://读数据
                readNdef();
                break;
        }
    }

    //往Ndef标签中写数据
    private void writeNdef(){
        if (mTag==null){
            Toast.makeText(this,"不能识别的标签类型！",Toast.LENGTH_SHORT);
            finish();
            return;
        }
        Ndef ndef=Ndef.get(mTag);//获取ndef对象
        if (!ndef.isWritable()){
            Toast.makeText(this,"该标签不能写入数据!",Toast.LENGTH_SHORT);
            return;
        }
        NdefRecord ndefRecord=createTextRecord(writeEdt.getText().toString());//创建一个NdefRecord对象
        NdefMessage ndefMessage=new NdefMessage(new NdefRecord[]{ndefRecord});//根据NdefRecord数组，创建一个NdefMessage对象
        int size=ndefMessage.getByteArrayLength();
        if (ndef.getMaxSize()<size){
            Toast.makeText(this,"标签容量不足！",Toast.LENGTH_SHORT);
            return;
        }
        try {
            ndef.connect();//连接
            ndef.writeNdefMessage(ndefMessage);
            Toast.makeText(this,"数据写入成功！",Toast.LENGTH_SHORT);
        } catch (IOException e) {
            e.printStackTrace();
        } catch (FormatException e) {
            e.printStackTrace();
        }finally {
            try {
                ndef.close();//关闭连接
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 创建NDEF文本数据
     * @param text
     * @return
     */
    public static NdefRecord createTextRecord(String text) {
        byte[] langBytes = Locale.CHINA.getLanguage().getBytes(Charset.forName("US-ASCII"));
        Charset utfEncoding = Charset.forName("UTF-8");
        //将文本转换为UTF-8格式
        byte[] textBytes = text.getBytes(utfEncoding);
        //设置状态字节编码最高位数为0
        int utfBit = 0;
        //定义状态字节
        char status = (char) (utfBit + langBytes.length);
        byte[] data = new byte[1 + langBytes.length + textBytes.length];
        //设置第一个状态字节，先将状态码转换成字节
        data[0] = (byte) status;
        //设置语言编码，使用数组拷贝方法，从0开始拷贝到data中，拷贝到data的1到langBytes.length的位置
        System.arraycopy(langBytes, 0, data, 1, langBytes.length);
        //设置文本字节，使用数组拷贝方法，从0开始拷贝到data中，拷贝到data的1 + langBytes.length
        //到textBytes.length的位置
        System.arraycopy(textBytes, 0, data, 1 + langBytes.length, textBytes.length);
        //通过字节传入NdefRecord对象
        //NdefRecord.RTD_TEXT：传入类型 读写
        NdefRecord ndefRecord = new NdefRecord(NdefRecord.TNF_WELL_KNOWN,
                NdefRecord.RTD_TEXT, new byte[0], data);
        return ndefRecord;
    }

    //读取Ndef标签中数据
    private void readNdef(){
        if (mTag==null){
            Toast.makeText(this,"不能识别的标签类型！",Toast.LENGTH_SHORT);
            finish();
            return;
        }
        Ndef ndef=Ndef.get(mTag);//获取ndef对象
        try {
            ndef.connect();
            NdefMessage ndefMessage=ndef.getNdefMessage();
            if (ndefMessage!=null)
                readEdt.setText(parseTextRecord(ndefMessage.getRecords()[0]));
            Toast.makeText(this,"数据读取成功！",Toast.LENGTH_SHORT);
        } catch (IOException e) {
            e.printStackTrace();
        } catch (FormatException e) {
            e.printStackTrace();
        }finally {
            try {
                ndef.close();//关闭链接
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 解析NDEF文本数据，从第三个字节开始，后面的文本数据
     * @param ndefRecord
     * @return
     */
    public static String parseTextRecord(NdefRecord ndefRecord) {
        /**
         * 判断数据是否为NDEF格式
         */
        //判断TNF
        if (ndefRecord.getTnf() != NdefRecord.TNF_WELL_KNOWN) {
            return null;
        }
        //判断可变的长度的类型
        if (!Arrays.equals(ndefRecord.getType(), NdefRecord.RTD_TEXT)) {
            return null;
        }
        try {
            //获得字节数组，然后进行分析
            byte[] payload = ndefRecord.getPayload();
            //下面开始NDEF文本数据第一个字节，状态字节
            //判断文本是基于UTF-8还是UTF-16的，取第一个字节"位与"上16进制的80，16进制的80也就是最高位是1，
            //其他位都是0，所以进行"位与"运算后就会保留最高位
            String textEncoding = ((payload[0] & 0x80) == 0) ? "UTF-8" : "UTF-16";
            //3f最高两位是0，第六位是1，所以进行"位与"运算后获得第六位
            int languageCodeLength = payload[0] & 0x3f;
            //下面开始NDEF文本数据第二个字节，语言编码
            //获得语言编码
            String languageCode = new String(payload, 1, languageCodeLength, "US-ASCII");
            //下面开始NDEF文本数据后面的字节，解析出文本
            String textRecord = new String(payload, languageCodeLength + 1,
                    payload.length - languageCodeLength - 1, textEncoding);
            return textRecord;
        } catch (Exception e) {
            throw new IllegalArgumentException();
        }
    }
}
