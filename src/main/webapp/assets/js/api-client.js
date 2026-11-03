/* Preserve existing get/post/postForm interfaces. Business JSON is returned intact. */
const api = {
    async request(url, options = {}) {
        try {
            const response = await fetch(url, {
                ...options, credentials: 'same-origin',
                headers: { Accept: 'application/json', 'X-Requested-With': 'XMLHttpRequest', ...(options.headers || {}) }
            });
            const isJson = (response.headers.get('content-type') || '').toLowerCase().includes('json');
            if (!isJson) return { success: false, status: 'error', httpStatus: response.status,
                message: '서버가 JSON 응답을 반환하지 않았습니다.', data: null };
            const result = await response.json();
            if (!response.ok) return { ...result, success: false, status: 'error',
                httpStatus: response.status, message: result?.message || `HTTP ${response.status}` };
            return result;
        } catch (error) {
            return { success: false, status: 'error', message: '서버에 연결할 수 없습니다.', data: null };
        }
    },
    get(url) { return this.request(url, { method: 'GET' }); },
    post(url, data = {}) { return this.request(url, { method: 'POST',
        headers: { 'Content-Type': 'application/json' }, body: JSON.stringify(data) }); },
    postForm(url, data = {}) {
        const body = new URLSearchParams();
        for (const [key, value] of Object.entries(data)) {
            for (const item of Array.isArray(value) ? value : [value]) {
                if (item != null) body.append(key, String(item));
            }
        }
        return this.request(url, { method: 'POST', body });
    }
};
