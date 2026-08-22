package com.sparta.reservations_service.domain.exception;

// 호출자가 해당 예약의 구매자·판매자가 아님
public class ReservationAccessDeniedException extends RuntimeException {

	// 안정적 에러 코드
	private final String code;

	public ReservationAccessDeniedException() {
		super("예약 당사자만 요청할 수 있습니다.");
		this.code = "RESERVATION_ACCESS_DENIED";
	}

	public String getCode() {
		return code;
	}
}
