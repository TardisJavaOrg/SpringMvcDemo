package com.huixiong;

import com.huixiong.service.UserService;
import com.mitchellbosecke.pebble.PebbleEngine;
import com.mitchellbosecke.pebble.extension.AbstractExtension;
import com.mitchellbosecke.pebble.extension.Extension;
import com.mitchellbosecke.pebble.extension.Function;
import com.mitchellbosecke.pebble.loader.ServletLoader;
import com.mitchellbosecke.pebble.spring.extension.SpringExtension;
import com.mitchellbosecke.pebble.spring.servlet.PebbleViewResolver;
import com.mitchellbosecke.pebble.template.EvaluationContext;
import com.mitchellbosecke.pebble.template.PebbleTemplate;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.apache.catalina.Context;
import org.apache.catalina.LifecycleException;
import org.apache.catalina.WebResourceRoot;
import org.apache.catalina.startup.Tomcat;
import org.apache.catalina.webresources.DirResourceSet;
import org.apache.catalina.webresources.StandardRoot;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.context.MessageSource;
import org.springframework.context.annotation.*;
import org.springframework.context.support.ResourceBundleMessageSource;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.LocaleResolver;
import org.springframework.web.servlet.ViewResolver;
import org.springframework.web.servlet.config.annotation.*;
import org.springframework.web.servlet.i18n.CookieLocaleResolver;

import javax.servlet.ServletContext;
import javax.sql.DataSource;
import java.io.File;

import java.util.*;

/**
 * web应用中启动spring有很多中方式，Listener、Servlet、xml配置、注解
 * 这里选择xml
 */
@Configuration
@ComponentScan
@EnableWebMvc
@EnableTransactionManagement
@PropertySource("classpath:jdbc.properties")
public class AppConfig {
    @Value("${jdbc.url}")
    String jdbcUrl;
    @Value("${jdbc.username}")
    String jdbcUsername;
    @Value("${jdbc.password}")
    String jdbcPassword;

    @Bean
    DataSource createDataSource() {
        HikariConfig config = new HikariConfig();
        config.setJdbcUrl(jdbcUrl);
        config.setUsername(jdbcUsername);
        config.setPassword(jdbcPassword);
        config.addDataSourceProperty("autocommit", "true");
        config.addDataSourceProperty("connectionTimeout", "5");
        config.addDataSourceProperty("idleTime", "60");
        return new HikariDataSource(config);
    }

    @Bean
    JdbcTemplate createJdbcTemplate(@Autowired DataSource dataSource) {
        return new JdbcTemplate(dataSource);
    }

    @Bean
    PlatformTransactionManager createTxManager(@Autowired DataSource dataSource) {
        return new DataSourceTransactionManager(dataSource);
    }

    /**
     * 该bean并不是必须的，这里创建默认的WebMvcConfigurer，覆写addResourceHandlers
     * 为了让Spring Mvc 自动处理静态文件，并映射/static/**
     *
     * @return
     */
    @Bean
    WebMvcConfigurer createWebMvcConfigurer(@Autowired HandlerInterceptor[] interceptors) {
        return new WebMvcConfigurer() {
            @Override
            public void addResourceHandlers(ResourceHandlerRegistry registry) {
                registry.addResourceHandler("/static/**").addResourceLocations("/static/");
            }

            @Override
            public void addInterceptors(InterceptorRegistry registry) {
                for (HandlerInterceptor i :
                        interceptors) {
                    registry.addInterceptor(i);
                }
            }

            @Override
            public void addCorsMappings(CorsRegistry registry) {
                registry.addMapping("/api/**")
                        .allowedOrigins("http://local.huixiong.com:8080")
                        .allowedOrigins("http://localhost:8080")
                        .allowedMethods("GET", "POST")
                        .maxAge(3600);
            }
        };
    }

    /**
     * viewResolver 通过prefix和suffix来查找view
     * 该方法使用了pebble引擎来渲染模板，指定模板文件存放在/WEB-INF/templates/路径下
     *
     * @param servletContext
     * @return
     */
    @Bean
    ViewResolver createViewResolver(@Autowired ServletContext servletContext, @Autowired @Qualifier("i18n") MessageSource msgSource) {
        PebbleEngine engine = new PebbleEngine.Builder()
                .autoEscaping(true)
                .cacheActive(false)
                .loader(new ServletLoader(servletContext))
                .extension(createExtension(msgSource)) // 这个是国际化函数
                .build();
        PebbleViewResolver viewResolver = new PebbleViewResolver();
        viewResolver.setPrefix("/WEB-INF/templates/");
        viewResolver.setSuffix("");
        viewResolver.setPebbleEngine(engine);
        return viewResolver;
    }

    /**
     * 封装一个国际化函数，名称就是 _
     * @param msgSource
     * @return
     */
    private Extension createExtension(MessageSource msgSource) {
        return new AbstractExtension() {
            @Override
            public Map<String, Function> getFunctions() {
                return new HashMap<String, Function>() {{
                    put("_", new Function() {

                        @Override
                        public List<String> getArgumentNames() {
                            return null;
                        }

                        @Override
                        public Object execute(Map<String, Object> args, PebbleTemplate self, EvaluationContext context, int lineNumber) {
                            String key = (String) args.get("0");
                            List<Object> args_tmp = this.extractArgs(args);
                            Locale locale = (Locale) context.getVariable("__locale__");
                            return msgSource.getMessage(key, args_tmp.toArray(), "???" + key + "???", locale);
                        }

                        private List<Object> extractArgs(Map<String, Object> args) {
                            int i = 1;
                            List<Object> args_tmp = new ArrayList<>();
                            while (args.containsKey(String.valueOf(i))) {
                                Object param = args.get(String.valueOf(i));
                                args_tmp.add(param);
                                i++;
                            }
                            return args_tmp;
                        }
                    });
                }};
            }
        };
    }

    /**
     * 该Bean 将用来处理国际化显示问题
     * LocaleResolver能自动从httpServletRequest中获取Locale
     * CookieLocaleResolver是对LocaleResolver的最常用的一个实现类
     * <p>
     * 首先根据一个特定Cookie判断是否指定了locale ,若没有，就从HTTP头中获取；
     * 还没有，就返回默认的Locale
     *
     * @return
     */
    @Bean
    LocaleResolver createLocaleResolver() {
        CookieLocaleResolver resolver = new CookieLocaleResolver();
        resolver.setDefaultLocale(Locale.ENGLISH);
        resolver.setDefaultTimeZone(TimeZone.getDefault());
        return resolver;
    }

    /**
     * 创建一个实例，使用Spring 提供的MessageSource
     * 会自动读取。properties文件，提供一个同意接口实现翻译
     * ResouceBundleMsgSource 会自动根据著文件名自动把相关语言的资源文件读取进来
     * Spring不知创建了一个msgSource，我们自己创建的这个msgSource是专门给国际化使用的，命名为 i18n；
     * 不与其他msgSource实例冲突
     *
     * @return
     */
    @Bean("i18n")
    MessageSource createMessageSource() {
        ResourceBundleMessageSource msgSource = new ResourceBundleMessageSource();
        msgSource.setDefaultEncoding("UTF-8");
        msgSource.setBasename("messages");
        return msgSource;
    }

    public static void main(String[] args) throws LifecycleException {
        Tomcat tomcat = new Tomcat();
        tomcat.setPort(Integer.getInteger("port", 8080));
        tomcat.getConnector();
        Context context = tomcat.addWebapp("", new File("src/main/webapp").getAbsolutePath());
        WebResourceRoot resources = new StandardRoot(context);
        resources.addPreResources(new DirResourceSet(resources, "/WEB-INF/classes", new File("target/classes").getAbsolutePath(), "/"));
        context.setResources(resources);
        tomcat.start();
        tomcat.getServer().await();
    }
}
