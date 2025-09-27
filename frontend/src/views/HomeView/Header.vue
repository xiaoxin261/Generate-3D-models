<template>
    <div class="header">
        <router-link to="/" class="left">
            <span class="title">七牛AI</span>
            <span class="littleTitle">您的专业装修平台</span>
        </router-link>
        <div class="right">
            <router-link to="/help" class="problem">
                <img src="/images/icon_problem.png" alt="">
                <span>帮助中心</span>
            </router-link>
            <router-link to="/login" class="login">登录/注册</router-link>
        </div>
    </div>
</template>

<script setup>
</script>

<style scoped lang="less">
@headerHeight: 80px;

.header {
    height: @headerHeight;
    width: 100%;
    background-color: #383838;
    display: flex;
    justify-content: space-between;
    align-items: center;
}

.left {
    height: 100%;
    width: 30%;
    padding-left: 36px;

    .title {
        margin-right: 24px;
        font-size: 36px;
        font-weight: 700;
        line-height: @headerHeight;
        color: #fff;
    }

    .littleTitle {
        font-size: 24px;
        font-weight: 400;
        color: #fff;
    }
}
.right {
    height: 100%;
    width: 30%;
    display: flex;
    justify-content: flex-end;
    align-items: center;
    padding-right: 36px;
    gap: 24px;
    .problem {
        height: 100%;
        width: 56px;
        display: flex;
        flex-direction: column;
        align-items: center;
        justify-content: center;
        img {
            width: 48px;
            height: 40px;
        }
        span {
            font-size: 14px;
            font-weight: 400;
            color: #fff;
        }
    }
    .login {
        height: 32px;
        width: 161px;
        border-radius: 16px;
        text-align: center;
        line-height: 32px;
        background-color: #fff;
        color: #000;
    }
}
</style>
