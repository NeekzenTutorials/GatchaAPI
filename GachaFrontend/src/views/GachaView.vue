<template>
  <section class="panel glass">
    <div class="gacha-header">
      <h2>Portail d’Invocation</h2>
      <p>Canalise l’énergie astrale pour découvrir un nouveau monstre.</p>
    </div>

    <div class="gacha-tools">
      <label class="sound-toggle">
        <input v-model="soundEnabled" type="checkbox" />
        <span>Son cinématique</span>
      </label>
    </div>

    <div class="gacha-action-wrap">
      <button class="btn-summon" :disabled="invocationStore.isSummoning" @click="onSummon">
        {{ invocationStore.isSummoning ? 'Invocation...' : 'Invoquer' }}
      </button>
    </div>

    <Transition name="wheel-pop">
      <div v-if="wheelActive" class="summon-wheel-stage" :class="{ revealed: wheelRevealReady }">
        <div class="summon-wheel">
          <div class="wheel-ring ring-a"></div>
          <div class="wheel-ring ring-b"></div>
          <div class="wheel-ring ring-c"></div>
          <div class="wheel-center">
            <MonsterIllustration
              v-if="wheelMonster && wheelRevealReady"
              :monster="wheelMonster"
              size="reveal"
            />
            <span v-else class="wheel-glyph">✦</span>
          </div>
        </div>
        <p class="wheel-caption">{{ wheelRevealReady ? 'Créature matérialisée !' : 'Le portail tourne…' }}</p>
      </div>
    </Transition>

    <Transition name="reveal">
      <SummonReveal v-if="showReveal && invocationStore.lastSummoned" :monster="invocationStore.lastSummoned" />
    </Transition>

    <div class="template-block" v-if="invocationStore.templates.length">
      <h3>Taux d’apparition</h3>
      <div class="template-list">
        <article
          v-for="(tpl, index) in invocationStore.templates"
          :key="`${tpl.id || tpl._id || tpl.rarity}-${index}`"
          class="template-item"
          :class="`rarity-${String(tpl.rarity || tpl.tier || 'common').toLowerCase()}`"
        >
          <strong>{{ tpl.name || tpl.rarity || tpl.tier || 'Template' }}</strong>
          <span>{{ tpl.rate || tpl.probability || tpl.chance || '?' }}%</span>
        </article>
      </div>
    </div>

    <div class="history-block">
      <h3>Historique des invocations</h3>
      <div v-if="invocationStore.history.length" class="history-list">
        <article
          v-for="(item, index) in invocationStore.history"
          :key="`${item.id || item._id || item.name}-${index}`"
          class="history-item"
          :class="`rarity-${String(item.rarity || 'common').toLowerCase()}`"
        >
          <strong>{{ formatInvocationName(item) }}</strong>
          <small class="history-time">{{ formatDate(item.summonedAt) }}</small>
        </article>
      </div>
      <p v-else class="empty-state">Aucune invocation pour le moment.</p>
    </div>

    <PremiumSummonCinematic
      :open="showCinematic"
      :monster="invocationStore.lastSummoned"
      @close="showCinematic = false"
    />
  </section>
</template>

<script setup>
import { onMounted, ref } from 'vue';
import SummonReveal from '../components/SummonReveal.vue';
import PremiumSummonCinematic from '../components/PremiumSummonCinematic.vue';
import MonsterIllustration from '../components/MonsterIllustration.vue';
import { useInvocationStore } from '../stores/invocationStore';
import { formatElementTypeLabel } from '../utils/monster';

const invocationStore = useInvocationStore();
const showReveal = ref(false);
const showCinematic = ref(false);
const soundEnabled = ref(true);
const wheelActive = ref(false);
const wheelRevealReady = ref(false);
const wheelMonster = ref(null);

onMounted(() => {
  invocationStore.fetchTemplates();
  invocationStore.fetchHistory();
});

function playRarityFx(rarity) {
  if (!soundEnabled.value) return;
  const AudioCtx = window.AudioContext || window.webkitAudioContext;
  if (!AudioCtx) return;

  const ctx = new AudioCtx();
  const osc = ctx.createOscillator();
  const gain = ctx.createGain();

  const label = String(rarity || 'common').toLowerCase();
  const frequency = label.includes('legend') ? 920 : label.includes('epic') ? 760 : label.includes('rare') ? 620 : 510;

  osc.type = 'sine';
  osc.frequency.setValueAtTime(frequency, ctx.currentTime);
  gain.gain.setValueAtTime(0.0001, ctx.currentTime);
  gain.gain.exponentialRampToValueAtTime(0.12, ctx.currentTime + 0.03);
  gain.gain.exponentialRampToValueAtTime(0.0001, ctx.currentTime + 0.55);

  osc.connect(gain);
  gain.connect(ctx.destination);
  osc.start();
  osc.stop(ctx.currentTime + 0.56);
  osc.onended = () => ctx.close();
}

function formatDate(value) {
  if (!value) return 'maintenant';
  const date = new Date(value);
  if (Number.isNaN(date.getTime())) return 'maintenant';
  return date.toLocaleString('fr-FR', {
    day: '2-digit',
    month: '2-digit',
    hour: '2-digit',
    minute: '2-digit',
  });
}

function formatInvocationName(item) {
  const raw = String(item?.displayName || item?.name || item?.monsterName || 'Monstre');

  // 1) Normalise les séparateurs
  let text = raw
    .replace(/([a-zà-ÿ])([A-ZÀ-Ÿ])/g, '$1 $2')
    .replace(/[_-]+/g, ' ')
    .replace(/\s+/g, ' ')
    .trim();

  // 2) Retire les suffixes "élément + rareté" en fin de chaîne (répété si besoin)
  const suffixRx = /(\s+(water|fire|wind|earth|light|dark|neutral|neutre|eau|feu|vent|terre|lumiere|lumière|tenebre|ténèbre))?(\s+(legendary|epic|rare|common))$/i;
  while (suffixRx.test(text)) {
    text = text.replace(suffixRx, '').trim();
  }

  // 3) Évite les doublons adjacents "Vent Vent" / "VentVent"
  text = text
    .replace(/\b(Eau|Feu|Vent|Terre|Lumière|Ténèbre|Neutre)\s+\1\b/gi, '$1')
    .replace(/\b(Eau|Feu|Vent|Terre|Lumière|Ténèbre|Neutre)\1\b/gi, '$1')
    .trim();

  return text || 'Monstre';
}

async function onSummon() {
  showReveal.value = false;
  showCinematic.value = false;
  wheelActive.value = true;
  wheelRevealReady.value = false;
  wheelMonster.value = null;

  const minWheelDuration = new Promise((resolve) => setTimeout(resolve, 1850));

  try {
    const monster = await invocationStore.summon();
    await minWheelDuration;

    if (monster) {
      playRarityFx(monster.rarity);
      wheelMonster.value = monster;
      wheelRevealReady.value = true;

      await new Promise((resolve) => setTimeout(resolve, 950));
      wheelActive.value = false;
      showReveal.value = true;
      showCinematic.value = true;
    } else {
      wheelActive.value = false;
    }
  } catch {
    wheelActive.value = false;
  }
}
</script>
