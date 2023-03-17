package com.mz.jarboot;

import com.mz.jarboot.api.AgentService;
import com.mz.jarboot.api.JarbootFactory;
import com.mz.jarboot.api.cmd.spi.CommandProcessor;
import com.mz.jarboot.api.service.ServiceManager;
import com.mz.jarboot.api.service.SettingService;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.core.env.Environment;

/**
 * @author majianzheng
 */
@ConditionalOnProperty(name = "spring.jarboot.enabled", matchIfMissing = true)
@EnableConfigurationProperties({ JarbootConfigProperties.class })
public class JarbootAutoConfiguration {
    @Bean
    @ConditionalOnClass(name = Constants.AGENT_CLASS)
    public AgentService agentService() {
        return JarbootFactory.createAgentService();
    }

    @Bean
    @ConditionalOnProperty(havingValue = "serverAddr")
    public ServiceManager serviceManager(JarbootConfigProperties properties) {
        return JarbootFactory
                .createServerManager(properties.getServerAddr(), properties.getUsername(), properties.getPassword());
    }

    @Bean
    @ConditionalOnProperty(havingValue = "serverAddr")
    public SettingService setting(JarbootConfigProperties properties) {
        return JarbootFactory
                .createSettingService(properties.getServerAddr(), properties.getUsername(), properties.getPassword());
    }

    @Bean("spring.env")
    public CommandProcessor springEnv(Environment environment) {
        return new com.mz.jarboot.command.SpringEnvCommandProcessor(environment);
    }

    @Bean("spring.bean")
    public CommandProcessor springBean(ApplicationContext context) {
        return new com.mz.jarboot.command.SpringBeanCommandProcessor(context);
    }
}
