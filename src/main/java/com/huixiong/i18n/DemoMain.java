package com.huixiong.i18n;

import java.text.MessageFormat;
import java.util.Locale;

/**
 * Java要实现国际化通常使用MessageFormat+Locale实现。
 */
public class DemoMain {
    public static void main(String[] args) {
        double price = 123.5;
        int num = 10;
        Object[] arguments = {price, num};
        MessageFormat mf_us = new MessageFormat("Pay {0,number,currency} for {1} books.", Locale.US);
        System.out.println(mf_us.format(arguments));

        MessageFormat mf_zh = new MessageFormat("{1}本书，一共{0,number,currency}。");
        System.out.println(mf_zh.format(arguments));
    }
}
