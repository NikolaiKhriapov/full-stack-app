package my.project.fullstackapp.config;

import io.github.cdimascio.dotenv.Dotenv;
import io.github.cdimascio.dotenv.DotenvEntry;
import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;
import org.springframework.context.support.ReloadableResourceBundleMessageSource;

import java.util.Properties;
import java.util.Set;

@Configuration
public class MessageSourceConfig {

    @Bean
    public static PropertySourcesPlaceholderConfigurer propertySourcesPlaceholderConfigurer() {
        Dotenv dotenv;

        if (!isRunningTests()) {
            try {
                dotenv = Dotenv.configure().filename("backend/.env").load();
            } catch (Exception e) {
                dotenv = Dotenv.configure().filename(".env").load();
            }
        } else {
            dotenv = Dotenv.configure().filename(".env").load();
        }


        Properties properties = new Properties();
        Set<DotenvEntry> entries = dotenv.entries();
        for (DotenvEntry entry : entries) {
            properties.setProperty(entry.getKey(), entry.getValue());
        }

        PropertySourcesPlaceholderConfigurer configurer = new PropertySourcesPlaceholderConfigurer();
        configurer.setProperties(properties);
        return configurer;
    }

    private static boolean isRunningTests() {
        StackTraceElement[] stackTrace = Thread.currentThread().getStackTrace();
        for (StackTraceElement element : stackTrace) {
            if (element.getClassName().startsWith("org.junit.")) {
                return true;
            }
        }
        return false;
    }

    @Bean
    public MessageSource messageSource() {
        ReloadableResourceBundleMessageSource messageSource = new ReloadableResourceBundleMessageSource();
        messageSource.setBasenames(
                "classpath:exceptions"
        );
        messageSource.setDefaultEncoding("UTF-8");
        return messageSource;
    }
}
