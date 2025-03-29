<template>
  <div class="main-layout">
    <header class="top-nav">
      <div class="nav-left">
        <img src="../assets/images/logo1.png" alt="Logo" class="header-logo" />
        <span class="header-title">AI Chat</span>
      </div>
      <div class="nav-right">
        <button class="nav-link button-as-link" @click="handleNavigation('about')">关于</button>
        <span>🌙</span>
        <span>⚙️</span>
      </div>
    </header>

    <div class="layout-body">
      <Sidebar :active-view-prop="currentView" @navigate="handleNavigation" />

      <main class="content-area">
        <div class="dynamic-component-container">
          <keep-alive v-if="activeComponent">
            <component :is="activeComponent" />
          </keep-alive>
          <div v-else class="placeholder-content">
            无法加载视图: {{ currentView }}
          </div>
        </div>
      </main>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, markRaw, defineProps, onMounted } from 'vue';
import Sidebar from './Sidebar.vue';
import ChatInterface from './ChatInterface.vue';
import ConfigEditor from './ConfigEditor.vue';
import AboutPage from './AboutPage.vue';

const props = defineProps({
  initialViewProp: {
    type: String,
    required: false,
    default: 'chat'
  }
});


const currentView = ref(props.initialViewProp);


console.log(`MainLayout setup. Initial view from prop: "${props.initialViewProp}", currentView set to: "${currentView.value}"`);


const viewComponents = {
  chat: markRaw(ChatInterface),
  config: markRaw(ConfigEditor),
  about: markRaw(AboutPage),
};


const activeComponent = computed(() => {
  const component = viewComponents[currentView.value] || null;
  return component;
});


function handleNavigation(viewId) {
  console.log(`MainLayout received navigate request for: "${viewId}"`);
  if (viewComponents[viewId]) {
    if (currentView.value !== viewId) {
      currentView.value = viewId;
      console.log(`MainLayout currentView changed to: "${currentView.value}"`);
    } else {
      console.log(`Already on view: "${viewId}". No change needed.`);
    }
  } else {
    console.warn(`Invalid view ID received in handleNavigation: ${viewId}`);
  }
}

onMounted(() => {
  console.log(`MainLayout onMounted hook. currentView is: "${currentView.value}"`);
});

</script>

<style scoped>
.main-layout {
  display: flex;
  flex-direction: column;
  height: 100vh;
  background-color: #121212;
  overflow: hidden;
}

.top-nav {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 12px 60px;
  position: relative;
  z-index: 10;
  background-color: rgba(25, 25, 25, 0.6);
  backdrop-filter: blur(8px);
  border-bottom: 1px solid #303030;
  flex-shrink: 0;
}
.nav-left { display: flex; align-items: center; gap: 10px; }
.header-logo { height: 28px; vertical-align: middle; }
.header-title { font-size: 1.1em; font-weight: 600; color: #fff; }
.nav-right { display: flex; align-items: center; gap: 18px; }
.nav-link {
  color: #b0b0b0;
  text-decoration: none;
  font-size: 0.85em;
  transition: color 0.2s ease, background-color 0.2s ease;
  background: none;
  border: none;
  padding: 6px 12px;
  cursor: pointer;
  font-family: inherit;
  border-radius: 6px;
  line-height: 1;
}
.nav-link:hover {
  color: #fff;
  background-color: rgba(255, 255, 255, 0.1);
}
.button-as-link {
}
.nav-right span {
  cursor: pointer;
  font-size: 1.1em;
  color: #b0b0b0;
  padding: 5px;
  display: inline-flex;
  align-items: center;
  justify-content: center;
}
.nav-right span:hover { color: #fff; }

.layout-body {
  display: flex;
  flex-grow: 1;
  overflow: hidden;
}


.layout-body > :deep(.sidebar) {
  flex-shrink: 0;
}

.content-area {
  flex-grow: 1;
  background-color: #181818;
  display: flex;
  overflow: hidden;
  position: relative;
}


.dynamic-component-container {
  position: absolute;
  inset: 0;
  overflow-y: auto;
  overflow-x: hidden;
  box-sizing: border-box;
}

.placeholder-content {
  display: flex;
  justify-content: center;
  align-items: center;
  height: 100%;
  color: #666;
  font-size: 1.1em;
  padding: 20px;
  text-align: center;
}

.dynamic-component-container > :deep(.chat-interface-wrapper),
.dynamic-component-container > :deep(.config-editor-container),
.dynamic-component-container > :deep(.about-page-container)
{
  width: 100%;
  min-height: 100%;
  height: fit-content;
  box-sizing: border-box;
  background-color: #181818;
  display: flex;
  flex-direction: column;
}

.dynamic-component-container::-webkit-scrollbar { width: 8px; }
.dynamic-component-container::-webkit-scrollbar-track { background: #2a2a2a; }
.dynamic-component-container::-webkit-scrollbar-thumb { background: #555; border-radius: 4px; }
.dynamic-component-container::-webkit-scrollbar-thumb:hover { background: #666; }

</style>