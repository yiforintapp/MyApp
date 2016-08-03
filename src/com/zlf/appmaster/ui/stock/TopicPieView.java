package com.zlf.appmaster.ui.stock;

import android.content.Context;
import android.util.AttributeSet;

import com.zlf.appmaster.model.stock.StockFavoriteItem;
import com.zlf.appmaster.model.topic.TopicItem;

import java.util.List;

/**
 * Created by Huang on 2015/6/23.
 */
public class TopicPieView extends DistributionPieView {
    public TopicPieView(Context context) {
        super(context);
        init();
    }
    public TopicPieView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public TopicPieView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init(){
        setPromptTag("题材");
    }

    /**
     * 设置数据
     */
    public void setData(List<TopicItem> itemsSet){
        mPercentPrompt.clear();
        mbShowPrompt = true;

        int len = itemsSet.size();
        if (len > 6){       // 多于6个不显示提示线
            //len = 6;
            mbShowPrompt = false;
        }

        int total = 0;  // 先计算总数
        for (TopicItem item:itemsSet) {
            List<StockFavoriteItem> values = item.getSubStockIDs();
            if (null != values)
                total += values.size();
        }

        if (total > 0){
            for (TopicItem item : itemsSet) {
                List<StockFavoriteItem> values = item.getSubStockIDs();
                int subStockCount = 0;
                if (null != values){
                    subStockCount  = values.size();
                }
                String prompt = String.format("%s %d只", item.getName(), subStockCount);

                float percent = (float)subStockCount/total;
                if (percent < 0.1){     // 占比小于10%，不显示标记
                    mbShowPrompt = false;
                }

                mPercentPrompt.add(new PercentPrompt(item.getColor(), percent, prompt));
            }

        }
        invalidate();
    }


}
