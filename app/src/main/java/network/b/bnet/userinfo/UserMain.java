package network.b.bnet.userinfo;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AppCompatActivity;
import android.widget.Toast;

import java.io.File;
import java.util.List;

import network.b.bnet.R;
import network.b.bnet.base.BNetApplication;
import network.b.bnet.model.User;
import network.b.bnet.utils.FilesUtils;
import network.b.bnet.utils.SharePreferenceMain;

public class UserMain extends AppCompatActivity {

    private final int PHOTO_REQUEST_TAKEPHOTO = 1;//拍照
    private final int PHOTO_REQUEST_GALLERY = 2;// 从相册中选择
    private static final int PHOTO_REQUEST_CUT = 3;// 结果
    private String changePhoneName = "";

    private int changPhoneStatus = 0;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_main);
    }


    /**
     * 选择打开采集图片方式
     */
    private void ShowPickDialog() {
        new AlertDialog.Builder(UserMain.this).setTitle("设置头像")
                .setNegativeButton("相册", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        getPhotosFromLibrary(BNetApplication.getInstance());
                    }
                }).setPositiveButton("拍照", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    getPhotosFromCarmera24();
                } else {
                    getPhotosFromCarmera();
                }
            }
        }).show();
    }

    private void getPhotosFromLibrary(BNetApplication context) {
        File outputImage = new File(FilesUtils.getAPPBasePath(),
                getPhotoFileName());
        Uri uri = Uri.fromFile(outputImage);

        if (outputImage.exists()) {
            outputImage.delete();
        }

        Intent intent = new Intent(Intent.ACTION_PICK, null);
        //此处调用了图片选择器;如果直接写intent.setDataAndType("image/*") ,调用的是系统图库
        intent.setDataAndType(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, "image/*");
        intent.putExtra(MediaStore.EXTRA_OUTPUT, uri);
        startActivityForResult(intent, PHOTO_REQUEST_GALLERY);
    }

    //使用照片的名称
    private String getPhotoFileName() {
        String userId = "1";
        User user = SharePreferenceMain.getSharedPreference(this.getApplicationContext()).getLoginData();
        if (user != null) {
            userId = String.valueOf(user.getUserId());
        }

        String fileName = "cmx_" + userId + ".jpg";
        changePhoneName = fileName;
        return fileName;
    }

    //用相机拍照
    public void getPhotosFromCarmera() {
        changePhoneName = getPhotoFileName();
        changPhoneStatus = PHOTO_REQUEST_TAKEPHOTO;

        String state = Environment.getExternalStorageState();
        Intent openCameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (state.equals(Environment.MEDIA_MOUNTED)) {

            Uri imageUri = Uri.fromFile(new File(FilesUtils.getAPPBasePath(), changePhoneName));

            //指定照片保存路径（SD卡）
            openCameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);
            startActivityForResult(openCameraIntent, PHOTO_REQUEST_TAKEPHOTO);
        } else {
            Toast.makeText(UserMain.this, "SD卡不能正常使用", Toast.LENGTH_SHORT).show();
        }
    }

    Uri RequestPhoneimageUri = null;

    //用相机拍照
    public void getPhotosFromCarmera24() {
        changePhoneName = getPhotoFileName();
        changPhoneStatus = PHOTO_REQUEST_TAKEPHOTO;

        String state = Environment.getExternalStorageState();
        Intent openCameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (openCameraIntent.resolveActivity(getPackageManager()) != null) {
            if (state.equals(Environment.MEDIA_MOUNTED)) {
                File newFile = createTakePhotoFile();
                RequestPhoneimageUri = FileProvider.getUriForFile(this, "com.cmx.preview", newFile);
                List<ResolveInfo> resInfoList = getPackageManager().queryIntentActivities(openCameraIntent, PackageManager.MATCH_DEFAULT_ONLY);
                for (ResolveInfo resolveInfo : resInfoList) {
                    String packageName = resolveInfo.activityInfo.packageName;
                    grantUriPermission(packageName, RequestPhoneimageUri,
                            Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
                }

                //指定照片保存路径（SD卡）
                openCameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, RequestPhoneimageUri);
                startActivityForResult(openCameraIntent, PHOTO_REQUEST_TAKEPHOTO);
            } else {
                Toast.makeText(UserMain.this, "SD卡不能正常使用", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private File createTakePhotoFile() {
        File imagePath = new File(getExternalFilesDir(Environment.DIRECTORY_PICTURES), "take_photo");
        if (!imagePath.exists()) {
            imagePath.mkdirs();
        }
        File file = new File(imagePath, changePhoneName);
        return file;
    }

    public void finishGetPhotoFromCarmar() {
        if (changePhoneName == null) {
            return;
        }
        Uri outputUri = null;
        String imgNmae = "final_" + changePhoneName;
        Intent cropIntent = new Intent("com.android.camera.action.CROP");
        //        Uri imageUri = null;

        outputUri = Uri.fromFile(new File(FilesUtils.getAPPBasePath(), imgNmae));
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            //            imageUri = Uri.fromFile(new File(mChangeHeadImgPhotoPath));
            //添加这一句表示对目标应用临时授权该Uri所代表的文件
            cropIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            //通过FileProvider创建一个content类型的Uri
            RequestPhoneimageUri = FileProvider.getUriForFile(this, "com.cmx.preview", createTakePhotoFile());

        } else {
            RequestPhoneimageUri = Uri.fromFile(new File(FilesUtils.getAPPBasePath(), changePhoneName));
        }

        cropIntent.setDataAndType(RequestPhoneimageUri, "image/*");
        cropIntent.putExtra("crop", "true");
        cropIntent.putExtra("aspectX", 1);
        cropIntent.putExtra("aspectY", 1);
        // outputX outputY 是裁剪后生成图片的宽高
        cropIntent.putExtra("outputX", 300);
        cropIntent.putExtra("outputY", 300);
        cropIntent.putExtra("return-data", true);
        cropIntent.putExtra(MediaStore.EXTRA_OUTPUT, outputUri);
        startActivityForResult(cropIntent, PHOTO_REQUEST_CUT);
    }

    public void gotoCutPicture(Uri uri) {
        Intent cropIntent = new Intent("com.android.camera.action.CROP");
        cropIntent.setDataAndType(uri, "image/*");
        cropIntent.putExtra("crop", "true");
        cropIntent.putExtra("aspectX", 1);
        cropIntent.putExtra("aspectY", 1);
        // outputX outputY 是裁剪后生成图片的宽高
        cropIntent.putExtra("outputX", 300);
        cropIntent.putExtra("outputY", 300);
        cropIntent.putExtra("return-data", true);
        startActivityForResult(cropIntent, PHOTO_REQUEST_CUT);
    }

    /**
     * 从图库获取完图片后进入裁剪步骤
     *
     * @param data
     */
    public void finishGetPhotoFromLibrary(Intent data) {
        // 从相册选取图片后剪切的结果（储存剪切完的图片，拿到路径）
        // 照片的原始资源地址
        Uri originalUri = data.getData();
        String text = data.getData().toString();
        //显示图片路径
        //        Toast.makeText(this, text, Toast.LENGTH_LONG).show();
        if (originalUri == null) {
            return;
        }
        gotoCutPicture(originalUri);
    }
}
