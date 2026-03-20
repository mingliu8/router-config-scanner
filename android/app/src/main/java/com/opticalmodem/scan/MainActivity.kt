package com.opticalmodem.scan

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.net.wifi.WifiConfiguration
import android.net.wifi.WifiManager
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.journeyapps.barcodescanner.ScanContract
import com.journeyapps.barcodescanner.ScanOptions
import com.opticalmodem.scan.databinding.ActivityMainBinding
import com.permissionx.guolindev.PermissionX
import okhttp3.*
import org.json.JSONObject
import java.io.IOException
import java.net.URL

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private val client = OkHttpClient()

    private val barcodeLauncher = registerForActivityResult(ScanContract()) {
        if (it.contents != null) {
            handleScanResult(it.contents)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.scanButton.setOnClickListener {
            checkPermissions()
        }

        binding.checkConnectionButton.setOnClickListener {
            checkConnectionStatus()
        }
    }

    private fun checkPermissions() {
        PermissionX.init(this)
            .permissions(android.Manifest.permission.CAMERA)
            .request {allGranted, _, _ ->
                if (allGranted) {
                    startScan()
                } else {
                    Toast.makeText(this, "需要相机权限才能扫码", Toast.LENGTH_SHORT).show()
                }
            }
    }

    private fun startScan() {
        val options = ScanOptions()
        options.setDesiredBarcodeFormats(ScanOptions.QR_CODE)
        options.setPrompt("请扫描路由器二维码")
        options.setCameraId(0)
        options.setBeepEnabled(true)
        options.setBarcodeImageEnabled(true)
        barcodeLauncher.launch(options)
    }

    private fun handleScanResult(result: String) {
        try {
            // 解析不同格式的二维码
            val parsedInfo = parseQrCode(result)
            if (parsedInfo != null) {
                val ip = parsedInfo["ip"] ?: ""
                val username = parsedInfo["username"] ?: ""
                val password = parsedInfo["password"] ?: ""
                val ssid = parsedInfo["ssid"] ?: ""
                val wifiPassword = parsedInfo["wifi_password"] ?: ""
                
                if (ip.isNotEmpty() && username.isNotEmpty() && password.isNotEmpty()) {
                    Toast.makeText(this, "扫码成功，正在登录...", Toast.LENGTH_SHORT).show()
                    loginRouter(ip, username, password)
                } else if (ssid.isNotEmpty() && wifiPassword.isNotEmpty()) {
                    Toast.makeText(this, "扫码成功，正在连接WiFi...", Toast.LENGTH_SHORT).show()
                    connectToWifi(ssid, wifiPassword)
                } else {
                    Toast.makeText(this, "二维码信息不完整", Toast.LENGTH_SHORT).show()
                }
            } else {
                Toast.makeText(this, "二维码格式错误", Toast.LENGTH_SHORT).show()
            }
        } catch (e: Exception) {
            Toast.makeText(this, "解析二维码失败: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    private fun connectToWifi(ssid: String, password: String) {
        // 检查WiFi权限
        PermissionX.init(this)
            .permissions(
                android.Manifest.permission.ACCESS_FINE_LOCATION,
                android.Manifest.permission.ACCESS_COARSE_LOCATION
            )
            .request { allGranted, _, _ ->
                if (allGranted) {
                    doConnectToWifi(ssid, password)
                } else {
                    Toast.makeText(this, "需要位置权限才能连接WiFi", Toast.LENGTH_SHORT).show()
                }
            }
    }

    private fun doConnectToWifi(ssid: String, password: String) {
        val wifiManager = getSystemService(Context.WIFI_SERVICE) as WifiManager
        
        if (!wifiManager.isWifiEnabled) {
            wifiManager.isWifiEnabled = true
            Toast.makeText(this, "正在开启WiFi...", Toast.LENGTH_SHORT).show()
        }
        
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                // Android 10及以上
                val networkSpecifier = android.net.wifi.WifiNetworkSpecifier.Builder()
                    .setSsid(ssid)
                    .setWpa2Passphrase(password)
                    .build()
                
                val networkRequest = android.net.NetworkRequest.Builder()
                    .addTransportType(android.net.NetworkCapabilities.TRANSPORT_WIFI)
                    .setNetworkSpecifier(networkSpecifier)
                    .build()
                
                val connectivityManager = getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
                connectivityManager.requestNetwork(networkRequest, object : ConnectivityManager.NetworkCallback() {
                    override fun onAvailable(network: android.net.Network) {
                        super.onAvailable(network)
                        runOnUiThread {
                            Toast.makeText(this@MainActivity, "WiFi连接成功！", Toast.LENGTH_SHORT).show()
                            binding.statusText.text = "WiFi状态：已连接到 $ssid"
                        }
                    }
                    
                    override fun onUnavailable() {
                        super.onUnavailable()
                        runOnUiThread {
                            Toast.makeText(this@MainActivity, "WiFi连接失败", Toast.LENGTH_SHORT).show()
                            binding.statusText.text = "WiFi状态：连接失败"
                        }
                    }
                })
            } else {
                // Android 10以下
                val wifiConfig = WifiConfiguration()
                wifiConfig.SSID = "\"$ssid\""
                wifiConfig.preSharedKey = "\"$password\""
                
                val networkId = wifiManager.addNetwork(wifiConfig)
                if (networkId != -1) {
                    wifiManager.disconnect()
                    wifiManager.enableNetwork(networkId, true)
                    wifiManager.reconnect()
                    Toast.makeText(this, "WiFi连接成功！", Toast.LENGTH_SHORT).show()
                    binding.statusText.text = "WiFi状态：已连接到 $ssid"
                } else {
                    Toast.makeText(this, "WiFi连接失败", Toast.LENGTH_SHORT).show()
                    binding.statusText.text = "WiFi状态：连接失败"
                }
            }
        } catch (e: Exception) {
            Toast.makeText(this, "连接WiFi失败: ${e.message}", Toast.LENGTH_SHORT).show()
            binding.statusText.text = "WiFi状态：连接失败"
        }
    }

    private fun parseQrCode(content: String): Map<String, String>? {
        val result = mutableMapOf<String, String>()
        
        // 格式1: ip|username|password
        val parts = content.split("|")
        if (parts.size == 3) {
            result["ip"] = parts[0]
            result["username"] = parts[1]
            result["password"] = parts[2]
            return result
        }
        
        // 格式2: JSON格式
        if (content.startsWith("{") && content.endsWith("}")) {
            try {
                val json = JSONObject(content)
                if (json.has("ip") && json.has("username") && json.has("password")) {
                    result["ip"] = json.getString("ip")
                    result["username"] = json.getString("username")
                    result["password"] = json.getString("password")
                    return result
                }
            } catch (e: Exception) {
                // JSON解析失败，继续尝试其他格式
            }
        }
        
        // 格式3: URL格式，如 http://192.168.1.1/login?user=admin&pass=admin
        if (content.startsWith("http")) {
            try {
                val url = java.net.URL(content)
                val query = url.query
                if (query != null) {
                    val params = query.split("&")
                    for (param in params) {
                        val keyValue = param.split("=")
                        if (keyValue.size == 2) {
                            val key = keyValue[0].lowercase()
                            val value = keyValue[1]
                            when (key) {
                                "ip", "address" -> result["ip"] = value
                                "user", "username", "name" -> result["username"] = value
                                "pass", "password", "pwd" -> result["password"] = value
                            }
                        }
                    }
                    // 从URL中提取IP
                    if (!result.containsKey("ip")) {
                        val host = url.host
                        if (host.isNotEmpty()) {
                            result["ip"] = host
                        }
                    }
                    if (result.containsKey("ip") && result.containsKey("username") && result.containsKey("password")) {
                        return result
                    }
                }
            } catch (e: Exception) {
                // URL解析失败，继续尝试其他格式
            }
        }
        
        // 格式4: 文本格式，包含IP、用户名、密码
        val ipPattern = Regex("\b(?:\d{1,3}\.){3}\d{1,3}\b")
        val ipMatch = ipPattern.find(content)
        if (ipMatch != null) {
            result["ip"] = ipMatch.value
            
            // 尝试提取用户名和密码
            val userPattern = Regex("(?:user|username|账户)[:：]\s*([^\s,]+)")
            val passPattern = Regex("(?:pass|password|密码)[:：]\s*([^\s,]+)")
            
            userPattern.find(content)?.let { result["username"] = it.groupValues[1] }
            passPattern.find(content)?.let { result["password"] = it.groupValues[1] }
            
            if (result.containsKey("username") && result.containsKey("password")) {
                return result
            }
        }
        
        // 格式5: WiFi二维码格式，如 WIFI:S:SSID;T:WPA;P:PASSWORD;;
        if (content.startsWith("WIFI:")) {
            try {
                val wifiParts = content.substring(5).split(";").filter { it.isNotEmpty() }
                for (part in wifiParts) {
                    if (part.startsWith("S:")) {
                        result["ssid"] = part.substring(2)
                    } else if (part.startsWith("P:")) {
                        result["wifi_password"] = part.substring(2)
                    }
                }
                if (result.containsKey("ssid") && result.containsKey("wifi_password")) {
                    return result
                }
            } catch (e: Exception) {
                // WiFi格式解析失败，继续尝试其他格式
            }
        }
        
        // 格式6: 文本格式，包含WiFi信息
        val ssidPattern = Regex("(?:ssid|wifi|网络)[:：]\s*([^\s,]+)")
        val wifiPassPattern = Regex("(?:wifi_pass|wifi_password|wifi密码)[:：]\s*([^\s,]+)")
        val ssidMatch = ssidPattern.find(content)
        val wifiPassMatch = wifiPassPattern.find(content)
        if (ssidMatch != null && wifiPassMatch != null) {
            result["ssid"] = ssidMatch.groupValues[1]
            result["wifi_password"] = wifiPassMatch.groupValues[1]
            return result
        }
        
        return null
    }

    private fun loginRouter(ip: String, username: String, password: String) {
        // 尝试不同的登录路径
        val loginPaths = listOf(
            "/login.cgi",
            "/cgi-bin/login.cgi",
            "/web/login.cgi",
            "/userRpm/LoginRpm.htm",  // TP-Link
            "/login.asp",
            "/index.asp",
            "/login.html",
            "/index.html"
        )

        // 尝试不同的参数名
        val paramCombinations = listOf(
            mapOf("username" to username, "password" to password),
            mapOf("user" to username, "pass" to password),
            mapOf("userName" to username, "passWord" to password),
            mapOf("name" to username, "pwd" to password)
        )

        // 尝试第一个成功的组合
        attemptLogin(ip, loginPaths, paramCombinations, 0, 0)
    }

    private fun attemptLogin(ip: String, paths: List<String>, paramCombinations: List<Map<String, String>>, pathIndex: Int, paramIndex: Int) {
        if (pathIndex >= paths.size) {
            runOnUiThread {
                Toast.makeText(this, "所有登录路径均失败，请检查路由器信息", Toast.LENGTH_SHORT).show()
                binding.statusText.text = "登录状态：未登录"
            }
            return
        }

        if (paramIndex >= paramCombinations.size) {
            // 尝试下一个路径
            attemptLogin(ip, paths, paramCombinations, pathIndex + 1, 0)
            return
        }

        val path = paths[pathIndex]
        val params = paramCombinations[paramIndex]
        val url = "http://$ip$path"

        val formBody = FormBody.Builder()
        params.forEach { (key, value) ->
            formBody.add(key, value)
        }

        val request = Request.Builder()
            .url(url)
            .post(formBody.build())
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                // 尝试下一个参数组合
                attemptLogin(ip, paths, paramCombinations, pathIndex, paramIndex + 1)
            }

            override fun onResponse(call: Call, response: Response) {
                runOnUiThread {
                    if (response.isSuccessful) {
                        Toast.makeText(this@MainActivity, "登录成功！", Toast.LENGTH_SHORT).show()
                        binding.statusText.text = "登录状态：已登录"
                    } else {
                        // 尝试下一个参数组合
                        attemptLogin(ip, paths, paramCombinations, pathIndex, paramIndex + 1)
                    }
                }
            }
        })
    }

    private fun checkConnectionStatus() {
        val connectivityManager = getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val network = connectivityManager.activeNetwork
        val capabilities = connectivityManager.getNetworkCapabilities(network)

        if (capabilities != null) {
            if (capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)) {
                val wifiManager = getSystemService(Context.WIFI_SERVICE) as WifiManager
                val ssid = wifiManager.connectionInfo.ssid
                binding.statusText.text = "网络状态：已连接到 $ssid"
            } else if (capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR)) {
                binding.statusText.text = "网络状态：已连接到移动网络"
            } else {
                binding.statusText.text = "网络状态：已连接"
            }
        } else {
            binding.statusText.text = "网络状态：未连接"
        }
    }
}
