package com.sparta.reservations_service.adaptor.in.web.vo;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonSetter;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.Instant;

// 거래 예약 수정 요청. 보낸 필드만 변경
@Getter
@NoArgsConstructor
@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY)
public class UpdateReservationRequestVo {

	// 거래 예정 일시
	private Instant scheduledAt;
	// 장소명
	private String placeName;
	// 위도
	private Double latitude;
	// 경도
	private Double longitude;
	// 주소. JSON null이면 제거
	@JsonIgnore
	private String address;
	// 요청 JSON에 address가 포함됐는지
	@JsonIgnore
	private boolean addressSpecified;

	@JsonSetter("address")
	private void readAddress(String address) {
		this.address = address;
		this.addressSpecified = true;
	}
}
