// 로그아웃
async function handleLogout() {
    try {
        const pathSegments = window.location.pathname.split('/');
        const contextPath = pathSegments[1] ? `/${pathSegments[1]}` : '';

        const res = await api.post(`${contextPath}/auth/logout`);

        window.location.href = `${contextPath}/`;
    } catch (error) {
        console.error('로그아웃 처리 실패:', error);
        window.location.href = '/sangsae/';
    }
}