
package com.leo.appmaster.mgr.impl;

import java.io.File;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.leo.appmaster.AppMasterApplication;
import com.leo.appmaster.AppMasterPreference;
import com.leo.appmaster.Constants;
import com.leo.appmaster.applocker.IntruderPhotoInfo;
import com.leo.appmaster.db.IntruderPhotoTable;
import com.leo.appmaster.db.PreferenceTable;
import com.leo.appmaster.intruderprotection.CameraUtils;
import com.leo.appmaster.intruderprotection.IntruderprotectionActivity;
import com.leo.appmaster.mgr.IntrudeSecurityManager;
import com.leo.appmaster.sdk.SDKWrapper;
import com.leo.appmaster.utils.BuildProperties;
import com.leo.appmaster.utils.LeoLog;
import com.leo.appmaster.utils.PrefConst;

public class IntrudeSecurityManagerImpl extends IntrudeSecurityManager {

    private IntruderPhotoTable iptable = new IntruderPhotoTable();
    // private AppMasterPreference sp =
    // AppMasterPreference.getInstance(mContext);
    private PreferenceTable pt = PreferenceTable.getInstance();

    /**
     * 获取当前模块的安全得分
     * 
     * @return
     */
    @Override
    public int getSecurityScore() {
        if (getIntruderMode()) {
            return VALUE_SCORE;
        } else {
            return 0;
        }
    }

    @Override
    public void onDestory() {
    }

    @Override
    public void clearAllPhotos() {
        iptable.deleteAllInfo();
    }

    @Override
    public void deletePhotoInfo(String path) {
        iptable.deleteIntruderPhotoInfo(path);
    }

    @Override
    public void switchIntruderMode(boolean flag) {
        boolean current = getIntruderMode();
        if(current!=flag){
            pt.putBoolean(PrefConst.KEY_SWITCH_FOR_INTRUDER_PROTECTION, flag);
            if(flag){
                SDKWrapper.addEvent(AppMasterApplication.getInstance(), SDKWrapper.P1,
                        "intruder", "intruder_enable");
            }else{
                SDKWrapper.addEvent(AppMasterApplication.getInstance(), SDKWrapper.P1,
                        "intruder", "intruder_close");
            }
            
            notifySecurityChange();
        }
    
        // sp.setIntruderProtectionOpen(flag);
    }

    @Override
    public void setTimesForTakePhoto(int times) {
        // sp.setFailureTimesToCatch(times);
        pt.putInt(PrefConst.KEY_FAILURE_TIMES_TO_CATCH, times);
    }

    @Override
    public int getTimesForTakePhoto() {
        return pt.getInt(PrefConst.KEY_FAILURE_TIMES_TO_CATCH, 1);
    }

    @Override
    public ArrayList<IntruderPhotoInfo> getPhotoInfoList() {
        ArrayList<IntruderPhotoInfo> infos = iptable.queryIntruderPhotoInfo();
        ArrayList<IntruderPhotoInfo> removedInfos = new ArrayList<IntruderPhotoInfo>();
        if (infos == null && infos.size() == 0) {
            return infos;
        }
        File file;
        for (int i = 0; i < infos.size(); i++) {
            file = new File(infos.get(i).getFilePath());
            if (!file.exists()) {
                LeoLog.i("ISManager", "file doesn't exist! delete :"+infos.get(i).getFilePath());
                removedInfos.add(infos.get(i));
            }
            else{
                LeoLog.i("ISManager", "file  exist! hasn't deleted :"+infos.get(i).getFilePath());
            }
        }
        // remove
        if(removedInfos.size()>0){
            infos.removeAll(removedInfos);
            for(IntruderPhotoInfo info: removedInfos){
                deletePhotoInfo(info.getFilePath());
            }
        }
        return infos;
    }

    @Override
    public boolean getIntruderMode() {
        // return sp.getIntruderProtectionOpen();
        boolean isIntruderSecurityAvailable = getIsIntruderSecurityAvailable();
        if(isIntruderSecurityAvailable) {
            return pt.getBoolean(PrefConst.KEY_SWITCH_FOR_INTRUDER_PROTECTION, true);
        } else {
            return pt.getBoolean(PrefConst.KEY_SWITCH_FOR_INTRUDER_PROTECTION, false);
        }
    }

    @Override
    public void setCatchTimes(int times) {
        // sp.setTimesOfCatchIntruder(times);
        pt.putInt(PrefConst.KEY_TIMES_OF_CATCH_INTRUDER, times);
    }

    @Override
    public int getCatchTimes() {
        // return sp.getTimesOfCatchIntruder();
        return pt.getInt(PrefConst.KEY_TIMES_OF_CATCH_INTRUDER, 0);
    }

    @Override
    public int getShowADorEvaluate() {
        // return sp.getShowTypeAtIntruderView();
        return pt.getInt(PrefConst.KEY_AD_TYPE_IN_INTRUDER_VIEW, 0);
    }

    @Override
    public void setShowADorEvaluate(int type) {
        pt.putInt(PrefConst.KEY_AD_TYPE_IN_INTRUDER_VIEW, type);
    }

    @Override
    public void insertInfo(IntruderPhotoInfo info) {
        if (iptable.isExist(info)) {
            iptable.updateIntruderPhotoInfo(info);
        } else {
            iptable.insertIntruderPhotoInfo(info);
        }
    }
    
    public ArrayList<IntruderPhotoInfo> sortInfosByTimeStamp(ArrayList<IntruderPhotoInfo> infos){
        
        ArrayList<IntruderPhotoInfo> resultList = new ArrayList<IntruderPhotoInfo>();
        if (infos != null) {
            resultList.addAll(infos);
        }else{
            return resultList;
        }
        // Collections.reverse(mInfosSorted);
        // "yyyyMMdd_HHmmss"
        Date date1 = null;
        Date date2 = null;
        for (int i = 0; i < resultList.size() - 1; i++) {
            for (int j = 0; j < resultList.size() - i - 1; j++) {
                SimpleDateFormat sdf = new SimpleDateFormat(Constants.INTRUDER_PHOTO_TIMESTAMP_FORMAT);
                String timeStamp1 = resultList.get(j).getTimeStamp();
                String timeStamp2 = resultList.get(j + 1).getTimeStamp();
                try {
                    date1 = sdf.parse(timeStamp1);
                    date2 = sdf.parse(timeStamp2);
                } catch (ParseException e) {
                    e.printStackTrace();
                    return resultList;
                }
                if (date1 == null || date2 == null)
                    return resultList;
                if (date1.before(date2)) {
                    IntruderPhotoInfo temp = resultList.get(j);
                    resultList.set(j, resultList.get(j + 1));
                    resultList.set(j + 1, temp);
                }
            }
        }
        return resultList;
    }

    @Override
    public int getMaxScore() {
        return VALUE_SCORE;
    }

    @Override
    public boolean getIsIntruderSecurityAvailable() {
        int checkCameraFacing = CameraUtils.checkCameraFacing();
        if (((checkCameraFacing == CameraUtils.FRONT_AND_BACK)  || (checkCameraFacing == CameraUtils.FRONT_FACING_ONLY)) && (!BuildProperties.isApiLevel14())){
            return true;
        }
        return false;
    }
}
