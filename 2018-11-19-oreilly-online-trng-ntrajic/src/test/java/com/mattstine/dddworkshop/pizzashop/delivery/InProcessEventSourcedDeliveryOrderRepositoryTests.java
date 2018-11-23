package com.mattstine.dddworkshop.pizzashop.delivery;

import com.mattstine.dddworkshop.pizzashop.infrastructure.events.ports.EventLog;
import com.mattstine.dddworkshop.pizzashop.infrastructure.events.ports.Topic;
import com.mattstine.dddworkshop.pizzashop.kitchen.KitchenOrderRef;
import com.mattstine.dddworkshop.pizzashop.ordering.OnlineOrderRef;
import com.mattstine.lab.infrastructure.Lab7Tests;
import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import java.util.Collections;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

/**
 * @author Matt Stine
 */
public class InProcessEventSourcedDeliveryOrderRepositoryTests {

	private DeliveryOrderRepository repository;
	private EventLog eventLog;
	private DeliveryOrderRef ref;
	private DeliveryOrder deliveryOrder;

	@Before
	public void setUp() {
		eventLog = mock(EventLog.class);
		repository = new InProcessEventSourcedDeliveryOrderRepository(eventLog,
				new Topic("delivery_orders"));
		ref = repository.nextIdentity();
		deliveryOrder = DeliveryOrder.builder()
				.ref(ref)
				.kitchenOrderRef(new KitchenOrderRef())
				.onlineOrderRef(new OnlineOrderRef())
				.pizza(DeliveryOrder.Pizza.builder().size(DeliveryOrder.Pizza.Size.MEDIUM).build())
				.eventLog(eventLog)
				.build();
	}

	@Test
	@Category(Lab7Tests.class)
	public void provides_next_identity() {
		assertThat(ref.getReference()).isNotNull();
	}

	@Test
	@Category(Lab7Tests.class)
	public void add_fires_event() {
		repository.add(deliveryOrder);
		DeliveryOrderAddedEvent event = new DeliveryOrderAddedEvent(ref, deliveryOrder.state());
		verify(eventLog).publish(eq(new Topic("delivery_orders")), eq(event));
	}

	@Test
	@Category(Lab7Tests.class)
	public void find_by_ref_hydrates_added_order() {
		repository.add(deliveryOrder);

		when(eventLog.eventsBy(new Topic("delivery_orders")))
				.thenReturn(Collections.singletonList(new DeliveryOrderAddedEvent(ref, deliveryOrder.state())));

		assertThat(repository.findByRef(ref)).isEqualTo(deliveryOrder);
	}
}
