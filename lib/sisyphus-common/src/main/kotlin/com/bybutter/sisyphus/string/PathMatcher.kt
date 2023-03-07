package com.bybutter.sisyphus.string

object PathMatcher {
    fun CharSequence.matches(pattern: CharSequence, pathDelimiters: Set<Char> = setOf('/')): Boolean {
        return match(pattern, this, pathDelimiters)
    }

    fun match(pattern: CharSequence, path: CharSequence, pathDelimiters: Set<Char> = setOf('/')): Boolean {
        return normalMatch(pattern, 0, path, 0, pathDelimiters)
    }

    private fun normalMatch(pat: CharSequence, p: Int, str: CharSequence, s: Int, pathDelimiters: Set<Char>): Boolean {
        var pi = p
        var si = s
        while (pi < pat.length) {
            val pc = pat[pi]
            val sc = str.getOrZero(si)
            // Got * in pattern, enter the wildcard mode.
            //            ↓        ↓
            // pattern: a/*      a/*
            //            ↓        ↓
            // string:  a/bcd    a/
            if (pc == '*') {
                pi++
                // Got * in pattern again, enter the multi-wildcard mode.
                //             ↓        ↓
                // pattern: a/**     a/**
                //            ↓        ↓
                // string:  a/bcd    a/
                return if (pat.getOrZero(pi) == '*') {
                    pi++
                    // Enter the multi-wildcard mode.
                    //              ↓        ↓
                    // pattern: a/**     a/**
                    //            ↓        ↓
                    // string:  a/bcd    a/
                    multiWildcardMatch(pat, pi, str, si, pathDelimiters)
                } else { // Enter the wildcard mode.
                    //             ↓
                    // pattern: a/*
                    //            ↓
                    // string:  a/bcd
                    wildcardMatch(pat, pi, str, si, pathDelimiters)
                }
            }
            // Matching ? for non-'/' char, or matching the same chars.
            //            ↓        ↓       ↓
            // pattern: a/?/c    a/b/c    a/b
            //            ↓        ↓       ↓
            // string:  a/b/c    a/b/d    a/d
            if (pc == '?' && sc.code != 0 && sc !in pathDelimiters || pc == sc) {
                si++
                pi++
                continue
            }
            // Not matched.
            //            ↓
            // pattern: a/b
            //            ↓
            // string:  a/c
            return false
        }
        return si == str.length
    }

    private fun wildcardMatch(
        pat: CharSequence,
        p: Int,
        str: CharSequence,
        s: Int,
        pathDelimiters: Set<Char>
    ): Boolean {
        var si = s
        val pc = pat.getOrZero(p)
        while (true) {
            val sc = str.getOrZero(si)
            if (sc in pathDelimiters) {
                // Both of pattern and string '/' matched, exit wildcard mode.
                //             ↓
                // pattern: a/*/
                //              ↓
                // string:  a/bc/
                return if (pc == sc) {
                    normalMatch(pat, p + 1, str, si + 1, pathDelimiters)
                } else {
                    false
                }
                // Not matched string in current path part.
                //             ↓        ↓
                // pattern: a/*      a/*d
                //              ↓        ↓
                // string:  a/bc/    a/bc/
            }
            // Try to enter normal mode, if not matched, increasing pointer of string and try again.
            if (!normalMatch(pat, p, str, si, pathDelimiters)) { // End of string, not matched.
                if (si >= str.length) {
                    return false
                }
                si++
                continue
            }
            // Matched in next normal mode.
            return true
        }
    }

    private fun multiWildcardMatch(
        pat: CharSequence,
        p: Int,
        str: CharSequence,
        s: Int,
        pathDelimiters: Set<Char>
    ): Boolean {
        // End of pattern, just check the end of string is '/' quickly.
        var si = s
        if (p >= pat.length && si < str.length) {
            return str[str.length - 1] !in pathDelimiters
        }
        while (true) {
            // Try to enter normal mode, if not matched, increasing pointer of string and try again.
            if (!normalMatch(pat, p, str, si, pathDelimiters)) {
                // End of string, not matched.
                if (si >= str.length) {
                    return false
                }
                si++
                continue
            }
            return true
        }
    }
}
