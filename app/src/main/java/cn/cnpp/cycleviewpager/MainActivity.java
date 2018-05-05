package cn.cnpp.cycleviewpager;

import android.content.Context;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private CycleMyViewPager cycleViewPager;
    private List<View> viewArrayList = new ArrayList<>();

    private CycleImageViewPager cycleImageViewPager;
    private List<ImageView> imageViewList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initMyViewPager();
        initImageViewPager();
    }

    private void initMyViewPager() {
        cycleViewPager = (CycleMyViewPager) getFragmentManager().findFragmentById(R.id.fragment_cycle_viewpager);
        viewArrayList.clear();
        //测试所用数据源
        int[] imgs = {R.mipmap.cate_test1, R.mipmap.cate_test2, R.mipmap.cate_test3, R.mipmap.cate_test4, R.mipmap.cate_test5, R.mipmap.cate_test6,
                R.mipmap.cate_test7, R.mipmap.cate_test8};
        viewArrayList.add(cycleViewItem(imgs[imgs.length - 1]));
        for (int i = 0; i < imgs.length; i++) {
            viewArrayList.add(cycleViewItem(imgs[i]));
        }
        // 将第一个ImageView添加进来
        viewArrayList.add(cycleViewItem(imgs[0]));
        //循环
        cycleViewPager.setCycle(true);
        //设置指示器居中显示
        cycleViewPager.setIndicatorCenter();
        //开启轮播
        cycleViewPager.setWheel(true);
        //设置可滑动
        cycleViewPager.setScrollable(true);
        cycleViewPager.setCycleViewData(viewArrayList, imgs, mAdCycleViewListener, mScrollListener);
    }

    private View cycleViewItem(int date) {
        View view = LayoutInflater.from(this).inflate(R.layout.view_cycleview_item, null);
        ImageView iv = view.findViewById(R.id.img_address);
        TextView tv = view.findViewById(R.id.tv_name);
        iv.setImageResource(date);
        tv.setText("cnpp" + date);
        return view;
    }

    private CycleMyViewPager.ItemCycleViewListener mAdCycleViewListener = new CycleMyViewPager.ItemCycleViewListener() {

        @Override
        public void onItemClick(int info, int position, View imageView) {
            showToastShort(MainActivity.this, "您点击了" + position);
        }
    };

    private CycleMyViewPager.ItemScrollCycleViewListener mScrollListener = new CycleMyViewPager.ItemScrollCycleViewListener() {

        @Override
        public void onScrollListener(int position) {
            showToastShort(MainActivity.this, "您滑动到了" + position);
        }
    };

    private void initImageViewPager() {
        cycleImageViewPager = (CycleImageViewPager) getFragmentManager().findFragmentById(R.id.fragment_image_viewpager);
        // 将最后一个ImageView添加进来
        imageViewList.clear();
        int[] imgs = {R.mipmap.ad_img1, R.mipmap.ad_img2, R.mipmap.ad_img3};
        imageViewList.add(setImageResource(imgs[imgs.length - 1]));
        for (int i = 0; i < imgs.length; i++) {
            imageViewList.add(setImageResource(imgs[i]));
        }
        // 将第一个ImageView添加进来
        imageViewList.add(setImageResource(imgs[0]));
        // 设置循环，在调用setData方法前调用
        cycleImageViewPager.setCycle(true);
        // 在加载数据前设置是否循环
        cycleImageViewPager.setIntData(imageViewList, imgs, mCycleViewListener);
        // 设置轮播
        cycleImageViewPager.setWheel(true);
        // 设置轮播时间，默认5000ms
        cycleImageViewPager.setTime(3000);
        // 设置圆点指示图标组居中显示，默认靠右
        cycleImageViewPager.setIndicatorCenter();
    }

    private CycleImageViewPager.ImageCycleViewListener mCycleViewListener = new CycleImageViewPager.ImageCycleViewListener() {

        @Override
        public void onImageClick(int info, int position, View imageView) {
            if (cycleImageViewPager.isCycle()) {
            }
        }
    };

    private ImageView setImageResource(int img) {
        ImageView imageView = (ImageView) LayoutInflater.from(this).inflate(R.layout.view_imageview, null);
        imageView.setImageResource(img);
        return imageView;
    }

    private void showToastShort(Context context, String str) {
        Toast toast = Toast.makeText(context, str, Toast.LENGTH_SHORT);
        toast.setGravity(Gravity.CENTER, 0, 0);
        toast.show();
    }
}
