package network.b.bnet.utils;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.concurrent.TimeUnit;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;


public class FileUpLoadUtil {

    private static OkHttpClient mUploadClient = new OkHttpClient.Builder()
            .connectTimeout(15, TimeUnit.SECONDS)
            .writeTimeout(15, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .build();

    /**
     *up files
     * @param actionUrl
     * @param paramsMap
     * @param callBack
     * @param <T>
     */
    public static <T>void upLoadFile(String actionUrl, HashMap<String, Object> paramsMap, final ReqCallBack callBack) {
        try {
            //repair address
            //PLString requestUrl = PLString.format("%s/%s", upload_head, actionUrl);
            String requestUrl = actionUrl;

            MultipartBody.Builder builder = new MultipartBody.Builder();
            //set type
            builder.setType(MultipartBody.FORM);
            //Additional parameters
            for (String key : paramsMap.keySet()) {
                Object object = paramsMap.get(key);
                if (!(object instanceof File)) {
                    builder.addFormDataPart(key, object.toString());
                } else {
                    File file = (File) object;
                    RequestBody fileBody = RequestBody.create(MediaType.parse("application/octet-stream"), file);
                    builder.addFormDataPart(key, file.getName(), fileBody);
                }
            }
            //create RequestBody
            RequestBody body = builder.build();
            //create Request
            final Request request = new Request.Builder().url(requestUrl).post(body).build();
            //set timeout
            final Call call = mUploadClient.newBuilder().writeTimeout(50, TimeUnit.SECONDS).build().newCall(request);
            call.enqueue(new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    callBack.onFailure(call, e);
                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {

                    int responseCode = response.code();
                    byte[] responseBody = response.body().bytes();

                    if (response.isSuccessful()) {
                        callBack.onResponseSuccess(call, responseCode, responseBody);
                    } else {
                        callBack.onResponseFailure(call, responseCode, responseBody);
                    }
                }
            });
        } catch (Exception e) {
            //LogUtils.e(e.toString());
        }
    }

    public interface ReqCallBack{
        //on request fail
        void onFailure(Call call, IOException e);
        //on request success
        void onResponseSuccess(Call call, int responseCode, byte[] responseBody);
        void onResponseFailure(Call call, int responseCode, byte[] responseBody);
    }
}
