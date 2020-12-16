package com.bybutter.sisyphus.string

class BbCodeBuilder {
    private val builder: StringBuilder = StringBuilder()

    fun tag(value: String, parameter: String, block: BbCodeBuilder.() -> Unit): BbCodeBuilder {
        builder.append('[')
        builder.append(value)
        builder.append('=')
        builder.append(parameter)
        builder.append(']')
        block()
        builder.append("[/")
        builder.append(value)
        builder.append(']')
        return this
    }

    fun tag(value: String, block: BbCodeBuilder.() -> Unit): BbCodeBuilder {
        builder.append('[')
        builder.append(value)
        builder.append(']')
        block()
        builder.append("[/")
        builder.append(value)
        builder.append(']')
        return this
    }

    fun ln(): BbCodeBuilder {
        builder.appendln()
        return this
    }

    private fun escape(value: String): String {
        return value.replace("[", "\\[")
            .replace("]", "\\]")
    }

    fun text(value: String): BbCodeBuilder {
        builder.append(escape(value))
        return this
    }

    fun textLn(value: String): BbCodeBuilder {
        return text(value).ln()
    }

    fun b(value: String): BbCodeBuilder {
        return bold(value)
    }

    fun bold(value: String): BbCodeBuilder {
        return tag("b") {
            text(value)
        }
    }

    fun b(block: BbCodeBuilder.() -> Unit): BbCodeBuilder {
        return bold(block)
    }

    fun bold(block: BbCodeBuilder.() -> Unit): BbCodeBuilder {
        return tag("b", block)
    }

    fun i(value: String): BbCodeBuilder {
        return italic(value)
    }

    fun italic(value: String): BbCodeBuilder {
        return tag("i") {
            text(value)
        }
    }

    fun i(block: BbCodeBuilder.() -> Unit): BbCodeBuilder {
        return italic(block)
    }

    fun italic(block: BbCodeBuilder.() -> Unit): BbCodeBuilder {
        return tag("i", block)
    }

    fun u(value: String): BbCodeBuilder {
        return underscore(value)
    }

    fun underscore(value: String): BbCodeBuilder {
        return tag("u") {
            text(value)
        }
    }

    fun u(block: BbCodeBuilder.() -> Unit): BbCodeBuilder {
        return underscore(block)
    }

    fun underscore(block: BbCodeBuilder.() -> Unit): BbCodeBuilder {
        return tag("u", block)
    }

    fun url(url: String, value: String? = null): BbCodeBuilder {
        return if (value == null) {
            tag("url") {
                text(url)
            }
        } else {
            tag("url", url) {
                text(value)
            }
        }
    }

    fun url(url: String, block: BbCodeBuilder.() -> Unit): BbCodeBuilder {
        return tag("url", url, block)
    }

    fun img(url: String): BbCodeBuilder {
        return image(url)
    }

    fun image(url: String): BbCodeBuilder {
        return tag("img") {
            text(url)
        }
    }

    fun size(size: Int, value: String): BbCodeBuilder {
        return tag("size", size.toString()) {
            text(value)
        }
    }

    fun size(size: Int, block: BbCodeBuilder.() -> Unit): BbCodeBuilder {
        return tag("size", size.toString(), block)
    }

    fun color(color: String, value: String): BbCodeBuilder {
        return tag("color", color) {
            text(value)
        }
    }

    fun color(color: String, block: BbCodeBuilder.() -> Unit): BbCodeBuilder {
        return tag("color", color, block)
    }

    override fun toString(): String {
        return builder.toString()
    }
}

fun bbCode(block: BbCodeBuilder.() -> Unit): String {
    val builder = BbCodeBuilder()
    builder.block()
    return builder.toString()
}
