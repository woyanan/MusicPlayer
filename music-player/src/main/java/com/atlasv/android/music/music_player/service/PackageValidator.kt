package com.atlasv.android.music.music_player.service

import android.content.Context
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.content.res.XmlResourceParser
import android.os.Process
import android.util.Base64
import com.atlasv.android.music.music_player.R
import org.xmlpull.v1.XmlPullParserException
import java.io.IOException
import java.util.*

/**
 * Created by woyanan on 2020-02-10
 */
class PackageValidator {
    private val TAG = javaClass.simpleName

    /**
     * Map allowed callers' certificate keys to the expected caller information.
     */
    private var mValidCertificates: Map<String, ArrayList<CallerInfo>>? =
        null

    fun PackageValidator(ctx: Context) {
        mValidCertificates = readValidCertificates(
            ctx.resources.getXml(
                R.xml.allowed_media_browser_callers
            )
        )
    }

    private fun readValidCertificates(parser: XmlResourceParser): Map<String, ArrayList<CallerInfo>>? {
        val validCertificates =
            HashMap<String, ArrayList<CallerInfo>>()
        try {
            var eventType = parser.next()
            while (eventType != XmlResourceParser.END_DOCUMENT) {
                if (eventType == XmlResourceParser.START_TAG
                    && parser.name == "signing_certificate"
                ) {
                    val name = parser.getAttributeValue(null, "name")
                    val packageName = parser.getAttributeValue(null, "package")
                    val isRelease =
                        parser.getAttributeBooleanValue(null, "release", false)
                    val certificate =
                        parser.nextText().replace("\\s|\\n".toRegex(), "")
                    val info = CallerInfo(name, packageName, isRelease)
                    var infos = validCertificates[certificate]
                    if (infos == null) {
                        infos = ArrayList()
                        validCertificates[certificate] = infos
                    }
                    infos.add(info)
                }
                eventType = parser.next()
            }
        } catch (e: XmlPullParserException) {
            e.printStackTrace()
        } catch (e: IOException) {
            e.printStackTrace()
        }
        return validCertificates
    }

    /**
     * @return false if the caller is not authorized to get data from this MediaBrowserService
     */
    fun isCallerAllowed(
        context: Context,
        callingPackage: String,
        callingUid: Int
    ): Boolean { // Always allow calls from the framework, self app or development environment.
        if (Process.SYSTEM_UID == callingUid || Process.myUid() == callingUid) {
            return true
        }
        if (isPlatformSigned(context, callingPackage)) {
            return true
        }
        val packageInfo = getPackageInfo(context, callingPackage) ?: return false
        if (packageInfo.signatures.size != 1) {
            return false
        }
        val signature = Base64.encodeToString(
            packageInfo.signatures[0].toByteArray(), Base64.NO_WRAP
        )
        // Test for known signatures:
        val validCallers = mValidCertificates!![signature] ?: return false
        // Check if the package name is valid for the certificate:
        val expectedPackages = StringBuffer()
        for (info in validCallers) {
            if (callingPackage == info.packageName) {
                return true
            }
            expectedPackages.append(info.packageName).append(' ')
        }
        return false
    }

    /**
     * @return true if the installed package signature matches the platform signature.
     */
    private fun isPlatformSigned(
        context: Context,
        pkgName: String
    ): Boolean {
        val platformPackageInfo =
            getPackageInfo(context, "android")
        // Should never happen.
        if (platformPackageInfo?.signatures == null || platformPackageInfo.signatures.isEmpty()
        ) {
            return false
        }
        val clientPackageInfo = getPackageInfo(context, pkgName)
        return clientPackageInfo?.signatures != null && clientPackageInfo.signatures.isNotEmpty() && platformPackageInfo.signatures[0] == clientPackageInfo.signatures[0]
    }

    /**
     * @return [PackageInfo] for the package name or null if it's not found.
     */
    private fun getPackageInfo(
        context: Context,
        pkgName: String
    ): PackageInfo? {
        try {
            val pm = context.packageManager
            return pm.getPackageInfo(pkgName, PackageManager.GET_SIGNATURES)
        } catch (e: PackageManager.NameNotFoundException) {
            e.printStackTrace()
        }
        return null
    }

    private class CallerInfo(
        val name: String,
        val packageName: String,
        val release: Boolean
    )
}