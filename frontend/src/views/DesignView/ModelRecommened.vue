<template>
    <div class="model-recommened">
        <div class="recommened-item" v-for="recommenedItem in recommendList" :key="recommenedItem.title">
            <div class="recommened-item-title">
                {{ recommenedItem.title }}
            </div>
            <div class="recommened-item-content">
                <img :src="item.src" alt="" v-for="item in recommenedItem.content" :key="item.id">
            </div>
        </div>
    </div>
</template>

<script setup>
import { ref } from 'vue';

const recommendList = ref([
    {
        title: '模型推荐',
        content: [
            { id: 0, src: '/modals/model_1.png' },
            { id: 1, src: '/modals/model_2.png' },
            { id: 2, src: '/modals/model_3.png' },
        ]
    },
    {
        title: '房间推荐',
        content: [
            { id: 0, src: '/modals/room_1.png' },
            { id: 1, src: '/modals/room_2.png' },
            { id: 2, src: '/modals/room_3.png' },
        ]
    }, {
        title: '模型推荐',
        content: [
            { id: 0, src: '/modals/model_1.png' },
            { id: 1, src: '/modals/model_2.png' },
            { id: 2, src: '/modals/model_3.png' },
        ]
    },
    {
        title: '房间推荐',
        content: [
            { id: 0, src: '/modals/room_1.png' },
            { id: 1, src: '/modals/room_2.png' },
            { id: 2, src: '/modals/room_3.png' },
        ]
    }, {
        title: '模型推荐',
        content: [
            { id: 0, src: '/modals/model_1.png' },
            { id: 1, src: '/modals/model_2.png' },
            { id: 2, src: '/modals/model_3.png' },
        ]
    },
    {
        title: '房间推荐',
        content: [
            { id: 0, src: '/modals/room_1.png' },
            { id: 1, src: '/modals/room_2.png' },
            { id: 2, src: '/modals/room_3.png' },
        ]
    },
]);
</script>

<style scoped lang="less">
.model-recommened {
    width: 100%;
    height: 100%;
    overflow: auto;
    display: flex;
    flex-direction: column;
    justify-content: start;
    align-items: center;
    gap: 190px;

    .recommened-item {
        width: 100%;
        height: 100px;

        .recommened-item-title {
            font-size: 16px;
            margin-bottom: 10px;
        }

        .recommened-item-content {
            width: 100%;
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
}

.model-recommened::-webkit-scrollbar {
    display: none;
}
</style>
