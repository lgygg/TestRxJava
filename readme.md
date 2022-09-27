# 1.背景
RxJava 是Reactive Extensions的 Java VM 实现：一个使用可观察序列组成异步和基于事件的程序的库。

它扩展了观察者模式以支持数据/事件序列，并添加了允许您以声明方式组合序列的运算符，同时抽象出对低级线程、同步、线程安全和并发数据结构等事物的关注。
# 2.作用
基本作用就是使用观察者模式实现异步操作。

# 3.原理

![原理](./pic/pic1.png)

# 3.使用

基本操作就是：

1）创建被观察者。

2）创建观察者。

3）使用subscribe把观察者和被观察者关联。

![使用](./pic/pic2.png)

### （1）依赖RxJava

```
//RxAndroid中包含RxJava的内容，只引入RxAndroid还是会报错
dependencies {
    ......
    compile 'io.reactivex.rxjava2:rxjava:2.1.3'
    compile 'io.reactivex.rxjava2:rxandroid:2.0.1'
```
### （2）创建被观察者

RxJava 提供了很多方法，创建被观察者对象Observable

![创建被观察者](./pic/pic3.png)

RxJava1.x中，Observeable用于订阅Observer和Subscriber。

RxJava2.x中， Observeable用于订阅Observer ，是不支持背压的，而 Flowable用于订阅Subscriber ，是支持背压(Backpressure)的。

如下例子：

```
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
```


### （3）创建观察者

RxJava1.x中，Observeable用于订阅Observer和Subscriber。

RxJava2.x中， Observeable用于订阅Observer ，是不支持背压的，而 Flowable用于订阅Subscriber ，是支持背压(Backpressure)的。


例子如下：

```
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
```

onCompleted()  —— 表示事件队列完结的回调。RxJava规定，当不会再有新的 onNext() 发出
                                   时，需要触发onCompleted() 方法作为标志。
								   
onError()           —— 表示事件队列异常。事件处理过程中如果发生异常，就被触发onError()，同
                                   时事件队列自动终止，不允许再有事件发出。
								   
注意： 在一个正确运行的事件序列中， onCompleted() 和  onError() 有且只有一个会触发，两者是互斥的，并且是事件序列的最后一个事件

### （4）使用subscribe把观察者和被观察者关联

RxJava1.x中，Observeable用于订阅Observer和Subscriber。

RxJava2.x中， Observeable用于订阅Observer ，是不支持背压的，而 Flowable用于订阅Subscriber ，是支持背压(Backpressure)的。

```
 //Observable4返回的是Observeable，createObserver2返回的是Observer
 Observable4().subscribe(createObserver2());
 
 //Observable5返回的是Flowable，createObserver1返回的是Subscriber
Observable5().subscribe(createObserver1());
```