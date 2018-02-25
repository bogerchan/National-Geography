package cc.bogerchan.geographic.viewmodel

import android.arch.lifecycle.MutableLiveData
import android.arch.lifecycle.ViewModel
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.support.design.widget.Snackbar
import android.support.v4.content.FileProvider
import android.view.View
import cc.bogerchan.geographic.GApplication
import cc.bogerchan.geographic.R
import cc.bogerchan.geographic.dao.NGCardData
import cc.bogerchan.geographic.dao.NGCardElementData
import cc.bogerchan.geographic.util.CommonUtil
import cc.bogerchan.geographic.util.Timber
import com.facebook.common.executors.CallerThreadExecutor
import com.facebook.common.references.CloseableReference
import com.facebook.datasource.DataSource
import com.facebook.drawee.backends.pipeline.Fresco
import com.facebook.imagepipeline.datasource.BaseBitmapDataSubscriber
import com.facebook.imagepipeline.image.CloseableImage
import com.facebook.imagepipeline.request.ImageRequestBuilder
import java.io.File
import java.io.FileOutputStream

/**
 * Created by Boger Chan on 2018/2/4.
 */
class NGCardShowViewModel : ViewModel() {

    val isMenuShowing = MutableLiveData<Boolean>()

    val cardData = MutableLiveData<NGCardData>()

    fun performOperationSave(view: View, cardElement: NGCardElementData) {
        if (!CommonUtil.hasPermission(GApplication.app, android.Manifest.permission.READ_EXTERNAL_STORAGE)
                || !CommonUtil.hasPermission(GApplication.app, android.Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
            Snackbar.make(view, R.string.tip_no_file_permissions, Snackbar.LENGTH_SHORT).show()
            return
        }
        val file = File(GApplication.imageDir, "${CommonUtil.md5(cardElement.imgUrl)}.jpg")
        if (file.exists()) {
            Snackbar.make(view, GApplication.app.getString(R.string.tip_image_exist, file.absolutePath), Snackbar.LENGTH_LONG).show()
            return
        }
        downloadFile(cardElement.imgUrl, file, { success, path ->
            if (!success || path == null) {
                Snackbar.make(view, R.string.tip_save_image_error, Snackbar.LENGTH_SHORT).show()
                return@downloadFile
            }
            Snackbar.make(view, GApplication.app.getString(R.string.tip_save_image_successfully, path.absolutePath), Snackbar.LENGTH_LONG).show()
            GApplication.app.sendBroadcast(Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, Uri.fromFile(path)))
        })
    }

    fun performOperationShare(view: View, cardElement: NGCardElementData) {
        if (!CommonUtil.hasPermission(GApplication.app, android.Manifest.permission.READ_EXTERNAL_STORAGE)
                || !CommonUtil.hasPermission(GApplication.app, android.Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
            Snackbar.make(view, R.string.tip_no_file_permissions, Snackbar.LENGTH_SHORT).show()
            return
        }
        val file = File(GApplication.imageDir, "${CommonUtil.md5(cardElement.imgUrl)}.jpg")
        if (file.exists()) {
            shareTo(file)
            return
        }
        Snackbar.make(view, R.string.tip_prepare_image, Snackbar.LENGTH_SHORT).show()
        downloadFile(cardElement.imgUrl, file, { success, path ->
            if (!success || path == null) {
                Snackbar.make(view, R.string.tip_save_image_error, Snackbar.LENGTH_SHORT).show()
                return@downloadFile
            }
            shareTo(path)
            GApplication.app.sendBroadcast(Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, Uri.fromFile(path)))
        })
    }

    private fun shareTo(jpgFile: File) {
        GApplication.app.startActivity(Intent(Intent.ACTION_SEND).apply {
            type = "image/jpg"
            putExtra(Intent.EXTRA_STREAM, FileProvider.getUriForFile(GApplication.app, "${GApplication.app.packageName}.fileprovider", jpgFile))
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
            flags = Intent.FLAG_ACTIVITY_CLEAR_TASK
        })
    }

    private fun downloadFile(uri: String, path: File, callback: (Boolean, File?) -> Unit) {
        val request = ImageRequestBuilder.newBuilderWithSource(Uri.parse(uri)).build()
        Fresco.getImagePipeline().fetchDecodedImage(request, GApplication.app).subscribe(object : BaseBitmapDataSubscriber() {
            override fun onFailureImpl(dataSource: DataSource<CloseableReference<CloseableImage>>?) {
                callback.invoke(false, null)
            }

            override fun onNewResultImpl(bitmap: Bitmap?) {
                if (bitmap == null) {
                    callback.invoke(false, null)
                    return
                }
                var fos: FileOutputStream? = null
                try {
                    fos = FileOutputStream(path)
                    if (bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fos)) {
                        callback.invoke(true, path)
                    } else {
                        Timber.e("Can't compress bitmap, path: ${path.absolutePath}")
                        callback.invoke(false, null)
                    }
                } catch (tr: Throwable) {
                    Timber.e(tr)
                    callback.invoke(false, null)
                } finally {
                    CommonUtil.closeQuietly(fos)
                }
            }

        }, CallerThreadExecutor.getInstance())
    }
}