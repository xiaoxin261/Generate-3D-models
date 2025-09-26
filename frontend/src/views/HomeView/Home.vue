<template>
  <div class="home">
    <div class="content">
      <h2 class="title">心中所想，我来实现</h2>
      <div class="sub-title">无论您想要什么样的装修效果，</div>
      <div class="sub-title">都可以来这里尝试一下。</div>
      <div class="sub-title">只需简单几步，让所思所想轻松实现。</div>
    </div>
    <router-link to="/design" class="design">开始设计</router-link>
  </div>
</template>

<script setup>

</script>

<style scoped lang="less">
.content {
  position: absolute;
  top: 22%;
  left: 30%;
  width: 648px;
  text-align: center;
  color: #fff;

  .title {
    font-size: 72px;
    font-weight: 700;
    margin-bottom: 20px;
    text-align: left;
    margin-bottom: 60px;
  }

  .sub-title {
    font-size: 30px;
    font-weight: 700;
    line-height: 44px;
    text-align: left;
  }

}

.design {
  position: absolute;
  top: 55%;
  left: 30%;
  width: 194px;
  height: 64px;
  text-align: center;
  background-color: #fff;
  color: #000;
  font-size: 30px;
  font-weight: 700;
  line-height: 64px;
  border-radius: 15px;
  margin-top: 10px;
}
</style>