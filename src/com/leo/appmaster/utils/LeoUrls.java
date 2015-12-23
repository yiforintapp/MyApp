package com.leo.appmaster.utils;

/**
 * Created by Jasper on 2015/12/21.
 */
public class LeoUrls {
    public static final String HTTP = "http://";
    public static final String DOMAIN = "api.leomaster.com";

    public static final String DOMAIN_FEEDBACK_PREFIX = "feedback.";
    public static final String PATH_FEEDBACK = "/appmaster/feedbackimage";

    public static final String URL_FEEDBACK = HTTP + DOMAIN_FEEDBACK_PREFIX + DOMAIN + PATH_FEEDBACK;

    public static final String DOMAIN_UPLOAD_BLACK_PREFIX = "telintercept.";
    public static final String PATH_UPLOAD_BLACK_ = "/report";
    public static final String URL_UPLOAD_BLACK = HTTP + DOMAIN_UPLOAD_BLACK_PREFIX + DOMAIN + PATH_UPLOAD_BLACK_;

    //FAQ
    public static final String FAR_REQUEST = "http://api.leomaster.com/appmaster/faq";
}
