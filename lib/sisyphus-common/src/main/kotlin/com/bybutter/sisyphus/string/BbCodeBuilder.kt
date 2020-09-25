package com.bybutter.sisyphus.string

class BbCodeBuilder {
    private val builder: StringBuilder = StringBuilder()

    fun ln(): BbCodeBuilder {
        builder.appendln()
        return this
    }

    fun text(value: String): BbCodeBuilder {
        builder.append(value)
        return this
    }

    fun textLn(value: String): BbCodeBuilder {
        builder.appendln(value)
        return this
    }

    fun b(value: String): BbCodeBuilder {
        return bold(value)
    }

    fun bold(value: String): BbCodeBuilder {
        text("[b]")
        text(value)
        return text("[/b]")
    }

    fun b(block: BbCodeBuilder.() -> Unit): BbCodeBuilder {
        return bold(block)
    }

    fun bold(block: BbCodeBuilder.() -> Unit): BbCodeBuilder {
        text("[b]")
        block()
        return text("[/b]")
    }

    fun i(value: String): BbCodeBuilder {
        return italic(value)
    }

    fun italic(value: String): BbCodeBuilder {
        text("[i]")
        text(value)
        return text("[/i]")
    }

    fun i(block: BbCodeBuilder.() -> Unit): BbCodeBuilder {
        return italic(block)
    }

    fun italic(block: BbCodeBuilder.() -> Unit): BbCodeBuilder {
        text("[i]")
        block()
        return text("[/i]")
    }

    fun u(value: String): BbCodeBuilder {
        return underscore(value)
    }

    fun underscore(value: String): BbCodeBuilder {
        text("[u]")
        text(value)
        return text("[/u]")
    }

    fun u(block: BbCodeBuilder.() -> Unit): BbCodeBuilder {
        return underscore(block)
    }

    fun underscore(block: BbCodeBuilder.() -> Unit): BbCodeBuilder {
        text("[u]")
        block()
        return text("[/u]")
    }

    fun url(url: String, value: String? = null): BbCodeBuilder {
        if (value == null) {
            text("[url]")
            text(url)
        } else {
            text("[url=$url]")
            text(value)
        }
        return text("[/url]")
    }

    fun url(url: String, block: BbCodeBuilder.() -> Unit): BbCodeBuilder {
        text("[url=$url]")
        block()
        return text("[/url]")
    }

    fun img(url: String): BbCodeBuilder {
        return image(url)
    }

    fun image(url: String): BbCodeBuilder {
        text("[img]")
        text(url)
        return text("[/img]")
    }

    fun size(size: Int, value: String): BbCodeBuilder {
        text("[size=$size]")
        text(value)
        return text("[/size]")
    }

    fun size(size: Int, block: BbCodeBuilder.() -> Unit): BbCodeBuilder {
        text("[size=$size]")
        block()
        return text("[/size]")
    }

    fun color(color: String, value: String): BbCodeBuilder {
        text("[color=$color]")
        text(value)
        return text("[/color]")
    }

    fun color(color: String, block: BbCodeBuilder.() -> Unit): BbCodeBuilder {
        text("[color=$color]")
        block()
        return text("[/color]")
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
