package com.ooftf.docking.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 用于标记想要注入主App的
 */
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.CLASS)
public @interface AppShip {
}
