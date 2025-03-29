<template>
  <div class="about-page-container">
    <h1>关于 AI Chat 本地网页程序</h1>

    <section>
      <h2>项目简介</h2>
      <p>
        这是一个本地运行的 AI 聊天网页程序示例。前端使用 Vue 3、Vite 构建，后端使用纯 Java 实现 HTTP 服务。
        当前版本集成了 Google Gemini API (通过配置)。
      </p>
      <p>
        主要目的是演示如何在本地环境中结合现代前端技术和纯 Java 后端来创建一个简单的 AI 应用。
      </p>
    </section>

    <section>
      <h2>技术栈</h2>
      <ul>
        <li>**前端:** HTML, CSS, JavaScript, Vue 3 (Composition API), Vite</li>
        <li>**后端:** Java (JDK 17), 内置 HttpServer, Gson (JSON处理), SnakeYAML (配置处理)</li>
        <li>**AI:** Google Gemini API (可替换)</li>
        <li>**通信:** HTTP (Fetch API), JSON</li>
      </ul>
    </section>

    <section>
      <h2>作者/联系方式</h2>
      <p>
        如果你有任何问题或建议，可以通过以下方式联系我：
      </p>
      <ul>
        <li>GitHub:  https://github.com/QcrTiMo</li>
        <li>QQ: 3192377376</li>
      </ul>
      <p>感谢使用！</p>
    </section>



  </div>
</template>

<script setup>
</script>

<style scoped>
.about-page-container {
  padding: 25px 40px;
  color: #ccc;
  line-height: 1.6;
  font-size: 1em;
  box-sizing: border-box;
  width: 100%;
}

h1 {
  color: #eee;
  margin-top: 0;
  margin-bottom: 30px;
  padding-bottom: 15px;
  border-bottom: 1px solid #444;
  font-size: 1.8em;
  font-weight: 600;
}

h2 {
  color: #ddd;
  margin-top: 35px;
  margin-bottom: 15px;
  font-size: 1.4em;
  border-bottom: 1px solid #3a3a3a;
  padding-bottom: 8px;
}

h3 {
  color: #bbb;
  margin-top: 25px;
  margin-bottom: 10px;
  font-size: 1.1em;
}


section {
  margin-bottom: 30px;
}

p {
  margin-bottom: 15px;
  color: #bbb;
}

ul {
  list-style: disc;
  margin-left: 25px;
  padding-left: 10px;
}

li {
  margin-bottom: 8px;
}

a {
  color: #4dabf7;
  text-decoration: none;
}

a:hover {
  text-decoration: underline;
}
</style>