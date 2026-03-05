package com.sonicsignature.model

enum class BudgetTier(
        val maxPrice: Int,
        val displayName: String,
        val label: String,
        val range: String
) {
    ULTRA_BUDGET(2500, "Ultra Budget", "Ultra-Budget", "<₹1000"),
    ENTRY(10000, "Entry Level", "Entry", "₹2,500–₹10,000"),
    MID_RANGE(40000, "Mid-Fi", "Mid-Range", "₹10,000–₹40,000"),
    HIGH_END(100000, "High-End / Summit-Fi", "High-End", ">₹80,000")
}
