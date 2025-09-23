<template>
  <div class="model-list">
    <h2>我的模型</h2>
    <div class="model-items">
      <div 
        v-for="(model, index) in models" 
        :key="model.id || index"
        class="model-item"
        draggable="true"
        @dragstart="handleDragStart($event, model)"
      >
        <img :src="model.previewUrl" alt="模型预览" @error="handleImageError($event)">
        <span>{{ model.name }}</span>
        <button @click="removeModel(index)" class="remove-btn">×</button>
      </div>
      
      <div v-if="models.length === 0" class="no-models">
        暂无模型，请先生成3D模型
      </div>
    </div>
    
    <!-- 推荐模型区域 -->
    <h2>推荐模型</h2>
    <div class="recommended-models">
      <div class="no-data">推荐模型接口待开发...</div>
    </div>
  </div>
</template>

<script setup>
import { defineProps, defineEmits } from 'vue';

// 定义props
const props = defineProps({
  models: {
    type: Array,
    default: () => []
  }
});

// 定义emits
const emit = defineEmits(['drag-start', 'remove-model']);

// 拖拽开始
const handleDragStart = (e, model) => {
  e.dataTransfer.setData('model', JSON.stringify(model));
  emit('drag-start', model);
};

// 处理图片加载错误
const handleImageError = (e) => {
  e.target.src = 'https://via.placeholder.com/100/cccccc/999999?text=3D+Model';
};

// 移除模型
const removeModel = (index) => {
  if (confirm('确定要删除这个模型吗？')) {
    emit('remove-model', index);
  }
};
</script>

<style scoped>
.model-list {
  width: 100%;
  height: 100%;
  display: flex;
  flex-direction: column;
}

h2 {
  margin-top: 0;
  margin-bottom: 15px;
  color: #333;
  font-size: 16px;
}

.model-items {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 15px;
  margin-bottom: 30px;
  overflow-y: auto;
  padding-right: 5px;
}

.model-item {
  padding: 10px;
  border: 1px solid #ddd;
  border-radius: 4px;
  cursor: grab;
  transition: all 0.2s;
  text-align: center;
  position: relative;
  background-color: white;
}

.model-item:hover {
  background-color: #f5f5f5;
  border-color: #4285f4;
  transform: translateY(-2px);
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}

.model-item:active {
  cursor: grabbing;
}

.model-item img {
  max-width: 100%;
  height: auto;
  border-radius: 4px;
  margin-bottom: 5px;
}

.model-item span {
  display: block;
  font-size: 12px;
  color: #666;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.remove-btn {
  position: absolute;
  top: 5px;
  right: 5px;
  width: 20px;
  height: 20px;
  padding: 0;
  background-color: #ff4444;
  color: white;
  border: none;
  border-radius: 50%;
  cursor: pointer;
  font-size: 16px;
  line-height: 1;
  opacity: 0.7;
  transition: opacity 0.2s;
}

.remove-btn:hover {
  opacity: 1;
}

.no-models {
  padding: 40px 20px;
  text-align: center;
  color: #999;
  font-style: italic;
  border: 2px dashed #ddd;
  border-radius: 4px;
  background-color: #f9f9f9;
}

.recommended-models {
  padding: 20px;
  background-color: #f9f9f9;
  border-radius: 4px;
  text-align: center;
  flex-shrink: 0;
}

.no-data {
  color: #999;
  font-style: italic;
}

/* 自定义滚动条 */
.model-items::-webkit-scrollbar {
  width: 6px;
}

.model-items::-webkit-scrollbar-track {
  background: #f1f1f1;
  border-radius: 3px;
}

.model-items::-webkit-scrollbar-thumb {
  background: #ccc;
  border-radius: 3px;
}

.model-items::-webkit-scrollbar-thumb:hover {
  background: #999;
}
</style>