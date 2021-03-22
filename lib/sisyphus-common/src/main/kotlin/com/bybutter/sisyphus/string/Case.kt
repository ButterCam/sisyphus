package com.bybutter.sisyphus.string

import com.bybutter.sisyphus.string.case.CamelCaseFormatter
import com.bybutter.sisyphus.string.case.CaseFormat
import com.bybutter.sisyphus.string.case.CaseFormatter
import com.bybutter.sisyphus.string.case.CommonWordSplitter
import com.bybutter.sisyphus.string.case.DotCaseFormatter
import com.bybutter.sisyphus.string.case.KebabCaseFormatter
import com.bybutter.sisyphus.string.case.PascalCaseFormatter
import com.bybutter.sisyphus.string.case.ScreamingSnakeCaseFormatter
import com.bybutter.sisyphus.string.case.SnakeCaseFormatter
import com.bybutter.sisyphus.string.case.SpaceCaseFormatter
import com.bybutter.sisyphus.string.case.TitleCaseFormatter
import com.bybutter.sisyphus.string.case.TrainCaseFormatter
import com.bybutter.sisyphus.string.case.UpperDotCaseFormatter
import com.bybutter.sisyphus.string.case.UpperSpaceCaseFormatter
import com.bybutter.sisyphus.string.case.WordSplitter

fun String.toCase(format: CaseFormat) = format.format(this)

fun String.toCase(formatter: CaseFormatter, splitter: WordSplitter = CommonWordSplitter) =
    formatter.format(splitter.split(this))

/** Converts a string to 'SCREAMING_SNAKE_CASE'. */
fun String.toScreamingSnakeCase() = toCase(ScreamingSnakeCaseFormatter)

/** Converts a string to 'snake_case.' */
fun String.toSnakeCase() = toCase(SnakeCaseFormatter)

/** Converts a string to 'PascalCase'. */
fun String.toPascalCase() = toCase(PascalCaseFormatter)

/** Converts a string to 'camelCase'. */
fun String.toCamelCase() = toCase(CamelCaseFormatter)

/** Converts a string to 'TRAIN-CASE'. */
fun String.toTrainCase() = toCase(TrainCaseFormatter)

/** Converts a string to 'kebab-case'. */
fun String.toKebabCase() = toCase(KebabCaseFormatter)

/** Converts a string to 'UPPER SPACE CASE'. */
fun String.toUpperSpaceCase() = toCase(UpperSpaceCaseFormatter)

/** Converts a string to 'Title Case'. */
fun String.toTitleCase() = toCase(TitleCaseFormatter)

/** Converts a string to 'lower space case'. */
fun String.toLowerSpaceCase() = toCase(SpaceCaseFormatter)

/** Converts a string to 'UPPER.DOT.CASE'. */
fun String.toUpperDotCase() = toCase(UpperDotCaseFormatter)

/** Converts a string to 'dot.case'. */
fun String.toDotCase() = toCase(DotCaseFormatter)
