package com.leo.appmaster.mgr;

import com.leo.appmaster.imagehide.PhotoAibum;
import com.leo.appmaster.imagehide.PhotoItem;
import com.leo.appmaster.videohide.VideoBean;
import com.leo.appmaster.videohide.VideoItemBean;

import java.util.List;

/**
 * 隐私数据，图片、视频、账号等
 * Created by Jasper on 2015/9/28.
 */
public abstract class PrivacyDataManager extends Manager {

    /**
     * 单个图片扣除分数
     */
    public static final int SPA_PIC = 1;
    /**
     * 单个视频扣除分数
     */
    public static final int SPA_VID = 5;

    /**
     * 图片最大得分
     */
    public static final int MAX_PIC_SCORE = 30;
    /**
     * 视频最大得分
     */
    public static final int MAX_VID_SCORE = 20;

    @Override
    public String description() {
        return MgrContext.MGR_PRIVACY_DATA;
    }

    public abstract void init();

    /**
     * 获取所有隐藏图片文件夹
     *
     * @param mSuffix
     */
    public abstract List<PhotoAibum> getHidePicAlbum(String mSuffix);

    /**
     * 获取某个文件夹下的隐藏图片
     *
     * @param mFileInfo
     */
    public abstract List<PhotoItem> getHidePicFile(PhotoAibum mFileInfo);

    /**
     * 取消隐藏图片
     *
     * @param mPicPath
     */
    public abstract String cancelHidePic(String mPicPath);

    /**
     * 删除隐藏图片
     *
     * @param mPicPath
     */
    public abstract boolean deleteHidePic(String mPicPath);

    /**
     * 获取所有图片文件夹
     */
    public abstract List<PhotoAibum> getAllPicFile(String mmSuffix);

    /**
     * 隐藏图片
     *
     * @param mPicPath
     * @param mSuffix
     */
    public abstract String onHidePic(String mPicPath, String mSuffix);

    public abstract int onHideAllPic(List<String> mString);

    /**
     * 获取所有隐藏视频文件夹
     *
     * @param mVidSuffix
     */
    public abstract List<VideoBean> getHideVidAlbum(String mVidSuffix);

    /**
     * 获取某个隐藏视频文件夹
     *
     * @param mFileInfo
     */
    public abstract List<VideoItemBean> getHideVidFile(VideoBean mFileInfo);

    /**
     * 取消隐藏视频
     *
     * @param mVidPath
     */
    public abstract boolean cancelHideVid(String mVidPath);

    /**
     * 删除隐藏视频
     *
     * @param mVidPath
     */
    public abstract boolean deleteHideVid(String mVidPath);

    /**
     * 获取所有视频文件夹
     */
    public abstract List<VideoBean> getAllVidFile();

    /**
     * 隐藏视频
     *
     * @param mVidPath
     * @param mVidSuffix
     */
    public abstract boolean onHideVid(String mVidPath, String mVidSuffix);

    public abstract int onHideAllVid(List<String> mString);

    /**
     * 获取新增图片
     */
    public abstract List<PhotoItem> getAddPic();

    public abstract int getAddPicNum();

    /**
     * 已阅新增图片，更新数据
     */
    public abstract int haveCheckedPic();

    /**
     * 获取新增视频
     */
    public abstract List<VideoItemBean> getAddVid();

    public abstract int getAddVidNum();

    /**
     * 已阅新增视频，更新数据
     */
    public abstract int haveCheckedVid();

    public abstract int getNextToTargetId(int id);


    public abstract void registerMediaListener();

    public abstract void unregisterMediaListener();

    /**
     * 获取最大可被扣除分数的图片数量
     *
     * @return
     */
    public abstract int getMaxPicNum();

    /**
     * 获取最大可被扣除分数的视频数量
     *
     * @return
     */
    public abstract int getMaxVidNum();

    /**
     * 根据新增图片个数获取图片的分数
     *
     * @param newPicNum 新增图片个数
     * @return
     */
    public abstract int getPicScore(int newPicNum);

    /**
     * 根据新增图片个数获取视频的分数
     *
     * @param newVidNum 新增视频个数
     * @return
     */
    public abstract int getVidScore(int newVidNum);

    public abstract int getPicShouldScore(int newPicNum);

    public abstract int getVidShouldScore(int newVidNum);

    /**
     * 删除图片，来自图片媒体数据库
     * @param picUri
     * @return
     */
    public abstract int deletePicFromDatebase(String picUri);

}
