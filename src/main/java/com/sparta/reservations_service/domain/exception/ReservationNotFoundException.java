package com.sparta.reservations_service.domain.exception;

// 예약이 없음
public class ReservationNotFoundException extends RuntimeException {

	// 안정적 에러 코드
	private final String code;

	public ReservationNotFoundException() {
		super("예약을 찾을 수 없습니다.");
		this.code = "RESERVATION_NOT_FOUND";
	}

	public String getCode() {
		return code;
	}
}
