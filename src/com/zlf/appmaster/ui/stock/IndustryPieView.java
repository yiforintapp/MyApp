package com.zlf.appmaster.ui.stock;

import android.content.Context;
import android.util.AttributeSet;

import com.zlf.appmaster.model.industry.IndustryItem;

import java.util.List;

/**
 * Created by Huang on 2015/6/23.
 */
public class IndustryPieView extends DistributionPieView {
    public IndustryPieView(Context context) {
        super(context);
        init();
    }
    public IndustryPieView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public IndustryPieView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init(){
        setPromptTag("行业");
    }

    /**
     * 设置数据
     */
    public void setData(List<IndustryItem> items){
        mPercentPrompt.clear();
        mbShowPrompt = true;

        int len = items.size();
        if (len > 6){       // 多于6个不显示提示线
            //len = 6;
            mbShowPrompt = false;
        }

        int total = 0;  // 先计算总数
        for (int i = 0; i < len; i ++) {
            total += items.get(i).getSubStockCount();
        }

        if (total > 0){
            for (int i = 0; i < len; i ++){
                IndustryItem item = items.get(i);
                int subStockCount = item.getSubStockCount();
                String prompt = String.format("%s %d只", item.getName(), subStockCount);

                float percent = (float)subStockCount/total;
                if (percent < 0.1){     // 行业占比小于10%，不显示标记
                    mbShowPrompt = false;
                }

                mPercentPrompt.add(new PercentPrompt(item.getColor(), percent, prompt));
            }

        }

        invalidate();

    }
}
