package com.el.ytrepo

import com.el.ytrepo.data.BrowseRequestBody
import com.el.ytrepo.data.MusicRequestBody
import com.el.ytrepo.data.PlayListRequestBody
import com.el.ytrepo.data.SearchRequestBody
import com.el.ytrepo.data.SearchSugRequestBody
import com.fasterxml.jackson.databind.JsonNode
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.Headers
import retrofit2.http.POST

interface YTInterface {
    @POST("next?key=AIzaSyA8eiZmM1FaDVjRy-df2KTyQ_vz_yYM39w")
    suspend fun next(@Body body: PlayListRequestBody) : Response<JsonNode>

    @POST("next?key=AIzaSyA8eiZmM1FaDVjRy-df2KTyQ_vz_yYM39w")
    suspend fun next(@Body body: String) : Response<String>

    @POST("player?key=AIzaSyAOghZGza2MQSZkY_zfZ370N-PUdXEo8AI")
    @Headers("Content-Type: application/json")
    suspend fun player(@Body body: MusicRequestBody) : Response<JsonNode>

    @POST("player?key=AIzaSyAOghZGza2MQSZkY_zfZ370N-PUdXEo8AI")
    @Headers("Content-Type: application/json")
    suspend fun player(@Body body: String) : Response<String>

    @POST("browse?key=AIzaSyA8eiZmM1FaDVjRy-df2KTyQ_vz_yYM39w")
    suspend fun browse(@Body body: BrowseRequestBody): Response<JsonNode>

    @POST("browse?key=AIzaSyA8eiZmM1FaDVjRy-df2KTyQ_vz_yYM39w")
    suspend fun browse(@Body body: String): Response<String>

    @POST("search?key=AIzaSyA8eiZmM1FaDVjRy-df2KTyQ_vz_yYM39w")
    suspend fun search(@Body body: SearchRequestBody): Response<JsonNode>

    @POST("search?key=AIzaSyA8eiZmM1FaDVjRy-df2KTyQ_vz_yYM39w")
    suspend fun search(@Body body: String): Response<String>

    @POST("music/get_search_suggestions?key=AIzaSyA8eiZmM1FaDVjRy-df2KTyQ_vz_yYM39w")
    suspend fun searchSug(@Body body: SearchSugRequestBody): Response<JsonNode>

    @POST("music/get_search_suggestions?key=AIzaSyA8eiZmM1FaDVjRy-df2KTyQ_vz_yYM39w")
    suspend fun searchSug(@Body body: String): Response<String>
}