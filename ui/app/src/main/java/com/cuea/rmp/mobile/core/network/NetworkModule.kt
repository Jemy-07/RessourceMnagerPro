package com.cuea.rmp.mobile.core.network

import com.cuea.rmp.mobile.BuildConfig
import com.cuea.rmp.mobile.auth.AuthApi
import com.cuea.rmp.mobile.auth.RefreshAuthApi
import com.cuea.rmp.mobile.budget.BudgetApi
import com.cuea.rmp.mobile.notification.DeviceApi
import com.cuea.rmp.mobile.notification.NotificationApi
import com.cuea.rmp.mobile.project.AssignmentApi
import com.cuea.rmp.mobile.project.ProjectApi
import com.cuea.rmp.mobile.request.RequestApi
import com.cuea.rmp.mobile.resource.ResourceApi
import com.cuea.rmp.mobile.resource.SkillApi
import com.cuea.rmp.mobile.timesheet.TimesheetApi
import com.cuea.rmp.mobile.user.UserApi
import com.jakewharton.retrofit2.converter.kotlinx.serialization.asConverterFactory
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import java.util.concurrent.TimeUnit
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    @Provides
    @Singleton
    @OptIn(ExperimentalSerializationApi::class)
    fun provideJson(): Json {
        return Json {
            ignoreUnknownKeys = true
            explicitNulls = false
            isLenient = true
        }
    }

    @Provides
    @Singleton
    fun provideLoggingInterceptor(): HttpLoggingInterceptor {
        return HttpLoggingInterceptor().apply {
            level = if (BuildConfig.DEBUG) {
                HttpLoggingInterceptor.Level.BODY
            } else {
                HttpLoggingInterceptor.Level.NONE
            }
        }
    }

    @Provides
    @Singleton
    @RefreshClient
    fun provideRefreshClient(loggingInterceptor: HttpLoggingInterceptor): OkHttpClient {
        return OkHttpClient.Builder()
            .addInterceptor(loggingInterceptor)
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .build()
    }

    @Provides
    @Singleton
    @AuthenticatedClient
    fun provideAuthenticatedClient(
        authInterceptor: AuthInterceptor,
        tokenAuthenticator: TokenAuthenticator,
        loggingInterceptor: HttpLoggingInterceptor
    ): OkHttpClient {
        return OkHttpClient.Builder()
            .addInterceptor(authInterceptor)
            .authenticator(tokenAuthenticator)
            .addInterceptor(loggingInterceptor)
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .build()
    }

    @Provides
    @Singleton
    @RefreshRetrofit
    fun provideRefreshRetrofit(
        json: Json,
        @RefreshClient client: OkHttpClient
    ): Retrofit {
        return Retrofit.Builder()
            .baseUrl(BuildConfig.BASE_URL)
            .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
            .client(client)
            .build()
    }

    @Provides
    @Singleton
    @AuthenticatedRetrofit
    fun provideAuthenticatedRetrofit(
        json: Json,
        @AuthenticatedClient client: OkHttpClient
    ): Retrofit {
        return Retrofit.Builder()
            .baseUrl(BuildConfig.BASE_URL)
            .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
            .client(client)
            .build()
    }

    @Provides
    @Singleton
    fun provideAuthApi(@AuthenticatedRetrofit retrofit: Retrofit): AuthApi = retrofit.create(AuthApi::class.java)

    @Provides
    @Singleton
    fun provideRefreshAuthApi(@RefreshRetrofit retrofit: Retrofit): RefreshAuthApi {
        return retrofit.create(RefreshAuthApi::class.java)
    }

    @Provides
    @Singleton
    fun provideUserApi(@AuthenticatedRetrofit retrofit: Retrofit): UserApi = retrofit.create(UserApi::class.java)

    @Provides
    @Singleton
    fun provideResourceApi(@AuthenticatedRetrofit retrofit: Retrofit): ResourceApi {
        return retrofit.create(ResourceApi::class.java)
    }

    @Provides
    @Singleton
    fun provideSkillApi(@AuthenticatedRetrofit retrofit: Retrofit): SkillApi = retrofit.create(SkillApi::class.java)

    @Provides
    @Singleton
    fun provideProjectApi(@AuthenticatedRetrofit retrofit: Retrofit): ProjectApi {
        return retrofit.create(ProjectApi::class.java)
    }

    @Provides
    @Singleton
    fun provideAssignmentApi(@AuthenticatedRetrofit retrofit: Retrofit): AssignmentApi {
        return retrofit.create(AssignmentApi::class.java)
    }

    @Provides
    @Singleton
    fun provideRequestApi(@AuthenticatedRetrofit retrofit: Retrofit): RequestApi {
        return retrofit.create(RequestApi::class.java)
    }

    @Provides
    @Singleton
    fun provideTimesheetApi(@AuthenticatedRetrofit retrofit: Retrofit): TimesheetApi {
        return retrofit.create(TimesheetApi::class.java)
    }

    @Provides
    @Singleton
    fun provideBudgetApi(@AuthenticatedRetrofit retrofit: Retrofit): BudgetApi = retrofit.create(BudgetApi::class.java)

    @Provides
    @Singleton
    fun provideNotificationApi(@AuthenticatedRetrofit retrofit: Retrofit): NotificationApi {
        return retrofit.create(NotificationApi::class.java)
    }

    @Provides
    @Singleton
    fun provideDeviceApi(@AuthenticatedRetrofit retrofit: Retrofit): DeviceApi {
        return retrofit.create(DeviceApi::class.java)
    }
}

