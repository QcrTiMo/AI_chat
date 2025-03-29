# AI_chat 本地聊天 Web 应用

AI_chat 是一个本地运行的 AI 聊天 Web 应用程序。它提供了一个现代化的 Web 界面，允许用户与配置好的 AI 模型（当前主要对接 Google Gemini）进行交互，并将对话历史记录保存在本地。

该项目是为了不想到期末才写java作业,于是提前干完了。


## ✨ 功能特性

*   **本地部署:** 前后端均可在本地轻松运行。
*   **现代化 Web 界面:** 使用 Vue 3 (Composition API) 和 Vite 构建，界面简洁、响应快速。
*   **纯 Java 后端:** 后端基于 Java 内置 HttpServer 实现，无 Spring Boot 等重型框架依赖。
*   **Google Gemini API 对接:** 通过配置好的 API Key 与 Gemini 模型进行通信。
*   **对话历史记录:** 聊天记录（用户与 AI 的消息）自动保存在本地 `conversation_history.json` 文件中，实现多轮对话记忆。
*   **可配置性 (YAML):**
    *   通过 `basic_config.yaml` 配置 AI 模型名称、API Keys（支持多个轮换）和初始人设 (System Prompt)。
    *   通过 `web_config.yaml` 配置网络代理 (HTTP/SOCKS - 后端需实现 SOCKS 支持)。
    *   首次运行后端时自动生成配置文件模板。
*   **代理支持:** 支持通过配置的 HTTP 代理访问 Gemini API（方便本地测试）。
*   **API Key 轮换:** 支持配置多个 API Key，在一个 Key 失效（如认证、限流）时自动尝试下一个。
*   **系统提示/人设:** 支持在 `basic_config.yaml` 中设置初始人设，引导 AI 的行为。
*   **动态配置编辑:** 提供 Web 界面（基础配置页面）用于查看和修改 `web_config.yaml` 和 `basic_config.yaml` 的内容。

## 🛠️ 技术栈

*   **前端:**
    *   Vue 3 (Composition API)
    *   Vite
    *   HTML5
    *   CSS3
    *   Fetch API (用于与后端通信)
*   **后端:**
    *   Java (JDK 17)
    *   内置 `com.sun.net.httpserver` (用于 HTTP 服务)
    *   `java.net.http.HttpClient` (用于调用 Gemini API)
    *   Gson (用于 JSON 解析和序列化)
    *   SnakeYAML (用于 YAML 配置文件处理)
*   **构建与依赖管理:**
    *   Maven (后端)
    *   npm (前端)

## 🚀 快速开始

### 先决条件

*   **Java Development Kit (JDK):** 版本 17或更高。
*   **Maven:** 用于构建后端项目。
*   **Node.js 和 npm:** 用于构建和运行前端项目。
*   **Git:** 用于克隆代码库。
*   **Google Gemini API Key:** 前往 [Google AI Studio](https://aistudio.google.com/) 或 Google Cloud Console 获取。

### 安装与运行

1.  **克隆代码库:**
    ```bash
    git clone https://github.com/QcrTiMo/AI_chat 
    cd AI_chat
    ```

2.  **后端设置与运行:**
    *   **进入后端目录:**
        ```bash
        cd backend
        ```
    *   **首次编译打包:**
        ```bash
        mvn clean package
        ```
    *   **首次运行:**
        ```bash
        java -jar target/aichat-backend-1.0-SNAPSHOT-jar-with-dependencies.jar
        ```
        此时后端会启动，并提示找不到配置文件，然后自动在 `backend` 目录下创建 `web_config.yaml` 和 `basic_config.yaml` 的模板文件。同时会提示没有找到有效的 API Key。
    *   **编辑配置文件:**
        *   打开 `basic_config.yaml`，将 `YOUR_DEFAULT_API_KEY_PLACEHOLDER` 替换为你的真实 Google Gemini API Key。你可以添加多个 Key，每个 Key 占一行并以 `- ` 开头。根据需要修改 `modelName` 和 `initialPersona`。
        *   打开 `web_config.yaml`，如果需要通过代理访问 Google API，请设置 `proxyUrl` (例如 `http://127.0.0.1:7890`)；如果不需要代理，请将 `proxyUrl` 的值设置为空字符串 `""` 或 `null`。
    *   **再次运行后端:** 按 `Ctrl+C` 停止之前的运行，然后再次执行：
        ```bash
        java -jar target/aichat-backend-1.0-SNAPSHOT-jar-with-dependencies.jar
        ```
        这次后端应该会加载你的配置并正常启动，监听在 8080 端口。**保持此终端窗口运行。**

3.  **前端设置与运行:**
    *   **打开新的终端窗口。**
    *   **进入前端目录:**
        ```bash
        cd ../frontend
        # 或者从 AI_chat 根目录执行: cd frontend
        ```
    *   **安装依赖:**
        ```bash
        npm install
        ```
    *   **启动开发服务器:**
        ```bash
        npm run dev
        ```
    *   前端开发服务器通常会运行在 `http://localhost:5173` (具体地址看终端输出)。

4.  **访问应用:** 在浏览器中打开 Vite 提供的本地地址 (例如 `http://localhost:5173`)。

## ⚙️ 配置说明

项目启动时会在后端目录 (`backend/`) 下查找或创建以下 YAML 配置文件：

*   **`basic_config.yaml`:**
    *   `modelName`: (字符串) 要使用的 Gemini 模型名称，例如 `gemini-1.5-flash-latest`。
    *   `apiKeys`: (列表) 一个或多个 Google Gemini API Key 字符串。程序会按列表顺序尝试使用，遇到认证/限流等问题时自动切换到下一个。**请务必将默认占位符替换为你的真实 Key！**
    *   `initialPersona`: (字符串) AI 的初始人设或系统提示，会在对话开始时发送给模型。

*   **`web_config.yaml`:**
    *   `proxyUrl`: (字符串) 网络代理的 URL。支持 HTTP (如 `http://127.0.0.1:7890`) 或 SOCKS (如 `socks://127.0.0.1:1080`，但请注意后端代码目前默认按 HTTP 处理，需要修改才能完美支持 SOCKS)。如果不需要代理，请设置为空字符串 `""` 或 `null`。

你可以直接编辑这些 YAML 文件来修改配置，修改后需要**重启后端服务**才能生效。你也可以通过访问应用的“基础配置”页面来动态修改和保存这些配置。

**⚠️ 安全警告:** 切勿将包含真实 API Key 的 `basic_config.yaml` 文件提交到公共代码库！建议将此文件添加到你的 `.gitignore` 文件中。

## 📖 使用

1.  确保后端和前端服务都已成功启动。
2.  在浏览器中访问前端应用的 URL。
3.  你会首先看到欢迎页面，点击“开始体验”进入主界面。
4.  主界面左侧是导航栏，右侧是内容区域。
    *   **快速开始:** 显示聊天界面，你可以在底部的输入框输入消息，按 Enter 或点击“发送”按钮与 AI 对话。聊天记录会自动保存。
    *   **基础配置:** 显示配置编辑页面，你可以在此修改网络代理、模型名称、API Keys 和初始人设，修改后点击“保存配置”会更新对应的 YAML 文件（需要重启后端才能完全生效）。
    *   **关于:** 显示项目的相关信息。
5.  顶部导航栏也提供了“快速开始”和“关于”的快捷入口。

## 💡 未来可能的改进

*   支持更多 AI 模型 (如 OpenAI GPT 系列、本地模型 Ollama 等)。
*   更完善的错误处理和用户提示。
*   UI/UX 优化（例如流式输出、Markdown 渲染、代码高亮）。
*   使用数据库替代本地文件存储聊天记录和配置。
*   完善 SOCKS 代理支持。
*   添加单元测试和集成测试。
*   实现真正的主题切换（浅色/深色模式）。
*   打包成独立可运行文件。
*   对话多模态,将来可能会支持图片等。

## 🤝 贡献

欢迎各种形式的贡献！如果你发现 Bug 或有改进建议，请随时创建 Issue。如果你想贡献代码，请 Fork 本仓库，在你的分支上进行修改，然后提交 Pull Request。
