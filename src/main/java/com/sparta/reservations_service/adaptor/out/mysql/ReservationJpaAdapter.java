package com.sparta.reservations_service.adaptor.out.mysql;

import com.sparta.reservations_service.adaptor.out.mysql.entity.ReservationEntity;
import com.sparta.reservations_service.adaptor.out.mysql.mapper.ReservationJpaMapper;
import com.sparta.reservations_service.adaptor.out.mysql.repository.ReservationJpaRepository;
import com.sparta.reservations_service.application.port.out.LoadReservationPort;
import com.sparta.reservations_service.application.port.out.SaveReservationPort;
import com.sparta.reservations_service.domain.model.Reservation;
import com.sparta.reservations_service.domain.model.ReservationStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

// reservations JPA Adapter
@Repository
@RequiredArgsConstructor
public class ReservationJpaAdapter implements LoadReservationPort, SaveReservationPort {

	private final ReservationJpaRepository reservationJpaRepository;
	private final ReservationJpaMapper reservationJpaMapper;

	@Override
	public boolean existsConfirmedByChatRoomId(String chatRoomId) {
		if (chatRoomId == null || chatRoomId.isBlank()) {
			return false;
		}
		return reservationJpaRepository.existsByChatRoomIdAndStatus(chatRoomId, ReservationStatus.CONFIRMED);
	}

	@Override
	public Optional<Reservation> findConfirmedByChatRoomId(String chatRoomId) {
		if (chatRoomId == null || chatRoomId.isBlank()) {
			return Optional.empty();
		}
		return reservationJpaRepository.findByChatRoomIdAndStatus(chatRoomId, ReservationStatus.CONFIRMED)
				.map(reservationJpaMapper::toDomain);
	}

	@Override
	public Optional<Reservation> findByReservationUuid(String reservationUuid) {
		if (reservationUuid == null || reservationUuid.isBlank()) {
			return Optional.empty();
		}
		return reservationJpaRepository.findByReservationUuid(reservationUuid)
				.map(reservationJpaMapper::toDomain);
	}

	@Override
	public List<Reservation> findByPartyMemberUuid(String memberUuid) {
		if (memberUuid == null || memberUuid.isBlank()) {
			return List.of();
		}
		return reservationJpaRepository.findByPartyMemberUuid(memberUuid).stream()
				.map(reservationJpaMapper::toDomain)
				.toList();
	}

	@Override
	public List<Reservation> findByPartyMemberUuidAndStatus(String memberUuid, ReservationStatus status) {
		if (memberUuid == null || memberUuid.isBlank() || status == null) {
			return List.of();
		}
		return reservationJpaRepository.findByPartyMemberUuidAndStatus(memberUuid, status).stream()
				.map(reservationJpaMapper::toDomain)
				.toList();
	}

	@Override
	public Reservation save(Reservation reservation) {
		if (reservation.getReservationId() == null) {
			return reservationJpaMapper.toDomain(
					reservationJpaRepository.save(reservationJpaMapper.toEntity(reservation))
			);
		}
		return reservationJpaRepository.findById(reservation.getReservationId())
				.map(entity -> updateExisting(entity, reservation))
				.orElseGet(() -> reservationJpaMapper.toDomain(
						reservationJpaRepository.save(reservationJpaMapper.toEntity(reservation))
				));
	}

	private Reservation updateExisting(ReservationEntity entity, Reservation reservation) {
		reservationJpaMapper.updateEntity(entity, reservation);
		return reservationJpaMapper.toDomain(reservationJpaRepository.save(entity));
	}
}
