package com.leo.appmaster.ad;

import com.leo.leoadlib.ioc.InjectConfig;

/**
 * 配置sdk调用的业务类的相关信息
 * Created by lilibin on 16-3-23.
 */
public class Bean {
	/**
	 * 配置类路径
	 */
	@InjectConfig(classPath = "com.leo.appmaster.ad.Business")
	public String classPath;

	/**
	 * 配置方法名
	 */
	@InjectConfig(methodPath = "addEvent")
	public String methodName;

	/**
	 * 配置方法参数列表
	 */
	@InjectConfig(paramsList = "java.util.Map")
	public Object params;


}