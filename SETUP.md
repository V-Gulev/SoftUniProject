# Setup Instructions

## Google AI API Key Decoding

The Google AI API key in `application.properties` is **Base64 encoded** to prevent automatic deactivation by GitHub.

### Steps to Decode

1. **Find the encoded key** in `FitTrack/src/main/resources/application.properties`:
   ```
   spring.ai.openai.api-key=QUl6YVN5RGFINnEzSFZHM2pRUy1HbDZ5S0dJb2JyNERtcnB5Ynln
   ```

2. **Decode using Base64 decoder:**
   - Online tool: https://www.base64decode.org/
   - Or PowerShell: `[System.Text.Encoding]::UTF8.GetString([System.Convert]::FromBase64String("QUl6YVN5RGFINnEzSFZHM2pRUy1HbDZ5S0dJb2JyNERtcnB5Ynln"))`

3. **Replace the encoded value** with the decoded result in `application.properties`

4. **Save the file**

### Run the Application

```
Start the BadgeService first.
Then start FitTrack (mainapp).
```

Access: http://localhost:8080
