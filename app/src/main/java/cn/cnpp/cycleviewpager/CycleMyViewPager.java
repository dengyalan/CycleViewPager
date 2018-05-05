package cn.cnpp.cycleviewpager;

import android.app.Fragment;
import android.os.Bundle;
import android.os.Message;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import java.util.ArrayList;
import java.util.List;

/**
 * @author dyl
 *         实现可循环，可轮播的viewpager
 */
public class CycleMyViewPager extends Fragment implements OnPageChangeListener {

    private List<View> viewList = new ArrayList<>();
    private ImageView[] indicators;
    private FrameLayout viewPagerFragmentLayout;
    /**
     * 指示器
     */
    private LinearLayout indicatorLayout;
    private BaseViewPager viewPager;
    private ViewPagerAdapter adapter;
    private CycleViewPagerHandler handler;
    /**
     * 默认轮播时间
     */
    private int time = 5000;
    /**
     * 轮播当前位置
     */
    private int currentPosition = 0;
    /**
     * 滚动框是否滚动着
     */
    private boolean isScrolling = false;
    /**
     * 是否循环
     */
    private boolean isCycle = false;
    /**
     * 是否轮播
     */
    private boolean isWheel = false;
    /**
     * 手指松开、页面不滚动时间，防止手机松开后短时间进行切换
     */
    private long releaseTime = 0;
    /**
     * 转动
     */
    private int WHEEL = 100;
    /**
     * 等待
     */
    private int WHEEL_WAIT = 101;

    private ItemCycleViewListener mImageCycleViewListener;
    private ItemScrollCycleViewListener itemScrollCycleViewListener;
    private int[] intInfos;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = LayoutInflater.from(getActivity()).inflate(R.layout.view_cycle_viewpager_contet, null);
        viewPager = view.findViewById(R.id.viewPager);
        indicatorLayout = view.findViewById(R.id.layout_viewpager_indicator);
        viewPagerFragmentLayout = view.findViewById(R.id.layout_viewager_content);

        handler = new CycleViewPagerHandler(getActivity()) {

            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);
                if (msg.what == WHEEL && viewList.size() != 0) {
                    if (!isScrolling) {
                        int max = viewList.size() + 1;
                        int position = (currentPosition + 1) % viewList.size();
                        viewPager.setCurrentItem(position, true);
                        // 最后一页时回到第一页
                        if (position == max) {
                            viewPager.setCurrentItem(1, false);
                        }
                    }
                    releaseTime = System.currentTimeMillis();
                    handler.removeCallbacks(runnable);
                    handler.postDelayed(runnable, time);
                    return;
                }
                if (msg.what == WHEEL_WAIT && viewList.size() != 0) {
                    handler.removeCallbacks(runnable);
                    handler.postDelayed(runnable, time);
                }
            }
        };

        return view;
    }

    public void setCycleViewData(List<View> views, int[] list, ItemCycleViewListener listener, ItemScrollCycleViewListener scrollListener) {
        setData(views, list, listener, 0, scrollListener);
    }

    /**
     * 初始化viewpager
     *
     * @param views        要显示的views
     * @param showPosition 默认显示位置
     */
    public void setData(List<View> views, int[] list, ItemCycleViewListener listener, int showPosition, ItemScrollCycleViewListener scrollListener) {
        mImageCycleViewListener = listener;
        itemScrollCycleViewListener = scrollListener;
        intInfos = list;
        this.viewList.clear();
        if (views.size() == 0) {
            viewPagerFragmentLayout.setVisibility(View.GONE);
            return;
        }
        for (View item : views) {
            this.viewList.add(item);
        }

        int ivSize = views.size();
        // 设置指示器
        indicators = new ImageView[ivSize - 2];
        indicatorLayout.removeAllViews();
        for (int i = 0; i < indicators.length; i++) {
            View view = LayoutInflater.from(getActivity()).inflate(R.layout.view_cycle_viewpager_indicator, null);
            indicators[i] = view.findViewById(R.id.image_indicator);
            indicatorLayout.addView(view);
        }
        adapter = new ViewPagerAdapter();
        // 默认指向第一项，下方viewPager.setCurrentItem将触发重新计算指示器指向
        setIndicator(0);
        viewPager.setOffscreenPageLimit(3);
        viewPager.setOnPageChangeListener(this);
        viewPager.setAdapter(adapter);
        if (showPosition < 0 || showPosition >= views.size()) {
            showPosition = 0;
        }
        if (isCycle) {
            showPosition = showPosition + 1;
        }
        viewPager.setCurrentItem(showPosition);
    }

    /**
     * 设置指示器居中，默认指示器在右方
     */
    public void setIndicatorCenter() {
        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
        params.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
        params.addRule(RelativeLayout.CENTER_HORIZONTAL);
        indicatorLayout.setLayoutParams(params);
    }

    /**
     * 是否循环，默认不开启，开启前，请将views的最前面与最后面各加入一个视图，用于循环
     *
     * @param isCycle 是否循环
     */
    public void setCycle(boolean isCycle) {
        this.isCycle = isCycle;
    }

    /**
     * 设置是否轮播，默认不轮播,轮播一定是循环的
     *
     * @param isWheel
     */
    public void setWheel(boolean isWheel) {
        this.isWheel = isWheel;
        isCycle = true;
        if (isWheel) {
            handler.postDelayed(runnable, time);
        }
    }

    final Runnable runnable = new Runnable() {

        @Override
        public void run() {
            if (getActivity() != null && !getActivity().isFinishing() && isWheel) {
                long now = System.currentTimeMillis();
                // 检测上一次滑动时间与本次之间是否有触击(手滑动)操作，有的话等待下次轮播
                if (now - releaseTime > time - 500) {
                    handler.sendEmptyMessage(WHEEL);
                } else {
                    handler.sendEmptyMessage(WHEEL_WAIT);
                }
            }
        }
    };

    /**
     * 设置轮播暂停时间，即没多少秒切换到下一张视图.默认5000ms
     *
     * @param time 毫秒为单位
     */
    public void setTime(int time) {
        this.time = time;
    }


    /**
     * 设置viewpager是否可以滚动
     *
     * @param enable
     */
    public void setScrollable(boolean enable) {
        viewPager.setScrollable(enable);
    }

    /**
     * 刷新数据，当外部视图更新后，通知刷新数据
     */
    public void refreshData() {
        if (adapter != null) {
            adapter.notifyDataSetChanged();
        }
    }

    /**
     * 页面适配器 返回对应的view
     *
     * @author Yuedong Li
     */
    private class ViewPagerAdapter extends PagerAdapter {

        @Override
        public int getCount() {
            return viewList.size();
        }

        @Override
        public boolean isViewFromObject(View arg0, Object arg1) {
            return arg0 == arg1;
        }

        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {
            container.removeView((View) object);
        }

        @Override
        public View instantiateItem(ViewGroup container, final int position) {
            View v = viewList.get(position);
            if (mImageCycleViewListener != null) {
                v.setOnClickListener(new OnClickListener() {

                    @Override
                    public void onClick(View v) {
                        mImageCycleViewListener.onItemClick(intInfos[currentPosition - 1], currentPosition, v);
                    }
                });
            }
            container.addView(v);
            return v;
        }

        @Override
        public int getItemPosition(Object object) {
            return POSITION_NONE;
        }
    }

    @Override
    public void onPageScrollStateChanged(int state) {
        // viewPager滚动状态
        if (state == 1) {
            isScrolling = true;
            return;
        } else if (state == 0) {
            releaseTime = System.currentTimeMillis();
            viewPager.setCurrentItem(currentPosition, false);

        }
        isScrolling = false;
    }

    @Override
    public void onPageScrolled(int position, float offset, int offsetPixels) {
    }

    @Override
    public void onPageSelected(int pos) {
        int max = viewList.size() - 1;
        int position = pos;
        currentPosition = pos;
        if (isCycle) {
            if (pos == 0) {
                currentPosition = max - 1;
            } else if (pos == max) {
                currentPosition = 1;
            }
            position = currentPosition - 1;
        }
        setIndicator(position);
        itemScrollCycleViewListener.onScrollListener(position + 1);
    }

    /**
     * 设置指示器
     *
     * @param selectedPosition 默认指示器位置
     */
    private void setIndicator(int selectedPosition) {
        for (int i = 0; i < indicators.length; i++) {
            indicators[i].setBackgroundResource(R.mipmap.dot_grey);
        }
        if (indicators.length > selectedPosition) {
            indicators[selectedPosition].setBackgroundResource(R.mipmap.dot_yellow);
        }
    }

    /**
     * 轮播控件的监听事件
     *
     * @author minking
     */
    public interface ItemCycleViewListener {

        /**
         * 单击图片事件
         *
         * @param info
         * @param position
         * @param imageView
         */
        void onItemClick(int info, int position, View imageView);
    }

    /**
     * 轮播控件的滚动监听事件
     *
     * @author minking
     */
    public interface ItemScrollCycleViewListener {

        /**
         * 单击图片事件
         *
         * @param position
         */
        void onScrollListener(int position);
    }
}