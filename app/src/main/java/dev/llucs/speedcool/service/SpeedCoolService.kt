package dev.llucs.speedcool.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Intent
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import dev.llucs.speedcool.core.*
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.first
import java.util.Calendar

class SpeedCoolService : Service() {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    private lateinit var settings: AppSettings
    private lateinit var logger: SpeedCoolLogger
    private val probe = DeviceProbe()
    private val profiles = ProfilesEngine(probe)
    private val statusRepo = StatusRepository(probe)
    private val sampler = MetricsSampler()

    private var running = false
    private var lastApplied: ProfileId? = null
    private var coolingOverride: Boolean = false
    private var userProfile: ProfileId = ProfileId.BALANCED

    override fun onCreate() {
        super.onCreate()
        settings = AppSettings(this)
        logger = SpeedCoolLogger(this)
        startForeground(1, buildNotification("Starting…"))
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (!running) {
            running = true
            scope.launch { mainLoop() }
        }
        return START_STICKY
    }

    override fun onDestroy() {
        running = false
        scope.cancel()
        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? = null

    private fun buildNotification(text: String): Notification {
        val channelId = "speedcool"
        if (Build.VERSION.SDK_INT >= 26) {
            val nm = getSystemService(NotificationManager::class.java)
            val ch = NotificationChannel(channelId, "SpeedCool", NotificationManager.IMPORTANCE_LOW)
            nm.createNotificationChannel(ch)
        }
        return NotificationCompat.Builder(this, channelId)
            .setSmallIcon(android.R.drawable.stat_notify_sync)
            .setContentTitle("SpeedCool")
            .setContentText(text)
            .setOngoing(true)
            .build()
    }

    private suspend fun mainLoop() {
        if (!RootShell.isRootAvailable()) {
            logger.log("Service", "Root not available. Stopping service.")
            stopSelf()
            return
        }

        logger.log("Service", "SpeedCool engine started.")

        while (running) {
            runCatching {
                val engineEnabled = settings.enableEngine.first()
                if (!engineEnabled) {
                    updateNotif("Engine disabled")
                    delay(3000)
                    return@runCatching
                }

                val desiredUserProfile = settings.activeProfile.first()
                userProfile = desiredUserProfile

                val weeklyMask = settings.weeklyMask.first()
                val scheduledProfile = scheduledProfileForToday(weeklyMask) ?: desiredUserProfile

                val enableCooling = settings.enableCooling.first()
                val enableLearning = settings.enableLearning.first()

                val onC = settings.coolingOnC.first()
                val offC = settings.coolingOffC.first()

                val metrics = sampler.sample()
                val tempC = statusRepo.readStatus(desiredUserProfile).temperatureC

                var targetProfile = scheduledProfile

                if (enableLearning) {
                    targetProfile = decideProfileByLearning(metrics, tempC, scheduledProfile)
                }

                if (enableCooling && tempC != null) {
                    if (!coolingOverride && tempC >= onC) {
                        coolingOverride = true
                        logger.log("Cooling", "Temperature ${"%.1f".format(tempC)}C >= $onC C. Cooling override ON.")
                    } else if (coolingOverride && tempC <= offC) {
                        coolingOverride = false
                        logger.log("Cooling", "Temperature ${"%.1f".format(tempC)}C <= $offC C. Cooling override OFF.")
                    }
                    if (coolingOverride) targetProfile = ProfileId.COOLING
                }

                if (lastApplied != targetProfile) {
                    val report = profiles.apply(targetProfile, logger)
                    lastApplied = report.profile
                }

                updateNotif(buildStatusLine(metrics, tempC, lastApplied ?: targetProfile))
            }.onFailure {
                logger.log("Service", "Loop error: ${it.message}")
            }

            delay(5000)
        }
    }

    private fun updateNotif(text: String) {
        val nm = getSystemService(NotificationManager::class.java)
        nm.notify(1, buildNotification(text))
    }

    private fun buildStatusLine(metrics: Metrics, tempC: Double?, profile: ProfileId): String {
        val t = tempC?.let { "${"%.1f".format(it)}C" } ?: "N/A"
        return "${profile.displayName} • CPU ${metrics.cpuUsagePct}% • RAM ${metrics.ramUsagePct}% • $t"
    }

    private fun scheduledProfileForToday(mask: Int): ProfileId? {
        if (mask == 0) return null
        val cal = Calendar.getInstance()
        val day = cal.get(Calendar.DAY_OF_WEEK) // 1=Sunday
        val bit = when (day) {
            Calendar.MONDAY -> 0
            Calendar.TUESDAY -> 1
            Calendar.WEDNESDAY -> 2
            Calendar.THURSDAY -> 3
            Calendar.FRIDAY -> 4
            Calendar.SATURDAY -> 5
            Calendar.SUNDAY -> 6
            else -> 6
        }
        val enabled = (mask shr bit) and 1
        return if (enabled == 1) ProfileId.PERFORMANCE else null
    }

    private fun decideProfileByLearning(metrics: Metrics, tempC: Double?, baseline: ProfileId): ProfileId {
        val idleHours = Calendar.getInstance().get(Calendar.HOUR_OF_DAY) in 1..6
        if (idleHours && metrics.cpuUsagePct < 15 && metrics.ramUsagePct < 60) return ProfileId.ECO

        if (tempC != null && tempC >= 48.0) return ProfileId.COOLING

        if (metrics.cpuUsagePct >= 75 || metrics.ramUsagePct >= 85) return ProfileId.PERFORMANCE

        return baseline
    }
}
