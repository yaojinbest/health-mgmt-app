package com.opck.health.data.model

/**
 * 健康数据 (对应 health_data 表)
 *
 * 一个用户一天可有多个数据点 (三餐前血糖, 早晚血压等)
 */
data class HealthData(
    val id: Long? = null,
    val userId: Long,
    val systolic: Int? = null,
    val diastolic: Int? = null,
    val bloodSugar: java.math.BigDecimal? = null,
    val heartRate: Int? = null,
    val steps: Int? = null,
    val sleepHours: java.math.BigDecimal? = null,
    val weight: java.math.BigDecimal? = null,
    val warningLevel: String? = null,
    val warningMessage: String? = null,
    val recordTime: String? = null
)

/**
 * 用药记录
 */
data class MedicineRecord(
    val id: Long? = null,
    val userId: Long? = null,
    val medicineName: String,
    val usageMethod: String? = null,
    val dosage: String? = null,
    val reminderTimes: String? = null,
    val startDate: String? = null,
    val endDate: String? = null,
    val status: String = "ACTIVE",
    val warning: String? = null,
    val createTime: String? = null
)

/**
 * 紧急联系人
 */
data class EmergencyContact(
    val id: Long? = null,
    val userId: Long? = null,
    val name: String,
    val relation: String? = null,
    val phone: String,
    val sortNo: Int? = null
)

/**
 * 紧急求救记录
 */
data class EmergencyRecord(
    val id: Long? = null,
    val userId: Long? = null,
    val locationText: String? = null,
    val latitude: String? = null,
    val longitude: String? = null,
    val contactSnapshot: String? = null,
    val status: String? = null,
    val helpTime: String? = null,
    val result: String? = null
)

/**
 * 健康档案
 */
data class HealthArchive(
    val id: Long? = null,
    val userId: Long? = null,
    val name: String? = null,
    val age: Int? = null,
    val gender: String? = null,
    val height: java.math.BigDecimal? = null,
    val weight: java.math.BigDecimal? = null,
    val bloodType: String? = null,
    val diseaseHistory: String? = null,
    val allergyHistory: String? = null,
    val commonMedicine: String? = null,
    val privacyLevel: String? = null,
    val updateTime: String? = null
)

/**
 * 档案附件
 */
data class ArchiveFile(
    val id: Long? = null,
    val archiveId: Long? = null,
    val fileName: String,
    val fileUrl: String,
    val uploadTime: String? = null
)

/**
 * 健康文章
 */
data class HealthArticle(
    val id: Long? = null,
    val title: String,
    val category: String? = null,
    val diseaseTag: String? = null,
    val summary: String? = null,
    val content: String? = null,
    val authorId: Long? = null,
    val viewCount: Int? = null,
    val publishTime: String? = null
)

/**
 * 医院
 */
data class Hospital(
    val id: Long? = null,
    val name: String,
    val level: String? = null,
    val address: String? = null,
    val phone: String? = null
)

/**
 * 科室
 */
data class Department(
    val id: Long? = null,
    val hospitalId: Long? = null,
    val name: String,
    val description: String? = null
)

/**
 * 医生
 */
data class Doctor(
    val id: Long? = null,
    val userId: Long? = null,
    val hospitalId: Long? = null,
    val departmentId: Long? = null,
    val doctorName: String? = null,
    val title: String? = null,
    val specialty: String? = null,
    val profile: String? = null,
    val status: String? = null
)

/**
 * 在线咨询会话
 */
data class Consultation(
    val id: Long? = null,
    val userId: Long? = null,
    val doctorId: Long? = null,
    val title: String,
    val status: String = "OPEN",
    val createTime: String? = null,
    val followUpTime: String? = null,
    val doctorName: String? = null,
    val messageCount: Int? = null
)

/**
 * 咨询消息
 */
data class ConsultationMessage(
    val id: Long? = null,
    val consultationId: Long? = null,
    val senderId: Long? = null,
    val senderRole: String?,
    val messageType: String? = null,
    val content: String,
    val sendTime: String? = null
)

/**
 * 预约
 */
data class Appointment(
    val id: Long? = null,
    val userId: Long? = null,
    val userName: String? = null,
    val hospitalId: Long? = null,
    val hospitalName: String? = null,
    val departmentId: Long? = null,
    val departmentName: String? = null,
    val doctorId: Long? = null,
    val doctorName: String? = null,
    val scheduleId: Long? = null,
    val appointmentDate: String? = null,
    val timeSlot: String? = null,
    val status: String? = null,
    val symptom: String? = null,
    val createTime: String? = null,
    val remindTime: String? = null
)

/**
 * 医生排班
 */
data class DoctorSchedule(
    val id: Long? = null,
    val doctorId: Long? = null,
    val scheduleDate: String? = null,
    val timeSlot: String? = null,
    val totalQuota: Int? = null,
    val remainQuota: Int? = null
)
