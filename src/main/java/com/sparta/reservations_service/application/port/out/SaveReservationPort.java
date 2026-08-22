package com.sparta.reservations_service.application.port.out;

import com.sparta.reservations_service.domain.model.Reservation;

// 예약 저장
public interface SaveReservationPort {

	Reservation save(Reservation reservation);
}
