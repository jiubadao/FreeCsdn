package com.free.csdn.activity;

import java.util.Iterator;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Element;

import com.free.csdn.R;
import com.free.csdn.base.BaseActivity;
import com.free.csdn.bean.BlogHtml;
import com.free.csdn.bean.BlogItem;
import com.free.csdn.db.BlogCollectDao;
import com.free.csdn.db.BlogContentDao;
import com.free.csdn.db.impl.BlogCollectDaoImpl;
import com.free.csdn.db.impl.BlogContentDaoImpl;
import com.free.csdn.task.HttpAsyncTask;
import com.free.csdn.task.OnResponseListener;
import com.free.csdn.util.JsoupUtil;
import com.free.csdn.util.ToastUtil;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.ToggleButton;

/**
 * 博客详细内容界面
 * 
 * @author tangqi
 * @data 2015年7月20日下午9:20:20
 *
 */
@SuppressLint("SetJavaScriptEnabled")
public class BlogContentActivity extends BaseActivity implements OnResponseListener,
		OnClickListener, OnCheckedChangeListener {
	private WebView mWebView = null;
	private ProgressBar mProgressBar; // 进度条
	private ImageView mReLoadImageView; // 重新加载的图片
	private ImageView mBackBtn; // 回退按钮
	private ImageView mCommemtBtn;
	private ImageView mShareBtn;
	private ImageView mMoreBtn;
	private ToggleButton mCollectBtn;

	private BlogCollectDao db;
	private BlogItem blogItem;
	public String mTitle;
	private String mUrl;
	private String mFileName;
	private static final int MSG_RELOAD_DATA = 1000;
	private boolean isFirstCheck = false;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		requestWindowFeature(Window.FEATURE_NO_TITLE);// 无标题
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_article_detail);

		init();
		initComponent();

		// 预加载数据
		mHandler.sendEmptyMessage(MSG_RELOAD_DATA);
	}

	// 初始化
	private void init() {
		db = new BlogCollectDaoImpl(this);

		blogItem = (BlogItem) getIntent().getSerializableExtra("blogItem");
		if (blogItem != null) {
			mUrl = blogItem.getLink();
			mTitle = blogItem.getTitle();
			mFileName = mUrl.substring(mUrl.lastIndexOf("/") + 1);
		}
	}

	// 初始化组件
	private void initComponent() {
		TextView mTitleView = (TextView) findViewById(R.id.tv_title);
		mTitleView.setText(R.string.blog_detail);

		mProgressBar = (ProgressBar) findViewById(R.id.blogContentPro);
		mReLoadImageView = (ImageView) findViewById(R.id.reLoadImage);
		mBackBtn = (ImageView) findViewById(R.id.btn_back);
		mBackBtn.setVisibility(View.VISIBLE);
		mCommemtBtn = (ImageView) findViewById(R.id.iv_comment);
		mShareBtn = (ImageView) findViewById(R.id.iv_share);
		mMoreBtn = (ImageView) findViewById(R.id.iv_more);
		mCollectBtn = (ToggleButton) findViewById(R.id.tb_collect);

		mReLoadImageView.setOnClickListener(this);
		mBackBtn.setOnClickListener(this);
		mCommemtBtn.setOnClickListener(this);
		mShareBtn.setOnClickListener(this);
		mMoreBtn.setOnClickListener(this);
		mCollectBtn.setOnCheckedChangeListener(this);
		if (isCollect()) {
			isFirstCheck = true;
			mCollectBtn.setChecked(true);
		}

		initWebView();
	}

	private void initWebView() {
		mWebView = (WebView) findViewById(R.id.webview);
		mWebView.setWebViewClient(new MyWebViewClient());
		mWebView.getSettings().setJavaScriptEnabled(true);
		mWebView.getSettings().setDefaultTextEncodingName("utf-8");
		mWebView.getSettings().setAppCacheEnabled(true);
		mWebView.getSettings().setDatabaseEnabled(true);

		// LOAD_CACHE_ELSE_NETWORK，只要本地有，无论是否过期，或者no-cache，都使用缓存中的数据。
		// LOAD_DEFAULT: 根据cache-control决定是否从网络上取数据。
		// 总结：根据以上两种模式，建议缓存策略为，判断是否有网络，有的话，使用LOAD_DEFAULT，无网络时，使用LOAD_CACHE_ELSE_NETWORK。
		mWebView.getSettings().setCacheMode(WebSettings.LOAD_CACHE_ELSE_NETWORK);
	}

	@Override
	public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
		// TODO Auto-generated method stub

		// 屏蔽第一次也会进来的问题
		if (isFirstCheck) {
			isFirstCheck = false;
			return;
		}

		if (isChecked) {
			ToastUtil.show(this, "收藏成功");
			blogItem.setUpdateTime(System.currentTimeMillis());
			db.insert(blogItem);
		} else {
			ToastUtil.show(this, "取消收藏");
			db.delete(blogItem);
		}
	}

	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		switch (v.getId()) {
		case R.id.btn_back:
			finish();
			break;

		case R.id.reLoadImage:
			reload();
			break;

		case R.id.iv_comment:
		case R.id.comment:
			comment();
			break;

		case R.id.iv_share:
			share();
			break;

		case R.id.iv_more:
			more();
			break;

		default:
			break;
		}
	}

	/**
	 * 重新加载
	 */
	private void reload() {
		mReLoadImageView.setVisibility(View.INVISIBLE);
		mProgressBar.setVisibility(View.VISIBLE);
		requestData(mUrl);
	}

	/**
	 * 判断是否收藏
	 * 
	 * @return
	 */
	private boolean isCollect() {
		if (null != db.query(blogItem.getLink())) {
			return true;
		}
		return false;
	}

	/**
	 * 评论
	 */
	private void comment() {
		Intent i = new Intent();
		i.setClass(BlogContentActivity.this, BlogCommentActivity.class);
		i.putExtra("filename", mFileName);
		startActivity(i);
		overridePendingTransition(R.anim.push_left_in, R.anim.push_no);
	}

	/**
	 * 分享
	 */
	private void share() {
		// TODO Auto-generated method stub
		Intent intent = new Intent(Intent.ACTION_SEND);
		intent.setType("text/plain");
		intent.putExtra(Intent.EXTRA_TEXT, mTitle + "：" + "\n" + mUrl);
		startActivity(Intent.createChooser(intent, "CSDN博客分享"));
	}

	// 更多
	private void more() {

	}

	/**
	 * 处理WebView返回
	 * 
	 * @param keyCode
	 * @param event
	 * @return
	 */
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		// if (keyCode == KeyEvent.KEYCODE_BACK && webView.canGoBack()) {
		// webView.goBack();// WEBVIEW返回前一个页面
		// return true;
		// }
		return super.onKeyDown(keyCode, event);
	}

	/**
	 * 加载数据
	 */
	private void requestData(String url) {
		// TODO Auto-generated method stub
		mProgressBar.setVisibility(View.VISIBLE);
		HttpAsyncTask httpAsyncTask = new HttpAsyncTask(this);
		httpAsyncTask.execute(url);
		httpAsyncTask.setOnResponseListener(this);
	}

	/**
	 * 数据请求完成
	 */
	@Override
	public void onResponse(String resultString) {
		// TODO Auto-generated method stub
		String html = adjustPicSize(JsoupUtil.getContent(resultString));
		loadHtml(html);
		saveDb(html);
	}

	/**
	 * 加载页面
	 * 
	 * @param resultString
	 */
	private void loadHtml(String html) {
		if (!TextUtils.isEmpty(html)) {
			mWebView.loadDataWithBaseURL("http://blog.csdn.net", html, "text/html", "utf-8", null);
			mReLoadImageView.setVisibility(View.GONE);
		} else {
			mProgressBar.setVisibility(View.GONE);
			mReLoadImageView.setVisibility(View.VISIBLE);
			ToastUtil.show(this, "网络已断开");
		}
	}

	/**
	 * 保存数据库
	 * 
	 * @param html
	 */
	private void saveDb(String html) {
		if (TextUtils.isEmpty(html)) {
			return;
		}
		BlogHtml blogHtml = new BlogHtml();
		blogHtml.setUrl(mUrl);
		blogHtml.setHtml(html);
		blogHtml.setReserve("");

		BlogContentDao blogContentDb = new BlogContentDaoImpl(this, mUrl);
		blogContentDb.insert(blogHtml);
	}

	/**
	 * 适应 页面
	 * 
	 * @param paramString
	 * @return
	 */
	private String adjustPicSize(String paramString) {
		if (TextUtils.isEmpty(paramString)) {
			return null;
		}
		Element localElement = Jsoup.parse(paramString).getElementsByClass("details").get(0);
		Iterator<?> localIterator = localElement.getElementsByTag("img").iterator();
		while (true) {
			if (!localIterator.hasNext())
				return "<script type=\"text/javascript\" src=\"file:///android_asset/shCore.js\"></script><script type=\"text/javascript\" src=\"file:///android_asset/shBrushCpp.js\"></script><script type=\"text/javascript\" src=\"file:///android_asset/shBrushXml.js\"></script><script type=\"text/javascript\" src=\"file:///android_asset/shBrushJScript.js\"></script><script type=\"text/javascript\" src=\"file:///android_asset/shBrushJava.js\"></script><link rel=\"stylesheet\" type=\"text/css\" href=\"file:///android_asset/shThemeDefault.css\"><link rel=\"stylesheet\" type=\"text/css\" href=\"file:///android_asset/shCore.css\"><script type=\"text/javascript\">SyntaxHighlighter.all();</script>"
						+ localElement.toString();
			((Element) localIterator.next()).attr("width", "100%");
		}
	}

	private void getData(String url) {
		BlogContentDao blogContentDb = new BlogContentDaoImpl(this, url);
		BlogHtml blogHtml = blogContentDb.query(url);
		if (blogHtml != null) {
			loadHtml(blogHtml.getHtml());
		} else {
			requestData(url);
		}
	}

	// 预加载数据
	@SuppressLint("HandlerLeak")
	private Handler mHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			// TODO Auto-generated method stub
			switch (msg.what) {
			case MSG_RELOAD_DATA:
				getData(mUrl);
				break;

			default:
				break;
			}
			super.handleMessage(msg);
		}
	};

	/**
	 * 自定义
	 * 
	 * @author Administrator
	 *
	 */
	class MyWebViewClient extends WebViewClient {

		MyWebViewClient() {

		}

		@Override
		public void onPageStarted(WebView view, String url, Bitmap favicon) {
			// TODO Auto-generated method stub
			super.onPageStarted(view, url, favicon);
		}

		public void onPageFinished(WebView paramWebView, String paramString) {
			mWebView.getSettings().setBlockNetworkImage(false);
			mProgressBar.setVisibility(View.GONE);
			super.onPageFinished(paramWebView, paramString);
		}

		public void onReceivedError(WebView paramWebView, int paramInt, String paramString1,
				String paramString2) {
			super.onReceivedError(paramWebView, paramInt, paramString1, paramString2);
		}

		public boolean shouldOverrideUrlLoading(WebView paramWebView, String paramString) {
			// int i = 1;
			// String str = "brian512";
			// Log.i("CSDNBlog_BlogContentActivity", "url=" + paramString);
			// if
			// (paramString.matches("http://blog.csdn.net/(\\w+)/article/details/(\\d+)"))
			// BlogContentActivity.this.mQueue.add(new
			// StringRequest(paramString, BlogContentActivity.this, null));
			// while (true)
			// {
			// label61: return i;
			// if (paramString.matches("http://blog.csdn.net/(\\w+)[/]{0,1}"));
			// for (Pattern localPattern =
			// Pattern.compile("http://blog.csdn.net/(\\w+)[/]{0,1}"); ;
			// localPattern =
			// Pattern.compile("http://blog.csdn.net/(\\w+)/article/\\w+/(\\d+)"))
			// {
			// Matcher localMatcher = localPattern.matcher(paramString);
			// if (localMatcher.find())
			// str = localMatcher.group(i);
			// Intent localIntent = new Intent();
			// localIntent.setClass(BlogContentActivity.this,
			// MainTabActivity.class);
			// localIntent.putExtra(BlogContentActivity.this.getString(2131296299),
			// str);
			// BlogContentActivity.this.startActivity(localIntent);
			// BlogContentActivity.this.finish();
			// break label61:
			// if
			// ((!paramString.matches("http://blog.csdn.net/(\\w+)/article/category/(\\d+)"))
			// &&
			// (!paramString.matches("http://blog.csdn.net/(\\w+)/article/list/(\\d+)")))
			// break;
			// }
			// i = 0;
			// }
			//
			if ((paramString.matches("http://blog.csdn.net/(\\w+)/article/details/(\\d+)"))) {
				mUrl = paramString;
				getData(paramString);
				return false;
			}
			return true;
		}
	}

}
