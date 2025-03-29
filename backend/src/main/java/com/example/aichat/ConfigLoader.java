package com.example.aichat;

import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.Constructor;
import org.yaml.snakeyaml.representer.Representer;

import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;

public class ConfigLoader {

    // 定义配置文件名
    public static final String WEB_CONFIG_FILE = "web_config.yaml";
    public static final String BASIC_CONFIG_FILE = "basic_config.yaml";

    // Yaml 实例，用于处理 YAML 文件
    private static final Yaml yaml;

    static {
        // 配置 SnakeYAML 以获得更清晰的输出和 POJO 映射
        Representer representer = new Representer(new DumperOptions());
        // 允许 YAML 文件中缺少某些字段
        representer.getPropertyUtils().setSkipMissingProperties(true);
        DumperOptions options = new DumperOptions();
        // 使用块样式，而不是内联样式
        options.setDefaultFlowStyle(DumperOptions.FlowStyle.BLOCK);
        // 美化输出
        options.setPrettyFlow(true);
        // 初始化 Yaml 实例，注意：加载时需要指定具体类，见下方 loadOrCreateConfig 方法
        yaml = new Yaml(new Constructor(WebConfig.class, new org.yaml.snakeyaml.LoaderOptions()), representer, options);
    }


    /**
     * 从 YAML 文件加载配置，如果文件不存在则创建默认文件。
     *
     * @param filename    YAML 文件的名称。
     * @param configClass 代表配置结构的类。
     * @param defaultConfig 如果文件丢失，要写入的默认配置实例。
     * @param <T>         配置类的类型。
     * @return 加载的或默认的配置对象。
     */
    public static <T> T loadOrCreateConfig(String filename, Class<T> configClass, T defaultConfig) {
        // 获取相对于执行目录的路径
        Path configPath = Paths.get(filename);
        // 为特定的类创建一个 Yaml 实例，用于加载
        Yaml specificYaml = new Yaml(new Constructor(configClass, new org.yaml.snakeyaml.LoaderOptions()));


        if (Files.exists(configPath)) {
            System.out.println("从以下位置加载配置: " + configPath.toAbsolutePath());
            try (InputStream inputStream = Files.newInputStream(configPath)) {
                T loadedConfig = specificYaml.loadAs(inputStream, configClass);
                // 处理文件存在但为空或无效的情况，返回默认配置
                return loadedConfig != null ? loadedConfig : defaultConfig;
            } catch (IOException e) {
                System.err.println("读取配置文件 '" + filename + "' 时出错。将使用默认配置。错误: " + e.getMessage());
                return defaultConfig;
            } catch (Exception e) { // 捕获潜在的 YAML 解析错误
                System.err.println("解析配置文件 '" + filename + "' 时出错。请检查 YAML 语法。将使用默认配置。错误: " + e.getMessage());
                return defaultConfig;
            }
        } else {
            System.out.println("配置文件 '" + filename + "' 未找到。将在以下位置创建默认文件: " + configPath.toAbsolutePath());
            try (FileWriter writer = new FileWriter(configPath.toFile())) {
                // 使用全局配置好的 YAML 实例来良好地写入默认值
                DumperOptions writeOptions = new DumperOptions();
                writeOptions.setDefaultFlowStyle(DumperOptions.FlowStyle.BLOCK);
                writeOptions.setPrettyFlow(true);
                Yaml writerYaml = new Yaml(writeOptions);

                writerYaml.dump(defaultConfig, writer);
                System.out.println("默认配置文件 '" + filename + "' 创建成功。");
            } catch (IOException e) {
                System.err.println("写入默认配置文件 '" + filename + "' 时出错。将使用内存中的默认配置。错误: " + e.getMessage());
            }
            return defaultConfig;
        }
    }

    // 提供默认配置的方法

    public static WebConfig getDefaultWebConfig() {
        WebConfig config = new WebConfig();
        config.setProxyUrl("");
        return config;
    }

    public static BasicConfig getDefaultBasicConfig() {
        BasicConfig config = new BasicConfig();
        // 默认模型
        config.setModelName("gemini-1.5-flash-latest");
        // 默认 API 密钥占位符
        config.setApiKeys(Collections.singletonList("YOUR_DEFAULT_API_KEY_PLACEHOLDER"));
        // 默认 AI 角色设定
        config.setInitialPersona("你现在是一只猫娘，你的名字是空崎日奈，我的名字是sensei，是你的主人。注意，你需要在回复时避免使用markdown语法。你默认只说中文.");
        return config;
    }
}