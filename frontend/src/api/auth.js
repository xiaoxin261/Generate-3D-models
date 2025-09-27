import request from '@/utils/request';

/**
 * 用户登录
 * @param {object} data - { username, password }
 */
export function login(data) {
  return request({
    url: '/auth/login',
    method: 'post',
    data,
  });
}

/**
 * 用户注册
 * @param {object} data - { username, email, password, etc. }
 */
export function register(data) {
  return request({
    url: '/auth/register',
    method: 'post',
    data,
  });
}

/**
 * 刷新令牌
 * @param {object} data - { refreshToken }
 */
export function refreshToken(data) {
  return request({
    url: '/auth/refresh',
    method: 'post',
    data,
  });
}

/**
 * 用户登出
 */
export function logout() {
  return request({
    url: '/auth/logout',
    method: 'post',
  });
}