package com.hcondor.t3_examen.ui.history

import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.ListView
import androidx.appcompat.app.AppCompatActivity
import com.hcondor.t3_examen.R
import com.hcondor.t3_examen.data.local.AppDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class HistoryActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_history)

        val listView: ListView = findViewById(R.id.listHistory)

        CoroutineScope(Dispatchers.IO).launch {
            val history = AppDatabase.getDatabase(applicationContext).placeHistoryDao().getAll()
            val texts = history.map {
                "${it.name} (${it.date})\n${it.note}\nLat: ${it.latitude}, Lon: ${it.longitude}"
            }
            runOnUiThread {
                listView.adapter = ArrayAdapter(this@HistoryActivity, android.R.layout.simple_list_item_1, texts)
            }
        }
    }
}
