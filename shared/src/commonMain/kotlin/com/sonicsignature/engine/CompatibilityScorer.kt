package com.sonicsignature.engine

import com.sonicsignature.model.BudgetTier
import com.sonicsignature.model.IEMRecommendation
import com.sonicsignature.model.UserSonicProfile

object CompatibilityScorer {

        fun scoreMatch(
                profile: UserSonicProfile,
                recommendation: IEMRecommendation,
                targetBudget: BudgetTier
        ): Int {
                return 100
        }
}
