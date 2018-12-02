package com.zj.myapplication;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.util.AttributeSet;
import android.util.Log;
import android.util.TypedValue;
import android.view.View;

/**
 * Created by Administrator on 2018/11/29 0029.
 */

public class LotteryWheelView extends View {

    private String TAG = "LotteryWheelView";

    /**
     * 绘制扇形区域画笔
     */
    private Paint mArcPaint;

    /**
     * 绘制文本画笔
     */
    private Paint mTextPaint;

    /**
     * 盘块的数量
     */
    private int mItems = 6;

    /**
      * 扇形分区的颜色
     */
    private int[] colors = new int[]{R.color.red, R.color.gray, R.color.beige, R.color.red, R.color.gray, R.color.beige};
    /**
     * 扇形区域文字描述
     */
    private String mArcTexts[] = new String[]{"单反相机","ipad","手机","50QB","20QB","谢谢参与"};//静态初始
    /**
     *扇形区域对应图片
     */
    private int mArcImgs[] = new int[]{R.drawable.danfan,R.drawable.ipad,R.drawable.iphone,R.drawable.meizi,R.drawable.f015,R.drawable.f040};

    /**
     * 图片对应的bitmap
     */
    private Bitmap[] mImgBitmap;
    /**
     * 圆盘的大小范围
     */
    private RectF mRange = new RectF();
    /**
     * 圆盘的直径
     */
    private int mRadius;

    /**
     * 圆盘滚动的速度
     */
    private double mSpeed = 50;

    /**
     * 绘制的起始角度
     */
    private double mStartAngle;
    /**
     * 是否点击了结束按钮
     */
    private boolean start = false;
    /**
     * 转盘的中心位置
     */
    private int mCenter;

    /**
     * 圆盘pading值
     */
    private int mPadding;

    /**
     * 圆盘背景图片
     */
    private Bitmap mBgBitmap = BitmapFactory.decodeResource(getResources(),R.drawable.bg2);

    /**
     * 文字的大小
     */
    private float mTextSize = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP,15,getResources().getDisplayMetrics());//单位 值 DisplayMetrics(获取屏幕宽高类)


    @SuppressLint("HandlerLeak")
    private Handler mHandler = new Handler();

    public LotteryWheelView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public LotteryWheelView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        //初始 绘制扇形区域画笔
        mArcPaint = new Paint();
        mArcPaint.setAntiAlias(true);//设置抗锯齿
        mArcPaint.setDither(true);//设置防震动

        //初始文本画笔
        mTextPaint = new Paint();
        mTextPaint.setAntiAlias(true);
        mTextPaint.setDither(true);
        mTextPaint.setColor(Color.WHITE);
        mTextPaint.setTextSize(mTextSize);
        //动态初始
        mImgBitmap = new Bitmap[mItems];
        for(int i=0;i<mImgBitmap.length;i++){
            mImgBitmap[i] = BitmapFactory.decodeResource(getResources(),mArcImgs[i]);
        }
    }

    /**
     *不管静态加入控件还是动态加入控件---View回调顺序---onMeasure, onLayout, onDraw 的执行顺序
     * MODE分为以下三类
     MeasureSpec.UNSPECIFIED  父元素没给子元素施加任何束缚，子元素可以得到任意想要的大小(可以超过父类)
     MeasureSpec.EXACTLY      父元素决定子元素的确切大小(最多等于父类)  (具体值或设置的Match)
     MeasureSpec.AT_MOST      自适应子view自己决定(类似 warp)
     onAttach, onMeasure, onLayout, onDraw  执行顺序
     */
    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        Log.v(TAG,"onMeasure");
        //获取宽高的最小值
        int width = Math.min(getMeasuredWidth(),getMeasuredHeight());//1080  1920
        Log.v(TAG,"width="+width);//1080-8*3*2(边距8*3*2 (两）) = 1032
        mPadding = getPaddingLeft();
        Log.v(TAG,"mPadding="+mPadding);//xmldp*3等于类中dp
        //内圆直径(扇形对应) 1032-90*2
        mRadius = width - mPadding * 2;
        Log.v(TAG,"mRadius="+mRadius);

        setMeasuredDimension(width, width);//调用此方法才能储存宽高(否则显示控件宽高为空显示不出控件)  宽高都为852
        //初始化圆盘的范围
        mRange = new RectF(mPadding, mPadding, mRadius + mPadding, mRadius + mPadding);
        //中心位置
        mCenter = getMeasuredWidth() / 2;
    }

    @SuppressLint("DrawAllocation")
    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        Log.v(TAG,"onDraw");
        //1.绘制背景---paint可填可不填--  mPadding边距(位置被挤压不是外扩 是内收)    其实点(15*3,15*3)--(1032-15*3,1032-15*3)     画图片
        canvas.drawBitmap(mBgBitmap, null, new RectF(mPadding / 2, mPadding / 2
                , getMeasuredWidth() - mPadding / 2, getMeasuredHeight() - mPadding / 2), null);

        //mRange圆弧所在的椭圆对象  startAngle圆弧的起始角度  sweepAngle圆弧的角度  useCenter true表示显示圆弧与圆心的半径连线（如逆时针 扇形显示下边那根线与圆弧）

        //2.绘制盘块
        int tempAngle = (int) mStartAngle;
        float sweepAngle = 360 / mItems;


//                //旋转绘制的图片
//                ArrayList<Bitmap> bitmaps = new ArrayList<>();
//                for (int j = 0; j < mArcImgs.length; j++) {
//                    //获取bitmap
//                    Bitmap bitmap = BitmapFactory.decodeResource(getResources(), mArcImgs[j]);
//                    int width = bitmap.getWidth();
//                    int height = bitmap.getHeight();
//                    //https://www.jianshu.com/p/4a911f048e5c 旋转，平移，缩放，扭曲的作用
//                    Matrix matrix = new Matrix();
//                    //设置缩放值
//                    matrix.postScale(1f, 1f);
//                    //旋转的角度
//                    matrix.postRotate(sweepAngle * j);
//                    //获取旋转后的bitmap
//                    Bitmap rotateBitmap = Bitmap.createBitmap(bitmap, 0, 0, width, height, matrix, true);
//                    //将旋转过的图片保存到列表中
//                    bitmaps.add(rotateBitmap);
//                }

        for (int i = 0; i < mItems; i++) {
            mArcPaint.setColor(ContextCompat.getColor(getContext(),colors[i]));
            //1画扇形--默认顺时针画
            canvas.drawArc(mRange,tempAngle,sweepAngle,true,mArcPaint);//第一块粉红色

            //2.绘制盘块的文字
            Path path = new Path();
            path.addArc(mRange,tempAngle,sweepAngle);
            //通过水平偏移量使得文字居中  水平偏移量=弧度/2 - 文字宽度/2  //πD=圆的面积
            float textWidth = mTextPaint.measureText(mArcTexts[i]);
            float hOffset = (float) (mRadius * Math.PI / mItems / 2- textWidth/2);//以当前方位  X Y 中 X 代表hOffset(水平偏移)， 正顺时针偏移   负逆时针偏移
            float vOffset = mRadius/2/6;//以当前方位  X Y 中Y代表vOffset(垂直偏移)， 正内偏移
            canvas.drawTextOnPath(mArcTexts[i], path, hOffset, vOffset, mTextPaint);//范围在mRange上 内圆上，tempAngle 0度时   文字左下为基准向下画(水平线)

            //3.绘制盘块上面的IMG
            //约束下图片的宽度
            int imgWidth = mRadius / 8;
            //获取弧度  https://blog.csdn.net/yljj930205/article/details/80192501
            float angle = (float) Math.toRadians(tempAngle + sweepAngle / 2);// 第一块 0.5弧度切线自己取(0度)   第二块位置,切线自己取(60度)
//            Log.v(TAG,"angle="+angle);//相机中心位置xy
            //将图片移动到圆弧中心位置
            float x = (float) (mCenter + mRadius / 2 / 2 * Math.cos(angle));//以上变垂直   mRadius / 2 / 2 * Math.cos(angle) 获取r/4的xy
            float y = (float) (mCenter + mRadius / 2 / 2 * Math.sin(angle));
//            Log.v(TAG,"x="+x);//相机中心位置xy
//            Log.v(TAG,"y="+y);
            //确认绘制的矩形---   X轴右正,Y轴下正
            RectF rectF = new RectF(x - imgWidth / 2, y - imgWidth / 2, x + imgWidth / 2, y + imgWidth / 2);
            Bitmap bitmap = BitmapFactory.decodeResource(getResources(), mArcImgs[i]);
            canvas.drawBitmap(bitmap, null, rectF, null);
            tempAngle+=sweepAngle;
        }


        if (start) {
            mStartAngle += mSpeed;//mStartAngle自加40 改变绘制起点实角度
            //16ms之后刷新界面
            mHandler.postDelayed(new MyRunnable(), 16);
            mSpeed -= 1;//mSpeed自减1  以下判断mSpeed减小能看见绘制角度相差很小(有速度变慢的视觉效果)
            if (mSpeed < 10) {
                mSpeed -= 0.5;
            }
            if (mSpeed < 3) {
                mSpeed -= 0.1;
            }
            if (mSpeed < 0) {
                mSpeed = 0;
                start = false;
            }
    }
    }

    public void start() {
        start = true;
        mSpeed = 40;
        invalidate();
    }

    private class MyRunnable implements Runnable {
        @Override
        public void run() {
            invalidate();//执行一次开下一个线程
        }
    }

}
