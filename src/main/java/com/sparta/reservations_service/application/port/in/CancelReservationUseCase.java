package com.sparta.reservations_service.application.port.in;

import com.sparta.reservations_service.application.port.in.dto.ReservationDetailResultDto;

// 예약 취소. 행은 남기고 CANCELED로 바꾼다
public interface CancelReservationUseCase {

	ReservationDetailResultDto cancel(String memberUuid, String reservationId);
}
