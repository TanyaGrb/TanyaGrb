package com.fktimp.news

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import com.fktimp.news.requests.NewsHelper
import com.vk.api.sdk.VK
import com.vk.api.sdk.auth.VKAccessToken
import com.vk.api.sdk.auth.VKAuthCallback
import com.vk.api.sdk.auth.VKScope


class WelcomeActivity : Activity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (this.getSharedPreferences(
                NewsHelper.SOURCE_SET,
                Context.MODE_PRIVATE
            ).getStringSet(NewsHelper.SOURCE_SET, null) == null
        )
            NewsHelper.saveDefaultSources(this)

        if (VK.isLoggedIn()) {
            MainActivity.startFrom(this)
            finish()
            return
        }
        VK.login(this, arrayListOf(VKScope.WALL, VKScope.FRIENDS, VKScope.OFFLINE))
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        val callback = object : VKAuthCallback {
            override fun onLogin(token: VKAccessToken) {
                MainActivity.startFrom(this@WelcomeActivity)
                finish()
            }

            override fun onLoginFailed(errorCode: Int) {
            }
        }
        if (!VK.onActivityResult(requestCode, resultCode, data, callback)) {
            super.onActivityResult(requestCode, resultCode, data)
        }
    }

    companion object {
        fun startFrom(context: Context) {
            val intent = Intent(context, WelcomeActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            context.startActivity(intent)
        }
    }
}