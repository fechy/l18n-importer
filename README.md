# L18N importer
Simple script to download a GDoc spreadsheet and processing resulting in localization.properties.

## Google Drive API Preparation
To obtain the client secret file necessary to connect to GDoc, you need to follow the step 1 in this tutorial:
https://developers.google.com/drive/v3/web/quickstart/java

## The GDoc
The script relies on a format, check: `resources/Sample L18n file.csv`

## The config
You need now to configure your script. 
Find config.json on the resources folder.
```json
{
  "application_name": "test",
  "file_id": "XXXXXXXXXXXXXXXXXXXX",
  "languages": [
    "en", "es", "pt"
  ],
  "default_lang": "en",
  "destination_folder": "resources"
}
```

Change the information accordingly.
Notice `file_id`. You need to get the file id from your GDoc url.

## Run from Source
Simply run:
```bash
./build.sh
```

## Run from jar
```bash
./run.sh --secret=client_secret.json --config=config.json
```