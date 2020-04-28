package com.yujing.test

import android.util.Log
import com.yujing.net.Ynet
import com.yujing.net.YnetAndroid
import kotlinx.android.synthetic.main.activity_main.*
import kotlin.math.log

class MainActivity : BaseActivity() {

    override val layoutId: Int
        get() = R.layout.activity_main

    override fun init() {
        //var a=findViewById<Button>(R.id.button1)
        button1.text = "网络请求"
        button1.setOnClickListener { net() }
        button2.setOnClickListener { show("456") }
        button3.setOnClickListener { }
        button4.setOnClickListener { }
        button5.setOnClickListener { }
        button6.setOnClickListener { }
        button7.setOnClickListener { }
        button8.setOnClickListener { }

    }

    private fun net() {
        var url = "http://192.168.1.120:10007/api/SweepCode/JjdTwoDownload"
        var p =
            "{\"DeviceNo\":\"868403023178079\",\"BatchNum\":\"54511002\",\"Command\":112,\"MsgID\":1}"
        YnetAndroid.post(url, p, object : Ynet.YnetListener {
            override fun success(value: String?) {
                Log.e("1111",value)
                text1.text = value
            }
            override fun fail(value: String?) {
                Log.e("1111",value)
                text1.text = value

            }

        })
    }
}
