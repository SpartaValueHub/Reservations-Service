package com.sparta.reservations_service.application.port.out;

import com.sparta.reservations_service.domain.model.Reservation;

// 예약 등록 커밋 후 reservation.events 발행. 1차는 CREATED만
public interface PublishReservationEventPort {

	void publishCreated(Reservation reservation);
}
