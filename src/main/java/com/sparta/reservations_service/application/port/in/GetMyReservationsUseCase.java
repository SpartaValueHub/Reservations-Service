package com.sparta.reservations_service.application.port.in;

import com.sparta.reservations_service.application.port.in.dto.MyReservationListResultDto;

// 로그인한 회원의 거래 예약 목록
public interface GetMyReservationsUseCase {

	MyReservationListResultDto get(String memberUuid, String status);
}
