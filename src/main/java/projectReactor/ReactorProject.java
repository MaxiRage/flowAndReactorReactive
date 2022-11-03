package projectReactor;

import reactor.core.publisher.Flux;
import reactor.core.publisher.FluxSink;
import reactor.core.scheduler.Schedulers;

public class ReactorProject {
    public static void main(String[] args) throws InterruptedException {

        Flux<Object> flux = Flux.create(productionCars -> {

                    // Publish 1000 numbers
                    for (int i = 1; i <= 300; i++) {
                        System.out.println(Thread.currentThread().getName() + " --> Produced car number " + i);
                        try {
                            Thread.sleep(10);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                        productionCars.next(i);
                    }
                    // When all values or emitted, call complete
                    productionCars.complete();
                }, FluxSink.OverflowStrategy.DROP)
                .onBackpressureDrop(i -> System.out.println(Thread.currentThread().getName() + " --> You are too slow getting cars and we don't have more space for them! I'll sale your car:  = " + i));

        flux.subscribeOn(Schedulers.elastic()).publishOn(Schedulers.elastic()).subscribe(i -> {
            // Process received value.
            System.out.println(Thread.currentThread().getName() + " --> Car received = " + i);
            // 500 mills delay to simulate slow subscriber
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        });
        // Since publisher & subscriber run on different thread than main thread, keep
        // main thread active for 100 seconds.
        Thread.sleep(100_000);
    }
}