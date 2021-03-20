package com.huixiong.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;


public class User {
    private String email;
    private String name;
    private  String password;

    public User(){}
    public User(String email, String name,String password) {
        this.email = email;
        this.name = name;
        this.password = password;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    /**
     * 注解JsonIgnore：完全忽略改参数
     * 注解JsonProperty(access=Access.WRITE_ONLY)： 只写
     * 注解JsonProperty(access=Access.READ_ONLY)： 只读
     * @return
     */
    @JsonProperty(access= JsonProperty.Access.WRITE_ONLY)
    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}
