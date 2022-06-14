package com.roi.training;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.BooleanSupplier;
import java.util.stream.Stream;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
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
        Flux<Integer> numberSeq = Flux.range(1,20)
                .map( e -> {
                    if (e== 8) {throw new RuntimeException("error on the eights");}
                    return e;});
        numberSeq.subscribe(
                e -> System.out.printf("Value received %s%n",e),
                error -> System.err.println("Error Published:: " + error),
                () -> {
                    System.out.println("Stream completed");
                });
    }
}
