package com.projeto.controlefinanceirodb

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity

class EditarActivity : AppCompatActivity() {

    private lateinit var listViewEditar: ListView
    private lateinit var adapter: GastosAdapter
    private lateinit var banco: BancoLite
    private var listaDeGastos: MutableList<Gasto> = mutableListOf()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.lista_telaeditar)

        banco = BancoLite(this)
        listViewEditar = findViewById(R.id.ListaEditar) // ID da ListView -> lista_telaeditar.xml

        val btnVoltar: Button = findViewById(R.id.BtnVoltar)
        btnVoltar.setOnClickListener {
            finish()                        // Termina a activity e volta para a anterior -> MainActivity (faz .pop())
        }

        adapter = GastosAdapter(this, listaDeGastos)
        listViewEditar.adapter = adapter

        carregarGastos()
    }

    override fun onResume() {
        super.onResume()
        carregarGastos()
    }

    private fun carregarGastos() {
        val gastosDoBanco = banco.buscarGastos()
        listaDeGastos.clear()
        listaDeGastos.addAll(gastosDoBanco)
        adapter.notifyDataSetChanged()          // "Notifica" o adapter que ocorreram mudanças
    }

    private fun mostrarDialogoEdicao(gasto: Gasto) {
        val builder = AlertDialog.Builder(this)
        val inflater = layoutInflater
        val dialogView = inflater.inflate(R.layout.dialog_editar_gasto, null)
        builder.setView(dialogView)

        val editTextTipo: EditText = dialogView.findViewById(R.id.editTextDialogTipo)
        val editTextDescricao: EditText = dialogView.findViewById(R.id.editTextDialogDescricao)
        val editTextValor: EditText = dialogView.findViewById(R.id.editTextDialogValor)
        val textViewId: TextView = dialogView.findViewById(R.id.textViewDialogId)           // Id do registro -> ReadOnly

        editTextTipo.setText(gasto.tipo)
        editTextDescricao.setText(gasto.descricao)
        editTextValor.setText(gasto.valor.toString())
        textViewId.text = "ID: ${gasto.id}"

        builder.setTitle("Editar Gasto (ID: ${gasto.id})")
        builder.setPositiveButton("Salvar") { dialog, _ ->
            val novoTipo = editTextTipo.text.toString().trim()
            val novaDescricao = editTextDescricao.text.toString().trim()
            val novoValorStr = editTextValor.text.toString().trim()

            if (novoTipo.isNotEmpty() && novaDescricao.isNotEmpty() && novoValorStr.isNotEmpty()) {
                val novoValor = novoValorStr.toDoubleOrNull()
                if (novoValor != null && novoValor > 0) {
                    val linhasAfetadas = banco.atualizarGastos(gasto.id, novoTipo, novaDescricao, novoValor)
                    if (linhasAfetadas > 0) {
                        Toast.makeText(this, "Gasto atualizado!", Toast.LENGTH_SHORT).show()
                        carregarGastos()            // Recarrega a lista para com alteração
                    } else {
                        Toast.makeText(this, "Falha ao atualizar.", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    Toast.makeText(this, "Valor inválido ou não positivo.", Toast.LENGTH_SHORT).show()
                }
            } else {
                Toast.makeText(this, "Todos os campos devem ser preenchidos.", Toast.LENGTH_SHORT).show()
            }
            dialog.dismiss()
        }
        builder.setNegativeButton("Cancelar") { dialog, _ ->
            dialog.dismiss()
        }
        val alertDialog = builder.create()
        alertDialog.show()
    }

    // Adapter customizado para a lista de gastos com botões de editar/apagar
    inner class GastosAdapter(
        private val context: Context,
        private val dataSource: MutableList<Gasto>
    ) : BaseAdapter() {

        private val inflater: LayoutInflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater

        override fun getCount(): Int = dataSource.size
        override fun getItem(position: Int): Gasto = dataSource[position]
        override fun getItemId(position: Int): Long = dataSource[position].id.toLong()          // usa o ID do gasto

        override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
            val view: View
            val holder: ViewHolder

            if (convertView == null) {
                view = inflater.inflate(R.layout.item_lista_editar, parent, false) // Nosso layout de item
                holder = ViewHolder()
                holder.textoDetalhes = view.findViewById(R.id.textItemDetalhes_editar)
                holder.btnEditar = view.findViewById(R.id.btnEditarItem_editar)
                holder.btnApagar = view.findViewById(R.id.btnApagarItem_editar)
                view.tag = holder
            } else {
                view = convertView
                holder = view.tag as ViewHolder
            }

            val gasto = getItem(position)

            holder.textoDetalhes.text = "${gasto.tipo}: ${gasto.descricao} - R$${String.format("%.2f", gasto.valor)}"

            // Ação personalizada do botão Apagar
            holder.btnApagar.setOnClickListener {
                AlertDialog.Builder(context)
                    .setTitle("Confirmar Exclusão")
                    .setMessage("Tem certeza que deseja excluir '${gasto.descricao}'?")
                    .setPositiveButton("Sim") { _, _ ->
                        val linhasAfetadas = banco.deletarGastos(gasto.id)
                        if (linhasAfetadas > 0) {
                            Toast.makeText(context, "Item excluído!", Toast.LENGTH_SHORT).show()

                            carregarGastos()
                        } else {
                            Toast.makeText(context, "Erro ao excluir.", Toast.LENGTH_SHORT).show()
                        }
                    }
                    .setNegativeButton("Não", null)
                    .show()
            }

            // Ação para botão Editar
            holder.btnEditar.setOnClickListener {
                mostrarDialogoEdicao(gasto)
            }
            return view
        }

        private inner class ViewHolder {
            lateinit var textoDetalhes: TextView
            lateinit var btnEditar: Button
            lateinit var btnApagar: Button
        }
    }
}