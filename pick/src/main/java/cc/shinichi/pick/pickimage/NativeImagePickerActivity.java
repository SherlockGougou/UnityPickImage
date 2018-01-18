package cc.shinichi.pick.pickimage;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.v4.content.FileProvider;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;
import cc.shinichi.pick.luban.Luban;
import cc.shinichi.pick.luban.OnCompressListener;
import cc.shinichi.pick.utils.PathUtil;
import cc.shinichi.pick.utils.PhotoUtils;
import com.unity3d.player.UnityPlayer;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;

import static android.os.Environment.DIRECTORY_PICTURES;

/**
 * Created by SherlockHolmes on 2017/12/20.16:23
 */

public class NativeImagePickerActivity extends Activity {

    private Context context;
    private static String TAG = "NativeImagePickerActivity";
    public static final String AUTHORITY = "cc.shinichi.pickimagelibrary.fileprovider";
    private static final String DEFAULT_DISK_CACHE_DIR = "ChatImageCache";

    private static final int CODE_GALLERY_REQUEST = 0xa0;
    private static final int CODE_CAMERA_REQUEST = 0xa1;

    private static File fileUri =
        new File(Environment.getExternalStorageDirectory().getPath() + "/sjyym_photo.jpg");
    private static Uri imageUri;

    private String resultPath = "";// 最终图片路径

    @Override protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        context = this;

        Intent intent = getIntent();
        if (intent != null) {
            boolean fromCamera = intent.getBooleanExtra("fromCamera", false);
            if (fromCamera) {
                imageUri = Uri.fromFile(fileUri);
                //通过FileProvider创建一个content类型的Uri
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    imageUri = FileProvider.getUriForFile(context, AUTHORITY, fileUri);
                }
                PhotoUtils.takePicture(this, imageUri, CODE_CAMERA_REQUEST);
            } else {
                PhotoUtils.openPic(this, CODE_GALLERY_REQUEST);
            }
        } else {
            t("操作失败，请返回重试");
            finish();
        }
    }

    @Override protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == RESULT_OK) {
            try {
                switch (requestCode) {
                    //拍照完成回调
                    case CODE_CAMERA_REQUEST:
                        // 拍照得到的图片路径
                        resultPath = PathUtil.getRealPathFromUri(context, imageUri);
                        compressImage(resultPath);
                        break;

                    //访问相册完成回调
                    case CODE_GALLERY_REQUEST:
                        // 相册选择的图片路径
                        imageUri = data.getData();
                        resultPath = PathUtil.getRealPathFromUri(context, imageUri);
                        compressImage(resultPath);
                        break;
                    default:
                        break;
                }
            } catch (Exception e) {
                e.printStackTrace();
                t("操作失败，请返回重试");
                finish();
            }
        } else {
            finish();
        }
    }

    private void t(String msg) {
        Toast.makeText(context, msg, Toast.LENGTH_SHORT).show();
    }

    private void compressImage(String resultPath) {
        if (TextUtils.isEmpty(resultPath)) {
            t("操作失败，请返回重试");
            finish();
            return;
        }
        Luban.with(this).load(resultPath) // 传入要压缩的图片列表
            .ignoreBy(100)                        // 忽略不压缩图片的大小 KB
            .setTargetDir(getImageCacheDir(context).getAbsolutePath()) // 设置压缩后文件存储位置
            .setCompressListener(new OnCompressListener() { //设置回调
                @Override public void onStart() {
                    // 开始压缩
                }

                @Override public void onSuccess(File file) {
                    // 压缩成功，返回unity...
                    Message message = new Message();
                    message.what = 1;
                    message.obj = file.getAbsolutePath();
                    handler.sendMessage(message);
                }

                @Override public void onError(Throwable e) {
                    e.printStackTrace();
                    // 压缩失败
                    handler.sendEmptyMessage(2);
                }
            }).launch();    //启动压缩
    }

    @SuppressLint("HandlerLeak") Handler handler = new Handler() {
        @Override public void handleMessage(Message msg) {
            switch (msg.what) {
                case 1:
                    // 压缩成功
                    String path = (String) msg.obj;
                    Log.d(TAG, "handleMessage: 压缩成功 path==" + path);
                    sendMessageToUnity(path);
                    break;
                case 2:
                    // 压缩失败
                    t("操作失败，请返回重试");
                    finish();
                    break;
            }
        }
    };

    /**
     * 复制文件到指定目录
     */
    public String copyFile(String oldPath, String newPath) {
        try {
            int bytesum = 0;
            int byteread = 0;
            File oldfile = new File(oldPath);
            if (oldfile.exists()) { //文件存在时
                InputStream inStream = new FileInputStream(oldPath); //读入原文件
                FileOutputStream fs =
                    new FileOutputStream(newPath + File.separator + oldfile.getName());
                byte[] buffer = new byte[1024];
                int length;
                while ((byteread = inStream.read(buffer)) != -1) {
                    bytesum += byteread; //字节数 文件大小
                    fs.write(buffer, 0, byteread);
                }
                inStream.close();
                return newPath + File.separator + oldfile.getName();
            } else {
                return "";
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "";
    }

    /**
     * 获取压缩后保存的路径
     */
    private String getImageCacheDirPath(Context context, String cacheName) {
        String path = "";
        File file = context.getExternalFilesDir(null);
        if (file != null && file.exists()) {
            path = file.getPath() + File.separator + cacheName;
            File dirFile = new File(path);
            if (!dirFile.exists()) {
                boolean b = dirFile.mkdirs();
                if (!b) {
                    path = "";
                }
            }
        } else {
            path = "";
        }
        if (TextUtils.isEmpty(path)) {
            String cacheDir = "/Android/data/" + context.getPackageName() + "/files/" + cacheName;
            String pathError = Environment.getExternalStorageDirectory().getPath() + cacheDir;
            File dirFile = new File(pathError);
            if (!dirFile.exists()) {
                boolean b = dirFile.mkdirs();
                if (!b) {
                    path = "";
                } else {
                    path = pathError;
                }
            } else {
                path = pathError;
            }
        }
        if (TextUtils.isEmpty(path)) {
            Log.d(TAG, "getImageCacheDirPath:path 缓存文件获取失败==" + path);
            t("操作失败，请返回重试");
            finish();
            return "";
        } else {
            Log.d(TAG, "getImageCacheDirPath:path 缓存文件获取成功==" + path);
        }
        return path;
    }

    /**
     * Returns a directory with a default name in the private cache directory of the application to
     * use to store retrieved audio.
     *
     * @param context A context.
     * @see #getImageCacheDir(Context, String)
     */
    @Nullable private File getImageCacheDir(Context context) {
        return getImageCacheDir(context, DEFAULT_DISK_CACHE_DIR);
    }

    /**
     * Returns a directory with the given name in the private cache directory of the application to
     * use to store retrieved media and thumbnails.
     *
     * @param context A context.
     * @param cacheName The name of the subdirectory in which to store the cache.
     * @see #getImageCacheDir(Context)
     */
    @Nullable private File getImageCacheDir(Context context, String cacheName) {
        //File cacheDir = context.getExternalCacheDir();
        File cacheDir = context.getExternalFilesDir(DIRECTORY_PICTURES);
        if (cacheDir != null) {
            File result = new File(cacheDir, cacheName);
            if (!result.mkdirs() && (!result.exists() || !result.isDirectory())) {
                // File wasn't able to create a directory, or the result exists but not a directory
                return null;
            }
            return result;
        }
        if (Log.isLoggable(TAG, Log.ERROR)) {
            Log.e(TAG, "default disk cache dir is null");
        }
        return null;
    }

    private void sendMessageToUnity(String resultPath) {
        if (TextUtils.isEmpty(resultPath)) {
            t("操作失败，请返回重试");
            finish();
        }
        Log.d("sendMessageToUnity", "sendMessageToUnity: " + resultPath);
        UnityPlayer.UnitySendMessage("GameChatUI", "PickImagePathMessage", resultPath);
        finish();
    }
}