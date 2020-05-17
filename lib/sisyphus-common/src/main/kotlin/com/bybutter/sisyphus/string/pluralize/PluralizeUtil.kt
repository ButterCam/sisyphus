package com.bybutter.sisyphus.string.pluralize

import com.bybutter.sisyphus.string.toPascalCase

/**
 * Translate from JavaScript module 'https://github.com/blakeembrey/pluralize'
 */
object PluralizeUtil {
    private val uncountableWords = hashSetOf<String>()
    private val uncountableRules = mutableListOf<Regex>()
    private val irregularSingles = hashMapOf<String, String>()
    private val irregularPlurals = hashMapOf<String, String>()
    private val pluralizationRules = mutableListOf<Rule>()
    private val singularizationRules = mutableListOf<Rule>()

    fun addIrregularRule(single: String, plural: String) {
        irregularSingles[single] = plural
        irregularPlurals[plural] = single
    }

    fun addPluralRule(rule: Regex, replace: String) {
        pluralizationRules.add(Rule(rule, replace))
    }

    fun addSingularRule(rule: Regex, replace: String) {
        singularizationRules.add(Rule(rule, replace))
    }

    fun addUncountableWord(word: String) {
        uncountableWords.add(word)
    }

    fun addUncountableRule(rule: Regex) {
        uncountableRules.add(rule)
    }

    fun singular(word: String): String {
        return replaceWord(word, irregularPlurals, irregularSingles, singularizationRules)
    }

    fun isSingular(word: String): Boolean {
        return checkWord(word, irregularPlurals, irregularSingles, singularizationRules)
    }

    fun plural(word: String): String {
        return replaceWord(word, irregularSingles, irregularPlurals, pluralizationRules)
    }

    fun isPlural(word: String): Boolean {
        return checkWord(word, irregularSingles, irregularPlurals, pluralizationRules)
    }

    private fun restoreCase(word: String, token: String): String {
        if (word == token) return token
        if (word.all { it.isLowerCase() }) return token.toLowerCase()
        if (word.all { it.isUpperCase() }) return token.toUpperCase()
        if (word[0].isUpperCase() && word.substring(1).all { it.isLowerCase() }) return token.toPascalCase()
        return token
    }

    private fun replaceWord(word: String, replaceMap: Map<String, String>, keepMap: Map<String, String>, rules: List<Rule>): String {
        if (word.isBlank()) return word

        val token = word.toLowerCase()
        if (keepMap.containsKey(token)) {
            return word
        }

        replaceMap[token]?.let {
            return restoreCase(word, it)
        }

        if (uncountableWords.contains(token)) {
            return word
        }

        if (uncountableRules.any { it.matches(token) }) {
            return word
        }

        val sanitizedToken = sanitizeWord(token, rules)
        if (sanitizedToken == token) {
            return word
        }
        return restoreCase(word, sanitizedToken)
    }

    private fun sanitizeWord(token: String, rules: List<Rule>): String {
        for (rule in rules.asReversed()) {
            if (rule.regex.containsMatchIn(token)) {
                return rule.regex.replace(token, rule.replace)
            }
        }
        return token
    }

    private fun checkWord(word: String, replaceMap: Map<String, String>, keepMap: Map<String, String>, rules: List<Rule>): Boolean {
        if (word.isBlank()) return false

        val token = word.toLowerCase()
        if (keepMap.containsKey(token)) {
            return true
        }
        if (replaceMap.containsKey(token)) {
            return false
        }
        if (uncountableWords.contains(token)) {
            return false
        }
        if (uncountableRules.any { it.matches(token) }) {
            return false
        }

        return sanitizeWord(token, rules) == token
    }

    // irregular rules
    init {
        // Pronouns.
        addIrregularRule("I", "we")
        addIrregularRule("me", "us")
        addIrregularRule("he", "they")
        addIrregularRule("she", "they")
        addIrregularRule("them", "them")
        addIrregularRule("myself", "ourselves")
        addIrregularRule("yourself", "yourselves")
        addIrregularRule("itself", "themselves")
        addIrregularRule("herself", "themselves")
        addIrregularRule("himself", "themselves")
        addIrregularRule("themself", "themselves")
        addIrregularRule("is", "are")
        addIrregularRule("was", "were")
        addIrregularRule("has", "have")
        addIrregularRule("this", "these")
        addIrregularRule("that", "those")
        // Words ending in with a consonant and `o`.
        addIrregularRule("echo", "echoes")
        addIrregularRule("dingo", "dingoes")
        addIrregularRule("volcano", "volcanoes")
        addIrregularRule("tornado", "tornadoes")
        addIrregularRule("torpedo", "torpedoes")
        // Ends with `us`.
        addIrregularRule("genus", "genera")
        addIrregularRule("viscus", "viscera")
        // Ends with `ma`.
        addIrregularRule("stigma", "stigmata")
        addIrregularRule("stoma", "stomata")
        addIrregularRule("dogma", "dogmata")
        addIrregularRule("lemma", "lemmata")
        addIrregularRule("schema", "schemata")
        addIrregularRule("anathema", "anathemata")
        // Other irregular rules.
        addIrregularRule("ox", "oxen")
        addIrregularRule("axe", "axes")
        addIrregularRule("die", "dice")
        addIrregularRule("yes", "yeses")
        addIrregularRule("foot", "feet")
        addIrregularRule("eave", "eaves")
        addIrregularRule("goose", "geese")
        addIrregularRule("tooth", "teeth")
        addIrregularRule("quiz", "quizzes")
        addIrregularRule("human", "humans")
        addIrregularRule("proof", "proofs")
        addIrregularRule("carve", "carves")
        addIrregularRule("valve", "valves")
        addIrregularRule("looey", "looies")
        addIrregularRule("thief", "thieves")
        addIrregularRule("groove", "grooves")
        addIrregularRule("pickaxe", "pickaxes")
        addIrregularRule("passerby", "passersby")
    }

    // pluralization rules
    init {
        addPluralRule("""s?$""".toRegex(), "s")
        addPluralRule("""[^\u0000-\u007F]$""".toRegex(), "$0")
        addPluralRule("""([^aeiou]ese)$""".toRegex(), "$1")
        addPluralRule("""(ax|test)is$""".toRegex(), "$1es")
        addPluralRule("""(alias|[^aou]us|t[lm]as|gas|ris)$""".toRegex(), "$1es")
        addPluralRule("""(e[mn]u)s?$""".toRegex(), "$1s")
        addPluralRule("""([^l]ias|[aeiou]las|[ejzr]as|[iu]am)$""".toRegex(), "$1")
        addPluralRule("""(alumn|syllab|vir|radi|nucle|fung|cact|stimul|termin|bacill|foc|uter|loc|strat)(?:us|i)$""".toRegex(), "$1i")
        addPluralRule("""(alumn|alg|vertebr)(?:a|ae)$""".toRegex(), "$1ae")
        addPluralRule("""(seraph|cherub)(?:im)?$""".toRegex(), "$1im")
        addPluralRule("""(her|at|gr)o$""".toRegex(), "$1oes")
        addPluralRule("""(agend|addend|millenni|dat|extrem|bacteri|desiderat|strat|candelabr|errat|ov|symposi|curricul|automat|quor)(?:a|um)$""".toRegex(), "$1a")
        addPluralRule("""(apheli|hyperbat|periheli|asyndet|noumen|phenomen|criteri|organ|prolegomen|hedr|automat)(?:a|on)$""".toRegex(), "$1a")
        addPluralRule("""sis$""".toRegex(), "ses")
        addPluralRule("""(?:(kni|wi|li)fe|(ar|l|ea|eo|oa|hoo)f)$""".toRegex(), "$1$2ves")
        addPluralRule("""([^aeiouy]|qu)y$""".toRegex(), "$1ies")
        addPluralRule("""([^ch][ieo][ln])ey$""".toRegex(), "$1ies")
        addPluralRule("""(x|ch|ss|sh|zz)$""".toRegex(), "$1es")
        addPluralRule("""(matr|cod|mur|sil|vert|ind|append)(?:ix|ex)$""".toRegex(), "$1ices")
        addPluralRule("""\b((?:tit)?m|l)(?:ice|ouse)$""".toRegex(), "$1ice")
        addPluralRule("""(pe)(?:rson|ople)$""".toRegex(), "$1ople")
        addPluralRule("""(child)(?:ren)?$""".toRegex(), "$1ren")
        addPluralRule("""eaux$""".toRegex(), "$0")
        addPluralRule("""m[ae]n$""".toRegex(), "men")
        addPluralRule("""^thou$""".toRegex(), "you")
    }

    // singularization rules
    init {
        addSingularRule("""s$""".toRegex(), "")
        addSingularRule("""(ss)$""".toRegex(), "$1")
        addSingularRule("""(wi|kni|(?:after|half|high|low|mid|non|night|[^\w]|^)li)ves$""".toRegex(), "$1fe")
        addSingularRule("""(ar|(?:wo|[ae])l|[eo][ao])ves$""".toRegex(), "$1f")
        addSingularRule("""ies$""".toRegex(), "y")
        addSingularRule("""(dg|ss|ois|lk|ok|wn|mb|th|ch|ec|oal|is|ec|ck|ix|sser|ts|wb)ies$""".toRegex(), "$1ie")
        addSingularRule("""\b(l|(?:neck|cross|hog|aun)?t|coll|faer|food|gen|goon|group|hipp|junk|vegg|(?:pork)?p|charl|calor|cut)ies$""".toRegex(), "$1ie")
        addSingularRule("""\b(mon|smil)ies$""".toRegex(), "$1ey")
        addSingularRule("""\b((?:tit)?m|l)ice$""".toRegex(), "$1ouse")
        addSingularRule("""(seraph|cherub)im$""".toRegex(), "$1")
        addSingularRule("""(x|ch|ss|sh|zz|tto|go|cho|alias|[^aou]us|t[lm]as|gas|(?:her|at|gr)o|[aeiou]ris)(?:es)?$""".toRegex(), "$1")
        addSingularRule("""(analy|diagno|parenthe|progno|synop|the|empha|cri|ne)(?:sis|ses)$""".toRegex(), "$1sis")
        addSingularRule("""(movie|twelve|abuse|e[mn]u)s$""".toRegex(), "$1")
        addSingularRule("""(test)(?:is|es)$""".toRegex(), "$1is")
        addSingularRule("""(alumn|syllab|vir|radi|nucle|fung|cact|stimul|termin|bacill|foc|uter|loc|strat)(?:us|i)$""".toRegex(), "$1us")
        addSingularRule("""(agend|addend|millenni|dat|extrem|bacteri|desiderat|strat|candelabr|errat|ov|symposi|curricul|quor)a$""".toRegex(), "$1um")
        addSingularRule("""(apheli|hyperbat|periheli|asyndet|noumen|phenomen|criteri|organ|prolegomen|hedr|automat)a$""".toRegex(), "$1on")
        addSingularRule("""(alumn|alg|vertebr)ae$""".toRegex(), "$1a")
        addSingularRule("""(cod|mur|sil|vert|ind)ices$""".toRegex(), "$1ex")
        addSingularRule("""(matr|append)ices$""".toRegex(), "$1ix")
        addSingularRule("""(pe)(rson|ople)$""".toRegex(), "$1rson")
        addSingularRule("""(child)ren$""".toRegex(), "$1")
        addSingularRule("""(eau)x?$""".toRegex(), "$1")
        addSingularRule("""men$""".toRegex(), "man")
    }

    // uncountable
    init {
        // Singular words with no plurals.
        addUncountableWord("adulthood")
        addUncountableWord("advice")
        addUncountableWord("agenda")
        addUncountableWord("aid")
        addUncountableWord("aircraft")
        addUncountableWord("alcohol")
        addUncountableWord("ammo")
        addUncountableWord("analytics")
        addUncountableWord("anime")
        addUncountableWord("athletics")
        addUncountableWord("audio")
        addUncountableWord("bison")
        addUncountableWord("blood")
        addUncountableWord("bream")
        addUncountableWord("buffalo")
        addUncountableWord("butter")
        addUncountableWord("carp")
        addUncountableWord("cash")
        addUncountableWord("chassis")
        addUncountableWord("chess")
        addUncountableWord("clothing")
        addUncountableWord("cod")
        addUncountableWord("commerce")
        addUncountableWord("cooperation")
        addUncountableWord("corps")
        addUncountableWord("debris")
        addUncountableWord("diabetes")
        addUncountableWord("digestion")
        addUncountableWord("elk")
        addUncountableWord("energy")
        addUncountableWord("equipment")
        addUncountableWord("excretion")
        addUncountableWord("expertise")
        addUncountableWord("firmware")
        addUncountableWord("flounder")
        addUncountableWord("fun")
        addUncountableWord("gallows")
        addUncountableWord("garbage")
        addUncountableWord("graffiti")
        addUncountableWord("hardware")
        addUncountableWord("headquarters")
        addUncountableWord("health")
        addUncountableWord("herpes")
        addUncountableWord("highjinks")
        addUncountableWord("homework")
        addUncountableWord("housework")
        addUncountableWord("information")
        addUncountableWord("jeans")
        addUncountableWord("justice")
        addUncountableWord("kudos")
        addUncountableWord("labour")
        addUncountableWord("literature")
        addUncountableWord("machinery")
        addUncountableWord("mackerel")
        addUncountableWord("mail")
        addUncountableWord("media")
        addUncountableWord("mews")
        addUncountableWord("moose")
        addUncountableWord("music")
        addUncountableWord("mud")
        addUncountableWord("manga")
        addUncountableWord("news")
        addUncountableWord("only")
        addUncountableWord("personnel")
        addUncountableWord("pike")
        addUncountableWord("plankton")
        addUncountableWord("pliers")
        addUncountableWord("police")
        addUncountableWord("pollution")
        addUncountableWord("premises")
        addUncountableWord("rain")
        addUncountableWord("research")
        addUncountableWord("rice")
        addUncountableWord("salmon")
        addUncountableWord("scissors")
        addUncountableWord("series")
        addUncountableWord("sewage")
        addUncountableWord("shambles")
        addUncountableWord("shrimp")
        addUncountableWord("software")
        addUncountableWord("staff")
        addUncountableWord("swine")
        addUncountableWord("tennis")
        addUncountableWord("traffic")
        addUncountableWord("transportation")
        addUncountableWord("trout")
        addUncountableWord("tuna")
        addUncountableWord("wealth")
        addUncountableWord("welfare")
        addUncountableWord("whiting")
        addUncountableWord("wildebeest")
        addUncountableWord("wildlife")
        addUncountableWord("you")
        addUncountableRule("""pok[eé]mon$""".toRegex())
        // Regexes.
        addUncountableRule("""[^aeiou]ese$""".toRegex()) // "chinese", "japanese"
        addUncountableRule("""deer$""".toRegex()) // "deer", "reindeer"
        addUncountableRule("""fish$""".toRegex()) // "fish", "blowfish", "angelfish"
        addUncountableRule("""measles$""".toRegex())
        addUncountableRule("""o[iu]s$""".toRegex()) // "carnivorous"
        addUncountableRule("""pox$""".toRegex()) // "chickpox", "smallpox"
        addUncountableRule("""sheep$""".toRegex())
    }

    private data class Rule(val regex: Regex, val replace: String)
}
