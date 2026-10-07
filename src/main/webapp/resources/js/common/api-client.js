/* api 클라이언트 툴킷 */
const api = {
    /**
     * 공통 GET 요청
     * @param {string} url - 요청할 API 주소 (예: '/api/todo/list?userId=101')
     */
    async get(url) {
        try {
            const response = await fetch(url, {
                method: 'GET',
                headers: {
                    'Accept': 'application/json'
                }
            });

            // HTTP 통신 에러 체크 (404, 500 등)
            if (!response.ok) {
                throw new Error(`HTTP error! status: ${response.status}`);
            }

            return await response.json();
        } catch (error) {
            console.error("[API GET Error]:", error);
            // 에러가 나도 프론트가 멈추지 않도록 안전한 기본 포맷 반환
            return { status: "error", message: error.message, data: null };
        }
    },

    /**
     * 공통 POST 요청 (데이터 생성 및 전송)
     * @param {string} url - 요청할 API 주소
     * @param {Object} data - 서버로 보낼 자바스크립트 객체 (자동으로 JSON 변환됨)
     */
    async post(url, data = {}) {
        try {
            const response = await fetch(url, {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json',
                    'Accept': 'application/json'
                },
                body: JSON.stringify(data) // 객체를 JSON 문자열로 변환
            });

            if (!response.ok) {
                throw new Error(`HTTP error! status: ${response.status}`);
            }

            return await response.json();
        } catch (error) {
            console.error("[API POST Error]:", error);
            return { status: "error", message: error.message, data: null };
        }
    }
};