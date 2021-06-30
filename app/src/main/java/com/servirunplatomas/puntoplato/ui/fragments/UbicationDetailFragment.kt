package com.servirunplatomas.puntoplato.ui.fragments

import android.content.Intent
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.os.CountDownTimer
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.fragment.app.DialogFragment
import com.servirunplatomas.puntoplato.R
import com.squareup.picasso.Callback
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.fragment_ubication_detail.*
import java.lang.Exception

class UbicationDetailFragment : DialogFragment() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(STYLE_NORMAL, R.style.FullScreenDialogStyle)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_ubication_detail, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setToolbar()
        showLoadingImages()
        showDataMarker()
        openInstagram()
    }

    private fun setToolbar() {

        toolbarUbication.navigationIcon = view?.context?.let { ContextCompat.getDrawable(it, R.drawable.ic_close) }
        toolbarUbication.setTitleTextColor(Color.WHITE)
        toolbarUbication.setNavigationOnClickListener {
            dismiss()
        }
    }

    private fun showLoadingImages() {
        layoutProgressDetail?.visibility = View.VISIBLE
    }

    private fun showDataMarker() {

        textName.text = arguments?.getString("name")

        toolbarUbication.title = arguments?.getString("address")
        textName.text = arguments?.getString("name")
        textAddress.text = arguments?.getString("address")
        textInstagram.text = arguments?.getString("instagram")
        textSchedule.text = arguments?.getString("schedule")

        Picasso.get().load(arguments?.getString("image")).into(imagePoint, object: Callback {

            override fun onSuccess() {
                layoutProgressDetail?.visibility = View.GONE
            }

            override fun onError(e: Exception?) {
                var counter = 0
                object : CountDownTimer(3000, 1000) {
                    override fun onTick(millisUntilFinished: Long) {
                        counter++
                        layoutErrorDetailFragment?.visibility = View.VISIBLE
                    }
                    override fun onFinish() {
                        layoutErrorDetailFragment?.visibility = View.GONE
                    }
                }.start()
            }
        })
    }

    private fun openInstagram() {

        layoutInstagram.setOnClickListener {

            var intent: Intent

            try {
                context?.packageManager?.getPackageInfo("com.instagram.android", 0)
                intent = Intent(Intent.ACTION_VIEW,
                    Uri.parse("http://instagram.com/_u/${arguments?.getString("instagram")?.replace("@","")}"))
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)

            } catch (e: Exception) {
                intent = Intent(Intent.ACTION_VIEW,
                    Uri.parse("https://instagram.com/${arguments?.getString("instagram")?.replace("@","")}"))
            }
            startActivity(intent)
        }
    }

    override fun onStart() {
        super.onStart()
        dialog?.window?.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
    }
}