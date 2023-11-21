package com.gastometro.despesa.notification

import android.app.*
import android.content.Intent
import android.os.IBinder
import androidx.work.*
import com.gastometro.despesa.notification.work.MyWorker


class MyForegroundService : Service() {

    private val workManager = WorkManager.getInstance(this)

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        // Inicie o código que deseja executar quando o serviço é iniciado
        startMyWorker()
        return START_STICKY
    }


    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    private fun startMyWorker() {
        val constraints = Constraints.Builder()
            .setRequiresDeviceIdle(false)
            .setRequiresCharging(false)
            .setRequiredNetworkType(NetworkType.NOT_REQUIRED)
            .build()

        val workRequest = OneTimeWorkRequest.Builder(MyWorker::class.java)
            .setConstraints(constraints)
            .build()


        workManager.enqueue(workRequest)
    }

}
