package com.richarddewan.languagetranslation

import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.speech.tts.TextToSpeech
import android.util.Log
import android.view.View
import androidx.annotation.RequiresApi
import com.google.firebase.ml.common.modeldownload.FirebaseModelDownloadConditions
import com.google.firebase.ml.naturallanguage.FirebaseNaturalLanguage
import com.google.firebase.ml.naturallanguage.translate.FirebaseTranslateLanguage
import com.google.firebase.ml.naturallanguage.translate.FirebaseTranslatorOptions
import kotlinx.android.synthetic.main.activity_main.*
import org.jetbrains.anko.alert
import java.util.*

class MainActivity : AppCompatActivity() {
    companion object {
        const val TAG = "MainActivity"
    }
    private lateinit var textToSpeech: TextToSpeech


    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        //hide the play button
        btn_speak.visibility = View.GONE

        // Create an English-Thai translator:
        val options = FirebaseTranslatorOptions.Builder()
                .setSourceLanguage(FirebaseTranslateLanguage.EN)
                .setTargetLanguage(FirebaseTranslateLanguage.TH)
                .build()

        //initialize FirebaseNaturalLanguage
        val translator = FirebaseNaturalLanguage.getInstance().getTranslator(options)

        //model download condition
        val condition = FirebaseModelDownloadConditions.Builder()
                .requireWifi()
                .build()

        /*
        download the model
         */
        translator.downloadModelIfNeeded(condition)
                .addOnSuccessListener {
                    // Model downloaded successfully. Okay to start translating.
                    btn_translate.isEnabled = true
                }
                .addOnFailureListener {
                    // Model couldn’t be downloaded or other internal error.
                    btn_translate.isEnabled = false
                    txt_translated.setText(it.message.toString())
                    Log.e(TAG, it.message.toString())
                }

        /*
        translate on button click
         */
        btn_translate.setOnClickListener {
            if (txt_to_translate.length() != 0) {
                translator.translate(txt_to_translate.text.toString())
                        .addOnSuccessListener { translatedText ->
                            // Translation successful.
                            txt_translated.setText(translatedText)
                            //un hide the play button
                            btn_speak.visibility = View.VISIBLE
                        }
                        .addOnFailureListener { exception ->
                            // Error.
                            Log.e(TAG, exception.message.toString())
                        }
            }
            else {
                alert {
                    title = "Empty!"
                    message = "Text to translate cannot be empty."
                    positiveButton("OK"){
                        it.dismiss()
                    }
                }.show()

            }
        }

        /*
        play the text
         */
        btn_speak.setOnClickListener {
            if (txt_translated.length() != 0){
                textToSpeech.speak(txt_translated.text.toString(),TextToSpeech.QUEUE_FLUSH,null)
            }
            else {
                alert {
                    title = "Empty!"
                    message = "There is no translated text."
                    positiveButton("OK"){
                        it.dismiss()
                    }
                }.show()

            }
        }

        /*
        text to speak
         */
        textToSpeech = TextToSpeech(this, TextToSpeech.OnInitListener { status ->
            if (status != TextToSpeech.ERROR){
                textToSpeech.language = Locale.forLanguageTag("TH")
            }
        })


    }

    override fun onPause() {
        textToSpeech.stop()
        textToSpeech.shutdown()
        super.onPause()
    }
}
