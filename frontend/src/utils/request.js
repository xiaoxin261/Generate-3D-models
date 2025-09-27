import axios from 'axios';
import useAuthStore from '../stores/auth';
const instance = axios.create({
  baseURL: 'http://localhost:8081',
  timeout: 100000,
  headers: {
    'Content-Type': 'application/json',
  },
});
instance.interceptors.request.use(function (config) {
  // const authStore = useAuthStore();
  // // 如果用户已登录，且请求不是去刷新Token的，就为请求添加 Authorization 头
  // if (authStore.isLoggedIn && config.url !== '/auth/refresh') {
  //   config.headers['Authorization'] = `Bearer ${authStore.accessToken}`;
  // }
  return config;
}, function (error) {
  return ElMessage.error('发送失败');
});

// 添加响应拦截器
instance.interceptors.response.use(function (response) {
  return response;
}, async (error) => {
  const originalRequest = error.config;

  // --- Token 自动刷新逻辑 ---
  // 如果是 401 Unauthorized 错误，并且不是刷新Token的请求，并且没有正在尝试刷新
  if (error.response?.status === 401 && originalRequest.url !== '/auth/refresh' && !originalRequest._retry) {
    originalRequest._retry = true; // 标记为正在重试，防止无限循环

    const authStore = useAuthStore();
    try {
      // 尝试刷新 Token
      const newAccessToken = await authStore.refreshToken();

      // 刷新成功，用新 Token 重新发起原请求
      originalRequest.headers['Authorization'] = `Bearer ${newAccessToken}`;
      return instance(originalRequest);
    } catch (refreshError) {
      // 刷新 Token 失败，说明登录已过期，需要重新登录
      console.error('Token refresh failed, redirecting to login page.', refreshError);
      // 这里可以添加路由跳转，比如跳转到登录页
      // router.push('/login');
      return Promise.reject(refreshError);
    }
  }

  return Promise.reject(error);
});
export default instance;
