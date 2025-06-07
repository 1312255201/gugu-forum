import { createRouter, createWebHistory } from 'vue-router'
import {isRoleAdmin, isUnauthorized} from "@/net";

const router = createRouter({
    history: createWebHistory(import.meta.env.BASE_URL),
    routes: [
        {
            path: '/',
            name: 'welcome',
            component: () => import('@/views/WelcomeView.vue'),
            children: [
                {
                    path: '',
                    name: 'welcome-login',
                    component: () => import('@/views/welcome/LoginPage.vue')
                }, {
                    path: 'register',
                    name: 'welcome-register',
                    component: () => import('@/views/welcome/RegisterPage.vue')
                }, {
                    path: 'forget',
                    name: 'welcome-forget',
                    component: () => import('@/views/welcome/ForgetPage.vue')
                }
            ]
        }, {
            path: '/index',
            name: 'index',
            component: () => import('@/views/IndexView.vue'),
            children: [
                {
                    path: '',
                    name: 'topics',
                    component: () => import('@/views/forum/Forum.vue'),
                    children: [
                        {
                            path: '',
                            name: 'topic-list',
                            component: () => import('@/views/forum/TopicList.vue')
                        },{
                            path: 'topic-detail/:tid',
                            name: 'topic-detail',
                            component: () => import('@/views/forum/TopicDetail.vue')
                        }
                    ]
                }, {
                    path: 'lost-found',
                    name: 'lost-found',
                    component: () => import('@/views/lostfound/LostFoundList.vue')
                }, {
                    path: 'lost-found/create',
                    name: 'lost-found-create',
                    component: () => import('@/views/lostfound/LostFoundCreate.vue')
                }, {
                    path: 'lost-found/:id',
                    name: 'lost-found-detail',
                    component: () => import('@/views/lostfound/LostFoundDetail.vue')
                }, {
                    path: 'activity',
                    name: 'activity',
                    component: () => import('@/views/activity/ActivityList.vue')
                }, {
                    path: 'activity/:id',
                    name: 'activity-detail',
                    component: () => import('@/views/activity/ActivityDetail.vue')
                }, {
                    path: 'user-setting',
                    name: 'user-setting',
                    component: () => import('@/views/settings/UserSetting.vue')
                }, {
                    path: 'privacy-setting',
                    name: 'privacy-setting',
                    component: () => import('@/views/settings/PrivacySetting.vue')
                }
            ]
        },{
            path: '/admin',
            name: 'admin',
            component:() => import('@/views/AdminView.vue'),
            children:[
                {
                    path: '',
                    name: 'admin-welcome',
                    component: () => import('@/views/admin/WelcomeAdmin.vue')
                }, {
                    path: 'user',
                    name: 'admin-user',
                    component: () => import('@/views/admin/UserAdmin.vue')
                }, {
                    path: 'forum',
                    name: 'admin-forum',
                    component: () => import('@/views/admin/ForumAdmin.vue')
                }, {
                    path: 'activity',
                    name: 'admin-activity',
                    component: () => import('@/views/admin/ActivityAdmin.vue')
                }
            ]
        }
    ]
})

router.beforeEach((to, from, next) => {
    const unauthorized = isUnauthorized(), admin = isRoleAdmin()
    if(to.name && to.name.startsWith('welcome') && !unauthorized) {
        next('/index')
    } else if(to.fullPath.startsWith('/admin') && !admin) {
        next('/index')
    }else if(to.fullPath.startsWith('/index') && unauthorized) {
        next('/')
    } else {
        next()
    }
})

export default router
