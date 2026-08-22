package com.sparta.reservations_service.application.port.in;

import com.sparta.reservations_service.application.port.in.dto.ReservationDetailResultDto;

import java.util.Optional;

// 채팅방의 현재 CONFIRMED 예약 조회. 없으면 empty
public interface GetCurrentReservationByChatRoomUseCase {

	Optional<ReservationDetailResultDto> get(String memberUuid, String chatRoomId);
}
