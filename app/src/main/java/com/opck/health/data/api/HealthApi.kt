package com.opck.health.data.api

import com.opck.health.data.model.ApiResult
import com.opck.health.data.model.ArchiveFile
import com.opck.health.data.model.Consultation
import com.opck.health.data.model.ConsultationMessage
import com.opck.health.data.model.Department
import com.opck.health.data.model.Doctor
import com.opck.health.data.model.EmergencyContact
import com.opck.health.data.model.EmergencyRecord
import com.opck.health.data.model.SysUser
import com.opck.health.data.model.HealthArchive
import com.opck.health.data.model.HealthArticle
import com.opck.health.data.model.HealthData
import com.opck.health.data.model.Hospital
import com.opck.health.data.model.LoginRequest
import com.opck.health.data.model.LoginVO
import com.opck.health.data.model.MedicineRecord
import com.opck.health.data.model.RegisterRequest
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path
import retrofit2.http.Query

/**
 * REST API 定义 - 与后端 controller 一一对应
 *
 * 注: 所有路径都跟 Controller 的 @RequestMapping + 方法注解 一致。
 *      后端 entity 字段已对齐 (snake_case -> camelCase, Lombok @Data 自动生成 getter)。
 */
interface HealthApi {

    // ---- Auth ----
    @POST("api/auth/login")
    suspend fun login(@Body req: LoginRequest): ApiResult<LoginVO>

    @POST("api/auth/register")
    suspend fun register(@Body req: RegisterRequest): ApiResult<LoginVO>

    // ---- Health Data ----
    @POST("api/health-data/save")
    suspend fun saveHealthData(@Body data: HealthData): ApiResult<HealthData>

    @GET("api/health-data/list")
    suspend fun listHealthData(@Query("userId") userId: Long? = null): ApiResult<List<HealthData>>

    @GET("api/health-data/chart")
    suspend fun chartHealthData(@Query("userId") userId: Long): ApiResult<List<HealthData>>

    // ---- Medicine ----
    @POST("api/medicine/save")
    suspend fun saveMedicine(@Body req: MedicineRecord): ApiResult<MedicineRecord>

    @GET("api/medicine/list")
    suspend fun listMedicines(@Query("userId") userId: Long? = null): ApiResult<List<MedicineRecord>>

    @POST("api/medicine/finish/{id}")
    suspend fun finishMedicine(@Path("id") id: Long): ApiResult<Any?>

    // ---- Emergency ----
    // 后端: EmergencyController @RequestMapping("/api/emergency")
    @POST("api/emergency/contact/save")
    suspend fun saveContact(@Body req: EmergencyContact): ApiResult<EmergencyContact>

    @GET("api/emergency/contacts")     // 注意: 是 contacts (复数)
    suspend fun listContacts(@Query("userId") userId: Long? = null): ApiResult<List<EmergencyContact>>

    @POST("api/emergency/contact/delete/{id}")  // 后端用 @PostMapping 不是 DELETE
    suspend fun deleteContact(@Path("id") id: Long): ApiResult<Any?>

    @POST("api/emergency/help")         // sos -> help
    suspend fun sendSos(@Body req: EmergencyRecord): ApiResult<EmergencyRecord>

    @GET("api/emergency/records")
    suspend fun listEmergencyRecords(@Query("userId") userId: Long? = null): ApiResult<List<EmergencyRecord>>

    // ---- Archive ----
    // 后端: ArchiveController @RequestMapping("/api/archive")
    @POST("api/archive/save")
    suspend fun saveArchive(@Body req: HealthArchive): ApiResult<HealthArchive>

    @GET("api/archive/{userId}")        // 注意: 路径参数 userId, 不是 query
    suspend fun getArchive(@Path("userId") userId: Long): ApiResult<HealthArchive>

    @POST("api/archive/{archiveId}/upload")  // 文件上传, Body 里是 ArchiveFile
    suspend fun uploadArchiveFile(@Path("archiveId") archiveId: Long, @Body req: ArchiveFile): ApiResult<ArchiveFile>

    @GET("api/archive/list")            // 注意: 没有 /file/ 前缀
    suspend fun listArchiveFiles(): ApiResult<List<ArchiveFile>>

    // ---- Articles (复数!) ----
    // 后端: ArticleController @RequestMapping("/api/articles")
    @GET("api/articles")
    suspend fun listArticles(
        @Query("category") category: String? = null,
        @Query("keyword") keyword: String? = null,
        @Query("diseaseTag") diseaseTag: String? = null
    ): ApiResult<List<HealthArticle>>

    @GET("api/articles/{id}")
    suspend fun getArticle(@Path("id") id: Long): ApiResult<HealthArticle>

    // ---- Medical ----
    // 后端: MedicalController @RequestMapping("/api/medical")
    @GET("api/medical/hospitals")       // 注意: 复数
    suspend fun listHospitals(): ApiResult<List<Hospital>>

    @GET("api/medical/departments")     // 注意: 复数
    suspend fun listDepartments(@Query("hospitalId") hospitalId: Long): ApiResult<List<Department>>

    @GET("api/medical/doctors")         // 注意: 复数
    suspend fun listDoctors(@Query("departmentId") departmentId: Long? = null): ApiResult<List<Doctor>>

    @POST("api/medical/appointment/create")
    suspend fun createAppointment(@Body req: Any): ApiResult<Any?>

    @POST("api/medical/appointment/cancel/{id}")
    suspend fun cancelAppointment(@Path("id") id: Long): ApiResult<Any?>

    @GET("api/medical/appointments")
    suspend fun listAppointments(
        @Query("userId") userId: Long? = null,
        @Query("doctorId") doctorId: Long? = null
    ): ApiResult<List<Map<String, Any>>>

    @GET("api/medical/schedules")
    suspend fun listSchedules(@Query("doctorId") doctorId: Long): ApiResult<List<Map<String, Any>>>

    // ---- Users (admin only) ----
    @GET("api/users")
    suspend fun listUsers(@Query("role") role: String? = null): ApiResult<List<SysUser>>

    @POST("api/users/save")
    suspend fun saveUser(@Body user: SysUser): ApiResult<SysUser>

    // ---- Consultations (复数!) ----
    // 后端: ConsultationController @RequestMapping("/api/consultations")
    @POST("api/consultations/create")
    suspend fun createConsultation(@Body req: Consultation): ApiResult<Consultation>

    @GET("api/consultations")           // 注意: 复数
    suspend fun listConsultations(@Query("userId") userId: Long? = null): ApiResult<List<Consultation>>

    @GET("api/consultations/{consultationId}/messages")  // 路径参数名要跟后端 @PathVariable 一致
    suspend fun listMessages(@Path("consultationId") id: Long): ApiResult<List<ConsultationMessage>>

    @POST("api/consultations/message/send")  // 注意: 不是 /{id}/message
    suspend fun sendMessage(@Body message: ConsultationMessage): ApiResult<ConsultationMessage>

    @POST("api/consultations/close/{id}")
    suspend fun closeConsultation(@Path("id") id: Long): ApiResult<Any?>

    // ---- Dashboard (admin/doctor 统计页) ----
    // 后端: DashboardController @RequestMapping("/api/dashboard")
    @GET("api/dashboard/admin")
    suspend fun dashboardAdmin(): ApiResult<Map<String, Any>>

    @GET("api/dashboard/doctor/{doctorId}")
    suspend fun dashboardDoctor(@Path("doctorId") doctorId: Long): ApiResult<Map<String, Any>>

    // 后端: StatsController @RequestMapping("/api/admin/stats")
    @GET("api/admin/stats/overview")
    suspend fun statsOverview(): ApiResult<Map<String, Any>>

    @GET("api/admin/stats/health-trends")
    suspend fun statsHealthTrends(@Query("days") days: Int = 7): ApiResult<Map<String, Any>>

    @GET("api/admin/stats/consultation-trends")
    suspend fun statsConsultationTrends(@Query("days") days: Int = 7): ApiResult<Map<String, Any>>

    @GET("api/admin/stats/user-distribution")
    suspend fun statsUserDistribution(): ApiResult<Map<String, Any>>

    @GET("api/admin/stats/warn-top")
    suspend fun statsWarnTop(@Query("limit") limit: Int = 10): ApiResult<Map<String, Any>>
}