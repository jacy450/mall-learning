package com.jacky.mall.config;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.context.annotation.Configuration;

/**
 * MyBatis配置类
 * Created by macro on 2019/4/8.
 */
@Configuration
@MapperScan({"com.jacky.mall.mbg.mapper","com.jacky.mall.dao"})
public class MyBatisConfig {
}
