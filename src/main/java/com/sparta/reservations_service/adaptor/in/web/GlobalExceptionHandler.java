package com.sparta.reservations_service.adaptor.in.web;

import com.sparta.reservations_service.adaptor.in.web.vo.ErrorResponseVo;
import com.sparta.reservations_service.domain.exception.CannotReserveWithSelfException;
import com.sparta.reservations_service.domain.exception.InvalidReservationRequestException;
import com.sparta.reservations_service.domain.exception.ReservationAccessDeniedException;
import com.sparta.reservations_service.domain.exception.ReservationAlreadyCanceledException;
import com.sparta.reservations_service.domain.exception.ReservationAlreadyConfirmedException;
import com.sparta.reservations_service.domain.exception.ReservationAuthMissingException;
import com.sparta.reservations_service.domain.exception.ReservationNotConfirmedException;
import com.sparta.reservations_service.domain.exception.ReservationNotFoundException;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.Instant;

@RestControllerAdvice
public class GlobalExceptionHandler {

	@ExceptionHandler(ReservationAuthMissingException.class)
	public ResponseEntity<ErrorResponseVo> handleAuthMissing(
			ReservationAuthMissingException exception,
			HttpServletRequest request
	) {
		return error(HttpStatus.UNAUTHORIZED, exception.getCode(), exception.getMessage(), request);
	}

	@ExceptionHandler(InvalidReservationRequestException.class)
	public ResponseEntity<ErrorResponseVo> handleInvalidRequest(
			InvalidReservationRequestException exception,
			HttpServletRequest request
	) {
		return error(HttpStatus.BAD_REQUEST, exception.getCode(), exception.getMessage(), request);
	}

	@ExceptionHandler(HttpMessageNotReadableException.class)
	public ResponseEntity<ErrorResponseVo> handleUnreadable(
			HttpMessageNotReadableException exception,
			HttpServletRequest request
	) {
		return error(HttpStatus.BAD_REQUEST, "INVALID_REQUEST", "요청 본문이 올바르지 않습니다.", request);
	}

	@ExceptionHandler(CannotReserveWithSelfException.class)
	public ResponseEntity<ErrorResponseVo> handleCannotReserveWithSelf(
			CannotReserveWithSelfException exception,
			HttpServletRequest request
	) {
		return error(HttpStatus.BAD_REQUEST, exception.getCode(), exception.getMessage(), request);
	}

	@ExceptionHandler(ReservationAccessDeniedException.class)
	public ResponseEntity<ErrorResponseVo> handleAccessDenied(
			ReservationAccessDeniedException exception,
			HttpServletRequest request
	) {
		return error(HttpStatus.FORBIDDEN, exception.getCode(), exception.getMessage(), request);
	}

	@ExceptionHandler(ReservationAlreadyConfirmedException.class)
	public ResponseEntity<ErrorResponseVo> handleAlreadyConfirmed(
			ReservationAlreadyConfirmedException exception,
			HttpServletRequest request
	) {
		return error(HttpStatus.CONFLICT, exception.getCode(), exception.getMessage(), request);
	}

	@ExceptionHandler(ReservationNotFoundException.class)
	public ResponseEntity<ErrorResponseVo> handleNotFound(
			ReservationNotFoundException exception,
			HttpServletRequest request
	) {
		return error(HttpStatus.NOT_FOUND, exception.getCode(), exception.getMessage(), request);
	}

	@ExceptionHandler(ReservationNotConfirmedException.class)
	public ResponseEntity<ErrorResponseVo> handleNotConfirmed(
			ReservationNotConfirmedException exception,
			HttpServletRequest request
	) {
		return error(HttpStatus.CONFLICT, exception.getCode(), exception.getMessage(), request);
	}

	@ExceptionHandler(ReservationAlreadyCanceledException.class)
	public ResponseEntity<ErrorResponseVo> handleAlreadyCanceled(
			ReservationAlreadyCanceledException exception,
			HttpServletRequest request
	) {
		return error(HttpStatus.CONFLICT, exception.getCode(), exception.getMessage(), request);
	}

	private ResponseEntity<ErrorResponseVo> error(
			HttpStatus status,
			String code,
			String message,
			HttpServletRequest request
	) {
		return ResponseEntity.status(status)
				.body(ErrorResponseVo.builder()
						.timestamp(Instant.now())
						.status(status.value())
						.code(code)
						.message(message)
						.path(request.getRequestURI())
						.build());
	}
}
