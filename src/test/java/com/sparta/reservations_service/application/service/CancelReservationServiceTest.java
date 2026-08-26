package com.sparta.reservations_service.application.service;

import com.sparta.reservations_service.application.port.in.dto.ReservationDetailResultDto;
import com.sparta.reservations_service.application.port.out.LoadReservationPort;
import com.sparta.reservations_service.application.port.out.SaveReservationPort;
import com.sparta.reservations_service.domain.exception.ReservationAccessDeniedException;
import com.sparta.reservations_service.domain.exception.ReservationAlreadyCanceledException;
import com.sparta.reservations_service.domain.exception.ReservationAuthMissingException;
import com.sparta.reservations_service.domain.exception.ReservationNotFoundException;
import com.sparta.reservations_service.domain.model.Reservation;
import com.sparta.reservations_service.domain.model.ReservationStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

class CancelReservationServiceTest {

	private static final String BUYER_UUID = "22222222-2222-4222-8222-222222222222";
	private static final String SELLER_UUID = "33333333-3333-4333-8333-333333333333";
	private static final String OTHER_UUID = "55555555-5555-4555-8555-555555555555";
	private static final String PRODUCT_POST_UUID = "11111111-1111-4111-8111-111111111111";
	private static final String CHAT_ROOM_ID = "67a1c2d3e4f5a6b7c8d9e0f1";
	private static final Instant SCHEDULED_AT = Instant.parse("2026-08-31T10:10:00Z");

	private InMemoryReservationStore store;
	private CancelReservationService service;

	@BeforeEach
	void setUp() {
		store = new InMemoryReservationStore();
		service = new CancelReservationService(store, store);
	}

	@Test
	void cancel_marksCanceledForSeller() {
		Reservation saved = store.add(confirmed());

		ReservationDetailResultDto result = service.cancel(SELLER_UUID, saved.getReservationUuid());

		assertEquals(ReservationStatus.CANCELED, result.getStatus());
		assertEquals(SELLER_UUID, result.getCanceledBy());
		assertNotNull(result.getCanceledAt());
		assertEquals(1, store.reservations.size());
		assertEquals(ReservationStatus.CANCELED, store.reservations.get(0).getStatus());
	}

	@Test
	void cancel_rejectsBuyer() {
		Reservation saved = store.add(confirmed());

		assertThrows(
				ReservationAccessDeniedException.class,
				() -> service.cancel(BUYER_UUID, saved.getReservationUuid())
		);
		assertEquals(ReservationStatus.CONFIRMED, store.reservations.get(0).getStatus());
	}

	@Test
	void cancel_rejectsThirdParty() {
		Reservation saved = store.add(confirmed());

		assertThrows(
				ReservationAccessDeniedException.class,
				() -> service.cancel(OTHER_UUID, saved.getReservationUuid())
		);
	}

	@Test
	void cancel_rejectsMissingAuth() {
		Reservation saved = store.add(confirmed());

		assertThrows(
				ReservationAuthMissingException.class,
				() -> service.cancel(null, saved.getReservationUuid())
		);
	}

	@Test
	void cancel_rejectsUnknownReservation() {
		assertThrows(
				ReservationNotFoundException.class,
				() -> service.cancel(SELLER_UUID, UUID.randomUUID().toString())
		);
	}

	@Test
	void cancel_rejectsAlreadyCanceled() {
		Reservation saved = store.add(canceled());

		assertThrows(
				ReservationAlreadyCanceledException.class,
				() -> service.cancel(SELLER_UUID, saved.getReservationUuid())
		);
	}

	private Reservation confirmed() {
		return Reservation.create(
				PRODUCT_POST_UUID,
				CHAT_ROOM_ID,
				BUYER_UUID,
				SELLER_UUID,
				SCHEDULED_AT,
				"해동병원 앞",
				null,
				35.115,
				129.042,
				SELLER_UUID
		);
	}

	private Reservation canceled() {
		Instant now = Instant.now();
		return Reservation.restore(
				null,
				UUID.randomUUID().toString(),
				PRODUCT_POST_UUID,
				CHAT_ROOM_ID,
				BUYER_UUID,
				SELLER_UUID,
				SCHEDULED_AT,
				"해동병원 앞",
				null,
				35.115,
				129.042,
				ReservationStatus.CANCELED,
				SELLER_UUID,
				SELLER_UUID,
				now,
				now,
				now
		);
	}

	private static class InMemoryReservationStore implements LoadReservationPort, SaveReservationPort {

		private final List<Reservation> reservations = new ArrayList<>();
		private long nextId = 1L;

		private Reservation add(Reservation reservation) {
			return save(reservation);
		}

		@Override
		public boolean existsConfirmedByChatRoomId(String chatRoomId) {
			return findConfirmedByChatRoomId(chatRoomId).isPresent();
		}

		@Override
		public Optional<Reservation> findConfirmedByChatRoomId(String chatRoomId) {
			return reservations.stream()
					.filter(reservation -> reservation.getChatRoomId().equals(chatRoomId)
							&& reservation.getStatus() == ReservationStatus.CONFIRMED)
					.findFirst();
		}

		@Override
		public Optional<Reservation> findByReservationUuid(String reservationUuid) {
			return reservations.stream()
					.filter(reservation -> reservation.getReservationUuid().equals(reservationUuid))
					.findFirst();
		}

		@Override
		public Reservation save(Reservation reservation) {
			long id = reservation.getReservationId() == null ? nextId++ : reservation.getReservationId();
			Reservation persisted = Reservation.restore(
					id,
					reservation.getReservationUuid(),
					reservation.getProductPostUuid(),
					reservation.getChatRoomId(),
					reservation.getBuyerUuid(),
					reservation.getSellerUuid(),
					reservation.getScheduledAt(),
					reservation.getPlaceName(),
					reservation.getAddress(),
					reservation.getLatitude(),
					reservation.getLongitude(),
					reservation.getStatus(),
					reservation.getCreatedBy(),
					reservation.getCanceledBy(),
					reservation.getCanceledAt(),
					reservation.getCreatedAt(),
					reservation.getUpdatedAt()
			);
			reservations.removeIf(existing -> existing.getReservationUuid().equals(persisted.getReservationUuid()));
			reservations.add(persisted);
			return persisted;
		}
	}
}
