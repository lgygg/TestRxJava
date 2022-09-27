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

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import io.reactivex.BackpressureStrategy;
import io.reactivex.Flowable;
import io.reactivex.FlowableEmitter;
import io.reactivex.FlowableOnSubscribe;
import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.ObservableSource;
import io.reactivex.Observer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Action;
import io.reactivex.functions.Consumer;
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
//        Observable5().subscribe(createObserver1());
//        testSerial().subscribe(createObserver2());
        testSerialWithData();
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
                Log.e(TAG,"Subscriber-onSubscribe");
            }

            @Override
            public void onNext(String o) {
                Log.e(TAG,"Subscriber-onNext: "+ o);
            }

            @Override
            public void onError(Throwable t) {
                Log.e(TAG,"Subscriber-onError"+t.getMessage());
            }

            @Override
            public void onComplete() {
                Log.e(TAG,"Subscriber-onComplete");
            }
        };
    }

    private Observer createObserver2(){
        return new Observer<String>() {
            @Override
            public void onSubscribe(Disposable d) {
                Log.e(TAG,"onNext: "+ d.isDisposed());
            }

            @Override
            public void onNext(String o) {
                Log.e(TAG,"onNext: "+ o);
            }

            @Override
            public void onError(Throwable t) {
                Log.e(TAG,"onError: "+ t.getMessage());
            }

            @Override
            public void onComplete() {
                Log.e(TAG,"onComplete: ");
            }
        };
    }
    //=====================创建观察者 end========================//

    //=====================测试串行========================//
    private Observable testSerial(){
        return Observable.concat(thread1(),thread2(),thread3())
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread());
    }

    private Observable thread1(){
        return Observable.create(new ObservableOnSubscribe<String>() {
            @Override
            public void subscribe(ObservableEmitter<String> e) throws Exception {
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        for (int i = 0;i<5;i++) {
                            Log.e(TAG,"Thread1:"+i);
                            e.onNext("Thread1:"+i);
                        }
                        e.onComplete();
                    }
                }).start();
            }
        });

    }
    private Observable thread2(){
        return Observable.create(new ObservableOnSubscribe<String>() {
            @Override
            public void subscribe(ObservableEmitter<String> e) throws Exception {
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        for (int i = 0;i<5;i++) {
                            Log.e(TAG,"Thread2:"+i);
                            e.onNext("Thread2:"+i);
                        }
                        e.onComplete();
                    }
                }).start();
            }
        });

    }
    private Observable thread3(){
        return Observable.create(new ObservableOnSubscribe<String>() {
            @Override
            public void subscribe(ObservableEmitter<String> e) throws Exception {
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        for (int i = 0;i<5;i++) {
                            Log.e(TAG," Thread3:"+i);
                            e.onNext("Thread3:"+i);
                        }
                        e.onComplete();
                    }
                }).start();
            }
        });

    }
    //=====================测试串行 end========================//

    //=====================测试串行传值========================//
    private  void testSerialWithData(){
        Observable.create(new ObservableOnSubscribe<Map<String,String>>() {
                    @Override
                    public void subscribe(ObservableEmitter<Map<String,String>> e) throws Exception {
                        //onNext里的参数要和ObservableEmitter<Map<String,String>>以及ObservableOnSubscribe<Map<String,String>>匹配上，否则会报错
                        Map<String,String> map = new HashMap<>();
                        map.put("request1",request1());
                        Log.e(TAG,"request1:"+map.toString());
                        e.onNext(map);
                        e.onComplete();
                    }
                })
                .map(new Function<Map<String,String>, Map<String,String>>() {
                    @Override
                    public Map<String,String> apply(Map<String,String> s) throws Exception {
                        Log.e(TAG,"request2:"+s.toString());
                        s.put("request2",request2());
                        return s;
                    }
                })
                .map(new Function<Map<String,String>, Map<String,String>>() {
                    @Override
                    public Map<String,String> apply(Map<String,String> s) throws Exception {
                        Log.e(TAG,"request3:"+s.toString());
                        s.put("request3",request3());
                        return s;
                    }
                })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<Map<String, String>>() {
                    @Override
                    public void onSubscribe(Disposable d) {

                    }

                    @Override
                    public void onNext(Map<String, String> stringStringMap) {
                        Log.e(TAG,"request:"+stringStringMap.toString());
                    }

                    @Override
                    public void onError(Throwable e) {
                        Log.e(TAG,"request-onError:"+e.getMessage());
                    }

                    @Override
                    public void onComplete() {
                        Log.e(TAG,"request-onComplete:");
                    }
                });
    }
    private String request1(){
        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return "request1";
    }
    private String request2(){
        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return "request2";
    }
    private String request3(){
        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return "request3";
    }
    //=====================测试串行传值 end========================//
}
