package com.sparta.reservations_service.application.service;

import com.sparta.reservations_service.application.port.in.dto.CreateReservationCommandDto;
import com.sparta.reservations_service.application.port.in.dto.ReservationDetailResultDto;
import com.sparta.reservations_service.application.port.out.LoadReservationPort;
import com.sparta.reservations_service.application.port.out.SaveReservationPort;
import com.sparta.reservations_service.domain.exception.CannotReserveWithSelfException;
import com.sparta.reservations_service.domain.exception.InvalidReservationRequestException;
import com.sparta.reservations_service.domain.exception.ReservationAccessDeniedException;
import com.sparta.reservations_service.domain.exception.ReservationAlreadyConfirmedException;
import com.sparta.reservations_service.domain.exception.ReservationAuthMissingException;
import com.sparta.reservations_service.domain.model.Reservation;
import com.sparta.reservations_service.domain.model.ReservationStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

class CreateReservationServiceTest {

	private static final String BUYER_UUID = "22222222-2222-4222-8222-222222222222";
	private static final String SELLER_UUID = "33333333-3333-4333-8333-333333333333";
	private static final String OTHER_UUID = "55555555-5555-4555-8555-555555555555";
	private static final String PRODUCT_POST_UUID = "11111111-1111-4111-8111-111111111111";
	private static final String CHAT_ROOM_ID = "67a1c2d3e4f5a6b7c8d9e0f1";
	private static final Instant SCHEDULED_AT = Instant.parse("2026-08-31T10:10:00Z");

	private InMemoryReservationStore store;
	private CreateReservationService service;

	@BeforeEach
	void setUp() {
		store = new InMemoryReservationStore();
		service = new CreateReservationService(store, store);
	}

	@Test
	void create_savesConfirmedReservation() {
		ReservationDetailResultDto result = service.create(command(BUYER_UUID));

		assertEquals(ReservationStatus.CONFIRMED, result.getStatus());
		assertEquals(CHAT_ROOM_ID, result.getChatRoomId());
		assertEquals(PRODUCT_POST_UUID, result.getProductPostUuid());
		assertEquals(BUYER_UUID, result.getBuyerUuid());
		assertEquals(SELLER_UUID, result.getSellerUuid());
		assertEquals(SCHEDULED_AT, result.getScheduledAt());
		assertEquals("해동병원 앞", result.getPlaceName());
		assertEquals(35.115, result.getLatitude());
		assertEquals(129.042, result.getLongitude());
		assertEquals(BUYER_UUID, result.getCreatedBy());
		assertNull(result.getCanceledBy());
		assertNull(result.getCanceledAt());
		assertNotNull(result.getReservationId());
		assertEquals(1, store.reservations.size());
	}

	@Test
	void create_allowsSeller() {
		ReservationDetailResultDto result = service.create(command(SELLER_UUID));

		assertEquals(SELLER_UUID, result.getCreatedBy());
		assertEquals(ReservationStatus.CONFIRMED, result.getStatus());
	}

	@Test
	void create_rejectsMissingAuth() {
		assertThrows(ReservationAuthMissingException.class, () -> service.create(command(null)));
	}

	@Test
	void create_rejectsWhenBuyerEqualsSeller() {
		CreateReservationCommandDto command = command(BUYER_UUID).toBuilder()
				.sellerUuid(BUYER_UUID)
				.build();

		assertThrows(CannotReserveWithSelfException.class, () -> service.create(command));
		assertEquals(0, store.reservations.size());
	}

	@Test
	void create_rejectsThirdParty() {
		assertThrows(ReservationAccessDeniedException.class, () -> service.create(command(OTHER_UUID)));
		assertEquals(0, store.reservations.size());
	}

	@Test
	void create_rejectsWhenConfirmedAlreadyExists() {
		service.create(command(BUYER_UUID));

		assertThrows(ReservationAlreadyConfirmedException.class, () -> service.create(command(SELLER_UUID)));
		assertEquals(1, store.reservations.size());
	}

	@Test
	void create_rejectsPartialCoordinates() {
		CreateReservationCommandDto command = command(BUYER_UUID).toBuilder()
				.longitude(null)
				.build();

		assertThrows(InvalidReservationRequestException.class, () -> service.create(command));
	}

	@Test
	void create_rejectsMissingCoordinates() {
		CreateReservationCommandDto command = command(BUYER_UUID).toBuilder()
				.latitude(null)
				.longitude(null)
				.build();

		assertThrows(InvalidReservationRequestException.class, () -> service.create(command));
		assertEquals(0, store.reservations.size());
	}

	@Test
	void create_rejectsBlankPlaceName() {
		CreateReservationCommandDto command = command(BUYER_UUID).toBuilder()
				.placeName("  ")
				.build();

		assertThrows(InvalidReservationRequestException.class, () -> service.create(command));
	}

	@Test
	void create_rejectsInvalidChatRoomId() {
		CreateReservationCommandDto command = command(BUYER_UUID).toBuilder()
				.chatRoomId("not-a-mongo-id")
				.build();

		assertThrows(InvalidReservationRequestException.class, () -> service.create(command));
	}

	private CreateReservationCommandDto command(String memberUuid) {
		return CreateReservationCommandDto.builder()
				.memberUuid(memberUuid)
				.chatRoomId(CHAT_ROOM_ID)
				.productPostUuid(PRODUCT_POST_UUID)
				.buyerUuid(BUYER_UUID)
				.sellerUuid(SELLER_UUID)
				.scheduledAt(SCHEDULED_AT)
				.placeName("해동병원 앞")
				.address(null)
				.latitude(35.115)
				.longitude(129.042)
				.build();
	}

	private static class InMemoryReservationStore implements LoadReservationPort, SaveReservationPort {

		private final List<Reservation> reservations = new ArrayList<>();
		private long nextId = 1L;

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
			return List.of();
		}

		@Override
		public List<Reservation> findByPartyMemberUuidAndStatus(String memberUuid, ReservationStatus status) {
			return List.of();
		}

		@Override
		public Reservation save(Reservation reservation) {
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
	}
}
