package com.auramirage.app

import android.Manifest
import android.app.Activity
import android.app.AlertDialog
import android.content.ClipboardManager
import android.content.Intent
import android.content.pm.PackageManager
import android.database.Cursor
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.view.ViewGroup
import android.widget.*
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat

class MainActivity : Activity() {
    private lateinit var repo: NotesRepository
    private lateinit var reader: ReaderController
    private lateinit var status: TextView
    private lateinit var list: LinearLayout

    override fun onCreate(state: Bundle?) {
        super.onCreate(state)
        repo = NotesRepository(this)
        reader = ReaderController(this) { runOnUiThread { status.text = it } }
        buildUi()
        handleNotificationAction(intent)
        if (android.os.Build.VERSION.SDK_INT >= 33 && ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.POST_NOTIFICATIONS), 20)
        }
        NotificationHelper(this).showReady()
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        handleNotificationAction(intent)
    }

    private fun handleNotificationAction(intent: Intent?) {
        when (intent?.action) {
            NotificationHelper.ACTION_PASTE -> paste()
            NotificationHelper.ACTION_ATTACH -> chooseFile()
            NotificationHelper.ACTION_MORE -> showMoreOptions()
        }
    }

    private fun buildUi() {
        val root = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL; setPadding(32, 28, 32, 16); setBackgroundColor(Color.rgb(247, 250, 248)) }
        root.addView(TextView(this).apply { text = getString(R.string.app_name); textSize = 24f; setTextColor(Color.rgb(49, 92, 82)) })
        status = TextView(this).apply { text = "Voz carregando…"; textSize = 14f; contentDescription = "Status da leitura" }
        root.addView(status)
        val actions = LinearLayout(this)
        actions.addView(Button(this).apply { text = "📋 Colar"; contentDescription = "Colar texto"; setOnClickListener { paste() } }, LinearLayout.LayoutParams(0, -2, 1f))
        actions.addView(Button(this).apply { text = "＋ Nova"; contentDescription = "Criar nova nota"; setOnClickListener { editNote(null) } }, LinearLayout.LayoutParams(0, -2, 1f))
        actions.addView(Button(this).apply { text = "🎙 Voz"; contentDescription = "Escolher voz"; setOnClickListener { showVoicePicker() } }, LinearLayout.LayoutParams(0, -2, 1f))
        root.addView(actions)
        list = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL }
        root.addView(ScrollView(this).apply { addView(list) }, LinearLayout.LayoutParams(-1, 0, 1f))
        setContentView(root)
        refresh()
    }

    private fun paste() {
        val text = getSystemService(ClipboardManager::class.java).primaryClip?.getItemAt(0)?.coerceToText(this)?.toString().orEmpty()
        if (text.isBlank()) { Toast.makeText(this, "A área de transferência está vazia", Toast.LENGTH_SHORT).show(); return }
        editNote(text)
    }

    private fun chooseFile() {
        startActivityForResult(Intent(Intent.ACTION_OPEN_DOCUMENT).apply { type = "*/*"; addCategory(Intent.CATEGORY_OPENABLE) }, REQUEST_FILE)
    }

    @Deprecated("Kept for the Android activity result contract used by this MVP")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_FILE && resultCode == RESULT_OK) {
            val uri = data?.data ?: return
            val name = documentName(uri) ?: "arquivo selecionado"
            Toast.makeText(this, "$name pronto para importar", Toast.LENGTH_LONG).show()
        }
    }

    private fun documentName(uri: Uri): String? {
        val cursor: Cursor? = contentResolver.query(uri, arrayOf("_display_name"), null, null, null)
        return cursor?.use { if (it.moveToFirst()) it.getString(0) else null }
    }

    private fun showMoreOptions() {
        AlertDialog.Builder(this).setTitle("Mais opções").setItems(arrayOf("🎙 Escolher voz", "📚 Abrir biblioteca", "⚙️ Configurações")) { _, which ->
            when (which) { 0 -> showVoicePicker(); 1 -> list.requestFocus(); 2 -> Toast.makeText(this, "Configurações serão adicionadas depois", Toast.LENGTH_SHORT).show() }
        }.show()
    }

    private fun showVoicePicker() {
        val voices = reader.voices()
        if (voices.isEmpty()) { Toast.makeText(this, "As vozes ainda estão carregando ou não há voz em português", Toast.LENGTH_LONG).show(); return }
        val labels = voices.map { "${it.locale.toLanguageTag()} — ${it.name}" }.toTypedArray()
        val checked = voices.indexOfFirst { it.name == reader.selectedVoiceName }.coerceAtLeast(-1)
        AlertDialog.Builder(this).setTitle("Escolher voz").setSingleChoiceItems(labels, checked) { dialog, which -> reader.selectVoice(voices[which]); dialog.dismiss() }.setNegativeButton("Cancelar", null).show()
    }

    private fun editNote(initial: String?) {
        val box = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL }
        val title = EditText(this).apply { hint = "Título"; setSingleLine() }
        val content = EditText(this).apply { hint = "Texto para ouvir"; minLines = 8; gravity = 48; setText(initial.orEmpty()) }
        box.addView(title); box.addView(content)
        AlertDialog.Builder(this).setTitle("Nova nota").setView(box).setPositiveButton("Salvar") { _, _ -> repo.save(null, title.text.toString(), content.text.toString()); refresh() }.setNegativeButton("Cancelar", null).show()
    }

    private fun editExisting(note: Note) {
        val box = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL }
        val title = EditText(this).apply { setText(note.title) }
        val content = EditText(this).apply { setText(note.content); minLines = 8 }
        box.addView(title); box.addView(content)
        AlertDialog.Builder(this).setTitle("Editar nota").setView(box).setPositiveButton("Salvar") { _, _ -> repo.save(note.id, title.text.toString(), content.text.toString(), note.folderId); refresh() }.setNegativeButton("Cancelar", null).show()
    }

    private fun refresh() {
        if (!::list.isInitialized) return
        list.removeAllViews()
        repo.listNotes().forEach { note ->
            val row = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL; setPadding(0, 14, 0, 14) }
            row.addView(TextView(this).apply { text = note.title; textSize = 18f })
            row.addView(TextView(this).apply { text = note.content.take(120); textSize = 14f })
            val actions = LinearLayout(this)
            actions.addView(Button(this).apply { text = "🔊 Ler"; contentDescription = "Ler ${note.title}"; setOnClickListener { reader.speak(note.content) } })
            actions.addView(Button(this).apply { text = "✏️ Editar"; contentDescription = "Editar ${note.title}"; setOnClickListener { editExisting(note) } })
            actions.addView(Button(this).apply { text = "Excluir"; contentDescription = "Excluir ${note.title}"; setOnClickListener { repo.delete(note.id); refresh() } })
            row.addView(actions); list.addView(row, ViewGroup.LayoutParams(-1, -2))
        }
    }

    override fun onDestroy() { reader.shutdown(); repo.close(); super.onDestroy() }
    companion object { private const val REQUEST_FILE = 40 }
}
