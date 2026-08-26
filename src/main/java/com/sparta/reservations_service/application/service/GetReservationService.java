package com.sparta.reservations_service.application.service;

import com.sparta.reservations_service.application.port.in.GetReservationUseCase;
import com.sparta.reservations_service.application.port.in.dto.ReservationDetailResultDto;
import com.sparta.reservations_service.application.port.out.LoadReservationPort;
import com.sparta.reservations_service.domain.exception.InvalidReservationRequestException;
import com.sparta.reservations_service.domain.exception.ReservationAccessDeniedException;
import com.sparta.reservations_service.domain.exception.ReservationAuthMissingException;
import com.sparta.reservations_service.domain.exception.ReservationNotFoundException;
import com.sparta.reservations_service.domain.model.Reservation;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class GetReservationService implements GetReservationUseCase {

	// 예약 조회
	private final LoadReservationPort loadReservationPort;

	@Override
	@Transactional(readOnly = true)
	public ReservationDetailResultDto get(String memberUuid, String reservationId) {
		String actorUuid = requireMemberUuid(memberUuid);
		String reservationUuid = requireReservationUuid(reservationId);
		Reservation reservation = loadReservationPort.findByReservationUuid(reservationUuid)
				.orElseThrow(ReservationNotFoundException::new);
		if (!reservation.isParty(actorUuid)) {
			throw new ReservationAccessDeniedException();
		}
		return toResult(reservation);
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
		return requireUuid(normalized);
	}

	private String requireReservationUuid(String value) {
		String normalized = value == null ? "" : value.trim();
		if (normalized.isBlank()) {
			throw new InvalidReservationRequestException("reservationId는 필수입니다.");
		}
		return requireUuid(normalized);
	}

	private String requireUuid(String value) {
		try {
			return UUID.fromString(value).toString();
		}
		catch (IllegalArgumentException exception) {
			throw new InvalidReservationRequestException("UUID 형식이 올바르지 않습니다.");
		}
	}
}
