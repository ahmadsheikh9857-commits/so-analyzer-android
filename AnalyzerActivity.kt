package com.example.soanalyzer

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.soanalyzer.utils.CodeInstruction
import com.example.soanalyzer.utils.ELFParser
import com.example.soanalyzer.utils.ELFSection
import com.example.soanalyzer.utils.ELFSymbol
import com.example.soanalyzer.utils.FileSystemManager
import com.google.android.material.button.MaterialButton
import com.google.android.material.tabs.TabLayout
import com.google.android.material.textview.MaterialTextView
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class AnalyzerActivity : AppCompatActivity() {

    private lateinit var tabLayout: TabLayout
    private lateinit var contentContainer: LinearLayout
    private lateinit var progressBar: ProgressBar
    private lateinit var fileSystemManager: FileSystemManager
    private lateinit var parser: ELFParser
    private var filePath = ""
    private var fileName = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_analyzer)

        filePath = intent.getStringExtra("FILE_PATH") ?: ""
        fileName = intent.getStringExtra("FILE_NAME") ?: ""

        fileSystemManager = FileSystemManager(this)
        parser = ELFParser(filePath)

        setupUI()
        loadAnalysis()
    }

    private fun setupUI() {
        val titleView: MaterialTextView = findViewById(R.id.analyzerTitle)
        titleView.text = fileName

        tabLayout = findViewById(R.id.tabLayout)
        contentContainer = findViewById(R.id.contentContainer)
        progressBar = findViewById(R.id.progressBar)

        tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab) {
                showTabContent(tab.position)
            }

            override fun onTabUnselected(tab: TabLayout.Tab) {}
            override fun onTabReselected(tab: TabLayout.Tab) {}
        })
    }

    private fun loadAnalysis() {
        GlobalScope.launch {
            val analysis = withContext(Dispatchers.IO) {
                parser.parse()
            }

            withContext(Dispatchers.Main) {
                progressBar.visibility = android.view.View.GONE
                if (analysis != null) {
                    showTabContent(0) // Show code tab by default
                } else {
                    Toast.makeText(this@AnalyzerActivity, "Failed to parse file", Toast.LENGTH_SHORT)
                        .show()
                }
            }
        }
    }

    private fun showTabContent(tabIndex: Int) {
        contentContainer.removeAllViews()

        GlobalScope.launch {
            val analysis = withContext(Dispatchers.IO) {
                parser.parse()
            }

            withContext(Dispatchers.Main) {
                if (analysis == null) return@withContext

                when (tabIndex) {
                    0 -> showCodeView(analysis.code)
                    1 -> showSectionsView(analysis.sections)
                    2 -> showSymbolsView(analysis.symbols)
                    3 -> showStringsView(analysis.strings)
                    4 -> showSearchView(analysis.code)
                }
            }
        }
    }

    private fun showCodeView(code: List<CodeInstruction>) {
        val recyclerView = RecyclerView(this)
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = CodeAdapter(code)
        contentContainer.addView(
            recyclerView,
            LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )
        )
    }

    private fun showSectionsView(sections: List<ELFSection>) {
        val recyclerView = RecyclerView(this)
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = SectionAdapter(sections)
        contentContainer.addView(
            recyclerView,
            LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )
        )
    }

    private fun showSymbolsView(symbols: List<ELFSymbol>) {
        val recyclerView = RecyclerView(this)
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = SymbolAdapter(symbols)
        contentContainer.addView(
            recyclerView,
            LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )
        )
    }

    private fun showStringsView(strings: List<String>) {
        val recyclerView = RecyclerView(this)
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = StringAdapter(strings)
        contentContainer.addView(
            recyclerView,
            LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )
        )
    }

    private fun showSearchView(code: List<CodeInstruction>) {
        val container = LinearLayout(this)
        container.orientation = LinearLayout.VERTICAL
        container.layoutParams = LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.MATCH_PARENT
        )

        val searchInput = EditText(this)
        searchInput.hint = "Search..."
        searchInput.layoutParams = LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        ).apply { setMargins(16, 16, 16, 16) }

        val searchButton = MaterialButton(this)
        searchButton.text = "Search"
        searchButton.layoutParams = LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        ).apply { setMargins(16, 0, 16, 16) }

        val resultsRecycler = RecyclerView(this)
        resultsRecycler.layoutManager = LinearLayoutManager(this)
        resultsRecycler.layoutParams = LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            0,
            1f
        )

        var currentResults = listOf<CodeInstruction>()

        searchButton.setOnClickListener {
            val query = searchInput.text.toString()
            if (query.isNotEmpty()) {
                currentResults = parser.search(query)
                resultsRecycler.adapter = CodeAdapter(currentResults)
            }
        }

        container.addView(searchInput)
        container.addView(searchButton)
        container.addView(resultsRecycler)
        contentContainer.addView(container)
    }
}

class CodeAdapter(private val instructions: List<CodeInstruction>) :
    RecyclerView.Adapter<CodeAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_code, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(instructions[position])
    }

    override fun getItemCount() = instructions.size

    inner class ViewHolder(itemView: android.view.View) :
        RecyclerView.ViewHolder(itemView) {
        private val addressView: MaterialTextView = itemView.findViewById(R.id.address)
        private val instructionView: MaterialTextView = itemView.findViewById(R.id.instruction)
        private val operandsView: MaterialTextView = itemView.findViewById(R.id.operands)

        fun bind(instruction: CodeInstruction) {
            addressView.text = instruction.address
            instructionView.text = instruction.instruction
            operandsView.text = instruction.operands.joinToString(", ")
        }
    }
}

class SectionAdapter(private val sections: List<ELFSection>) :
    RecyclerView.Adapter<SectionAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_section, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(sections[position])
    }

    override fun getItemCount() = sections.size

    inner class ViewHolder(itemView: android.view.View) :
        RecyclerView.ViewHolder(itemView) {
        private val nameView: MaterialTextView = itemView.findViewById(R.id.sectionName)
        private val detailView: MaterialTextView = itemView.findViewById(R.id.sectionDetail)

        fun bind(section: ELFSection) {
            nameView.text = section.name
            detailView.text = "Address: 0x${section.address.toString(16)} | Size: ${section.size} bytes"
        }
    }
}

class SymbolAdapter(private val symbols: List<ELFSymbol>) :
    RecyclerView.Adapter<SymbolAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_symbol, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(symbols[position])
    }

    override fun getItemCount() = symbols.size

    inner class ViewHolder(itemView: android.view.View) :
        RecyclerView.ViewHolder(itemView) {
        private val nameView: MaterialTextView = itemView.findViewById(R.id.symbolName)
        private val detailView: MaterialTextView = itemView.findViewById(R.id.symbolDetail)

        fun bind(symbol: ELFSymbol) {
            nameView.text = symbol.name
            detailView.text = "Type: ${symbol.type} | Binding: ${symbol.binding}"
        }
    }
}

class StringAdapter(private val strings: List<String>) :
    RecyclerView.Adapter<StringAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_string, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(strings[position])
    }

    override fun getItemCount() = strings.size

    inner class ViewHolder(itemView: android.view.View) :
        RecyclerView.ViewHolder(itemView) {
        private val stringView: MaterialTextView = itemView.findViewById(R.id.stringContent)

        fun bind(string: String) {
            stringView.text = string
        }
    }
}
