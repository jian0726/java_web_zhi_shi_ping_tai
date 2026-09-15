<template>
  <div class="creator-layout">
    <header class="bar">好YOU · 创作端</header>
    <main class="content"><router-view /></main>
  </div>
</template>

<script setup lang="ts"></script>

<style scoped>
.creator-layout {
  min-height: 100%;
}
.bar {
  height: 56px;
  display: flex;
  align-items: center;
  padding: 0 24px;
  background: var(--brand);
  color: #fff;
  font-weight: 500;
}
.content {
  padding: 24px;
}
</style>
