
package com.leo.appmaster.ui;

import java.util.ArrayList;
import java.util.Collections;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.Region;
import android.graphics.Shader.TileMode;
import android.graphics.drawable.NinePatchDrawable;
import android.os.Parcelable;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import com.leo.appmaster.R;
import com.leo.appmaster.utils.ManagerFlowUtils;

/**
 * Created by Dacer on 11/4/13. Edited by Lee youngchan 21/1/14 Edited by dector
 * 30-Jun-2014
 */
public class LineView extends View {
    private int mViewHeight;
    private int mViewWidth2;
    // drawBackground
    private boolean autoSetDataOfGird = true;
    private boolean autoSetGridWidth = true;
    private int dataOfAGird = 10;
    private int bottomTextHeight = 0;
    private ArrayList<String> bottomTextList = new ArrayList<String>();

    private ArrayList<ArrayList<Integer>> dataLists;

    private ArrayList<Integer> xCoordinateList = new ArrayList<Integer>();
    private ArrayList<Integer> yCoordinateList = new ArrayList<Integer>();

    private ArrayList<ArrayList<Dot>> drawDotLists = new ArrayList<ArrayList<Dot>>();

    private Paint bottomTextPaint = new Paint();
    private Paint TodayTextPaint = new Paint();
    private int bottomTextDescent;

    // popup
    private Paint popupTextPaint = new Paint();
    private Paint TextPaint = null;
    private final int bottomTriangleHeight = 12;
    public boolean showPopup = true;

    private Dot pointToSelect;
    private Dot selectedDot;

    private int topLineLength = ManagerFlowUtils.dip2px(getContext(), 12);; // |
                                                                            // |
                                                                            // ←this
    // -+-+-
    private int sideLineLength = ManagerFlowUtils.dip2px(getContext(), 45) / 3 * 2;// --+--+--+--+--+--+--
    // ↑this
    private int backgroundGridWidth = ManagerFlowUtils.dip2px(getContext(), 45);

    // Constants
    private final int popupTopPadding = ManagerFlowUtils.dip2px(getContext(), 2);
    private final int popupBottomMargin = ManagerFlowUtils.dip2px(getContext(), 5);
    private final int bottomTextTopMargin = ManagerFlowUtils.sp2px(getContext(), 5);
    private final int bottomLineLength = ManagerFlowUtils.sp2px(getContext(), 22);
    private final int DOT_INNER_CIR_RADIUS = ManagerFlowUtils.dip2px(getContext(), 2);
    private final int DOT_OUTER_CIR_RADIUS = ManagerFlowUtils.dip2px(getContext(), 5);
    private final int MIN_TOP_LINE_LENGTH = ManagerFlowUtils.dip2px(getContext(), 12);
    private final int MIN_VERTICAL_GRID_NUM = 4;
    private final int MIN_HORIZONTAL_GRID_NUM = 1;
    private final int BACKGROUND_LINE_COLOR = Color.parseColor("#EEEEEE");
    private final int BOTTOM_TEXT_COLOR = Color.parseColor("#9B9A9B");

    public static final int SHOW_POPUPS_All = 1;
    public static final int SHOW_POPUPS_MAXMIN_ONLY = 2;
    public static final int SHOW_POPUPS_NONE = 3;

    private int showPopupType = SHOW_POPUPS_NONE;

    public void setShowPopup(int popupType) {
        this.showPopupType = popupType;
    }

    // private String[] colorArray = {
    // "#2980b9", "#e74c3c", "#e74c3c"
    // };
    // 25-175 #ef7b19
    // 175-325 #d49c11
    // 225-475 #b2c508
    // 475-625 #9ddf01
    private String[] colorArray = {
            "#9ddf01", "#b2c508", "#d49c11", "#ef7b19"
    };
    // height = 200:
    // 25-135 #ef7b19
    // 135-250 #d49c11
    // 250-365 #b2c508
    // 365-475 #9ddf01
    // private int[] height = {
    // 135, 250, 365, 475
    // }; 
    private int[] mColorheight = {0,0,0,0};
    // Color.rgb(140, 234, 255)

    // popup 컬러
    private int[] popupColorArray = {
            R.drawable.popup_c1, R.drawable.popup_c2, R.drawable.popup_c3, R.drawable.popup_c4
    };
    // private int[] popupColorArray = {
    // R.drawable.popup_blue, R.drawable.popup_red, R.drawable.popup_green
    // };

    // onDraw optimisations
    private final Point tmpPoint = new Point();

    public interface BackUpCallBack {
        public void beforeBackup(int max);
    }

    public BackUpCallBack mCallBack;

    public void setDrawDotLine(Boolean drawDotLine, BackUpCallBack backUpCallBack) {
        this.mCallBack = backUpCallBack;
    }

    private Runnable animator = new Runnable() {
        @Override
        public void run() {
            boolean needNewFrame = false;
            for (ArrayList<Dot> data : drawDotLists) {
                for (Dot dot : data) {
                    dot.update();
                    if (!dot.isAtRest()) {
                        needNewFrame = true;
                    }
                }
            }
            if (needNewFrame) {
                postDelayed(this, 50);
            }
            invalidate();
        }
    };

    public LineView(Context context) {
        this(context, null);
    }

    public LineView(Context context, AttributeSet attrs) {
        super(context, attrs);
        // TextPaint.setAntiAlias(true);
        // TextPaint.setTextSize(ManagerFlowUtils.sp2px(getContext(), 13));
        // TextPaint.setTypeface(Typeface.DEFAULT_BOLD);
        // TextPaint.setTextAlign(Paint.Align.CENTER);
        // TextPaint.setStrokeWidth(5);

        popupTextPaint.setAntiAlias(true);
        popupTextPaint.setColor(Color.WHITE);
        popupTextPaint.setTextSize(ManagerFlowUtils.sp2px(getContext(), 13));
        popupTextPaint.setStrokeWidth(5);
        popupTextPaint.setTextAlign(Paint.Align.CENTER);

        bottomTextPaint.setAntiAlias(true);
        bottomTextPaint.setTextSize(ManagerFlowUtils.sp2px(getContext(), 12));
        bottomTextPaint.setTextAlign(Paint.Align.CENTER);
        bottomTextPaint.setStyle(Paint.Style.FILL);
        bottomTextPaint.setColor(BOTTOM_TEXT_COLOR);

        TodayTextPaint.setAntiAlias(true);
        TodayTextPaint.setFakeBoldText(true);
        TodayTextPaint.setTextSize(ManagerFlowUtils.sp2px(getContext(), 12));
        TodayTextPaint.setTextAlign(Paint.Align.CENTER);
        TodayTextPaint.setStyle(Paint.Style.FILL);
        TodayTextPaint.setColor(Color.parseColor("#ef7b19"));

        // line_preferences = AppMasterPreference.getInstance(context);
        // MonthTotalTraffic = line_preferences.getTotalTraffic();
    }

    /**
     * dataList will be reset when called is method.
     * 
     * @param bottomTextList The String ArrayList in the bottom.
     */
    public void setBottomTextList(ArrayList<String> bottomTextList) {
        this.bottomTextList = bottomTextList;

        Rect r = new Rect();
        int longestWidth = 0;
        String longestStr = "";
        bottomTextDescent = 0;

        // 通过循环对比出字符集合中最长最宽的字符，设置矩形框
        for (String s : bottomTextList) {
            bottomTextPaint.getTextBounds(s, 0, s.length(), r);
            if (bottomTextHeight < r.height()) {
                bottomTextHeight = r.height();
            }
            if (autoSetGridWidth && (longestWidth < r.width())) {
                longestWidth = r.width();
                longestStr = s;
            }
            if (bottomTextDescent < (Math.abs(r.bottom))) {
                bottomTextDescent = Math.abs(r.bottom);
            }
        }

        if (autoSetGridWidth) {
            if (backgroundGridWidth < longestWidth) {
                backgroundGridWidth = longestWidth
                        + (int) bottomTextPaint.measureText(longestStr, 0, 1);
            }
            if (sideLineLength < longestWidth / 2) {
                sideLineLength = longestWidth / 2;
            }
        }

        refreshXCoordinateList(getHorizontalGridNum());
    }

    /**
     * @param dataLists The Integer ArrayLists for showing, dataList.size() must
     *            < bottomTextList.size()
     */
    public void setDataList(ArrayList<ArrayList<Integer>> dataLists) {
        selectedDot = null;
        this.dataLists = dataLists;
        for (ArrayList<Integer> list : dataLists) {
//            LeoLog.d("testLineView", "list.size() : " + list.size() + "-- bottomTextList.size() : " +  bottomTextList.size());
            if (list.size() > bottomTextList.size()) {
                throw new RuntimeException("dacer.LineView error:" +
                        " dataList.size() > bottomTextList.size() !!!");
            }
        }
        int biggestData = 0;
        for (ArrayList<Integer> list : dataLists) {
            if (autoSetDataOfGird) {
                for (Integer i : list) {
                    if (biggestData < i) {
                        biggestData = i;
                    }
                }
            }
            dataOfAGird = 1;
            while (biggestData / 10 > dataOfAGird) {
                dataOfAGird *= 10;
            }
        }

        refreshAfterDataChanged();
        showPopup = true;
        setMinimumWidth(0); // It can help the LineView reset the Width,
                            // I don't know the better way..
        postInvalidate();
    }

    private void refreshAfterDataChanged() {
        int verticalGridNum = getVerticalGridlNum();
        refreshTopLineLength(verticalGridNum);
        refreshYCoordinateList(verticalGridNum);
        refreshDrawDotList(verticalGridNum);
    }

    private int getVerticalGridlNum() {
        int verticalGridNum = MIN_VERTICAL_GRID_NUM;
        if (dataLists != null && !dataLists.isEmpty()) {
            for (ArrayList<Integer> list : dataLists) {
                for (Integer integer : list) {
                    if (verticalGridNum < (integer + 1)) {
                        verticalGridNum = integer + 1;
                    }
                }
            }
        }
        return verticalGridNum;
    }

    private int getHorizontalGridNum() {
        int horizontalGridNum = bottomTextList.size() - 1;
        if (horizontalGridNum < MIN_HORIZONTAL_GRID_NUM) {
            horizontalGridNum = MIN_HORIZONTAL_GRID_NUM;
        }
        return horizontalGridNum;
    }

    private void refreshXCoordinateList(int horizontalGridNum) {
        xCoordinateList.clear();
        for (int i = 0; i < (horizontalGridNum + 1); i++) {
            xCoordinateList.add(sideLineLength + backgroundGridWidth * i);
        }

    }

    private void refreshYCoordinateList(int verticalGridNum) {
        yCoordinateList.clear();
        for (int i = 0; i < (verticalGridNum + 1); i++) {
            yCoordinateList.add(topLineLength +
                    ((mViewHeight - topLineLength - bottomTextHeight - bottomTextTopMargin -
                            bottomLineLength - bottomTextDescent) * i / (verticalGridNum)));
        }
    }

    private void refreshDrawDotList(int verticalGridNum) {
        if (dataLists != null && !dataLists.isEmpty()) {
            if (drawDotLists.size() == 0) {
                for (int k = 0; k < dataLists.size(); k++) {
                    drawDotLists.add(new ArrayList<LineView.Dot>());
                }
            }
            for (int k = 0; k < dataLists.size(); k++) {
                int drawDotSize = drawDotLists.get(k).isEmpty() ? 0 : drawDotLists.get(k).size();

                for (int i = 0; i < dataLists.get(k).size(); i++) {
                    int x = xCoordinateList.get(i);
                    int y = yCoordinateList.get(verticalGridNum - dataLists.get(k).get(i));
                    if (i > drawDotSize - 1) {
                        // 도트리스트를 추가한다.
                        drawDotLists.get(k).add(new Dot(x, 0, x, y, dataLists.get(k).get(i), k));
                    } else {
                        // 도트리스트에 타겟을 설정한다.
                        drawDotLists.get(k).set(
                                i,
                                drawDotLists.get(k).get(i)
                                        .setTargetData(x, y, dataLists.get(k).get(i), k));
                    }
                }

                int temp = drawDotLists.get(k).size() - dataLists.get(k).size();
                for (int i = 0; i < temp; i++) {
                    drawDotLists.get(k).remove(drawDotLists.get(k).size() - 1);
                }
            }
        }
        removeCallbacks(animator);
        post(animator);
    }

    private void refreshTopLineLength(int verticalGridNum) {
        // For prevent popup can't be completely showed when
        // backgroundGridHeight is too small.
        // But this code not so good.
        if ((mViewHeight - topLineLength - bottomTextHeight - bottomTextTopMargin) /
                (verticalGridNum + 2) < getPopupHeight()) {
            topLineLength = getPopupHeight() + DOT_OUTER_CIR_RADIUS + DOT_INNER_CIR_RADIUS + 2;
            // topLineLength=5;
        } else {
            topLineLength = MIN_TOP_LINE_LENGTH;
            // topLineLength=1;
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        drawBackgroundLines(canvas);
        drawLines(canvas);
        drawDots(canvas);

        for (int k = 0; k < drawDotLists.size(); k++) {
            int MaxValue = Collections.max(dataLists.get(k));
            int MinValue = Collections.min(dataLists.get(k));
            for (Dot d : drawDotLists.get(k)) {

                // 点线已画好，现在把泡泡上的KB数字转换成MB（如需要）
                int mKb = d.data;
                float kb = d.data;
                String popText;
                if (mKb < 1024) {
                    popText = String.valueOf(mKb) + "KB";
                } else {
                    double e = kb / 1024;
                    java.text.DecimalFormat df = new java.text.DecimalFormat("#.00");
                    popText = df.format(e) + "MB";
                }

                // String.valueOf(d.data)
                if (showPopupType == SHOW_POPUPS_All)
                    drawPopup(canvas, popText, d.setupPoint(tmpPoint),
                            popupColorArray[0]);
                else if (showPopupType == SHOW_POPUPS_MAXMIN_ONLY) {
                    if (d.data == MaxValue)
                        drawPopup(canvas, String.valueOf(d.data), d.setupPoint(tmpPoint),
                                popupColorArray[k % 3]);
                    if (d.data == MinValue)
                        drawPopup(canvas, String.valueOf(d.data), d.setupPoint(tmpPoint),
                                popupColorArray[k % 3]);
                } else {
                    JustDrawText(canvas, popText, d.setupPoint(tmpPoint));
                }
            }
        }

        // 선택한 dot 만 popup 이 뜨게 한다.
        if (showPopup && selectedDot != null) {
            drawPopup(canvas,
                    String.valueOf(selectedDot.data),
                    selectedDot.setupPoint(tmpPoint), popupColorArray[selectedDot.linenumber % 3]);
        }
    }

    private void JustDrawText(Canvas canvas, String popText, Point point) {
        // 25-175 #ef7b19
        // 175-325 #d49c11
        // 225-475 #b2c508
        // 475-625 #9ddf01
        int mTextColor = 0;
        int x = point.x;
        int y = point.y - ManagerFlowUtils.dip2px(getContext(), 5);
        if (y < 175) {
            mTextColor = Color.parseColor(colorArray[3]);
        } else if (y > 175 && y < 325) {
            mTextColor = Color.parseColor(colorArray[2]);
        } else if (y > 325 && y < 475) {
            mTextColor = Color.parseColor(colorArray[1]);
        } else {
            mTextColor = Color.parseColor(colorArray[0]);
        }
        TextPaint.setColor(mTextColor);
        Rect popupTextRect = new Rect();
        TextPaint.getTextBounds(popText, 0, popText.length(), popupTextRect);
        canvas.drawText(popText, x, y - bottomTriangleHeight - popupBottomMargin, TextPaint);
    }

    /**
     * @param canvas The canvas you need to draw on.
     * @param point The Point consists of the x y coordinates from left bottom
     *            to right top. Like is 3 2 1 0 1 2 3 4 5
     */
    private void drawPopup(Canvas canvas, String num, Point point, int PopupColor) {
        // 25-175 #ef7b19
        // 175-325 #d49c11
        // 225-475 #b2c508
        // 475-625 #9ddf01

        boolean singularNum = (num.length() == 1);
        int sidePadding = ManagerFlowUtils.dip2px(getContext(), singularNum ? 8 : 5);
        int x = point.x;
        int y = point.y - ManagerFlowUtils.dip2px(getContext(), 5);
        Rect popupTextRect = new Rect();
        popupTextPaint.getTextBounds(num, 0, num.length(), popupTextRect);
        Rect r = new Rect(x - popupTextRect.width() / 2 - sidePadding,
                y - popupTextRect.height() - bottomTriangleHeight - popupTopPadding * 2
                        - popupBottomMargin,
                x + popupTextRect.width() / 2 + sidePadding,
                y + popupTopPadding - popupBottomMargin);

        if (y < mColorheight[0]) {
            PopupColor = popupColorArray[3];
        } else if (y > mColorheight[0] && y < mColorheight[1]) {
            PopupColor = popupColorArray[2];
        } else if (y > mColorheight[1] && y < mColorheight[2]) {
            PopupColor = popupColorArray[1];
        } else {
            PopupColor = popupColorArray[0];
        }

        NinePatchDrawable popup = (NinePatchDrawable) getResources().getDrawable(PopupColor);
        popup.setBounds(r);
        popup.draw(canvas);
        canvas.drawText(num, x, y - bottomTriangleHeight - popupBottomMargin, popupTextPaint);
    }

    private int getPopupHeight() {
        Rect popupTextRect = new Rect();
        popupTextPaint.getTextBounds("9", 0, 1, popupTextRect);
        Rect r = new Rect(-popupTextRect.width() / 2,
                -popupTextRect.height() - bottomTriangleHeight - popupTopPadding * 2
                        - popupBottomMargin,
                +popupTextRect.width() / 2,
                +popupTopPadding - popupBottomMargin);
        return r.height();
    }

    // 도트그리기
    private void drawDots(Canvas canvas) {

        int scrollToDoc = 0;
        int todayIs = ManagerFlowUtils.getDayOfMonth();
        // int todayIs = 15;
        int scroll1 = 0;
        int scroll2 = 0;
        int dotX = 0;
        int totalMove = 0;
        int mDocColor = 0;
        // 25-175 #ef7b19
        // 175-325 #d49c11
        // 225-475 #b2c508
        // 475-625 #9ddf01
        Paint bigCirPaint = new Paint();
        bigCirPaint.setAntiAlias(true);
        Paint smallCirPaint = new Paint(bigCirPaint);
        smallCirPaint.setColor(Color.parseColor("#FFFFFF"));
        if (drawDotLists != null && !drawDotLists.isEmpty()) {
            for (int k = 0; k < drawDotLists.size(); k++) {
                for (Dot dot : drawDotLists.get(k)) {
                    if (dot.y < mColorheight[0]) {
                        mDocColor = Color.parseColor(colorArray[3]);
                    } else if (dot.y > mColorheight[0] && dot.y < mColorheight[1]) {
                        mDocColor = Color.parseColor(colorArray[2]);
                    } else if (dot.y > mColorheight[1] && dot.y < mColorheight[2]) {
                        mDocColor = Color.parseColor(colorArray[1]);
                    } else {
                        mDocColor = Color.parseColor(colorArray[0]);
                    }
                    bigCirPaint.setColor(mDocColor);

                    if (scrollToDoc == 0) {
                        scroll1 = dot.x;
                    }

                    if (scrollToDoc == 1) {
                        scroll2 = dot.x;
                    }

                    canvas.drawCircle(dot.x, dot.y, DOT_OUTER_CIR_RADIUS, bigCirPaint);
                    canvas.drawCircle(dot.x, dot.y, DOT_INNER_CIR_RADIUS, smallCirPaint);
                    scrollToDoc++;
                }
            }
        }

        // scroll To where on first create
        dotX = scroll2 - scroll1;
        if (todayIs < 5) {
            totalMove = 0;
        } else {
            int over = todayIs - 4;
            // totalMove = dotX*(todayIs-3) + scroll1+dotX/2;
            totalMove = over * dotX + scroll1;
        }
        mCallBack.beforeBackup(totalMove);

    }

    // 线
    private void drawLines(Canvas canvas) {
        // height = 250:
        // 25-175 #ef7b19
        // 175-325 #d49c11
        // 325-475 #b2c508
        // 475-625 #9ddf01
        // height = 200:
        // 25-135 #ef7b19
        // 135-250 #d49c11
        // 250-365 #b2c508
        // 365-475 #9ddf01
        Paint linePaint = new Paint();
        int mOneColor = 0;
        int mTwoColor = 0;
        for (int k = 0; k < drawDotLists.size(); k++) {
            // linePaint.setColor(Color.parseColor(colorArray[k % 3]));
            for (int i = 0; i < drawDotLists.get(k).size() - 1; i++) {
                int docOneY = drawDotLists.get(k).get(i).y;
                int docTwoY = drawDotLists.get(k).get(i + 1).y;
                if (docOneY < mColorheight[0]) {
                    if (docTwoY < mColorheight[0]) {
                        mOneColor = Color.parseColor(colorArray[3]);
                        mTwoColor = Color.parseColor(colorArray[3]);
                    } else if (docTwoY > mColorheight[0] && docTwoY < mColorheight[1]) {
                        mOneColor = Color.parseColor(colorArray[3]);
                        mTwoColor = Color.parseColor(colorArray[2]);
                    } else if (docTwoY > mColorheight[1] && docTwoY < mColorheight[2]) {
                        mOneColor = Color.parseColor(colorArray[3]);
                        mTwoColor = Color.parseColor(colorArray[1]);
                    } else {
                        mOneColor = Color.parseColor(colorArray[3]);
                        mTwoColor = Color.parseColor(colorArray[0]);
                    }
                } else if (docOneY > mColorheight[0] && docOneY < mColorheight[1]) {
                    if (docTwoY < mColorheight[0]) {
                        mOneColor = Color.parseColor(colorArray[2]);
                        mTwoColor = Color.parseColor(colorArray[3]);
                    } else if (docTwoY > mColorheight[0] && docTwoY < mColorheight[1]) {
                        mOneColor = Color.parseColor(colorArray[2]);
                        mTwoColor = Color.parseColor(colorArray[2]);
                    } else if (docTwoY > mColorheight[1] && docTwoY < mColorheight[2]) {
                        mOneColor = Color.parseColor(colorArray[2]);
                        mTwoColor = Color.parseColor(colorArray[1]);
                    } else {
                        mOneColor = Color.parseColor(colorArray[2]);
                        mTwoColor = Color.parseColor(colorArray[0]);
                    }
                } else if (docOneY > mColorheight[1] && docOneY < mColorheight[2]) {
                    if (docTwoY < mColorheight[0]) {
                        mOneColor = Color.parseColor(colorArray[1]);
                        mTwoColor = Color.parseColor(colorArray[3]);
                    } else if (docTwoY > mColorheight[0] && docTwoY < mColorheight[1]) {
                        mOneColor = Color.parseColor(colorArray[1]);
                        mTwoColor = Color.parseColor(colorArray[2]);
                    } else if (docTwoY > mColorheight[1] && docTwoY < mColorheight[2]) {
                        mOneColor = Color.parseColor(colorArray[1]);
                        mTwoColor = Color.parseColor(colorArray[1]);
                    } else {
                        mOneColor = Color.parseColor(colorArray[1]);
                        mTwoColor = Color.parseColor(colorArray[0]);
                    }
                } else {
                    if (docTwoY < mColorheight[0]) {
                        mOneColor = Color.parseColor(colorArray[0]);
                        mTwoColor = Color.parseColor(colorArray[3]);
                    } else if (docTwoY > mColorheight[0] && docTwoY < mColorheight[1]) {
                        mOneColor = Color.parseColor(colorArray[0]);
                        mTwoColor = Color.parseColor(colorArray[2]);
                    } else if (docTwoY > mColorheight[1] && docTwoY < mColorheight[2]) {
                        mOneColor = Color.parseColor(colorArray[0]);
                        mTwoColor = Color.parseColor(colorArray[1]);
                    } else {
                        mOneColor = Color.parseColor(colorArray[0]);
                        mTwoColor = Color.parseColor(colorArray[0]);
                    }
                }
                LinearGradient Agradient = new LinearGradient(drawDotLists.get(k).get(i).x,
                        drawDotLists.get(k).get(i).y, drawDotLists.get(k).get(i + 1).x,
                        drawDotLists.get(k).get(i + 1).y, new int[] {
                                mOneColor,
                                mTwoColor
                        }, new float[] {
                                (float) 0, (float) 1
                        },
                        TileMode.REPEAT);
                linePaint.setShader(Agradient);
                linePaint.setAntiAlias(true);
                linePaint.setStrokeWidth(ManagerFlowUtils.dip2px(getContext(), 2));

//                Log.d(TAG, "first point Y is :" + drawDotLists.get(k).get(i + 1).y);

                canvas.drawLine(drawDotLists.get(k).get(i).x,
                        drawDotLists.get(k).get(i).y,
                        drawDotLists.get(k).get(i + 1).x,
                        drawDotLists.get(k).get(i + 1).y,
                        linePaint);
            }
        }
    }

    private void drawBackgroundLines(Canvas canvas) {
        Paint paint = new Paint();
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(ManagerFlowUtils.dip2px(getContext(), 1f));
        paint.setColor(BACKGROUND_LINE_COLOR);

        // draw vertical lines
        for (int i = 0; i < xCoordinateList.size(); i++) {
            canvas.drawLine(xCoordinateList.get(i),
                    0,
                    xCoordinateList.get(i),
                    mViewHeight - bottomTextTopMargin * 5 - bottomTextHeight - bottomTextDescent,
                    paint);
        }

        // draw horizontal lines
        float totalLenght = mViewHeight - bottomTextTopMargin * 5 - bottomTextHeight
                - bottomTextDescent;
        float fivePart = totalLenght / 5;
        for (int i = 0; i < 6; i++) {
            float y = totalLenght - i * fivePart;
            if (i == 5) {
                y = 1.5f;
            }
            canvas.drawLine(0, y, mViewWidth2, y, paint);
        }

        // // draw dotted lines
        // paint.setPathEffect(effects);
        // Path dottedPath = new Path();
        // for (int i = 0; i < yCoordinateList.size(); i++) {
        // if ((yCoordinateList.size() - 1 - i) % dataOfAGird == 0) {
        // dottedPath.moveTo(0, yCoordinateList.get(i));
        // dottedPath.lineTo(getWidth(), yCoordinateList.get(i));
        // canvas.drawPath(dottedPath, paint);
        // }
        // }

        // draw bottom text
        if (bottomTextList != null) {
            for (int i = 0; i < bottomTextList.size(); i++) {
                float TextX = sideLineLength + backgroundGridWidth * i;
                float TextY = mViewHeight - bottomTextDescent - bottomTextTopMargin * 2;

                if (i == 0) {

                } else {

                }

                // Today Text
                if (!bottomTextList.get(i).contains("/")) {
                    canvas.drawText(bottomTextList.get(i), TextX, TextY, TodayTextPaint);
                    continue;
                }

                canvas.drawText(bottomTextList.get(i), TextX, TextY, bottomTextPaint);
            }
        }

        // if (!drawDotLine) {
        // // draw solid lines
        // for (int i = 0; i < yCoordinateList.size(); i++) {
        // if ((yCoordinateList.size() - 1 - i) % dataOfAGird == 0) {
        // canvas.drawLine(0, yCoordinateList.get(i), getWidth(),
        // yCoordinateList.get(i),
        // paint);
        // }
        // }
        // }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int mViewWidth = measureWidth(widthMeasureSpec);
        mViewWidth2 = measureWidth(widthMeasureSpec);
        mViewHeight = measureHeight(heightMeasureSpec);
        // 就算总高度，减去text的高度，剩下设置颜色区域
        setLineDocColorArea(mViewHeight, bottomTextHeight);

        // mViewHeight = MeasureSpec.getSize(measureSpec);
        refreshAfterDataChanged();
        setMeasuredDimension(mViewWidth, mViewHeight);
    }

    private void setLineDocColorArea(int allHeight, int textHeight) {
        int mSettingArea = allHeight - textHeight * 2;
//        LeoLog.d("LineView", "mSettingArea is : " + mSettingArea);
        int partOne = mSettingArea / 4;
//        LeoLog.d("LineView", "partOne :" + partOne);
        for (int i = 0; i < 4; i++) {
            mColorheight[i] = partOne * (i+1);
        }

    }

    private int measureWidth(int measureSpec) {
        int horizontalGridNum = getHorizontalGridNum();
        int preferred = backgroundGridWidth * horizontalGridNum + sideLineLength * 2;
        return getMeasurement(measureSpec, preferred);
    }

    private int measureHeight(int measureSpec) {
        int preferred = 0;
        return getMeasurement(measureSpec, preferred);
    }

    private int getMeasurement(int measureSpec, int preferred) {
        int specSize = MeasureSpec.getSize(measureSpec);
        int measurement;
        switch (MeasureSpec.getMode(measureSpec)) {
            case MeasureSpec.EXACTLY:
                measurement = specSize;
                break;
            case MeasureSpec.AT_MOST:
                measurement = Math.min(preferred, specSize);
                break;
            default:
                measurement = preferred;
                break;
        }
        return measurement;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            pointToSelect = findPointAt((int) event.getX(), (int) event.getY());
        } else if (event.getAction() == MotionEvent.ACTION_UP) {
            if (pointToSelect != null) {
                selectedDot = pointToSelect;
                pointToSelect = null;
                postInvalidate();
            }
        }

        return true;
    }

    private Dot findPointAt(int x, int y) {
        if (drawDotLists.isEmpty()) {
            return null;
        }

        final int width = backgroundGridWidth / 2;
        final Region r = new Region();

        for (ArrayList<Dot> data : drawDotLists) {
            for (Dot dot : data) {
                final int pointX = dot.x;
                final int pointY = dot.y;

                r.set(pointX - width, pointY - width, pointX + width, pointY + width);
                if (r.contains(x, y)) {
                    return dot;
                }
            }
        }

        return null;
    }

    class Dot {
        int x;
        int y;
        int data;
        int targetX;
        int targetY;
        int linenumber;
        int velocity = ManagerFlowUtils.dip2px(getContext(), 18);

        Dot(int x, int y, int targetX, int targetY, Integer data, int linenumber) {
            this.x = x;
            this.y = y;
            this.linenumber = linenumber;
            setTargetData(targetX, targetY, data, linenumber);
        }

        Point setupPoint(Point point) {
            point.set(x, y);
            return point;
        }

        Dot setTargetData(int targetX, int targetY, Integer data, int linenumber) {
            this.targetX = targetX;
            this.targetY = targetY;
            this.data = data;
            this.linenumber = linenumber;
            return this;
        }

        boolean isAtRest() {
            return (x == targetX) && (y == targetY);
        }

        void update() {
            x = updateSelf(x, targetX, velocity);
            y = updateSelf(y, targetY, velocity);
        }

        private int updateSelf(int origin, int target, int velocity) {
            if (origin < target) {
                origin += velocity;
            } else if (origin > target) {
                origin -= velocity;
            }
            if (Math.abs(target - origin) < velocity) {
                origin = target;
            }
            return origin;
        }
    }
    
    @Override
    public void onRestoreInstanceState(Parcelable state) {
        try {
            super.onRestoreInstanceState(state);
        } catch (Exception e) {
            
        }
    }

}
