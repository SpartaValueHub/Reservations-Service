package com.sparta.reservations_service.domain.exception;

// 이미 CANCELED인 예약을 다시 취소함
public class ReservationAlreadyCanceledException extends RuntimeException {

	// 안정적 에러 코드
	private final String code;

	public ReservationAlreadyCanceledException() {
		super("이미 취소된 예약입니다.");
		this.code = "RESERVATION_ALREADY_CANCELED";
	}

	public String getCode() {
		return code;
	}
}
