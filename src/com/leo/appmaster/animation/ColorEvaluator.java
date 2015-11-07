package com.leo.appmaster.animation;


import com.leo.appmaster.home.HomeColor.ColorHolder;
import com.leo.appmaster.utils.LeoLog;
import com.leo.tools.animator.TypeEvaluator;

public class ColorEvaluator  implements TypeEvaluator{
    
    private ColorHolder targetHolder  = new ColorHolder();
    private int mColor;

    @Override
    public Object evaluate(float fraction, Object startValue, Object endValue) {
        // TODO Auto-generated method stub
        String startColor = (String) startValue;  
        String endColor = (String) endValue;  
        int startRed = Integer.parseInt(startColor.substring(1, 3), 16);  
        int startGreen = Integer.parseInt(startColor.substring(3, 5), 16);  
        int startBlue = Integer.parseInt(startColor.substring(5, 7), 16);  
        int endRed = Integer.parseInt(endColor.substring(1, 3), 16);  
        int endGreen = Integer.parseInt(endColor.substring(3, 5), 16);  
        int endBlue = Integer.parseInt(endColor.substring(5, 7), 16); 
        
        ColorHolder startHolder = new ColorHolder(startRed, startGreen, startBlue);
        ColorHolder endHolder = new ColorHolder(endRed, endGreen, endBlue);
        
        //fraction = fraction+0.3f >1.0f ? 1.0f:fraction+0.5f;
        
        targetHolder.red = (int) (startHolder.red+(endHolder.red-startHolder.red)*fraction);
        targetHolder.green = (int) (startHolder.green+(endHolder.green - startHolder.green)*fraction);
        targetHolder.blue = (int) (startHolder.blue+ (endHolder.blue-startHolder.blue)*fraction);
        
        LeoLog.i("ColorEvaluator",startValue.toString()+"  ---- "+endValue.toString()+" -----------"+fraction);
        LeoLog.i("ColorEvaluator","startRed="+startRed+",startGreen="+startGreen+",startBlue="+startBlue);
        LeoLog.i("ColorEvaluator","endRed="+endRed+",endGreen="+endGreen+",endBlue="+endBlue);
        LeoLog.i("ColorEvaluator","targetRed="+targetHolder.red+",targetGreen="+targetHolder.green+",targetBlue="+targetHolder.blue);
        
        mColor = targetHolder.toIntColor();
        LeoLog.i("ColorEvaluator", "return Color ="+mColor);
        return mColor;  
    }

}
