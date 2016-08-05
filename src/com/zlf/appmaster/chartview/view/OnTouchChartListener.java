package com.zlf.appmaster.chartview.view;

public interface OnTouchChartListener
{

  public abstract void onTouchDown(int i);

  public abstract void onTouchUp();

  public abstract void onMove(int i);

  public abstract void onChange();
}