# Reservations Service 설정 컨벤션



Discovery / Gateway / Auth-Service 와 동일한 YAML 프로필 구조를 사용합니다.



## YAML 파일 구조



| 파일 | 역할 |

|------|------|

| `application.yml` | 모든 환경 공통 설정 (앱명, JPA, Eureka, springdoc) |

| `application-local.yml` | 로컬 개발 (팀 공통, Git 포함) |

| `application-dev.yml` | 통합/개발 서버 |

| `application-prod.yml` | 운영 서버 |



## 프로필 활성화



```yaml

spring.profiles.active: ${SPRING_PROFILES_ACTIVE:local}

```



- 개인 PC: 기본 `local`

- 통합 검증 노트북: `SPRING_PROFILES_ACTIVE=dev`

- 배포: `SPRING_PROFILES_ACTIVE=prod`



## 포트 및 Eureka



- `server.port: 0` — OS가 할당하는 사용 가능한 랜덤 포트

- Eureka instance-id: `서비스명:실제포트` (예: `reservations-service:51234`) — 기동 로그와 Eureka 대시보드에서 확인

- `eureka.instance.prefer-ip-address: true`



## DB (MSA 스키마 분리)



Auth-Service 와 **동일한 MySQL 인스턴스**를 사용하며, **스키마만 분리**합니다.



| 서비스 | 스키마 |

|--------|--------|

| auth-service | `auth_db` |

| member-service | `member_db` |

| member-regions-service | `member_regions_db` |

| listing-service | `listing_db` |

| reviews-service | `reviews_db` |

| reservations-service | `reservations_db` |



로컬 JDBC URL 예시 (호스트·포트·스키마는 `.env.example` 참고):



```

jdbc:mysql://<host>:<port>/reservations_db?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=Asia/Seoul&characterEncoding=UTF-8

```



DB 사용자명·비밀번호는 `.env` 파일에 설정하세요 (예: `SPRING_DATASOURCE_PASSWORD=your-password-here`).



## Secret 관리 규칙



**YAML 파일에 비밀번호, DB 계정을 직접 작성하지 않습니다.**



| 항목 | local | dev / prod |

|------|-------|------------|

| DB URL / 계정 / 비밀번호 | `.env` | 배포 환경변수 |

| Eureka URL (local) | `application-local.yml` (`localhost:8761`) | `${EUREKA_CLIENT_SERVICEURL_DEFAULTZONE}` |



### 로컬 실행 준비



```bash

cp .env.example .env

# .env 파일에 실제 값 입력

```



개인별 override가 필요하면 Git에 포함되지 않는 `application-local-secret.yml` 을 추가할 수 있습니다.



## 로컬 실행



```bash

# 1. Discovery 실행

cd ../discovery && ./gradlew bootRun



# 2. .env 설정 후 Reservations Service 실행

cd ../reservations-service && ./gradlew bootRun

```



- `bootRun` 은 프로젝트 루트의 `.env` 를 자동 로드합니다.

- IDE에서 실행할 때는 **Run Configuration** `ReservationsServiceApplication` 을 사용하거나, `ReservationsServiceApplication.java` 에서 main 실행 시 Working directory 가 프로젝트 루트인지 확인하세요.

- `application-local.yml` 이 `optional:file:.env[.properties]` 를 import 하므로 IDE main 실행 시에도 `.env` 가 로드됩니다.



Eureka Dashboard (`http://localhost:8761`) 에서 `reservations-service` 등록을 확인합니다.



## 필수 환경변수 (local)



| 변수 | 설명 |

|------|------|

| `SPRING_DATASOURCE_URL` | MySQL JDBC URL (`reservations_db` 스키마) |

| `SPRING_DATASOURCE_USERNAME` | DB 사용자 |

| `SPRING_DATASOURCE_PASSWORD` | DB 비밀번호 |



## 필수 환경변수 (dev / prod)



| 변수 | 설명 |

|------|------|

| `SPRING_DATASOURCE_URL` | MySQL JDBC URL (`reservations_db` 스키마) |

| `SPRING_DATASOURCE_USERNAME` | DB 사용자 |

| `SPRING_DATASOURCE_PASSWORD` | DB 비밀번호 |

| `EUREKA_CLIENT_SERVICEURL_DEFAULTZONE` | Eureka Server URL |



## 패키지 구조 (Hexagonal)



```

com.sparta.reservations_service/

├── domain/model/              # 도메인 모델

├── application/

│   ├── service/               # Use Case 구현

│   ├── exception/

│   └── port/

│       ├── in/ (+ dto/)       # 인바운드 포트

│       └── out/               # 아웃바운드 포트

├── adaptor/

│   ├── in/web/                # REST API (Controller, VO, Mapper)

│   └── out/mysql/             # JPA (Entity, Repository, Adapter)

└── config/                    # 인프라 설정

```



엔티티·도메인·비즈니스 로직은 기능 개발 시 추가합니다.
