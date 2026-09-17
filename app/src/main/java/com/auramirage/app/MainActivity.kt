package com.auramirage.app
import android.app.AlertDialog
import android.Manifest
import android.app.Activity
import android.content.ClipboardManager
import android.content.pm.PackageManager
import android.graphics.Color
import android.os.*
import android.view.ViewGroup
import android.widget.*
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
class MainActivity:Activity(){private lateinit var repo:NotesRepository;private lateinit var reader:ReaderController;private lateinit var status:TextView;private lateinit var list:LinearLayout
 override fun onCreate(b:Bundle?){super.onCreate(b);repo=NotesRepository(this);reader=ReaderController(this){runOnUiThread{status.text=it}};ui();if(Build.VERSION.SDK_INT>=33&&ContextCompat.checkSelfPermission(this,Manifest.permission.POST_NOTIFICATIONS)!=PackageManager.PERMISSION_GRANTED)ActivityCompat.requestPermissions(this,arrayOf(Manifest.permission.POST_NOTIFICATIONS),20);NotificationHelper(this).showReady()}
 private fun ui(){val root=LinearLayout(this).apply{orientation=LinearLayout.VERTICAL;setPadding(32,28,32,16);setBackgroundColor(Color.rgb(247,250,248))};root.addView(TextView(this).apply{text="Files and text for listening";textSize=24f;setTextColor(Color.rgb(49,92,82))});status=TextView(this).apply{text="Voz carregando…";textSize=14f};root.addView(status);val actions=LinearLayout(this);actions.addView(Button(this).apply{text="📋 Colar";setOnClickListener{paste()}},LinearLayout.LayoutParams(0,-2,1f));actions.addView(Button(this).apply{text="＋ Nova";setOnClickListener{editNote(null)}},LinearLayout.LayoutParams(0,-2,1f));root.addView(actions);list=LinearLayout(this).apply{orientation=LinearLayout.VERTICAL};root.addView(ScrollView(this).apply{addView(list)},LinearLayout.LayoutParams(-1,0,1f));setContentView(root);refresh()}
 private fun paste(){val text=getSystemService(ClipboardManager::class.java).primaryClip?.getItemAt(0)?.coerceToText(this)?.toString().orEmpty();if(text.isBlank()){Toast.makeText(this,"A área de transferência está vazia",Toast.LENGTH_SHORT).show();return};editNote(text)}
 private fun editNote(initial:String?){val box=LinearLayout(this).apply{orientation=LinearLayout.VERTICAL};val title=EditText(this).apply{hint="Título";setSingleLine()};val content=EditText(this).apply{hint="Texto para ouvir";minLines=8;gravity=48;setText(initial.orEmpty())};box.addView(title);box.addView(content);AlertDialog.Builder(this).setTitle("Nova nota").setView(box).setPositiveButton("Salvar"){_,_->repo.save(null,title.text.toString(),content.text.toString());refresh()}.setNegativeButton("Cancelar",null).show()}
 private fun editExisting(n:Note){val box=LinearLayout(this).apply{orientation=LinearLayout.VERTICAL};val t=EditText(this).apply{setText(n.title)};val c=EditText(this).apply{setText(n.content);minLines=8};box.addView(t);box.addView(c);AlertDialog.Builder(this).setTitle("Editar nota").setView(box).setPositiveButton("Salvar"){_,_->repo.save(n.id,t.text.toString(),c.text.toString(),n.folderId);refresh()}.setNegativeButton("Cancelar",null).show()}
 private fun refresh(){if(!::list.isInitialized)return;list.removeAllViews();repo.listNotes().forEach{n->val row=LinearLayout(this).apply{orientation=LinearLayout.VERTICAL;setPadding(0,14,0,14)};row.addView(TextView(this).apply{text=n.title;textSize=18f});row.addView(TextView(this).apply{text=n.content.take(120);textSize=14f});val a=LinearLayout(this);a.addView(Button(this).apply{text="🔊 Ler";setOnClickListener{reader.speak(n.content)}});a.addView(Button(this).apply{text="✏️ Editar";setOnClickListener{editExisting(n)}});a.addView(Button(this).apply{text="Excluir";setOnClickListener{repo.delete(n.id);refresh()}});row.addView(a);list.addView(row,ViewGroup.LayoutParams(-1,-2))}}
 override fun onDestroy(){reader.shutdown();repo.close();super.onDestroy()}}
