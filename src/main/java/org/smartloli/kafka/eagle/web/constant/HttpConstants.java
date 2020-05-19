package org.smartloli.kafka.eagle.web.constant;

/**
 * Http 常量
 * @author zhiwei_yang
 * @time 2020-4-27-15:47
 */
public class HttpConstants {

    /** 根路径 **/
    public final static String ROOT_URL = "/";

    /**登陆页**/
    public final static String LOGIN_URL = "/account/signin";

    /**登陆逻辑**/
    public final static String LOGIN_ACTION_URL = "/account/signin/action";

    /** 退出登陆 **/
    public final static String LOGOUT_URL = "/account/signout";

    /**主页**/
    public final static String INDEX_URL = "/index";

    /** 403 错误页 **/
    public final static String ERROR_403 = "/403";

    /** 404 错误页 **/
    public final static String ERROR_404 = "/404";

    /** 405 错误页 **/
    public final static String ERROR_405= "/405";

    /** 500 错误页 **/
    public final static String ERROR_500= "/500";

    /** 503 错误页 **/
    public final static String ERROR_503= "/503";

    //登陆用户名/密码参数
    public final static String LOGIN_USERNAME = "username";
    public final static String LOGIN_PASSWORD = "password";

    public final static String[] STATIC_RESOURCE_PATTERN_ARRAY = {"/**/*.js","/**/*.css","/**/*.png","/**/*.jpg","/**/*.ico","/**/*.ttf","**/*.woff","**/*.woff2"};
}
