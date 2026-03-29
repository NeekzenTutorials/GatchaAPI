<template>
  <section class="auth-layout">
    <div class="auth-card glass">
      <h2>Entrée du Sanctuaire</h2>
      <p>Connecte-toi pour reprendre tes invocations.</p>

      <div class="auth-tabs">
        <button :class="{ active: mode === 'login' }" @click="mode = 'login'">Connexion</button>
        <button :class="{ active: mode === 'register' }" @click="mode = 'register'">Inscription</button>
      </div>

      <form class="auth-form" @submit.prevent="onSubmit">
        <small class="auth-hint" v-if="mode === 'login'">Connexion avec ton email (pas le pseudo).</small>
        <input
          v-if="mode === 'register'"
          v-model="form.pseudo"
          type="text"
          placeholder="Pseudo"
          required
        />
        <input
          v-model="form.email"
          type="email"
          placeholder="Email"
          required
        />
        <input v-model="form.password" type="password" placeholder="Mot de passe" required />
        <button class="btn-primary" type="submit">
          {{ mode === 'login' ? 'Se connecter' : 'Créer un compte' }}
        </button>
        <p v-if="errorMessage" class="auth-error">{{ errorMessage }}</p>
      </form>
    </div>
  </section>
</template>

<script setup>
import { reactive, ref } from 'vue';
import { useRouter } from 'vue-router';
import { useAuthStore } from '../stores/authStore';

const authStore = useAuthStore();
const router = useRouter();

const mode = ref('login');
const form = reactive({
  pseudo: '',
  email: '',
  password: '',
});
const errorMessage = ref('');

function validateForm(mode, payload) {
  if (!payload.email || !payload.email.includes('@')) {
    return 'Email invalide. Format attendu : nom@domaine.com';
  }
  if (!payload.password || payload.password.length < 6) {
    return 'Mot de passe invalide. Minimum 6 caractères.';
  }
  if (mode === 'register') {
    if (!payload.pseudo || payload.pseudo.length < 2) {
      return 'Pseudo invalide. Minimum 2 caractères.';
    }
  }
  return '';
}

async function onSubmit() {
  errorMessage.value = '';
  const cleanEmail = String(form.email || '').trim().toLowerCase();
  const cleanPseudo = String(form.pseudo || '').trim();
  const cleanPassword = String(form.password || '').trim();
  const payload = { email: cleanEmail, pseudo: cleanPseudo, password: cleanPassword };

  const validationMessage = validateForm(mode.value, payload);
  if (validationMessage) {
    errorMessage.value = validationMessage;
    return;
  }

  try {
    if (mode.value === 'login') {
      await authStore.login({ email: payload.email, password: payload.password });
    } else {
      await authStore.register({ pseudo: payload.pseudo, email: payload.email, password: payload.password });
      if (!authStore.isAuthenticated) {
        await authStore.login({ email: payload.email, password: payload.password, pseudo: payload.pseudo });
      }
    }
    router.push('/profile');
  } catch (error) {
    errorMessage.value =
      error?.response?.data?.message ||
      error?.response?.data?.error ||
      error?.response?.data?.detail ||
      'Requête refusée par l’API d’authentification.';
  }
}
</script>
