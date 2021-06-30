package com.servirunplatomas.puntoplato.ui.fragments

import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.lifecycle.ViewModelProvider
import com.denzcoskun.imageslider.constants.ScaleTypes
import com.denzcoskun.imageslider.models.SlideModel
import com.servirunplatomas.puntoplato.BuildConfig
import com.servirunplatomas.puntoplato.R
import com.servirunplatomas.puntoplato.data.model.Contact
import com.servirunplatomas.puntoplato.data.repository.FirestoreImpl
import com.servirunplatomas.puntoplato.domain.UseCaseImpl
import com.servirunplatomas.puntoplato.ui.MainViewModel
import com.servirunplatomas.puntoplato.ui.MainViewModelFactory
import com.servirunplatomas.puntoplato.vo.State
import kotlinx.android.synthetic.main.fragment_contact.*
import kotlinx.coroutines.ExperimentalCoroutinesApi
import java.lang.Exception

@ExperimentalCoroutinesApi
class ContactFragment : Fragment() {

    private val viewModel by lazy { ViewModelProvider(this, MainViewModelFactory(UseCaseImpl(FirestoreImpl())))
      .get(MainViewModel::class.java) }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_contact, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setImageSlider()
        observeData()
    }

    private fun observeData(){

        viewModel.liveDataAppDetails.observe(viewLifecycleOwner, { result ->

            when(result) {

                is State.Loading -> { }

                is State.Success -> {
                    setDataContact(result.data, Contact())
                    checkAppVersion(result.data)
                }

                is State.Failure -> {
                    setContactButtons(Contact())
                }
            }
        })
    }

    private fun setImageSlider() {

        val imageList = ArrayList<SlideModel>()

        imageList.add(SlideModel(R.drawable.image_contact))
        imageList.add(SlideModel(R.drawable.image_contact1))
        imageList.add(SlideModel(R.drawable.image_contact2))
        imageList.add(SlideModel(R.drawable.image_contact3))
        imageList.add(SlideModel(R.drawable.image_contact4))

        imageSlider.setImageList(imageList, ScaleTypes.CENTER_CROP)
    }

    private fun setDataContact(result: List<String>, contact: Contact) {

        if (!result.isNullOrEmpty()) {

            for (data in result) {
                contact.instagram = data.substringAfter("instagram=").substringBefore(',').replace("}","")
                contact.email = data.substringAfter("email=").substringBefore(',').replace("}","")
            }

        } else {

            contact.instagram = R.string.contact_instagram.toString()
            contact.email = R.string.contact_email.toString()
        }

        textInstagram.text = contact.instagram
        textEmail.text = contact.email

        setContactButtons(contact)
    }

    private fun setContactButtons(contact: Contact) {

        if (contact.instagram == "" && contact.email == "") {
            contact.instagram = R.string.contact_instagram.toString()
            contact.email = R.string.contact_email.toString()
        }

        layoutInstagram.setOnClickListener {

            var intent: Intent

            try {
                context?.packageManager?.getPackageInfo("com.instagram.android", 0)
                intent = Intent(Intent.ACTION_VIEW, Uri.parse("http://instagram.com/_u/${contact.instagram.replace("@","")}"))
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)

            } catch (e: Exception) {
                intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://instagram.com/${contact.instagram.replace("@","")}"))
            }
            startActivity(intent)
        }

        layoutEmail.setOnClickListener {
            val intent = Intent(Intent.ACTION_SENDTO).apply {
                data = Uri.parse("mailto:${contact.email}")
            }
            startActivity(intent)
        }
    }

    private fun checkAppVersion(result: List<String>) {

        val userVersion = BuildConfig.VERSION_CODE

        for (data in result) {

            val lastVersion = data.substringAfter("version=").substringBefore(',').replace("}","")

            if (userVersion < lastVersion.toInt()) {
                showUpdateDialog()
            }
        }
    }

    private fun showUpdateDialog(){

        val dialogBuilder = AlertDialog.Builder(requireContext())

        dialogBuilder.setMessage("Existe una nueva versión de la aplicación. Podés actualizarla ahora o más tarde!").setCancelable(false)
            .setPositiveButton("Actualizar ahora") { dialog, _ ->
                openPlaystore()
                dialog.dismiss()
            }
            .setNegativeButton("Más tarde") { dialog, _ ->
                dialog.dismiss()
            }

        val alert = dialogBuilder.create()
        alert.setTitle("Actualización disponible")
        alert.show()
    }

    private fun openPlaystore(){

        try {
            startActivity(Intent(Intent.ACTION_VIEW, Uri
                .parse("market://details?id=com.servirunplatomas.puntoplato")))

        } catch (e: ActivityNotFoundException) {
            startActivity(Intent(Intent.ACTION_VIEW, Uri
                .parse("https://play.google.com/store/apps/details?id=com.servirunplatomas.puntoplato")))
        }
    }

    override fun onPause() {
        imageSlider.stopSliding()
        super.onPause()
    }
}