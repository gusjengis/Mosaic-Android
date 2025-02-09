import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.IOException

// Create a single OkHttpClient instance (you can reuse it for all requests)
val client = OkHttpClient()

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
