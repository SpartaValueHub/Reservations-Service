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
	public Optional<ReservationDetailResultDto> get(String memberUuid, String chatRoomId, String productPostUuid) {
		String actorUuid = requireMemberUuid(memberUuid);
		String normalizedChatRoomId = requireChatRoomId(chatRoomId);
		Optional<Reservation> roomReservation = loadReservationPort.findConfirmedByChatRoomId(normalizedChatRoomId);
		if (roomReservation.isPresent()) {
			return Optional.of(ReservationDetailResultDto.from(requireParty(roomReservation.get(), actorUuid)));
		}
		if (productPostUuid == null || productPostUuid.isBlank()) {
			return Optional.empty();
		}
		String normalizedProductPostUuid = requireUuid(productPostUuid);
		return loadReservationPort.findConfirmedByProductPostUuid(normalizedProductPostUuid)
				.filter(reservation -> reservation.isParty(actorUuid))
				.map(ReservationDetailResultDto::from);
	}

	private Reservation requireParty(Reservation reservation, String memberUuid) {
		if (!reservation.isParty(memberUuid)) {
			throw new ReservationAccessDeniedException();
		}
		return reservation;
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

	private String requireUuid(String value) {
		String normalized = value.trim();
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
