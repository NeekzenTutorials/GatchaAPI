<template>
  <div class="summon-reveal" :class="`rarity-${rarityClass}`">
    <div class="reveal-beam"></div>
    <div class="reveal-card">
      <p class="reveal-title">Invocation réussie</p>
      <MonsterIllustration :monster="monster" size="reveal" />
      <h3>{{ monster?.name || '???' }}</h3>
      <p class="rarity-label">{{ monster?.rarity || 'Common' }}</p>
      <p class="type-pill" :class="`type-${typeClass}`">{{ monster?.typeDisplay || monster?.typeLabel || monster?.type || monster?.elementType || 'neutre' }}</p>

      <div class="stats-grid reveal-stats">
        <span>HP: {{ monster?.hp ?? '-' }}</span>
        <span>ATK: {{ monster?.atk ?? '-' }}</span>
        <span>DEF: {{ monster?.def ?? '-' }}</span>
        <span>SPD: {{ monster?.speed ?? monster?.vit ?? '-' }}</span>
      </div>
    </div>
  </div>
</template>

<script setup>
import { computed } from 'vue';
import MonsterIllustration from './MonsterIllustration.vue';

const props = defineProps({
  monster: {
    type: Object,
    required: true,
  },
});

const rarityClass = computed(() => String(props.monster.rarity || 'common').toLowerCase());
const typeClass = computed(() => {
  const raw = String(props.monster.typeLabel || props.monster.type || props.monster.elementType || 'neutral').toLowerCase();
  if (raw.includes('eau') || raw.includes('water')) return 'eau';
  if (raw.includes('feu') || raw.includes('fire')) return 'feu';
  if (raw.includes('vent') || raw.includes('wind')) return 'vent';
  return 'neutral';
});
</script>
