package com.sparta.reservations_service.application.service;

import com.sparta.reservations_service.application.port.in.dto.MyReservationItemDto;
import com.sparta.reservations_service.application.port.in.dto.MyReservationListResultDto;
import com.sparta.reservations_service.application.port.out.LoadReservationPort;
import com.sparta.reservations_service.domain.exception.InvalidReservationRequestException;
import com.sparta.reservations_service.domain.exception.ReservationAuthMissingException;
import com.sparta.reservations_service.domain.model.Reservation;
import com.sparta.reservations_service.domain.model.ReservationStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class GetMyReservationsServiceTest {

	private static final String BUYER_UUID = "22222222-2222-4222-8222-222222222222";
	private static final String SELLER_UUID = "33333333-3333-4333-8333-333333333333";
	private static final String OTHER_UUID = "55555555-5555-4555-8555-555555555555";
	private static final String PRODUCT_POST_UUID = "11111111-1111-4111-8111-111111111111";
	private static final String CHAT_ROOM_ID = "67a1c2d3e4f5a6b7c8d9e0f1";
	private static final String OTHER_CHAT_ROOM_ID = "67a1c2d3e4f5a6b7c8d9e0f2";
	private static final Instant SCHEDULED_AT = Instant.parse("2026-08-31T10:10:00Z");

	private InMemoryReservationStore store;
	private GetMyReservationsService service;

	@BeforeEach
	void setUp() {
		store = new InMemoryReservationStore();
		service = new GetMyReservationsService(store);
	}

	@Test
	void get_returnsConfirmedByDefault() {
		store.add(confirmed(CHAT_ROOM_ID, BUYER_UUID, SELLER_UUID));
		store.add(canceled(OTHER_CHAT_ROOM_ID, BUYER_UUID, SELLER_UUID));

		MyReservationListResultDto result = service.get(BUYER_UUID, null);

		assertEquals(1, result.getReservations().size());
		MyReservationItemDto item = result.getReservations().get(0);
		assertEquals(CHAT_ROOM_ID, item.getChatRoomId());
		assertEquals(ReservationStatus.CONFIRMED, item.getStatus());
		assertEquals("해동병원 앞", item.getPlaceName());
		assertEquals(PRODUCT_POST_UUID, item.getProductPostUuid());
	}

	@Test
	void get_includesSellerReservations() {
		store.add(confirmed(CHAT_ROOM_ID, BUYER_UUID, SELLER_UUID));

		MyReservationListResultDto result = service.get(SELLER_UUID, null);

		assertEquals(1, result.getReservations().size());
		assertEquals(CHAT_ROOM_ID, result.getReservations().get(0).getChatRoomId());
	}

	@Test
	void get_filtersCanceled() {
		store.add(confirmed(CHAT_ROOM_ID, BUYER_UUID, SELLER_UUID));
		store.add(canceled(OTHER_CHAT_ROOM_ID, BUYER_UUID, SELLER_UUID));

		MyReservationListResultDto result = service.get(BUYER_UUID, "CANCELED");

		assertEquals(1, result.getReservations().size());
		assertEquals(ReservationStatus.CANCELED, result.getReservations().get(0).getStatus());
		assertEquals(OTHER_CHAT_ROOM_ID, result.getReservations().get(0).getChatRoomId());
	}

	@Test
	void get_returnsAllWhenStatusAll() {
		store.add(confirmed(CHAT_ROOM_ID, BUYER_UUID, SELLER_UUID));
		store.add(canceled(OTHER_CHAT_ROOM_ID, BUYER_UUID, SELLER_UUID));

		MyReservationListResultDto result = service.get(BUYER_UUID, "ALL");

		assertEquals(2, result.getReservations().size());
	}

	@Test
	void get_returnsEmptyWhenNone() {
		MyReservationListResultDto result = service.get(BUYER_UUID, null);

		assertTrue(result.getReservations().isEmpty());
	}

	@Test
	void get_excludesOtherMembers() {
		store.add(confirmed(CHAT_ROOM_ID, OTHER_UUID, SELLER_UUID));

		MyReservationListResultDto result = service.get(BUYER_UUID, null);

		assertTrue(result.getReservations().isEmpty());
	}

	@Test
	void get_sortsByUpdatedAtDesc() {
		Instant older = Instant.parse("2026-08-22T06:00:00Z");
		Instant newer = Instant.parse("2026-08-23T06:00:00Z");
		store.add(confirmedAt(CHAT_ROOM_ID, older));
		store.add(confirmedAt(OTHER_CHAT_ROOM_ID, newer));

		MyReservationListResultDto result = service.get(BUYER_UUID, null);

		assertEquals(2, result.getReservations().size());
		assertEquals(OTHER_CHAT_ROOM_ID, result.getReservations().get(0).getChatRoomId());
		assertEquals(CHAT_ROOM_ID, result.getReservations().get(1).getChatRoomId());
	}

	@Test
	void get_rejectsMissingAuth() {
		assertThrows(ReservationAuthMissingException.class, () -> service.get(null, null));
	}

	@Test
	void get_rejectsInvalidStatus() {
		assertThrows(InvalidReservationRequestException.class, () -> service.get(BUYER_UUID, "DONE"));
	}

	private Reservation confirmed(String chatRoomId, String buyerUuid, String sellerUuid) {
		return Reservation.create(
				PRODUCT_POST_UUID,
				chatRoomId,
				buyerUuid,
				sellerUuid,
				SCHEDULED_AT,
				"해동병원 앞",
				null,
				35.115,
				129.042,
				buyerUuid
		);
	}

	private Reservation confirmedAt(String chatRoomId, Instant updatedAt) {
		return Reservation.restore(
				null,
				UUID.randomUUID().toString(),
				PRODUCT_POST_UUID,
				chatRoomId,
				BUYER_UUID,
				SELLER_UUID,
				SCHEDULED_AT,
				"해동병원 앞",
				null,
				35.115,
				129.042,
				ReservationStatus.CONFIRMED,
				BUYER_UUID,
				null,
				null,
				updatedAt,
				updatedAt
		);
	}

	private Reservation canceled(String chatRoomId, String buyerUuid, String sellerUuid) {
		Instant now = Instant.now();
		return Reservation.restore(
				null,
				UUID.randomUUID().toString(),
				PRODUCT_POST_UUID,
				chatRoomId,
				buyerUuid,
				sellerUuid,
				SCHEDULED_AT,
				"해동병원 앞",
				null,
				35.115,
				129.042,
				ReservationStatus.CANCELED,
				buyerUuid,
				buyerUuid,
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
		public List<Reservation> findByPartyMemberUuid(String memberUuid) {
			return reservations.stream()
					.filter(reservation -> reservation.isParty(memberUuid))
					.sorted(Comparator.comparing(Reservation::getUpdatedAt).reversed())
					.toList();
		}

		@Override
		public List<Reservation> findByPartyMemberUuidAndStatus(String memberUuid, ReservationStatus status) {
			return reservations.stream()
					.filter(reservation -> reservation.isParty(memberUuid) && reservation.getStatus() == status)
					.sorted(Comparator.comparing(Reservation::getUpdatedAt).reversed())
					.toList();
		}

		@Override
		public Optional<Reservation> findByReservationUuid(String reservationUuid) {
			return Optional.empty();
		}
	}
}
