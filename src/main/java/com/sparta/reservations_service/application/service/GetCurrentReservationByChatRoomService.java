package com.sparta.reservations_service.application.service;

import com.sparta.reservations_service.application.port.in.GetCurrentReservationByChatRoomUseCase;
import com.sparta.reservations_service.application.port.in.dto.ReservationDetailResultDto;
import com.sparta.reservations_service.application.port.out.LoadReservationPort;
import com.sparta.reservations_service.domain.exception.InvalidReservationRequestException;
import com.sparta.reservations_service.domain.exception.ReservationAccessDeniedException;
import com.sparta.reservations_service.domain.exception.ReservationAuthMissingException;
import com.sparta.reservations_service.domain.model.Reservation;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.UUID;
import java.util.regex.Pattern;

@Service
@RequiredArgsConstructor
public class GetCurrentReservationByChatRoomService implements GetCurrentReservationByChatRoomUseCase {

	private static final Pattern CHAT_ROOM_ID = Pattern.compile("^[0-9a-fA-F]{24}$");

	private final LoadReservationPort loadReservationPort;

	@Override
	@Transactional(readOnly = true)
	public Optional<ReservationDetailResultDto> get(String memberUuid, String chatRoomId) {
		String actorUuid = requireMemberUuid(memberUuid);
		String normalizedChatRoomId = requireChatRoomId(chatRoomId);
		return loadReservationPort.findConfirmedByChatRoomId(normalizedChatRoomId)
				.map(reservation -> toResult(requireParty(reservation, actorUuid)));
	}

	private Reservation requireParty(Reservation reservation, String memberUuid) {
		if (!reservation.isParty(memberUuid)) {
			throw new ReservationAccessDeniedException();
		}
		return reservation;
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
		try {
			return UUID.fromString(normalized).toString();
		}
		catch (IllegalArgumentException exception) {
			throw new InvalidReservationRequestException("UUID 형식이 올바르지 않습니다.");
		}
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
}
