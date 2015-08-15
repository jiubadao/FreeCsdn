package com.free.csdn.activity;

import java.util.List;

import com.free.csdn.R;
import com.free.csdn.adapter.BlogListAdapter;
import com.free.csdn.base.BaseActivity;
import com.free.csdn.bean.BlogItem;
import com.free.csdn.config.AppConstants;
import com.free.csdn.db.BlogCollectDao;
import com.free.csdn.db.impl.BlogCollectDaoImpl;
import com.free.csdn.util.DateUtil;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import me.maxwin.view.IXListViewLoadMore;
import me.maxwin.view.IXListViewRefreshListener;
import me.maxwin.view.XListView;

/**
 * 博客收藏列表
 * 
 * @author tangqi
 * @data 2015年7月8日下午9:20:20
 *
 */
public class BlogCollectActivity extends BaseActivity
		implements OnItemClickListener, OnClickListener, IXListViewRefreshListener, IXListViewLoadMore {

	private XListView mListView;
	private BlogListAdapter mAdapter;// 列表适配器
	private ImageView mReLoadImageView; // 重新加载的图片
	private ProgressBar mPbLoading;

	private TextView tvTitle;
	private int page = 1;
	private int pageSize = 20;
	private BlogCollectDao db;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_bloglist);

		initData();
		initView();
	}

	private void initData() {
		db = new BlogCollectDaoImpl(this);
	}

	private void initView() {
		mListView = (XListView) findViewById(R.id.listView_blog);
		mPbLoading = (ProgressBar) findViewById(R.id.pb_loading);
		tvTitle = (TextView) findViewById(R.id.tvTitle);
		ImageView mBackBtn = (ImageView) findViewById(R.id.backBtn);
		mBackBtn.setVisibility(View.VISIBLE);
		mBackBtn.setOnClickListener(this);

		tvTitle.setText("博客收藏");
		mReLoadImageView = (ImageView) findViewById(R.id.reLoadImage);
		mReLoadImageView.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View view) {
				mReLoadImageView.setVisibility(View.INVISIBLE);
				mPbLoading.setVisibility(View.VISIBLE);

				refresh();
			}
		});

		initListView();
	}

	/**
	 * 初始化ListView
	 */
	private void initListView() {
		mAdapter = new BlogListAdapter(this);
		mListView.setPullRefreshEnable(this);// 设置可下拉刷新
		mListView.NotRefreshAtBegin();
		mListView.setAdapter(mAdapter);// 设置适配器
		// 设置列表项点击事件
		mListView.setOnItemClickListener(this);

		// 先预加载数据，再请求最新数据
		mHandler.sendEmptyMessage(AppConstants.MSG_PRELOAD_DATA);
	}

	@Override
	public void onClick(View view) {
		// TODO Auto-generated method stub
		switch (view.getId()) {
		case R.id.backBtn:
			finish();
			break;

		default:
			break;
		}
	}

	/**
	 * ListView点击事件
	 */
	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
		// TODO Auto-generated method stub
		// // 获得博客列表项
		BlogItem item = (BlogItem) mAdapter.getItem(position - 1);
		Intent i = new Intent();
		i.setClass(BlogCollectActivity.this, BlogContentActivity.class);
		i.putExtra("blogItem", item);
		startActivity(i);
		// 动画过渡
		overridePendingTransition(R.anim.push_left_in, R.anim.push_no);
		Log.e("position", "" + position);
	}

	@Override
	public void onLoadMore() {
		// TODO Auto-generated method stub
		page++;
		mHandler.sendEmptyMessage(AppConstants.MSG_PRELOAD_DATA);
	}

	@Override
	public void onRefresh() {
		// TODO Auto-generated method stub
		refresh();
	}

	private void refresh() {
		page = 1;
		mHandler.sendEmptyMessage(AppConstants.MSG_PRELOAD_DATA);
	}

	@SuppressLint("HandlerLeak")
	private Handler mHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			// TODO Auto-generated method stub
			switch (msg.what) {
			case AppConstants.MSG_PRELOAD_DATA:
				List<BlogItem> list = db.query(page, pageSize);
				if (list != null && list.size() != 0) {
					mAdapter.setList(list);
					mAdapter.notifyDataSetChanged();
					mListView.setPullLoadEnable(BlogCollectActivity.this);// 设置可上拉加载
					mListView.setRefreshTime(DateUtil.getDate());
					mListView.stopLoadMore();
					mListView.stopRefresh(DateUtil.getDate());
					
					mPbLoading.setVisibility(View.GONE);
				}

				break;

			default:
				break;
			}
			super.handleMessage(msg);
		}
	};

}
