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
     * 查询黑名单列表
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
     * @param info
     * @return
     */
    public abstract boolean addBlackList(BlackListInfo info,boolean update);

    /**
     * 删除黑名单
     *
     * @param info
     * @return
     */
    public abstract boolean removeBlackList(BlackListInfo info);

    /**
     * 查询为上传到服务器的黑名单列表
     *
     * @return
     */
    public abstract List<BlackListInfo> getNoUploadBlackList();

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
     * @param info
     * @return
     */
    public abstract boolean addFilterGr(CallFilterInfo info);

    /**
     * 删除拦截分组
     *
     * @param info
     * @return
     */
    public abstract boolean removeFilterGr(CallFilterInfo info);

    /*拦截详细*/

    /**
     * 查询拦截详细列表
     *
     * @return
     */
    public abstract List<CallFilterInfo> getFilterDetList();

    /**
     * 查询拦截详细列表
     *
     * @return
     */
    public abstract int getFilterDetCount();

    /**
     * 增加拦截详情
     *
     * @param info
     * @return
     */
    public abstract boolean addFilterDet(CallFilterInfo info);

    /**
     * 删除拦截详情
     * @param info
     * @return
     */
    public abstract boolean removeFilterDet(CallFilterInfo info);

    /*陌生人分组*/

    /**
     * 查询陌生人分组列表
     * @return
     */
    public abstract  List<StrangerInfo> getStrangerGrList();

    /**
     * 查询陌生人分组数量
     * @return
     */
    public abstract int getStranagerGrCount();

    /**
     * 增加陌生人分组
     * @param info
     * @return
     */
    public abstract boolean addStrangerGr(StrangerInfo info);

    /**
     * 删除陌生人分组
     * @param info
     * @return
     */
    public abstract boolean removeStrangerGr(StrangerInfo info);

    /*陌生人详细*/

    /**
     * 查询陌生人详细列表
     * @return
     */
    public abstract List<StrangerInfo> getStrangerDetList();

    /**
     * 查询陌生人详细列表数量
     * @return
     */
    public abstract int getStrangerDetCount();

    /**
     * 增加陌生人详细列表
     * @param info
     * @return
     */
    public abstract boolean addStrangerDet(StrangerInfo info);

    /**
     * 删除陌生人详细列表
     * @param info
     * @return
     */
    public abstract boolean removeStrangerDet(StrangerInfo info);


    /**
     * 骚扰拦截是否开启
     * @return
     */
    public abstract boolean getFilterOpenState();

    /**
     * 设置骚扰拦截开启状态
     * @param flag
     */
    public abstract void setFilterOpenState(boolean flag);

    /**
     *  int[0]指定号码是否满足？：0,不满足;1,满足
     *  int[1]哪个弹框类型？
     * @param number
     * @return
     */
    public abstract int[] isCallFilterTip(String number);


    /*后台接口*/

    /**
     * 通话时长阀值，判断是否显示提示
     * @return
     */
    public abstract long getCallDurationMax();

    /**
     * 倍率参数：陌生号码通知提示显示
     * @return
     */
    public abstract int getStraNotiTipParam();

    /**
     * 倍率参数：黑名单，标记名单显示值
     * @return
     */
    public abstract int getBlackMarkTipParam();

    /**
     * 骚扰拦截用户量
     * @return
     */
    public abstract int getFilterUserNumber();

    /**
     * 后台下发黑名单列表
     * @return
     */
    public abstract List<BlackListInfo> getServerBlackList();


}
