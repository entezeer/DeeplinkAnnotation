Deeplink Processor Library
This library allows you to automatically generate deep link handlers for your Android application using annotations and Kotlin Symbol Processing (KSP). The deep link handler will map URLs to corresponding activities and extract query parameters from the URLs dynamically.

Features
Annotation-based deep link handling: Define deep links in your activities using @DeeplinkActivity.
Dynamic parameter extraction: Automatically extract parameters from the deep link URL and pass them to the activity.
Static and dynamic URL support: The library supports both static and dynamic parts of URLs.
KSP-based code generation: The handler is automatically generated during compilation.
Getting Started
1. Add Dependencies
First, add the required dependencies in your project's build.gradle.kts:

  ```
  plugins {
    id("com.google.devtools.ksp") version "1.6.10-1.0.2"
    kotlin("jvm") version "1.8.0"
  }  

  dependencies {
    implementation("com.github.entezeer:DeeplinkAnnotation:1.0.7")
    ksp("com.github.entezeer:DeeplinkAnnotation:1.0.7")
  }
  ```

2. Define Your Annotations
Annotate your activities with @DeeplinkActivity and specify the URL pattern:

  ```
  package com.example.app

  import com.yourdomain.annotations.DeeplinkActivity
  
  @DeeplinkActivity("https://o.kg/l/a?t=wl_atmtrn&type={processFlowType}&flow={processFlow}")
  class MainActivity : AppCompatActivity() {
      // Your activity code here
  }
  
  @DeeplinkActivity("https://deeplink.com/link&type=ghost")
  class SecondActivity : AppCompatActivity() {
      // Your activity code here
  }
  ```

3. Implement the Generated Handler
In your application, use the generated DeeplinkHandler to handle incoming deep links:

  ```
  package com.example.app
  
  import android.content.Intent
  import android.os.Bundle
  import androidx.appcompat.app.AppCompatActivity
  import com.yourdomain.generated.DeeplinkHandler
  
  class MainActivity : AppCompatActivity() {
  
      override fun onCreate(savedInstanceState: Bundle?) {
          super.onCreate(savedInstanceState)
  
          // Check if the intent contains a deep link and handle it
          val deepLink = intent?.data?.toString()
          if (deepLink != null) {
              val handled = DeeplinkHandler.navigateToActivity(this, deepLink)
              if (!handled) {
                  // Handle case when deep link is not recognized
                  // For example, show an error or open a default screen
              }
          }
      }
  }
  ```
4. Customize and Extend
You can define as many activities and deep link patterns as you need. The library will automatically generate the appropriate handler based on your annotations.

Example
Given the following annotations:

  ```
  @DeeplinkActivity("https://deeplink.com/link&type={type}")
  class MainActivity : AppCompatActivity() {
      // ...
  }
  
  @DeeplinkActivity("https://deeplink.com/link&type1=ghost")
  class SecondActivity : AppCompatActivity() {
      // ...
  }
```
The library will generate a DeeplinkHandler class with a navigateToActivity method that matches deep links and starts the appropriate activity.

  ```
  public fun navigateToActivity(context: Context, deepLink: String): Boolean = when {
      deepLink.matches("""https:\/\/deeplink\.com\/link&type=([^&]+)""".toRegex()) -> {
          val params = extractParameters(deepLink, "https://o.kg/l/a?t=wl_atmtrn&type={processFlowType}&flow={processFlow}")
          val intent = Intent(context, MainActivity::class.java)
          intent.putExtra("processFlowType", params["type"] ?: "")
          context.startActivity(intent)
          true
      }
      deepLink == "https://deeplink.com/link&type1=ghost" -> {
          val intent = Intent(context, SecondActivity::class.java)
          context.startActivity(intent)
          true
      }
      else -> false
  }
  ```
How It Works
The library uses KSP (Kotlin Symbol Processing) to analyze your code during compilation and automatically generate the DeeplinkHandler class. This handler will map URLs to activities based on the annotations you have defined.

Limitations
Ensure that your URL patterns are unique within the application to avoid conflicts.
Dynamic parameters should be defined using {} in the URL pattern.
