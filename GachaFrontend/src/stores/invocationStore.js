import { defineStore } from 'pinia';
import { invocationService } from '../services/invocationService';
import { monsterService } from '../services/monsterService';
import { useUiStore } from './uiStore';
import { normalizeMonsterModel } from '../utils/monster';

export const useInvocationStore = defineStore('invocations', {
  state: () => ({
    templates: [],
    history: [],
    lastSummoned: null,
    isSummoning: false,
  }),
  actions: {
    findTemplate(monster) {
      return this.templates.find(
        (tpl) =>
          String(tpl.name || '').toLowerCase() === String(monster.name || '').toLowerCase() &&
          String(tpl.elementType || '').toLowerCase() === String(monster.elementType || monster.type || '').toLowerCase()
      );
    },
    async enrichMonster(baseMonster) {
      const template = this.findTemplate(baseMonster);
      let merged = normalizeMonsterModel(baseMonster, template);

      if (merged.id) {
        try {
          const detail = await monsterService.getMonsterById(merged.id);
          merged = normalizeMonsterModel({ ...merged, ...detail }, template);
        } catch {
          // Le détail peut être temporairement indisponible juste après l'invocation.
        }
      }
      return merged;
    },
    normalizeMonster(raw) {
      const source = raw?.monster || raw?.result || raw || {};
      return normalizeMonsterModel(source);
    },
    async fetchTemplates() {
      try {
        const data = await invocationService.getTemplates();
        const rows = Array.isArray(data) ? data : data?.templates ?? [];
        this.templates = rows.map((tpl) => ({
          ...tpl,
          rate: Number(tpl.rate ?? tpl.probability ?? tpl.chance ?? tpl.invocationRate ?? 0),
          elementType: tpl.elementType || tpl.type,
        }));
      } catch {
        this.templates = [];
      }
    },
    async fetchHistory() {
      try {
        const data = await invocationService.getHistory();
        const rows = Array.isArray(data) ? data : data?.history ?? [];
        const enriched = await Promise.all(rows.map((item) => this.enrichMonster(this.normalizeMonster(item))));
        this.history = enriched;
      } catch {
        this.history = [];
      }
    },
    async summon() {
      const ui = useUiStore();
      this.isSummoning = true;
      ui.startLoading();
      try {
        const data = await invocationService.summon();
        const base = this.normalizeMonster(data);
        const monster = await this.enrichMonster(base);
        this.lastSummoned = monster;
        this.history = [monster, ...this.history].slice(0, 20);
        ui.notify(`Invocation réussie : ${monster.name || 'Monstre inconnu'}`, 'success');
        return monster;
      } catch (error) {
        ui.notify(error?.response?.data?.message || 'Invocation échouée', 'error');
        throw error;
      } finally {
        this.isSummoning = false;
        ui.stopLoading();
      }
    },
  },
});
