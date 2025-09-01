package com.example.expenso.ApiUsage

import com.example.expenso.Data_Class.Add_expense_response
import com.example.expenso.Data_Class.ApiCategoryPostResponse
import com.example.expenso.Data_Class.CategoryResponse
import com.example.expenso.Data_Class.Contact
import com.example.expenso.Data_Class.ExpenseListResponse
import com.example.expenso.Data_Class.ForgotPassword
import com.example.expenso.Data_Class.Get_Budget
import com.example.expenso.Data_Class.RecieveLogInData
import com.example.expenso.Data_Class.RecievePasswordUpdateRequest
import com.example.expenso.Data_Class.RecieveProfileDetails
import com.example.expenso.Data_Class.RecieveProfileInfo
import com.example.expenso.Data_Class.Recieve_Profile_Update
import com.example.expenso.Data_Class.RegisterData
import com.example.expenso.Data_Class.Resend_Otp
import com.example.expenso.Data_Class.Send_AddBudget
import com.example.expenso.Data_Class.TransactionBudget
import com.example.expenso.Data_Class.UpdatePassword
import com.example.expenso.Data_Class.Update_Expense
import com.example.expenso.Data_Class.UserCategoryResponse
import com.example.expenso.Data_Class.VerifyOtp
import com.example.expenso.view.AnalysisData
import com.example.expenso.view.ProfileInfoForLogIn
import com.example.expenso.view.TransactionExpense
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Header
import retrofit2.http.Multipart
import retrofit2.http.Part
import retrofit2.http.DELETE
import retrofit2.http.PUT
import retrofit2.http.Path


interface API {
    @FormUrlEncoded
    @POST("auth/register")
    fun register_user(
        @Field("firstName") firstName: String,
        @Field("lastName") lastName: String,
        @Field("email") email: String,
        @Field("password") password: String,
        @Field("confirmPassword") confirmPassword: String
    ): Call<RegisterData>


    @FormUrlEncoded
    @POST("auth/login")
    fun log_in_user( @Field("email") email:String,
                     @Field("password") password:String):Call<RecieveLogInData>
    @FormUrlEncoded
    @POST("auth/verify-otp")
    fun verify_otp(@Field("email") email:String,
    @Field("otp") otp:String):Call<VerifyOtp>

    @FormUrlEncoded
    @POST("auth/resend-otp")
    fun resend_otp(@Field("email") email:String):Call<Resend_Otp>

    @FormUrlEncoded
    @POST("auth/forget-password")
    fun forgot_passwod(@Field("email") email:String):Call<ForgotPassword>

    @POST("auth/update-password")
    fun update_password(
        @Header("x-access-token") token: String,
        @Body request: UpdatePassword
    ): Call<RecievePasswordUpdateRequest>

    @Multipart
    @POST("user/profile/create")
    fun create_profile(@Header("x-access-token") token: String,
                       @Part("address") address: RequestBody,
                       @Part("phone") phone: RequestBody,
                       @Part profilePic: MultipartBody.Part
    ):Call<RecieveProfileInfo>

    @GET("user/profile")
    fun get_profile_details(@Header("x-access-token") token:String): Call<RecieveProfileDetails>

    @Multipart
    @POST("user/profile/update")
    fun update_profile(@Header("x-access-token") token:String,
                       @Part("address") address: RequestBody,
                       @Part("phone") phone: RequestBody,
                       @Part profilePic: MultipartBody.Part): Call<Recieve_Profile_Update>

    @GET("api/get-default-categories")
    fun get_default_categories():Call<CategoryResponse>

    @GET("api/get-user-categories")
    fun get_user_categories(@Header("x-access-token") token:String):Call<UserCategoryResponse>

    @FormUrlEncoded
    @POST("api/add-category")
    fun add_user_categories(@Header("x-access-token") token:String,@Field("name") name:String):Call<ApiCategoryPostResponse>

    @DELETE("api/delete-category/{id}")
    fun deleteUserCategory(
        @Path("id") categoryId: String,
        @Header("x-access-token") token: String
    ): Call<Void>

    @GET("api/get-expenses")
     fun getExpenses(@Header("x-access-token") token: String): Call<ExpenseListResponse>

    @FormUrlEncoded
     @POST("api/add-expense")
     fun add_expense(@Header("x-access-token") token:String,@Field("amount") amount:String,
                     @Field("categoryId") categoryId:String,@Field("date") date:String
                     ,@Field("note") note:String):Call<Add_expense_response>

    @FormUrlEncoded
    @PUT("api/update-expense/{id}")
    fun updateExpense(
        @Header("x-access-token") token: String,
        @Path("id") id: String,
        @Field("amount") amount: String,
        @Field("categoryId") category: String,  // <-- field name here is "category"
        @Field("date") date: String,
        @Field("note") note: String
    ): Call<Update_Expense>

    @DELETE("api/delete-expense/{id}")
    fun delete_expense(
        @Header("x-access-token") token: String,
        @Path("id") expenseId: String
    ): Call<Void>

    @GET("api/get-budgets")
    fun get_budget(@Header("x-access-token") token:String):Call<Get_Budget>

    @FormUrlEncoded
    @POST("api/set-budget")
    fun add_budget(@Header("x-access-token") token:String,@Field("categoryId") categoryId:String,
                    @Field("amount") amount:String,@Field("frequency") frequency:String):Call<Send_AddBudget>

    @GET("api/get-budget-details")
    fun get_budget_detail(@Header("x-access-token") token:String):Call<AnalysisData>

    @FormUrlEncoded
    @PUT("api/update-budget/{budgetId}")
    fun update_budget(
        @Header("x-access-token") token: String,
        @Path("budgetId") budgetId: String,
        @Field("categoryId") categoryId: String,
        @Field("amount") amount: String,
        @Field("frequency") frequency: String
    ): Call<Send_AddBudget>

    @DELETE("api/delete-budget/{id}")
    fun delete_budget(
        @Header("x-access-token") token: String,
        @Path("id") id: String
    ): Call<Void>

    @GET("api/get-budgets")
    fun get_budget_transaction(@Header("x-access-token") token:String):Call<TransactionBudget>

    @GET("api/get-expenses")
    fun get_Expenses_transaction(@Header("x-access-token") token: String): Call<TransactionExpense>

    @GET("user/profile")
    fun get_profile_details_for_login(@Header("x-access-token") token:String): Call<ProfileInfoForLogIn>
    @GET("api/get-expenses")
    fun get_Expenses_Analysis(@Header("x-access-token") token: String): Call<ExpenseListResponse>


    @POST("api/submit-contact-form")
    fun contct_form(@Body contactRequest: Contact):Call<Contact>









}