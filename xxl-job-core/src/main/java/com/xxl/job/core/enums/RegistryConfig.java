package com.xxl.job.core.enums;

/**
 * Created by xuxueli on 17/5/10.
 */
//执行器向注册中心注册是的一些配置
public class RegistryConfig {

    public static final int BEAT_TIMEOUT = 30;
    public static final int DEAD_TIMEOUT = BEAT_TIMEOUT * 3;

    public enum RegistType{ EXECUTOR, ADMIN }

}
