package com.baidu.tts.sample.speechutil;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.TextUtils;
import android.text.style.ForegroundColorSpan;
import android.util.Log;
import android.util.Pair;

import com.baidu.tts.chainofresponsibility.logger.LoggerProxy;
import com.baidu.tts.client.SpeechSynthesizer;
import com.baidu.tts.client.SpeechSynthesizerListener;
import com.baidu.tts.client.TtsMode;
import com.baidu.tts.sample.control.InitConfig;
import com.baidu.tts.sample.control.MySyntherizer;
import com.baidu.tts.sample.control.NonBlockSyntherizer;
import com.baidu.tts.sample.listener.UiMessageListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;


/**
 * Author: luki
 * Date:    2018/11/20.
 */

public class VoiceApi {
    private  static final String TAG = "VoiceApi";


    private static volatile VoiceApi singleton;

    private VoiceApi() {
    }

    public static VoiceApi getInstance() {
        if (singleton == null) {
            synchronized (VoiceApi.class) {
                if (singleton == null) {
                    singleton = new VoiceApi();

                }
            }
        }
        return singleton;
    }

    private String appId;

    private String appKey;

    private String secretKey;

    private static final String TAG = "VoiceApi";

    private static Handler mainHandler;

    // TtsMode.MIX; 离在线融合，在线优先； TtsMode.ONLINE 纯在线； 没有纯离线
    private TtsMode ttsMode = TtsMode.ONLINE;


    // ===============初始化参数设置完毕，更多合成参数请至getParams()方法中设置 =================

    // 主控制类，所有合成控制方法从这个类开始
    protected MySyntherizer synthesizer;

    private boolean initFlag = false;


    public VoiceApi initialTts(Context context) {
        if (!initFlag) {
//            Log.i("initialTts ");
            LoggerProxy.printable(true); // 日志打印在logcat中
            initHandler();

            // 设置初始化参数
            // 此处可以改为 含有您业务逻辑的SpeechSynthesizerListener的实现类
            SpeechSynthesizerListener listener = new UiMessageListener(mainHandler);
            Map<String, String> params = getParams(context);
            initConfigure(context);
            // appId appKey secretKey 网站上您申请的应用获取。注意使用离线合成功能的话，需要应用中填写您app的包名。包名在build.gradle中获取。
            InitConfig initConfig = new InitConfig(appId, appKey, secretKey, ttsMode, params, listener);

            // 如果您集成中出错，请将下面一段代码放在和demo中相同的位置，并复制InitConfig 和 AutoCheck到您的项目中
            // 上线时请删除AutoCheck的调用
//            initAuto(context,initConfig);

            synthesizer = new NonBlockSyntherizer(context, initConfig, mainHandler); // 此处可以改为MySyntherizer 了解调用过程
            initFlag = true;
        } else {
            Log.i(TAG,"initialTts   已初始化完成 ");
        }

        return singleton;


    }


    /**
     * 开启语音
     */
    public static String OPEN_SPEECH = "1";
    /**
     * 关闭语音
     */
    public static String CLOSE_SPEECH = "0";

    /**
     * speak 实际上是调用 synthesize后，获取音频流，然后播放。
     * 获取音频流的方式见SaveFileActivity及FileSaveListener
     * 需要合成的文本text的长度不能超过1024个GBK字节。
     */
    public void speak(String str) {
        /**
         * 1== 打开
         * 0==关闭
         */

        String isSpeech = (String) SpUtil.getSpValue(SpConstants.IS_SPEECH);

        Log.i(TAG,"speak -----speak: " + isSpeech);

        if(CLOSE_SPEECH.equals(isSpeech)){

            return;

        }


        // 需要合成的文本text的长度不能超过1024个GBK字节。
        if (TextUtils.isEmpty(str)) {
            return;
        }

        if (null == synthesizer) {
            return;
        }
        // 合成前可以修改参数：
        // Map<String, String> params = getParams();
        // synthesizer.setParams(params);
        int result = synthesizer.speak(str);
        checkResult(result, "speak");
    }


    /**
     * 合成但是不播放，
     * 音频流保存为文件的方法可以参见SaveFileActivity及FileSaveListener
     */
    public void synthesize(String str) {

        if (null == synthesizer) {
            return;
        }

        if (TextUtils.isEmpty(str)) {
            return;
        }
        int result = synthesizer.synthesize(str);
        checkResult(result, "synthesize");
    }

    /**
     * 批量播放
     */
    public void batchSpeak() {


        List<Pair<String, String>> texts = new ArrayList<Pair<String, String>>();
        texts.add(new Pair<String, String>("开始批量播放，", "a0"));
        if (null == synthesizer) {
            return;
        }
        int result = synthesizer.batchSpeak(texts);
        checkResult(result, "batchSpeak");
    }




    public void pause() {

        if (null == synthesizer) {
            return;
        }
        int result = synthesizer.pause();
        checkResult(result, "pause");
    }

    /**
     * 继续播放。仅调用speak后生效，调用pause生效
     */
    public void resume() {

        if (null == synthesizer) {
            return;
        }
        int result = synthesizer.resume();
        checkResult(result, "resume");
    }

    /*
     * 停止合成引擎。即停止播放，合成，清空内部合成队列。
     */
    public void stop() {

        if (null == synthesizer) {
            return;
        }
        int result = synthesizer.stop();
        checkResult(result, "stop");
    }


    public void onDestroy() {

        if (null == synthesizer) {
            return;
        }
        synthesizer.release();
        Log.i(TAG,"synthesizer.release");

    }


    private void checkResult(int result, String method) {
        if (result != 0) {
            toPrint("error code :" + result + " method:" + method + ", 错误码文档:http://yuyin.baidu.com/docs/tts/122 ");
        }
    }


    /**
     * 合成的参数，可以初始化时填写，也可以在合成前设置。
     *
     * @return
     */
    private Map<String, String> getParams(Context context) {
        Log.i(TAG,"getParams: ---------------------1----1" );
        Map<String, String> params = new HashMap<String, String>();
        // 以下参数均为选填
        // 设置在线发声音人： 0 普通女声（默认） 1 普通男声 2 特别男声 3 情感男声<度逍遥> 4 情感儿童声<度丫丫>
        params.put(SpeechSynthesizer.PARAM_SPEAKER, "0");
        // 设置合成的音量，0-9 ，默认 5
        params.put(SpeechSynthesizer.PARAM_VOLUME, "9");
        // 设置合成的语速，0-9 ，默认 5
        params.put(SpeechSynthesizer.PARAM_SPEED, "5");
        // 设置合成的语调，0-9 ，默认 5
        params.put(SpeechSynthesizer.PARAM_PITCH, "5");

        params.put(SpeechSynthesizer.PARAM_MIX_MODE, SpeechSynthesizer.MIX_MODE_HIGH_SPEED_SYNTHESIZE);
        // 该参数设置为TtsMode.MIX生效。即纯在线模式不生效。
        // MIX_MODE_DEFAULT 默认 ，wifi状态下使用在线，非wifi离线。在线状态下，请求超时6s自动转离线
        // MIX_MODE_HIGH_SPEED_SYNTHESIZE_WIFI wifi状态下使用在线，非wifi离线。在线状态下， 请求超时1.2s自动转离线
        // MIX_MODE_HIGH_SPEED_NETWORK ， 3G 4G wifi状态下使用在线，其它状态离线。在线状态下，请求超时1.2s自动转离线
        // MIX_MODE_HIGH_SPEED_SYNTHESIZE, 2G 3G 4G wifi状态下使用在线，其它状态离线。在线状态下，请求超时1.2s自动转离线

        return params;
    }




    private void initConfigure(Context context) {

        try {
            ApplicationInfo appInfo = BaseApplication.getInstance().getPackageManager()
                    .getApplicationInfo(context.getPackageName(), PackageManager.GET_META_DATA);
            Bundle bundle = appInfo.metaData;
            appId = bundle.getInt("com.baidu.speech.APP_ID") + "";
            appKey = bundle.getString("com.baidu.speech.API_KEY");
            secretKey = bundle.getString("com.baidu.speech.SECRET_KEY");
            Log.i(TAG,"initConfigure : " + appId + "--" + appKey + "--" + secretKey);

        } catch (Exception e) {
            e.printStackTrace();

        }
    }


    private void toPrint(String str) {
        Message msg = Message.obtain();
        msg.obj = str;
        mainHandler.sendMessage(msg);
    }

    private void print(Message msg) {
        String message = (String) msg.obj;
        if (message != null) {
            scrollLog(message);
        }
    }

    private void scrollLog(String message) {
        Spannable colorMessage = new SpannableString(message + "\n");
        colorMessage.setSpan(new ForegroundColorSpan(0xff0000ff), 0, message.length(),
                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        Log.i(TAG,"------scrollLog-----------" + colorMessage.toString());

    }

    private void initHandler() {
        mainHandler = new Handler() {
            /*
             * @param msg
             */
            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);
                print(msg);

            }

        };
    }
/**
 * 测试日志
 */
//    private void initAuto(Context context,InitConfig initConfig) {
//        AutoCheck.getInstance(context.getApplicationContext()).check(initConfig, new Handler() {
//            @Override
//            public void handleMessage(Message msg) {
//                if (msg.what == 100) {
//                    AutoCheck autoCheck = (AutoCheck) msg.obj;
//                    synchronized (autoCheck) {
//                        String message = autoCheck.obtainDebugMessage();
//                        Log.i(TAG,"AutoCheckMessage:" + message.toString());
//                    }
//                }
//            }
//
//        });
//    }


    /**
     * android 6.0 以上需要动态申请权限
     */
//    private void initPermission(  ) {
//        String[] permissions = {
//                Manifest.permission.INTERNET,
//                Manifest.permission.ACCESS_NETWORK_STATE,
//                Manifest.permission.MODIFY_AUDIO_SETTINGS,
//                Manifest.permission.WRITE_EXTERNAL_STORAGE,
//                Manifest.permission.WRITE_SETTINGS,
//                Manifest.permission.READ_PHONE_STATE,
//                Manifest.permission.ACCESS_WIFI_STATE,
//                Manifest.permission.CHANGE_WIFI_STATE
//        };
//
//        ArrayList<String> toApplyList = new ArrayList<String>();
//
//        for (String perm : permissions) {
//            if (PackageManager.PERMISSION_GRANTED != ContextCompat.checkSelfPermission(this, perm)) {
//                toApplyList.add(perm);
//                // 进入到这里代表没有权限.
//            }
//        }
//        String[] tmpList = new String[toApplyList.size()];
//        if (!toApplyList.isEmpty()) {
//            ActivityCompat.requestPermissions(this, toApplyList.toArray(tmpList), 123);
//        }
//
//    }


//    @Override
//    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
//        // 此处为android 6.0以上动态授权的回调，用户自行实现。
//    }

}
