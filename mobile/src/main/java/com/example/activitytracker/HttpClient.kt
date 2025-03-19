import okhttp3.Call
import okhttp3.Callback
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Response
import java.io.IOException

// Create a single OkHttpClient instance (you can reuse it for all requests)
val client = OkHttpClient()
public const val serverURL = "http://35.155.119.40"

// Function to send a POST request
fun sendPostRequest(url: String, jsonBody: String) {
    // Define the media type (here JSON)
    val mediaType = "application/json; charset=utf-8".toMediaType()
    // Create the request body with the JSON payload
    val requestBody = jsonBody.toRequestBody(mediaType)

    // Build the POST request
    val request = Request.Builder()
        .url(url)
        .post(requestBody)
        .build()

    // Send the request asynchronously
    client.newCall(request).enqueue(object : Callback {
        override fun onFailure(call: Call, e: IOException) {
            // Handle request failure (for example, show an error message)
            e.printStackTrace()
        }

        override fun onResponse(call: Call, response: Response) {
            // Process the response
            if (response.isSuccessful) {
                val responseData = response.body?.string()
                println("Response: $responseData")
                // You can update your UI here by posting to the main thread if needed.
            } else {
                println("Server returned error: ${response.code}")
            }
            response.close()
        }
    })
}
