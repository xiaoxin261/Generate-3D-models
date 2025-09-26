// 1. 从 vue-router 中引入 createRouter 和 createWebHistory
import { createRouter, createWebHistory } from 'vue-router';

// 2. 引入你的页面组件
// 懒加载方式 (推荐)：只有当用户访问该路由时，才会加载对应的组件，提升首屏加载速度
const HomeView = () => import('../views/HomeView/index.vue');

// 3. 定义路由
// 每个路由都应该映射到一个组件。
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
    ],
  },
  {
    path: '/help',
    name: 'help',
    component: () => import('../views/HelpView/index.vue'),
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