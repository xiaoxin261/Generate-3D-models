<template>
  <div class="app-container">
    <div class="scene-container">
      <ThreeScene ref="threeSceneRef" :room-params="roomParams" :current-drag-model="currentDragModel"
        :room-model-url="roomModelUrl" @export-models="showExportDialog = true" />
    </div>
    <div class="tab">
      <div class="item" v-for="item in tabList" :key="item.id" @click="currentIndex = item.id">
        <img :class="{ 'active': item.id === currentIndex }"
          :src="item.id === currentIndex ? item.selectedSrc : item.src" alt="">
        <div>{{ item.name }}</div>
      </div>
    </div>
    <div class="model-generate-section">
      <RoomGenerator v-show="currentIndex === 0"/>
      <ModelGenerator v-show="currentIndex === 1" :is-generating="isGenerating" :progress="progress" @generate="handleGenerateModel" />
    </div>
    <!-- <div class="room-generate-section">
      <RoomGenerator />
      <button v-loading.fullscreen.lock="fullscreenLoading" type="primary" @click="generateModel(1)">生成模型</button>
    </div> -->
  </div>

</template>

<script setup>
import { ref, onMounted } from 'vue';
import ModelGenerator from './ModelGenerator.vue';
import ThreeScene from './ThreeScene.vue';
import RoomGenerator from './RoomGenerator.vue';

const currentIndex = ref(0);
const tabList = ref([
  { id: 0, name: '房间', src: '/images/icon_room.png', selectedSrc: '/images/icon_room_selected.png' },
  { id: 1, name: '装修', src: '/images/icon_design.png', selectedSrc: '/images/icon_design_selected.png' },
  { id: 2, name: '灵感库', src: '/images/icon_source.png', selectedSrc: '/images/icon_source_selected.png' },
  { id: 3, name: '我的', src: '/images/icon_mine.png', selectedSrc: '/images/icon_mine_selected.png' },
]);

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

const fullscreenLoading = ref(false);

// 生成房间模型
const generateRoomModel = async (scale) => {
  try {
    // 在实际应用中，这里应该调用API生成房间模型
    // 这里使用模拟数据，假设有一个OBJ格式的房间模型和对应的MTL纹理文件
    // 注意：在真实环境中，这里应该是有效的URL
    const mockRoomModelUrl = '/modals/mock.obj';
    const mockRoomMtlUrl = '/modals/mock.mtl';

    roomModelUrl.value = mockRoomModelUrl;

    console.log('房间模型URL生成成功:', mockRoomModelUrl);
    console.log('房间模型MTL URL生成成功:', mockRoomMtlUrl);

    // 如果ThreeScene组件已经挂载，可以立即加载房间模型
    if (threeSceneRef.value && roomModelUrl.value) {
      // 直接调用ThreeScene的方法加载房间模型，并传递MTL文件路径
      if (typeof threeSceneRef.value.loadRoomModel === 'function') {
        threeSceneRef.value.loadRoomModel(mockRoomModelUrl, mockRoomMtlUrl, scale);
      }
      console.log('准备加载房间模型');
    }
  } catch (error) {
    console.error('生成房间模型失败:', error);
  }
};

const generateModel = async (scale) => {
  try {
    // 在实际应用中，这里应该调用API生成普通模型
    // 这里使用模拟数据，假设有一个OBJ格式的普通模型和对应的MTL纹理文件
    // 注意：在真实环境中，这里应该是有效的URL
    fullscreenLoading.value = true;
    console.log('生成模型按钮点击');
    setTimeout(() => {
      fullscreenLoading.value = false;
    }, 2000)
    const mockModelUrl = '/modals/mock.obj';
    const mockModelMtlUrl = '/modals/mock.mtl';

    console.log('普通模型URL生成成功:', mockModelUrl);
    console.log('普通模型MTL URL生成成功:', mockModelMtlUrl);

    // 如果ThreeScene组件已经挂载
    if (threeSceneRef.value) {
      // 直接调用ThreeScene的方法加载普通模型
      if (typeof threeSceneRef.value.loadSimpleModel === 'function') {
        console.log('准备加载普通模型');
        // 注意：loadSimpleModel方法目前不支持直接传递MTL文件，我们需要修改它
        threeSceneRef.value.loadSimpleModel(mockModelUrl, scale, mockModelMtlUrl);
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
  // generateRoomModel(10);
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

<style scoped lang="less">
.app-container {
  position: relative;
  height: 100%;
  box-sizing: border-box;
  background-color: #f5f5f5;

  .tab {
    position: absolute;
    width: 70px;
    height: 100%;
    top: 0;
    padding-top: 20px;
    background-color: #f5f5f5;
    display: flex;
    flex-direction: column;
    justify-content: start;
    align-items: center;
    gap: 40px;

    .item {
      width: 100%;
      height: 50px;
      text-align: center;
      cursor: pointer;

      img {
        width: 40px;
        height: 40px;
        padding: 7px;
      }

      .active {
        background-color: #000;
        border-radius: 50%;
      }

      div {
        font-size: 18px;
        line-height: 15px;
      }
    }

  }

  .model-generate-section {
    position: absolute;
    top: 0;
    left: 70px;
    padding: 20px;
    background-color: white;
    width: 15%;
    height: 100%;
    border-radius: 16px;
    box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
  }

  .scene-container {
    background-color: white;
    border-radius: 16px;
    box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
  }
}



.room-generate-section {
  position: absolute;
  top: 0;
  right: 10px;
  padding: 20px;
  background-color: white;
  width: 15%;
  height: 100%;
  border-radius: 16px;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
}
</style>