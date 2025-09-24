<template>
  <div class="app-container">

    <!-- 中央3D场景区域 -->
    <div class="scene-container">
      <ThreeScene ref="threeSceneRef" :room-params="roomParams" :current-drag-model="currentDragModel"
        :room-model-url="roomModelUrl"
        @export-models="showExportDialog = true" />
    </div>
    <!-- 左侧模型生成区域 -->
    <div class="model-generate-section">
      <ModelGenerator :is-generating="isGenerating" :progress="progress" @generate="handleGenerateModel" />
    </div>
    <!-- 右侧房间参数设置区域 -->
    <div class="room-generate-section">
      <RoomGenerator />
      <button @click="generateModel(1)">生成模型</button>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue';
import ModelGenerator from './components/ModelGenerator.vue';
import ThreeScene from './components/ThreeScene.vue';
import RoomGenerator from './components/RoomGenerator.vue';

// 生成状态
const isGenerating = ref(false);
const progress = ref(0);

// 模型列表
const models = ref([]);
const currentDragModel = ref(null);

// 房间参数
const roomParams = ref({
  length: 5,
  width: 5,
  height: 3,
  style: 'modern'
});

// 房间模型URL
const roomModelUrl = ref(null);

// 导出相关
const showExportDialog = ref(false);

// 组件引用
const threeSceneRef = ref(null);

// 生成房间模型
const generateRoomModel = async (scale) => {
  try {
    // 在实际应用中，这里应该调用API生成房间模型
    // 这里使用模拟数据，假设有一个OBJ格式的房间模型
    // 注意：在真实环境中，这里应该是一个有效的URL
    const mockRoomModelUrl = '/modals/mock.obj';
    
    roomModelUrl.value = mockRoomModelUrl;
    
    console.log('房间模型URL生成成功:', mockRoomModelUrl);
    
    // 如果ThreeScene组件已经挂载，可以立即加载房间模型
    if (threeSceneRef.value && roomModelUrl.value) {
      // 直接调用ThreeScene的方法加载房间模型
      if (typeof threeSceneRef.value.loadRoomModel === 'function') {
        threeSceneRef.value.loadRoomModel(mockRoomModelUrl, scale);
      }
      console.log('准备加载房间模型');
    }
  } catch (error) {
    console.error('生成房间模型失败:', error);
  }
};

const generateModel = async (scale) => {
  try {
    // 在实际应用中，这里应该调用API生成房间模型
    // 这里使用模拟数据，假设有一个OBJ格式的房间模型
    // 注意：在真实环境中，这里应该是一个有效的URL
    const mockModelUrl = '/modals/mock.obj';
    
    console.log('房间模型URL生成成功:', mockModelUrl);
    
    // 如果ThreeScene组件已经挂载
    if (threeSceneRef.value) {
      // 直接调用ThreeScene的方法加载普通模型
      if (typeof threeSceneRef.value.loadSimpleModel === 'function') {
        console.log('准备加载普通模型');
        threeSceneRef.value.loadSimpleModel(mockModelUrl, scale);
      }
    }
  } catch (error) {
    console.error('生成普通模型失败:', error);
  }
};

// 监听房间参数变化，重新生成房间模型
const watchRoomParams = () => {
  // 在实际应用中，这里可以使用watch API监听roomParams的变化
  // 这里简化处理，直接提供一个方法供RoomGenerator调用
  generateRoomModel();
};

// 暴露给子组件的方法
defineExpose({
  watchRoomParams
});

// 处理模型生成
const handleGenerateModel = (params) => {
  if (isGenerating.value) return;

  isGenerating.value = true;
  progress.value = 0;

  // 根据参数调用腾讯混元API
  callHunyuanAPI(params);

  // 模拟API调用过程
  simulateModelGeneration(params);
};

// 生命周期钩子
onMounted(() => {
  // 组件挂载后，生成房间模型
  generateRoomModel(10);
});

// 调用腾讯混元API
const callHunyuanAPI = (params) => {
  // 腾讯混元API调用实现
  console.log('调用腾讯混元API');
  console.log('参数:', params);

  // 实际环境中的API调用代码（注释部分）
};

// 轮询任务状态
const pollJobStatus = (jobId) => {
  console.log('轮询任务状态:', jobId);

  // 实际环境中的轮询代码（注释部分）
};

// 模拟模型生成过程
const simulateModelGeneration = (params) => {
  // 模拟进度更新
  const interval = setInterval(() => {
    progress.value += 5;

    if (progress.value >= 100) {
      clearInterval(interval);
      isGenerating.value = false;

      // 添加到模型列表
      const newModel = {
        id: Date.now(),
        name: params.prompt || 'Generated Model',
        previewUrl: 'https://via.placeholder.com/100',
        modelUrl: '',
        format: params.resultFormat
      };

      models.value.push(newModel);


    }
  }, 200);
};

// 处理生成的模型
const handleGeneratedModel = (resultFile, params) => {
  const newModel = {
    id: Date.now(),
    name: params.prompt || 'Generated Model',
    previewUrl: resultFile.PreviewImageUrl,
    modelUrl: resultFile.Url,
    format: resultFile.Type
  };

  models.value.push(newModel);


};

// 拖拽开始
const handleDragStart = (model) => {
  currentDragModel.value = model;

  // 监听拖拽结束事件
  setTimeout(() => {
    const handleDragEnd = () => {
      currentDragModel.value = null;
      document.removeEventListener('dragend', handleDragEnd);
    };
    document.addEventListener('dragend', handleDragEnd);
  }, 0);
};

// 移除模型
const removeModel = (index) => {
  models.value.splice(index, 1);
};

// 确认导出
const confirmExport = (format) => {
  console.log(`导出模型为${format}格式`);
  closeExportDialog();

  // 模拟导出成功
  setTimeout(() => {
    alert(`模型已成功导出为${format}格式`);
  }, 500);
};

// 关闭导出对话框
const closeExportDialog = () => {
  showExportDialog.value = false;
};
</script>

<style scoped>
.app-container {
  position: relative;
  height: 100vh;
  box-sizing: border-box;
  background-color: #f5f5f5;
}

.model-generate-section {
  position: absolute;
  top: 20px;
  left: 20px;
  padding: 20px;
  background-color: white;
  width: 15%;
  height: 95vh;
  border-radius: 8px;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
}

.room-generate-section {
  position: absolute;
  top: 20px;
  right: 20px;
  padding: 20px;
  background-color: white;
  width: 15%;
  height: 95vh;
  border-radius: 8px;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
}

.scene-container {
  background-color: white;
  border-radius: 8px;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
}
</style>