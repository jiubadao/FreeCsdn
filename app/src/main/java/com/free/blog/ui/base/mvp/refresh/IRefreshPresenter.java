package com.free.blog.ui.base.mvp.refresh;

import com.free.blog.ui.base.mvp.IBasePresenter;

/**
 * @author tangqi on 17-3-20.
 */
public interface IRefreshPresenter extends IBasePresenter {

    void loadRefreshData();

    void loadMoreData();
}
