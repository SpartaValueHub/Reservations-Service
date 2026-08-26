package com.sparta.reservations_service.domain.exception;

// CANCELED 예약을 수정하려 함
public class ReservationNotConfirmedException extends RuntimeException {

	// 안정적 에러 코드
	private final String code;

	public ReservationNotConfirmedException() {
		super("확정된 예약만 수정할 수 있습니다.");
		this.code = "RESERVATION_NOT_CONFIRMED";
	}

	public String getCode() {
		return code;
	}
}
