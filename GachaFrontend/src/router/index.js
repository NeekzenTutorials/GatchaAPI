import { createRouter, createWebHistory } from 'vue-router';
import { useAuthStore } from '../stores/authStore';
import AuthView from '../views/AuthView.vue';
import ProfileView from '../views/ProfileView.vue';
import MonstersView from '../views/MonstersView.vue';
import GachaView from '../views/GachaView.vue';

const routes = [
  { path: '/', redirect: '/profile' },
  { path: '/login', name: 'login', component: AuthView, meta: { layout: 'auth' } },
  { path: '/profile', name: 'profile', component: ProfileView, meta: { requiresAuth: true } },
  { path: '/monsters', name: 'monsters', component: MonstersView, meta: { requiresAuth: true } },
  { path: '/gacha', name: 'gacha', component: GachaView, meta: { requiresAuth: true } },
  { path: '/:pathMatch(.*)*', redirect: '/profile' },
];

const router = createRouter({
  history: createWebHistory(),
  routes,
});

router.beforeEach(async (to) => {
  const authStore = useAuthStore();
  if (!authStore.hasHydrated) {
    authStore.hydrateFromStorage();
    if (authStore.token) {
      await authStore.validateSession();
    }
  }

  if (to.meta.requiresAuth && !authStore.isAuthenticated) {
    return { name: 'login' };
  }

  if (to.name === 'login' && authStore.isAuthenticated) {
    return { name: 'profile' };
  }

  return true;
});

export default router;
