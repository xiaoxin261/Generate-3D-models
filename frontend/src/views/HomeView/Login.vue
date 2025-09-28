<template>
    <div class="login-container">
        <div class="title">七牛AI</div>
        <div class="subTilte">七牛AI，您的装修选择必需品！</div>
        <div class="tabs-container">
            <el-tabs v-show="currentPage === 'login'" v-model="activeName" class="demo-tabs" @tab-click="handleClick">
                <el-tab-pane label="账号密码登录" name="first">
                    <el-form :model="form" label-width="auto" style="max-width: 600px">
                        <el-form-item label="" prop="username">
                            <el-input class="mobile-input" v-model="form.loginName" placeholder="请输入用户名"
                                prefix-icon="User"></el-input>
                        </el-form-item>
                        <el-form-item label="" prop="password">
                            <el-input class="mobile-input" v-model="form.password" placeholder="请输入密码" type="password"
                                prefix-icon="Lock"></el-input>
                        </el-form-item>
                        <el-form-item label="" prop="captcha">
                            <el-input class="mobile-input" v-model="form.captcha" placeholder="请输入验证码"
                                prefix-icon="Lock"></el-input>
                        </el-form-item>
                    </el-form>
                    <div class="remember-login">
                        <el-checkbox v-model="form.rememberMe" style="margin-right: 8px;">自动登录</el-checkbox>
                        <el-link type="primary" style="margin-right: 8px; font-weight: 400;"
                            @click="currentPage = 'forgot'">忘记密码</el-link>
                    </div>
                    <div class="login-btn">
                        <el-button type="primary" style="margin-top: 10px; width: 100%;"
                            @click="handleClick('login')">登录</el-button>
                    </div>
                    <div class="register-btn">
                        <el-link type="primary" style="margin-right: 8px; font-weight: 400;"
                            @click="currentPage = 'register'">注册账号</el-link>
                    </div>
                </el-tab-pane>
                <el-tab-pane label="手机号登录" name="second">
                    <el-form :model="form" label-width="auto" style="max-width: 600px">
                        <el-form-item label="" prop="mobile">
                            <el-input class="mobile-input" v-model="form.mobile" placeholder="请输入手机号"
                                prefix-icon="Phone"></el-input>
                        </el-form-item>
                        <el-form-item label="" prop="code">
                            <el-input class="mobile-input" v-model="form.code" placeholder="请输入验证码"
                                prefix-icon="ChatDotSquare"></el-input>
                        </el-form-item>
                        <el-button type="primary" style="margin-top: 10px; width: 100%;"
                            @click="handleClick('login')">登录</el-button>
                    </el-form>
                </el-tab-pane>
            </el-tabs>
            <el-tabs v-show="currentPage === 'register'" v-model="activeName" class="demo-tabs" @tab-click="handleClick">
                <el-tab-pane label="注册" name="first">
                    <el-form :model="formRegister" label-width="auto" style="max-width: 600px">
                        <el-form-item label="" prop="username">
                            <el-input class="mobile-input" v-model="formRegister.username" placeholder="请输入账号"
                                prefix-icon="User"></el-input>
                        </el-form-item>
                        <el-form-item label="" prop="email">
                            <el-input class="mobile-input" v-model="formRegister.email" placeholder="请输入邮箱"
                                prefix-icon="Message"></el-input>
                        </el-form-item>
                        <el-form-item label="" prop="phone">
                            <el-input class="mobile-input" v-model="formRegister.phone" placeholder="请输入手机号"
                                prefix-icon="Phone"></el-input>
                        </el-form-item>
                        <el-form-item label="" prop="password">
                            <el-input class="mobile-input" v-model="formRegister.password" placeholder="请输入密码" type="password"
                                prefix-icon="Lock"></el-input>
                        </el-form-item>
                        <el-form-item label="" prop="confirmPassword">
                            <el-input class="mobile-input" v-model="formRegister.confirmPassword" placeholder="请确认密码" type="password"
                                prefix-icon="Lock"></el-input>
                        </el-form-item>
                        <el-form-item label="" prop="verificationCode">
                            <el-input class="mobile-input" v-model="formRegister.verificationCode" placeholder="请输入验证码"
                                prefix-icon="Lock"></el-input>
                        </el-form-item>
                    </el-form>
                    <div class="login-btn">
                        <el-button type="primary" style="margin-top: 10px; width: 100%;"
                            @click="handleClick('register')">注册</el-button>
                    </div>
                    <div class="register-btn">
                        <el-link type="primary" style="margin-right: 8px; font-weight: 400;"
                            @click="currentPage = 'login'">使用已有账号登录</el-link>
                    </div>
                </el-tab-pane>
            </el-tabs>
            <el-tabs v-show="currentPage === 'forgot'" v-model="activeName" class="demo-tabs" @tab-click="handleClick">
                <el-tab-pane label="账号密码登录" name="first">
                    <el-form :model="form" label-width="auto" style="max-width: 600px">
                        <el-form-item label="" prop="loginName">
                            <el-input class="mobile-input" v-model="form.loginName" placeholder="请输入用户名"
                                prefix-icon="User"></el-input>
                        </el-form-item>
                        <el-form-item label="" prop="password">
                            <el-input class="mobile-input" v-model="form.password" placeholder="请输入密码" type="password"
                                prefix-icon="Lock"></el-input>
                        </el-form-item>
                        <el-form-item label="" prop="captcha">
                            <el-input class="mobile-input" v-model="form.captcha" placeholder="请输入验证码"
                                prefix-icon="Lock"></el-input>
                        </el-form-item>
                    </el-form>
                    <div class="remember-login">
                        <el-checkbox v-model="form.rememberLogin" style="margin-right: 8px;">自动登录</el-checkbox>
                        <el-link type="primary" style="margin-right: 8px; font-weight: 400;"
                            @click="handleClick('password')">忘记密码</el-link>
                    </div>
                    <div class="login-btn">
                        <el-button type="primary" style="margin-top: 10px; width: 100%;"
                            @click="handleClick('login')">登录</el-button>
                    </div>
                    <div class="register-btn">
                        <el-link type="primary" style="margin-right: 8px; font-weight: 400;"
                            @click="handleClick('register')">注册账号</el-link>
                    </div>
                </el-tab-pane>
            </el-tabs>
        </div>
    </div>
</template>

<script setup>
import { ref } from 'vue'
import { login, register } from '@/api/auth'

const form = ref({
    loginName: '',
    password: '',
    captcha: '',
    rememberMe: false
})

const formRegister = ref({
    username: '',
    email: '',
    phone: '',
    password: '',
    confirmPassword: '',
    verificationCode: ''
})

const activeName = ref('first')
const currentPage = ref('login')

const handleClick = (to) => {
    switch (to) {
        case 'login':
            if (!form.value.loginName) {
                ElMessage.error('请输入用户名');
                return;
            }
            if (!form.value.password) {
                ElMessage.error('请输入密码');
                return;
            }
            if (!form.value.captcha) {
                ElMessage.error('请输入验证码');
                return;
            }
            login(form.value).then(res => {
                console.log(res)
                if (res.code === 200) {
                    ElMessage.success('登录成功');
                    router.push({ name: 'home' });
                } else {
                    ElMessage.error(res.msg || '登录失败');
                }
            }).catch(() => {
                ElMessage.error('登录失败');
            });
            break
        case 'register':
            console.log(formRegister.value)
            register(formRegister.value).then(res => {
                console.log(res)
                if (res.success === true) {
                    ElMessage.success('注册成功');
                    router.push({ name: 'login' });
                } else {
                    ElMessage.error(res.message || '注册失败');
                }
            }).catch(() => {
                ElMessage.error('注册失败');
            });
            break
        case 'password':
            break
    }
}
</script>

<style scoped lang="less">
.login-container {
    position: absolute;
    top: 50%;
    left: 50%;
    transform: translate(-50%, -50%);
    width: 70%;
    height: 70%;
    background-image: url('/images/bg_login.jpg');
    background-size: cover;
    background-position: center;

    .title {
        position: absolute;
        top: 20%;
        left: 50%;
        transform: translate(-50%, -50%);
        font-size: 33px;
        font-weight: 500;
        color: #000000;
    }

    .subTilte {
        position: absolute;
        top: 27%;
        left: 50%;
        transform: translate(-50%, -50%);
        font-size: 14px;
        font-weight: 400;
        opacity: 0.45;
        color: #000000;
    }

    .tabs-container {
        position: absolute;
        top: 33%;
        left: 50%;
        transform: translate(-50%, 0);
        width: 368px;
        height: 348px;
    }

    .remember-login {
        display: flex;
        justify-content: space-between;
    }

    .register-btn {
        display: flex;
        justify-content: right;
        margin-top: 10px;
    }
}

.demo-tabs>.el-tabs__content {
    padding: 32px;
    color: #6b778c;
    font-size: 32px;
    font-weight: 600;
}

.mobile-input :deep(.el-input__prefix svg) {
    color: #409EFF;
    /* Element Plus 的 primary 蓝色 */
}
</style>
