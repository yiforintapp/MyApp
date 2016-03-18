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
    public static final String ICON_SYSTEM = "icon_system";

    //抓拍界面必需在use_present之后展示，此时可能异步的图片保存,隐藏等操作并未完成，所以需要判断以下标记。
    //如果user_present之后已经存好了，则进入界面，如果还没有保存完，则由异步保存操作完成后自己进入界面

    //是否已经在系统锁界面有拍过照，在onPictureTaken时置为true，每次进入抓拍界面后置为false，用于避免单次系统解锁重复拍照
    public static boolean sHasTakenWhenUnlockSystemLock = false;

    //系统锁抓拍的照片是否已经完成保存，在异步线程的保存操作完成后置为true，每次onPictureTaken时置为false,用于判断在user present时是否可以展示抓拍界面
    public static boolean sHasPicTakenAtSystemLockSaved = false;

    //在user present时是否已经展示过抓拍界面，如果user present时照片已经保存完了，将会置为true并进入抓拍界面，用于判断异步保存线程在
    //保存完成后是否需要展示抓拍界面，（false表示：在user present时图片还没有保存完，因此没有展示，需要做保存操作的线程自己在保存完成后进入），在用于判断后置为false
    public static boolean sHasPicShowedWhenUserPresent = false;
    
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
