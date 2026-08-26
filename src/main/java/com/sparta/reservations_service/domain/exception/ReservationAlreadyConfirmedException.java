package com.sparta.reservations_service.domain.exception;

// 채팅방 또는 같은 상품에 이미 CONFIRMED 예약이 있음
public class ReservationAlreadyConfirmedException extends RuntimeException {

	// 안정적 에러 코드
	private final String code;

	public ReservationAlreadyConfirmedException() {
		super("이미 예약된 거래가 있습니다.");
		this.code = "RESERVATION_ALREADY_CONFIRMED";
	}

	public String getCode() {
		return code;
	}
}
