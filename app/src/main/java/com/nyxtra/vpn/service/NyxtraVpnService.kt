package com.nyxtra.vpn.service

import android.content.Intent
import android.net.VpnService
import android.os.ParcelFileDescriptor

/**
 * NyxtraVpnService handles the Android VpnService lifecycle.
 * In Phase 2, this will directly pass the ParcelFileDescriptor (FD) to the
 * embedded Sing-box Go binary without local proxy loops.
 */
class NyxtraVpnService : VpnService() {

    private var vpnInterface: ParcelFileDescriptor? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        return START_STICKY
    }

    override fun onDestroy() {
        super.onDestroy()
        vpnInterface?.close()
        vpnInterface = null
    }

    companion object {
        const val ACTION_CONNECT = "com.nyxtra.vpn.CONNECT"
        const val ACTION_DISCONNECT = "com.nyxtra.vpn.DISCONNECT"
    }
}
