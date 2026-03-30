<template>
  <div v-if="open" class="modal-backdrop" @click.self="$emit('close')">
    <section class="modal-card glass">
      <header class="modal-header">
        <h2>{{ monster?.name || 'Détail du monstre' }}</h2>
        <button class="btn-icon" @click="$emit('close')">✕</button>
      </header>

      <div class="modal-illustration-wrap">
        <MonsterIllustration v-if="monster" :monster="monster" size="modal" />
      </div>

      <p class="rarity-label">Rareté : {{ monster?.rarity || 'Common' }}</p>
      <p>Type : <span class="type-pill" :class="`type-${typeClass}`">{{ monster?.typeDisplay || monster?.typeLabel || monster?.type || monster?.elementType || 'neutre' }}</span></p>

      <div class="stats-grid details">
        <span>HP: {{ monster?.hp ?? '-' }}</span>
        <span>ATK: {{ monster?.atk ?? '-' }}</span>
        <span>DEF: {{ monster?.def ?? '-' }}</span>
        <span>SPD: {{ monster?.speed ?? monster?.vit ?? '-' }}</span>
      </div>

      <h3>Compétences</h3>
      <ul class="skills-list">
        <li v-for="(skill, index) in skills" :key="index">{{ skill.name || skill }}</li>
      </ul>
    </section>
  </div>
</template>

<script setup>
import { computed } from 'vue';
import MonsterIllustration from './MonsterIllustration.vue';

const props = defineProps({
  open: Boolean,
  monster: Object,
});

defineEmits(['close']);

const typeClass = computed(() => {
  const raw = String(props.monster?.typeLabel || props.monster?.type || props.monster?.elementType || 'neutral').toLowerCase();
  if (raw.includes('eau') || raw.includes('water')) return 'eau';
  if (raw.includes('feu') || raw.includes('fire')) return 'feu';
  if (raw.includes('vent') || raw.includes('wind')) return 'vent';
  return 'neutral';
});
const skills = computed(() => {
  const raw = props.monster?.skills || props.monster?.abilities || [];
  return Array.isArray(raw) ? raw : [];
});
</script>
