package org.dev_alex.mojo_qa.mojo.fragments.create_task

import android.Manifest
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Intent
import android.content.pm.PackageManager
import android.content.res.Resources
import android.graphics.Bitmap
import android.graphics.Color
import android.os.Bundle
import android.os.Environment
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.ContextCompat.getSystemService
import androidx.core.content.FileProvider
import androidx.fragment.app.Fragment
import com.github.sumimakito.awesomeqr.AwesomeQrRenderer
import com.github.sumimakito.awesomeqr.option.RenderOption
import com.github.sumimakito.awesomeqr.option.background.BlendBackground
import io.reactivex.Completable
import io.reactivex.android.schedulers.AndroidSchedulers
import kotlinx.android.synthetic.main.fragment_show_poll_data.*
import org.dev_alex.mojo_qa.mojo.CreateTaskModel
import org.dev_alex.mojo_qa.mojo.R
import org.dev_alex.mojo_qa.mojo.services.BitmapService
import org.dev_alex.mojo_qa.mojo.services.Utils
import java.io.File
import java.util.*


@Suppress("DEPRECATION")
class ShowPollDataFragment : Fragment() {
    private val model: CreateTaskModel
        get() = CreateTaskModel.instance!!

    var qrBitmap: Bitmap? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val rootView = inflater.inflate(R.layout.fragment_show_poll_data, container, false)

        Utils.setupCloseKeyboardUI(activity, rootView)
        setupHeader()
        return rootView
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        btExit.setOnClickListener {
            activity?.finish()
        }

        val link = model.createAppointmentResponse?.links?.firstOrNull()?.link.orEmpty()
        drawQr(link)

        btSave.setOnClickListener {
            if (checkExternalPermissions()) {
                val file = saveQrToFile()
                val resultFile = File(
                    Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS),
                    UUID.randomUUID().toString() + ".png"
                )

                file.copyTo(resultFile)
                Toast.makeText(requireContext(), "Сохранено в загрузках", Toast.LENGTH_LONG).show()
            } else {
                requestExternalPermissions()
            }
        }

        btShare.setOnClickListener {
            val file = saveQrToFile()
            val fileUri = FileProvider.getUriForFile(requireContext(), requireContext().applicationContext.packageName + ".provider", file)

            val sendIntent = Intent().apply {
                action = Intent.ACTION_SEND
                putExtra(Intent.EXTRA_STREAM, fileUri)
                putExtra(Intent.EXTRA_TEXT, link)
                type = "text/plain"
            }

            val shareIntent = Intent.createChooser(sendIntent, null)
            startActivity(shareIntent)
        }

        tvQrLink.text = link
        tvQrLink.setOnClickListener {
            val clipboard = getSystemService(requireContext(), ClipboardManager::class.java)
            val clip = ClipData.newPlainText("link", link)
            clipboard?.setPrimaryClip(clip)

            Toast.makeText(context, getString(R.string.copied_to_clipboard), Toast.LENGTH_LONG).show()
        }
    }

    private fun saveQrToFile(): File {
        val file = File(requireContext().cacheDir, "qr_share.png")
        if (file.exists()) {
            file.delete()
        }

        BitmapService.saveBitmapToFile(file, qrBitmap)
        return file
    }

    private fun drawQr(link: String) {
        val width = resources.displayMetrics.widthPixels - (30 * 2).dp

        val renderOption = RenderOption()
        renderOption.content = link
        renderOption.size = width
        renderOption.borderWidth = 30.dp
        renderOption.patternScale = 1f
        renderOption.roundedPatterns = false
        renderOption.clearBorder = true
        renderOption.color =
            com.github.sumimakito.awesomeqr.option.color.Color(false, background = Color.WHITE, light = Color.WHITE, dark = Color.BLACK)

        val background = BlendBackground()
        background.bitmap = Bitmap.createBitmap(width, width, Bitmap.Config.ARGB_8888)
        background.alpha = 1f
        background.borderRadius = 21.dp
        renderOption.background = background

        AwesomeQrRenderer.renderAsync(renderOption, { result ->
            if (result.bitmap != null) {
                qrBitmap = result.bitmap

                Completable.complete().observeOn(AndroidSchedulers.mainThread()).subscribe({
                    ivQr?.setImageBitmap(result.bitmap)
                }, {
                    it.printStackTrace()
                })
            }
        }, { exception ->
            exception.printStackTrace()
        })
    }

    private fun checkExternalPermissions(): Boolean {
        val permissionCheckWrite = ContextCompat.checkSelfPermission(context!!, Manifest.permission.WRITE_EXTERNAL_STORAGE)
        val permissionCheckRead = ContextCompat.checkSelfPermission(context!!, Manifest.permission.READ_EXTERNAL_STORAGE)
        return permissionCheckRead == PackageManager.PERMISSION_GRANTED && permissionCheckWrite == PackageManager.PERMISSION_GRANTED
    }

    private fun requestExternalPermissions() {
        ActivityCompat.requestPermissions(
            activity!!,
            arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE),
            0
        )
    }

    private fun setupHeader() {
        (activity?.findViewById<View>(R.id.title) as TextView).text = getString(R.string.assignments)
        activity?.findViewById<View>(R.id.back_btn)?.visibility = View.VISIBLE
        activity?.findViewById<View>(R.id.grid_btn)?.visibility = View.GONE
        activity?.findViewById<View>(R.id.sandwich_btn)?.visibility = View.GONE
        activity?.findViewById<View>(R.id.group_by_btn)?.visibility = View.GONE
        activity?.findViewById<View>(R.id.search_btn)?.visibility = View.GONE
        activity?.findViewById<View>(R.id.notification_btn)?.visibility = View.GONE
        activity?.findViewById<View>(R.id.qr_btn)?.visibility = View.GONE

        activity?.findViewById<View>(R.id.back_btn)?.setOnClickListener {
            activity?.finish()
        }
    }

    companion object {
        @JvmStatic
        fun newInstance(): ShowPollDataFragment {
            return ShowPollDataFragment()
        }
    }

    private val Int.dp: Int
        get() = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, this.toFloat(), Resources.getSystem().displayMetrics).toInt()
}