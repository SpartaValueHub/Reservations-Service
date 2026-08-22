package com.sparta.reservations_service.domain.exception;

// 예약 요청 값이 비어 있거나 형식이 잘못됨
public class InvalidReservationRequestException extends RuntimeException {

	// 안정적 에러 코드
	private final String code;

	public InvalidReservationRequestException(String message) {
		super(message);
		this.code = "INVALID_REQUEST";
	}

	public String getCode() {
		return code;
	}
}
