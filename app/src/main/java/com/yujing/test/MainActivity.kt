package com.yujing.test

import com.yujing.net.Ynet
import com.yujing.net.YnetAndroid
import kotlinx.android.synthetic.main.activity_main.*

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
        YnetAndroid.get("http://www.baidu.com", object : Ynet.YnetListener {
            override fun fail(value: String?) {
                text1.text = value
            }

            override fun success(value: String?) {
                text1.text = value
            }

        })
    }
}
