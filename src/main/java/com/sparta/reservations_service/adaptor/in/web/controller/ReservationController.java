package com.sparta.reservations_service.adaptor.in.web.controller;

import com.sparta.reservations_service.adaptor.in.web.vo.CreateReservationRequestVo;
import com.sparta.reservations_service.adaptor.in.web.vo.MyReservationItemVo;
import com.sparta.reservations_service.adaptor.in.web.vo.MyReservationListResponseVo;
import com.sparta.reservations_service.adaptor.in.web.vo.ReservationResponseVo;
import com.sparta.reservations_service.adaptor.in.web.vo.UpdateReservationRequestVo;
import com.sparta.reservations_service.application.port.in.CreateReservationUseCase;
import com.sparta.reservations_service.application.port.in.GetCurrentReservationByChatRoomUseCase;
import com.sparta.reservations_service.application.port.in.GetMyReservationsUseCase;
import com.sparta.reservations_service.application.port.in.GetReservationUseCase;
import com.sparta.reservations_service.application.port.in.UpdateReservationUseCase;
import com.sparta.reservations_service.application.port.in.dto.CreateReservationCommandDto;
import com.sparta.reservations_service.application.port.in.dto.MyReservationItemDto;
import com.sparta.reservations_service.application.port.in.dto.MyReservationListResultDto;
import com.sparta.reservations_service.application.port.in.dto.ReservationDetailResultDto;
import com.sparta.reservations_service.application.port.in.dto.UpdateReservationCommandDto;
import com.sparta.reservations_service.domain.exception.InvalidReservationRequestException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@RequestMapping("/api/v1/reservations")
@RestController
public class ReservationController {

	// Gateway가 주입하는 회원 UUID 헤더
	private static final String MEMBER_UUID_HEADER = "X-Member-Uuid";

	private final CreateReservationUseCase createReservationUseCase;
	private final GetCurrentReservationByChatRoomUseCase getCurrentReservationByChatRoomUseCase;
	private final GetMyReservationsUseCase getMyReservationsUseCase;
	private final GetReservationUseCase getReservationUseCase;
	private final UpdateReservationUseCase updateReservationUseCase;

	@GetMapping("/me")
	public ResponseEntity<MyReservationListResponseVo> getMyReservations(
			@RequestHeader(value = MEMBER_UUID_HEADER, required = false) String memberUuid,
			@RequestParam(value = "status", required = false) String status
	) {
		MyReservationListResultDto resultDto = getMyReservationsUseCase.get(memberUuid, status);
		return ResponseEntity.ok(toListVo(resultDto));
	}

	@GetMapping("/by-chat-room/{chatRoomId}")
	public ResponseEntity<ReservationResponseVo> getCurrentReservationByChatRoom(
			@RequestHeader(value = MEMBER_UUID_HEADER, required = false) String memberUuid,
			@PathVariable String chatRoomId
	) {
		return getCurrentReservationByChatRoomUseCase.get(memberUuid, chatRoomId)
				.map(resultDto -> ResponseEntity.ok(toVo(resultDto)))
				.orElseGet(() -> ResponseEntity.noContent().build());
	}

	@GetMapping("/{reservationId}")
	public ResponseEntity<ReservationResponseVo> getReservation(
			@RequestHeader(value = MEMBER_UUID_HEADER, required = false) String memberUuid,
			@PathVariable String reservationId
	) {
		ReservationDetailResultDto resultDto = getReservationUseCase.get(memberUuid, reservationId);
		return ResponseEntity.ok(toVo(resultDto));
	}

	@PatchMapping("/{reservationId}")
	public ResponseEntity<ReservationResponseVo> updateReservation(
			@RequestHeader(value = MEMBER_UUID_HEADER, required = false) String memberUuid,
			@PathVariable String reservationId,
			@RequestBody(required = false) UpdateReservationRequestVo requestVo
	) {
		if (requestVo == null) {
			throw new InvalidReservationRequestException("요청 본문이 필요합니다.");
		}
		ReservationDetailResultDto resultDto = updateReservationUseCase.update(toUpdateCommand(memberUuid, reservationId, requestVo));
		return ResponseEntity.ok(toVo(resultDto));
	}

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

	private UpdateReservationCommandDto toUpdateCommand(
			String memberUuid,
			String reservationId,
			UpdateReservationRequestVo requestVo
	) {
		return UpdateReservationCommandDto.builder()
				.memberUuid(memberUuid)
				.reservationId(reservationId)
				.scheduledAt(requestVo.getScheduledAt())
				.placeName(requestVo.getPlaceName())
				.address(requestVo.getAddress())
				.addressSpecified(requestVo.isAddressSpecified())
				.latitude(requestVo.getLatitude())
				.longitude(requestVo.getLongitude())
				.build();
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

	private MyReservationListResponseVo toListVo(MyReservationListResultDto resultDto) {
		return MyReservationListResponseVo.builder()
				.reservations(resultDto.getReservations().stream().map(this::toItemVo).toList())
				.build();
	}

	private MyReservationItemVo toItemVo(MyReservationItemDto itemDto) {
		return MyReservationItemVo.builder()
				.reservationId(itemDto.getReservationId())
				.chatRoomId(itemDto.getChatRoomId())
				.productPostUuid(itemDto.getProductPostUuid())
				.scheduledAt(itemDto.getScheduledAt())
				.placeName(itemDto.getPlaceName())
				.status(itemDto.getStatus())
				.build();
	}
}
