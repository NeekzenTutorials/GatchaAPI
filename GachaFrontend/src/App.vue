<template>
  <div class="app-bg">
    <AppShell v-if="isAuthenticated && $route.meta.layout !== 'auth'">
      <RouterView />
    </AppShell>
    <RouterView v-else />
    <ToastContainer />
    <LoaderOverlay v-if="uiStore.isGlobalLoading" />
  </div>
</template>

<script setup>
import { computed } from 'vue';
import { RouterView } from 'vue-router';
import { useAuthStore } from './stores/authStore';
import { useUiStore } from './stores/uiStore';
import AppShell from './components/AppShell.vue';
import ToastContainer from './components/ToastContainer.vue';
import LoaderOverlay from './components/LoaderOverlay.vue';

const authStore = useAuthStore();
const uiStore = useUiStore();
const isAuthenticated = computed(() => authStore.isAuthenticated);
</script>
