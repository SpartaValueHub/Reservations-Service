package com.sparta.reservations_service.adaptor.out.mysql;

import com.sparta.reservations_service.adaptor.out.mysql.mapper.ReservationJpaMapper;
import com.sparta.reservations_service.adaptor.out.mysql.repository.ReservationJpaRepository;
import com.sparta.reservations_service.application.port.out.LoadReservationPort;
import com.sparta.reservations_service.application.port.out.SaveReservationPort;
import com.sparta.reservations_service.domain.model.Reservation;
import com.sparta.reservations_service.domain.model.ReservationStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

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
	public Reservation save(Reservation reservation) {
		return reservationJpaMapper.toDomain(
				reservationJpaRepository.save(reservationJpaMapper.toEntity(reservation))
		);
	}
}
