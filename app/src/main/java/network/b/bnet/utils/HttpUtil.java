package network.b.bnet.utils;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.BufferedInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Type;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import network.b.bnet.base.BNetApplication;
import network.b.bnet.base.MyCallBackParam;
import network.b.bnet.config.Constants;
import network.b.bnet.model.BaseModel;
import network.b.bnet.model.BaseModelList;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.Headers;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;


/**
 * Created by jack.ma on 2018/06/4.
 */

public class HttpUtil {
    private Context mContext = null;
    private Handler mHandler;
    private static Object LOCK = new Object();
    private static HttpUtil mUtil;
    private static final int TIME_OUT = 5 * 5000;

    private HttpUtil() {
    }

    private HttpUtil(Context context) {
        mContext = context;
        mHandler = new Handler(Looper.getMainLooper());
    }

    public static HttpUtil getInstance(Context context) {
        if (mUtil == null) {
            synchronized (LOCK) {
                if (mUtil == null) {
                    mUtil = new HttpUtil(context);
                }
            }
        }
        return mUtil;
    }

    public static HttpUtil getInstance() {
        if (mUtil == null) {
            synchronized (LOCK) {
                if (mUtil == null) {
                    mUtil = new HttpUtil(BNetApplication.getInstance().getContext());
                }
            }
        }
        return mUtil;
    }

    /**
     * get method
     */
    public <T> void getBackBean(final @NonNull String url, final Map<String, String> params
            , final Class<T> bc, final MyCallBackParam absResponse) {
        String requestUrl = url;
//        String sign = paramsToFormBody(params);
        if (params != null && params.size() > 0) {
            Set set = params.entrySet();
            int i = 0;
            requestUrl += "?";
            for (Object aSet : set) {
                i++;
                Map.Entry entry = (Map.Entry) aSet;
                requestUrl += entry.getKey() + "=" + entry.getValue() + (i < params.size() ? "&" : "");
            }
//            if (sign != null) {
//                requestUrl += "&sign=" + sign;
//            }
        }

        OkHttpClient client = new OkHttpClient.Builder().connectTimeout(TIME_OUT, TimeUnit.MILLISECONDS)
                .writeTimeout(TIME_OUT, TimeUnit.MILLISECONDS).readTimeout(10000, TimeUnit.MILLISECONDS).build();
        final Request request = new Request.Builder().url(requestUrl).build();
        Call call = client.newCall(request);

        //request join
        call.enqueue(new Callback() {
                         @Override
                         public void onFailure(Call call, IOException e) {
                             if (absResponse != null)
                                 absResponse.callback(null);
                         }

                         @Override
                         public void onResponse(Call call, Response response) throws IOException {
                             final String data = response.body().string();
                             mHandler.post(new Runnable() {
                                               @Override
                                               public void run() {
                                                   if (bc.getName().endsWith("String")) {
                                                       if (absResponse != null)
                                                           absResponse.callback(data);
                                                   } else {
                                                       Gson gson = new GsonBuilder().setDateFormat(
                                                               Constants.APP_DATE_FORMAT).create();
                                                       try {
                                                           Type type = CommUtil.GetType(BaseModel.class, bc);
                                                           if (absResponse != null) {
                                                               String jsonBack = data;
                                                               absResponse.callback(gson.fromJson(jsonBack.replace(":\"\"", ":\"\""), type));
                                                           }
                                                       } catch (Exception e) {
                                                           Log.e("errorlog", Log.getStackTraceString(e));
                                                           if (absResponse != null)
                                                               absResponse.callback(null);
                                                       }
                                                   }
                                               }
                                           }

                             );
                         }
                     }

        );
    }


    /**
     * no header Post method
     *
     * @param url
     * @param params        map
     * @param absResponse   success callback
     * @param bc            //use of String.class does not convert
     * @param <T>//Types  need to be converted
     */
    public <T> void postBackBean(final @NonNull String url, final Map<String, String> params,
                                 final Class<T> bc, final MyCallBackParam absResponse) {
        postBackBean(true, url, params, null, bc, absResponse);
    }

    /**
     * Post back bean
     *
     * @param isGuest
     * @param url
     * @param params        Map
     * @param header        can value of null
     * @param bc            use of String.class does not convert
     * @param absResponse   callback of success
     * @param <T>Types  need to be converted
     */
    public <T> void postBackBean(boolean isGuest, final @NonNull String url, final Map<String, String> params,
                                 final Map<String, String> header,
                                 final Class<T> bc, final MyCallBackParam absResponse) {

        OkHttpClient client = new OkHttpClient.Builder().connectTimeout(TIME_OUT, TimeUnit.MILLISECONDS)
                .writeTimeout(TIME_OUT, TimeUnit.MILLISECONDS).readTimeout(300000, TimeUnit.MILLISECONDS).build();
//        String sign = paramsToFormBody(params, isGuest);
        FormBody.Builder formB = new FormBody.Builder();
        //header data
        Headers.Builder hb = new Headers.Builder();
        hb.add("Charset", "UTF-8");
        if (header != null && header.size() > 0) {
            Set set = header.entrySet();
            for (Object aSet : set) {
                Map.Entry entry = (Map.Entry) aSet;
                hb.add(entry.getKey() + "", entry.getValue() + "");
            }
        }
        //request parameters,
        if (params != null && params.size() > 0) {
            Set set = params.entrySet();
            for (Object aSet : set) {
                Map.Entry entry = (Map.Entry) aSet;
                formB.add(entry.getKey() + "", entry.getValue() + "");
            }
            //
//            if (sign != null) {
//                formB.add("sign", sign);
//            }
        } else {
            formB.add("null", "null");
        }

        Request request = new Request.Builder().url(url).post(formB.build()).headers(hb.build()).build();

        Call call = client.newCall(request);
        call.enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {

                mHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        absResponse.callback(null);
                    }
                });
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                final String data = response.body().string();
                //L.j(data);
                //LogUtil.i("Response :\n"+data);

                mHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        if (bc.getName().endsWith("String")) {
                            if (absResponse != null)
                                absResponse.callback(data);
                        } else {
                            Gson gson = new GsonBuilder().setDateFormat(
                                    Constants.APP_DATE_FORMAT).create();
                            try {
                                Type type = CommUtil.GetType(BaseModel.class, bc);
                                if (absResponse != null) {
                                    String jsonBack = data.replace(":\"\"", ":\"\"").replace(":null", ":\"\"");
                                    absResponse.callback(gson.fromJson(jsonBack, type));
                                }
                            } catch (Exception e) {
                                Log.e("errorlog", Log.getStackTraceString(e));
                                if (absResponse != null)
                                    absResponse.callback(null);
                            }
                        }
                    }
                });
            }

        });
    }

    /**
     * Post back list
     *
     * @param url
     * @param params
     * @param header
     * @param absResponse
     * @param bc
     * @param <T>
     */
    public <T> void postBackList(final @NonNull String url, final Map<String, String> params,
                                 final Map<String, String> header,
                                 final Class<T> bc, @NonNull final MyCallBackParam absResponse) {

        OkHttpClient client = new OkHttpClient.Builder().connectTimeout(TIME_OUT, TimeUnit.MILLISECONDS)
                .writeTimeout(TIME_OUT, TimeUnit.MILLISECONDS).readTimeout(300000, TimeUnit.MILLISECONDS).build();
//        String sign = paramsToFormBody(params);
        FormBody.Builder formB = new FormBody.Builder();

        Headers.Builder hb = new Headers.Builder();
        hb.add("Charset", "UTF-8");
        if (header != null && header.size() > 0) {
            Set set = header.entrySet();
            for (Object aSet : set) {
                Map.Entry entry = (Map.Entry) aSet;
                hb.add(entry.getKey() + "", entry.getValue() + "");
            }
        }

        if (params != null && params.size() > 0) {
            Set set = params.entrySet();
            for (Object aSet : set) {
                Map.Entry entry = (Map.Entry) aSet;
                formB.add(entry.getKey() + "", entry.getValue() + "");
            }

        } else {
            formB.add("null", "null");
        }

        Request request = new Request.Builder().url(url).post(formB.build()).headers(hb.build()).build();

        Call call = client.newCall(request);
        call.enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                mHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        absResponse.callback(null);
                    }
                });
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                final String data = response.body().string();
                mHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        if (bc.getName().endsWith("String")) {
                            if (absResponse != null)
                                absResponse.callback(data);
                        } else {
                            Gson gson = new GsonBuilder().setDateFormat(
                                    Constants.APP_DATE_FORMAT).create();
                            try {
                                Type type = CommUtil.GetType(BaseModelList.class, bc);
                                if (absResponse != null) {
                                    String jsonBack = data;
                                    absResponse.callback(gson.fromJson(jsonBack.replace(":\"\"", ":\"\""), type));
                                }
                            } catch (Exception e) {
                                Log.e("errorlog", Log.getStackTraceString(e));
                            }
                        }
                    }
                });
            }

        });
    }


    public static final String SUCCESS = "down load complete";


    /**
     * up files
     *
     * @param key
     */
    public void uploadFile(@NonNull final String url, @NonNull final String filePath, @NonNull final String key,
                           final String contentType, final Map<String, String> header, @NonNull final MyCallBackParam absResponse) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                File file = new File(filePath);
                String BOUNDARY = UUID.randomUUID().toString(); //
                String PREFIX = "--", LINE_END = "\r\n";
                String CONTENT_TYPE = "multipart/form-data"; //
                try {
                    HttpURLConnection conn = (HttpURLConnection) new URL(url).openConnection();
                    conn.setReadTimeout(5000);
                    conn.setConnectTimeout(5000);
                    conn.setDoInput(true);
                    conn.setDoOutput(true);
                    conn.setUseCaches(false);
                    conn.setRequestMethod("POST");
                    conn.setRequestProperty("Charset", "utf-8"); //
                    conn.setRequestProperty("connection", "keep-alive");
                    conn.setRequestProperty("Connection", "close");//create pipe(2) failed: Too many open files
                    conn.setRequestProperty("Content-Type", CONTENT_TYPE + ";boundary=" + BOUNDARY);

                    if (header != null && header.size() > 0) {
                        Set set = header.entrySet();
                        for (Object aSet : set) {
                            Map.Entry entry = (Map.Entry) aSet;
                            conn.setRequestProperty(entry.getKey() + "", entry.getValue() + "");
                        }
                    }
                    OutputStream outputSteam = conn.getOutputStream();
                    DataOutputStream dos = new DataOutputStream(outputSteam);
                    StringBuilder sb = new StringBuilder();
                    sb.append(PREFIX);
                    sb.append(BOUNDARY);
                    sb.append(LINE_END);
                    sb.append("Content-Disposition: form-data; name=\"").append(key).append("\"; filename=\"").append(file.getName()).append("\"").append(LINE_END);
                    sb.append("Content-Type:").append(contentType).append("; charset=utf-8").append(LINE_END);
                    sb.append(LINE_END);
                    dos.write(sb.toString().getBytes());
                    InputStream is = new FileInputStream(file);
                    byte[] bytes = new byte[1024];
                    int len = 0;
                    while ((len = is.read(bytes)) != -1) {
                        dos.write(bytes, 0, len);
                    }
                    is.close();
                    dos.write(LINE_END.getBytes());
                    byte[] end_data = (PREFIX + BOUNDARY + PREFIX + LINE_END).getBytes();
                    dos.write(end_data);
                    dos.flush();
                    int res = conn.getResponseCode();
                    if (res == 200) {
                        BufferedInputStream inputStream = new BufferedInputStream(conn.getInputStream());
                        byte[] buf = new byte[1024];
                        StringBuilder stringBuilder = new StringBuilder();
                        while (inputStream.read(buf) > 0) {
                            stringBuilder.append(new String(buf, 0, buf.length));
                        }
                        String data = stringBuilder.toString();
                        absResponse.callback(data);
                    } else {
                        absResponse.callback("error");
                    }
                } catch (MalformedURLException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    private static OkHttpClient mUploadClient = new OkHttpClient.Builder()
            .connectTimeout(15, TimeUnit.SECONDS)
            .writeTimeout(15, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .build();

    public interface ReqCallBack {

        void onFailure(Call call, IOException e);


        void onResponseSuccess(Call call, int responseCode, byte[] responseBody);


        void onResponseFailure(Call call, int responseCode, byte[] responseBody);
    }
}
