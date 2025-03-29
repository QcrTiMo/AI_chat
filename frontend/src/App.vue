<script setup>
import { ref } from 'vue'; // 确保 ref 被导入
import WelcomeScreen from './components/WelcomeScreen.vue';
import MainLayout from './components/MainLayout.vue';

// --- State ---
// 控制显示哪个主界面 (Welcome or MainLayout)
const showAppLayout = ref(false);
// 控制 MainLayout 加载时应该显示哪个子视图 ('chat', 'config', 'about')
const initialView = ref('chat'); // <-- 添加 initialView ref，默认值为 'chat'

// --- Event Handlers ---
// 处理来自 WelcomeScreen 的 'start-chat' 事件
function handleStartChat() {
  console.log('App received start-chat event'); // 确认事件接收
  initialView.value = 'chat'; // 确保进入聊天视图
  showAppLayout.value = true; // 显示主布局
  console.log('App state updated: showAppLayout=', showAppLayout.value, 'initialView=', initialView.value); // 确认状态更新
}

// --- 添加缺失的 handleGoToAbout 函数 ---
function handleGoToAbout() {
  console.log('App received go-to-about event'); // 确认事件接收
  initialView.value = 'about'; // 设置目标视图为 'about'
  showAppLayout.value = true;  // 显示主布局
  console.log('App state updated: showAppLayout=', showAppLayout.value, 'initialView=', initialView.value); // 确认状态更新
}
// --- 结束添加 ---

</script>

<template>
  <div id="main-app">
    <!-- WelcomeScreen 监听两个事件 -->
    <WelcomeScreen
        v-if="!showAppLayout"
        @start-chat="handleStartChat"
        @go-to-about="handleGoToAbout"
    />
    <!-- MainLayout 接收 initialView 作为 prop -->
    <MainLayout v-else :initial-view-prop="initialView" />
  </div>
</template>

<style>
/* Global styles */
body {
  margin: 0;
  background-color: #121212;
  color: #e0e0e0;
  font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, Helvetica, Arial, sans-serif; /* Added default font */
}

#main-app {
  min-height: 100vh;
}
</style>