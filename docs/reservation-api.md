# 거래 예약 API

Reservations-Service가 채팅 약속(거래 예약)의 원본을 소유합니다. Chat은 방·메시지, 상품 서비스는 게시글 상태를 소유합니다.

1차 범위: 등록, 채팅방 현재 예약, 내 목록, 단건, 수정, 취소.  
채팅 `RESERVATION` 말풍선 삽입, 게시글 `tradeStatus` 연동, Chat·상품 HTTP 조회는 하지 않습니다.

구현됨: `POST /api/v1/reservations`, `GET /api/v1/reservations/by-chat-room/{chatRoomId}`, `GET /api/v1/reservations/me`, `GET /api/v1/reservations/{reservationId}`, `PATCH /api/v1/reservations/{reservationId}`, `DELETE /api/v1/reservations/{reservationId}`

공통 Error Response:

```json
{
  "timestamp": "2026-08-22T06:00:00Z",
  "status": 400,
  "code": "ERROR_CODE",
  "message": "설명",
  "path": "/api/v1/reservations"
}
```

외부 식별자는 `reservationId`입니다. 값은 `reservation_uuid`(UUID)입니다. 내부 PK `reservation_id`는 응답에 넣지 않습니다.

`chatRoomId`는 Chat Mongo 방 문서 ID입니다. UUID가 아닙니다.

---

## 도메인 규칙

- 구매자와 판매자 모두 등록·수정·취소할 수 있습니다. 호출자는 Gateway `X-Member-Uuid`이며, 그 사람이 `buyerUuid` 또는 `sellerUuid`여야 합니다.
- 채팅방마다 `CONFIRMED` 예약은 최대 1건입니다.
- 취소는 행을 삭제하지 않습니다. `status`를 `CANCELED`로 바꿉니다. `deleted_at`은 없습니다.
- 취소 후 다시 예약하면 새 행을 INSERT 합니다. 취소된 행을 `CONFIRMED`로 되돌리지 않습니다.
- 오른쪽 패널은 그 방의 `CONFIRMED` 한 건만 봅니다. 없으면 빈 상태(예약하기)입니다.
- 등록 시 Chat·상품 서비스를 조회하지 않습니다. 요청 본문의 `chatRoomId`, `productPostUuid`, `buyerUuid`, `sellerUuid`를 저장합니다.

상태

| status | 의미 |
| ------ | ---- |
| CONFIRMED | 현재 유효한 예약. 패널에 표시 |
| CANCELED | 취소됨. 패널에서는 없는 것과 같음. 단건 조회로는 보임 |

---

## 테이블 `reservations`

스키마: `reservations_db`

| 컬럼 | 타입 | 제약 | 의미 |
| ---- | ---- | ---- | ---- |
| reservation_id | BIGINT | PK | 내부 식별자 |
| reservation_uuid | CHAR(36) | UNIQUE NOT NULL | API `reservationId` |
| product_post_uuid | CHAR(36) | NOT NULL | 상품 게시글 UUID |
| chat_room_id | VARCHAR(24) | NOT NULL | Chat 방 Mongo ObjectId |
| buyer_uuid | CHAR(36) | NOT NULL | 구매자 |
| seller_uuid | CHAR(36) | NOT NULL | 판매자 |
| scheduled_at | DATETIME | NOT NULL | 거래 예정 일시 |
| place_name | VARCHAR(100) | NOT NULL | 거래 장소명 |
| address | VARCHAR(255) | NULL | 거래 장소 주소 |
| latitude | DECIMAL(10,7) | NOT NULL | 위도 |
| longitude | DECIMAL(10,7) | NOT NULL | 경도 |
| status | ENUM('CONFIRMED','CANCELED') | NOT NULL | 예약 상태 |
| created_by | CHAR(36) | NOT NULL | 등록한 회원 |
| canceled_by | CHAR(36) | NULL | 취소한 회원 |
| canceled_at | DATETIME | NULL | 취소 시각 |
| created_at | DATETIME | NOT NULL | 생성 시각 |
| updated_at | DATETIME | NOT NULL | 수정 시각 |

부분 유니크: `(chat_room_id)` WHERE `status = 'CONFIRMED'`

`latitude`와 `longitude`는 필수입니다. 지도 재표시에 좌표가 필요합니다.

---

## 공통 상세 바디

등록·현재 예약·단건·수정·취소 응답이 이 형태를 씁니다.

| 필드 | 타입 |
| ---- | ---- |
| reservationId | string |
| chatRoomId | string |
| productPostUuid | string |
| buyerUuid | string |
| sellerUuid | string |
| scheduledAt | string (ISO-8601) |
| placeName | string |
| address | string \| null |
| latitude | number |
| longitude | number |
| status | string (`CONFIRMED` `CANCELED`) |
| createdBy | string |
| canceledBy | string \| null |
| canceledAt | string (ISO-8601) \| null |
| createdAt | string (ISO-8601) |
| updatedAt | string (ISO-8601) |

```json
{
  "reservationId": "44444444-4444-4444-8444-444444444444",
  "chatRoomId": "67a1c2d3e4f5a6b7c8d9e0f1",
  "productPostUuid": "11111111-1111-4111-8111-111111111111",
  "buyerUuid": "22222222-2222-4222-8222-222222222222",
  "sellerUuid": "33333333-3333-4333-8333-333333333333",
  "scheduledAt": "2026-08-31T10:10:00Z",
  "placeName": "해동병원 앞",
  "address": null,
  "latitude": 35.115,
  "longitude": 129.042,
  "status": "CONFIRMED",
  "createdBy": "22222222-2222-4222-8222-222222222222",
  "canceledBy": null,
  "canceledAt": null,
  "createdAt": "2026-08-22T06:00:00Z",
  "updatedAt": "2026-08-22T06:00:00Z"
}
```

컨트롤러는 `GET /me`, `GET /by-chat-room/{chatRoomId}`를 `GET /{reservationId}`보다 먼저 매핑합니다.

---

## 거래 예약 등록

구현됨.

### Summary

채팅 오른쪽 패널에서 날짜·시간·장소를 정해 예약을 만듭니다. 구매자·판매자 모두 호출할 수 있습니다. Chat·상품 서비스는 조회하지 않습니다. `placeName`, `latitude`, `longitude`는 필수입니다. 백엔드는 지오코딩하지 않습니다.

### Method · Path

`POST /api/v1/reservations`

### Auth

필요 — Gateway JWT. Gateway가 `X-Member-Uuid` 헤더를 주입합니다.

### Request

Header

| 필드          | 타입   | 필수 | 제약      |
| ------------- | ------ | ---- | --------- |
| X-Member-Uuid | string | O    | 회원 UUID |

Body

| 필드            | 타입   | 필수 | 제약 |
| --------------- | ------ | ---- | ---- |
| chatRoomId      | string | O    | Chat 방 Mongo ObjectId |
| productPostUuid | string | O    | 상품 게시글 UUID. 상품 서비스 조회 없음 |
| buyerUuid       | string | O    | 구매자 UUID. 판매자와 같으면 안 됨 |
| sellerUuid      | string | O    | 판매자 UUID |
| scheduledAt     | string | O    | ISO-8601 |
| placeName       | string | O    | 거래 장소명. trim 후 빈 값 불가 |
| address         | string | X    | 거래 장소 주소 |
| latitude        | number | O    | 위도. longitude와 함께 필수 |
| longitude       | number | O    | 경도. latitude와 함께 필수 |

호출자는 `buyerUuid` 또는 `sellerUuid`여야 합니다.

```json
{
  "chatRoomId": "67a1c2d3e4f5a6b7c8d9e0f1",
  "productPostUuid": "11111111-1111-4111-8111-111111111111",
  "buyerUuid": "22222222-2222-4222-8222-222222222222",
  "sellerUuid": "33333333-3333-4333-8333-333333333333",
  "scheduledAt": "2026-08-31T10:10:00Z",
  "placeName": "해동병원 앞",
  "address": null,
  "latitude": 35.115,
  "longitude": 129.042
}
```

### Response

`201` — 공통 상세 바디. `status`는 `CONFIRMED`, `createdBy`는 호출자입니다.

### Errors

| status | code | 의미 |
| ------ | ---- | ---- |
| 401 | RESERVATION_AUTH_MISSING | X-Member-Uuid 헤더 없음 |
| 400 | INVALID_REQUEST | 필수 필드 없음, 좌표 없음, 좌표 한쪽만 있음, UUID/시각 형식 오류 |
| 400 | CANNOT_RESERVE_WITH_SELF | buyerUuid와 sellerUuid가 동일 |
| 403 | RESERVATION_ACCESS_DENIED | 호출자가 buyer도 seller도 아님 |
| 409 | RESERVATION_ALREADY_CONFIRMED | 해당 채팅방에 CONFIRMED 예약이 이미 있음 |

---

## 채팅방 현재 예약

구현됨.

### Summary

채팅방 입장 시 오른쪽 패널을 채웁니다. Chat 상세(`GET /api/v1/chat/rooms/{roomId}`)와 다릅니다. 방 헤더·상대 프로필·읽음 처리는 Chat, 이 API는 현재 약속만 반환합니다. 프론트는 입장 때 두 요청을 같이 칩니다.

### Method · Path

`GET /api/v1/reservations/by-chat-room/{chatRoomId}`

### Auth

필요 — Gateway JWT. Gateway가 `X-Member-Uuid` 헤더를 주입합니다.

### Request

Header

| 필드          | 타입   | 필수 | 제약      |
| ------------- | ------ | ---- | --------- |
| X-Member-Uuid | string | O    | 회원 UUID |

Path

| 필드       | 타입   | 필수 | 제약 |
| ---------- | ------ | ---- | ---- |
| chatRoomId | string | O    | Chat 방 Mongo ObjectId |

### Response

해당 방의 `CONFIRMED` 1건: `200` + 공통 상세 바디. 패널에 예약 정보·수정/취소를 그립니다.

없거나 전부 `CANCELED`: **`204` 바디 없음**. 잘못된 id가 아니라 빈 패널입니다. 프론트는 “예약된 거래가 없습니다”와 예약하기를 그립니다.

`404`는 쓰지 않습니다. 빈 예약과 리소스 없음을 섞지 않기 위함입니다.

### Errors

| status | code | 의미 |
| ------ | ---- | ---- |
| 401 | RESERVATION_AUTH_MISSING | X-Member-Uuid 헤더 없음 |
| 400 | INVALID_REQUEST | chatRoomId 없음 |
| 403 | RESERVATION_ACCESS_DENIED | 이 방의 CONFIRMED 예약이 있는데 호출자가 당사자가 아님 |

`CONFIRMED`가 없고 과거 `CANCELED`만 있을 때, 1차는 당사자 검사를 하지 않고 `204`를 반환합니다. Reservations는 Chat 참여자를 조회하지 않습니다.

---

## 내 거래 예약 목록

구현됨.

### Summary

로그인한 회원이 buyer이거나 seller인 예약을 카드 목록으로 반환합니다. 카드를 누르면 `chatRoomId`로 채팅방에 들어갑니다.

### Method · Path

`GET /api/v1/reservations/me`

### Auth

필요 — Gateway JWT. Gateway가 `X-Member-Uuid` 헤더를 주입합니다.

### Request

Header

| 필드          | 타입   | 필수 | 제약      |
| ------------- | ------ | ---- | --------- |
| X-Member-Uuid | string | O    | 회원 UUID |

Query

| 필드   | 타입   | 필수 | 제약 |
| ------ | ------ | ---- | ---- |
| status | string | X    | 기본 `CONFIRMED`. `CANCELED` 또는 `ALL` |

페이징 없음. 1차는 `updated_at` desc입니다.

### Response

`200`

| 필드 | 타입 |
| ---- | ---- |
| reservations | array |
| reservations[].reservationId | string |
| reservations[].chatRoomId | string |
| reservations[].productPostUuid | string |
| reservations[].scheduledAt | string (ISO-8601) |
| reservations[].placeName | string |
| reservations[].status | string |

`chatRoomId`는 카드마다 필수입니다.

```json
{
  "reservations": [
    {
      "reservationId": "44444444-4444-4444-8444-444444444444",
      "chatRoomId": "67a1c2d3e4f5a6b7c8d9e0f1",
      "productPostUuid": "11111111-1111-4111-8111-111111111111",
      "scheduledAt": "2026-08-31T10:10:00Z",
      "placeName": "해동병원 앞",
      "status": "CONFIRMED"
    }
  ]
}
```

없으면 `reservations`는 빈 배열입니다.

### Errors

| status | code | 의미 |
| ------ | ---- | ---- |
| 401 | RESERVATION_AUTH_MISSING | X-Member-Uuid 헤더 없음 |
| 400 | INVALID_REQUEST | status 값이 허용 목록 밖 |

---

## 거래 예약 상세

구현됨.

### Summary

수정 폼, 취소 전 확인, 이후 채팅 시스템 메시지의 `reservationId`로 다시 볼 때 사용합니다. `CONFIRMED`와 `CANCELED` 모두 반환합니다.

### Method · Path

`GET /api/v1/reservations/{reservationId}`

### Auth

필요 — Gateway JWT. Gateway가 `X-Member-Uuid` 헤더를 주입합니다.

### Request

Header

| 필드          | 타입   | 필수 | 제약      |
| ------------- | ------ | ---- | --------- |
| X-Member-Uuid | string | O    | 회원 UUID |

Path

| 필드           | 타입   | 필수 | 제약 |
| -------------- | ------ | ---- | ---- |
| reservationId  | string | O    | reservation_uuid |

### Response

`200` — 공통 상세 바디

### Errors

| status | code | 의미 |
| ------ | ---- | ---- |
| 401 | RESERVATION_AUTH_MISSING | X-Member-Uuid 헤더 없음 |
| 400 | INVALID_REQUEST | reservationId 형식 오류 |
| 404 | RESERVATION_NOT_FOUND | 예약 없음 |
| 403 | RESERVATION_ACCESS_DENIED | 호출자가 당사자가 아님 |

---

## 거래 예약 수정

구현됨.

### Summary

현재 예약의 날짜·시간·장소만 바꿉니다. 같은 행을 업데이트합니다. 새 행을 만들지 않습니다. 좌표를 보내면 `latitude`와 `longitude`를 함께 보냅니다. 등록된 좌표는 제거하지 않습니다.

### Method · Path

`PATCH /api/v1/reservations/{reservationId}`

### Auth

필요 — Gateway JWT. Gateway가 `X-Member-Uuid` 헤더를 주입합니다.

### Request

Header

| 필드          | 타입   | 필수 | 제약      |
| ------------- | ------ | ---- | --------- |
| X-Member-Uuid | string | O    | 회원 UUID |

Path

| 필드          | 타입   | 필수 | 제약 |
| ------------- | ------ | ---- | ---- |
| reservationId | string | O    | reservation_uuid |

Body — 보낸 필드만 변경합니다. 비어 있으면 400입니다.

| 필드        | 타입   | 필수 | 제약 |
| ----------- | ------ | ---- | ---- |
| scheduledAt | string | X    | ISO-8601 |
| placeName   | string | X    | trim 후 빈 값 불가 |
| address     | string | X    | null이면 주소 제거 |
| latitude    | number | X    | 보내면 longitude와 함께 필수. 제거 불가 |
| longitude   | number | X    | 보내면 latitude와 함께 필수. 제거 불가 |

```json
{
  "scheduledAt": "2026-09-01T11:00:00Z",
  "placeName": "해동병원 정문",
  "address": null,
  "latitude": 35.115,
  "longitude": 129.042
}
```

### Response

`200` — 공통 상세 바디

### Errors

| status | code | 의미 |
| ------ | ---- | ---- |
| 401 | RESERVATION_AUTH_MISSING | X-Member-Uuid 헤더 없음 |
| 400 | INVALID_REQUEST | 본문 없음, 좌표 한쪽만, 필드 형식 오류 |
| 404 | RESERVATION_NOT_FOUND | 예약 없음 |
| 403 | RESERVATION_ACCESS_DENIED | 호출자가 당사자가 아님 |
| 409 | RESERVATION_NOT_CONFIRMED | CANCELED 예약을 수정함 |

---

## 거래 예약 취소

구현됨.

### Summary

HTTP `DELETE`이지만 행은 남깁니다. `status`를 `CANCELED`로 바꾸고 `canceledBy`·`canceledAt`을 기록합니다. 이후 현재 예약 조회는 `204`입니다.

### Method · Path

`DELETE /api/v1/reservations/{reservationId}`

### Auth

필요 — Gateway JWT. Gateway가 `X-Member-Uuid` 헤더를 주입합니다.

### Request

Header

| 필드          | 타입   | 필수 | 제약      |
| ------------- | ------ | ---- | --------- |
| X-Member-Uuid | string | O    | 회원 UUID |

Path

| 필드          | 타입   | 필수 | 제약 |
| ------------- | ------ | ---- | ---- |
| reservationId | string | O    | reservation_uuid |

Body 없음.

### Response

`200` — 공통 상세 바디. `status`는 `CANCELED`입니다.

### Errors

| status | code | 의미 |
| ------ | ---- | ---- |
| 401 | RESERVATION_AUTH_MISSING | X-Member-Uuid 헤더 없음 |
| 400 | INVALID_REQUEST | reservationId 형식 오류 |
| 404 | RESERVATION_NOT_FOUND | 예약 없음 |
| 403 | RESERVATION_ACCESS_DENIED | 호출자가 당사자가 아님 |
| 409 | RESERVATION_ALREADY_CANCELED | 이미 CANCELED |

---

## 화면 호출

채팅 입장

```
GET /api/v1/chat/rooms/{roomId}
GET /api/v1/reservations/by-chat-room/{roomId}
GET /api/v1/chat/rooms/{roomId}/messages
```

예약하기: `POST /api/v1/reservations` → 패널은 201 바디로 채움. `placeName`·`latitude`·`longitude` 필수  
목록 카드: `GET /api/v1/reservations/me` → `chatRoomId`로 채팅 이동  
수정: `PATCH /api/v1/reservations/{reservationId}`  
취소: `DELETE /api/v1/reservations/{reservationId}` → 현재 예약은 204
