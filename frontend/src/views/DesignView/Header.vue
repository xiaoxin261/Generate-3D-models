<template>
    <div class="header">
        <div class="left">
            <router-link to="/" class="logo">
                七牛AI
            </router-link>
            <div class="export">
                <img src="/images/icon_export.png" alt="export">
                <span>导出</span>
            </div>
        </div>
        <div class="center">
            <div class="item" v-for="item in navCenterList" :key="item.id" @click="clickItem(item.id)">
                <img :src="item.img" alt="item.name">
                <span>{{ item.name }}</span>
            </div>
        </div>
        <div class="right">
            <div class="item" v-for="item in navRightList" :key="item.id" @click="clickItem(item.id)">
                <img :src="item.img" alt="item.name">
                <span>{{ item.name }}</span>
            </div>
        </div>
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
const navRightList = ref([
    {
        id: 'setting',
        name: '设置',
        img: '/images/icon_setting.png'
    },
    {
        id: 'notice',
        name: '通知',
        img: '/images/icon_notice.png'
    },
    {
        id: 'user',
        name: '用户',
        img: '/images/icon_user.png'
    }
])
const clickItem = (id) => {
    console.log(id)
}

</script>

<style scoped lang="less">
.header {
    width: 100%;
    height: 100%;
    display: flex;
    justify-content: space-between;

    .left {
        width: 30%;
        padding-left: 3rem;
        padding-right: 2rem;
        background-color: #000;
        display: flex;
        align-items: center;
        justify-content: space-between;

        .logo {
            font-size: 36px;
            font-weight: 400;
            line-height: 32px;
            cursor: pointer;
            color: #fff;
        }

        .export {
            width: 9rem;
            color: #fff;
            display: flex;
            gap: 10px;
            align-items: center;
            cursor: pointer;
        }

    }
    .center {
        width: 40%;
        background-color: #FFFFFF;
        display: flex;
        justify-content: space-around;

        .item {
            display: flex;
            flex-direction: column;
            gap: 4px;
            justify-content: center;
            cursor: pointer;
            align-items: center;

            img {
                width: 24px;
                height: 24px;
            }
        }
    }
    .right {
        width: 30%;
        padding-left: 4rem;
        padding-right: 3rem;
        background-color: #000;
        display: flex;
        align-items: center;
        justify-content: space-evenly;
        gap: 30%;
        .item {
            display: flex;
            flex-direction: column;
            gap: 4px;
            cursor: pointer;
            justify-content: center;
            align-items: center;

            img {
                width: 24px;
                height: 24px;
            }
            span {
                font-size: 14px;
                font-weight: 400;
                line-height: 24px;
                color: #fff;
            }
        }
    }
}
</style>