package SheetsAPI

import java.io.InputStreamReader
import java.util.Collections

import com.google.api.client.auth.oauth2.Credential
import com.google.api.client.json.jackson2.JacksonFactory
import com.google.api.services.sheets.v4.SheetsScopes
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport
import com.google.api.client.http.javanet.NetHttpTransport
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets
import com.google.api.client.util.store.FileDataStoreFactory
import com.google.api.services.sheets.v4.Sheets
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow
object SheetsQuickstart {
  val APPLICATION_NAME = "Google Sheets API Java Quickstart"
  val JSON_FACTORY = JacksonFactory.getDefaultInstance()
  val TOKENS_DIRECTORY_PATH = "tokens"
  val SCOPES = Collections.singletonList(SheetsScopes.SPREADSHEETS_READONLY)
  val CREDENTIALS_FILE_PATH = "/credentials.json"

  def getCredentials(HTTP_TRANSPORT: NetHttpTransport): Credential = {

    val in =
      classOf[SheetsQuickstart.type].getResourceAsStream(CREDENTIALS_FILE_PATH)
    import java.io.FileNotFoundException
    if (in == null)
      throw new FileNotFoundException(
        "Resource not found: " + CREDENTIALS_FILE_PATH
      )

    val clientSecrets =
      GoogleClientSecrets.load(JSON_FACTORY, new InputStreamReader(in))
    val flow = new GoogleAuthorizationCodeFlow.Builder(
      HTTP_TRANSPORT,
      JSON_FACTORY,
      clientSecrets,
      SCOPES
    ).setDataStoreFactory(
        new FileDataStoreFactory(new java.io.File(TOKENS_DIRECTORY_PATH))
      )
      .setAccessType("offline")
      .build()
    import com.google.api.client.extensions.jetty.auth.oauth2.LocalServerReceiver
    val receiver = new LocalServerReceiver.Builder().setPort(8888).build
    import com.google.api.client.extensions.java6.auth.oauth2.AuthorizationCodeInstalledApp
    return new AuthorizationCodeInstalledApp(flow, receiver).authorize("user")
  }

  def main(args: Array[String]): Unit = {

    val HTTP_TRANSPORT = GoogleNetHttpTransport.newTrustedTransport
    val spreadsheetId = "1BxiMVs0XRA5nFMdKvBdBZjgmUUqptlbs74OgvE2upms"
    val range = "Class Data!A2:E"

    val service = new Sheets.Builder(
      HTTP_TRANSPORT,
      JSON_FACTORY,
      getCredentials(HTTP_TRANSPORT)
    ).setApplicationName(APPLICATION_NAME).build()
    val response = service.spreadsheets.values.get(spreadsheetId, range).execute
    val values = response.getValues
    if (values == null || values.isEmpty()) {
      System.out.println("No data found.")
    } else {
      System.out.println("Name, Major")
      Seq(values).map(
        value => System.out.printf("%s, %s\n", value.get(0), value.get(4))
      )
    }
  }
}
