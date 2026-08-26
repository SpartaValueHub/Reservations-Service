package com.sparta.reservations_service.application.service;

import com.sparta.reservations_service.application.port.in.dto.ReservationDetailResultDto;
import com.sparta.reservations_service.application.port.in.dto.UpdateReservationCommandDto;
import com.sparta.reservations_service.application.port.out.LoadReservationPort;
import com.sparta.reservations_service.application.port.out.SaveReservationPort;
import com.sparta.reservations_service.domain.exception.InvalidReservationRequestException;
import com.sparta.reservations_service.domain.exception.ReservationAccessDeniedException;
import com.sparta.reservations_service.domain.exception.ReservationAuthMissingException;
import com.sparta.reservations_service.domain.exception.ReservationNotConfirmedException;
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
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

class UpdateReservationServiceTest {

	private static final String BUYER_UUID = "22222222-2222-4222-8222-222222222222";
	private static final String SELLER_UUID = "33333333-3333-4333-8333-333333333333";
	private static final String OTHER_UUID = "55555555-5555-4555-8555-555555555555";
	private static final String PRODUCT_POST_UUID = "11111111-1111-4111-8111-111111111111";
	private static final String CHAT_ROOM_ID = "67a1c2d3e4f5a6b7c8d9e0f1";
	private static final Instant SCHEDULED_AT = Instant.parse("2026-08-31T10:10:00Z");
	private static final Instant NEW_SCHEDULED_AT = Instant.parse("2026-09-01T11:00:00Z");
	private static final String MISSING_UUID = "99999999-9999-4999-8999-999999999999";

	private InMemoryReservationStore store;
	private UpdateReservationService service;

	@BeforeEach
	void setUp() {
		store = new InMemoryReservationStore();
		service = new UpdateReservationService(store, store);
	}

	@Test
	void update_changesMeetingFieldsOnSameRow() {
		Reservation saved = store.add(confirmed());

		ReservationDetailResultDto result = service.update(UpdateReservationCommandDto.builder()
				.memberUuid(SELLER_UUID)
				.reservationId(saved.getReservationUuid())
				.scheduledAt(NEW_SCHEDULED_AT)
				.placeName("해동병원 정문")
				.latitude(35.12)
				.longitude(129.05)
				.build());

		assertEquals(saved.getReservationUuid(), result.getReservationId());
		assertEquals(NEW_SCHEDULED_AT, result.getScheduledAt());
		assertEquals("해동병원 정문", result.getPlaceName());
		assertEquals(35.12, result.getLatitude());
		assertEquals(129.05, result.getLongitude());
		assertEquals(ReservationStatus.CONFIRMED, result.getStatus());
		assertEquals(1, store.reservations.size());
		assertEquals(saved.getReservationId(), store.reservations.get(0).getReservationId());
		assertEquals(saved.getCreatedAt(), result.getCreatedAt());
	}

	@Test
	void update_keepsOmittedFields() {
		Reservation saved = store.add(withAddress());

		ReservationDetailResultDto result = service.update(UpdateReservationCommandDto.builder()
				.memberUuid(SELLER_UUID)
				.reservationId(saved.getReservationUuid())
				.placeName("해동병원 정문")
				.build());

		assertEquals("해동병원 정문", result.getPlaceName());
		assertEquals("부산 영도구", result.getAddress());
		assertEquals(35.115, result.getLatitude());
		assertEquals(129.042, result.getLongitude());
		assertEquals(SCHEDULED_AT, result.getScheduledAt());
	}

	@Test
	void update_clearsAddressWhenSpecifiedNull() {
		Reservation saved = store.add(withAddress());

		ReservationDetailResultDto result = service.update(UpdateReservationCommandDto.builder()
				.memberUuid(SELLER_UUID)
				.reservationId(saved.getReservationUuid())
				.address(null)
				.addressSpecified(true)
				.build());

		assertNull(result.getAddress());
		assertEquals("해동병원 앞", result.getPlaceName());
	}

	@Test
	void update_rejectsBuyer() {
		Reservation saved = store.add(confirmed());

		assertThrows(ReservationAccessDeniedException.class, () -> service.update(UpdateReservationCommandDto.builder()
				.memberUuid(BUYER_UUID)
				.reservationId(saved.getReservationUuid())
				.placeName("해동병원 정문")
				.build()));
		assertEquals("해동병원 앞", store.reservations.get(0).getPlaceName());
	}

	@Test
	void update_rejectsEmptyPatch() {
		Reservation saved = store.add(confirmed());

		assertThrows(InvalidReservationRequestException.class, () -> service.update(UpdateReservationCommandDto.builder()
				.memberUuid(SELLER_UUID)
				.reservationId(saved.getReservationUuid())
				.build()));
	}

	@Test
	void update_rejectsOneCoordinate() {
		Reservation saved = store.add(confirmed());

		assertThrows(InvalidReservationRequestException.class, () -> service.update(UpdateReservationCommandDto.builder()
				.memberUuid(SELLER_UUID)
				.reservationId(saved.getReservationUuid())
				.latitude(35.12)
				.build()));
	}

	@Test
	void update_rejectsBlankPlaceName() {
		Reservation saved = store.add(confirmed());

		assertThrows(InvalidReservationRequestException.class, () -> service.update(UpdateReservationCommandDto.builder()
				.memberUuid(SELLER_UUID)
				.reservationId(saved.getReservationUuid())
				.placeName("  ")
				.build()));
	}

	@Test
	void update_rejectsMissingAuth() {
		Reservation saved = store.add(confirmed());

		assertThrows(ReservationAuthMissingException.class, () -> service.update(UpdateReservationCommandDto.builder()
				.reservationId(saved.getReservationUuid())
				.placeName("해동병원 정문")
				.build()));
	}

	@Test
	void update_rejectsInvalidReservationId() {
		assertThrows(InvalidReservationRequestException.class, () -> service.update(UpdateReservationCommandDto.builder()
				.memberUuid(SELLER_UUID)
				.reservationId("not-a-uuid")
				.placeName("해동병원 정문")
				.build()));
	}

	@Test
	void update_rejectsNotFound() {
		assertThrows(ReservationNotFoundException.class, () -> service.update(UpdateReservationCommandDto.builder()
				.memberUuid(SELLER_UUID)
				.reservationId(MISSING_UUID)
				.placeName("해동병원 정문")
				.build()));
	}

	@Test
	void update_rejectsThirdParty() {
		Reservation saved = store.add(confirmed());

		assertThrows(ReservationAccessDeniedException.class, () -> service.update(UpdateReservationCommandDto.builder()
				.memberUuid(OTHER_UUID)
				.reservationId(saved.getReservationUuid())
				.placeName("해동병원 정문")
				.build()));
	}

	@Test
	void update_rejectsCanceled() {
		Reservation saved = store.add(canceled());

		assertThrows(ReservationNotConfirmedException.class, () -> service.update(UpdateReservationCommandDto.builder()
				.memberUuid(SELLER_UUID)
				.reservationId(saved.getReservationUuid())
				.placeName("해동병원 정문")
				.build()));
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

	private Reservation withAddress() {
		return Reservation.create(
				PRODUCT_POST_UUID,
				CHAT_ROOM_ID,
				BUYER_UUID,
				SELLER_UUID,
				SCHEDULED_AT,
				"해동병원 앞",
				"부산 영도구",
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

		@Override
		public Reservation save(Reservation reservation) {
			for (int index = 0; index < reservations.size(); index++) {
				if (reservations.get(index).getReservationUuid().equals(reservation.getReservationUuid())) {
					Reservation persisted = Reservation.restore(
							reservations.get(index).getReservationId(),
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
					reservations.set(index, persisted);
					return persisted;
				}
			}
			throw new IllegalStateException("수정 대상 예약이 없습니다.");
		}
	}
}
