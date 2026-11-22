package pro.upchain.wallet.utils;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import androidx.annotation.DrawableRes;
import androidx.annotation.Nullable;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.request.target.Target;
import com.youth.banner.loader.ImageLoader;


public class GlideImageLoader extends ImageLoader {

    public GlideImageLoader(int defaultImage) {
        this.defaultImage = defaultImage;
    }

    private int defaultImage;

    /**
     * Url鍔犺浇鍥剧墖
     *
     * @param view
     * @param url
     * @param defaultImage
     */
    public static void loadImage(ImageView view, String url, int defaultImage) {
        loadImage(view, url, defaultImage, -1);
    }

    /**
     * 鍔犺浇Bitmap
     *
     * @param imageView
     * @param bitmap
     * @param defaultImage
     */
    public static void loadBmpImage(ImageView imageView, Bitmap bitmap, int defaultImage) {
        loadImage(imageView, bitmap, defaultImage, -1);
    }

    @SuppressLint("CheckResult")
    private static void loadImage(final ImageView view, Object img, @DrawableRes int defaultImage, @DrawableRes int errorImage) {
        // 涓嶈兘宕?
        if (view == null) {
            LogUtils.e("GlideUtils -> display -> imageView is null");
            return;
        }
        Context context = view.getContext();
        // View浣犺繕娲荤潃鍚楋紵
        if (context instanceof Activity) {
            if (((Activity) context).isFinishing()) {
                return;
            }
        }

        RequestOptions options = new RequestOptions().centerCrop();
        if (defaultImage != -1) {
            options.placeholder(defaultImage);
        }
        if (errorImage != -1) {
            options.error(errorImage);
        }

        Glide.with(context)
                .load(img)
                .apply(options)
                .listener(new RequestListener<Drawable>() {
                    @Override
                    public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Drawable> target, boolean isFirstResource) {
                        return false;
                    }

                    @Override
                    public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target, DataSource dataSource, boolean isFirstResource) {
                        return false;
                    }
                }).into(view);
    }

    @Override
    public void displayImage(Context context, Object path, ImageView imageView) {
        /**
         娉ㄦ剰锛?
         1.鍥剧墖鍔犺浇鍣ㄧ敱鑷繁閫夋嫨锛岃繖閲屼笉闄愬埗锛屽彧鏄彁渚涘嚑绉嶄娇鐢ㄦ柟娉?
         2.杩斿洖鐨勫浘鐗囪矾寰勪负Object绫诲瀷锛岀敱浜庝笉鑳界‘瀹氫綘鍒板簳浣跨敤鐨勯偅绉嶅浘鐗囧姞杞藉櫒锛?
         浼犺緭鐨勫埌鐨勬槸浠€涔堟牸寮忥紝閭ｄ箞杩欑灏变娇鐢∣bject鎺ユ敹鍜岃繑鍥烇紝浣犲彧闇€瑕佸己杞垚浣犱紶杈撶殑绫诲瀷灏辫锛?
         鍒囪涓嶈鑳′贡寮鸿浆锛?
         */

        loadImage(imageView, path, defaultImage, -1);

    }

}

