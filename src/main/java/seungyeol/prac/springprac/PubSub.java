package seungyeol.prac.springprac;

import lombok.extern.slf4j.Slf4j;
import org.reactivestreams.Publisher;
import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;

import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.stream.Stream;

import static java.util.stream.Collectors.*;

@Slf4j
public class PubSub {

    public static void main(String[] args) {
        Publisher<Integer> pub = iterPub(Stream.iterate(1, i -> i + 1).limit(10).collect(toList()));
        //Publisher<String> mapPub = mapPub(pub, s -> "[" + s + "]");
       // Publisher<Integer> sumPub  = sumPub(mapPub);
       Publisher<String> reducePub = reducePub(pub,"", (a,b) -> a+ "-" + b);
       reducePub.subscribe(LogSub());
    }

    // 1,2,3,4,5
    // 0 -> (0,1) -> 0 + 1 =1
    // 1 -> (1,2) -> 1 + 2 = 3


    private static <T,R> Publisher<String> reducePub(Publisher<Integer> pub, String init, BiFunction<String, Integer, String> bf) {
        return new Publisher<String>() {
            @Override
            public void subscribe(Subscriber<? super String> sub) {

                pub.subscribe(new DelegateSub<Integer,String>(sub) {
                    String result = init;

                    @Override
                    public void onNext(Integer t) {
                        result = bf.apply(result,t);
                    }

                    @Override
                    public void onComplete() {
                        sub.onNext(result);
                        sub.onComplete();
                    }
                });
            }
        };
    }


    // T -> R
    private static <T,R> Publisher<R> mapPub(Publisher<T> pub, Function<T, R> f) {
        return new Publisher<R>() {
            @Override
            public void subscribe(Subscriber<? super R> sub) {
                pub.subscribe(new DelegateSub<T,R>(sub) {
                    @Override
                    public void onNext(T t) {
                        sub.onNext(f.apply(t));
                    }
                });
            }
        };
    }

    private static <T> Subscriber<T> LogSub() {
        return new Subscriber<T>() {
            @Override
            public void onSubscribe(Subscription s) {
                log.info("Start");
                s.request(Long.MAX_VALUE);
            }

            @Override
            public void onNext(T integer) {
                log.info("on Next:" + integer);
            }

            @Override
            public void onError(Throwable t) {
                log.info("Error");
            }

            @Override
            public void onComplete() {
                log.info("complete");
            }
        };
    }

    private static Publisher<Integer> iterPub(final List<Integer> iter) {
        Publisher<Integer> pub = new Publisher<>() {

            @Override
            public void subscribe(Subscriber<? super Integer> sub) {
                sub.onSubscribe(new Subscription() {
                    @Override
                    public void request(long n) {
                        iter.forEach(a -> sub.onNext(a));
                        sub.onComplete();
                    }

                    @Override
                    public void cancel() {

                    }
                });
            }
        };
        return pub;
    }
}

