<script setup>
import { ref, onMounted, onUnmounted } from 'vue';
const emit = defineEmits(['start-chat', 'go-to-about']);
function startChat() {
  console.log('WelcomeScreen emitting: start-chat');
  emit('start-chat');
}
function goToAboutPage() {
  console.log('WelcomeScreen emitting: go-to-about');
  emit('go-to-about');
}
function openGitHub() {
  const githubUrl = 'https://github.com/'; 
  console.log(`Opening GitHub URL: ${githubUrl}`);
  window.open(githubUrl, '_blank', 'noopener noreferrer'); 
}
const logoContainerRef = ref(null); 
const logoRef = ref(null);     
const maxTilt = 15;        

function handleMouseMove(event) {
  if (!logoContainerRef.value || !logoRef.value) return;

  const { left, top, width, height } = logoContainerRef.value.getBoundingClientRect();
  const mouseX = event.clientX - left;
  const mouseY = event.clientY - top;
  const rotateY = ((mouseX - width / 2) / (width / 2)) * maxTilt;
  const rotateX = ((mouseY - height / 2) / (height / 2)) * -maxTilt; 

  logoRef.value.style.transform = `perspective(1000px) rotateX(${rotateX}deg) rotateY(${rotateY}deg) scale3d(1.05, 1.05, 1.05)`;
}
function handleMouseLeave() {
  if (!logoRef.value) return; 
  logoRef.value.style.transform = 'perspective(1000px) rotateX(0deg) rotateY(0deg) scale3d(1, 1, 1)';
}
onMounted(() => {
  if (logoContainerRef.value) {
    logoContainerRef.value.addEventListener('mousemove', handleMouseMove);
    logoContainerRef.value.addEventListener('mouseleave', handleMouseLeave);
    console.log('WelcomeScreen tilt listeners added.');
  }
});
onUnmounted(() => {
  if (logoContainerRef.value) {
    logoContainerRef.value.removeEventListener('mousemove', handleMouseMove);
    logoContainerRef.value.removeEventListener('mouseleave', handleMouseLeave);
    console.log('WelcomeScreen tilt listeners removed.');
  }
});

</script>

<template>
  <div class="welcome-wrapper">
    <div class="aurora-background">
      <div class="aurora aurora-1"></div>
      <div class="aurora aurora-2"></div>
      <div class="aurora aurora-3"></div>
      <div class="aurora aurora-4"></div>
    </div>

    <header class="top-nav">
      <div class="nav-left">
        <img src="../assets/images/logo1.png" alt="Logo" class="header-logo" />
        <span class="header-title">AI Chat</span>
      </div>
      <div class="nav-right">
        <a href="#" class="nav-link">快速开始</a>
        <button class="nav-link button-as-link" @click="goToAboutPage">关于</button>
        <span>🌙</span>
        <span>⚙️</span>
      </div>
    </header>

    <main class="main-content">
      <div class="text-content">
        <h1 class="title">
          AI Chat<br />
          现代化的本地<br />
          聊天机器人实现
        </h1>
        <p class="subtitle">基于 Java & Vue 构建</p>
        <div class="button-group">
          <button class="start-button" @click="startChat">开始体验</button>
          <button class="github-button" @click="openGitHub">GitHub</button>
        </div>
      </div>
      <div class="logo-container" ref="logoContainerRef">
        <img src="../assets/images/logo1.png" alt="Large Logo" class="main-logo" ref="logoRef" />
      </div>
    </main>
  </div>
</template>

<style src="../assets/css/welcome-screen.css"></style>
<style scoped>
.button-as-link {
  background: none;
  border: none;
  padding: 0;
  margin: 0;
  color: #b0b0b0;
  text-decoration: none;
  font-size: 0.85em; 
  font-family: inherit; 
  cursor: pointer;
  transition: color 0.2s ease;
}
.button-as-link:hover {
  color: #fff; 
  text-decoration: none; 
}
</style>
