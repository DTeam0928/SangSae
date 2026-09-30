# 프로젝트 공통 API 명세 템플릿

### **📌 [기능 이름]**
- **Method / Endpoint**: `HTTP_METHOD` / `/api/...`
- **Domain**: `DOMAIN_NAME`
- **설명**: 해당 기능에 대한 간단한 설명

---

### **📥 Request Payload (요청)**

```json
{
  "command": "COMMAND_NAME",
  "data": {
    "key1": "value1",
    "key2": "value2"
  }
}
```

#### **`data` 객체 필드 상세**

| **Field Key** | **Type** | **Nullable** | **필수여부** | **Description** | **Example** |
| :--- | :--- | :---: | :---: | :--- | :--- |
| `key1` | `String` | N | 필수 | 필드 설명 | `"value"` |
| `key2` | `String` | N | 필수 | 필드 설명 | `"value2"` |

---

### **📤 Response Payload (응답)**

#### **🟢 200 OK (성공)**

```json
{
  "command": "COMMAND_NAME",
  "success": true,
  "code": 200,
  "message": "성공 메시지",
  "data": {
    "resultKey": "resultValue"
  }
}
```

#### **🔴 400 / 401 / 500 Error (실패)**

```json
{
  "command": "COMMAND_NAME",
  "success": false,
  "code": 400,
  "message": "오류 관련 메시지",
  "data": null
}
```

---

### **🚨 Status & Error Code (상태 코드 표)**

| **Code** | **Status** | **Message** | **발생 원인 / 비고** |
| :--- | :--- | :--- | :--- |
| `200` | SUCCESS | 성공 | 요청 처리 완료 |
| `400` | BAD_REQUEST | 파라미터 누락 / 유효하지 않은 데이터 | 필수 필드가 없거나 형식 오류 |
| `401` | UNAUTHORIZED | 인증 실패 | 세션 만료 또는 권한 없음 |
| `500` | INTERNAL_ERROR | 서버 내부 오류 | DB 오류 또는 서버 예외 발생 |
