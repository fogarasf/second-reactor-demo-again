package com.roi.training;

import static org.junit.jupiter.api.Assertions.assertTrue;

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
import reactor.core.Disposable;
import reactor.core.publisher.Flux;
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
                .map(i -> {System.out.println(i); return i;});

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
                    ctr.getAndIncrement(),  id );
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
                    if (e == 28) {
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
                Flux.range(1,20).delayElements(Duration.ofSeconds(3));
        Disposable cancelRef =
                numberSeq.subscribe( e -> System.out.printf("Value received %s%n",e),
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
}
