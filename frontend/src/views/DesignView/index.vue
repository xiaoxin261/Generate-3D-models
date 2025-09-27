<template>
  <div class="design-container">
    <div class="header">
      <Header />
    </div>
    <div class="content">
      <router-view />
    </div>
  </div>
</template>

<script setup>
import Header from './Header.vue';
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
