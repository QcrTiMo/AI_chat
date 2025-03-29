<script setup>
// Import necessary functions from Vue
import { ref, onMounted, onUnmounted } from 'vue';

// --- Define Events Emitted to Parent (App.vue) ---
// 'start-chat': User clicked the main "Start" button, go to main layout (chat view).
// 'go-to-about': User clicked the header "About" button, go to main layout (about view).
const emit = defineEmits(['start-chat', 'go-to-about']);

// --- Event Handlers ---
// Called by the main "开始体验" button
function startChat() {
  console.log('WelcomeScreen emitting: start-chat');
  emit('start-chat');
}

// Called by the "关于" button in the header
function goToAboutPage() {
  console.log('WelcomeScreen emitting: go-to-about');
  emit('go-to-about');
}

// Called by the "GitHub" button
function openGitHub() {
  const githubUrl = 'https://github.com/'; // <-- ** 请将 # 替换为你的 GitHub 仓库真实 URL! **
  console.log(`Opening GitHub URL: ${githubUrl}`);
  // Use window.open to open the link in a new tab
  window.open(githubUrl, '_blank', 'noopener noreferrer'); // Added security attributes
}


// --- Interactive Tilt Effect Logic ---
const logoContainerRef = ref(null); // Ref for the container triggering the effect
const logoRef = ref(null);        // Ref for the logo image itself
const maxTilt = 15;               // Maximum degrees for tilt effect

// Handles mouse movement over the logo container
function handleMouseMove(event) {
  if (!logoContainerRef.value || !logoRef.value) return; // Exit if refs aren't ready

  // Get dimensions and position of the container
  const { left, top, width, height } = logoContainerRef.value.getBoundingClientRect();
  // Calculate mouse position relative to the container's top-left corner
  const mouseX = event.clientX - left;
  const mouseY = event.clientY - top;

  // Calculate rotation based on mouse position relative to the center
  // Normalize position to a range of -1 to 1
  // RotateY is based on horizontal position, RotateX on vertical (inverted for natural feel)
  const rotateY = ((mouseX - width / 2) / (width / 2)) * maxTilt;
  const rotateX = ((mouseY - height / 2) / (height / 2)) * -maxTilt; // Invert X rotation

  // Apply the 3D transform with perspective and a slight scale increase
  logoRef.value.style.transform = `perspective(1000px) rotateX(${rotateX}deg) rotateY(${rotateY}deg) scale3d(1.05, 1.05, 1.05)`;
}

// Resets the logo transform when the mouse leaves the container
function handleMouseLeave() {
  if (!logoRef.value) return; // Exit if ref isn't ready
  // Reset transform back to initial state
  logoRef.value.style.transform = 'perspective(1000px) rotateX(0deg) rotateY(0deg) scale3d(1, 1, 1)';
}

// --- Lifecycle Hooks for Event Listeners ---
// Add event listeners when the component is mounted
onMounted(() => {
  if (logoContainerRef.value) {
    logoContainerRef.value.addEventListener('mousemove', handleMouseMove);
    logoContainerRef.value.addEventListener('mouseleave', handleMouseLeave);
    console.log('WelcomeScreen tilt listeners added.');
  }
});

// Remove event listeners when the component is unmounted to prevent memory leaks
onUnmounted(() => {
  if (logoContainerRef.value) {
    logoContainerRef.value.removeEventListener('mousemove', handleMouseMove);
    logoContainerRef.value.removeEventListener('mouseleave', handleMouseLeave);
    console.log('WelcomeScreen tilt listeners removed.');
  }
});

</script>

<template>
  <!-- Root container for the welcome screen -->
  <div class="welcome-wrapper">
    <!-- Background Aurora Effect Elements -->
    <div class="aurora-background">
      <div class="aurora aurora-1"></div>
      <div class="aurora aurora-2"></div>
      <div class="aurora aurora-3"></div>
      <div class="aurora aurora-4"></div>
    </div>

    <!-- Top Navigation Bar -->
    <header class="top-nav">
      <div class="nav-left">
        <!-- Replace with your actual logo path -->
        <img src="../assets/images/logo1.png" alt="Logo" class="header-logo" />
        <span class="header-title">AI Chat</span>
      </div>
      <div class="nav-right">
        <!-- Quick Start link (currently inactive) -->
        <a href="#" class="nav-link">快速开始</a>
        <!-- About button emitting 'go-to-about' -->
        <button class="nav-link button-as-link" @click="goToAboutPage">关于</button>
        <!-- Placeholder icons -->
        <span>🌙</span>
        <span>⚙️</span>
      </div>
    </header>

    <!-- Main Content Area (Text + Logo) -->
    <main class="main-content">
      <!-- Left side text content -->
      <div class="text-content">
        <h1 class="title">
          AI Chat<br />
          现代化的本地<br />
          聊天机器人实现
        </h1>
        <p class="subtitle">基于 Java & Vue 构建</p>
        <!-- Action Buttons -->
        <div class="button-group">
          <!-- Start Chat Button emitting 'start-chat' -->
          <button class="start-button" @click="startChat">开始体验</button>
          <!-- GitHub Button opening link -->
          <button class="github-button" @click="openGitHub">GitHub</button>
        </div>
      </div>
      <!-- Right side logo area with tilt effect -->
      <div class="logo-container" ref="logoContainerRef">
        <!-- Replace with your actual large logo path -->
        <img src="../assets/images/logo1.png" alt="Large Logo" class="main-logo" ref="logoRef" />
      </div>
    </main>

    <!-- Footer / Features Section (Removed previously) -->

  </div>
</template>

<!-- Import the separate CSS file for welcome screen styles -->
<!-- Make sure the path is correct relative to this component file -->
<style src="../assets/css/welcome-screen.css"></style>

<!-- Optional Scoped Styles -->
<style scoped>
/* --- Style for buttons that should look like links --- */
/* Ensure this style exists either here or in welcome-screen.css */
.button-as-link {
  background: none;
  border: none;
  padding: 0;
  margin: 0;
  color: #b0b0b0; /* Match link color */
  text-decoration: none;
  font-size: 0.85em; /* Match link size */
  font-family: inherit; /* Use surrounding font */
  cursor: pointer;
  transition: color 0.2s ease;
}
.button-as-link:hover {
  color: #fff; /* Match link hover color */
  text-decoration: none; /* Buttons typically don't underline */
}

/* --- Other specific scoped styles for WelcomeScreen if needed --- */
/* --- For example, if welcome-screen.css doesn't cover everything --- */

</style>