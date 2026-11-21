# BTH5 – Dịch vụ báo cáo theo kiến trúc SOA

Dịch vụ thống kê **hàng tồn, hàng bán, doanh thu, chi phí và lợi nhuận**, có báo cáo theo **sản phẩm** và **đơn hàng**. Java 21, Spring Boot 3.5.7, MySQL 8, REST API.

## 1. Kiến trúc

| Dịch vụ | Cổng mặc định | Database |
|---|---:|---|
| BTH2 – Xác thực JWT | 8080 | Riêng của BTH2 |
| BTH3 – Sản phẩm | 8081 | BTH3_PRODUCT |
| BTH4 – Đơn hàng | 8082 | BTH4_ORDER |
| **BTH5 – Báo cáo** | **8083** | **BTH5_REPORT** |

BTH5 chỉ kết nối database của mình, không truy vấn database sản phẩm hay đơn hàng. Dữ liệu được lấy qua HTTP: `GET /products` và `GET /orders`. JWT của người dùng được xác thực qua `GET /auth` ở BTH2 rồi chuyển tiếp tới BTH3/BTH4.

BTH5 tương thích với repo `buiminhquoc2004-collab/BTH4-Order-Service`: trạng thái `pending/completed/cancelled`, thành tiền chi tiết `totalPrice`. Đồng thời chấp nhận trạng thái chữ hoa và tên trường `subtotal` ở các bản BTH4 khác.

## 2. Công thức và quy tắc

| Chỉ tiêu | Cách tính |
|---|---|
| Tồn kho | Tổng `quantity` hiện tại do Product Service trả về |
| Hàng bán | Tổng số lượng trong đơn `completed` |
| Doanh thu | Tổng thành tiền chi tiết các đơn `completed` |
| Chi phí giá vốn | Tổng `số lượng bán × unitCost` |
| Lợi nhuận gộp | Doanh thu − chi phí giá vốn |

- Đơn pending, confirmed hoặc cancelled vẫn xuất hiện trong báo cáo đơn, nhưng không tính doanh thu, hàng bán hay giá vốn. `orderValue` là giá trị đơn, khác với doanh thu được ghi nhận.
- Giá bán lấy từ chi tiết đơn hàng đã lưu, không lấy giá bán hiện tại của sản phẩm.
- Giá vốn do BTH5 quản lý trong bảng `product_costs`. Cần nhập cho tất cả sản phẩm đã bán. Thiếu giá vốn trả **422**, không tự gán bằng 0.
- Tồn kho hiển thị đúng dữ liệu BTH3, không trừ số lượng đã bán thêm lần nữa. Nếu BTH4 chưa có chức năng trừ kho thì BTH5 vẫn báo tồn theo BTH3.
- Sản phẩm đã bị xóa khỏi BTH3 nhưng có trong đơn cũ vẫn được thống kê; tồn kho của sản phẩm đó là `null`, `inCatalog=false`.
- Mỗi lần tạo là một snapshot mới. Lịch sử đã lưu không đổi khi giá vốn hoặc dữ liệu nguồn thay đổi. Không cộng các snapshot với nhau vì chúng có thể chứa cùng một đơn hàng.
- Giá vốn áp dụng là giá vốn tại lúc tạo báo cáo; chưa phải lịch sử giá vốn tại thời điểm bán. Lợi nhuận là **lợi nhuận gộp**, chưa bao gồm thuế, lương, vận chuyển và chi phí vận hành.

## 3. Chạy trong Spring Tool Suite / Eclipse

1. Chuẩn bị JDK 21, MySQL 8 và STS/Eclipse có Maven.
2. MySQL Workbench: mở **database.sql**, chạy toàn bộ để tạo `BTH5_REPORT` và 5 bảng.
3. STS: **File → Import → Maven → Existing Maven Projects**.
4. Chọn thư mục `BTH5-Report-Service` có `pom.xml` → Finish.
5. Chuột phải project → **Maven → Update Project**. JRE System Library phải dùng JavaSE-21.
6. **Run → Run Configurations → Spring Boot App**; chọn `Bth5ReportApplication`.
7. Tab **Environment**, thêm các biến sau:

| Biến | Giá trị |
|---|---|
| `DB_USER` | `root` hoặc tài khoản MySQL của bạn |
| `DB_PASSWORD` | Mật khẩu MySQL của bạn |
| `REPORT_DB_URL` | Tùy chọn, URL JDBC MySQL nếu không dùng localhost:3306/BTH5_REPORT |
| `AUTH_SERVICE_URL` | Mặc định `http://localhost:8080` |
| `PRODUCT_SERVICE_URL` | Mặc định `http://localhost:8081` |
| `ORDER_SERVICE_URL` | Mặc định `http://localhost:8082` |
| `SERVER_PORT` | Mặc định `8083` |

8. Chạy BTH2, BTH3, BTH4, sau đó chạy `Bth5ReportApplication`.
9. Đăng nhập BTH2 để lấy JWT; gửi token bằng Postman ở mọi request BTH5.

Nếu dùng IntelliJ: mở thư mục có `pom.xml`, chọn Project SDK 21, Maven Reload, rồi thêm các biến Environment trong Run Configuration.

Chạy bằng Maven sau khi đặt biến môi trường:

```bash
mvn clean verify
mvn spring-boot:run
```

Chạy file JAR sau build:

```bash
java -jar target/bth5-report-service-1.0.0.jar
```

## 4. API

Base URL: `http://localhost:8083`. Mọi request cần header:

```http
Authorization: Bearer <token từ BTH2>
```

| Method | Endpoint | Chức năng |
|---|---|---|
| PUT | `/costs/{productId}` | Nhập/cập nhật giá vốn |
| GET | `/costs` | Danh sách giá vốn |
| POST | `/reports/generate` | Lấy dữ liệu BTH3/BTH4 và lưu snapshot |
| GET | `/reports` | Lịch sử báo cáo tổng hợp |
| GET | `/reports/latest` | Báo cáo mới nhất gồm tổng hợp và chi tiết |
| GET | `/reports/{id}` | Xem một snapshot đã lưu |
| GET | `/reports/products` | Báo cáo theo sản phẩm của snapshot mới nhất |
| GET | `/reports/orders` | Báo cáo theo đơn hàng của snapshot mới nhất |
| GET | `/reports/{id}/products` | Chi tiết sản phẩm theo snapshot |
| GET | `/reports/{id}/orders` | Chi tiết đơn hàng theo snapshot |

Các endpoint cũng có alias `/api/...`.

Mã lỗi: **400** input sai; **401** token thiếu/không hợp lệ; **404** không có báo cáo; **422** thiếu giá vốn hàng bán; **502** dữ liệu nguồn không hợp lệ; **503** dịch vụ nguồn hoặc xác thực không truy cập được.

## 5. Demo bằng Postman

Import `BTH5-Report-Service.postman_collection.json`. Điền collection variables `token`, `productId`, `unitCost`; thay ID theo dữ liệu thật. Có thể dùng `api-tests.http` trong IDE.

Ví dụ sản phẩm BTH3 có ID 1, giá 100.000, tồn 10. Tạo đơn ở BTH4:

```http
POST http://localhost:8082/orders
Content-Type: application/json
Authorization: Bearer <token>
```

```json
{"customerName":"Nguyen Van A","customerEmail":"a@example.com","items":[{"productId":1,"quantity":2}]}
```

Dùng ID đơn vừa tạo, chuyển sang hoàn thành:

```http
PUT http://localhost:8082/orders/1
```

```json
{"status":"completed"}
```

Nhập giá vốn sản phẩm:

```http
PUT http://localhost:8083/costs/1
```

```json
{"unitCost":60000}
```

Tạo báo cáo, không cần body:

```http
POST http://localhost:8083/reports/generate
```

Nếu nguồn chỉ có đơn hoàn thành trên, kết quả minh họa:

| Hàng bán | Doanh thu | Giá vốn | Lợi nhuận gộp |
|---:|---:|---:|---:|
| 2 | 200.000 | 120.000 | 80.000 |

Tồn kho lấy trực tiếp từ BTH3: nếu BTH3 báo 10 thì tồn là 10, nếu đã trừ và báo 8 thì tồn là 8.

Xem `/reports/products`, `/reports/orders`, `/reports/latest`. Gọi lại GET không tạo snapshot mới. Muốn cập nhật số liệu nguồn thì gọi POST generate.

## 6. Database riêng

```sql
USE BTH5_REPORT;
SELECT * FROM product_costs;
SELECT * FROM report_runs ORDER BY id DESC;
SELECT * FROM report_product_rows;
SELECT * FROM report_order_rows;
SELECT * FROM report_order_item_rows;
```

`report_runs` lưu tổng hợp; hai bảng product/order lưu chi tiết theo snapshot; bảng item lưu các dòng của đơn. Chỉ có khóa ngoại nội bộ trong BTH5_REPORT.

## 7. Kiểm thử

Đã build thành công và đạt **12/12 test**, không lỗi, không bỏ qua, ngày 21/11/2026.

```bash
mvn clean verify
```

- `ReportMathTest`: công thức, loại đơn chưa bán khỏi doanh thu, thiếu giá vốn, sản phẩm đã xóa và tổng tiền nguồn sai.
- `ReportApiIntegrationTest`: controller, validation và lưu/đọc dữ liệu bằng H2, mock các API nguồn.
- `ReportHttpTest`: chạy server BTH5 thật và các HTTP stub cho BTH2/BTH3/BTH4. Kiểm tra JWT, chuyển tiếp token, trạng thái chữ thường, `totalPrice`, lưu snapshot, thiếu giá vốn và lỗi upstream.

Test dùng H2 riêng, không cần mật khẩu MySQL hay các dịch vụ thật. Chạy test không thay thế kiểm thử với MySQL và bộ BTH2/BTH3/BTH4 trên máy bạn.

## 8. Phạm vi bài thực hành

Báo cáo tổng hợp toàn bộ dữ liệu nguồn tại lúc tạo, chưa lọc theo khoảng ngày. Dữ liệu hai service được đọc lần lượt, không phải snapshot giao dịch phân tán đồng thời. API yêu cầu JWT hợp lệ nhưng chưa có phân quyền chức danh kế toán/quản trị. Cần bổ sung lịch sử giá vốn theo giao dịch và cơ chế đồng bộ nếu dùng trong thực tế.
