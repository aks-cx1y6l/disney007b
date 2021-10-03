package com.mz.jarboot.utils;

import com.mz.jarboot.common.ResultCodeConst;
import com.mz.jarboot.constant.CommonConst;
import com.mz.jarboot.dto.ServerSettingDTO;
import com.mz.jarboot.common.MzException;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;

public class PropertyFileUtils {
    private static final Logger logger = LoggerFactory.getLogger(PropertyFileUtils.class);

    public static Properties getProperties(File file) {
        Properties properties = new Properties();
        if (null == file || !file.isFile() || !file.exists()) {
            return properties;
        }
        try (FileInputStream fis = FileUtils.openInputStream(file)) {
            properties.load(fis);
        } catch (IOException e) {
            logger.error(e.getMessage(), e);
        }
        return properties;
    }

    public static void storeProperties(File file, Properties properties) {
        try (FileOutputStream fos = new FileOutputStream(file, false)) {
            properties.store(fos, "Properties file Jarboot created.");
        } catch (IOException e) {
            logger.error(e.getMessage(), e);
        }
    }

    private static boolean checkFileExist(String file) {
        return Files.exists(Paths.get(file));
    }

    public static boolean checkEnvironmentVar(String env) {
        if (StringUtils.isEmpty(env)) {
            return true;
        }
        String[] envs = env.split(",");
        for (String en : envs) {
            //只能包含一个等号，且等号不能在边界
            if (en.length() < 3 && 1 != StringUtils.countMatches(en, '=') &&
                    '=' != en.charAt(0) && '=' != en.charAt(en.length() - 1)) {
                return false;
            }
        }
        return true;
    }

    public static ServerSettingDTO getServerSetting(String server) {
        ServerSettingDTO setting = new ServerSettingDTO(server);
        File file = SettingUtils.getServerSettingFile(server);
        Properties properties = getProperties(file);
        String serverPath = SettingUtils.getServerPath(server);
        if (properties.isEmpty()) {
            return setting;
        }
        String jar = properties.getProperty("jar", "");
        if (StringUtils.isNotEmpty(jar)) {
            if (checkFileExist(serverPath + File.separator + jar)) {
                setting.setJar(jar);
            } else {
                logger.warn("配置的启动jar文件({})不存在", jar);
            }
        }

        String jvm = properties.getProperty("jvm", CommonConst.DEFAULT_JVM_FILE);
        setting.setJvm(jvm);
        String args = properties.getProperty("args", "");
        setting.setArgs(args);
        checkAndGetHome(setting, properties);

        //环境变量
        String env = properties.getProperty("envp", "");
        if (checkEnvironmentVar(env) && StringUtils.isNotEmpty(env)) {
            setting.setEnvp(env);
        }

        int priority = NumberUtils.toInt(properties.getProperty("priority", "1"), 1);
        setting.setPriority(priority);

        String s = properties.getProperty("daemon", "true");
        if (StringUtils.equalsIgnoreCase("false", s)) {
            //初始默认true
            setting.setDaemon(false);
        }

        s = properties.getProperty("jarUpdateWatch", "true");
        if (StringUtils.equalsIgnoreCase("false", s)) {
            //初始默认true
            setting.setJarUpdateWatch(false);
        }
        return setting;
    }

    private static void checkAndGetHome(ServerSettingDTO setting, Properties properties) {
        //工作目录
        String workHome = properties.getProperty("workHome", "");
        if (StringUtils.isNotEmpty(workHome)) {
            File dir = new File(workHome);
            if (dir.isDirectory() && dir.exists()) {
                setting.setWorkHome(workHome);
            } else {
                //默认启动目录在服务目录，不继承父进程的工作目录
                setting.setWorkHome(StringUtils.EMPTY);
            }
        }

        //Java home路径
        String javaHome = properties.getProperty("javaHome", "");
        if (StringUtils.isEmpty(javaHome)) {
            //默认启动目录在服务目录，不继承父进程的工作目录
            setting.setJavaHome(CommonConst.EMPTY_STRING);
        } else {
            File dir = new File(javaHome);
            if (dir.isDirectory() && dir.exists()) {
                setting.setJavaHome(javaHome);
            } else {
                //默认启动目录在服务目录，不继承父进程的工作目录
                setting.setJavaHome(CommonConst.EMPTY_STRING);
            }
        }
    }


    /**
     * 写入properties或INI文件属性
     * @param file 属性文件
     * @param props 属性
     */
    public static void writeProperty(File file, Map<String, String> props) {
        if (null == file || MapUtils.isEmpty(props)) {
            return;
        }
        if (!file.isFile() || !file.exists()) {
            throw new MzException(ResultCodeConst.INTERNAL_ERROR, "配置文件不存在：" + file.getPath());
        }
        List<String> lines = new ArrayList<>();
        try {
            lines = FileUtils.readLines(file, StandardCharsets.UTF_8.name());
        } catch (IOException e) {
            logger.info(e.getMessage(), e);
        }
        HashMap<String, String> copy = new HashMap<>(props);
        if (CollectionUtils.isNotEmpty(lines)) {
            for (int i = 0; i < lines.size(); ++i) {
                String line = lines.get(i);
                try {
                    line = parsePropLine(line, copy);
                    lines.set(i, line);
                } catch (MzException e) {
                    //do nothing
                }
            }
        }
        if (!copy.isEmpty()) {
            for (Map.Entry<String, String> entry : copy.entrySet()) {
                lines.add(String.format("%s=%s", entry.getKey(), entry.getValue()));
            }
        }
        try {
            FileUtils.writeLines(file, StandardCharsets.UTF_8.name(), lines);
        } catch (IOException e) {
            logger.info(e.getMessage(), e);
        }
    }

    //解析启动优先级配置
    public static Queue<ServerSettingDTO> parseStartPriority(List<String> servers) {
        //优先级最大的排在最前面
        PriorityQueue<ServerSettingDTO> queue = new PriorityQueue<>((o1, o2) -> o2.getPriority() - o1.getPriority());
        if (CollectionUtils.isEmpty(servers)) {
            return queue;
        }
        servers.forEach(server -> {
            ServerSettingDTO setting = getServerSetting(server);
            queue.offer(setting);
        });
        return queue;
    }

    //解析终止优先级配置，与启动优先级相反
    public static Queue<ServerSettingDTO> parseStopPriority(List<String> servers) {
        //优先级小的排在最前面
        PriorityQueue<ServerSettingDTO> queue = new PriorityQueue<>(Comparator.comparingInt(ServerSettingDTO::getPriority));
        if (CollectionUtils.isEmpty(servers)) {
            return queue;
        }
        servers.forEach(server -> {
            ServerSettingDTO setting = getServerSetting(server);
            queue.offer(setting);
        });
        return queue;
    }

    private static String parsePropLine(String line, Map<String, String> props) {
        line = StringUtils.trim(line);
        if (StringUtils.indexOf(line, '=') <= 0 || 0 == StringUtils.indexOf(line, '#')) {
            throw new MzException();
        }
        String[] spliced = StringUtils.split(line, "=", 2);
        if (spliced.length <= 0) {
            throw new MzException();
        }
        String key = StringUtils.trim(spliced[0]);
        String value = props.getOrDefault(key, null);
        if (null == value) {
            throw new MzException();
        }
        line = key + '=' + value;
        props.remove(key);
        return line;
    }

    private PropertyFileUtils(){}
}