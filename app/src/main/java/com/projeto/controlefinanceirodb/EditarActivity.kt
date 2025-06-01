package com.projeto.controlefinanceirodb

import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity

class EditarActivity : AppCompatActivity() {

    private lateinit var listView: ListView
    private lateinit var adapter: ArrayAdapter<String>
    private lateinit var banco: BancoLite

    override fun onCreate(savedInstanceState: Bundle?){
        super.onCreate(savedInstanceState)
        setContentView(R.layout.lista_telaeditar)

        banco = BancoLite(this)
        listView = findViewById(R.id.ListaEditar)

        val BtnVoltar = findViewById<Button>(R.id.BtnVoltar)
        BtnVoltar.setOnClickListener{
            finish()
        }

        val listaAtualizada = banco.buscarGastos()

        adapter = object : ArrayAdapter<String>(
            this, 0, listaAtualizada
        ) {
            override fun getView(position: Int, convertView: android.view.View?, parent: android.view.ViewGroup): android.view.View{
                val view = layoutInflater.inflate(R.layout.item_lista_editar, parent, false)
                val textItem = view.findViewById<TextView>(R.id.textItem)

                textItem.text = listaAtualizada[position]
                return view
            }
        }
        listView.adapter = adapter
    }
}