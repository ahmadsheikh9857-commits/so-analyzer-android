package com.example.soanalyzer.utils

import java.io.File
import java.nio.ByteBuffer
import java.nio.ByteOrder

data class ELFSection(
    val name: String,
    val offset: Long,
    val size: Long,
    val address: Long,
    val type: String
)

data class ELFSymbol(
    val name: String,
    val address: Long,
    val size: Long,
    val type: String,
    val binding: String,
    val offset: Long
)

data class CodeInstruction(
    val address: String,
    val bytes: String,
    val instruction: String,
    val operands: List<String>
)

data class ELFAnalysis(
    val filePath: String,
    val fileName: String,
    val fileSize: Long,
    val sections: List<ELFSection>,
    val symbols: List<ELFSymbol>,
    val strings: List<String>,
    val code: List<CodeInstruction>,
    val lastModified: Long
)

class ELFParser(private val filePath: String) {

    private val file = File(filePath)
    private val bytes: ByteArray? = try {
        file.readBytes()
    } catch (e: Exception) {
        null
    }

    fun parse(): ELFAnalysis? {
        if (bytes == null || !isValidELF()) {
            return null
        }

        return ELFAnalysis(
            filePath = filePath,
            fileName = file.name,
            fileSize = file.length(),
            sections = parseSections(),
            symbols = parseSymbols(),
            strings = extractStrings(),
            code = parseCode(),
            lastModified = file.lastModified()
        )
    }

    private fun isValidELF(): Boolean {
        if (bytes == null || bytes.size < 4) return false
        // ELF magic number: 0x7F 'E' 'L' 'F'
        return bytes[0] == 0x7F.toByte() &&
                bytes[1] == 'E'.code.toByte() &&
                bytes[2] == 'L'.code.toByte() &&
                bytes[3] == 'F'.code.toByte()
    }

    private fun parseSections(): List<ELFSection> {
        // Mock ELF sections for demonstration
        // In production, parse actual ELF header and section table
        return listOf(
            ELFSection(".text", 0x1000, 0x2000, 0x1000, "PROGBITS"),
            ELFSection(".rodata", 0x3000, 0x1000, 0x3000, "PROGBITS"),
            ELFSection(".data", 0x4000, 0x500, 0x4000, "PROGBITS"),
            ELFSection(".bss", 0x4500, 0x300, 0x4500, "NOBITS"),
            ELFSection(".dynsym", 0x5000, 0x400, 0x5000, "DYNSYM"),
            ELFSection(".dynstr", 0x5400, 0x200, 0x5400, "STRTAB")
        )
    }

    private fun parseSymbols(): List<ELFSymbol> {
        // Mock symbols for demonstration
        return listOf(
            ELFSymbol("func_checkLicense", 0x1000, 0x20, "FUNC", "GLOBAL", 0x1000),
            ELFSymbol("func_init", 0x1020, 0x30, "FUNC", "GLOBAL", 0x1020),
            ELFSymbol("func_cleanup", 0x1050, 0x18, "FUNC", "GLOBAL", 0x1050),
            ELFSymbol("func_verify", 0x1068, 0x28, "FUNC", "GLOBAL", 0x1068),
            ELFSymbol("_ZN7android3app14ActivityThread4mainEP7JavaVM", 0x1090, 0x100, "FUNC", "GLOBAL", 0x1090)
        )
    }

    private fun extractStrings(): List<String> {
        // Extract null-terminated ASCII strings from the binary
        val strings = mutableListOf<String>()
        if (bytes == null) return strings

        var current = StringBuilder()
        for (byte in bytes) {
            when {
                byte in 32..126 -> current.append(byte.toInt().toChar())
                byte == 0.toByte() && current.length > 4 -> {
                    strings.add(current.toString())
                    current = StringBuilder()
                }
                else -> current = StringBuilder()
            }
        }

        return strings.distinct().take(100) // Limit to first 100 unique strings
    }

    private fun parseCode(): List<CodeInstruction> {
        // Mock ARM64 disassembly for demonstration
        // In production, use a proper disassembler library or JNI binding
        return listOf(
            CodeInstruction("0x1000", "52800020", "mov", listOf("w0", "#1")),
            CodeInstruction("0x1004", "d65f03c0", "ret", emptyList()),
            CodeInstruction("0x1020", "a9bf7bfd", "stp", listOf("x29", "x30", "[sp, #-16]!")),
            CodeInstruction("0x1024", "910003fd", "mov", listOf("x29", "sp")),
            CodeInstruction("0x1028", "94000001", "bl", listOf("func_init")),
            CodeInstruction("0x102c", "a8c17bfd", "ldp", listOf("x29", "x30", "[sp], #16")),
            CodeInstruction("0x1030", "d65f03c0", "ret", emptyList()),
            CodeInstruction("0x1050", "52800000", "mov", listOf("w0", "#0")),
            CodeInstruction("0x1054", "d65f03c0", "ret", emptyList())
        )
    }

    fun search(query: String, filterType: String? = null): List<CodeInstruction> {
        val code = parseCode()
        val lowerQuery = query.lowercase()

        return code.filter { instr ->
            when (filterType) {
                "instructions" -> instr.instruction.lowercase().contains(lowerQuery)
                "registers" -> instr.operands.any { it.lowercase().contains(lowerQuery) }
                else -> instr.instruction.lowercase().contains(lowerQuery) ||
                        instr.operands.any { it.lowercase().contains(lowerQuery) }
            }
        }
    }
}
