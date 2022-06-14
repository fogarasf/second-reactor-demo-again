package com.roi.training;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.time.Duration;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.BooleanSupplier;
import java.util.stream.Stream;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.reactivestreams.Publisher;
import reactor.core.Disposable;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Scheduler;
import reactor.core.scheduler.Schedulers;
import reactor.test.StepVerifier;

/**
 * Unit test for simple App.
 */
@DisplayName("Write tests for App")
public class AppTest {

    private List<String> dataValues = Arrays.asList("The", "quick", "brown", "fox", "jumps", "over", "the", "lazy", "dog");
    private Flux dataSrc;
    private int ctr;

    @BeforeEach
    public void init() {
        this.dataSrc = Flux.fromIterable(this.dataValues);
        this.ctr = this.dataValues.size();
    }


    /**
     * Rigorous Test :-)
     */
    @Test
    @DisplayName("Should be true")
    public void shouldAnswerWithTrue() {
        assertTrue(true);

        // No printing as there is no subscriber
        Flux<Integer> publisher = Flux.range(1, 100)
                .map(i -> {
                    System.out.println(i);
                    return i;
                });

        publisher.subscribe(System.out::println);
    }

    @Test
    public void publishWithStrem() {
        Flux<UUID> pub = Flux.fromStream(Stream.generate(UUID::randomUUID).limit(100));

        pub.subscribe(System.out::println);
    }

    @Test
    public void publishMultiThreadDemo() {
        AtomicInteger ctr = new AtomicInteger();
        Flux<UUID> pub = Flux.fromStream(Stream.generate(UUID::randomUUID).limit(100));

        pub.subscribe(id -> {
            System.out.printf("%s has value %s%n",
                    ctr.getAndIncrement(), id);
        });
    }

    @Test
    public void verifyPublishSequence() {
        StepVerifier.create(this.dataSrc)
                .expectNextSequence(this.dataValues)
//                .expectNextCount(this.ctr)
                .verifyComplete();
    }

    @Test
    public void publishWithError() {
        Flux<Integer> numberSeq = Flux.range(1, 20)
                .map(e -> {
                    if (e == 8) {
                        throw new RuntimeException("error on the eights");
                    }
                    return e;
                });
        numberSeq.subscribe(
                e -> System.out.printf("Value received %s%n", e),
                error -> System.err.println("Error Published:: " + error),
                () -> {
                    System.out.println("Stream completed");
                });
    }

    @Test
    public void testDisposable() {
        Flux<Integer> numberSeq =
                Flux.range(1, 20).delayElements(Duration.ofSeconds(3));
        Disposable cancelRef =
                numberSeq.subscribe(e -> System.out.printf("Value received %s%n", e),
                        error -> System.err.println("Error Published:: " + error),
                        () -> System.out.println("Complete event published"));
        Runnable runnableTask = () -> {
            try {
                TimeUnit.SECONDS.sleep(12);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            System.out.println("Canceling subscription");
            cancelRef.dispose();
        };
        runnableTask.run();
    }

    @Test
    public void basicTest() {
        StepVerifier.create(this.dataSrc)
                .expectNext(this.dataValues.get(0))
                .expectNext(this.dataValues.get(1))
                .expectNext(this.dataValues.get(2))
                .expectNext(this.dataValues.get(3))
                .expectNext(this.dataValues.get(4), this.dataValues.get(5))
                .expectNext(this.dataValues.get(6))
                .expectNext(this.dataValues.get(7))
                .expectNext(this.dataValues.get(8))
                .verifyComplete();
    }

    @Test
    public void testCount() {
        StepVerifier.create(this.dataSrc)
                .expectNextCount(this.ctr)
                .verifyComplete();
    }

    @Test
    public void publishingDirectly() {
        Flux<String> strPublisher = Flux.generate(
                AtomicInteger::new,
                (mutableInt, publishIt) -> {
                    publishIt.next(String.format(" on next value:: %s",
                            mutableInt.getAndIncrement()));
                    if (mutableInt.get() == 17) {
                        publishIt.complete();
                    }
                    return mutableInt;
                });
        strPublisher.subscribe(
                s -> System.out.printf("Subscriber received:: %s%n", s),
                e -> System.out.println("Error published:: " + e),
                () -> System.out.println("Complete notification sent!"));
    }

    @Test
    public void testScheduler() {
        Scheduler reactScheduler = Schedulers.newParallel("pub-parallel", 4);
        final Flux<String> phrasePublish =
                Flux.range(1, 20)
                        .map(i -> 42 + i)
                        .publishOn(reactScheduler)
                        .map(m -> {
                            var v = Thread.currentThread().getName();
                            return String.format("%s value produced::%s", v, m);
                        });
        Runnable r0 = () -> phrasePublish.subscribe(
                n -> {
                    System.out.printf("subscriber recvd:: %s%n", n);
                }
        );

        Runnable r1 = () -> phrasePublish.subscribe(
                n -> {
                    System.out.printf("subscriber recvd:: %s%n", n);
                }
        );

        Runnable r2 = () -> phrasePublish.subscribe(
                n -> {
                    System.out.printf("subscriber recvd:: %s%n", n);
                }
        );

        Runnable r3 = () -> phrasePublish.subscribe(
                n -> {
                    System.out.printf("subscriber recvd:: %s%n", n);
                }
        );

        r0.run();
        r1.run();
        r2.run();
        r3.run();

    }

    @Test
    public void publishElastic() {
        Flux<UUID> pub = Flux.fromStream(Stream.generate(UUID::randomUUID).limit(100))
                .publishOn(Schedulers.boundedElastic())
                .map(u -> {
                    System.out.println("Test");
                    return u;
                });

        pub.subscribe(System.out::println);

    }

    @Test
    public void handleError() {
        Flux<String> strSeq = Flux.just(1, 3, 5, 8, 0)
                .map(e -> {
                    if (e == 5) {
                        throw new IllegalArgumentException("faux receipt");
                    }
                    int v = 100;
                    return String.format("reported percentage::%s", v / e);
                })
                .onErrorReturn(e -> e.getMessage().contains("faux"),
                        "alternate static message 8-)")
                .onErrorReturn(ArithmeticException.class, "Dividing by zero is bad")
                .onErrorReturn("static fallback return value");
        strSeq.subscribe(System.out::println);
    }

    @Test
    public void handleError2() {
        // using values as input representatives
        Flux<Integer> strSeq = Flux.just(12345,12346,12347,12348)
                .flatMap(sku -> callSkuService(sku))
                .onErrorResume(e ->{
                    System.out.println(e); return Mono.just(-2); })
                .onErrorResume(IllegalArgumentException.class,
                        e ->{
                            System.out.println(e);return Mono.just(-1);});
// source error → e , could be used for logical choice of alternate stream
        strSeq.subscribe(System.out::println);
    }

    private Publisher<Integer> callSkuService(Integer sku) {
        if (sku>12347) {
            throw new NullPointerException("ABC");
        }
        return Mono.just(1);
    }

    @Test
    public void handleError3() {
        Flux<Integer> strSeq =Flux.just(12345,12346,12347,12348)
                .flatMap(sku->callSkuService(sku))
                .onErrorMap(e -> new IllegalArgumentException("failed to find",e));
// overloaded versions support same patterns
        strSeq.subscribe(e -> System.out.printf("received::%s%n",e),
                err -> System.out.println(err.getMessage()));
        strSeq.subscribe(System.out::println);
    }
}
