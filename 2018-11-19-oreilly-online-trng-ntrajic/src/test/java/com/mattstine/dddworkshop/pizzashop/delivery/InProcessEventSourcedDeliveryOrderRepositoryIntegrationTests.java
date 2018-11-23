package com.mattstine.dddworkshop.pizzashop.delivery;

import com.mattstine.dddworkshop.pizzashop.infrastructure.events.adapters.InProcessEventLog;
import com.mattstine.dddworkshop.pizzashop.infrastructure.events.ports.Topic;
import com.mattstine.dddworkshop.pizzashop.kitchen.KitchenOrderRef;
import com.mattstine.dddworkshop.pizzashop.ordering.OnlineOrderRef;
import com.mattstine.lab.infrastructure.Lab7Tests;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author Matt Stine
 */
public class InProcessEventSourcedDeliveryOrderRepositoryIntegrationTests {

	private DeliveryOrderRepository repository;
	private InProcessEventLog eventLog;
	private DeliveryOrder deliveryOrder;
	private KitchenOrderRef kitchenOrderRef;

	@Before
	public void setUp() {
		eventLog = InProcessEventLog.instance();
		repository = new InProcessEventSourcedDeliveryOrderRepository(eventLog,
				new Topic("delivery_orders"));
		DeliveryOrderRef ref = repository.nextIdentity();
		kitchenOrderRef = new KitchenOrderRef();
		deliveryOrder = DeliveryOrder.builder()
				.ref(ref)
				.kitchenOrderRef(kitchenOrderRef)
				.onlineOrderRef(new OnlineOrderRef())
				.pizza(DeliveryOrder.Pizza.builder().size(DeliveryOrder.Pizza.Size.MEDIUM).build())
				.eventLog(eventLog)
				.build();
	}

	@After
	public void tearDown() {
		this.eventLog.purgeSubscribers();
	}

	@Test
	@Category(Lab7Tests.class)
	public void find_by_kitchenOrderRef_hydrates_deliveryOrder() {
		repository.add(deliveryOrder);

		assertThat(repository.findByKitchenOrderRef(kitchenOrderRef)).isEqualTo(deliveryOrder);
	}
}
