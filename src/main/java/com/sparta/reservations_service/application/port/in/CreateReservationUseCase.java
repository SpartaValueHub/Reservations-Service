package com.sparta.reservations_service.application.port.in;

import com.sparta.reservations_service.application.port.in.dto.CreateReservationCommandDto;
import com.sparta.reservations_service.application.port.in.dto.ReservationDetailResultDto;

// 거래 예약 등록
public interface CreateReservationUseCase {

	ReservationDetailResultDto create(CreateReservationCommandDto command);
}
