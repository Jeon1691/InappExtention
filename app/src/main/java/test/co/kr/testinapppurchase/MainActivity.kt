package test.co.kr.testinapppurchase

import android.os.Bundle
import android.view.View
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : PurchaseActivity() {

    var token = ""

    override fun licenceKey(): String {
        return "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAj3xEyFXzByNql04+agh+4tauvA6t04b6MdgHdZ/G8kjaWjnqgNTuuEXqcoHdUGP6YfO3oFKb3l96bSg25E4FeZ7qysOitxJyujmR3gTwsmy6+fsv363L9t0K65AY8bMLd/cobTrWtBqC8ilRgnqQ9dqcSHOXR1LOZloHjC38hqzoz9FJUfjKVR/nwQmMyHL+UfWMW4QIpIrxBd+ZNU465vaY4/Vs2ouhQ1jm1BRUK0Y4OTPWH7ryfYC9keqTlg0huzNGmv903NnXabpYEBAMhh5yPs6prd7MF0mF2Srt/TeSh6ZhTQ5Gkw+TbwU0bt62Gupgdxn+QSG5tjMbFIbyTQIDAQAB"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
    }

    fun onClick_purchase(v: View) {
        purchaseItem("test${edit_text.text}")
    }

    fun onClick_subscribe(v: View) {
        subscribeItem("subs${edit_text.text}")
    }

    fun onClick_consume(v: View) {
        consume(token)
    }

    fun onClick_remove(v: View) {
        itemTokens.firstOrNull()?.let(::consume)
    }

    override fun onPurchaseSuccess(itemId: String?, token: String?, dataSignature: String?) {
        btn_purchase.text = dataSignature
        this.token = token ?: ""
    }

    override fun onConsumeSuccess(response: Int?) {
        btn_consume.text = "$response"
    }
}
