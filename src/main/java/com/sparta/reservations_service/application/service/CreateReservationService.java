package com.sparta.reservations_service.application.service;

import com.sparta.reservations_service.application.port.in.CreateReservationUseCase;
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
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.UUID;
import java.util.regex.Pattern;

@Service
@RequiredArgsConstructor
public class CreateReservationService implements CreateReservationUseCase {

	private static final Pattern CHAT_ROOM_ID = Pattern.compile("^[0-9a-fA-F]{24}$");

	// 기존 CONFIRMED 예약 확인
	private final LoadReservationPort loadReservationPort;
	// 신규 예약 저장
	private final SaveReservationPort saveReservationPort;

	@Override
	@Transactional
	public ReservationDetailResultDto create(CreateReservationCommandDto command) {
		if (command == null) {
			throw new InvalidReservationRequestException("요청 본문이 필요합니다.");
		}

		String memberUuid = requireMemberUuid(command.getMemberUuid());
		String chatRoomId = requireChatRoomId(command.getChatRoomId());
		String productPostUuid = requireUuid(command.getProductPostUuid(), "productPostUuid는 필수입니다.");
		String buyerUuid = requireUuid(command.getBuyerUuid(), "buyerUuid는 필수입니다.");
		String sellerUuid = requireUuid(command.getSellerUuid(), "sellerUuid는 필수입니다.");
		Instant scheduledAt = requireScheduledAt(command.getScheduledAt());
		String placeName = requireText(command.getPlaceName(), "placeName은 필수입니다.");
		String address = optionalText(command.getAddress());
		Coordinate coordinate = requireCoordinate(command.getLatitude(), command.getLongitude());

		if (buyerUuid.equals(sellerUuid)) {
			throw new CannotReserveWithSelfException();
		}
		if (!memberUuid.equals(buyerUuid) && !memberUuid.equals(sellerUuid)) {
			throw new ReservationAccessDeniedException();
		}
		if (loadReservationPort.existsConfirmedByChatRoomId(chatRoomId)) {
			throw new ReservationAlreadyConfirmedException();
		}

		Reservation saved = saveReservationPort.save(Reservation.create(
				productPostUuid,
				chatRoomId,
				buyerUuid,
				sellerUuid,
				scheduledAt,
				placeName,
				address,
				coordinate.latitude,
				coordinate.longitude,
				memberUuid
		));
		return toResult(saved);
	}

	private ReservationDetailResultDto toResult(Reservation reservation) {
		return ReservationDetailResultDto.builder()
				.reservationId(reservation.getReservationUuid())
				.chatRoomId(reservation.getChatRoomId())
				.productPostUuid(reservation.getProductPostUuid())
				.buyerUuid(reservation.getBuyerUuid())
				.sellerUuid(reservation.getSellerUuid())
				.scheduledAt(reservation.getScheduledAt())
				.placeName(reservation.getPlaceName())
				.address(reservation.getAddress())
				.latitude(reservation.getLatitude())
				.longitude(reservation.getLongitude())
				.status(reservation.getStatus())
				.createdBy(reservation.getCreatedBy())
				.canceledBy(reservation.getCanceledBy())
				.canceledAt(reservation.getCanceledAt())
				.createdAt(reservation.getCreatedAt())
				.updatedAt(reservation.getUpdatedAt())
				.build();
	}

	private String requireMemberUuid(String value) {
		String normalized = value == null ? "" : value.trim();
		if (normalized.isBlank()) {
			throw new ReservationAuthMissingException();
		}
		return requireUuid(normalized, "X-Member-Uuid 형식이 올바르지 않습니다.");
	}

	private String requireChatRoomId(String value) {
		String normalized = value == null ? "" : value.trim();
		if (normalized.isBlank()) {
			throw new InvalidReservationRequestException("chatRoomId는 필수입니다.");
		}
		if (!CHAT_ROOM_ID.matcher(normalized).matches()) {
			throw new InvalidReservationRequestException("chatRoomId 형식이 올바르지 않습니다.");
		}
		return normalized;
	}

	private String requireUuid(String value, String blankMessage) {
		String normalized = value == null ? "" : value.trim();
		if (normalized.isBlank()) {
			throw new InvalidReservationRequestException(blankMessage);
		}
		try {
			return UUID.fromString(normalized).toString();
		}
		catch (IllegalArgumentException exception) {
			throw new InvalidReservationRequestException("UUID 형식이 올바르지 않습니다.");
		}
	}

	private Instant requireScheduledAt(Instant scheduledAt) {
		if (scheduledAt == null) {
			throw new InvalidReservationRequestException("scheduledAt은 필수입니다.");
		}
		return scheduledAt;
	}

	private String requireText(String value, String message) {
		String normalized = value == null ? "" : value.trim();
		if (normalized.isBlank()) {
			throw new InvalidReservationRequestException(message);
		}
		return normalized;
	}

	private String optionalText(String value) {
		if (value == null) {
			return null;
		}
		String normalized = value.trim();
		return normalized.isBlank() ? null : normalized;
	}

	private Coordinate requireCoordinate(Double latitude, Double longitude) {
		if (latitude == null && longitude == null) {
			return new Coordinate(null, null);
		}
		if (latitude == null || longitude == null) {
			throw new InvalidReservationRequestException("latitude와 longitude는 함께 보내야 합니다.");
		}
		if (latitude < -90 || latitude > 90) {
			throw new InvalidReservationRequestException("latitude 범위가 올바르지 않습니다.");
		}
		if (longitude < -180 || longitude > 180) {
			throw new InvalidReservationRequestException("longitude 범위가 올바르지 않습니다.");
		}
		return new Coordinate(latitude, longitude);
	}

	private record Coordinate(Double latitude, Double longitude) {
	}
}
