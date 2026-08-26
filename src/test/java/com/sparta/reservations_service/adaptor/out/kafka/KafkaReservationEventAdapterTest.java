package com.sparta.reservations_service.adaptor.out.kafka;

import com.sparta.reservations_service.domain.model.Reservation;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.time.Instant;
import java.util.concurrent.CompletableFuture;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class KafkaReservationEventAdapterTest {

	@Mock
	private KafkaTemplate<String, Object> kafkaTemplate;

	@InjectMocks
	private KafkaReservationEventAdapter adapter;

	@Test
	void publishCreated_sendsOnceToReservationEventsWithProductPostUuidKey() {
		stubSend();
		Reservation reservation = sampleReservation();

		adapter.publishCreated(reservation);

		assertSentCreated(reservation);
	}

	@Test
	void publishCreated_sendsAfterCommitWhenTransactionActive() {
		stubSend();
		Reservation reservation = sampleReservation();
		TransactionSynchronizationManager.initSynchronization();
		TransactionSynchronizationManager.setActualTransactionActive(true);
		try {
			adapter.publishCreated(reservation);
			verify(kafkaTemplate, never()).send(any(), any(), any());

			for (TransactionSynchronization synchronization : TransactionSynchronizationManager.getSynchronizations()) {
				synchronization.afterCommit();
			}
			assertSentCreated(reservation);
		}
		finally {
			TransactionSynchronizationManager.clear();
		}
	}

	private void stubSend() {
		CompletableFuture<SendResult<String, Object>> future = new CompletableFuture<>();
		future.complete(null);
		when(kafkaTemplate.send(any(), any(), any())).thenReturn(future);
	}

	private void assertSentCreated(Reservation reservation) {
		ArgumentCaptor<ReservationEventPayload> payloadCaptor = ArgumentCaptor.forClass(ReservationEventPayload.class);
		verify(kafkaTemplate, times(1)).send(
				eq("reservation.events"),
				eq("11111111-1111-4111-8111-111111111111"),
				payloadCaptor.capture()
		);
		ReservationEventPayload payload = payloadCaptor.getValue();
		assertEquals("CREATED", payload.getEventType());
		assertEquals(reservation.getReservationUuid(), payload.getReservationUuid());
		assertEquals("67a1c2d3e4f5a6b7c8d9e0f1", payload.getChatRoomUuid());
		assertEquals("해동병원 앞", payload.getPlaceName());
	}

	private Reservation sampleReservation() {
		return Reservation.create(
				"11111111-1111-4111-8111-111111111111",
				"67a1c2d3e4f5a6b7c8d9e0f1",
				"22222222-2222-4222-8222-222222222222",
				"33333333-3333-4333-8333-333333333333",
				Instant.parse("2026-08-31T10:10:00Z"),
				"해동병원 앞",
				null,
				35.115,
				129.042,
				"33333333-3333-4333-8333-333333333333"
		);
	}
}
