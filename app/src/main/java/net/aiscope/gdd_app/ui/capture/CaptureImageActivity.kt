package net.aiscope.gdd_app.ui.capture

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Matrix
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import dagger.android.AndroidInjection
import io.fotoapparat.Fotoapparat
import kotlinx.android.synthetic.main.activity_capture_image.*
import kotlinx.android.synthetic.main.toolbar.*
import net.aiscope.gdd_app.R
import net.aiscope.gdd_app.extensions.writeToFile
import net.aiscope.gdd_app.ui.mask.MaskActivity
import java.io.File
import javax.inject.Inject

class CaptureImageActivity : AppCompatActivity(), CaptureImageView {

    @Inject
    lateinit var presenter: CaptureImagePresenter

    lateinit var fotoapparat: Fotoapparat

    override fun onCreate(savedInstanceState: Bundle?) {
        AndroidInjection.inject(this)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_capture_image)
        setSupportActionBar(toolbar)

        fotoapparat = Fotoapparat(
            context = this,
            view = camera_view,
            cameraErrorCallback = { presenter   .onCaptureError(it) }
        )

        capture_image_button.setOnClickListener { presenter.handleCaptureImageButton() }
    }

    override fun onStart() {
        fotoapparat.start()
        super.onStart()
    }

    override fun onStop() {
        fotoapparat.stop()
        super.onStop()
    }

    override fun takePhoto(id: String, onPhotoReceived: (File?) -> Unit) {
        val result = fotoapparat.takePicture()
        val dest = File(this.filesDir, "${id}.jpg")
        result.toBitmap().whenAvailable {
            it?.let {
                val degrees = (-it.rotationDegrees) % 360
                val bmp = it.bitmap.rotate(degrees.toFloat())
                bmp.writeToFile(dest)

                onPhotoReceived(dest)
            } ?: notifyImageCouldNotBeTaken()
        }
    }


    fun Bitmap.rotate(degrees: Float): Bitmap {
        val matrix = Matrix().apply { postRotate(degrees) }
        return Bitmap.createBitmap(this, 0, 0, width, height, matrix, true)
    }

    override fun notifyImageCouldNotBeTaken() {
        Toast.makeText(this, getString(R.string.image_could_not_be_taken), Toast.LENGTH_SHORT).show()
    }


    override fun goToMask(imagePath: String?) {
        val intent = Intent(this, MaskActivity::class.java)
        intent.putExtra("imagePath", imagePath)

        startActivity(intent)
    }
}


