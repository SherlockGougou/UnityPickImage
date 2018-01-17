package cc.shinichi.pick.utils;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.provider.MediaStore;

/**
 * @author zhengzhong on 2016/8/6 16:16
 *         Email zheng_zhong@163.com
 */
public class PhotoUtils {
	private static final String TAG = "PhotoUtils";

	/**
	 * @param activity    当前activity
	 * @param imageUri    拍照后照片存储路径
	 * @param requestCode 调用系统相机请求码
	 */
	public static void takePicture(Activity activity, Uri imageUri, int requestCode) {
		//调用系统相机
		Intent intentCamera = new Intent();
		intentCamera.setAction(MediaStore.ACTION_IMAGE_CAPTURE);
		//将拍照结果保存至photo_file的Uri中，不保留在相册中
		intentCamera.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);
		activity.startActivityForResult(intentCamera, requestCode);
	}

	/**
	 * @param activity    当前activity
	 * @param requestCode 打开相册的请求码
	 */
	public static void openPic(Activity activity, int requestCode) {
		Intent photoPickerIntent = new Intent(Intent.ACTION_GET_CONTENT);
		photoPickerIntent.setType("image/*");
		activity.startActivityForResult(photoPickerIntent, requestCode);
	}

	/**
	 * @param activity    当前activity
	 * @param orgUri      剪裁原图的Uri
	 * @param desUri      剪裁后的图片的Uri
	 * @param aspectX     X方向的比例
	 * @param aspectY     Y方向的比例
	 * @param width       剪裁图片的宽度
	 * @param height      剪裁图片高度
	 * @param requestCode 剪裁图片的请求码
	 */
	public static void cropImageUri(Activity activity, Uri orgUri, Uri desUri, int aspectX, int
			aspectY, int width, int height, int requestCode) {
		Intent intent = new Intent("com.android.camera.action.CROP");
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
			intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
		}
		intent.setDataAndType(orgUri, "image/*");
		//发送裁剪信号
		intent.putExtra("crop", "true");
		intent.putExtra("aspectX", aspectX);
		intent.putExtra("aspectY", aspectY);
		intent.putExtra("outputX", width);
		intent.putExtra("outputY", height);
		intent.putExtra("scale", true);
		//将剪切的图片保存到目标Uri中
		intent.putExtra(MediaStore.EXTRA_OUTPUT, desUri);
		//1-false用uri返回图片
		//2-true直接用bitmap返回图片（此种只适用于小图片，返回图片过大会报错）
		intent.putExtra("return-data", false);
		intent.putExtra("outputFormat", Bitmap.CompressFormat.JPEG.toString());
		intent.putExtra("noFaceDetection", true);
		activity.startActivityForResult(intent, requestCode);
	}
}
