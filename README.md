# NSW Address Lookup Lambda

A simple AWS Lambda function that resolves **district, suburb, and coordinates** for a given NSW address using the [NSW Spatial Services API](https://portal.spatial.nsw.gov.au/).

The project includes a Lambda handler (`AddressHandler`) that can be triggered via **AWS Lambda Function URL**.

---

## Features
- Query NSW Spatial Services Address API for coordinates.
- Resolve **district**, **suburb** and **boundaries** from geometry.
- Return a clean JSON payload:
  ```json
  {
    "suburb": "BATHHURST",
    "coordinates": [151.2093, -33.8688],
    "district": "BATHURST"
  }

## Testing
### 1. Deployed Lambda
You can directly test the deployed Lambda using its **Function URL**:

```bash
curl "https://al37setg3p6tsobidtfoweygee0uwcge.lambda-url.ap-southeast-2.on.aws/?address=346 PANORAMA AVENUE BATHURST"

### 2. Local Unit Tests
Unit 5 test cases have been written to validate the Lambda handler and NSW API client logic.
These tests check:

✅ Valid address lookup returns suburb, district, and coordinates

❌ Missing/empty address query parameter returns 400

❌ Invalid address returns 404

  
