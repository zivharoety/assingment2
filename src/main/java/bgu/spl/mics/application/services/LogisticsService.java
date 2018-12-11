package bgu.spl.mics.application.services;

import bgu.spl.mics.Future;
import bgu.spl.mics.MicroService;
import bgu.spl.mics.application.messages.FetchVehicle;
import bgu.spl.mics.application.messages.ReleaseVehicle;
import bgu.spl.mics.application.messages.Tick;
import bgu.spl.mics.application.passiveObjects.*;
import bgu.spl.mics.application.messages.DeliveryEvent;

import java.util.concurrent.CountDownLatch;

/**
 * Logistic service in charge of delivering books that have been purchased to customers.
 * Handles {@link DeliveryEvent}.
 * This class may not hold references for objects which it is not responsible for:
 * {@link ResourcesHolder}, {@link MoneyRegister}, {@link Inventory}.
 * 
 * You can add private fields and public methods to this class.
 * You MAY change constructor signatures and even add new public constructors.
 */
public class LogisticsService extends MicroService {
			private Future<DeliveryVehicle> future;
			private CountDownLatch countDown;
	public LogisticsService(String name, CountDownLatch countD) {
		super(name);
		countDown = countD;
		// TODO Implement this
	}

	@Override
	protected void initialize() {
		subscribeEvent(DeliveryEvent.class, (DeliveryEvent message) -> {
			FetchVehicle toSend = new FetchVehicle(message.getCustomer().getDistance());
			Future<DeliveryVehicle> future = sendEvent(toSend);
			future.get().deliver(message.getCustomer().getAddress(),message.getCustomer().getDistance());
			ReleaseVehicle toRelease = new ReleaseVehicle(future.get());
			sendEvent(toRelease);
		});
		subscribeBroadcast(Tick.class , (Tick message)->{
			if(message.getDuration()==message.getTick()) {
				bus.unregister(this);
				terminate();
				System.out.println(getName()+ "is terminating");
			}
		});
		countDown.countDown();

	}

}
