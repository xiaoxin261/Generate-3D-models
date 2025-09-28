<template>
  <div class="design-container">
    <div class="header">
      <Header @export-models="handleExportModels" />
    </div>
    <div class="content">
      <router-view />
    </div>
  </div>
</template>

<script setup>
import Header from './Header.vue';
import { ref } from 'vue';

// 定义全局事件总线用于组件间通信
const eventBus = ref({
  on: (event, callback) => {
    window.addEventListener(`custom-${event}`, callback);
  },
  emit: (event, data) => {
    window.dispatchEvent(new CustomEvent(`custom-${event}`, { detail: data }));
  }
});

// 处理导出模型事件
const handleExportModels = () => {
  eventBus.value.emit('export-models');
};

// 暴露事件总线给全局使用
window.eventBus = eventBus.value;
</script>

<style scoped lang="less">
@header-height: 60px;
.design-container {
  height: 100%;
  width: 100%;
  .header {
    position: absolute;
    top: 0;
    left: 0;
    width: 100%;
    height: @header-height;
    display: flex;
    justify-content: space-between;
  }
  .content{
    position: absolute;
    top: @header-height;
    left: 0;
    width: 100%;
    height: calc(100% - @header-height);
  }
}
</style>
