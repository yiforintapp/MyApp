package com.leo.appmaster.ui;


public class LineChartView  { // extends GraphicalView {
	
//	private String TAG = "LineChartView";
//	private LineChart chart = new LineChart();
//	
//	//标签集合
//	private LinkedList<String> labels = new LinkedList<String>();
//	private LinkedList<LineData> chartData = new LinkedList<LineData>();
//
//	public LineChartView(Context context) {
//		super(context);
//		// TODO Auto-generated constructor stub
//			chartLabels();
//			chartDataSet();	
//			chartRender();
//	}
//	
//	 public LineChartView(Context context, AttributeSet attrs){   
//	        super(context, attrs);   
//	        chartLabels();
//			chartDataSet();	
//			chartRender();
//	 }
//	 
//	 public LineChartView(Context context, AttributeSet attrs, int defStyle) {
//			super(context, attrs, defStyle);
//			chartLabels();
//			chartDataSet();	
//			chartRender();
//	 }		
//
//	private void chartRender()
//	{
//		try {				
//
//			//设定数据源
//			chart.setCategories(labels);								
//			chart.setDataSource(chartData);
//			
//			//数据轴最大值
//			chart.getDataAxis().setAxisMax(100);
//			//数据轴刻度间隔
//			chart.getDataAxis().setAxisSteps(10);
//			
//			chart.setLineAxisIntersectVisible(true);
//	         //背景网格
//            chart.getPlotGrid().showHorizontalLines();
//            chart.getPlotGrid().showVerticalLines();
//            chart.getPlotGrid().getHorizontalLinePaint().setColor(Color.rgb(238, 238, 238));
//            chart.getPlotGrid().getVerticalLinePaint().setColor(Color.rgb(238, 238, 238));
//			
//			chart.getDataAxis().setAxisLineVisible(false);
////			chart.getDataAxis().setVisible(false);
//			
////			chart.getCategoryAxis().getTickLabelPaint().setTextAlign(Align.CENTER);
////			chart.getCategoryAxis().setTickMarksVisible(false);
//			chart.getCategoryAxis().setVisible(false);
//			
//			chart.setRightAxisVisible(false);
//			chart.setTopAxisVisible(false);
//			
//			chart.getPlotLegend().hideLegend();			
//			
//			
//		} catch (Exception e) {
//			// TODO Auto-generated catch block
//		}
//	}
//	private void chartDataSet()
//	{
//		//Line 1
//		LinkedList<Double> dataSeries1= new LinkedList<Double>();	
//	    dataSeries1.add(0d); 
//		dataSeries1.add(10d); 
//		dataSeries1.add(44d); 
//		dataSeries1.add(31d); 
//		dataSeries1.add(70d);
//		dataSeries1.add(5d);
//		LineData lineData1 = new LineData("",dataSeries1,(int)Color.rgb(234, 83, 71));
//		lineData1.setLineGradient(true);
//		lineData1.setLabelVisible(true);		
//		lineData1.setDotStyle(XEnum.DotStyle.RING_DOT);
//		lineData1.setDotRadius(DipPixelUtil.dip2px(mContext, 8));
//		lineData1.getDotLabelPaint().setColor(Color.BLUE);
////		lineData1.getDotLabelPaint().setTextSize(DipPixelUtil.dip2px(mContext, 10));
////		lineData1.getDotLabelPaint().setTextAlign(Align.CENTER);
//		lineData1.setLineGradient(true);
//		lineData1.setStartPoitPos(1);
//		
//		chartData.add(lineData1);
//	}
//	
//	private void chartLabels()
//	{
//        labels.add("2009");
//        labels.add("2010");
//        labels.add("2011");
//        labels.add("2012");
//        labels.add("2013");
//        labels.add("2014");
//        labels.add("2015");
//        labels.add("2016");
//        labels.add("2017");
//	}
//	
//	@Override  
//    protected void onSizeChanged(int w, int h, int oldw, int oldh) {  
//        super.onSizeChanged(w, h, oldw, oldh);  
//       //图所占范围大小
//        chart.setChartRange(w,h);
//    }  
//	
//	@Override
//    public void render(Canvas canvas) {
//        try{
//        	        
//        	//设置图表大小
//	        chart.setChartRange(0,0,
//	                getWidth() - 10,
//	        		getHeight() - 10);
//	        //设置绘图区内边距	  	        
//            chart.render(canvas);
//        } catch (Exception e){
//        }
//    }
//
//	@Override
//	 public void onDraw(Canvas canvas){   				
//	        super.onDraw(canvas);  
//	        
//	 }

}
