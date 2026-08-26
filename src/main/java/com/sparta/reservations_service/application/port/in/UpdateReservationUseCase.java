package com.sparta.reservations_service.application.port.in;

import com.sparta.reservations_service.application.port.in.dto.ReservationDetailResultDto;
import com.sparta.reservations_service.application.port.in.dto.UpdateReservationCommandDto;

// 거래 예약 수정. 판매자만
public interface UpdateReservationUseCase {

	ReservationDetailResultDto update(UpdateReservationCommandDto command);
}
