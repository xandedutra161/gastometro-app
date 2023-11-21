package com.gastometro.despesa.notification.work

import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.hilt.work.HiltWorker
import androidx.work.*
import com.gastometro.despesa.R
import com.gastometro.despesa.data.dao.MovementDao
import com.gastometro.despesa.ui.activities.MainActivity
import com.gastometro.despesa.util.Constants.CHANNEL_ID
import com.gastometro.despesa.util.Constants.CHANNEL_NAME
import com.gastometro.despesa.util.observeOnce
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.concurrent.TimeUnit

@HiltWorker
class MyWorker @AssistedInject constructor(
    @Assisted appContext: Context,
    @Assisted workerParams: WorkerParameters,
    private val movementDao: MovementDao
) : Worker(appContext, workerParams) {

    @SuppressLint("WrongThread")
    override fun doWork(): Result {
        try {
            // Lógica de agendamento aqui...
            val currentHour = getCurrentHour()
            val morningHour = 7
            val eveningHour = 19

            if (currentHour == morningHour || currentHour == eveningHour) {
                // Se a hora atual for igual à hora da manhã ou da noite, execute a função checkDatabaseExpenses()
                checkDatabaseExpenses()
            }
            // Agende a próxima execução...
            scheduleNextExecution(currentHour, morningHour, eveningHour)

            return Result.success()
        } catch (e: Exception) {
            return Result.retry()
        }
    }

    private fun checkDatabaseExpenses() {
        val today = formatDate(Calendar.getInstance().time)
        val handler = Handler(Looper.getMainLooper())

        checkOverduePayments(today, handler)
        checkPendingPayments(today, handler)
    }

    private fun checkOverduePayments(today: String, handler: Handler) {
        val splitDate = today.split("-")
        val day = splitDate[0]
        val month = splitDate[1]
        val year = splitDate[2]
        val id = 1

        val sourceLiveDataOverdue = movementDao.getCountExpensesOrverdue(day, month, year)

        handler.post {
            sourceLiveDataOverdue.observeOnce { count ->
                if (count > 0) {
                    val title = if (count == 1) {
                        "ATENÇÃO: Despesa vencida."
                    } else {
                        "ATENÇÃO: Existem despesas vencidas."
                    }
                    val msg = if (count == 1) {
                        "Total de 1 despesa vencida."
                    } else {
                        "Total de $count despesas vencidas."
                    }
                    showNotification(applicationContext, title, msg, id)
                }
            }
        }
    }

    private fun checkPendingPayments(today: String, handler: Handler) {
        val calendar = Calendar.getInstance()
        calendar.timeInMillis = System.currentTimeMillis()
        val id = 2

        // Data de amanhã
        calendar.add(Calendar.DAY_OF_YEAR, 1)
        val tomorrowCurrent = calendar.time
        val tomorrow = formatDate(tomorrowCurrent)

        val sourceLiveDataPending = movementDao.getCountExpensesNotPaid(today, tomorrow)

        handler.post {
            sourceLiveDataPending.observeOnce { count ->
                if (count > 0) {
                    val title = if (count == 1) {
                        "ATENÇÃO: Despesa prestes a vencer."
                    } else {
                        "ATENÇÃO: Existem despesas prestes a vencer."
                    }
                    val msg = if (count == 1) {
                        "Total de 1 despesa pendente."
                    } else {
                        "Total de $count despesas pendentes."
                    }
                    showNotification(applicationContext, title, msg, id)
                }
            }
        }
    }


    private fun formatDate(date: Date): String {
        val dateFormat = SimpleDateFormat("dd-MM-yyyy", Locale.getDefault())
        return dateFormat.format(date)
    }

    private fun showNotification(context: Context, title: String, message: String, id: Int) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_HIGH
            )
            val notificationManager = context.getSystemService(NotificationManager::class.java)
            notificationManager.createNotificationChannel(channel)
        }

        val intent = Intent(context, MainActivity::class.java)
        intent.putExtra("fragment_to_open", "MovementFragment")

        val pendingIntent =
            PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT)

        val builder = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.baseline_price_check_24)
            .setContentTitle(title)
            .setContentText(message)
            .setPriority(NotificationCompat.PRIORITY_MAX)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true) // Fecha a notificação após ser clicada

        val notificationManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(id, builder.build())
    }

    private fun getCurrentHour(): Int {
        val currentTime = Calendar.getInstance()
        return currentTime.get(Calendar.HOUR_OF_DAY)
    }

    private fun scheduleNextExecution(currentHour: Int, morningHour: Int, eveningHour: Int) {
        val delay: Long

        if (currentHour >= eveningHour) {
            // Se a hora atual for após as 19h, agende para amanhã de manhã
            val tomorrowMorning = Calendar.getInstance().apply {
                add(Calendar.DAY_OF_YEAR, 1)
                set(Calendar.HOUR_OF_DAY, morningHour)
                set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 0)
            }
            delay = tomorrowMorning.timeInMillis - System.currentTimeMillis()
        } else if (currentHour >= morningHour) {
            // Se a hora atual estiver entre as 7h e as 19h, agende para hoje à noite
            val tonight = Calendar.getInstance().apply {
                set(Calendar.HOUR_OF_DAY, eveningHour)
                set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 0)
            }
            delay = tonight.timeInMillis - System.currentTimeMillis()
        } else {
            // Se a hora atual for antes das 7h, agendamos para hoje de manhã às 7h
            val todayMorning = Calendar.getInstance().apply {
                set(Calendar.HOUR_OF_DAY, morningHour)
                set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 0)
            }
            delay = todayMorning.timeInMillis - System.currentTimeMillis()
        }

        Log.i("arroz", "Próxima execução em ${TimeUnit.MILLISECONDS.toMinutes(delay)} minutos")

        val constraints = Constraints.Builder()
            .setRequiresDeviceIdle(false)
            .setRequiresCharging(false)
            .setRequiredNetworkType(NetworkType.NOT_REQUIRED)
            .build()

        val nextWorkRequest = OneTimeWorkRequest.Builder(MyWorker::class.java)
            .setConstraints(constraints)
            .setInitialDelay(delay, TimeUnit.MILLISECONDS)
            .build()

        WorkManager.getInstance(applicationContext)
            .enqueueUniqueWork(
                "my_unique_work",
                ExistingWorkPolicy.REPLACE,
                nextWorkRequest
            )
    }
}
