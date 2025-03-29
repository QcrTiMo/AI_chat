<script setup>
import { ref } from 'vue'; 
import WelcomeScreen from './components/WelcomeScreen.vue';
import MainLayout from './components/MainLayout.vue';
const showAppLayout = ref(false);
const initialView = ref('chat');

function handleStartChat() {
  console.log('App received start-chat event');
  initialView.value = 'chat'; 
  showAppLayout.value = true;
  console.log('App state updated: showAppLayout=', showAppLayout.value, 'initialView=', initialView.value); 
}

function handleGoToAbout() {
  console.log('App received go-to-about event'); 
  initialView.value = 'about'; 
  showAppLayout.value = true;  
  console.log('App state updated: showAppLayout=', showAppLayout.value, 'initialView=', initialView.value); 
}
</script>

<template>
  <div id="main-app">
    <WelcomeScreen
        v-if="!showAppLayout"
        @start-chat="handleStartChat"
        @go-to-about="handleGoToAbout"
    />
    <MainLayout v-else :initial-view-prop="initialView" />
  </div>
</template>

<style>
body {
  margin: 0;
  background-color: #121212;
  color: #e0e0e0;
  font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, Helvetica, Arial, sans-serif; 
}

#main-app {
  min-height: 100vh;
}
</style>
