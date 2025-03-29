<template>
  <div class="config-editor-container">
    <h2>基础配置</h2>

    <div v-if="isLoading" class="loading-message">正在加载配置...</div>
    <div v-if="loadError" class="error-message">{{ loadError }}</div>

    <form v-if="!isLoading && !loadError && configData" @submit.prevent="saveConfig">


      <section class="config-section card">
        <h3>网络代理</h3>
        <div class="form-group">
          <label for="proxyUrl">代理 URL:</label>
          <input
              type="text"
              id="proxyUrl"
              v-model="configData.webConfig.proxyUrl"
              placeholder="例如: http://127.0.0.1:7890 或 socks://127.0.0.1:1080 或留空"
          />
          <small>在此处设置你的 HTTP 或 SOCKS5 代理服务器地址和端口。如果留空，则不使用代理。</small>
        </div>
      </section>


      <section class="config-section card">
        <h3>AI 模型</h3>
        <div class="form-group">
          <label for="modelName">模型名称:</label>
          <input
              type="text"
              id="modelName"
              v-model="configData.basicConfig.modelName"
              required
              placeholder="例如: gemini-2.5-pro-exp-03-25"
          />
          <small>指定要使用的 Gemini 模型名称。</small>
        </div>


        <div class="form-group api-keys-group">
          <label>API Keys:</label>
          <div v-if="!configData.basicConfig.apiKeys || configData.basicConfig.apiKeys.length === 0" class="no-keys-message">
            尚未添加 API Key。请点击下方按钮添加。
          </div>
          <div v-for="(key, index) in configData.basicConfig.apiKeys" :key="index" class="api-key-item">
            <input
                type="text"
                :id="'apiKey-' + index"
                v-model="configData.basicConfig.apiKeys[index]"
                placeholder="输入 API Key"
                class="api-key-input"
                autocomplete="off"
            />
            <button type="button" @click="removeApiKey(index)" class="delete-button" title="删除此 Key">
              ×
            </button>
          </div>

          <button type="button" @click="addApiKey" class="add-button">
            + 添加 Key
          </button>
          <small>程序将按列表顺序尝试使用 API Key。如果一个 Key 失败（例如，认证错误、达到速率限制或安全阻止），它会自动尝试列表中的下一个 Key。</small>
        </div>


        <div class="form-group">
          <label for="initialPersona">初始人设:</label>
          <textarea
              id="initialPersona"
              v-model="configData.basicConfig.initialPersona"
              rows="5"
              placeholder="设置 AI 的角色或行为指令 (例如: 你是一位知识渊博、乐于助人的 AI 助手，请用简洁明了的语言回答问题。)"
          ></textarea>
          <small>这段指令会在每次与 AI 开始新的对话逻辑时（或在需要重新建立上下文时）发送给模型，以引导其回应风格和内容。</small>
        </div>
      </section>



      <div class="form-actions">
        <button type="submit" :disabled="isSaving">
          {{ isSaving ? '正在保存...' : '保存配置' }}
        </button>
        <span v-if="saveStatus" :class="['save-status', saveStatus.success ? 'save-success' : 'save-error']">
            {{ saveStatus.message }}
         </span>
      </div>
    </form>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue';
const getConfigApiUrl = 'http://localhost:8080/api/config/get';
const saveConfigApiUrl = 'http://localhost:8080/api/config/save';
const configData = ref(null);
const isLoading = ref(true);
const loadError = ref('');
const isSaving = ref(false);
const saveStatus = ref(null);
let statusTimeoutId = null;
function addApiKey() { if (configData.value?.basicConfig) { if (!Array.isArray(configData.value.basicConfig.apiKeys)) { configData.value.basicConfig.apiKeys = []; } configData.value.basicConfig.apiKeys.push(''); } }
function removeApiKey(index) { if (configData.value?.basicConfig?.apiKeys) { configData.value.basicConfig.apiKeys.splice(index, 1); } }
onMounted(async () => { isLoading.value = true; loadError.value = ''; saveStatus.value = null; try { const response = await fetch(getConfigApiUrl); if (!response.ok) throw new Error(`获取配置失败: ${response.status} ${response.statusText}`); const loadedData = await response.json(); if (!loadedData.basicConfig) loadedData.basicConfig = { modelName: '', apiKeys: [], initialPersona: '' }; if (!Array.isArray(loadedData.basicConfig.apiKeys)) loadedData.basicConfig.apiKeys = []; if (!loadedData.webConfig) loadedData.webConfig = { proxyUrl: '' }; if (typeof loadedData.webConfig.proxyUrl === 'undefined') loadedData.webConfig.proxyUrl = ''; configData.value = loadedData; console.log('Configuration loaded:', JSON.parse(JSON.stringify(configData.value))); } catch (error) { console.error('Error loading configuration:', error); loadError.value = `无法加载配置: ${error.message}`; configData.value = { webConfig: { proxyUrl: ''}, basicConfig: { modelName: 'gemini-1.5-flash-latest', apiKeys: [''], initialPersona: '' } }; } finally { isLoading.value = false; } });
async function saveConfig() { if (!configData.value || isSaving.value) return; isSaving.value = true; saveStatus.value = null; if (statusTimeoutId) clearTimeout(statusTimeoutId); const configToSave = JSON.parse(JSON.stringify(configData.value)); if (configToSave.basicConfig && Array.isArray(configToSave.basicConfig.apiKeys)) { configToSave.basicConfig.apiKeys = configToSave.basicConfig.apiKeys.filter(key => key && key.trim().length > 0); if (configToSave.basicConfig.apiKeys.length === 0) console.warn("Saving empty API keys list."); } else if (configToSave.basicConfig) { configToSave.basicConfig.apiKeys = []; } if (!configToSave.webConfig) configToSave.webConfig = { proxyUrl: '' }; console.log('Saving configuration:', JSON.stringify(configToSave)); try { const response = await fetch(saveConfigApiUrl, { method: 'POST', headers: { 'Content-Type': 'application/json' }, body: JSON.stringify(configToSave) }); const result = await response.json(); if (!response.ok) throw new Error(result.error || `保存失败: ${response.status}`); saveStatus.value = { success: true, message: result.message || '配置保存成功！' }; } catch (error) { console.error('Error saving configuration:', error); saveStatus.value = { success: false, message: `保存失败: ${error.message}` }; } finally { isSaving.value = false; console.log('Save operation finished.'); statusTimeoutId = setTimeout(() => { saveStatus.value = null; console.log('Cleared save status message.'); }, 5000); } }
</script>

<style scoped>

.config-editor-container {
  padding: 25px 35px;
  color: #ccc;
  box-sizing: border-box;

  width: 100%;
}

h2 { color: #eee; margin-top: 0; margin-bottom: 30px; border-bottom: 1px solid #444; padding-bottom: 15px; font-size: 1.6em; }
h3 { color: #ddd; margin-top: 0; margin-bottom: 20px; font-size: 1.2em; border-bottom: 1px solid #3a3a3a; padding-bottom: 8px; }
.config-section.card { background-color: #252525; padding: 25px; border-radius: 8px; border: 1px solid #333; margin-bottom: 30px; box-shadow: 0 2px 5px rgba(0, 0, 0, 0.2); }
.form-group { margin-bottom: 25px; }
.form-group label { display: block; margin-bottom: 10px; font-weight: 500; color: #bbb; font-size: 0.9em; }
.form-group input[type="text"], .form-group input[type="password"], .form-group textarea { width: 100%; padding: 12px; background-color: #1e1e1e; border: 1px solid #444; border-radius: 5px; color: #eee; font-size: 0.95em; box-sizing: border-box; transition: border-color 0.2s, box-shadow 0.2s; }
.form-group textarea { min-height: 80px; resize: vertical; }
.form-group input[type="text"]:focus, .form-group input[type="password"]:focus, .form-group textarea:focus { outline: none; border-color: #007bff; box-shadow: 0 0 0 3px rgba(0, 123, 255, 0.2); }
.form-group small { display: block; margin-top: 8px; font-size: 0.8em; color: #888; line-height: 1.4; }
.api-keys-group label { margin-bottom: 15px; }
.api-key-item { display: flex; align-items: center; margin-bottom: 10px; gap: 10px; }
.api-key-input { flex-grow: 1; }
.delete-button { flex-shrink: 0; background-color: #552a2a; color: #ffaaaa; border: 1px solid #773a3a; padding: 0; width: 30px; height: 30px; font-size: 1.4em; line-height: 28px; text-align: center; border-radius: 4px; cursor: pointer; transition: background-color 0.2s, color 0.2s; }
.delete-button:hover { background-color: #773a3a; color: #ffcccc; }
.add-button { background-color: #2a4a2a; color: #aaffaa; border: 1px solid #3a6a3a; padding: 8px 15px; border-radius: 5px; cursor: pointer; font-size: 0.9em; margin-top: 5px; transition: background-color 0.2s, color 0.2s; }
.add-button:hover { background-color: #3a6a3a; color: #ccffcc; }
.no-keys-message { color: #aaa; font-style: italic; margin-bottom: 10px; }
.form-actions { margin-top: 30px; display: flex; align-items: center; gap: 15px; padding-bottom: 20px; /* Add padding at the bottom */ }
.form-actions button { padding: 10px 20px; background-color: #007bff; color: white; border: none; border-radius: 5px; cursor: pointer; font-size: 1em; transition: background-color 0.2s; }
.form-actions button:hover { background-color: #0056b3; }
.form-actions button:disabled { background-color: #555; cursor: not-allowed; }
.loading-message, .error-message { text-align: center; padding: 20px; font-size: 1.1em; }
.error-message { color: #ff6b6b; }
.save-status { font-weight: bold; font-size: 0.9em; }
.save-success { color: #78ffd6; }
.save-error { color: #ff8b8b; }
</style>
