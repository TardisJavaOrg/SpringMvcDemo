package com.huixiong.interceptor;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.MessageSource;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.LocaleResolver;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Locale;

@Order(3)
@Component
public class MvcInterceptor implements HandlerInterceptor {
    @Autowired
    LocaleResolver localResolver;

    @Autowired
    @Qualifier("i18n")
    MessageSource msgSource;

    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, ModelAndView modelAndView) throws Exception {
        if (modelAndView != null) {
            Locale locale = localResolver.resolveLocale(request);
            if(msgSource != null){
                System.out.println(msgSource.toString());
            }
            modelAndView.addObject("__messageSource__",msgSource);
            modelAndView.addObject("__locale__",locale);
        }
    }
}
