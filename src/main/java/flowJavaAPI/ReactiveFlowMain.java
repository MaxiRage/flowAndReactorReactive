package flowJavaAPI;

import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.SubmissionPublisher;
import java.util.concurrent.TimeUnit;
import java.util.stream.IntStream;

public class ReactiveFlowMain {
    private static final int NUMBER_OF_CARS = 20;
    private static final long MAX_SECONDS_TO_KEEP_IT_WHEN_NO_SPACE = 2;

    public static void main(String[] args) throws Exception {
        final ReactiveFlowMain app = new ReactiveFlowMain();

        System.out.println("### CASE 1: Subscribers are fast, buffer size is not so important in this case.");
        app.carFactoryDelivery(100L, 100L, 8);

        System.out.println("### CASE 2: A slow subscriber, but a good enough buffer size on the publisher's side to keep all items until they're picked up");
        app.carFactoryDelivery(1000L, 3000L, NUMBER_OF_CARS);

        System.out.println("### CASE 3: A slow subscriber, and a very limited buffer size on the " +
                "publisher's side so it's important to keep the slow subscriber under control");
        app.carFactoryDelivery(1000L, 3000L, 8);
    }

    void carFactoryDelivery(final long sleepTimeSubscriberRage,
                            final long sleepTimeSubscriberLermo,
                            final int maxStorageInParking) throws Exception {
        final SubmissionPublisher<Integer> publisher =
                new SubmissionPublisher<>(ForkJoinPool.commonPool(), maxStorageInParking);

        final CarDealershipSubscriber rage = new CarDealershipSubscriber(
                sleepTimeSubscriberRage,
                CarDealershipSubscriber.RAGE);

        final CarDealershipSubscriber lermo = new CarDealershipSubscriber(
                sleepTimeSubscriberLermo,
                CarDealershipSubscriber.LERMO);

        publisher.subscribe(rage);
        publisher.subscribe(lermo);

        System.out.println("Making 20 cars per subscriber, with parking in car factory for "
                + maxStorageInParking + ". They have " + MAX_SECONDS_TO_KEEP_IT_WHEN_NO_SPACE +
                " seconds to consume each car.");

        IntStream.rangeClosed(1, 20).forEach((number) -> {
            System.out.println(Thread.currentThread().getName() + " --> Offering car " + number + " to consumers");
            final int lag = publisher.offer(
                    number,
                    MAX_SECONDS_TO_KEEP_IT_WHEN_NO_SPACE,
                    TimeUnit.SECONDS,
                    (subscriber, msg) -> {
                        subscriber.onError(
                                new RuntimeException("Hey " + ((CarDealershipSubscriber) subscriber)
                                        .getSubscriberName() + "! You are too slow getting cars" +
                                        " and we don't have more space for them! " +
                                        "I'll sale your car: " + msg));
                        return false;
                    });
            if (lag < 0) {
                System.out.println(Thread.currentThread().getName() + " --> Selling " + -lag + " cars");
            } else {
                System.out.println(Thread.currentThread().getName() + " --> The slowest consumer has " + lag +
                        " cars in total to be picked up");
            }
        });

        // Blocks until all subscribers are done
        while (publisher.estimateMaximumLag() > 0) {
            Thread.sleep(500L);
        }

        // Closes the publisher, calling the onComplete() method on every subscriber
        publisher.close();
        // give some time to the slowest consumer to wake up and notice that it's completed
        Thread.sleep(Math.max(sleepTimeSubscriberRage, sleepTimeSubscriberLermo));
    }
}