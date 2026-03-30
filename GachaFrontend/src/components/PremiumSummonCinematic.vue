<template>
  <Transition name="cinematic-fade">
    <div v-if="open" class="cinematic-overlay" :class="`rarity-${rarityClass}`" @click.self="emit('close')">
      <div class="cinematic-particles"></div>

      <section ref="panelRef" class="cinematic-panel glass">
        <p ref="phaseRef" class="cinematic-phase">Rituel en cours…</p>

        <div ref="sigilRef" class="summon-sigil">
          <div class="sigil-ring ring-a"></div>
          <div class="sigil-ring ring-b"></div>
          <div class="sigil-core"></div>
        </div>

        <article ref="cardRef" class="cinematic-card" :class="`rarity-${rarityClass}`">
          <span class="cinematic-rarity">{{ rarityLabel }}</span>
          <h3>{{ monster?.name || '???' }}</h3>
          <p class="type-pill" :class="`type-${typeClass}`">{{ monster?.typeDisplay || monster?.typeLabel || monster?.type || monster?.elementType || 'neutre' }}</p>

          <div class="stats-grid reveal-stats">
            <span>HP: {{ monster?.hp ?? '-' }}</span>
            <span>ATK: {{ monster?.atk ?? '-' }}</span>
            <span>DEF: {{ monster?.def ?? '-' }}</span>
            <span>SPD: {{ monster?.speed ?? '-' }}</span>
          </div>
        </article>

        <button ref="closeBtnRef" class="btn-primary cinematic-close" @click="emit('close')">
          Continuer
        </button>
      </section>
    </div>
  </Transition>
</template>

<script setup>
import { computed, nextTick, onBeforeUnmount, ref, watch } from 'vue';
import { gsap } from 'gsap';

const props = defineProps({
  open: Boolean,
  monster: {
    type: Object,
    default: null,
  },
});

const emit = defineEmits(['close']);

const panelRef = ref(null);
const sigilRef = ref(null);
const cardRef = ref(null);
const closeBtnRef = ref(null);
const phaseRef = ref(null);

let timeline;

const rarityClass = computed(() => String(props.monster?.rarity || 'common').toLowerCase());
const rarityLabel = computed(() => String(props.monster?.rarity || 'Common'));
const typeClass = computed(() => {
  const raw = String(props.monster?.typeLabel || props.monster?.type || props.monster?.elementType || 'neutral').toLowerCase();
  if (raw.includes('eau') || raw.includes('water')) return 'eau';
  if (raw.includes('feu') || raw.includes('fire')) return 'feu';
  if (raw.includes('vent') || raw.includes('wind')) return 'vent';
  return 'neutral';
});

function getRevealMessage() {
  if (rarityClass.value.includes('legend')) return 'Anomalie astrale détectée !';
  if (rarityClass.value.includes('epic')) return 'Pic d’énergie mystique…';
  if (rarityClass.value.includes('rare')) return 'Flux rare détecté…';
  return 'Invocation stabilisée';
}

function runTimeline() {
  if (!panelRef.value || !sigilRef.value || !cardRef.value || !closeBtnRef.value || !phaseRef.value) return;
  phaseRef.value.textContent = 'Rituel en cours…';

  gsap.set([panelRef.value, cardRef.value, closeBtnRef.value], { opacity: 0 });
  gsap.set(panelRef.value, { y: 30, scale: 0.98 });
  gsap.set(sigilRef.value, { scale: 0.5, opacity: 0 });
  gsap.set(cardRef.value, { rotateY: -65, scale: 0.8, y: 30 });
  gsap.set(closeBtnRef.value, { y: 20 });

  timeline?.kill();
  timeline = gsap.timeline();

  timeline
    .to(panelRef.value, { opacity: 1, y: 0, scale: 1, duration: 0.35, ease: 'power2.out' })
    .to(sigilRef.value, { opacity: 1, scale: 1, duration: 0.45, ease: 'back.out(1.5)' }, '<0.05')
    .to(sigilRef.value, { rotate: 360, duration: 1.2, ease: 'none' }, '<')
    .add(() => {
      phaseRef.value.textContent = getRevealMessage();
    }, '>-0.5')
    .to(cardRef.value, { opacity: 1, rotateY: 0, scale: 1, y: 0, duration: 0.65, ease: 'power4.out' }, '>-0.1')
    .to(closeBtnRef.value, { opacity: 1, y: 0, duration: 0.3, ease: 'power2.out' }, '>-0.1');
}

watch(
  () => props.open,
  async (isOpen) => {
    if (isOpen) {
      await nextTick();
      runTimeline();
    } else {
      timeline?.kill();
    }
  }
);

onBeforeUnmount(() => {
  timeline?.kill();
});
</script>
