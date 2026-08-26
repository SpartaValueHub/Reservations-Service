package com.sparta.reservations_service.application.service;

import com.sparta.reservations_service.application.port.in.UpdateReservationUseCase;
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
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UpdateReservationService implements UpdateReservationUseCase {

	private final LoadReservationPort loadReservationPort;
	private final SaveReservationPort saveReservationPort;

	@Override
	@Transactional
	public ReservationDetailResultDto update(UpdateReservationCommandDto command) {
		if (command == null) {
			throw new InvalidReservationRequestException("요청 본문이 필요합니다.");
		}

		String memberUuid = requireMemberUuid(command.getMemberUuid());
		String reservationUuid = requireUuid(command.getReservationId(), "reservationId는 필수입니다.");
		Reservation reservation = loadReservationPort.findByReservationUuid(reservationUuid)
				.orElseThrow(ReservationNotFoundException::new);

		if (!reservation.isSeller(memberUuid)) {
			throw ReservationAccessDeniedException.sellerOnly();
		}
		if (!reservation.isConfirmed()) {
			throw new ReservationNotConfirmedException();
		}

		ChangeSet changeSet = resolveChanges(command, reservation);
		Reservation saved = saveReservationPort.save(reservation.changeSchedule(
				changeSet.scheduledAt,
				changeSet.placeName,
				changeSet.address,
				changeSet.latitude,
				changeSet.longitude
		));
		return ReservationDetailResultDto.from(saved);
	}

	private ChangeSet resolveChanges(UpdateReservationCommandDto command, Reservation reservation) {
		boolean scheduledAtSpecified = command.getScheduledAt() != null;
		boolean placeNameSpecified = command.getPlaceName() != null;
		boolean coordinatesSpecified = command.getLatitude() != null || command.getLongitude() != null;
		if (!scheduledAtSpecified && !placeNameSpecified && !command.isAddressSpecified() && !coordinatesSpecified) {
			throw new InvalidReservationRequestException("수정할 필드가 필요합니다.");
		}

		Instant scheduledAt = scheduledAtSpecified ? command.getScheduledAt() : reservation.getScheduledAt();
		String placeName = placeNameSpecified
				? requireText(command.getPlaceName(), "placeName은 비어 있을 수 없습니다.")
				: reservation.getPlaceName();
		String address = command.isAddressSpecified()
				? optionalText(command.getAddress())
				: reservation.getAddress();
		Coordinate coordinate = coordinatesSpecified
				? requireCoordinate(command.getLatitude(), command.getLongitude())
				: new Coordinate(reservation.getLatitude(), reservation.getLongitude());
		return new ChangeSet(scheduledAt, placeName, address, coordinate.latitude, coordinate.longitude);
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

	private record ChangeSet(
			Instant scheduledAt,
			String placeName,
			String address,
			Double latitude,
			Double longitude
	) {
	}

	private record Coordinate(Double latitude, Double longitude) {
	}
}
