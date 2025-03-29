<template>
  <aside class="sidebar">
    <nav>

      <div class="nav-section">
        <button
            class="section-toggle"
            :class="{ active: activeView === 'chat' }"
            @click="navigateTo('chat')"
        >
          快速开始
        </button>
      </div>


      <div class="nav-section">
        <button
            class="section-toggle"
            :class="{ active: activeView === 'config' }"
            @click="navigateTo('config')"
        >
          基础配置
        </button>
      </div>
    </nav>
  </aside>
</template>

<script setup>
import { ref } from 'vue';


const emit = defineEmits(['navigate']);


const activeView = ref('chat');

function navigateTo(viewId) {
  if (activeView.value !== viewId) {
    console.log(`Navigating to: ${viewId}`);
    activeView.value = viewId;
    emit('navigate', viewId);
  }
}
</script>

<style scoped>

.sidebar {
  width: 260px; background-color: #1f1f1f; color: #a0a0a0;
  padding: 20px 0; height: 100%; overflow-y: auto;
  border-right: 1px solid #303030; font-size: 0.9em; user-select: none;
  flex-shrink: 0;
}
nav { padding: 0 10px; }
.nav-section { margin-bottom: 8px; }
.section-toggle {
  background: none; border: none; color: #c0c0c0; font-size: 1.0em;
  font-weight: 500; padding: 10px 12px; width: 100%; text-align: left;
  cursor: pointer; display: flex; justify-content: space-between;
  align-items: center; border-radius: 6px; transition: background-color 0.2s, color 0.2s;
}
.section-toggle:hover { background-color: #2a2a2a; color: #fff; }
.section-toggle.active { background-color: #3a3a3a; color: #fff; font-weight: 600; }

.sidebar::-webkit-scrollbar { width: 5px; }
.sidebar::-webkit-scrollbar-track { background: transparent; }
.sidebar::-webkit-scrollbar-thumb { background: #444; border-radius: 3px; }
.sidebar::-webkit-scrollbar-thumb:hover { background: #555; }
</style>