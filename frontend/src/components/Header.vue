<template>
    <div class="header">
        <div class="connor">
            <div class="logo">
                七牛AI
            </div>
            <div class="export">
                <img src="/images/icon_export.png" alt="export">
                <span>导出</span>
            </div>
        </div>
        <div class="center">
            <div class="item" v-for="item in navCenterList" :key="item.id" @click="clickItem(item.id)">
                <img :src="item.img" alt="item.name">
                <span>{{item.name}}</span>
            </div>
        </div>
        <div class="connor"></div>
    </div>
</template>

<script setup>
import { ref } from 'vue'
const navCenterList = ref([
    {
        id: 'file',
        name: '文件',
        img: '/images/icon_file.png'
    },
    {
        id: 'save',
        name: '保存',
        img: '/images/icon_save.png'
    },
    {
        id: 'tool',
        name: '工具',
        img: '/images/icon_tool.png'
    },
    {
        id: 'paper',
        name: '贴纸',
        img: '/images/icon_paper.png'
    },
    {
        id: 'render',
        name: '渲染',
        img: '/images/icon_render.png'
    }
])
const clickItem = (id) => {
    console.log(id)
}

</script>

<style scoped>
.header {
    width: 100%;
    height: 100%;
    display: flex;
    justify-content: space-between;
}
.connor {
    width: 30%;
    padding-left: 3rem;
    padding-right: 2rem;
    background-color: #362020;
    display: flex;
    align-items: center;
    justify-content: space-between;
}
.center {
    width: 40%;
    background-color: #FFFFFF;
    display: flex;
    justify-content: space-around;
}
.item {
    display: flex;
    flex-direction: column;
    gap: 4px;
    justify-content: center;
    align-items: center;
}
.logo{
    font-size: 36px;
    font-weight: 400;
    line-height: 32px;
    color: #fff;
}
.export {
    width: 9rem;
    color: #fff;
    display: flex;
    gap: 10px;
    align-items: center;
}

</style>