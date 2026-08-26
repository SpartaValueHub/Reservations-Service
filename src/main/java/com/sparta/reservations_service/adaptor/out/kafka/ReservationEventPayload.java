package com.sparta.reservations_service.adaptor.out.kafka;

import com.sparta.reservations_service.domain.model.Reservation;
import lombok.Builder;
import lombok.Getter;

import java.time.Instant;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;

// kafka.md 3절 JSON. 키 이름은 문서 유지, 값은 엔티티에서 매핑
@Getter
@Builder
public class ReservationEventPayload {

	static final String EVENT_TYPE_CREATED = "CREATED";

	private static final ZoneId SEOUL = ZoneId.of("Asia/Seoul");
	private static final DateTimeFormatter MEET_AT = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ssXXX");
	private static final DateTimeFormatter UPDATED_AT = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss'Z'")
			.withZone(ZoneOffset.UTC);

	private final String eventType;
	private final String productPostUuid;
	private final String reservationUuid;
	private final String chatRoomUuid;
	private final String meetAt;
	private final String placeName;
	private final String sellerUuid;
	private final String buyerUuid;
	private final String updatedAt;

	public static ReservationEventPayload created(Reservation reservation) {
		return ReservationEventPayload.builder()
				.eventType(EVENT_TYPE_CREATED)
				.productPostUuid(reservation.getProductPostUuid())
				.reservationUuid(reservation.getReservationUuid())
				.chatRoomUuid(reservation.getChatRoomId())
				.meetAt(toMeetAt(reservation.getScheduledAt()))
				.placeName(reservation.getPlaceName())
				.sellerUuid(reservation.getSellerUuid())
				.buyerUuid(reservation.getBuyerUuid())
				.updatedAt(toUpdatedAt(reservation.getUpdatedAt()))
				.build();
	}

	// scheduled_at (Instant) → meetAt (+09:00)
	private static String toMeetAt(Instant scheduledAt) {
		return scheduledAt.atZone(SEOUL).toOffsetDateTime().truncatedTo(ChronoUnit.SECONDS).format(MEET_AT);
	}

	// updated_at (Instant) → updatedAt (UTC Z)
	private static String toUpdatedAt(Instant updatedAt) {
		return UPDATED_AT.format(updatedAt.truncatedTo(ChronoUnit.SECONDS));
	}
}
