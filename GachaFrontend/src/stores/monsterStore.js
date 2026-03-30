import { defineStore } from 'pinia';
import { monsterService } from '../services/monsterService';
import { useUiStore } from './uiStore';
import { normalizeMonsterModel } from '../utils/monster';

export const useMonsterStore = defineStore('monsters', {
  state: () => ({
    monsters: [],
    selectedMonster: null,
    isModalOpen: false,
  }),
  actions: {
    async fetchMonsters() {
      const ui = useUiStore();
      ui.startLoading();
      try {
        const data = await monsterService.getPlayerMonsters();
        const rows = Array.isArray(data) ? data : data?.monsters ?? [];

        if (!rows.length) {
          this.monsters = [];
          return;
        }

        // PlayerAPI renvoie souvent une liste d'IDs, on hydrate donc le détail de chaque monstre.
        if (typeof rows[0] === 'string') {
          const detailResults = await Promise.allSettled(rows.map((id) => monsterService.getMonsterById(id)));
          this.monsters = detailResults
            .filter((res) => res.status === 'fulfilled')
            .map((res) => normalizeMonsterModel(res.value));
          return;
        }

        this.monsters = rows.map((row) => normalizeMonsterModel(row));
      } catch (error) {
        ui.notify(error?.response?.data?.message || 'Impossible de charger les monstres', 'error');
      } finally {
        ui.stopLoading();
      }
    },
    async openMonsterDetail(monsterId) {
      const ui = useUiStore();
      ui.startLoading();
      try {
        const localMonster = typeof monsterId === 'object' ? normalizeMonsterModel(monsterId) : null;
        if (localMonster) {
          this.selectedMonster = localMonster;
          this.isModalOpen = true;
        }

        const targetId = typeof monsterId === 'string' ? monsterId : monsterId?.id || monsterId?._id;
        if (!targetId) {
          if (!localMonster) {
            throw new Error('ID de monstre manquant');
          }
          return;
        }

        this.selectedMonster = normalizeMonsterModel(await monsterService.getMonsterById(targetId));
        this.isModalOpen = true;
      } catch (error) {
        if (!this.selectedMonster) {
          ui.notify(error?.response?.data?.message || 'Détail du monstre indisponible', 'error');
        }
      } finally {
        ui.stopLoading();
      }
    },
    closeModal() {
      this.isModalOpen = false;
    },
  },
});
