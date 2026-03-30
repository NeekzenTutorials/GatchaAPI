<template>
  <article class="monster-card" :class="`rarity-${rarityClass}`" @click="$emit('details', monster)">
    <div class="monster-top">
      <MonsterIllustration :monster="monster" size="card" />
      <div class="monster-headline">
        <h3>{{ monster.name || 'Monstre inconnu' }}</h3>
        <span class="type-pill" :class="`type-${typeClass}`">{{ monster.typeDisplay || monster.typeLabel || monster.type || monster.elementType || 'neutre' }}</span>
      </div>
    </div>

    <p class="rarity-label">{{ monster.rarity || 'Common' }}</p>

    <div class="stats-grid">
      <span>HP: {{ monster.hp ?? '-' }}</span>
      <span>ATK: {{ monster.atk ?? '-' }}</span>
      <span>DEF: {{ monster.def ?? '-' }}</span>
      <span>SPD: {{ monster.speed ?? monster.vit ?? '-' }}</span>
    </div>
  </article>
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

defineEmits(['details']);

const rarityClass = computed(() => String(props.monster.rarity || 'common').toLowerCase());
const typeClass = computed(() => {
  const raw = String(props.monster.typeLabel || props.monster.type || props.monster.elementType || 'neutral').toLowerCase();
  if (raw.includes('eau') || raw.includes('water')) return 'eau';
  if (raw.includes('feu') || raw.includes('fire')) return 'feu';
  if (raw.includes('vent') || raw.includes('wind')) return 'vent';
  return 'neutral';
});
</script>
