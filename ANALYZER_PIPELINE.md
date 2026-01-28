# SO Analyzer - Analysis Pipeline Documentation

## Overview

The SO Analyzer pipeline transforms raw ELF binary files into human-readable, interactive analysis views. This document details each stage of the analysis process, from file selection to interactive exploration.

## Pipeline Architecture

```
┌─────────────────────────────────────────────────────────────┐
│                     User Selects .so File                   │
└────────────────────────┬────────────────────────────────────┘
                         │
                         ▼
┌─────────────────────────────────────────────────────────────┐
│              Stage 1: File Validation & Loading             │
│  - Verify file exists and is readable                       │
│  - Read file bytes into memory                              │
│  - Check ELF magic number (0x7F 'E' 'L' 'F')               │
└────────────────────────┬────────────────────────────────────┘
                         │
                         ▼
┌─────────────────────────────────────────────────────────────┐
│           Stage 2: ELF Header Parsing (Native)              │
│  - Parse ELF header (64 bytes)                              │
│  - Extract architecture info (32-bit vs 64-bit)             │
│  - Determine byte order (little-endian vs big-endian)       │
│  - Identify OS/ABI                                          │
└────────────────────────┬────────────────────────────────────┘
                         │
                         ▼
┌─────────────────────────────────────────────────────────────┐
│        Stage 3: Section & Program Header Parsing            │
│  - Parse section headers (.text, .rodata, .data, etc.)      │
│  - Parse program headers (loadable segments)                │
│  - Build section map                                        │
│  - Calculate addresses and offsets                          │
└────────────────────────┬────────────────────────────────────┘
                         │
                         ▼
┌─────────────────────────────────────────────────────────────┐
│        Stage 4: Symbol & Dynamic Information Extraction     │
│  - Parse .dynsym section (dynamic symbols)                  │
│  - Parse .symtab section (static symbols)                   │
│  - Extract function names and addresses                     │
│  - Parse relocation entries (.rel.dyn, .rel.plt)            │
│  - Identify JNI methods                                     │
└────────────────────────┬────────────────────────────────────┘
                         │
                         ▼
┌─────────────────────────────────────────────────────────────┐
│         Stage 5: String & Constant Extraction               │
│  - Extract null-terminated strings from .rodata             │
│  - Extract string table (.strtab, .dynstr)                  │
│  - Parse literal pools and constants                        │
│  - Build string index for fast lookup                       │
└────────────────────────┬────────────────────────────────────┘
                         │
                         ▼
┌─────────────────────────────────────────────────────────────┐
│     Stage 6: Disassembly & Code Analysis (Native/Capstone)  │
│  - Disassemble .text section (ARM64, ARM32, x86, etc.)      │
│  - Generate instruction stream                              │
│  - Identify function boundaries                             │
│  - Parse control flow (branches, calls, returns)            │
│  - Build basic block map                                    │
└────────────────────────┬────────────────────────────────────┘
                         │
                         ▼
┌─────────────────────────────────────────────────────────────┐
│         Stage 7: Cross-Reference & Analysis                 │
│  - Link symbols to code locations                           │
│  - Cross-reference strings in code                          │
│  - Identify function calls and imports                      │
│  - Build dependency graph                                   │
│  - Detect patterns (license checks, encryption, etc.)       │
└────────────────────────┬────────────────────────────────────┘
                         │
                         ▼
┌─────────────────────────────────────────────────────────────┐
│      Stage 8: UI Rendering & Interactive Exploration        │
│  - Display code view with syntax highlighting               │
│  - Show sections with jump-to functionality                 │
│  - List symbols with address linking                        │
│  - Display strings with reference highlighting             │
│  - Enable search with result navigation                     │
└────────────────────────┬────────────────────────────────────┘
                         │
                         ▼
┌─────────────────────────────────────────────────────────────┐
│                  User Interacts with Analysis                │
│  - Click symbol → Jump to code location                     │
│  - Click string → Highlight all references                 │
│  - Search → Navigate between matches                        │
│  - Scroll → Smooth navigation through large binaries        │
└─────────────────────────────────────────────────────────────┘
```

## Stage 1: File Validation & Loading

### Input
- File path: `/storage/emulated/0/example.so`

### Process

```kotlin
fun loadFile(filePath: String): ByteArray? {
    return try {
        val file = File(filePath)
        
        // Verify file exists
        if (!file.exists()) {
            Log.e("FileLoader", "File not found: $filePath")
            return null
        }
        
        // Verify readable
        if (!file.canRead()) {
            Log.e("FileLoader", "File not readable: $filePath")
            return null
        }
        
        // Verify is file (not directory)
        if (!file.isFile) {
            Log.e("FileLoader", "Not a file: $filePath")
            return null
        }
        
        // Read bytes
        file.readBytes()
    } catch (e: Exception) {
        Log.e("FileLoader", "Error reading file", e)
        null
    }
}
```

### Output
- Raw bytes: `ByteArray` containing entire file content
- Metadata: File size, modification date, path

### Performance
- Time: O(1) - single file read
- Memory: O(n) where n = file size
- Optimization: Async I/O via Coroutines

## Stage 2: ELF Header Parsing

### Input
- Raw bytes (first 64 bytes)

### ELF Header Structure (64-bit)

```
Offset  Size  Field           Description
0       4     e_ident[0:3]    Magic number (0x7F 'E' 'L' 'F')
4       1     e_ident[4]      Class (1=32-bit, 2=64-bit)
5       1     e_ident[5]      Data (1=little-endian, 2=big-endian)
6       1     e_ident[6]      Version
7       1     e_ident[7]      OS/ABI
8       1     e_ident[8]      ABI Version
16      2     e_type          Type (2=executable, 3=shared object)
18      2     e_machine       Machine (0xB7=ARM64, 0x28=ARM, 0x3E=x86-64)
20      4     e_version       Version
24      8     e_entry         Entry point address
32      8     e_phoff         Program header offset
40      8     e_shoff         Section header offset
48      4     e_flags         Flags
56      2     e_ehsize        ELF header size
58      2     e_phentsize     Program header size
60      2     e_phnum         Program header count
62      2     e_shentsize     Section header size
64      2     e_shnum         Section header count
66      2     e_shstrndx      String table index
```

### Process

```kotlin
data class ELFHeader(
    val magic: String,
    val elfClass: Int,          // 1 or 2
    val dataEncoding: Int,      // 1 or 2
    val version: Int,
    val osABI: Int,
    val abiVersion: Int,
    val type: Int,
    val machine: Int,
    val entry: Long,
    val programHeaderOffset: Long,
    val sectionHeaderOffset: Long,
    val flags: Long,
    val headerSize: Int,
    val programHeaderSize: Int,
    val programHeaderCount: Int,
    val sectionHeaderSize: Int,
    val sectionHeaderCount: Int,
    val stringTableIndex: Int
)

fun parseELFHeader(bytes: ByteArray): ELFHeader? {
    if (bytes.size < 64) return null
    
    // Validate magic
    if (bytes[0] != 0x7F.toByte() || 
        bytes[1] != 'E'.code.toByte() ||
        bytes[2] != 'L'.code.toByte() ||
        bytes[3] != 'F'.code.toByte()) {
        return null
    }
    
    return ELFHeader(
        magic = "0x7F454C46",
        elfClass = bytes[4].toInt(),
        dataEncoding = bytes[5].toInt(),
        version = bytes[6].toInt(),
        osABI = bytes[7].toInt(),
        abiVersion = bytes[8].toInt(),
        type = readHalfWord(bytes, 16),
        machine = readHalfWord(bytes, 18),
        entry = readWord(bytes, 24),
        programHeaderOffset = readWord(bytes, 32),
        sectionHeaderOffset = readWord(bytes, 40),
        flags = readWord(bytes, 48),
        headerSize = readHalfWord(bytes, 56),
        programHeaderSize = readHalfWord(bytes, 58),
        programHeaderCount = readHalfWord(bytes, 60),
        sectionHeaderSize = readHalfWord(bytes, 62),
        sectionHeaderCount = readHalfWord(bytes, 64),
        stringTableIndex = readHalfWord(bytes, 66)
    )
}
```

### Output
- ELFHeader object with all fields
- Architecture info: 32/64-bit, ARM/x86, endianness
- Offsets to sections and program headers

### Performance
- Time: O(1) - fixed 64-byte read
- Memory: O(1) - small header structure

## Stage 3: Section & Program Header Parsing

### Input
- Raw bytes
- ELF header with offsets and counts

### Section Header Structure

```
Offset  Size  Field           Description
0       4     sh_name         Name offset in .shstrtab
4       4     sh_type         Type (1=PROGBITS, 8=NOBITS, 11=DYNSYM)
8       8     sh_flags        Flags (1=WRITE, 2=ALLOC, 4=EXECINSTR)
16      8     sh_addr         Virtual address
24      8     sh_offset       File offset
32      8     sh_size         Section size
40      4     sh_link         Link to related section
44      4     sh_info         Additional info
48      8     sh_addralign    Alignment
56      8     sh_entsize      Entry size (for symbol tables)
```

### Process

```kotlin
data class Section(
    val name: String,
    val type: Int,
    val flags: Long,
    val address: Long,
    val offset: Long,
    val size: Long,
    val link: Int,
    val info: Int,
    val alignment: Long,
    val entrySize: Long
)

fun parseSectionHeaders(
    bytes: ByteArray,
    header: ELFHeader
): List<Section> {
    val sections = mutableListOf<Section>()
    
    for (i in 0 until header.sectionHeaderCount) {
        val offset = header.sectionHeaderOffset + 
                     (i * header.sectionHeaderSize)
        
        sections.add(Section(
            name = "", // Will be filled from .shstrtab
            type = readWord(bytes, offset + 4).toInt(),
            flags = readWord(bytes, offset + 8),
            address = readWord(bytes, offset + 16),
            offset = readWord(bytes, offset + 24),
            size = readWord(bytes, offset + 32),
            link = readWord(bytes, offset + 40).toInt(),
            info = readWord(bytes, offset + 44).toInt(),
            alignment = readWord(bytes, offset + 48),
            entrySize = readWord(bytes, offset + 56)
        ))
    }
    
    return sections
}
```

### Key Sections

| Section | Type | Purpose |
|---------|------|---------|
| .text | PROGBITS | Executable code |
| .rodata | PROGBITS | Read-only data (strings, constants) |
| .data | PROGBITS | Initialized data |
| .bss | NOBITS | Uninitialized data |
| .dynsym | DYNSYM | Dynamic symbols |
| .symtab | SYMTAB | Static symbols |
| .dynstr | STRTAB | Dynamic string table |
| .strtab | STRTAB | Static string table |
| .rel.dyn | REL | Dynamic relocations |
| .rel.plt | REL | PLT relocations |

### Output
- List of Section objects
- Section map for fast lookup
- Identified key sections (.text, .rodata, .dynsym, etc.)

### Performance
- Time: O(n) where n = section count (typically 20-50)
- Memory: O(n) - one Section object per section

## Stage 4: Symbol & Dynamic Information Extraction

### Input
- Raw bytes
- Section headers (especially .dynsym and .symtab)

### Symbol Entry Structure

```
Offset  Size  Field           Description
0       4     st_name         Name offset in string table
4       1     st_info         Type and binding info
5       1     st_other        Visibility
6       2     st_shndx        Section index
8       8     st_value        Symbol value (address)
16      8     st_size         Symbol size
```

### Process

```kotlin
data class Symbol(
    val name: String,
    val type: Int,              // 0=NOTYPE, 1=OBJECT, 2=FUNC, etc.
    val binding: Int,           // 0=LOCAL, 1=GLOBAL, 2=WEAK
    val visibility: Int,        // 0=DEFAULT, 1=INTERNAL, 2=HIDDEN, 3=PROTECTED
    val shndx: Int,
    val value: Long,            // Address
    val size: Long
)

fun extractSymbols(
    bytes: ByteArray,
    sections: List<Section>
): List<Symbol> {
    val symbols = mutableListOf<Symbol>()
    
    // Find .dynsym section
    val dynSymSection = sections.find { it.type == 11 } ?: return symbols
    
    val symbolCount = dynSymSection.size / dynSymSection.entrySize
    
    for (i in 0 until symbolCount) {
        val offset = dynSymSection.offset + (i * dynSymSection.entrySize)
        
        val info = bytes[offset + 4].toInt()
        val type = info and 0x0F
        val binding = (info shr 4) and 0x0F
        
        symbols.add(Symbol(
            name = "", // Will be filled from string table
            type = type,
            binding = binding,
            visibility = bytes[offset + 5].toInt() and 0x03,
            shndx = readHalfWord(bytes, offset + 6),
            value = readWord(bytes, offset + 8),
            size = readWord(bytes, offset + 16)
        ))
    }
    
    return symbols
}
```

### Symbol Types

| Type | Meaning |
|------|---------|
| 0 | NOTYPE (no type) |
| 1 | OBJECT (variable) |
| 2 | FUNC (function) |
| 3 | SECTION (section) |
| 4 | FILE (source file) |
| 5 | COMMON (common symbol) |
| 6 | TLS (thread-local) |

### Symbol Binding

| Binding | Meaning |
|---------|---------|
| 0 | LOCAL (internal) |
| 1 | GLOBAL (exported) |
| 2 | WEAK (optional) |

### Output
- List of Symbol objects
- Symbol map indexed by address
- Identified functions and variables
- JNI method detection

### Performance
- Time: O(n) where n = symbol count (typically 100-1000)
- Memory: O(n) - one Symbol object per symbol

## Stage 5: String & Constant Extraction

### Input
- Raw bytes
- Section headers (especially .rodata and .dynstr)

### Process

```kotlin
fun extractStrings(bytes: ByteArray): List<String> {
    val strings = mutableListOf<String>()
    var current = StringBuilder()
    
    for (byte in bytes) {
        when {
            byte in 32..126 -> current.append(byte.toInt().toChar())
            byte == 0.toByte() && current.length >= 4 -> {
                strings.add(current.toString())
                current = StringBuilder()
            }
            else -> current = StringBuilder()
        }
    }
    
    return strings.distinct()
}
```

### String Categories

1. **Embedded Strings**: Null-terminated ASCII in .rodata
   - Error messages
   - Log strings
   - Format strings
   - Library names

2. **String Table**: .dynstr and .strtab sections
   - Symbol names
   - Section names
   - File names

3. **Literal Pools**: Constants in .rodata
   - IP addresses
   - Magic numbers
   - URLs

### Output
- List of extracted strings
- String index for fast lookup
- String location map (address → string)

### Performance
- Time: O(n) where n = binary size
- Memory: O(m) where m = unique strings
- Optimization: Native C++ for speed

## Stage 6: Disassembly & Code Analysis

### Input
- Raw bytes
- .text section offset and size
- Architecture info (ARM64, ARM32, x86, etc.)

### Process (Capstone Integration)

```cpp
// Pseudo-code for Capstone integration
#include <capstone/capstone.h>

jobjectArray disassembleARM64(JNIEnv *env, jbyteArray code, jlong address) {
    csh handle;
    cs_arch arch = CS_ARCH_ARM64;
    cs_mode mode = CS_MODE_ARM;
    
    if (cs_open(arch, mode, &handle) != CS_ERR_OK) {
        return nullptr;
    }
    
    cs_insn *insn;
    size_t count = cs_disasm(handle, code, code_len, address, 0, &insn);
    
    // Convert to Java objects
    jobjectArray result = env->NewObjectArray(count, stringClass, nullptr);
    
    for (size_t i = 0; i < count; i++) {
        jstring instr = env->NewStringUTF(insn[i].mnemonic);
        env->SetObjectArrayElement(result, i, instr);
    }
    
    cs_free(insn, count);
    cs_close(&handle);
    
    return result;
}
```

### Fallback (Kotlin Mock)

```kotlin
fun disassembleARM64Fallback(bytes: ByteArray, address: Long): List<CodeInstruction> {
    val instructions = mutableListOf<CodeInstruction>()
    
    var i = 0
    var currentAddress = address
    
    while (i < bytes.size - 3) {
        val word = readWord(bytes, i).toInt()
        
        val instruction = when {
            word == 0x52800020 -> "mov w0, #1"
            word == 0xd65f03c0 -> "ret"
            word == 0xa9bf7bfd -> "stp x29, x30, [sp, #-16]!"
            else -> "unknown"
        }
        
        instructions.add(CodeInstruction(
            address = "0x${currentAddress.toString(16)}",
            bytes = String.format("%08x", word),
            instruction = instruction.split(" ")[0],
            operands = instruction.split(" ").drop(1)
        ))
        
        currentAddress += 4
        i += 4
    }
    
    return instructions
}
```

### Output
- List of CodeInstruction objects
- Instruction map indexed by address
- Function boundaries identified
- Control flow graph (future)

### Performance
- Time: O(n) where n = code size
- Memory: O(n) - one instruction per 4 bytes
- Optimization: Native Capstone for speed

## Stage 7: Cross-Reference & Analysis

### Input
- Symbols, strings, instructions
- Section information

### Process

```kotlin
fun buildCrossReferences(
    symbols: List<Symbol>,
    strings: List<String>,
    instructions: List<CodeInstruction>
): CrossReferenceMap {
    val xrefs = mutableMapOf<Long, MutableList<XRef>>()
    
    // Link symbols to code
    for (symbol in symbols) {
        if (symbol.type == 2) { // FUNC
            xrefs.getOrPut(symbol.value) { mutableListOf() }
                .add(XRef(symbol.name, XRefType.SYMBOL))
        }
    }
    
    // Link strings to code
    for ((addr, instr) in instructions.withIndex()) {
        for (string in strings) {
            if (instr.instruction.contains(string)) {
                xrefs.getOrPut(addr.toLong()) { mutableListOf() }
                    .add(XRef(string, XRefType.STRING))
            }
        }
    }
    
    return CrossReferenceMap(xrefs)
}
```

### Analysis Types

1. **Function Analysis**
   - Entry points
   - Call graph
   - Stack frame size

2. **String Analysis**
   - String references
   - Format strings
   - Error messages

3. **Pattern Detection**
   - License checks
   - Encryption routines
   - JNI calls

### Output
- Cross-reference map
- Function call graph
- String reference index
- Pattern detections

## Stage 8: UI Rendering & Interactive Exploration

### Input
- All analysis results
- User interaction events

### Code View

```kotlin
fun displayCodeView(instructions: List<CodeInstruction>) {
    val adapter = CodeAdapter(instructions)
    recyclerView.adapter = adapter
    
    // Syntax highlighting
    instructions.forEach { instr ->
        when {
            instr.instruction in ARM64_BRANCH_INSTRUCTIONS -> {
                // Highlight branches in red
            }
            instr.instruction in ARM64_LOAD_INSTRUCTIONS -> {
                // Highlight loads in blue
            }
            instr.instruction in ARM64_STORE_INSTRUCTIONS -> {
                // Highlight stores in green
            }
        }
    }
}
```

### Interactive Features

1. **Symbol Navigation**
   - Click symbol → Jump to address
   - Show all references

2. **String Highlighting**
   - Click string → Highlight all uses
   - Navigate between references

3. **Search**
   - Search instructions, registers, strings
   - Navigate between results
   - Filter by type

### Performance Optimizations

- RecyclerView for efficient rendering
- ViewHolder pattern for item reuse
- DiffUtil for list updates
- Smooth scrolling with predictive loading

## Performance Metrics

| Stage | Time | Memory | Scalability |
|-------|------|--------|-------------|
| File Loading | O(1) | O(n) | Limited by RAM |
| ELF Header | O(1) | O(1) | N/A |
| Sections | O(n) | O(n) | n ≈ 50 |
| Symbols | O(n) | O(n) | n ≈ 1000 |
| Strings | O(n) | O(m) | n ≈ file size |
| Disassembly | O(n) | O(n) | n ≈ code size |
| Cross-refs | O(n²) | O(n) | Optimizable |
| UI Rendering | O(n) | O(n) | Lazy loading |

## Optimization Strategies

### For Large Binaries (100MB+)

1. **Lazy Loading**
   - Load sections on demand
   - Disassemble visible range only
   - Cache parsed results

2. **Memory Mapping**
   - Use mmap for large files
   - Avoid loading entire file into RAM
   - Stream processing

3. **Parallel Processing**
   - Parse sections in parallel
   - Disassemble in background threads
   - Incremental UI updates

### Example: Lazy Disassembly

```kotlin
class LazyDisassembler(
    private val bytes: ByteArray,
    private val textSection: Section
) {
    private val cache = mutableMapOf<Long, CodeInstruction>()
    
    fun getInstructionsInRange(start: Long, end: Long): List<CodeInstruction> {
        val result = mutableListOf<CodeInstruction>()
        
        for (addr in start until end step 4) {
            cache.getOrPut(addr) {
                disassembleAt(addr)
            }.let { result.add(it) }
        }
        
        return result
    }
    
    private fun disassembleAt(address: Long): CodeInstruction {
        // Disassemble single instruction
        // Cache result
        // Return
    }
}
```

## Future Enhancements

1. **Advanced Analysis**
   - Control flow graphs
   - Data flow analysis
   - Vulnerability detection

2. **Visualization**
   - Interactive graphs
   - 3D dependency visualization
   - Timeline analysis

3. **Collaboration**
   - Shared annotations
   - Comment system
   - Export/import analysis

## References

- [Capstone Disassembly Engine](http://www.capstone-engine.org/)
- [ELF Format Specification](https://refspecs.linuxbase.org/elf/gabi4+/)
- [ARM64 ISA Reference](https://developer.arm.com/documentation/ddi0602/latest/)
- [Android NDK Guide](https://developer.android.com/ndk/guides)
