<template>
  <div class="toast-container">
    <article
      v-for="toast in uiStore.toasts"
      :key="toast.id"
      class="toast"
      :class="`toast-${toast.type}`"
      @click="uiStore.removeToast(toast.id)"
    >
      {{ toast.message }}
    </article>
  </div>
</template>

<script setup>
import { useUiStore } from '../stores/uiStore';

const uiStore = useUiStore();
</script>
