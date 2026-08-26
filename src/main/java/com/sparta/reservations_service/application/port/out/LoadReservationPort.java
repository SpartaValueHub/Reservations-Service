package com.sparta.reservations_service.application.port.out;

import com.sparta.reservations_service.domain.model.Reservation;

import java.util.Optional;

// 예약 조회
public interface LoadReservationPort {

	boolean existsConfirmedByChatRoomId(String chatRoomId);

	Optional<Reservation> findConfirmedByChatRoomId(String chatRoomId);

	Optional<Reservation> findByReservationUuid(String reservationUuid);
}
