<template>
  <section class="panel glass">
    <div class="section-header">
      <h2>Mes Monstres</h2>
      <button class="btn-secondary" @click="monsterStore.fetchMonsters">Rafraîchir</button>
    </div>

    <div class="monster-grid">
      <MonsterCard
        v-for="monster in monsterStore.monsters"
        :key="monster.id || monster._id"
        :monster="monster"
        @details="monsterStore.openMonsterDetail"
      />
    </div>

    <p v-if="!monsterStore.monsters.length" class="empty-state">Aucun monstre invoqué pour le moment.</p>

    <MonsterDetailModal
      :open="monsterStore.isModalOpen"
      :monster="monsterStore.selectedMonster"
      @close="monsterStore.closeModal"
    />
  </section>
</template>

<script setup>
import { onMounted } from 'vue';
import MonsterCard from '../components/MonsterCard.vue';
import MonsterDetailModal from '../components/MonsterDetailModal.vue';
import { useMonsterStore } from '../stores/monsterStore';

const monsterStore = useMonsterStore();

onMounted(() => {
  monsterStore.fetchMonsters();
});
</script>
