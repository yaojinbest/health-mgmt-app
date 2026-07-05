package com.opck.health.data.api

import com.opck.health.data.model.ApiResult
import com.opck.health.data.model.ArchiveFile
import com.opck.health.data.model.Consultation
import com.opck.health.data.model.ConsultationMessage
import com.opck.health.data.model.Department
import com.opck.health.data.model.Doctor
import com.opck.health.data.model.EmergencyContact
import com.opck.health.data.model.EmergencyRecord
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
    @POST("api/emergency/contact/save")
    suspend fun saveContact(@Body req: EmergencyContact): ApiResult<EmergencyContact>

    @GET("api/emergency/contact/list")
    suspend fun listContacts(@Query("userId") userId: Long? = null): ApiResult<List<EmergencyContact>>

    @DELETE("api/emergency/contact/{id}")
    suspend fun deleteContact(@Path("id") id: Long): ApiResult<Any?>

    @POST("api/emergency/sos")
    suspend fun sendSos(@Body req: EmergencyRecord): ApiResult<EmergencyRecord>

    @GET("api/emergency/records")
    suspend fun listEmergencyRecords(@Query("userId") userId: Long? = null): ApiResult<List<EmergencyRecord>>

    // ---- Archive ----
    @POST("api/archive/save")
    suspend fun saveArchive(@Body req: HealthArchive): ApiResult<HealthArchive>

    @GET("api/archive/get")
    suspend fun getArchive(@Query("userId") userId: Long): ApiResult<HealthArchive>

    @POST("api/archive/file/upload")
    suspend fun uploadArchiveFile(@Body req: ArchiveFile): ApiResult<ArchiveFile>

    @GET("api/archive/file/list")
    suspend fun listArchiveFiles(@Query("archiveId") archiveId: Long): ApiResult<List<ArchiveFile>>

    // ---- Article ----
    @GET("api/article/list")
    suspend fun listArticles(@Query("keyword") keyword: String? = null): ApiResult<List<HealthArticle>>

    @GET("api/article/{id}")
    suspend fun getArticle(@Path("id") id: Long): ApiResult<HealthArticle>

    // ---- Medical ----
    @GET("api/medical/hospital/list")
    suspend fun listHospitals(): ApiResult<List<Hospital>>

    @GET("api/medical/department/list")
    suspend fun listDepartments(@Query("hospitalId") hospitalId: Long): ApiResult<List<Department>>

    @GET("api/medical/doctor/list")
    suspend fun listDoctors(@Query("departmentId") departmentId: Long): ApiResult<List<Doctor>>

    @GET("api/medical/doctor/all")
    suspend fun listAllDoctors(): ApiResult<List<Doctor>>

    // ---- Consultation ----
    @POST("api/consultation/create")
    suspend fun createConsultation(@Body req: Consultation): ApiResult<Consultation>

    @GET("api/consultation/list")
    suspend fun listConsultations(@Query("userId") userId: Long? = null): ApiResult<List<Consultation>>

    @GET("api/consultation/{id}/messages")
    suspend fun listMessages(@Path("id") id: Long): ApiResult<List<ConsultationMessage>>

    @POST("api/consultation/{id}/message")
    suspend fun sendMessage(
        @Path("id") id: Long,
        @Body message: ConsultationMessage
    ): ApiResult<ConsultationMessage>

    @POST("api/consultation/{id}/close")
    suspend fun closeConsultation(@Path("id") id: Long): ApiResult<Any?>
}
