<script setup>
// --- Script setup remains the same ---
import { ref, watch, nextTick } from 'vue';
import { marked } from 'marked';
import DOMPurify from 'dompurify';
const newMessage = ref(''); const chatHistory = ref([]); const isLoading = ref(false); const errorMessage = ref(''); const apiUrl = 'http://localhost:8080/api/chat'; const chatHistoryRef = ref(null); const fileInputRef = ref(null); const selectedFile = ref(null); const previewImageUrl = ref(null);
function triggerFileInput() { fileInputRef.value?.click(); }
function handleFileChange(event) { const file = event.target.files?.[0]; if (file) { selectedFile.value = file; previewImageUrl.value = null; if (file.type.startsWith('image/')) { const reader = new FileReader(); reader.onload = (e) => { previewImageUrl.value = e.target?.result; newMessage.value = `[图片: ${file.name}] `; }; reader.onerror = (e) => { errorMessage.value = '无法预览图片。'; selectedFile.value = null; }; reader.readAsDataURL(file); } else { newMessage.value = `[文件: ${file.name}] `; } } if(fileInputRef.value) fileInputRef.value.value = ''; }
function removeSelectedFile() { selectedFile.value = null; previewImageUrl.value = null; newMessage.value = newMessage.value.replace(/\[(图片|文件):.*?\]\s*/, ''); }
async function sendMessage() { const messageText = newMessage.value.trim(); const imageToSendUrl = previewImageUrl.value; let textToSend = messageText; if ((!textToSend || textToSend.startsWith('[文件:') || textToSend.startsWith('[图片:')) && !imageToSendUrl) { if (!imageToSendUrl && (textToSend.startsWith('[文件:') || textToSend.startsWith('[图片:'))) newMessage.value = ''; return; } isLoading.value = true; errorMessage.value = ''; const userMessageEntry = { sender: 'user' }; if (textToSend && !(textToSend.startsWith('[文件:') && imageToSendUrl) && !(textToSend.startsWith('[图片:') && imageToSendUrl) ) userMessageEntry.text = textToSend; if (imageToSendUrl) userMessageEntry.imageUrl = imageToSendUrl; if (userMessageEntry.text || userMessageEntry.imageUrl) chatHistory.value.push(userMessageEntry); let textForBackend = messageText; if (imageToSendUrl && !messageText.includes(selectedFile.value?.name)) textForBackend = `[已发送图片: ${selectedFile.value?.name || '未知图片'}]`; else if (imageToSendUrl && messageText.includes(`[图片: ${selectedFile.value?.name}]`)) textForBackend = messageText; newMessage.value = ''; selectedFile.value = null; previewImageUrl.value = null; console.log(`发送消息到后端: "${textForBackend}"`); try { const response = await fetch(apiUrl, { method: 'POST', headers: { 'Content-Type': 'application/json' }, body: JSON.stringify({ message: textForBackend }) }); if (!response.ok) { let errorData=null; try{errorData=await response.json();}catch(e){} throw new Error(`HTTP ${response.status}: ${errorData?.error || response.statusText}`); } const data = await response.json(); if (data && data.reply) chatHistory.value.push({ sender: 'ai', text: data.reply }); else throw new Error("服务器响应格式无效。"); } catch (error) { console.error('发送/处理消息时出错:', error); errorMessage.value = `错误: ${error.message}`; } finally { isLoading.value = false; scrollToBottom(); } }
const scrollToBottom = async () => { await nextTick(); const c=chatHistoryRef.value; if(c) c.scrollTop = c.scrollHeight; };
function parseAndSanitize(rawText) { if (!rawText) return ''; marked.setOptions({ gfm: true, breaks: true }); const dirtyHtml = marked.parse(rawText); const cleanHtml = DOMPurify.sanitize(dirtyHtml, { USE_PROFILES: { html: true }, ADD_TAGS: ['pre', 'code'], ADD_ATTR: ['class'] }); return cleanHtml; }
watch(chatHistory, scrollToBottom, { deep: true }); watch([isLoading, errorMessage], scrollToBottom);
</script>

<template>
  <!-- Root wrapper sets the positioning context -->
  <div class="chat-interface-wrapper">

    <!-- Chat History Area - Scrolls independently above the input -->
    <div class="chat-history" ref="chatHistoryRef">
      <div v-if="chatHistory.length === 0 && !isLoading" class="empty-chat">开始聊天吧！</div>
      <div v-for="(message, index) in chatHistory" :key="index" class="message" :class="message.sender">
        <span class="sender">{{ message.sender === 'user' ? '你' : 'AI' }}:</span>
        <div v-if="message.text" class="text markdown-body" v-html="parseAndSanitize(message.text)"></div>
        <div v-if="message.imageUrl" class="image-attachment">
          <img :src="message.imageUrl" alt="聊天图片" class="chat-image"/>
        </div>
      </div>
      <div v-if="isLoading" class="message system">AI 正在思考...</div>
      <div v-if="errorMessage" class="message system error">{{ errorMessage }}</div>
    </div>

    <!-- Input Area Container - Absolutely positioned at the bottom -->
    <div class="chat-input-container">
      <div v-if="previewImageUrl" class="image-preview-area">
        <img :src="previewImageUrl" alt="图片预览" class="preview-image"/>
        <button @click="removeSelectedFile" class="remove-preview-button" title="移除图片">
          <svg viewBox="0 0 16 16"><path d="M2.146 2.854a.5.5 0 1 1 .708-.708L8 7.293l5.146-5.147a.5.5 0 0 1 .708.708L8.707 8l5.147 5.146a.5.5 0 0 1-.708.708L8 8.707l-5.146 5.147a.5.5 0 0 1-.708-.708L7.293 8z"/></svg>
        </button>
      </div>
      <div class="chat-input">
        <input type="file" ref="fileInputRef" @change="handleFileChange" style="display: none;" accept="image/*"/>
        <input type="text" v-model="newMessage" @keyup.enter="sendMessage" placeholder="输入消息..." :disabled="isLoading" class="text-input"/>
        <button @click="triggerFileInput" class="icon-button attach-button" title="添加图片" :disabled="isLoading">
          <svg viewBox="0 0 16 16"><path d="M4.5 3a2.5 2.5 0 0 1 5 0v9a1.5 1.5 0 0 1-3 0V5a.5.5 0 0 1 1 0v7a.5.5 0 0 0 1 0V3a1.5 1.5 0 1 0-3 0v9a2.5 2.5 0 0 0 5 0V5a.5.5 0 0 1 1 0v7a3.5 3.5 0 1 1-7 0z"/></svg>
        </button>
        <button @click="sendMessage" :disabled="isLoading || (!newMessage.trim() && !selectedFile)" class="icon-button send-button">
          <svg viewBox="0 0 16 16"><path d="M15.964.686a.5.5 0 0 0-.65-.65L.767 5.855H.766l-.452.18a.5.5 0 0 0-.082.887l.41.26.001.002 4.995 3.178 3.178 4.995.002.002.26.41a.5.5 0 0 0 .886-.083l6-15Zm-1.833 1.89L6.637 10.07l-.215-.338a.5.5 0 0 0-.154-.154l-.338-.215 7.494-7.494 1.178-.471Z"/></svg>
        </button>
      </div>
    </div>
  </div>
</template>

<style scoped>
/* --- Root Wrapper --- */
.chat-interface-wrapper {
  position: relative;   /* CRUCIAL: Context for absolute children */
  height: 100%;
  width: 100%;
  overflow: hidden;     /* Prevent wrapper scroll */
  background-color: #181818;
  box-sizing: border-box;
  /* REMOVED display: flex; */
}

/* --- Chat History Area --- */
.chat-history {
  position: absolute;   /* CRUCIAL: Absolute positioning */
  top: 0;
  left: 0;
  right: 0;
  /* CRUCIAL: Calculate bottom based on FINAL input area height */
  /* Let's estimate input row (40px height + 10px padding * 2 = 60px) */
  /* If preview is shown (max ~90px + border/padding), bottom needs to be larger */
  /* We'll use a CSS variable updated by JS later, for now, use a fixed value */
  /* Adjust this value after checking rendered height of .chat-input-container */
  bottom: 65px; /* STARTING VALUE - MIGHT NEED ADJUSTMENT */

  overflow-y: auto;     /* Scroll only this area */
  padding: 20px;
  padding-bottom: 10px; /* Extra space at bottom */
  background-color: #1e1e1e;
  color: #ccc;
  display: flex;
  flex-direction: column;
  box-sizing: border-box;
}
/* Scrollbar */
.chat-history::-webkit-scrollbar { width: 6px; }
.chat-history::-webkit-scrollbar-track { background: transparent; }
.chat-history::-webkit-scrollbar-thumb { background: #444; border-radius: 3px; }
.chat-history::-webkit-scrollbar-thumb:hover { background: #555; }

/* --- Input Area Container --- */
.chat-input-container {
  position: absolute;   /* CRUCIAL: Fixed at the bottom */
  bottom: 0;
  left: 0;
  right: 0;
  z-index: 10;
  background: linear-gradient(to top, rgba(37, 37, 37, 1) 80%, rgba(37, 37, 37, 0.9));
  border-top: 1px solid #353535;
  display: flex;
  flex-direction: column; /* Stack preview and input row */
  box-sizing: border-box;
  padding: 0; /* Remove padding here, add to inner .chat-input */
}

/* --- Image Preview Area --- */
.image-preview-area { padding: 8px 15px 5px 15px; position: relative; background-color: rgba(50, 50, 50, 0.7); border-bottom: 1px dashed #555; max-height: 90px; overflow: hidden; display: flex; align-items: center; flex-shrink: 0; }
.preview-image { max-width: 80px; max-height: 70px; height: auto; width: auto; display: block; border-radius: 4px; border: 1px solid #666; margin-right: 10px; }
.remove-preview-button { background: rgba(40, 40, 40, 0.8); color: #bbb; border: 1px solid #666; border-radius: 50%; width: 22px; height: 22px; font-size: 14px; line-height: 20px; text-align: center; cursor: pointer; font-weight: bold; padding: 0; margin-left: auto; transition: background-color 0.2s, color 0.2s, border-color 0.2s; }
.remove-preview-button:hover { background: #e74c3c; color: #fff; border-color: #c0392b; }
.remove-preview-button svg { vertical-align: middle; margin-bottom: 1px;}

/* --- Input Row --- */
.chat-input {
  display: flex; align-items: center; gap: 8px;
  width: 100%; padding: 10px 15px; /* Padding for input row */
  box-sizing: border-box;
  flex-shrink: 0;
  min-height: 60px; /* Ensure minimum height for the row */
}

/* --- Message Bubble Base Style --- */
.message { display: flex; flex-direction: column; max-width: 85%; padding: 10px 15px; margin-bottom: 12px; border-radius: 18px; word-wrap: break-word; line-height: 1.5; background-color: #38383a; color: #ffffff; border: 1px solid #484848; box-shadow: 0 1px 2px rgba(0, 0, 0, 0.1); min-width: 0; }
/* User & AI Message Alignment */
.message.user { align-self: flex-end; margin-left: auto; } /* Now grey */
.message.ai { align-self: flex-start; margin-right: auto; }
/* Sender Text Style */
.message .sender { font-weight: bold; display: block; margin-bottom: 5px; font-size: 0.8em; color: rgba(255, 255, 255, 0.7); opacity: 0.8; }
.message.user .sender { text-align: right; }
/* Message Text / Markdown Container */
.message .text { color: #ffffff; font-size: 0.95em; min-width: 0; }
.message .text :deep(p), .message .text :deep(li), .message .text :deep(span), .message .text :deep(strong), .message .text :deep(em) { color: inherit !important; }
.message .text :deep(a) { color: #80bfff !important; text-decoration: underline; }
.message .text :deep(a:hover) { color: #a0cfff !important; }

/* --- Code Block Styling --- */
.markdown-body :deep(pre) { background-color: #1a1d21; border: 1px solid #101214; border-radius: 6px; padding: 12px 15px; overflow-x: auto; margin: 10px 0; }
.markdown-body :deep(code) { font-family: 'Consolas', 'Monaco', 'Courier New', monospace; font-size: 0.9em; background-color: #2e2e30; padding: 3px 6px; border-radius: 4px; color: #e6e6e6; }
.markdown-body :deep(pre > code) { background-color: transparent; padding: 0; color: #e6e6e6; white-space: pre-wrap; word-wrap: break-word; }

/* --- Image Attachment Styling --- */
.image-attachment { margin-top: 8px; max-width: 35%; /* <<< REDUCED IMAGE SIZE FURTHER */ align-self: var(--align-self, flex-start); overflow: hidden; border-radius: 10px; }
.message.user .image-attachment { --align-self: flex-end; }
.message.ai .image-attachment { --align-self: flex-start; }
.chat-image { display: block; width: 100%; height: auto; cursor: pointer; }
/* --- Other Styles --- */
.empty-chat { color: #777; text-align: center; margin: auto; padding: 20px; }
.message.system { color: #888; font-style: italic; text-align: center; margin-bottom: 10px; align-self: center; background: none; border: none; padding: 5px;}
.message.system.error { color: #ff6b6b; font-style: normal; font-weight: bold;}
.text-input { background-color: #3a3a3a; border: 1px solid #505050; color: #e0e0e0; border-radius: 18px; flex-grow: 1; padding: 10px 16px; font-size: 0.95em; min-height: 40px; box-sizing: border-box; }
.text-input::placeholder { color: #888; }
.text-input:focus { outline: none; border-color: #007bff; box-shadow: 0 0 0 2px rgba(0, 123, 255, 0.3);}
.text-input:disabled { background-color: #444; cursor: not-allowed; opacity: 0.6; }
.icon-button { background-color: #404040; border: 1px solid #555; color: #bbb; border-radius: 50%; width: 40px; height: 40px; display: inline-flex; align-items: center; justify-content: center; padding: 0; cursor: pointer; flex-shrink: 0; transition: background-color 0.2s ease-in-out, color 0.2s ease-in-out, border-color 0.2s ease-in-out; }
.icon-button svg { width: 18px; height: 18px; }
.icon-button:hover { background-color: #505050; color: #ddd; border-color: #666; }
.icon-button:disabled { background-color: #383838; color: #777; border-color: #484848; cursor: not-allowed; opacity: 0.7; }
.send-button { background-color: #007bff; color: white; border-color: #0056b3; }
.send-button:hover { background-color: #0056b3; border-color: #004494; color: white; }
.send-button:disabled { background-color: #555; color: #aaa; border-color: #666; }
</style>