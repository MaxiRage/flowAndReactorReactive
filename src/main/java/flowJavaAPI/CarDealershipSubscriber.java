package flowJavaAPI;

import java.util.concurrent.Flow;
import java.util.stream.IntStream;

public class CarDealershipSubscriber implements Flow.Subscriber<Integer> {
    public static final String RAGE = "Rage";
    public static final String LERMO = "Lermo";
    private final long sleepTime;
    private final String subscriberName;
    private Flow.Subscription subscription;
    private int nextCarExpected;
    private int totalCars;

    CarDealershipSubscriber(final long sleepTime, final String subscriberName) {
        this.sleepTime = sleepTime;
        this.subscriberName = subscriberName;
        this.nextCarExpected = 1;
        this.totalCars = 0;
    }

    @Override
    public void onSubscribe(final Flow.Subscription subscription) {
        this.subscription = subscription;
        subscription.request(1);
    }

    @Override
    public void onNext(final Integer carNumber) {
        if (carNumber != nextCarExpected) {
            IntStream.range(nextCarExpected, carNumber).forEach(
                    (msgNumber) ->
                            System.out.println(Thread.currentThread().getName() + " --> [" + subscriberName + "] : " + "Oh no! I missed the car " + msgNumber)
            );
            nextCarExpected = carNumber;
        }
        System.out.println(Thread.currentThread().getName() + " --> [" + subscriberName + "] : " + "Great! I got a new car: " + carNumber);
        takeSomeRest();
        nextCarExpected++;
        totalCars++;

        System.out.println(Thread.currentThread().getName() + " --> [" + subscriberName + "] : " + "I'll get another car now, next one should be: " +
                nextCarExpected);
        subscription.request(1);
    }

    @Override
    public void onError(final Throwable throwable) {
        System.out.println(Thread.currentThread().getName() + " --> [" + subscriberName + "] : " + "Oops I got an error from the car factory: " + throwable.getMessage());
    }

    @Override
    public void onComplete() {
        System.out.println(Thread.currentThread().getName() + " --> [" + subscriberName + "] : " + "Finally! I completed the subscription, I got in total " +
                totalCars + " cars.");
    }

    public String getSubscriberName() {
        return subscriberName;
    }

    private void takeSomeRest() {
        try {
            Thread.sleep(sleepTime);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }
}