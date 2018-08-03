package com.dylan.dyn3rdparts.gallerypicker;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;

import com.dylan.dyn3rdparts.gallerypicker.bean.ImageFolder;
import com.dylan.dyn3rdparts.gallerypicker.bean.ImageItem;
import com.dylan.dyn3rdparts.gallerypicker.loader.ImageLoader;
import com.dylan.dyn3rdparts.gallerypicker.utils.Utils;
import com.dylan.dyn3rdparts.gallerypicker.view.CropImageView;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class ImagePicker {

    public static final String TAG = ImagePicker.class.getSimpleName();
    public static final int REQUEST_CODE_TAKE = 1001;
    public static final int REQUEST_CODE_CROP = 1002;
    public static final int REQUEST_CODE_PREVIEW = 1003;
    public static final int RESULT_CODE_ITEMS = 1004;
    public static final int RESULT_CODE_BACK = 1005;

    public static final String EXTRA_RESULT_ITEMS = "extra_result_items";
    public static final String EXTRA_SELECTED_IMAGE_POSITION = "selected_image_position";
    public static final String EXTRA_IMAGE_ITEMS = "extra_image_items";

    private boolean multiMode = true;    //图片选择模式
    private int selectLimit = 9;         //最大选择图片数量
    private boolean crop = true;         //裁剪
    private boolean showCamera = true;   //显示相机
    private boolean isSaveRectangle = false;  //裁剪后的图片是否是矩形，否者跟随裁剪框的形状
    private int outPutX = 800;           //裁剪保存宽度
    private int outPutY = 800;           //裁剪保存高度
    private int focusWidth = 280;         //焦点框的宽度
    private int focusHeight = 280;        //焦点框的高度
    private ImageLoader imageLoader;     //图片加载器
    private CropImageView.Style style = CropImageView.Style.RECTANGLE; //裁剪框的形状
    private File cropCacheFolder;
    private File takeImageFile;
    public Bitmap cropBitmap;

    private ArrayList<ImageItem> mSelectedImages = new ArrayList<>();   //选中的图片集合
    private List<ImageFolder> mImageFolders;      //所有的图片文件夹
    private int mCurrentImageFolderPosition = 0;  //当前选中的文件夹位置 0表示所有图片
    private List<OnImageSelectedListener> mImageSelectedListeners;          // 图片选中的监听回调

    private static ImagePicker mInstance;

    private ImagePicker() {
    }

    public static ImagePicker getInstance() {
        if (mInstance == null) {
            synchronized (ImagePicker.class) {
                if (mInstance == null) {
                    mInstance = new ImagePicker();
                }
            }
        }
        return mInstance;
    }

    public boolean isMultiMode() {
        return multiMode;
    }
    public ImagePicker setMultiMode(boolean multiMode) {
        this.multiMode = multiMode;
        return this;
    }

    public int getSelectLimit() {
        return selectLimit;
    }
    public ImagePicker setSelectLimit(int selectLimit) {
        this.selectLimit = selectLimit;
        return this;
    }

    public boolean isCrop() {
        return crop;
    }
    public ImagePicker setCrop(boolean crop) {
        this.crop = crop;
        return this;
    }

    public boolean isShowCamera() {
        return showCamera;
    }
    public ImagePicker setShowCamera(boolean showCamera) {
        this.showCamera = showCamera;
        return this;
    }

    public boolean isSaveRectangle() {
        return isSaveRectangle;
    }
    public ImagePicker setSaveRectangle(boolean isSaveRectangle) {
        this.isSaveRectangle = isSaveRectangle;
        return this;
    }

    public int getOutPutX() {
        return outPutX;
    }
    public ImagePicker setOutPutX(int outPutX) {
        this.outPutX = outPutX;
        return this;
    }

    public int getOutPutY() {
        return outPutY;
    }
    public ImagePicker setOutPutY(int outPutY) {
        this.outPutY = outPutY;
        return this;
    }

    public int getFocusWidth() {
        return focusWidth;
    }
    public ImagePicker setFocusWidth(int focusWidth) {
        this.focusWidth = focusWidth;
        return this;
    }

    public int getFocusHeight() {
        return focusHeight;
    }
    public ImagePicker setFocusHeight(int focusHeight) {
        this.focusHeight = focusHeight;
        return this;
    }

    public File getTakeImageFile() {
        return takeImageFile;
    }

    public File getCropCacheFolder(Context context) {
        if (cropCacheFolder == null) {
            cropCacheFolder = new File(context.getCacheDir() + "/ImagePicker/cropTemp/");
        }
        return cropCacheFolder;
    }
    public ImagePicker setCropCacheFolder(File cropCacheFolder) {
        this.cropCacheFolder = cropCacheFolder;
        return this;
    }

    public ImageLoader getImageLoader() {
        return imageLoader;
    }
    public ImagePicker setImageLoader(ImageLoader imageLoader) {
        this.imageLoader = imageLoader;
        return this;
    }

    public CropImageView.Style getStyle() {
        return style;
    }
    public ImagePicker setStyle(CropImageView.Style style) {
        this.style = style;
        return this;
    }

    public List<ImageFolder> getImageFolders() {
        return mImageFolders;
    }
    public ImagePicker setImageFolders(List<ImageFolder> imageFolders) {
        mImageFolders = imageFolders;
        return this;
    }

    public int getCurrentImageFolderPosition() {
        return mCurrentImageFolderPosition;
    }
    public ImagePicker setCurrentImageFolderPosition(int mCurrentSelectedImageSetPosition) {
        mCurrentImageFolderPosition = mCurrentSelectedImageSetPosition;
        return this;
    }

    public ArrayList<ImageItem> getCurrentImageFolderItems() {
        return mImageFolders.get(mCurrentImageFolderPosition).images;
    }

    public boolean isSelect(ImageItem item) {
        return mSelectedImages.contains(item);
    }

    public int getSelectImageCount() {
        if (mSelectedImages == null) {
            return 0;
        }
        return mSelectedImages.size();
    }
    public ArrayList<ImageItem> getSelectedImages() {
        return mSelectedImages;
    }
    public ImagePicker setSelectedImages(List<ImageItem> items) {
        mSelectedImages = items == null ? null : new ArrayList<>(items);
        return this;
    }
    public ImagePicker clearSelectedImages() {
        if (mSelectedImages != null) mSelectedImages.clear();
        return this;
    }

    public ImagePicker clear() {
        if (mImageSelectedListeners != null) {
            mImageSelectedListeners.clear();
            mImageSelectedListeners = null;
        }
        if (mImageFolders != null) {
            mImageFolders.clear();
            mImageFolders = null;
        }
        if (mSelectedImages != null) {
            mSelectedImages.clear();
        }
        mCurrentImageFolderPosition = 0;
        return this;
    }

    public void takePicture(Activity activity, int requestCode) {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        takePictureIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        if (takePictureIntent.resolveActivity(activity.getPackageManager()) != null) {
            if (Utils.existSDCard())
                takeImageFile = new File(Environment.getExternalStorageDirectory(), "/DCIM/camera/");
            else takeImageFile = Environment.getDataDirectory();
            takeImageFile = createFile(takeImageFile, "IMG_", ".jpg");
            if (takeImageFile != null) {
                // 默认情况下，即不需要指定intent.putExtra(MediaStore.EXTRA_OUTPUT, uri);
                // 照相机有自己默认的存储路径，拍摄的照片将返回一个缩略图。如果想访问原始图片，
                // 可以通过dat extra能够得到原始图片位置。即，如果指定了目标uri，data就没有数据，
                // 如果没有指定uri，则data就返回有数据！
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.parse("file://" + takeImageFile.getAbsolutePath()));
            }
        }
        activity.startActivityForResult(takePictureIntent, requestCode);
    }

    public static File createFile(File folder, String prefix, String suffix) {
        if (!folder.exists() || !folder.isDirectory()) folder.mkdirs();
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.CHINA);
        String filename = prefix + dateFormat.format(new Date(System.currentTimeMillis())) + suffix;
        return new File(folder, filename);
    }

    public static void galleryAddPic(Context context, File file) {
        Intent mediaScanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
        Uri contentUri = Uri.parse("file://" + file.getAbsolutePath());
        mediaScanIntent.setData(contentUri);
        context.sendBroadcast(mediaScanIntent);
    }

    public interface OnImageSelectedListener {
        void onImageSelected(int position, ImageItem item, boolean isAdd);
    }
    public void addOnImageSelectedListener(OnImageSelectedListener l) {
        if (mImageSelectedListeners == null) mImageSelectedListeners = new ArrayList<>();
        mImageSelectedListeners.add(l);
    }
    public void removeOnImageSelectedListener(OnImageSelectedListener l) {
        if (mImageSelectedListeners == null) return;
        mImageSelectedListeners.remove(l);
    }
    public void addSelectedImageItem(int position, ImageItem item, boolean isAdd) {
        if (isAdd) {
            mSelectedImages.add(item);
        } else {
            for (ImageItem ii : mSelectedImages) {
                if (ii.path.equals(item.path)) {
                    mSelectedImages.remove(ii);
                    break;
                }
            }
        }
        notifyImageSelectedChanged(position, item, isAdd);
    }
    private void notifyImageSelectedChanged(int position, ImageItem item, boolean isAdd) {
        if (mImageSelectedListeners == null) return;
        for (OnImageSelectedListener l : mImageSelectedListeners) {
            l.onImageSelected(position, item, isAdd);
        }
    }
}