package com.ninos.okhttp3addcookie

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import com.ninos.okhttp3addcookie.network.Net
import rx.Subscription
import rx.android.schedulers.AndroidSchedulers
import rx.schedulers.Schedulers
import rx.subscriptions.CompositeSubscription

/**
 * Created by ninos on 18-5-17.
 */
class LoginDemoActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        loginDemo("1", "1")
    }

    //登录示例login为ApiService内接口
    //subscribe内成功返回为Observable<String>中泛型类型
    fun loginDemo(username: String, password: String) {
        var subscription: Subscription = Net.getService()
                .login(username, password)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({
                    //成功返回
                }, {
                    //错误异常
                })
        addSubscription(subscription)
    }

    //单独抽取做公共类
    companion object {
        protected var mCompositeSubscription: CompositeSubscription? = null

        private fun getInstance() {
            mCompositeSubscription = Holder.compositeSubscription
        }

        fun unSubscription() {
            if (this.mCompositeSubscription != null) {
                this.mCompositeSubscription!!.unsubscribe()
            }
        }

        fun addSubscription(s: Subscription) {
            if (this.mCompositeSubscription == null) {
                getInstance()
            }
            this.mCompositeSubscription!!.add(s)
        }
    }

    private object Holder {
        val compositeSubscription: CompositeSubscription = CompositeSubscription()
    }
}