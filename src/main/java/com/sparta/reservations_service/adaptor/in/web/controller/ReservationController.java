package com.sparta.reservations_service.adaptor.in.web.controller;

import com.sparta.reservations_service.adaptor.in.web.vo.CreateReservationRequestVo;
import com.sparta.reservations_service.adaptor.in.web.vo.ReservationResponseVo;
import com.sparta.reservations_service.application.port.in.CreateReservationUseCase;
import com.sparta.reservations_service.application.port.in.dto.CreateReservationCommandDto;
import com.sparta.reservations_service.application.port.in.dto.ReservationDetailResultDto;
import com.sparta.reservations_service.domain.exception.InvalidReservationRequestException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@RequestMapping("/api/v1/reservations")
@RestController
public class ReservationController {

	// Gateway가 주입하는 회원 UUID 헤더
	private static final String MEMBER_UUID_HEADER = "X-Member-Uuid";

	private final CreateReservationUseCase createReservationUseCase;

	@PostMapping
	public ResponseEntity<ReservationResponseVo> createReservation(
			@RequestHeader(value = MEMBER_UUID_HEADER, required = false) String memberUuid,
			@RequestBody(required = false) CreateReservationRequestVo requestVo
	) {
		if (requestVo == null) {
			throw new InvalidReservationRequestException("요청 본문이 필요합니다.");
		}
		ReservationDetailResultDto resultDto = createReservationUseCase.create(toCommand(memberUuid, requestVo));
		return ResponseEntity.status(HttpStatus.CREATED).body(toVo(resultDto));
	}

	private CreateReservationCommandDto toCommand(String memberUuid, CreateReservationRequestVo requestVo) {
		return CreateReservationCommandDto.builder()
				.memberUuid(memberUuid)
				.chatRoomId(requestVo.getChatRoomId())
				.productPostUuid(requestVo.getProductPostUuid())
				.buyerUuid(requestVo.getBuyerUuid())
				.sellerUuid(requestVo.getSellerUuid())
				.scheduledAt(requestVo.getScheduledAt())
				.placeName(requestVo.getPlaceName())
				.address(requestVo.getAddress())
				.latitude(requestVo.getLatitude())
				.longitude(requestVo.getLongitude())
				.build();
	}

	private ReservationResponseVo toVo(ReservationDetailResultDto resultDto) {
		return ReservationResponseVo.builder()
				.reservationId(resultDto.getReservationId())
				.chatRoomId(resultDto.getChatRoomId())
				.productPostUuid(resultDto.getProductPostUuid())
				.buyerUuid(resultDto.getBuyerUuid())
				.sellerUuid(resultDto.getSellerUuid())
				.scheduledAt(resultDto.getScheduledAt())
				.placeName(resultDto.getPlaceName())
				.address(resultDto.getAddress())
				.latitude(resultDto.getLatitude())
				.longitude(resultDto.getLongitude())
				.status(resultDto.getStatus())
				.createdBy(resultDto.getCreatedBy())
				.canceledBy(resultDto.getCanceledBy())
				.canceledAt(resultDto.getCanceledAt())
				.createdAt(resultDto.getCreatedAt())
				.updatedAt(resultDto.getUpdatedAt())
				.build();
	}
}
