import request from '@/utils/request';

/**
 * 获取用户个人信息
 */
export function getUserProfile() {
  return request({
    url: '/user/profile',
    method: 'get',
  });
}

/**
 * 更新用户个人信息
 * @param {object} data - 用户个人信息
 */
export function updateUserProfile(data) {
  return request({
    url: '/user/profile',
    method: 'put',
    data,
  });
}

/**
 * 更新用户密码
 * @param {object} data - { oldPassword, newPassword }
 */
export function updateUserPassword(data) {
  return request({
    url: '/user/change-password',
    method: 'post',
    data,
  });
}

/**
 * 获取用户信息
 */
export function getUserInfo() {
  return request({
    url: '/user/info',
    method: 'get',
  });
}
