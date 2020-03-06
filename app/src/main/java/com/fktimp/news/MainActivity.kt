package com.fktimp.news

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.fktimp.news.models.VKWall
import com.fktimp.news.requests.VKWallsRequest
import com.vk.api.sdk.VK
import com.vk.api.sdk.VKApiCallback
import com.vk.api.sdk.exceptions.VKApiExecutionException
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        VK.setCredentials(
            this,
            7273146,
            "85a7c4e485a7c4e485a7c4e4f285c93e5e885a785a7c4e4dbb3670b90806f21f42228bf",
            null,
            false
        )
        button.setOnClickListener { requestWall() }
    }

    private fun requestWall() {
        VK.execute(VKWallsRequest(-50246288,5), object : VKApiCallback<List<VKWall>> {
            override fun fail(error: VKApiExecutionException) {
                Toast.makeText(applicationContext, error.message, Toast.LENGTH_LONG).show()
                messageET.setText(error.message)
            }

            override fun success(result: List<VKWall>) {
                if (!isFinishing && result.isNotEmpty()) {
                    var string = ""
                    if (result.isNotEmpty())
                        for (i in 0 until result[0].items.size)
                            string += "$i) ${result[0].items[i].text}\n \n"
                    messageET.setText(string)
                }
            }
        })
    }
}