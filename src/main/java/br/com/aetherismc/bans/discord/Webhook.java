package br.com.aetherismc.bans.discord;

import java.io.DataOutputStream;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;

import br.com.aetherismc.bans.Core;

public class Webhook  {
	private URL url;

	public Webhook(String url) {
		try {
			this.url = new URL(url + "/slack");
		} catch (Exception var3) {
			var3.printStackTrace();
		}
	}

	public void sendMessage(String message) {
		try {
			/*message = "``` " +
					  message +
					  " ```";*/
			Message message2 = new Message(Core.pl.getConfig().getString("Settings.BotName"));
			message2.setText(message);
			this.sendMessage(message2);
		} catch (Exception e) {
			System.out.println("[FatalGamerBans] ERROR: " + e.getMessage());
		}
	}

	public void sendMessage(Message message) throws IOException {
		if (!this.doPost(message.toJson().toString())) {
			throw new IOException("Não foi possível enviar a mensagem!");
		}
	}

	private HttpURLConnection doConnection() {
		HttpURLConnection connection = null;

		try {
			connection = (HttpURLConnection)this.url.openConnection();
			connection.setRequestMethod(EHttpMethod.POST.toString());
			connection.setRequestProperty("User-Agent", "Mozilla/5.0 (X11; Linux x86_64; rv:49.0) Gecko/20100101 Firefox/49.0");
			connection.setRequestProperty("Content-type", "application/json; charset=windows-1252");
			connection.setConnectTimeout(5000);
			connection.setDoOutput(true);
			connection.setDoInput(true);
		} catch (IOException var4) {
			connection = null;
		}
		return connection;
	}

	private boolean doPost(String payload) {
		HttpURLConnection connection = this.doConnection();
		if (connection == null) {
			System.out.println("Conexão vazia");
			return false;
		} else {
			connection.setRequestProperty("Content-length", String.valueOf(payload.length()));
			try {
				DataOutputStream outputStream = new DataOutputStream(connection.getOutputStream());
				outputStream.writeBytes(payload);
				outputStream.flush();
				outputStream.close();
			} catch (IOException var6) {
				System.out.println(var6);
				return false;
			}

			try {
				return connection.getResponseCode() == 200;
			} catch (IOException var5) {
				System.out.println(var5);
				return false;
			}
		}
	}

	private static enum EHttpMethod {
		GET,
		POST;
	}
}
