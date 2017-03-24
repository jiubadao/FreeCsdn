package com.free.blog.model.remote;

import retrofit2.http.GET;
import retrofit2.http.Path;
import retrofit2.http.Query;
import retrofit2.http.Url;
import rx.Observable;

/**
 * @author tangqi on 17-3-14.
 */
interface IBlogApi {

    @GET("{userId}")
    Observable<String> getBloggerInfo(@Path("userId") String userId);

    @GET("{userId}/article/list/{page}")
    Observable<String> getBlogList(@Path("userId") String userId, @Path("page") int page);

    @GET("{category}/{page}")
    Observable<String> getCategoryBlogList(@Path("category") String category, @Path("page") int page);

    @GET
    Observable<String> getHtml(@Url String url);

    @GET("me/comment/list/{blogId}")
    Observable<String> getBlogComment(@Path("blogId") String blogId, @Query("page") int page);

    @GET("column/list.html")
    Observable<String> getColumnList(@Query("q") String keywords, @Query("page") int page);

    @GET
    Observable<String> getHtmlByPage(@Url String url, @Query("page") int page);
}
