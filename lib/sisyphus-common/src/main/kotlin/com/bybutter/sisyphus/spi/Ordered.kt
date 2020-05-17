package com.bybutter.sisyphus.spi

interface Ordered : Comparable<Ordered> {
    val order: Int

    override fun compareTo(other: Ordered): Int {
        return order.compareTo(other.order)
    }
}
