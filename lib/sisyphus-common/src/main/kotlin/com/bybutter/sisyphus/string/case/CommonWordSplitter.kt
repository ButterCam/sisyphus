package com.bybutter.sisyphus.string.case

import com.bybutter.sisyphus.string.getOrZero

object CommonWordSplitter : WordSplitter {
    override fun split(string: CharSequence): List<String> {
        return entryPoint(string)
    }

    private fun entryPoint(
        string: CharSequence,
        pos: Int = 0,
        stack: StringBuilder = StringBuilder(),
        result: MutableList<String> = mutableListOf(),
    ): List<String> {
        result.append(stack)

        if (pos >= string.length) {
            return result
        }
        val ch = string[pos]
        return when {
            ch.isDigit() -> {
                handleDigital(string, pos, stack, result)
            }
            ch.isLowerCase() -> {
                handleLowerCase(string, pos, stack, result)
            }
            ch.isUpperCase() -> {
                handleUpperCase(string, pos, stack, result)
            }
            ch.isDelimiter() -> {
                handleDelimiter(string, pos, stack, result)
            }
            else -> {
                handleUnknown(string, pos, stack, result)
            }
        }
    }

    private fun handleDigital(
        string: CharSequence,
        pos: Int,
        stack: StringBuilder,
        result: MutableList<String>,
    ): List<String> {
        var index = pos
        val digital = StringBuilder()
        digital.append(string[index++])

        while (true) {
            val ch = string.getOrZero(index)
            when {
                ch.isDigit() -> {
                    digital.append(ch)
                    index++
                }
                ch.isUpperCase() -> {
                    digital.append(ch)
                    index++
                    return handleUpperCaseAfterDigital(string, index, stack, digital, result)
                }
                ch.isLowerCase() -> {
                    digital.append(ch)
                    index++

                    val lastCh = stack.getOrZero(stack.lastIndex)
                    return if (lastCh.isUpperCase()) {
                        result.append(stack)
                        handleLowerCase(string, index, digital, result)
                    } else {
                        stack.append(digital)
                        handleLowerCase(string, index, stack, result)
                    }
                }
                else -> {
                    stack.append(digital)
                    return entryPoint(string, index, stack, result)
                }
            }
        }
    }

    private fun handleLowerCase(
        string: CharSequence,
        pos: Int,
        stack: StringBuilder,
        result: MutableList<String>,
    ): List<String> {
        var index = pos

        while (true) {
            val ch = string.getOrZero(index)
            when {
                ch.isLowerCase() -> {
                    stack.append(ch)
                    index++
                }
                ch.isDigit() -> {
                    return handleDigital(string, index, stack, result)
                }
                else -> {
                    return entryPoint(string, index, stack, result)
                }
            }
        }
    }

    private fun handleUpperCase(
        string: CharSequence,
        pos: Int,
        stack: StringBuilder,
        result: MutableList<String>,
    ): List<String> {
        var index = pos

        while (true) {
            val ch = string.getOrZero(index)
            when {
                ch.isUpperCase() -> {
                    stack.append(ch)
                    index++
                }
                ch.isLowerCase() -> {
                    val last = stack.last()
                    stack.deleteCharAt(stack.length - 1)
                    result.append(stack)
                    stack.append(last)
                    return handleLowerCase(string, index, stack, result)
                }
                ch.isDigit() -> {
                    return handleDigital(string, index, stack, result)
                }
                else -> {
                    return entryPoint(string, index, stack, result)
                }
            }
        }
    }

    private fun handleUpperCaseAfterDigital(
        string: CharSequence,
        pos: Int,
        stack: StringBuilder,
        digital: StringBuilder,
        result: MutableList<String>,
    ): List<String> {
        var index = pos

        while (true) {
            val ch = string.getOrZero(index)
            when {
                ch.isUpperCase() -> {
                    digital.append(ch)
                    index++
                }
                ch.isLowerCase() -> {
                    val last = digital.last()
                    digital.deleteCharAt(digital.length - 1)

                    if (digital.last().isDigit()) {
                        stack.append(digital)
                        result.append(stack)
                    } else {
                        result.append(stack)
                        result.append(digital)
                    }

                    stack.append(last)
                    return handleLowerCase(string, index, stack, result)
                }
                else -> {
                    if (!digital.last().isDigit()) {
                        result.append(stack)
                    }
                    stack.append(digital)
                    return entryPoint(string, index, stack, result)
                }
            }
        }
    }

    private fun handleUnknown(
        string: CharSequence,
        pos: Int,
        stack: StringBuilder,
        result: MutableList<String>,
    ): List<String> {
        var index = pos

        while (true) {
            val ch = string.getOrZero(index)
            when {
                ch.isUpperCase() || ch.isLowerCase() || ch.isDigit() || ch.isDelimiter() -> {
                    return entryPoint(string, index, stack, result)
                }
                else -> {
                    stack.append(ch)
                    index++
                }
            }
        }
    }

    private fun handleDelimiter(
        string: CharSequence,
        pos: Int,
        stack: StringBuilder,
        result: MutableList<String>,
    ): List<String> {
        var index = pos + 1

        while (true) {
            val ch = string.getOrZero(index)
            when {
                ch.isDelimiter() -> {
                    index++
                }
                else -> {
                    return entryPoint(string, index, stack, result)
                }
            }
        }
    }

    private fun MutableList<String>.append(builder: StringBuilder) {
        if (builder.isEmpty()) {
            return
        }

        add(builder.toString())
        builder.clear()
    }

    private val delimiters = setOf(' ', '_', '-', '.')

    private fun Char.isDelimiter(): Boolean {
        return delimiters.contains(this)
    }
}
