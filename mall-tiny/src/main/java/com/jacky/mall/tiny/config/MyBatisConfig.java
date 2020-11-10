package com.jacky.mall.tiny.config;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.context.annotation.Configuration;

/**
 * MyBatis配置类
 */
@Configuration
@MapperScan("com.jacky.mall.tiny.mbg.mapper")
public class MyBatisConfig {
}
