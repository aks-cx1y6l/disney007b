package com.mz.jarboot.service.impl;

import com.mz.jarboot.api.event.Subscriber;
import com.mz.jarboot.api.event.WorkspaceChangeEvent;
import com.mz.jarboot.api.exception.JarbootRunException;
import com.mz.jarboot.base.AgentManager;
import com.mz.jarboot.common.notify.NotifyReactor;
import com.mz.jarboot.common.utils.OSUtils;
import com.mz.jarboot.common.pojo.ResultCodeConst;
import com.mz.jarboot.api.constant.CommonConst;
import com.mz.jarboot.api.constant.SettingPropConst;
import com.mz.jarboot.api.pojo.GlobalSetting;
import com.mz.jarboot.api.pojo.ServiceSetting;
import com.mz.jarboot.common.JarbootException;
import com.mz.jarboot.api.service.SettingService;
import com.mz.jarboot.common.utils.StringUtils;
import com.mz.jarboot.common.notify.FrontEndNotifyEventType;
import com.mz.jarboot.utils.MessageUtils;
import com.mz.jarboot.utils.PropertyFileUtils;
import com.mz.jarboot.utils.SettingUtils;
import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.*;

/**
 * @author majianzheng
 */
@Service
public class SettingServiceImpl implements SettingService {
    private final Logger logger = LoggerFactory.getLogger(getClass());

    @Override
    public ServiceSetting getServiceSetting(String serviceName) {
        return PropertyFileUtils.getServiceSetting(serviceName);
    }

    @Override
    public void submitServiceSetting(ServiceSetting setting) {
        final String path = SettingUtils.getServicePath(setting.getName());
        File file = getConfAndCheck(path);
        Properties prop = PropertyFileUtils.getProperties(file);
        String command = setting.getCommand();
        if (null == command) {
            command = StringUtils.EMPTY;
        } else {
            command = command.replace('\n', ' ');
        }
        prop.setProperty(SettingPropConst.COMMAND, command);
        String vm = setting.getVm();
        if (null == vm) {
            vm = SettingPropConst.DEFAULT_VM_FILE;
        } else {
            if (!SettingPropConst.DEFAULT_VM_FILE.equals(vm)) {
                checkFileExist(vm, path);
            }
        }
        prop.setProperty(SettingPropConst.VM, vm);
        String args = setting.getArgs();
        if (null == args) {
            args = StringUtils.EMPTY;
        }
        prop.setProperty(SettingPropConst.ARGS, args);
        String group = setting.getGroup();
        if (null == group) {
            group = StringUtils.EMPTY;
        }
        prop.setProperty(SettingPropConst.GROUP, group);
        if (null == setting.getPriority()) {
            prop.setProperty(SettingPropConst.PRIORITY, StringUtils.EMPTY);
        } else {
            prop.setProperty(SettingPropConst.PRIORITY, setting.getPriority().toString());
        }
        checkAndSet(path, setting, prop);
        if (null == setting.getDaemon()) {
            prop.setProperty(SettingPropConst.DAEMON, SettingPropConst.VALUE_TRUE);
        } else {
            prop.setProperty(SettingPropConst.DAEMON, setting.getDaemon().toString());
        }
        if (null == setting.getJarUpdateWatch()) {
            prop.setProperty(SettingPropConst.JAR_UPDATE_WATCH, SettingPropConst.VALUE_TRUE);
        } else {
            prop.setProperty(SettingPropConst.JAR_UPDATE_WATCH, setting.getJarUpdateWatch().toString());
        }
        setting.setWorkspace(SettingUtils.getWorkspace());
        saveServerConfig(path, setting, file, prop);
    }

    private void saveServerConfig(String path, ServiceSetting setting, File file, Properties prop) {
        //???????????????????????????
        ServiceSetting preSetting = PropertyFileUtils.getServiceSetting(setting.getName());
        if (!Objects.equals(setting.getName(), preSetting.getName())) {
            String sid = SettingUtils.createSid(path);
            //????????????????????????????????????????????????????????????????????????????????????
            if (AgentManager.getInstance().isOnline(sid)) {
                throw new JarbootRunException("????????????????????????????????????????????????????????????");
            }
            PropertyFileUtils.storeProperties(file, prop);
            //???????????????
            File dir = FileUtils.getFile(path);
            File renamed = FileUtils.getFile(SettingUtils.getWorkspace(), setting.getName());
            if (renamed.exists()) {
                throw new JarbootRunException(setting.getName() + "???????????????");
            }
            if (!dir.renameTo(renamed)) {
                throw new JarbootRunException("????????????????????????");
            }
            //????????????????????????????????????????????????
            MessageUtils.globalEvent(FrontEndNotifyEventType.WORKSPACE_CHANGE);
        } else {
            PropertyFileUtils.storeProperties(file, prop);
        }
        //???????????????????????????????????????????????????????????????
        PropertyFileUtils.getServiceSetting(setting.getName());
    }

    private void checkAndSet(String path, ServiceSetting setting, Properties prop) {
        String workDirectory = setting.getWorkDirectory();
        if (StringUtils.isNotEmpty(workDirectory)) {
            checkDirExist(workDirectory);
        } else {
            workDirectory = StringUtils.EMPTY;
        }
        prop.setProperty(SettingPropConst.WORK_DIR, workDirectory);

        String jdkPath = setting.getJdkPath();
        if (StringUtils.isNotEmpty(jdkPath)) {
            String javaFile = jdkPath + File.separator + CommonConst.BIN_NAME +
                    File.separator + CommonConst.JAVA_CMD;
            if (OSUtils.isWindows()) {
                javaFile += CommonConst.EXE_EXT;
            }

            checkFileExist(javaFile, path);
        } else {
            jdkPath = StringUtils.EMPTY;
        }
        prop.setProperty(SettingPropConst.JDK_PATH, jdkPath);

        String env = setting.getEnv();
        if (PropertyFileUtils.checkEnvironmentVar(env)) {
            if (null == env) {
                env = StringUtils.EMPTY;
            }
            prop.setProperty(SettingPropConst.ENV, env);
        } else {
            throw new JarbootException(ResultCodeConst.VALIDATE_FAILED,
                    String.format("????????????????????????(%s)???", setting.getEnv()));
        }
    }

    @Override
    public GlobalSetting getGlobalSetting() {
        return SettingUtils.getGlobalSetting();
    }

    @Override
    public void submitGlobalSetting(GlobalSetting setting) {
        SettingUtils.updateGlobalSetting(setting);
    }

    @Override
    public String getVmOptions(String serviceName, String file) {
        Path path = SettingUtils.getPath(file);
        if (!path.isAbsolute()) {
            path = SettingUtils.getPath(SettingUtils.getServicePath(serviceName), file);
        }
        File f = path.toFile();
        String content = StringUtils.EMPTY;
        if (f.exists()) {
            try {
                content = FileUtils.readFileToString(f, StandardCharsets.UTF_8);
            } catch (IOException e) {
                throw new JarbootException("Read file error.", e);
            }
        }
        return content;
    }

    @Override
    public void saveVmOptions(String serviceName, String file, String content) {
        Path path = SettingUtils.getPath(file);
        if (!path.isAbsolute()) {
            path = SettingUtils.getPath(SettingUtils.getServicePath(serviceName), file);
        }
        File f = path.toFile();
        if (!f.exists()) {
            try {
                if (!f.createNewFile()) {
                    throw new JarbootException("Create file failed.");
                }
            } catch (IOException e) {
                throw new JarbootException("Create file error.", e);
            }
        }
        try {
            FileUtils.writeStringToFile(f, content, StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new JarbootException("Write file error.", e);
        }
    }

    @Override
    public void registerSubscriber(Subscriber<WorkspaceChangeEvent> subscriber) {
        NotifyReactor.getInstance().registerSubscriber(subscriber);
    }

    @Override
    public void deregisterSubscriber(Subscriber<WorkspaceChangeEvent> subscriber) {
        NotifyReactor.getInstance().deregisterSubscriber(subscriber);
    }

    private File getConfAndCheck(String p) {
        File file = SettingUtils.getServiceSettingFile(p);
        if (!file.exists()) {
            try {
                boolean rlt = file.createNewFile();
                if (!rlt) {
                    logger.debug("Config file({}) create failed.", file.getPath());
                }
            } catch (IOException e) {
                throw new JarbootException(ResultCodeConst.INTERNAL_ERROR, e);
            }
        }
        return file;
    }

    private void checkDirExist(String path) {
        File dir = FileUtils.getFile(path);
        if (dir.exists() && dir.isDirectory()) {
            return;
        }
        throw new JarbootException(ResultCodeConst.NOT_EXIST, path + "?????????");
    }

    private void checkFileExist(String file, String serverPath) {
        File dir = SettingUtils.isAbsolutePath(file) ?
                FileUtils.getFile(file)
                :
                FileUtils.getFile(serverPath, file);
        if (dir.exists() && dir.isFile()) {
            return;
        }
        throw new JarbootException(ResultCodeConst.NOT_EXIST, file + "?????????");
    }
}
