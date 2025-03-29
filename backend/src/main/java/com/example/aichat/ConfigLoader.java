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

    private static final Yaml yaml;

    static {
        Representer representer = new Representer(new DumperOptions());
        representer.getPropertyUtils().setSkipMissingProperties(true);
        DumperOptions options = new DumperOptions();
        options.setDefaultFlowStyle(DumperOptions.FlowStyle.BLOCK);
        options.setPrettyFlow(true);
        yaml = new Yaml(new Constructor(WebConfig.class, new org.yaml.snakeyaml.LoaderOptions()), representer, options);
    }
    public static <T> T loadOrCreateConfig(String filename, Class<T> configClass, T defaultConfig) {
        Path configPath = Paths.get(filename);
        Yaml specificYaml = new Yaml(new Constructor(configClass, new org.yaml.snakeyaml.LoaderOptions()));


        if (Files.exists(configPath)) {
            System.out.println("从以下位置加载配置: " + configPath.toAbsolutePath());
            try (InputStream inputStream = Files.newInputStream(configPath)) {
                T loadedConfig = specificYaml.loadAs(inputStream, configClass);
                return loadedConfig != null ? loadedConfig : defaultConfig;
            } catch (IOException e) {
                System.err.println("读取配置文件 '" + filename + "' 时出错。将使用默认配置。错误: " + e.getMessage());
                return defaultConfig;
            } catch (Exception e) {
                System.err.println("解析配置文件 '" + filename + "' 时出错。请检查 YAML 语法。将使用默认配置。错误: " + e.getMessage());
                return defaultConfig;
            }
        } else {
            System.out.println("配置文件 '" + filename + "' 未找到。将在以下位置创建默认文件: " + configPath.toAbsolutePath());
            try (FileWriter writer = new FileWriter(configPath.toFile())) {
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
