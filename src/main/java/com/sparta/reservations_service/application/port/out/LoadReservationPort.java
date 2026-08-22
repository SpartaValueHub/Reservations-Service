package com.sparta.reservations_service.application.port.out;

// 예약 조회
public interface LoadReservationPort {

	boolean existsConfirmedByChatRoomId(String chatRoomId);
}
