package com.leo.appmaster.mgr;

import com.leo.appmaster.callfilter.BlackListInfo;
import com.leo.appmaster.callfilter.CallFilterInfo;
import com.leo.appmaster.callfilter.StrangerInfo;

import java.util.List;

/**
 * Created by runlee on 15-12-18.
 */
public abstract class CallFilterContextManager extends Manager {
    @Override
    public void onDestory() {

    }

    @Override
    public String description() {
        return MgrContext.MGR_CALL_FILTER;
    }

    /*黑名单*/

    /**
     * 查询黑名单列表(本地添加的黑名单)
     *
     * @return
     */
    public abstract List<BlackListInfo> getBlackList();

    /**
     * 查询黑名单数量
     *
     * @return
     */
    public abstract int getBlackListCount();

    /**
     * 增加黑名单
     *
     * @param blackList
     * @return
     */
    public abstract boolean addBlackList(List<BlackListInfo> blackList, boolean update);

    /**
     * 删除黑名单
     *
     * @param blackList
     * @return
     */
    public abstract boolean removeBlackList(List<BlackListInfo> blackList);

    /**
     * 指定号码是否存在黑名单中
     *
     * @param number
     * @return
     */
    public abstract boolean isExistBlackList(String number);

    /**
     * 查询为上传到服务器的黑名单列表
     *
     * @return
     */
    public abstract List<BlackListInfo> getNoUploadBlackList();

    /**
     * 分页查询为上传到服务器的黑名单列表
     *
     * @return
     */
    public abstract List<BlackListInfo> getNoUpBlackListLimit();

    /*拦截分组*/

    /**
     * 查询拦截分组列表
     *
     * @return
     */
    public abstract List<CallFilterInfo> getCallFilterGrList();

    /**
     * 查询拦截分组数量
     *
     * @return
     */
    public abstract int getCallFilterGrCount();

    /**
     * 增加拦截分组
     *
     * @param infos
     * @param update
     * @return
     */
    public abstract boolean addFilterGr(List<CallFilterInfo> infos, boolean update);

    /**
     * 删除拦截分组
     *
     * @param infos
     * @return
     */
    public abstract boolean removeFilterGr(List<CallFilterInfo> infos);

    /*拦截详细*/

    /**
     * 查询拦截详细列表
     *
     * @return
     */
    public abstract List<CallFilterInfo> getFilterDetList();

    /**
     * 指定ID查询拦截详细列表
     *
     * @param griId
     * @return
     */
    public abstract List<CallFilterInfo> getFilterDetListFroId(int griId);

    /**
     * 查询拦截详细列表
     *
     * @return
     */
    public abstract int getFilterDetCount();


    /**
     * 增加拦截详情
     *
     * @param update
     * @param infos
     * @return
     */
    public abstract boolean addFilterDet(List<CallFilterInfo> infos, boolean update);

    /**
     * 删除拦截详情
     *
     * @param infos
     * @return
     */
    public abstract boolean removeFilterDet(List<CallFilterInfo> infos);

    /*陌生人分组*/

    /**
     * 查询陌生人分组列表
     *
     * @return
     */
    public abstract List<StrangerInfo> getStrangerGrList();

    /**
     * 查询陌生人分组数量
     *
     * @return
     */
    public abstract int getStranagerGrCount();

    /**
     * 增加陌生人分组
     *
     * @param infos
     * @param update
     * @return
     */
    public abstract boolean addStrangerGr(List<StrangerInfo> infos, boolean update);

    /**
     * 删除陌生人分组
     *
     * @param infos
     * @return
     */
    public abstract boolean removeStrangerGr(List<StrangerInfo> infos);


    /**
     * 骚扰拦截是否开启
     *
     * @return
     */
    public abstract boolean getFilterOpenState();

    /**
     * 设置骚扰拦截开启状态
     *
     * @param flag
     */
    public abstract void setFilterOpenState(boolean flag);

    /**
     * int[0]指定号码是否满足？：0,不满足;1,满足
     * int[1]哪个弹框类型？：0,标记;1,黑名单
     *
     * @param number
     * @return
     */
    public abstract int[] isCallFilterTip(String number);


    /*后台接口*/

    /**
     * 通话时长阀值，判断是否显示提示
     *
     * @return
     */
    public abstract long getCallDurationMax();

    /**
     * 设置通话时长阀值，判断是否显示提示
     *
     * @param duration
     * @return
     */
    public abstract long setCallDurationMax(long duration);


    /**
     * 倍率参数：陌生号码通知提示显示
     *
     * @return
     */
    public abstract int getStraNotiTipParam();


    /**
     * 设置倍率参数：陌生号码通知提示显示
     *
     * @return
     */
    public abstract void setStraNotiTipParam(int params);
    /**
     * 倍率参数：黑名单，标记名单显示值
     *
     * @return
     */
    public abstract int getBlackMarkTipParam();

    /**
     * 设置倍率参数：黑名单，标记名单显示值
     *
     * @param number
     */
    public abstract void setBlackMarkTipParam(int number);

    /**
     * 骚扰拦截用户量
     *
     * @return
     */
    public abstract int getFilterUserNumber();

    /**
     * 设置链接用户量
     *
     * @param number
     */
    public abstract void setFilterUserNumber(int number);

    /**
     * 后台下发黑名单列表(本地未添加处理的黑名单)
     *
     * @return
     */
    public abstract List<BlackListInfo> getServerBlackList();

    /**
     * 骚扰拦截显示提示框：通过指定用户量参数值对比显示
     *
     * @return
     */
    public abstract int getFilterTipFroUser();

    /**
     * 设置骚扰拦截显示提示框：通过指定用户量参数值对比显示
     *
     * @param number
     */
    public abstract void setFilterTipFroUser(int number);

    /**
     * 添加服务器下发黑名单
     *
     * @param infos
     * @return
     */
    public abstract boolean addSerBlackList(List<BlackListInfo> infos);

    /**
     * 删除服务器下发黑名单
     *
     * @param infos
     * @return
     */
    public abstract boolean removeSerBlackList(List<BlackListInfo> infos);

    /**
     * 查询 服务器下发黑名单
     *
     * @return
     */
    public abstract List<BlackListInfo> getSerBlackList();

    /**
     * 通过指定的号码，查询服务器下发黑名单
     *
     * @return
     */
    public abstract List<BlackListInfo> getSerBlackListFroNum(String number);

    /**
     * 通过指定的号码，查询服务器下发黑名单人数
     *
     * @return
     */
    public abstract int getSerBlackNumFroNum(String number);

    /**
     * 通过指定的号码，查询服务器下发标记人数
     *
     * @return
     */
    public abstract int getSerMarkerNumFroNum(String number);

    /**
     * 获取黑名单来电提示显示的人数参数
     *
     * @return
     */
    public abstract int getSerBlackTipCount();

    /**
     * 设置黑名单来电提示显示的人数参数
     *
     * @param num
     * @return
     */
    public abstract void setSerBlackTipNum(int num);


    /**
     * 标记来电提示显示的人数参数
     *
     * @return
     */
    public abstract int getSerMarkTipCount();

    /**
     * 设置标记来电提示显示的人数参数
     *
     * @param num
     * @return
     */
    public abstract void setSerMarkTipNum(int num);

    /**
     * 设置服务器下发黑名单列表的链接
     *
     * @param filePath
     * @return
     */
    public abstract void setSerBlackFilePath(String filePath);

    /**
     * 获取服务器下发黑名单列表的链接
     *
     * @return
     */
    public abstract String getSerBlackFilePath();

    /**
     * 查询指定号码隐私联系人时候使用
     * @param number
     * @return
     */
    public abstract  boolean isPrivacyConUse(String number);


}
