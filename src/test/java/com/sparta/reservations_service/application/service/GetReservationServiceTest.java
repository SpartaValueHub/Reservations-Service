package com.sparta.reservations_service.application.service;

import com.sparta.reservations_service.application.port.in.dto.ReservationDetailResultDto;
import com.sparta.reservations_service.application.port.out.LoadReservationPort;
import com.sparta.reservations_service.domain.exception.InvalidReservationRequestException;
import com.sparta.reservations_service.domain.exception.ReservationAccessDeniedException;
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
import static org.junit.jupiter.api.Assertions.assertThrows;

class GetReservationServiceTest {

	private static final String BUYER_UUID = "22222222-2222-4222-8222-222222222222";
	private static final String SELLER_UUID = "33333333-3333-4333-8333-333333333333";
	private static final String OTHER_UUID = "55555555-5555-4555-8555-555555555555";
	private static final String PRODUCT_POST_UUID = "11111111-1111-4111-8111-111111111111";
	private static final String CHAT_ROOM_ID = "67a1c2d3e4f5a6b7c8d9e0f1";
	private static final Instant SCHEDULED_AT = Instant.parse("2026-08-31T10:10:00Z");
	private static final String MISSING_UUID = "99999999-9999-4999-8999-999999999999";

	private InMemoryReservationStore store;
	private GetReservationService service;

	@BeforeEach
	void setUp() {
		store = new InMemoryReservationStore();
		service = new GetReservationService(store);
	}

	@Test
	void get_returnsConfirmedReservation() {
		Reservation saved = store.add(confirmed());

		ReservationDetailResultDto result = service.get(BUYER_UUID, saved.getReservationUuid());

		assertEquals(saved.getReservationUuid(), result.getReservationId());
		assertEquals(CHAT_ROOM_ID, result.getChatRoomId());
		assertEquals(ReservationStatus.CONFIRMED, result.getStatus());
		assertEquals("해동병원 앞", result.getPlaceName());
	}

	@Test
	void get_returnsCanceledReservation() {
		Reservation saved = store.add(canceled());

		ReservationDetailResultDto result = service.get(BUYER_UUID, saved.getReservationUuid());

		assertEquals(ReservationStatus.CANCELED, result.getStatus());
		assertEquals(BUYER_UUID, result.getCanceledBy());
	}

	@Test
	void get_allowsSeller() {
		Reservation saved = store.add(confirmed());

		ReservationDetailResultDto result = service.get(SELLER_UUID, saved.getReservationUuid());

		assertEquals(saved.getReservationUuid(), result.getReservationId());
	}

	@Test
	void get_rejectsMissingAuth() {
		Reservation saved = store.add(confirmed());

		assertThrows(ReservationAuthMissingException.class, () -> service.get(null, saved.getReservationUuid()));
	}

	@Test
	void get_rejectsInvalidReservationId() {
		assertThrows(InvalidReservationRequestException.class, () -> service.get(BUYER_UUID, "not-a-uuid"));
	}

	@Test
	void get_rejectsNotFound() {
		assertThrows(ReservationNotFoundException.class, () -> service.get(BUYER_UUID, MISSING_UUID));
	}

	@Test
	void get_rejectsThirdParty() {
		Reservation saved = store.add(confirmed());

		assertThrows(ReservationAccessDeniedException.class, () -> service.get(OTHER_UUID, saved.getReservationUuid()));
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
				BUYER_UUID
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
				BUYER_UUID,
				BUYER_UUID,
				now,
				now,
				now
		);
	}

	private static class InMemoryReservationStore implements LoadReservationPort {

		private final List<Reservation> reservations = new ArrayList<>();
		private long nextId = 1L;

		private Reservation add(Reservation reservation) {
			Reservation persisted = Reservation.restore(
					nextId++,
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
			reservations.add(persisted);
			return persisted;
		}

		@Override
		public boolean existsConfirmedByChatRoomId(String chatRoomId) {
			return false;
		}

		@Override
		public Optional<Reservation> findConfirmedByChatRoomId(String chatRoomId) {
			return Optional.empty();
		}

		@Override
		public boolean existsConfirmedByProductPostUuid(String productPostUuid) {
			return false;
		}

		@Override
		public Optional<Reservation> findConfirmedByProductPostUuid(String productPostUuid) {
			return Optional.empty();
		}

		@Override
		public Optional<Reservation> findByReservationUuid(String reservationUuid) {
			return reservations.stream()
					.filter(reservation -> reservation.getReservationUuid().equals(reservationUuid))
					.findFirst();
		}

		@Override
		public List<Reservation> findByPartyMemberUuid(String memberUuid) {
			return List.of();
		}

		@Override
		public List<Reservation> findByPartyMemberUuidAndStatus(String memberUuid, ReservationStatus status) {
			return List.of();
		}
	}
}
