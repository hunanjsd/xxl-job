package com.xxl.job.admin.controller.interceptor;

import com.xxl.job.admin.controller.annotation.PermessionLimit;
import com.xxl.job.admin.core.conf.XxlJobAdminConfig;
import com.xxl.job.admin.core.util.CookieUtil;
import org.apache.commons.codec.digest.DigestUtils;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.math.BigInteger;

/**
 * 权限拦截, 简易版
 *
 * @author xuxueli 2015-12-12 18:09:04
 */
public class PermissionInterceptor extends HandlerInterceptorAdapter {

	//用于cookie设置记住我是的cookie key
	public static final String LOGIN_IDENTITY_KEY = "XXL_JOB_LOGIN_IDENTITY";
	//由账号和密码md5加密后的token
	public static final String LOGIN_IDENTITY_TOKEN;
	//静态代码块，一开始就从配置文件获取账号密码得到token
    static {
        String username = XxlJobAdminConfig.getAdminConfig().getLoginUsername();
        String password = XxlJobAdminConfig.getAdminConfig().getLoginPassword();

        // login token
        String tokenTmp = DigestUtils.md5Hex(username + "_" + password);
		tokenTmp = new BigInteger(1, tokenTmp.getBytes()).toString(16);

		LOGIN_IDENTITY_TOKEN = tokenTmp;
    }


	/**
	 *
	 * @param response
	 * @param username
	 * @param password
	 * @param ifRemember 是否记住账号密码
	 * @return
	 */
	public static boolean login(HttpServletResponse response, String username, String password, boolean ifRemember){

    	// login token
		String tokenTmp = DigestUtils.md5Hex(username + "_" + password);
		tokenTmp = new BigInteger(1, tokenTmp.getBytes()).toString(16);

		//从request中得到的token于static token进行对比
		if (!LOGIN_IDENTITY_TOKEN.equals(tokenTmp)){
			return false;
		}

		// do login,保存token 和cookie key
		CookieUtil.set(response, LOGIN_IDENTITY_KEY, LOGIN_IDENTITY_TOKEN, ifRemember);
		return true;
	}
	public static void logout(HttpServletRequest request, HttpServletResponse response){
		CookieUtil.remove(request, response, LOGIN_IDENTITY_KEY);
	}
	public static boolean ifLogin(HttpServletRequest request){
		String indentityInfo = CookieUtil.getValue(request, LOGIN_IDENTITY_KEY);
		if (indentityInfo==null || !LOGIN_IDENTITY_TOKEN.equals(indentityInfo.trim())) {
			return false;
		}
		return true;
	}


	/**
	 * 拦截每次request
	 * @param request
	 * @param response
	 * @param handler
	 * @return
	 * @throws Exception
	 */
	@Override
	public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
		
		if (!(handler instanceof HandlerMethod)) {
			return super.preHandle(request, response, handler);
		}
		//若没有登陆则查看request访问的方法是否需要权限访问
		if (!ifLogin(request)) {
			HandlerMethod method = (HandlerMethod)handler;
			PermessionLimit permission = method.getMethodAnnotation(PermessionLimit.class);
			if (permission == null || permission.limit()) {
				//无权限访问的方法，redirct到login页面
				response.sendRedirect(request.getContextPath() + "/toLogin");
				//request.getRequestDispatcher("/toLogin").forward(request, response);
				return false;
			}
		}
		
		return super.preHandle(request, response, handler);
	}
	
}
