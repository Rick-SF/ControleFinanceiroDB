package com.projeto.controlefinanceirodb

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class MainActivity : AppCompatActivity() {

    private lateinit var TextDescricao: EditText
    private lateinit var TextValor: EditText
    private lateinit var ListGastos: ListView
    private lateinit var ViewTotal: TextView
    private lateinit var BtnAdicionarReceita: Button
    private lateinit var BtnAdicionarDespesa: Button
    private lateinit var BtnNavegarParaEdicao: Button

    private lateinit var banco: BancoLite
    private lateinit var adapter: CustomAdapter
    private var somaTotal = 0.0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        banco = BancoLite(this)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        TextDescricao = findViewById(R.id.Descricao)
        ViewTotal = findViewById(R.id.TotalGasto)
        TextValor = findViewById(R.id.Valor)
        ListGastos = findViewById(R.id.ListaGastos)
        BtnAdicionarReceita = findViewById(R.id.BtnAdicionarReceita)
        BtnAdicionarDespesa = findViewById(R.id.BtnAdicionarDespesa)
        BtnNavegarParaEdicao = findViewById(R.id.BtnEditar)         // ID do XML -> BtnEditar

        adapter = CustomAdapter(mutableListOf())
        ListGastos.adapter = adapter

        BtnAdicionarReceita.setOnClickListener {
            adicionarItem(isGasto = false)          // "false" para Receita
        }
        BtnAdicionarDespesa.setOnClickListener {
            adicionarItem(isGasto = true)           // "true" para Despesa
        }

        BtnNavegarParaEdicao.setOnClickListener {
            val intent = Intent(this, EditarActivity::class.java)
            startActivity(intent)
        }

    }

    override fun onResume() {
        super.onResume()
        atualizarLista()
    }

    fun atualizarLista() {
        val listaDeGastos: List<Gasto> = banco.buscarGastos()
        adapter.updateData(listaDeGastos)
        recalculaTotal(listaDeGastos)
    }

    private fun recalculaTotal(lista: List<Gasto>) {
        somaTotal = 0.0
        for (gasto in lista) {
            if (gasto.tipo == "Receita") {
                somaTotal += gasto.valor
            } else if (gasto.tipo == "Despesa") {
                somaTotal -= gasto.valor
            }
        }
        ViewTotal.text = "Soma Total: R$${String.format("%.2f", somaTotal)}"
    }

    // Create
    fun adicionarItem(isGasto: Boolean) {
        val descricao = TextDescricao.text.toString().trim()
        val valorTexto = TextValor.text.toString().trim()

        if (descricao.isNotEmpty() && valorTexto.isNotEmpty()) {
            val valor = valorTexto.replace(",", ".").toDoubleOrNull()
            if (valor != null && valor > 0) {
                val tipo = if (isGasto) "Despesa" else "Receita"

                banco.adicionarGastos(tipo, descricao, valor)
                atualizarLista()

                TextDescricao.text.clear()
                TextValor.text.clear()
            } else {
                Toast.makeText(this, "Valor inválido ou não positivo.", Toast.LENGTH_SHORT).show()
            }
        } else {
            Toast.makeText(this, "Preencha todos os campos.", Toast.LENGTH_SHORT).show()
        }
    }

    inner class CustomAdapter(private var data: MutableList<Gasto>) :
        ArrayAdapter<Gasto>(this@MainActivity, R.layout.item_lista_main, R.id.textItem, data) {

        fun updateData(newData: List<Gasto>) {
            data.clear()
            data.addAll(newData)
            notifyDataSetChanged()
        }

        private inner class ViewHolder(val textItemView: TextView)

        override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
            val view: View
            val viewHolder: ViewHolder

            if (convertView == null) {

                view = layoutInflater.inflate(R.layout.item_lista_main, parent, false)

                viewHolder = ViewHolder(view.findViewById(R.id.textItem))

                view.tag = viewHolder
            } else {
                // Reutiliza a view e o ViewHolder existentes
                view = convertView
                viewHolder = view.tag as ViewHolder
            }

            val gasto = data[position]          // Pega o objeto Gasto na posição atual

            viewHolder.textItemView.text = "${gasto.tipo}: ${gasto.descricao} - R$${String.format("%.2f", gasto.valor)}"

            // Esquema de COres
            if (gasto.tipo == "Receita") {
                viewHolder.textItemView.setTextColor(Color.parseColor("#388E3C")) // Verde para receita
            } else if (gasto.tipo == "Despesa") {
                viewHolder.textItemView.setTextColor(Color.parseColor("#E71D41")) // Vermelho para despesa
            } else {
                viewHolder.textItemView.setTextColor(Color.WHITE) // Cor padrão (se houver outros tipos)
            }
            return view
        }
    }
}