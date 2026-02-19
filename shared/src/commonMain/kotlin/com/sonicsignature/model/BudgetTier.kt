package com.sonicsignature.model

enum class BudgetTier(val label: String, val range: String) {
    ULTRA_BUDGET("Ultra-Budget", "<₹2,000"),
    ENTRY("Entry", "₹2,000–₹6,500"),
    MID_RANGE("Mid-Range", "₹8,000–₹40,000"),
    HIGH_END("High-End", ">₹80,000")
}
