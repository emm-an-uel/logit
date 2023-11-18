package es.emmanuel.logit.settings

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.logit.R

class AllSettingsActivity : AppCompatActivity() {

    // note: this activity is just here for ColorCodesSettingsActivity to have a parent activity

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_all_settings)
    }
}
