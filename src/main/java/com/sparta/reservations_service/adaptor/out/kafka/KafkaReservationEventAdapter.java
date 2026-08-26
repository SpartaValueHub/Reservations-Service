package com.sparta.reservations_service.adaptor.out.kafka;

import com.sparta.reservations_service.application.port.out.PublishReservationEventPort;
import com.sparta.reservations_service.domain.model.Reservation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

// DB 커밋 성공 후에만 reservation.events 1건. 같은 트랜잭션에 send 하지 않음
@Slf4j
@Component
@RequiredArgsConstructor
public class KafkaReservationEventAdapter implements PublishReservationEventPort {

	static final String TOPIC_RESERVATION_EVENTS = "reservation.events";

	private final KafkaTemplate<String, Object> kafkaTemplate;

	@Override
	public void publishCreated(Reservation reservation) {
		ReservationEventPayload payload = ReservationEventPayload.created(reservation);
		String key = reservation.getProductPostUuid();
		if (TransactionSynchronizationManager.isSynchronizationActive()
				&& TransactionSynchronizationManager.isActualTransactionActive()) {
			TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
				@Override
				public void afterCommit() {
					send(key, payload);
				}
			});
			return;
		}
		send(key, payload);
	}

	private void send(String productPostUuid, ReservationEventPayload payload) {
		try {
			kafkaTemplate.send(TOPIC_RESERVATION_EVENTS, productPostUuid, payload)
					.whenComplete((result, exception) -> {
						if (exception != null) {
							log.error("reservation.events send failed. productPostUuid={}", productPostUuid, exception);
						}
					});
		}
		catch (RuntimeException exception) {
			log.error("reservation.events send failed. productPostUuid={}", productPostUuid, exception);
		}
	}
}
