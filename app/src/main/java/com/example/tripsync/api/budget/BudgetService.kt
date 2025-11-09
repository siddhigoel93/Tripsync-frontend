package com.example.tripsync.api.budget

import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.POST

interface BudgetService {

    @DELETE("api/expense/budget/")
    suspend fun deleteBudget(): Response<ResponseBody>

    @POST("api/expense/budget/")
    suspend fun createBudget(@Body body: CreateBudgetRequest): Response<ResponseBody>
}
