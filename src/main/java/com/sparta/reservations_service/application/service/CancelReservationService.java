package com.sparta.reservations_service.application.service;

import com.sparta.reservations_service.application.port.in.CancelReservationUseCase;
import com.sparta.reservations_service.application.port.in.dto.ReservationDetailResultDto;
import com.sparta.reservations_service.application.port.out.LoadReservationPort;
import com.sparta.reservations_service.application.port.out.SaveReservationPort;
import com.sparta.reservations_service.domain.exception.InvalidReservationRequestException;
import com.sparta.reservations_service.domain.exception.ReservationAccessDeniedException;
import com.sparta.reservations_service.domain.exception.ReservationAlreadyCanceledException;
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

	private final LoadReservationPort loadReservationPort;
	private final SaveReservationPort saveReservationPort;

	@Override
	@Transactional
	public ReservationDetailResultDto cancel(String memberUuid, String reservationId) {
		String actorUuid = requireMemberUuid(memberUuid);
		String reservationUuid = requireUuid(reservationId, "reservationId는 필수입니다.");
		Reservation reservation = loadReservationPort.findByReservationUuid(reservationUuid)
				.orElseThrow(ReservationNotFoundException::new);

		if (!reservation.isSeller(actorUuid)) {
			throw ReservationAccessDeniedException.sellerOnly();
		}
		if (!reservation.isConfirmed()) {
			throw new ReservationAlreadyCanceledException();
		}

		Reservation saved = saveReservationPort.save(reservation.cancel(actorUuid));
		return ReservationDetailResultDto.from(saved);
	}

	private String requireMemberUuid(String value) {
		String normalized = value == null ? "" : value.trim();
		if (normalized.isBlank()) {
			throw new ReservationAuthMissingException();
		}
		return requireUuid(normalized, "X-Member-Uuid 형식이 올바르지 않습니다.");
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
}
