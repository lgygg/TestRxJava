package com.lgy.testrxjava;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.Nullable;


import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;

import java.util.concurrent.TimeUnit;

import io.reactivex.BackpressureStrategy;
import io.reactivex.Flowable;
import io.reactivex.FlowableEmitter;
import io.reactivex.FlowableOnSubscribe;
import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.Observer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Action;
import io.reactivex.functions.Function;
import io.reactivex.schedulers.Schedulers;

/**
 * @author: Administrator
 * @date: 2022/9/27
 */
public class MainActivity extends Activity{
    private final String TAG = this.getClass().getName();
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        LinearLayout body = new LinearLayout(this);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        body.setLayoutParams(params);
        body.setOrientation(LinearLayout.VERTICAL);
        body.setGravity(Gravity.CENTER);
        TextView text = new TextView(this);
        body.addView(text);
        text.setText("rxjava");
        setContentView(body);

        //RxJava1.x中，Observeable用于订阅Observer和Subscriber。
       // RxJava2.x中， Observeable用于订阅Observer ，是不支持背压的，而 Flowable用于订阅Subscriber ，是支持背压(Backpressure)的。

//        Observable4().subscribe(createObserver2());
        Observable5().subscribe(createObserver1());
    }

    //=====================创建被观察者========================//
    //方法一：create
    private Observable Observable1(){
        return Observable.create(new ObservableOnSubscribe<String>() {
            @Override
            public void subscribe(ObservableEmitter<String> emitter) throws Exception {
                // 通过 ObservableEmitter类对象产生事件并通知观察者
                // ObservableEmitter：定义需要发送的事件 & 向观察者发送事件
                emitter.onNext("create-1");
                emitter.onNext("create-2");
                emitter.onNext("create-3");
                emitter.onComplete();
            }
        });
    }
    //方法二：just
    private Observable Observable2(){
        return Observable.just("just-1","just-2","just-3");
    }
    //方法三：fromArray
    private Observable Observable3(){
        String[] words = {"fromArray-1","fromArray-2","fromArray-3"};
        return Observable.fromArray(words);
    }
    private Observable Observable4(){
        int i = 0;
        Observable observable = Observable.interval(1, TimeUnit.SECONDS)
                .map(new Function<Long, String>() {
                    //注意： new Function<Long, String>第一个指定的是apply()的参数类型，第二个指定的是apply()的返回类型
                    @Override
                    public String apply(Long aLong) throws Exception {
                        return "interval-"+aLong.toString();
                    }
                });
        return observable;
    }

    private Flowable Observable5(){
        Flowable observable = Flowable.create(new FlowableOnSubscribe<String>() {
            @Override
            public void subscribe(FlowableEmitter<String> emitter) throws Exception {
                emitter.onNext("create-1");
                emitter.onNext("create-2");
                emitter.onNext("create-3");
                emitter.onComplete();
            }
        }, BackpressureStrategy.BUFFER)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread());
        return observable;
    }
    //=====================创建被观察者 end========================//

    //=====================创建观察者========================//
    private Subscriber createObserver1(){
        return new Subscriber<String>() {
            @Override
            public void onSubscribe(Subscription s) {
                s.request(10);
                Log.d(TAG,"Subscriber-onSubscribe");
            }

            @Override
            public void onNext(String o) {
                Log.d(TAG,"Subscriber-onNext: "+ o);
            }

            @Override
            public void onError(Throwable t) {
                Log.d(TAG,"Subscriber-onError"+t.getMessage());
            }

            @Override
            public void onComplete() {
                Log.d(TAG,"Subscriber-onComplete");
            }
        };
    }

    private Observer createObserver2(){
        return new Observer<String>() {
            @Override
            public void onSubscribe(Disposable d) {
                Log.d(TAG,"onNext: "+ d.isDisposed());
            }

            @Override
            public void onNext(String o) {
                Log.d(TAG,"onNext: "+ o);
            }

            @Override
            public void onError(Throwable t) {

            }

            @Override
            public void onComplete() {
                Log.d(TAG,"onComplete: ");
            }
        };
    }
    //=====================创建观察者 end========================//
}
