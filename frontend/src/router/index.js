import { createRouter, createWebHistory } from 'vue-router';

const HomeView = () => import('../views/HomeView/index.vue');

const routes = [
    {
        path: '/',
        name: 'homeView',
        component: HomeView,
        redirect: '/home',
        children: [
            {
                path: '/home',
                name: 'home',
                component: () => import('../views/HomeView/Home.vue'),
            },
            {
                path: '/login',
                name: 'login',
                component: () => import('../views/HomeView/Login.vue'),
            },
            {
                path: '/help',
                name: 'help',
                component: () => import('../views/HelpView/index.vue'),
            }
        ],
    },
    {
        path: '/design',
        name: 'design',
        redirect: '/design/home',
        component: () => import('../views/DesignView/index.vue'),
        children: [
            {
                path: '/design/home',
                name: 'designIndex',
                component: () => import('../views/DesignView/Home.vue'),
            },
        ]
    }

];

// 4. 创建路由实例
const router = createRouter({
    // 5. 配置 history 模式
    // createWebHistory: 使用 HTML5 History API，URL 看起来更美观 (例如: /about)
    // createWebHashHistory: URL 中会带有 # (例如: /#/about)，兼容性更好
    history: createWebHistory(),
    routes, // (缩写) 相当于 routes: routes
});

// 6. 导出路由实例
export default router;