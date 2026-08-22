package com.sparta.reservations_service.domain.exception;

// 구매자와 판매자가 동일하면 예약할 수 없음
public class CannotReserveWithSelfException extends RuntimeException {

	// 안정적 에러 코드
	private final String code;

	public CannotReserveWithSelfException() {
		super("구매자와 판매자가 같을 수 없습니다.");
		this.code = "CANNOT_RESERVE_WITH_SELF";
	}

	public String getCode() {
		return code;
	}
}
