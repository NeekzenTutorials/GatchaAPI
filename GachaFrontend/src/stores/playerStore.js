import { defineStore } from 'pinia';
import { playerService } from '../services/playerService';
import { useUiStore } from './uiStore';

export const usePlayerStore = defineStore('player', {
  state: () => ({
    profile: null,
    levelData: null,
  }),
  getters: {
    currentXp: (state) => Number(state.levelData?.experience ?? state.profile?.experience ?? 0),
    xpToNext: (state) => Number(state.levelData?.nextLevelXp ?? state.profile?.nextLevelXp ?? 100),
    level: (state) => Number(state.levelData?.level ?? state.profile?.level ?? 1),
    monsterCount: (state) => {
      const monsters = state.profile?.monsters;
      return Array.isArray(monsters) ? monsters.length : 0;
    },
    monsterCapacityLeft: (state) => {
      const monsters = Array.isArray(state.profile?.monsters) ? state.profile.monsters.length : 0;
      const max = Number(state.profile?.maxMonsters ?? 0);
      return Math.max(0, max - monsters);
    },
    canLevelUp: (state) => Boolean(state.levelData?.canLevelUp ?? state.profile?.canLevelUp ?? false),
    progressPercent: (state) => {
      const xp = Number(state.levelData?.experience ?? state.profile?.experience ?? 0);
      const xpToNext = Number(state.levelData?.nextLevelXp ?? state.profile?.nextLevelXp ?? 100);
      if (!xpToNext) return 0;
      return Math.max(0, Math.min(100, Math.round((xp / xpToNext) * 100)));
    },
  },
  actions: {
    async fetchProfile() {
      const ui = useUiStore();
      ui.startLoading();
      try {
        this.profile = await playerService.getProfile();
      } catch (error) {
        ui.notify(error?.response?.data?.message || 'Impossible de charger le profil', 'error');
      } finally {
        ui.stopLoading();
      }
    },
    async fetchLevel() {
      try {
        this.levelData = await playerService.getLevel();
      } catch {
        this.levelData = null;
      }
    },
    async refresh() {
      await Promise.all([this.fetchProfile(), this.fetchLevel()]);
    },
  },
});
