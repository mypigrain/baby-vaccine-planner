package com.vaccineplanner.data.model

import java.time.LocalDate

data class Baby(
    val id: String = "",
    val name: String = "",
    val birthDate: LocalDate = LocalDate.now()
)

data class VaccineSchedule(
    val babyId: String,
    val schedules: List<VaccinationRecord>
)

data class VaccinationRecord(
    val id: String,
    val vaccine: Vaccine,
    val scheduledDate: LocalDate,
    val isCompleted: Boolean = false,
    val completedDate: LocalDate? = null,
    val isReplaced: Boolean = false,
    val replacingVaccine: Vaccine? = null,
    val doseNumber: Int = 1
)

data class Vaccine(
    val id: String,
    val name: String,
    val chineseName: String,
    val description: String,
    val diseaseInfo: DiseaseInfo,
    val price: Double = 0.0,
    val isFree: Boolean = true,
    val doses: Int = 1,
    val intervalDays: List<Int> = listOf(),
    val replaceableVaccines: List<String> = listOf(),
    val category: VaccineCategory = VaccineCategory.ROUTINE,
    val ageRange: String = "",
    val notes: String = ""
)

data class DiseaseInfo(
    val name: String,
    val description: String,
    val incidenceData: List<EpidemicData> = listOf(),
    val outcomes: DiseaseOutcomes? = null,
    val comparisonWithFree: String? = null
)

data class EpidemicData(
    val title: String,
    val value: String,
    val source: String,
    val details: List<EpidemicData> = listOf()
)

data class DiseaseOutcomes(
    val best: String,
    val worst: String,
    val typical: String
)

enum class VaccineCategory {
    ROUTINE,
    VOLUNTARY,
    REPLACEMENT
}

enum class VaccineType {
    FREE,
    PAID
}
