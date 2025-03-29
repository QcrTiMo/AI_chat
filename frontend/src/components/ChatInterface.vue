<script setup>
import { ref, watch, nextTick } from 'vue';
const newMessage = ref('');
const chatHistory = ref([]);
const isLoading = ref(false);
const errorMessage = ref('');
const apiUrl = 'http://localhost:8080/api/chat';
const chatHistoryRef = ref(null);

async function sendMessage() {
  const messageText = newMessage.value.trim();
  if (!messageText || isLoading.value) return;
  chatHistory.value.push({ sender: 'user', text: messageText });
  const messageToSend = messageText;
  newMessage.value = '';
  isLoading.value = true;
  errorMessage.value = '';
  try {
    const response = await fetch(apiUrl, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ message: messageToSend }),
    });
    if (!response.ok) {
      let errorData = null;
      try { errorData = await response.json(); } catch (e) { /* ignore */ }
      throw new Error(`HTTP ${response.status}: ${errorData?.error || response.statusText}`);
    }
    const data = await response.json();
    if (data && data.reply) {
      chatHistory.value.push({ sender: 'ai', text: data.reply });
    } else {
      throw new Error("服务器响应格式无效。");
    }
  } catch (error) {
    console.error('Error sending/processing message:', error);
    errorMessage.value = `错误: ${error.message}`;
  } finally {
    isLoading.value = false;
    scrollToBottom();
  }
}

const scrollToBottom = async () => {
  await nextTick();
  const container = chatHistoryRef.value;
  if (container) container.scrollTop = container.scrollHeight;
};

watch(chatHistory, scrollToBottom, { deep: true });
watch([isLoading, errorMessage], scrollToBottom);
</script>

<template>
  <div class="chat-interface-wrapper">
    <div class="chat-history" ref="chatHistoryRef">
      <div v-if="chatHistory.length === 0 && !isLoading" class="empty-chat">
        开始聊天吧！在下方输入你的消息。
      </div>
      <div v-for="(message, index) in chatHistory" :key="index" class="message" :class="message.sender">
        <span class="sender">{{ message.sender === 'user' ? '你' : 'AI' }}:</span>
        <span class="text">{{ message.text }}</span>
      </div>
      <!-- Loading/Error messages -->
      <div v-if="isLoading" class="message system">AI 正在思考...</div>
      <div v-if="errorMessage" class="message system error">{{ errorMessage }}</div>
    </div>

    <!-- Chat Input Container - Fixed at the bottom -->
    <div class="chat-input-container">
      <div class="chat-input">
        <input
            type="text"
            v-model="newMessage"
            @keyup.enter="sendMessage"
            placeholder="在这里输入消息..."
            :disabled="isLoading"
        />
        <button @click="sendMessage" :disabled="isLoading || !newMessage.trim()">
          {{ isLoading ? '发送中...' : '发送' }}
        </button>
      </div>
    </div>

  </div>
</template>

<style scoped>
.chat-interface-wrapper {
  position: relative;
  height: 100%;
  width: 100%;
  overflow: hidden;
  background-color: #181818;
}

.chat-history {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 75px;
  overflow-y: auto;
  padding: 15px;
  padding-bottom: 10px;
  background-color: #1e1e1e;
  color: #ccc;
  display: flex;
  flex-direction: column;
}
.chat-history::-webkit-scrollbar { width: 6px; }
.chat-history::-webkit-scrollbar-track { background: #1e1e1e; }
.chat-history::-webkit-scrollbar-thumb { background: #444; border-radius: 3px; }
.chat-history::-webkit-scrollbar-thumb:hover { background: #555; }

.chat-input-container {
  position: absolute;
  bottom: 0;
  left: 0;
  right: 0;
  z-index: 10;
  padding: 15px 20px;
  box-sizing: border-box;
  background: linear-gradient(to top, rgba(37, 37, 37, 1) 70%, rgba(37, 37, 37, 0.8));
  border-top: 1px solid #353535;
}
.chat-input {
  display: flex;
  gap: 10px;
  width: 100%;
}
.message { max-width: 85%; padding: 10px 14px; margin-bottom: 12px; border-radius: 18px; word-wrap: break-word; line-height: 1.4; }
.message.user { background-color: #3b5998; color: #fff; border: none; align-self: flex-end; margin-left: auto; }
.message.ai { background-color: #303030; color: #e0e0e0; border: 1px solid #404040; align-self: flex-start; margin-right: auto; }
.message .text { color: inherit; }
.message .sender { font-weight: bold; display: block; margin-bottom: 4px; font-size: 0.8em; color: rgba(255, 255, 255, 0.6); }
.message.user .sender { color: rgba(255, 255, 255, 0.7); text-align: right;}

.empty-chat { color: #777; text-align: center; margin: auto; padding: 20px; }
.message.system { color: #888; font-style: italic; text-align: center; margin-bottom: 10px; align-self: center; background: none; border: none; padding: 5px;}
.message.system.error { color: #ff6b6b; font-style: normal; font-weight: bold;}

.chat-input input { background-color: #3a3a3a; border: 1px solid #505050; color: #e0e0e0; border-radius: 20px; flex-grow: 1; padding: 12px 18px; font-size: 1em;}
.chat-input input::placeholder { color: #888; }
.chat-input input:focus { outline: none; border-color: #007bff; box-shadow: 0 0 0 2px rgba(0, 123, 255, 0.3);}
.chat-input input:disabled { background-color: #444; cursor: not-allowed; opacity: 0.6; }

.chat-input button { background-color: #007bff; border-radius: 20px; padding: 12px 20px; border: none; color: white; cursor: pointer; font-size: 1em; font-weight: bold; transition: background-color 0.2s;}
.chat-input button:hover { background-color: #0056b3;}
.chat-input button:disabled { background-color: #555; cursor: not-allowed; opacity: 0.7;}
</style>