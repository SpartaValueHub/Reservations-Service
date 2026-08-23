package com.sparta.reservations_service.application.port.in;

import com.sparta.reservations_service.application.port.in.dto.ReservationDetailResultDto;

// 예약 단건 조회. CONFIRMED·CANCELED 모두 반환
public interface GetReservationUseCase {

	ReservationDetailResultDto get(String memberUuid, String reservationId);
}
