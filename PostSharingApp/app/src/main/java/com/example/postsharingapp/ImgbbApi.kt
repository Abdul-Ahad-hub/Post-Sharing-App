import com.example.postsharingapp.ImgbbResponse
import retrofit2.Call
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part
import retrofit2.http.Query

interface ImgBBApiDirect {
    @FormUrlEncoded
    @POST("upload")
    fun uploadImage(
        @Query("key") apiKey: String,
        @Field("image") base64Image: String
    ): Call<ImgbbResponse>
}
