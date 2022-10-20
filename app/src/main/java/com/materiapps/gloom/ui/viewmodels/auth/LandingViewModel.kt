package com.materiapps.gloom.ui.viewmodels.auth

import android.annotation.SuppressLint
import android.content.Context
import androidx.lifecycle.ViewModel
import com.materiapps.gloom.BuildConfig
import com.materiapps.gloom.utils.URLs
import com.materiapps.gloom.utils.openUrl
import io.ktor.http.URLBuilder

@SuppressLint("StaticFieldLeak")
class LandingViewModel(
//    private val context: Context
) : ViewModel() {

    fun signIn(context: Context) {
        val url = URLBuilder(URLs.AUTH.LOGIN).also {
            it.parameters.apply {
                append("client_id", BuildConfig.CLIENT_ID)
                append("redirect_uri", "gloom://oauth")
            }
        }.buildString()

        context.openUrl(url)
    }

}