package com.sparta.reservations_service.application.service;

import com.sparta.reservations_service.application.port.in.UpdateReservationUseCase;
import com.sparta.reservations_service.application.port.in.dto.ReservationDetailResultDto;
import com.sparta.reservations_service.application.port.in.dto.UpdateReservationCommandDto;
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

import java.time.Instant;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UpdateReservationService implements UpdateReservationUseCase {

	// 수정 대상 조회
	private final LoadReservationPort loadReservationPort;
	// 같은 행 저장
	private final SaveReservationPort saveReservationPort;

	@Override
	@Transactional
	public ReservationDetailResultDto update(UpdateReservationCommandDto command) {
		if (command == null) {
			throw new InvalidReservationRequestException("요청 본문이 필요합니다.");
		}

		String memberUuid = requireMemberUuid(command.getMemberUuid());
		String reservationUuid = requireReservationUuid(command.getReservationId());
		if (!hasPatch(command)) {
			throw new InvalidReservationRequestException("변경할 값이 필요합니다.");
		}

		Reservation reservation = loadReservationPort.findByReservationUuid(reservationUuid)
				.orElseThrow(ReservationNotFoundException::new);
		if (!reservation.isSeller(memberUuid)) {
			throw ReservationAccessDeniedException.sellerOnly();
		}

		Instant scheduledAt = command.getScheduledAt() == null
				? reservation.getScheduledAt()
				: command.getScheduledAt();
		String placeName = command.getPlaceName() == null
				? reservation.getPlaceName()
				: requireText(command.getPlaceName(), "placeName은 비울 수 없습니다.");
		String address = command.isAddressSpecified()
				? optionalText(command.getAddress())
				: reservation.getAddress();
		Coordinate coordinate = resolveCoordinate(command, reservation);

		Reservation saved = saveReservationPort.save(reservation.updateMeeting(
				scheduledAt,
				placeName,
				address,
				coordinate.latitude,
				coordinate.longitude
		));
		return ReservationDetailResultDto.from(saved);
	}

	private boolean hasPatch(UpdateReservationCommandDto command) {
		return command.getScheduledAt() != null
				|| command.getPlaceName() != null
				|| command.isAddressSpecified()
				|| command.getLatitude() != null
				|| command.getLongitude() != null;
	}

	private Coordinate resolveCoordinate(UpdateReservationCommandDto command, Reservation reservation) {
		if (command.getLatitude() == null && command.getLongitude() == null) {
			return new Coordinate(reservation.getLatitude(), reservation.getLongitude());
		}
		if (command.getLatitude() == null || command.getLongitude() == null) {
			throw new InvalidReservationRequestException("latitude와 longitude는 함께 보내야 합니다.");
		}
		return requireCoordinate(command.getLatitude(), command.getLongitude());
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
