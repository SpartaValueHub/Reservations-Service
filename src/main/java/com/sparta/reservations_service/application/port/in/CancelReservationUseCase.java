package com.sparta.reservations_service.application.port.in;

import com.sparta.reservations_service.application.port.in.dto.ReservationDetailResultDto;

// 거래 예약 취소. 판매자만. 행은 남기고 CANCELED
public interface CancelReservationUseCase {

	ReservationDetailResultDto cancel(String memberUuid, String reservationId);
}
