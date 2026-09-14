# Bài Kiểm Tra — Khắc Phục Lỗi Hệ Thống Microservices (ĐÃ SỬA)

**Sinh viên:** Nguyễn Thế Kiên — PTIT-HN-232
**Repo gốc:** https://github.com/huongcaoha/microservice-base-project-session08

Đây là bản **dựng lại đầy đủ 4 service từ source gốc**, đã sửa đúng 1 lỗi, KHÔNG đụng config user-service.

---

## 1. Lỗi nằm ở đâu

File `Api-Gateway/src/main/java/org/example/apigateway/config/GatewayRouteConfig.java` định tuyến `/api/users/**` bằng **URI cố định**:

```java
.route("user-route", r -> r
        .path("/api/users/**")
        .uri("http://localhost:8083")   // ❌ SAI
)
```

Nhưng `User-Service` cấu hình `server.port: 0` → **cổng ngẫu nhiên**, nó KHÔNG bao giờ chạy ở 8083. Vì vậy Gateway forward request tới `localhost:8083` → **Connection refused**.

**Vì sao không sửa user-service:** đề cấm, và vì port ngẫu nhiên nên không thể hardcode port. Bắt buộc phải để Gateway định tuyến qua Eureka bằng service-id.

## 2. Đã sửa như thế nào (chỉ 1 dòng, trong Gateway)

```java
.route("user-route", r -> r
        .path("/api/users/**")
        .uri("lb://user-service")   // ✅ ĐÚNG — LoadBalancer + Eureka tự phân giải host:port thật
)
```

`lb://` cần `spring-cloud-starter-loadbalancer` — đã có sẵn trong `Api-Gateway/build.gradle`. Không đụng `application.yml`, không đụng User-Service.

## 3. Chạy hệ thống (macOS + IntelliJ IDEA)

> Lưu ý: thư mục này chỉ có mã nguồn (không kèm `gradlew` binary). Mở bằng **IntelliJ IDEA** — nó tự dùng Gradle bản phù hợp, không cần wrapper.

**Yêu cầu trước:** cài JDK 21, MySQL đang chạy ở `localhost:3306` (user `root`, mật khẩu `12345678` — theo `config-repo/user-service.yml`; nếu MySQL của bạn khác thì sửa trong file đó, KHÔNG sửa trong User-Service).

Mở **4 cửa sổ IntelliJ**, mỗi cửa sổ Open 1 thư mục: `Discovery_Server`, `Config-Server`, `User-Service`, `Api-Gateway`. Chờ Gradle sync xong từng cái.

**Khởi động ĐÚNG THỨ TỰ** (Run class `*Application`):
1. **DiscoveryServerApplication** (8761) → mở `http://localhost:8761` xem Eureka sống.
2. **ConfigServerApplication** (8888) → kiểm tra `http://localhost:8888/user-service/default` trả về JSON config.
3. **UserServiceApplication** → chờ log "registering ... USER-SERVICE" (port ngẫu nhiên).
4. **ApiGatewayApplication** (8080) → chạy cuối.

## 4. Kiểm tra & chụp màn hình nộp bài
- **Ảnh 1** — Eureka Dashboard `http://localhost:8761`: thấy **USER-SERVICE** và **API-GATEWAY** trạng thái **UP**.
- **Ảnh 2** — gọi thành công qua Gateway cổng 8080:
  ```
  GET http://localhost:8080/api/users/1
  ```
  Trả về JSON user (`huongcaoha`, status ACTIVE...). Chụp rõ URL có port 8080 + data.

## 5. Nộp bài
- Clean project (xóa `build/`), nén cả thư mục `SS08_Microservice_Fixed` thành `.zip`.
- Kèm file Word/PDF chứa Ảnh 1 + Ảnh 2.
- Nộp lên LMS đúng hạn.

## 6. Nếu vẫn lỗi
- **503 no instances**: User-Service chưa UP trên Eureka (chờ nó đăng ký xong), hoặc Config-Server (8888) chưa chạy trước User-Service.
- **User-Service không khởi động**: kiểm tra MySQL đang chạy và đúng user/mật khẩu trong `config-repo/user-service.yml`.
- **404 tại /api/users/1**: chưa có user id=1 — User-Service seed sẵn 1 user khi bảng rỗng (id tự tăng từ 1), chạy lại nếu cần.
- **Connection refused 8080**: Api-Gateway chưa chạy hoặc chưa build lại sau khi sửa `GatewayRouteConfig.java`.

---
### Cấu trúc thư mục
```
SS08_Microservice_Fixed/
├── Discovery_Server/     (Eureka 8761)
├── Config-Server/        (8888, native, config-repo/user-service.yml)
├── User-Service/         (port 0 ngẫu nhiên — GIỮ NGUYÊN)
└── Api-Gateway/          (8080 — ĐÃ SỬA GatewayRouteConfig.java)
```
