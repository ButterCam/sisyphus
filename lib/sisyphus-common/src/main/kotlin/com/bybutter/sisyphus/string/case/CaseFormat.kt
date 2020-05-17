package com.bybutter.sisyphus.string.case

enum class CaseFormat(val formatter: CaseFormatter, val splitter: WordSplitter = CommonWordSplitter) {
    SCREAMING_SNAKE_CASE(ScreamingSnakeCaseFormatter),
    SNAKE_CASE(SnakeCaseFormatter),
    PASCAL_CASE(PascalCaseFormatter),
    CAMEL_CASE(CamelCaseFormatter),
    TRAIN_CASE(TrainCaseFormatter),
    KEBAB_CASE(KebabCaseFormatter),
    UPPER_SPACE_CASE(UpperSpaceCaseFormatter),
    SPACE_CASE(SpaceCaseFormatter),
    TITLE_CASE_CASE(TitleCaseFormatter),
    UPPER_DOT_CASE(UpperDotCaseFormatter),
    DOT_CASE(DotCaseFormatter);

    fun format(words: String): String {
        return formatter.format(splitter.split(words))
    }

    companion object {
        fun bestGuess(string: String): CaseFormat {
            if (string.isBlank()) return CAMEL_CASE
            val words = CommonWordSplitter.split(string)

            for (value in values()) {
                val formatted = value.formatter.format(words)
                if (formatted == string) return value
            }

            return CAMEL_CASE
        }
    }
}
