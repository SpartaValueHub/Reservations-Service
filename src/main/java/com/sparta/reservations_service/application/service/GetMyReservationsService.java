package com.sparta.reservations_service.application.service;

import com.sparta.reservations_service.application.port.in.GetMyReservationsUseCase;
import com.sparta.reservations_service.application.port.in.dto.MyReservationItemDto;
import com.sparta.reservations_service.application.port.in.dto.MyReservationListResultDto;
import com.sparta.reservations_service.application.port.out.LoadReservationPort;
import com.sparta.reservations_service.domain.exception.InvalidReservationRequestException;
import com.sparta.reservations_service.domain.exception.ReservationAuthMissingException;
import com.sparta.reservations_service.domain.model.Reservation;
import com.sparta.reservations_service.domain.model.ReservationStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Locale;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class GetMyReservationsService implements GetMyReservationsUseCase {

	private static final String STATUS_ALL = "ALL";

	// 예약 조회
	private final LoadReservationPort loadReservationPort;

	@Override
	@Transactional(readOnly = true)
	public MyReservationListResultDto get(String memberUuid, String status) {
		String actorUuid = requireMemberUuid(memberUuid);
		List<Reservation> reservations = loadByStatus(actorUuid, parseStatusFilter(status));
		return MyReservationListResultDto.builder()
				.reservations(reservations.stream().map(this::toItem).toList())
				.build();
	}

	private List<Reservation> loadByStatus(String memberUuid, String statusFilter) {
		if (STATUS_ALL.equals(statusFilter)) {
			return loadReservationPort.findByPartyMemberUuid(memberUuid);
		}
		return loadReservationPort.findByPartyMemberUuidAndStatus(
				memberUuid,
				ReservationStatus.valueOf(statusFilter)
		);
	}

	private String parseStatusFilter(String status) {
		if (status == null || status.isBlank()) {
			return ReservationStatus.CONFIRMED.name();
		}
		String normalized = status.trim().toUpperCase(Locale.ROOT);
		if (STATUS_ALL.equals(normalized)
				|| ReservationStatus.CONFIRMED.name().equals(normalized)
				|| ReservationStatus.CANCELED.name().equals(normalized)) {
			return normalized;
		}
		throw new InvalidReservationRequestException("status 값이 올바르지 않습니다.");
	}

	private MyReservationItemDto toItem(Reservation reservation) {
		return MyReservationItemDto.builder()
				.reservationId(reservation.getReservationUuid())
				.chatRoomId(reservation.getChatRoomId())
				.productPostUuid(reservation.getProductPostUuid())
				.scheduledAt(reservation.getScheduledAt())
				.placeName(reservation.getPlaceName())
				.status(reservation.getStatus())
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
}
