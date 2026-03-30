import { defineStore } from 'pinia';

let toastId = 0;

export const useUiStore = defineStore('ui', {
  state: () => ({
    loadingCount: 0,
    toasts: [],
  }),
  getters: {
    isGlobalLoading: (state) => state.loadingCount > 0,
  },
  actions: {
    startLoading() {
      this.loadingCount += 1;
    },
    stopLoading() {
      this.loadingCount = Math.max(0, this.loadingCount - 1);
    },
    notify(message, type = 'info', duration = 3800) {
      const id = ++toastId;
      this.toasts.push({ id, message, type });
      setTimeout(() => {
        this.toasts = this.toasts.filter((toast) => toast.id !== id);
      }, duration);
    },
    removeToast(id) {
      this.toasts = this.toasts.filter((toast) => toast.id !== id);
    },
  },
});
