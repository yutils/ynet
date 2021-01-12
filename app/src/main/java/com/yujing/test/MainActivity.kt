package com.yujing.test

import android.util.Log
import android.widget.TextView
import com.yujing.base.YBaseActivity
import com.yujing.net.Ynet
import com.yujing.net.YnetAndroid
import com.yujing.test.databinding.ActivityAllTestBinding
import com.yujing.utils.YPermissions
import com.yutils.view.utils.Create

class MainActivity : YBaseActivity<ActivityAllTestBinding>(R.layout.activity_all_test) {
    lateinit var textView1: TextView
    lateinit var textView2: TextView
    override fun init() {
        YPermissions.requestAll(this)
        binding.wll.removeAllViews()
        binding.ll.removeAllViews()
        textView1 = Create.textView(binding.ll)
        textView2 = Create.textView(binding.ll)
        Create.button(binding.wll, "测试") {
            test1()
        }
    }

    private fun test1() {
        var url = "http://192.168.1.120:10007/api/SweepCode/JjdTwoDownload"
        var p =
            "{\"DeviceNo\":\"868403023178079\",\"BatchNum\":\"54511002\",\"Command\":112,\"MsgID\":1}"
        YnetAndroid.post(url, p, object : Ynet.YnetListener {
            override fun success(value: String?) {
                Log.e("1111", value)
                textView1.text = value
            }

            override fun fail(value: String?) {
                Log.e("1111", value)
                textView1.text = value
            }
        })
    }
}
