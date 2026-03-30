<template>
  <div class="shell">
    <header class="topbar glass">
      <div class="brand">
        <SparklesIcon class="icon accent" />
        <div>
          <h1>Gacha Immersion</h1>
          <p>Portail d’invocation</p>
        </div>
      </div>
      <nav class="nav-links">
        <RouterLink to="/profile" class="nav-item">Profil</RouterLink>
        <RouterLink to="/monsters" class="nav-item">Monstres</RouterLink>
        <RouterLink to="/gacha" class="nav-item">Invocation</RouterLink>
      </nav>
      <button class="btn-secondary" @click="handleLogout">
        <ArrowLeftOnRectangleIcon class="icon" /> Déconnexion
      </button>
    </header>

    <main class="shell-content">
      <slot />
    </main>
  </div>
</template>

<script setup>
import { RouterLink, useRouter } from 'vue-router';
import { SparklesIcon, ArrowLeftOnRectangleIcon } from '@heroicons/vue/24/solid';
import { useAuthStore } from '../stores/authStore';

const authStore = useAuthStore();
const router = useRouter();

function handleLogout() {
  authStore.logout();
  router.push('/login');
}
</script>
