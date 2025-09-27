import { defineStore } from 'pinia'
import { login, register, refreshTokenApi, logout } from '@/api/auth'

const useAuthStore = defineStore('auth', {
  state: () => ({
    accessToken: localStorage.getItem('accessToken') || null,
    refreshToken: localStorage.getItem('refreshToken') || null,
    userInfo: null,
  }),
  getters:{
    isLoggedIn: (state) => state.accessToken !== null,
  },
  actions: {
    setTokens(tokens) {
      this.accessToken = tokens.accessToken
      this.refreshToken = tokens.refreshToken
      localStorage.setItem('accessToken', tokens.accessToken)
      localStorage.setItem('refreshToken', tokens.refreshToken)
    },

    async login(loginForm) {
      const response = await login(loginForm);
      if (response.code === 0) {
        this.setTokens(response.data);
        // 登录成功后可以顺便获取用户信息
        // await this.getUserInfo();
        return true;
      }
      return false;
    },

    async register(registerForm) {
      const response = await register(registerForm);
      return response.code === 0;
    },

    async refreshToken() {
      if (!this.refreshToken) {
        throw new Error('No refresh token available');
      }
      try {
        const response = await refreshTokenApi({ refreshToken: this.refreshToken });
        if (response.code === 0) {
          this.setTokens(response.data);
          return response.data.accessToken;
        }
        // 刷新失败，说明 refreshToken 也失效了
        this.logout();
        throw new Error('Refresh token expired');
      } catch (error) {
        this.logout();
        throw error;
      }
    },

    logout() {
      // 调用后端登出接口（可选，但推荐）
      logout().catch(() => {}); 
      
      // 清除 state 和 localStorage
      this.accessToken = null;
      this.refreshToken = null;
      this.userInfo = null;
      localStorage.removeItem('accessToken');
      localStorage.removeItem('refreshToken');
    },
  }
});

export default useAuthStore;