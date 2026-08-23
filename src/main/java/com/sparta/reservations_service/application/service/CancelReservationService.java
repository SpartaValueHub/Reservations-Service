package com.sparta.reservations_service.application.service;

import com.sparta.reservations_service.application.port.in.CancelReservationUseCase;
import com.sparta.reservations_service.application.port.in.dto.ReservationDetailResultDto;
import com.sparta.reservations_service.application.port.out.LoadReservationPort;
import com.sparta.reservations_service.application.port.out.SaveReservationPort;
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
public class CancelReservationService implements CancelReservationUseCase {

	// 취소 대상 조회
	private final LoadReservationPort loadReservationPort;
	// 같은 행 저장. 삭제가 아님
	private final SaveReservationPort saveReservationPort;

	@Override
	@Transactional
	public ReservationDetailResultDto cancel(String memberUuid, String reservationId) {
		String actorUuid = requireMemberUuid(memberUuid);
		String reservationUuid = requireReservationUuid(reservationId);
		Reservation reservation = loadReservationPort.findByReservationUuid(reservationUuid)
				.orElseThrow(ReservationNotFoundException::new);
		if (!reservation.isParty(actorUuid)) {
			throw new ReservationAccessDeniedException();
		}
		Reservation saved = saveReservationPort.save(reservation.cancel(actorUuid));
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
