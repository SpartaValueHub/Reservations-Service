package com.sparta.reservations_service.application.port.in;

import com.sparta.reservations_service.application.port.in.dto.ReservationDetailResultDto;
import com.sparta.reservations_service.application.port.in.dto.UpdateReservationCommandDto;

// 예약 일시·장소 수정. 판매자만. CONFIRMED만 가능
public interface UpdateReservationUseCase {

	ReservationDetailResultDto update(UpdateReservationCommandDto command);
}
