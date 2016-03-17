package com.leo.appmaster.mgr;

import java.util.ArrayList;
import java.util.List;

import com.leo.appmaster.applocker.IntruderPhotoInfo;

/**
 * 入侵者防护
 * Created by Jasper on 2015/9/28.
 */
public abstract class IntrudeSecurityManager extends Manager {
    public final static int VALUE_SCORE = 4;
    public static int sFailTimesAtSystLock = 0;
    public static boolean sHasTakenWhenUnlockSystemLock = false;
    public static final String ICON_SYSTEM = "icon_system";
    
    @Override
    public String description() {
        return MgrContext.MGR_INTRUDE_SECURITY;
    }
    /**
     * 获取入侵者照片信息的列表
     * @return
     */
    public abstract ArrayList getPhotoInfoList();
    
    /**
     * 获取入侵者防护功能是否可用
     */
    public abstract boolean getIsIntruderSecurityAvailable();
    /**
     * 清除所有入侵者照片信息和照片
     * @return
     */
    public abstract void clearAllPhotos();
    /**
     * 删除特定入侵者照片信息和照片
     * @return
     */
    public abstract void deletePhotoInfo(String path);

    /**
     * 设置系统入侵者防护开关
     */
    public abstract void setSystIntruderProtectionSwitch(boolean isOpen);

    /**
     * 获取当前系统入侵者防护的开关状态
     */
    public abstract boolean getSystIntruderProtecionSwitch();

    /**
     * 设置入侵者防护
     * @return
     */
    public abstract void switchIntruderMode(boolean flag);
    /**
     * 获取入侵者防护当前状态
     * @return
     */
    public abstract boolean getIntruderMode();
    /**
     * 设置拍照所需的错误解锁次数
     * @return
     */    
    public abstract void setTimesForTakePhoto(int times);
    /**
     * 获得拍照所需的错误解锁次数
     * @return
     */    
    public abstract int getTimesForTakePhoto();
    /**
     * 设置入侵者防护界面可选的广告/五星好评展示
     * @return
     */
    public abstract void setShowADorEvaluate(int Type);
    
    /**
     * 获取入侵者防护界面可选的广告/五星好评展示状态
     * @return
     */
    public abstract int getShowADorEvaluate();
    /**
     * 设置抓拍到入侵者的次数
     * @return
     */
    public abstract void setCatchTimes(int times);
    /**
     * 获取抓拍到入侵者的次数
     * @return
     */
    public abstract int getCatchTimes();
    
    /**
     *  插入一条抓拍照片的数据
     */
    public abstract void insertInfo(IntruderPhotoInfo info);
    
    /**
     * 将照片info封装类的list按时间排序
     */
    public abstract ArrayList<IntruderPhotoInfo> sortInfosByTimeStamp(ArrayList<IntruderPhotoInfo> infos);
}
