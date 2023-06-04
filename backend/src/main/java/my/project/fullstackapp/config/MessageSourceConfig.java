package my.project.fullstackapp.config;

import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.support.ReloadableResourceBundleMessageSource;

@Configuration
public class MessageSourceConfig {

//    @Bean
//    public static PropertySourcesPlaceholderConfigurer propertySourcesPlaceholderConfigurer() {
//        Dotenv dotenv = Dotenv.load();
//
//        Properties properties = new Properties();
//        Set<DotenvEntry> entries = dotenv.entries();
//        for (DotenvEntry entry : entries) {
//            properties.setProperty(entry.getKey(), entry.getValue());
//        }
//
//        PropertySourcesPlaceholderConfigurer configurer = new PropertySourcesPlaceholderConfigurer();
//        configurer.setProperties(properties);
//        return configurer;
//    }

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
