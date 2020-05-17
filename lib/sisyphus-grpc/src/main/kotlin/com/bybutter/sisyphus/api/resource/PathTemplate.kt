package com.bybutter.sisyphus.api.resource

import java.io.UnsupportedEncodingException
import java.net.URLDecoder
import java.net.URLEncoder

/**
 * Represents a path template.
 *
 *
 *
 * Templates use the syntax of the API platform; see the protobuf of HttpRule for details. A
 * template consists of a sequence of literals, wildcards, and variable bindings, where each binding
 * can have a sub-path. A string representation can be parsed into an instance of
 * [PathTemplate], which can then be used to perform matching and instantiation.
 *
 *
 *
 * Matching and instantiation deals with unescaping and escaping using URL encoding rules. For
 * example, if a template variable for a single segment is instantiated with a string like
 * `"a/b"`, the slash will be escaped to `"%2f"`. (Note that slash will not be escaped
 * for a multiple-segment variable, but other characters will). The literals in the template itself
 * are *not* escaped automatically, and must be already URL encoded.
 *
 *
 *
 * Here is an example for a template using simple variables:
 *
 * <pre>
 * PathTemplate template = PathTemplate.create("v1/shelves/{shelf}/books/{book}");
 * assert template.matches("v2/shelves") == false;
 * Map&lt;String, String&gt; values = template.match("v1/shelves/s1/books/b1");
 * Map&lt;String, String&gt; expectedValues = new HashMap&lt;&gt;();
 * expectedValues.put("shelf", "s1");
 * expectedValues.put("book", "b1");
 * assert values.equals(expectedValues);
 * assert template.instantiate(values).equals("v1/shelves/s1/books/b1");</pre> *
 *
 * Templates can use variables which match sub-paths. Example:
 *
 * <pre>
 * PathTemplate template = PathTemplate.create("v1/{name=shelves/&#42;&#47;books/&#42;}"};
 * assert template.match("v1/shelves/books/b1") == null;
 * Map&lt;String, String&gt; expectedValues = new HashMap&lt;&gt;();
 * expectedValues.put("name", "shelves/s1/books/b1");
 * assert template.match("v1/shelves/s1/books/b1").equals(expectedValues);
</pre> *
 *
 * Path templates can also be used with only wildcards. Each wildcard is associated with an implicit
 * variable `$n`, where n is the zero-based position of the wildcard. Example:
 *
 * <pre>
 * PathTemplate template = PathTemplate.create("shelves/&#42;&#47;books/&#42;"};
 * assert template.match("shelves/books/b1") == null;
 * Map&lt;String, String&gt; values = template.match("v1/shelves/s1/books/b1");
 * Map&lt;String, String&gt; expectedValues = new HashMap&lt;&gt;();
 * expectedValues.put("$0", s1");
 * expectedValues.put("$1", "b1");
 * assert values.equals(expectedValues);
</pre> *
 *
 * Paths input to matching can use URL relative syntax to indicate a host name by prefixing the host
 * name, as in `//somewhere.io/some/path`. The host name is matched into the special variable
 * [.HOSTNAME_VAR]. Patterns are agnostic about host names, and the same pattern can be used
 * for URL relative syntax and simple path syntax:
 *
 * <pre>
 * PathTemplate template = PathTemplate.create("shelves/&#42;"};
 * Map&lt;String, String&gt; expectedValues = new HashMap&lt;&gt;();
 * expectedValues.put(PathTemplate.HOSTNAME_VAR, "somewhere.io");
 * expectedValues.put("$0", s1");
 * assert template.match("//somewhere.io/shelves/s1").equals(expectedValues);
 * expectedValues.clear();
 * expectedValues.put("$0", s1");
 * assert template.match("shelves/s1").equals(expectedValues);
</pre> *
 *
 * For the representation of a *resource name* see [TemplatedResourceName], which is
 * based on path templates.
 */
class PathTemplate private constructor(
    segments: Iterable<Segment>, // Control use of URL encoding
    private val urlEncoding: Boolean
) {

    // List of segments of this template.
    private val segments: List<Segment>

    // Map from variable names to bindings in the template.
    private val bindings: Map<String, Segment>

    init {
        this.segments = segments.toList()
        if (this.segments.isEmpty()) {
            throw ValidationException("template cannot be empty.")
        }
        val bindings = mutableMapOf<String, Segment>()
        for (seg in this.segments) {
            if (seg.kind == SegmentKind.BINDING) {
                if (bindings.containsKey(seg.value)) {
                    throw ValidationException("Duplicate binding '%s'", seg.value)
                }
                bindings[seg.value] = seg
            }
        }
        this.bindings = bindings.toMap()
    }

    /**
     * Returns the set of variable names used in the template.
     */
    fun vars(): Set<String> {
        return bindings.keys
    }

    /**
     * Returns a template for the parent of this template.
     *
     * @throws ValidationException if the template has no parent.
     */
    fun parentTemplate(): PathTemplate {
        var i = segments.size
        val seg = segments[--i]
        if (seg.kind == SegmentKind.END_BINDING) {
            while (i > 0 && segments[--i].kind != SegmentKind.BINDING) {
            }
        }
        if (i == 0) {
            throw ValidationException("template does not have a parent")
        }
        return PathTemplate(segments.subList(0, i), urlEncoding)
    }

    /**
     * Returns a template where all variable bindings have been replaced by wildcards, but which is
     * equivalent regards matching to this one.
     */
    fun withoutVars(): PathTemplate {
        val result = buildString {
            val iterator = segments.listIterator()
            var start = true
            while (iterator.hasNext()) {
                val seg = iterator.next()
                when (seg.kind) {
                    SegmentKind.BINDING, SegmentKind.END_BINDING -> {
                    }
                    else -> {
                        if (!start) {
                            append(seg.separator())
                        } else {
                            start = false
                        }
                        append(seg.value)
                    }
                }
            }
        }
        return create(result, urlEncoding)
    }

    /**
     * Returns a path template for the sub-path of the given variable. Example:
     *
     * <pre>
     * PathTemplate template = PathTemplate.create("v1/{name=shelves/&#42;&#47;books/&#42;}");
     * assert template.subTemplate("name").toString().equals("shelves/&#42;&#47;books/&#42;");
    </pre> *
     *
     * The returned template will never have named variables, but only wildcards, which are dealt with
     * in matching and instantiation using '$n'-variables. See the documentation of
     * [.match] and [.instantiate], respectively.
     *
     *
     *
     * For a variable which has no sub-path, this returns a path template with a single wildcard
     * ('*').
     *
     * @throws ValidationException if the variable does not exist in the template.
     */
    fun subTemplate(varName: String): PathTemplate {
        val sub = mutableListOf<Segment>()
        var inBinding = false
        for (seg in segments) {
            if (seg.kind == SegmentKind.BINDING && seg.value == varName) {
                inBinding = true
            } else if (inBinding) {
                if (seg.kind == SegmentKind.END_BINDING) {
                    return create(toSyntax(sub, true), urlEncoding)
                } else {
                    sub.add(seg)
                }
            }
        }
        throw ValidationException(
                String.format("Variable '%s' is undefined in template '%s'", varName, this.toRawString()))
    }

    /**
     * Returns true of this template ends with a literal.
     */
    fun endsWithLiteral(): Boolean {
        return segments[segments.size - 1].kind == SegmentKind.LITERAL
    }

    /**
     * Returns true of this template ends with a custom verb.
     */
    fun endsWithCustomVerb(): Boolean {
        return segments[segments.size - 1].kind == SegmentKind.CUSTOM_VERB
    }

    /**
     * Creates a resource name from this template and a path.
     *
     * @throws ValidationException if the path does not match the template.
     */
    fun parse(path: String): TemplatedResourceName {
        return TemplatedResourceName.create(this, path)
    }

    /**
     * Returns the name of a singleton variable used by this template. If the template does not
     * contain a single variable, returns null.
     */
    fun singleVar(): String? {
        if (bindings.size == 1) {
            return bindings.entries.iterator().next().key
        }
        return null
    }

    /**
     * Throws a ValidationException if the template doesn't match the path. The exceptionMessagePrefix
     * parameter will be prepended to the ValidationException message.
     */
    fun validate(path: String, exceptionMessagePrefix: String) {
        if (!matches(path)) {
            throw ValidationException(
                    String.format(
                            "%s: Parameter \"%s\" must be in the form \"%s\"",
                            exceptionMessagePrefix,
                            path,
                            this.toString()))
        }
    }

    /**
     * Matches the path, returning a map from variable names to matched values. All matched values
     * will be properly unescaped using URL encoding rules. If the path does not match the template,
     * throws a ValidationException. The exceptionMessagePrefix parameter will be prepended to the
     * ValidationException message.
     *
     *
     *
     * If the path starts with '//', the first segment will be interpreted as a host name and stored
     * in the variable [HOSTNAME_VAR].
     *
     *
     *
     * See the [PathTemplate] class documentation for examples.
     *
     *
     *
     * For free wildcards in the template, the matching process creates variables named '$n', where
     * 'n' is the wildcard's position in the template (starting at n=0). For example:
     *
     * <pre>
     * PathTemplate template = PathTemplate.create("shelves/&#42;&#47;books/&#42;");
     * Map&lt;String, String&gt; expectedValues = new HashMap&lt;&gt;();
     * expectedValues.put("$0", "s1");
     * expectedValues.put("$1", "b1");
     * assert template.validatedMatch("shelves/s1/books/b2", "User exception string")
     * .equals(expectedValues);
     * expectedValues.clear();
     * expectedValues.put(HOSTNAME_VAR, "somewhere.io");
     * expectedValues.put("$0", "s1");
     * expectedValues.put("$1", "b1");
     * assert template.validatedMatch("//somewhere.io/shelves/s1/books/b2", "User exception string")
     * .equals(expectedValues);
    </pre> *
     *
     * All matched values will be properly unescaped using URL encoding rules (so long as URL encoding
     * has not been disabled by the [.createWithoutUrlEncoding] method).
     */
    fun validatedMatch(path: String, exceptionMessagePrefix: String): Map<String, String> {
        return match(path) ?: throw ValidationException(
                String.format(
                        "%s: Parameter \"%s\" must be in the form \"%s\"",
                        exceptionMessagePrefix,
                        path,
                        toString()))
    }

    /**
     * Returns true if the template matches the path.
     */
    fun matches(path: String): Boolean {
        return match(path) != null
    }

    /**
     * Matches the path, returning a map from variable names to matched values. All matched values
     * will be properly unescaped using URL encoding rules. If the path does not match the template,
     * null is returned.
     *
     *
     *
     * If the path starts with '//', the first segment will be interpreted as a host name and stored
     * in the variable [.HOSTNAME_VAR].
     *
     *
     *
     * See the [PathTemplate] class documentation for examples.
     *
     *
     *
     * For free wildcards in the template, the matching process creates variables named '$n', where
     * 'n' is the wildcard's position in the template (starting at n=0). For example:
     *
     * <pre>
     * PathTemplate template = PathTemplate.create("shelves/&#42;&#47;books/&#42;");
     * Map&lt;String, String&gt; expectedValues = new HashMap&lt;&gt;();
     * expectedValues.put("$0", "s1");
     * expectedValues.put("$1", "b1");
     * assert template.match("shelves/s1/books/b2").equals(expectedValues);
     * expectedValues.clear();
     * expectedValues.put(HOSTNAME_VAR, "somewhere.io");
     * expectedValues.put("$0", "s1");
     * expectedValues.put("$1", "b1");
     * assert template.match("//somewhere.io/shelves/s1/books/b2").equals(expectedValues);
     * </pre> *
     *
     * All matched values will be properly unescaped using URL encoding rules (so long as URL encoding
     * has not been disabled by the [.createWithoutUrlEncoding] method).
     */
    fun match(path: String): Map<String, String>? {
        var path = path
        // Quick check for trailing custom verb.
        val last = segments[segments.size - 1]
        if (last.kind == SegmentKind.CUSTOM_VERB) {
            val matcher = CUSTOM_VERB_PATTERN.matcher(path)
            if (!matcher.find() || decodeUrl(matcher.group(1)) != last.value) {
                return null
            }
            path = path.substring(0, matcher.start(0))
        }

        val matcher = HOSTNAME_PATTERN.matcher(path)
        val withHostName = matcher.find()
        if (withHostName) {
            path = matcher.replaceFirst("")
        }
        val input = path.split('/').map { it.trim() }
        var inPos = 0
        val values = mutableMapOf<String, String>()
        if (withHostName) {
            if (input.isEmpty()) {
                return null
            }
            values[HOSTNAME_VAR] = input[inPos++]
        }
        if (withHostName) {
            inPos = alignInputToAlignableSegment(input, inPos, segments[0])
        }
        if (!match(input, inPos, segments, 0, values)) {
            return null
        }
        return values.toMap()
    }

    // Aligns input to start of literal value of literal or binding segment if input contains hostname.
    private fun alignInputToAlignableSegment(input: List<String>, inPos: Int, segment: Segment): Int {
        return when (segment.kind) {
            SegmentKind.BINDING -> inPos
            SegmentKind.LITERAL -> alignInputPositionToLiteral(input, inPos, segment.value)
            else -> inPos
        }
    }

    // Aligns input to start of literal value if input contains hostname.
    private fun alignInputPositionToLiteral(input: List<String>, inPos: Int, literalSegmentValue: String): Int {
        var inPos = inPos
        while (inPos < input.size) {
            if (literalSegmentValue == input[inPos]) {
                return inPos
            }
            inPos++
        }
        return inPos
    }

    // Tries to match the input based on the segments at given positions. Returns a boolean
// indicating whether the match was successful.
    private fun match(
        input: List<String>,
        inPos: Int,
        segments: List<Segment>,
        segPos: Int,
        values: MutableMap<String, String>
    ): Boolean {
        var inPos = inPos
        var segPos = segPos
        var currentVar: String? = null
        while (segPos < segments.size) {
            val seg = segments[segPos++]
            when (seg.kind) {
                SegmentKind.END_BINDING -> {
                    // End current variable binding scope.
                    currentVar = null
                }
                SegmentKind.BINDING -> {
                    // Start variable binding scope.
                    currentVar = seg.value
                }
                SegmentKind.CUSTOM_VERB -> {
                }
                SegmentKind.LITERAL, SegmentKind.WILDCARD -> {
                    if (inPos >= input.size) {
                        // End of input
                        return false
                    }
                    // Check literal match.
                    val next = decodeUrl(input[inPos++])
                    if (seg.kind == SegmentKind.LITERAL) {
                        if (seg.value != next) {
                            // Literal does not match.
                            return false
                        }
                    }
                    if (currentVar != null) {
                        // Create or extend current match
                        values[currentVar] = concatCaptures(values[currentVar], next)
                    }
                }
                SegmentKind.PATH_WILDCARD -> {
                    // Compute the number of additional input the ** can consume. This
                    // is possible because we restrict patterns to have only one **.
                    var segsToMatch = 0
                    for (i in segPos until segments.size) {
                        when (segments[i].kind) {
                            SegmentKind.BINDING, SegmentKind.END_BINDING -> {
                            }
                            else -> segsToMatch++
                        }
                    }
                    var available = (input.size - inPos) - segsToMatch
                    // If this segment is empty, make sure it is still captured.
                    if (available == 0 && !values.containsKey(currentVar)) {
                        values[currentVar!!] = ""
                    }
                    while (available-- > 0) {
                        values[currentVar!!] = concatCaptures(values[currentVar], decodeUrl(input[inPos++]))
                    }
                }
            }
            // This is the final segment, and this check should have already been performed by the
            // caller. The matching value is no longer present in the input.
        }
        return inPos == input.size
    }

    /**
     * Instantiate the template based on the given variable assignment. Performs proper URL escaping
     * of variable assignments.
     *
     *
     *
     * Note that free wildcards in the template must have bindings of '$n' variables, where 'n' is the
     * position of the wildcard (starting at 0). See the documentation of [.match] for
     * details.
     *
     * @throws ValidationException if a variable occurs in the template without a binding.
     */
    fun instantiate(values: Map<String, String>): String {
        return instantiate(values, false)
    }

    /**
     * Shortcut for [.instantiate] with a vararg parameter for keys and values.
     */
    fun instantiate(vararg keysAndValues: String): String {
        val builder = mutableMapOf<String, String>()
        var i = 0
        while (i < keysAndValues.size) {
            builder[keysAndValues[i]] = keysAndValues[i + 1]
            i += 2
        }
        return instantiate(builder)
    }

    /**
     * Same like [.instantiate] but allows for unbound variables, which are substituted
     * using their original syntax. Example:
     *
     * <pre>
     * PathTemplate template = PathTemplate.create("v1/shelves/{shelf}/books/{book}");
     * Map&lt;String, String&gt; partialMap = new HashMap&lt;&gt;();
     * partialMap.put("shelf", "s1");
     * assert template.instantiatePartial(partialMap).equals("v1/shelves/s1/books/{book}");
    </pre> *
     *
     * The result of this call can be used to create a new template.
     */
    fun instantiatePartial(values: Map<String, String>): String {
        return instantiate(values, true)
    }

    private fun instantiate(values: Map<String, String>, allowPartial: Boolean): String {
        val result = StringBuilder()
        if (values.containsKey(HOSTNAME_VAR)) {
            result.append("//")
            result.append(values[HOSTNAME_VAR])
            result.append('/')
        }
        var continueLast = true // Whether to not append separator
        var skip = false // Whether we are substituting a binding and segments shall be skipped.
        val iterator = segments.listIterator()
        while (iterator.hasNext()) {
            val seg = iterator.next()
            if (!skip && !continueLast) {
                result.append(seg.separator())
            }
            continueLast = false
            when (seg.kind) {
                SegmentKind.BINDING -> {
                    val `var` = seg.value
                    val value = values[seg.value]
                    if (value == null) {
                        if (!allowPartial) {
                            throw ValidationException(
                                    String.format("Unbound variable '%s'. Bindings: %s", `var`, values))
                        }
                        // Append pattern to output
                        if (`var`.startsWith("$")) {
                            // Eliminate positional variable.
                            result.append(iterator.next().value)
                            iterator.next()
                        } else {
                            result.append('{')
                            result.append(seg.value)
                            result.append('=')
                            continueLast = true
                        }
                    } else {
                        val next = iterator.next()
                        val nextNext = iterator.next()
                        val pathEscape = (next.kind == SegmentKind.PATH_WILDCARD || nextNext.kind != SegmentKind.END_BINDING)
                        restore(iterator, iterator.nextIndex() - 2)
                        if (!pathEscape) {
                            result.append(encodeUrl(value))
                        } else {
                            // For a path wildcard or path of length greater 1, split the value and escape
                            // every sub-segment.
                            var first = true
                            for (subSeg in value.split('/').map { it.trim() }) {
                                if (!first) {
                                    result.append('/')
                                }
                                first = false
                                result.append(encodeUrl(subSeg))
                            }
                        }
                        skip = true
                    }
                }
                SegmentKind.END_BINDING -> {
                    if (!skip) {
                        result.append('}')
                    }
                    skip = false
                }
                else -> if (!skip) {
                    result.append(seg.value)
                }
            }
        }
        return result.toString()
    }

    /**
     * Instantiates the template from the given positional parameters. The template must not be build
     * from named bindings, but only contain wildcards. Each parameter position corresponds to a
     * wildcard of the according position in the template.
     */
    fun encode(vararg values: String): String {
        val builder = mutableMapOf<String, String>()
        var i = 0
        for (value in values) {
            builder["$" + i++] = value
        }
        // We will get an error if there are named bindings which are not reached by values.
        return instantiate(builder)
    }

    /**
     * Matches the template into a list of positional values. The template must not be build from
     * named bindings, but only contain wildcards. For each wildcard in the template, a value is
     * returned at corresponding position in the list.
     */
    fun decode(path: String): List<String> {
        val match = match(path) ?: throw IllegalArgumentException(
                String.format("template '%s' does not match '%s'", this, path))
        val result = mutableListOf<String>()
        for (entry in match.entries) {
            val key = entry.key
            if (!key.startsWith("$")) {
                throw IllegalArgumentException("template must not contain named bindings")
            }
            val i = Integer.parseInt(key.substring(1))
            while (result.size <= i) {
                result.add("")
            }
            result[i] = entry.value
        }
        return result
    }

    private fun encodeUrl(text: String?): String {
        return if (urlEncoding) {
            try {
                URLEncoder.encode(text!!, "UTF-8")
            } catch (e: UnsupportedEncodingException) {
                throw ValidationException("UTF-8 encoding is not supported on this platform")
            }
        } else {
            // When encoding is disabled, we accept any character except '/'
            val INVALID_CHAR = "/"
            if (text!!.contains(INVALID_CHAR)) {
                throw ValidationException("Invalid character \"$INVALID_CHAR\" in path section \"$text\".")
            }
            text
        }
    }

    private fun decodeUrl(url: String): String {
        return if (urlEncoding) {
            try {
                URLDecoder.decode(url, "UTF-8")
            } catch (e: UnsupportedEncodingException) {
                throw ValidationException("UTF-8 encoding is not supported on this platform")
            }
        } else {
            url
        }
    }

    /**
     * Returns a pretty version of the template as a string.
     */
    override fun toString(): String {
        return toSyntax(segments, true)
    }

    /**
     * Returns a raw version of the template as a string. This renders the template in its internal,
     * normalized form.
     */
    fun toRawString(): String {
        return toSyntax(segments, false)
    }

    override fun equals(obj: Any?): Boolean {
        if (!(obj is PathTemplate)) {
            return false
        }
        val other = obj as PathTemplate?
        return segments == other!!.segments
    }

    override fun hashCode(): Int {
        return segments.hashCode()
    }

    companion object {

        /**
         * A constant identifying the special variable used for endpoint bindings in the result of
         * [.matchFromFullName]. It may also contain protocol string, if its provided in the
         * input.
         */
        val HOSTNAME_VAR = "\$hostname"

        // A regexp to match a custom verb at the end of a path.
        private val CUSTOM_VERB_PATTERN = """:([^/*}{=]+)$""".toPattern()

        // A regex to match a hostname with or without protocol.
        private val HOSTNAME_PATTERN = """^(w+:)?//""".toPattern()

        /**
         * Creates a path template from a string. The string must satisfy the syntax of path templates of
         * the API platform; see HttpRule's proto source.
         *
         * @throws ValidationException if there are errors while parsing the template.
         */
        fun create(template: String): PathTemplate {
            return create(template, true)
        }

        /**
         * Creates a path template from a string. The string must satisfy the syntax of path templates of
         * the API platform; see HttpRule's proto source. Url encoding of template variables is disabled.
         *
         * @throws ValidationException if there are errors while parsing the template.
         */
        fun createWithoutUrlEncoding(template: String): PathTemplate {
            return create(template, false)
        }

        private fun create(template: String, urlEncoding: Boolean): PathTemplate {
            return PathTemplate(parseTemplate(template), urlEncoding)
        }

        private fun concatCaptures(cur: String?, next: String): String {
            return if (cur == null) next else "$cur/$next"
        }

        private fun parseTemplate(template: String): List<Segment> {
            var template = template
            val builder = mutableListOf<Segment>()

            // Skip useless leading slash.
            if (template.startsWith("/")) {
                builder.add(Segment.EMPTY)
                template = template.substring(1)
            }

            // Extract trailing custom verb.
            val matcher = CUSTOM_VERB_PATTERN.matcher(template)
            var customVerb: String? = null
            if (matcher.find()) {
                customVerb = matcher.group(1)
                template = template.substring(0, matcher.start(0))
            }

            var varName: String? = null
            var freeWildcardCounter = 0
            var pathWildCardBound = 0

            for (seg in template.split('/').map { it.trim() }) {
                var seg = seg
                // If segment starts with '{', a binding group starts.
                val bindingStarts = seg.startsWith("{")
                var implicitWildcard = false
                if (bindingStarts) {
                    if (varName != null) {
                        throw ValidationException("parse error: nested binding in '%s'", template)
                    }
                    seg = seg.substring(1)

                    val i = seg.indexOf('=')
                    if (i <= 0) {
                        // Possibly looking at something like "{name}" with implicit wildcard.
                        if (seg.endsWith("}")) {
                            // Remember to add an implicit wildcard later.
                            implicitWildcard = true
                            varName = seg.substring(0, seg.length - 1).trim { it <= ' ' }
                            seg = seg.substring(seg.length - 1).trim { it <= ' ' }
                        } else {
                            throw ValidationException("parse error: invalid binding syntax in '%s'", template)
                        }
                    } else {
                        // Looking at something like "{name=wildcard}".
                        varName = seg.substring(0, i).trim { it <= ' ' }
                        seg = seg.substring(i + 1).trim { it <= ' ' }
                    }
                    builder.add(Segment(SegmentKind.BINDING, varName))
                }

                // If segment ends with '}', a binding group ends. Remove the brace and remember.
                val bindingEnds = seg.endsWith("}")
                if (bindingEnds) {
                    seg = seg.substring(0, seg.length - 1).trim { it <= ' ' }
                }

                // Process the segment, after stripping off "{name=.." and "..}".
                when (seg) {
                    "**", "*" -> {
                        if ("**" == seg) {
                            pathWildCardBound++
                        }
                        val wildcard = if (seg.length == 2) Segment.PATH_WILDCARD else Segment.WILDCARD
                        if (varName == null) {
                            // Not in a binding, turn wildcard into implicit binding.
                            // "*" => "{$n=*}"
                            builder.add(Segment(SegmentKind.BINDING, "$$freeWildcardCounter"))
                            freeWildcardCounter++
                            builder.add(wildcard)
                            builder.add(Segment.END_BINDING)
                        } else {
                            builder.add(wildcard)
                        }
                    }
                    "" -> if (!bindingEnds) {
                        throw ValidationException(
                                "parse error: empty segment not allowed in '%s'", template)
                    }
                    else -> builder.add(Segment(SegmentKind.LITERAL, seg))
                } // If the wildcard is implicit, seg will be empty. Just continue.

                // End a binding.
                if (bindingEnds) {
                    // Reset varName to null for next binding.
                    varName = null

                    if (implicitWildcard) {
                        // Looking at something like "{var}". Insert an implicit wildcard, as it is the same
                        // as "{var=*}".
                        builder.add(Segment.WILDCARD)
                    }
                    builder.add(Segment.END_BINDING)
                }

                if (pathWildCardBound > 1) {
                    // Report restriction on number of '**' in the pattern. There can be only one, which
                    // enables non-backtracking based matching.
                    throw ValidationException(
                            "parse error: pattern must not contain more than one path wildcard ('**') in '%s'",
                            template)
                }
            }

            if (customVerb != null) {
                builder.add(Segment(SegmentKind.CUSTOM_VERB, customVerb))
            }
            return builder
        }

        // Checks for the given segments kind. On success, consumes them. Otherwise leaves
        // the list iterator in its state.
        private fun peek(segments: ListIterator<Segment>, vararg kinds: SegmentKind): Boolean {
            val start = segments.nextIndex()
            var success = false
            for (kind in kinds) {
                if (!segments.hasNext() || segments.next().kind != kind) {
                    success = false
                    break
                }
            }
            if (success) {
                return true
            }
            restore(segments, start)
            return false
        }

        // Restores a list iterator back to a given index.
        private fun restore(segments: ListIterator<*>, index: Int) {
            while (segments.nextIndex() > index) {
                segments.previous()
            }
        }

        private fun toSyntax(segments: List<Segment>, pretty: Boolean): String {
            val result = StringBuilder()
            var continueLast = true // if true, no slash is appended.
            val iterator = segments.listIterator()
            while (iterator.hasNext()) {
                var seg = iterator.next()
                if (!continueLast) {
                    result.append(seg.separator())
                }
                continueLast = false
                when (seg.kind) {
                    SegmentKind.BINDING -> {
                        if (pretty && seg.value.startsWith("$")) {
                            // Remove the internal binding.
                            seg = iterator.next() // Consume wildcard
                            result.append(seg.value)
                            iterator.next() // Consume END_BINDING
                        } else {
                            result.append('{')
                            result.append(seg.value)
                            if (pretty && peek(iterator, SegmentKind.WILDCARD, SegmentKind.END_BINDING)) {
                                // Reduce {name=*} to {name}.
                                result.append('}')
                            } else {
                                result.append('=')
                                continueLast = true
                            }
                        }
                    }
                    SegmentKind.END_BINDING -> {
                        result.append('}')
                    }
                    else -> {
                        result.append(seg.value)
                    }
                }
            }
            return result.toString()
        }
    }
}
