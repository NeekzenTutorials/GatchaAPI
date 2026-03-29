import { defineStore } from 'pinia';
import { authService } from '../services/authService';
import { useUiStore } from './uiStore';

export const useAuthStore = defineStore('auth', {
  state: () => ({
    token: null,
    user: null,
    hasHydrated: false,
  }),
  getters: {
    isAuthenticated: (state) => Boolean(state.token),
  },
  actions: {
    extractAuthErrorMessage(error, fallback) {
      const status = error?.response?.status;
      const rawMessage =
        error?.response?.data?.message ||
        error?.response?.data?.error ||
        error?.response?.data?.detail;

      if (status === 400) {
        return rawMessage || 'Requête invalide. Vérifie: email valide, mot de passe >= 6 caractères, pseudo non vide.';
      }
      if (status === 401) {
        return rawMessage || 'Identifiants invalides.';
      }
      if (status === 409) {
        return rawMessage || 'Email ou pseudo déjà utilisé.';
      }
      return rawMessage || fallback;
    },
    hydrateFromStorage() {
      this.token = localStorage.getItem('gacha_token');
      const user = localStorage.getItem('gacha_user');
      this.user = user ? JSON.parse(user) : null;
      this.hasHydrated = true;
    },
    persistAuth(token, user = null) {
      this.token = token;
      this.user = user;
      localStorage.setItem('gacha_token', token);
      if (user) {
        localStorage.setItem('gacha_user', JSON.stringify(user));
      }
    },
    logout() {
      this.token = null;
      this.user = null;
      localStorage.removeItem('gacha_token');
      localStorage.removeItem('gacha_user');
    },
    async login(credentials) {
      const ui = useUiStore();
      ui.startLoading();
      try {
        const data = await authService.login({
          email: credentials.email,
          password: credentials.password,
        });
        const token = data.token || data.jwt || data.accessToken;
        if (!token) throw new Error('Token JWT manquant dans la réponse.');
        const user = data.user || data.player || { email: credentials.email, pseudo: credentials.pseudo };
        this.persistAuth(token, user);
        ui.notify('Connexion réussie', 'success');
      } catch (error) {
        const apiMessage = this.extractAuthErrorMessage(error, 'Échec de connexion');
        ui.notify(apiMessage || 'Échec de connexion', 'error');
        throw error;
      } finally {
        ui.stopLoading();
      }
    },
    async register(payload) {
      const ui = useUiStore();
      ui.startLoading();
      try {
        const data = await authService.register({
          email: payload.email,
          password: payload.password,
          pseudo: payload.pseudo,
        });
        const token = data.token || data.jwt || data.accessToken;
        const user = data.user || data.player || {
          id: data.id,
          email: data.email || payload.email,
          pseudo: payload.pseudo,
          createdAt: data.createdAt,
        };
        if (token) {
          this.persistAuth(token, user);
        } else {
          this.user = user;
          localStorage.setItem('gacha_user', JSON.stringify(user));
        }
        ui.notify('Inscription réussie', 'success');
      } catch (error) {
        const apiMessage = this.extractAuthErrorMessage(error, 'Échec d’inscription');
        ui.notify(apiMessage || 'Échec d’inscription', 'error');
        throw error;
      } finally {
        ui.stopLoading();
      }
    },
    async validateSession() {
      if (!this.token) return false;
      try {
        const data = await authService.validate();
        const refreshedToken = data?.token || data?.newToken || data?.jwt;
        if (refreshedToken) {
          this.token = refreshedToken;
          localStorage.setItem('gacha_token', refreshedToken);
        }
        if (data?.pseudo && this.user) {
          this.user = { ...this.user, pseudo: data.pseudo };
          localStorage.setItem('gacha_user', JSON.stringify(this.user));
        }
        const isValid =
          typeof data?.valid === 'boolean'
            ? data.valid
            : typeof data?.status === 'string'
              ? data.status.toUpperCase() === 'OK'
              : true;
        if (!isValid) {
          this.logout();
          return false;
        }
        return true;
      } catch {
        this.logout();
        return false;
      }
    },
  },
});
