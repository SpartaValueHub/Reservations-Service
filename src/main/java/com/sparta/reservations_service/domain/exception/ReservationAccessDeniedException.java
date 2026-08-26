package com.sparta.reservations_service.domain.exception;

// 호출자가 해당 예약의 권한이 없음
public class ReservationAccessDeniedException extends RuntimeException {

	// 안정적 에러 코드
	private final String code;

	public ReservationAccessDeniedException() {
		this("예약 당사자만 요청할 수 있습니다.");
	}

	public ReservationAccessDeniedException(String message) {
		super(message);
		this.code = "RESERVATION_ACCESS_DENIED";
	}

	public static ReservationAccessDeniedException sellerOnly() {
		return new ReservationAccessDeniedException("판매자만 요청할 수 있습니다.");
	}

	public String getCode() {
		return code;
	}
}
