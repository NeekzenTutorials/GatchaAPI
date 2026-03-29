<template>
  <section class="panel glass">
    <div class="section-header">
      <h2>Profil du joueur</h2>
      <button class="btn-secondary" @click="playerStore.refresh">Rafraîchir</button>
    </div>

    <div class="profile-grid">
      <article class="metric-card">
        <p>Pseudo</p>
        <h3>{{ profile.username || profile.pseudo || 'Invocateur' }}</h3>
      </article>

      <article class="metric-card">
        <p>ID joueur</p>
        <h3>{{ profile.id || '-' }}</h3>
      </article>

      <article class="metric-card">
        <p>Niveau</p>
        <h3>{{ playerStore.level }}</h3>
      </article>

      <article class="metric-card">
        <p>XP actuelle</p>
        <h3>{{ playerStore.currentXp }}</h3>
      </article>

      <article class="metric-card">
        <p>XP prochain niveau</p>
        <h3>{{ playerStore.xpToNext }}</h3>
      </article>

      <article class="metric-card">
        <p>Progression</p>
        <h3>{{ playerStore.progressPercent }}%</h3>
      </article>

      <article class="metric-card">
        <p>Monstres max</p>
        <h3>{{ profile.maxMonsters ?? '-' }}</h3>
      </article>

      <article class="metric-card">
        <p>Monstres possédés</p>
        <h3>{{ playerStore.monsterCount }}</h3>
      </article>

      <article class="metric-card">
        <p>Places restantes</p>
        <h3>{{ playerStore.monsterCapacityLeft }}</h3>
      </article>

      <article class="metric-card">
        <p>État niveau</p>
        <h3>{{ playerStore.canLevelUp ? 'Level up prêt' : 'En progression' }}</h3>
      </article>
    </div>

    <div class="xp-block">
      <div class="xp-head">
        <span>XP</span>
        <strong>{{ playerStore.currentXp }} / {{ playerStore.xpToNext }}</strong>
      </div>
      <div class="xp-track">
        <div class="xp-fill" :style="{ width: `${playerStore.progressPercent}%` }"></div>
      </div>
    </div>

    <div class="xp-block" v-if="monsterIds.length">
      <div class="xp-head">
        <span>IDs de monstres possédés</span>
        <strong>{{ monsterIds.length }}</strong>
      </div>
      <div class="monster-chip-list">
        <span class="monster-chip" v-for="monsterId in monsterIds" :key="monsterId">{{ monsterId }}</span>
      </div>
    </div>
  </section>
</template>

<script setup>
import { computed, onMounted } from 'vue';
import { usePlayerStore } from '../stores/playerStore';

const playerStore = usePlayerStore();

onMounted(() => {
  playerStore.refresh();
});

const profile = computed(() => playerStore.profile || {});
const monsterIds = computed(() => (Array.isArray(profile.value.monsters) ? profile.value.monsters : []));
</script>
