<template>
  <div
    class="monster-illustration"
    :class="[
      `mi-size-${sizeClass}`,
      `mi-type-${typeClass}`,
      `mi-rarity-${rarityClass}`,
      `mi-face-${faceVariant}`,
      `mi-pattern-${patternVariant}`,
    ]"
    :style="{ '--mi-hue': `${hueShift}deg` }"
  >
    <div class="mi-aura"></div>
    <svg class="mi-svg" viewBox="0 0 120 120" role="img" aria-label="Illustration du monstre">
      <circle class="mi-bg-orb" cx="60" cy="60" r="52" />
      <circle class="mi-bg-orb-alt" cx="86" cy="34" r="14" />
      <circle class="mi-bg-orb-alt" cx="28" cy="88" r="10" />

      <g class="mi-creature" :style="{ transform: `translateY(${floatOffset}px) rotate(${tiltDeg}deg)` }">
        <path v-if="hornVariant === 0" class="mi-horn" d="M38 46 L24 36 L34 58 Z" />
        <path v-if="hornVariant === 0" class="mi-horn" d="M82 46 L96 36 L86 58 Z" />

        <path v-if="hornVariant === 1" class="mi-horn" d="M40 44 C30 28, 28 30, 36 54" />
        <path v-if="hornVariant === 1" class="mi-horn" d="M80 44 C90 28, 92 30, 84 54" />

        <path v-if="hornVariant === 2" class="mi-horn" d="M33 55 C22 45, 22 34, 33 33" />
        <path v-if="hornVariant === 2" class="mi-horn" d="M87 55 C98 45, 98 34, 87 33" />

        <path v-if="crestVariant === 0" class="mi-crest" d="M60 28 L68 44 L52 44 Z" />
        <path v-if="crestVariant === 1" class="mi-crest" d="M50 34 C55 22, 65 22, 70 34" />
        <circle v-if="crestVariant === 2" class="mi-crest-gem" cx="60" cy="36" r="5" />

        <circle class="mi-ear" cx="37" cy="62" r="10" />
        <circle class="mi-ear" cx="83" cy="62" r="10" />
        <circle class="mi-head" cx="60" cy="66" r="31" />

        <g v-if="faceVariant === 0">
          <circle class="mi-eye" :cx="60 - eyeOffset" cy="62" r="5.1" />
          <circle class="mi-eye" :cx="60 + eyeOffset" cy="62" r="5.1" />
          <circle class="mi-eye-pupil" :cx="60 - eyeOffset" cy="63" r="2.1" />
          <circle class="mi-eye-pupil" :cx="60 + eyeOffset" cy="63" r="2.1" />
        </g>

        <g v-else-if="faceVariant === 1">
          <path class="mi-eye-line" :d="`M${52 - eyeOffset / 2} 62 Q${60 - eyeOffset} 57 ${68 - eyeOffset / 2} 62`" />
          <path class="mi-eye-line" :d="`M${52 + eyeOffset / 2} 62 Q${60 + eyeOffset} 57 ${68 + eyeOffset / 2} 62`" />
        </g>

        <g v-else-if="faceVariant === 2">
          <circle class="mi-eye" cx="60" cy="62" r="7.2" />
          <circle class="mi-eye-pupil" cx="60" cy="63" r="2.8" />
        </g>

        <g v-else>
          <circle class="mi-eye" :cx="60 - eyeOffset" cy="62" r="5.1" />
          <circle class="mi-eye-pupil" :cx="60 - eyeOffset" cy="63" r="2.1" />
          <path class="mi-eye-line" :d="`M${52 + eyeOffset / 2} 62 Q${60 + eyeOffset} 59 ${68 + eyeOffset / 2} 62`" />
        </g>

        <g v-if="patternVariant === 0">
          <circle class="mi-mark" cx="52" cy="72" r="3.5" />
          <circle class="mi-mark" cx="67" cy="73" r="2.8" />
        </g>

        <g v-else-if="patternVariant === 1">
          <path class="mi-mark-line" d="M47 71 L73 71" />
          <path class="mi-mark-line" d="M49 75 L71 75" />
        </g>

        <g v-else>
          <path class="mi-mark-line" d="M54 71 Q60 67 66 71" />
          <path class="mi-mark-line" d="M53 76 Q60 82 67 76" />
        </g>

        <path v-if="mouthVariant === 0" class="mi-mouth" d="M49 78 Q60 88 71 78" />
        <path v-if="mouthVariant === 1" class="mi-mouth" d="M49 80 Q60 74 71 80" />
        <path v-if="mouthVariant === 2" class="mi-mouth" d="M50 79 L70 79" />
      </g>
    </svg>
    <small class="mi-name-tag">{{ initials }}</small>
  </div>
</template>

<script setup>
import { computed } from 'vue';

const props = defineProps({
  monster: {
    type: Object,
    required: true,
  },
  size: {
    type: String,
    default: 'card', // card | reveal | modal
  },
});

const rawType = computed(() => String(props.monster?.typeLabel || props.monster?.type || props.monster?.elementType || 'neutral').toLowerCase());
const rawRarity = computed(() => String(props.monster?.rarity || 'common').toLowerCase());

const typeClass = computed(() => {
  if (rawType.value.includes('eau') || rawType.value.includes('water')) return 'eau';
  if (rawType.value.includes('feu') || rawType.value.includes('fire')) return 'feu';
  if (rawType.value.includes('vent') || rawType.value.includes('wind')) return 'vent';
  return 'neutral';
});

const rarityClass = computed(() => {
  if (rawRarity.value.includes('legend')) return 'legendary';
  if (rawRarity.value.includes('epic')) return 'epic';
  if (rawRarity.value.includes('rare')) return 'rare';
  return 'common';
});

const initials = computed(() => {
  const src = String(props.monster?.name || 'M').trim();
  return src.slice(0, 2).toUpperCase();
});

const seed = computed(() => {
  const txt = String(props.monster?.name || 'monster');
  return [...txt].reduce((acc, c) => acc + c.charCodeAt(0), 0);
});

const eyeOffset = computed(() => 8 + (seed.value % 5));
const hornVariant = computed(() => seed.value % 3);
const patternVariant = computed(() => (seed.value >> 1) % 3);
const faceVariant = computed(() => (seed.value >> 2) % 4);
const crestVariant = computed(() => (seed.value >> 3) % 3);
const mouthVariant = computed(() => (seed.value >> 4) % 3);
const floatOffset = computed(() => (seed.value % 3) - 1);
const tiltDeg = computed(() => ((seed.value % 5) - 2) * 0.7);
const hueShift = computed(() => (seed.value % 28) - 14);

const sizeClass = computed(() => (['card', 'reveal', 'modal'].includes(props.size) ? props.size : 'card'));
</script>
