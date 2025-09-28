// src/api/model.js
import request from '@/utils/request';

/**
 * 获取表单配置
 */
export function getFormConfig() {
  return request({
    url: '/api/v1/models/form-config',
    method: 'get',
  });
}

/**
 * 生成3D模型
 * @param {object} data - { text, style, dimensions, etc. }
 */
export function generateModel(data) {
  return request({
    url: '/api/v1/models/generate',
    method: 'post',
    data,
  });
}

/**
 * 图片生成3D模型
 * @param {FormData} formData - 包含图片文件和其他参数的 FormData 对象
 */
export function generateModelFromImage(formData) {
  return request({
    url: '/api/v1/models/generate-from-image',
    method: 'post',
    data: formData,
    headers: {
      'Content-Type': 'multipart/form-data', // 上传文件必须是这个类型
    },
  });
}

/**
 * 获取3D模型风格选项
 */
export function getModelStyles() {
  return request({
    url: '/api/v1/models/styles',
    method: 'get',
  });
}

/**
 * 获取3D模型表单配置
 */
export function getModelFormConfig() {
  return request({
    url: '/api/v1/models/form-config',
    method: 'get',
  });
}

/**
 * 获取3D模型尺寸推荐
 */
export function getModelDimensionRecommendation(data) {
  return request({
    url: '/api/v1/models/dimension-recommendation',
    method: 'get',
    data
  });
}
