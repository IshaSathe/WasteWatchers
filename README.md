# Welcome to Waste Watchers !
## Steps to Run Locally:
- Install Android Studio
- Request API keys from Hugging Face and Open AI
    - OR email (igsathe@asu.edu) for temporary keys
    - Then fill in the 4 sections that say "YOUR-TOKEN-HERE" (hugging face) and "YOUR-API-KEY-HERE" (open ai)
- Download the test images from the "test_images" folder
    - In Android Studio, go to View > Tool Windows > Device Explorer
    - Locate sdcard/DCIM and add the images
    - Run these commands if the images do not appear in the app to refresh
    - `& "C:\Users\username\AppData\Local\Android\Sdk\platform-tools\adb.exe" shell am broadcast -a android.intent.action.MEDIA_SCANNER_SCAN_FILE -d file:///sdcard/DCIM/waterbottle.jpg`
    - `& "C:\Users\username\AppData\Local\Android\Sdk\platform-tools\adb.exe" shell am broadcast -a android.intent.action.MEDIA_SCANNER_SCAN_FILE -d file:///sdcard/DCIM/jeans.jpg`
- Run the app on a an emulated device or connect your own
    - Must use a device with Android API 31+
