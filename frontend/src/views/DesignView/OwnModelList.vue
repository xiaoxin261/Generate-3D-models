vue
<template>
    <div class="model-own">
        <div class="own-item-title">
            我的模型
        </div>
        <div class="own-item-content">
            <img :src="item.src" alt="" v-for="item in ownModelList" :key="item.id">
        </div>
    </div>
</template>

<script setup>
import { ref } from 'vue';

const ownModelList = ref([
    { id: 1, src: '/modals/room_1.png' },
    { id: 2, src: '/modals/room_2.png' },
    { id: 3, src: '/modals/room_3.png' },
]);
</script>

<style scoped lang="less">
.model-own {
    width: 100%;
    height: 100%;
    .own-item-title {
        margin-bottom: 10px;
    }
    .own-item-content {
        display: flex;
        flex-wrap: wrap;
        justify-content: space-between;
        align-items: center;
        gap: 15px;
        img {
            width: 45%;
            height: 50%;
        }
    }
}
</style>