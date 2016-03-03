package com.leo.appmaster.mgr;

import android.location.Location;

import com.leo.appmaster.phoneSecurity.SecurityInstructSet;
import com.leo.appmaster.privacycontact.ContactBean;

import java.util.List;

/**
 * 手机防盗
 * Created by Jasper on 2015/9/28.
 */
public abstract class LostSecurityManager extends Manager {
    @Override
    public String description() {
        return MgrContext.MGR_LOST_SECURITY;
    }

    /**
     * 获取使用手机防盗的数量
     * @return
     */
    public abstract int getUsePhoneSecurityCount();
    public abstract boolean setUsePhoneSecurityConut(int count);

    /**
     * 手机防盗是否打开
     * @return
     */
    public abstract boolean isUsePhoneSecurity();

    /**
     * 设置手机防盗开启
     * @return
     */
    public abstract boolean setUsePhoneSecurity(boolean securityState);

    /**
     * 添加手机防盗号码
     * @param contactBean
     *
     * @return 0:输入号码为空，1：输入号码为本机号码，2：添加成功，-1,默认值
     */
    public abstract int addPhoneSecurityNumber(ContactBean contactBean);

    /**
     * 修改手机防盗号码
     * @param phoneNumber
     * @return
     */
    public abstract boolean modifyPhoneSecurityNumber(String phoneNumber);

    /**
     * 获取手机防盗指令集合
     * @return
     */
    public abstract List<String> getPhoneSecurityInstructionSets();

    /**
     * 是否开启了高级保护(设备管理器)
     * @return
     */
    public abstract boolean isOpenAdvanceProtect();

    /**
     * 获取手机防盗已经保护的时间
     * int[0]:天数
     * int[1]：小时
     * 存在数据库操作
     * @return
     */
    public abstract int[] getPhoneProtectTime();

    /**
     * 设置打开手机防盗的时间
     *  需要异步操作
     */
    public abstract void setOpenSecurityTime();

    /**
     * 获取当前位置
     * Latitude 纬度
     * Longitude 经度
     * 注：必须使用异步线程调用
     * @param fromId 请求来源
     * @return
     * @throws InterruptedException
     */

    public abstract  Location getLocation(int fromId) throws InterruptedException;
    /***
     * 执行追踪手机位置操作
     * 如果不指定手机号，默认给防盗号码发送位置信息
     * @param number 电话号码
     * @param isExecuNoMsm 是否发送无法获取获取位置信息
     * @param otherFlag 其他需要直接进入到执行位置获取位置信息的情况，默认值：false
     * @return
     */
    public abstract  boolean executeLockLocateposition(String number,boolean isExecuNoMsm,boolean otherFlag);

    /**
     *执行擦除数据此操作
     * @return
     */
    public abstract  boolean executePhoneMasterClear();

    /**
     *  执行防盗警报操作
     *
     *
     * @param alertMode true，一直响铃，false，响铃一次
     * @return
     */

    public abstract  boolean executeAlert(boolean alertMode);
    public abstract boolean executeStopAlert(boolean isStopAlert);
    /**
     * 执行一键防盗操作()
     * @return
     */
    public abstract  boolean executeOnekey();

    /**
     * * 执行锁定手机操作
     * 耗时操作 异步
     * @param isLockListener 是否来自监控系统安装应用的加锁
     * @param packageName 安装应用的包名
     * @param isClearAllLockList 是否清空所有模式所有锁列表
     * @return
     */
    public abstract  boolean executeLockPhone(boolean isLockListener,String packageName,boolean isClearAllLockList);


    /**
     * 获取本机的电话号码
     * 注：不是所有的都能获取到
     * @return
     */
    public abstract String getSelfPhoneNumnber();

    /**
     * 获取手机防盗号码
     * @return
     */
    public abstract String getPhoneSecurityNumber();

    /**
     * 获取已经激活的防盗指令
     * @return
     */
    public abstract List<String> getActivateInstructs();

    /**
     * 获取SIM卡是否改变
     * @return
     */
    public abstract boolean getIsSimChange();

    /**
     * 设置保存手机卡的唯一标识IMEI
     * @return
     */
    public abstract boolean setSimIMEI();

    /**
     * 当前手机是否存在SIM卡
     * @return
     */
    public abstract boolean getIsExistSim();

}
