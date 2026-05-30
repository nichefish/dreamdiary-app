<template>
  <JournalAnnualDetailAside v-if="route.name === 'annual-detail'" @close="emit('close')" />
  <JournalAnnualListAside v-else @close="emit('close')" />
</template>

<script setup lang="ts">
import { useRoute } from "vue-router";
import JournalAnnualDetailAside from "./JournalAnnualDetailAside.vue";
import JournalAnnualListAside from "./JournalAnnualListAside.vue";

const emit = defineEmits<{ close: [] }>();
const route = useRoute();
</script>
