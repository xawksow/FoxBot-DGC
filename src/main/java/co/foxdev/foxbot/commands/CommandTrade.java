package co.foxdev.foxbot.commands;

import co.foxdev.foxbot.FoxBot;
import co.foxdev.foxbot.utils.Utils;
import org.pircbotx.Channel;
import org.pircbotx.User;
import org.pircbotx.hooks.events.MessageEvent;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by xawksow on 24.07.14.
 */
public class CommandTrade extends Command {
    private final FoxBot foxbot;
    private String address = "https://www.cryptsy.com";
    private String currency = "DGC";
    private float amount = 1000f;

    /**
     * Calculates the current market value of given coin.
     * <p/>
     * Usage: .trade 10 dgc
     */
    public CommandTrade(FoxBot foxbot) {
        super("trade", "command.trade");
        this.foxbot = foxbot;
    }

    @Override
    public void execute(MessageEvent event, String[] args) {
        User user = event.getUser();
        String info = "";
        Channel channel = event.getChannel();
        currency = "DGC/BTC";
        amount = 1000f;
        if (args.length > 0) {
            try {
                if (args[0].length() > 0)
                    amount = Float.parseFloat(args[0]);

            if (args.length > 1)
                if (args[1].length() > 0 && args[1].length() < 9) {
                    currency = args[1];
                }
            } catch (Exception e) {
                info = "Usage: !trade 1000 DGC/BTC";
            }
        } else
            info = "Usage: !trade 1000 DGC/BTC";

        try {
            if (info.equals(""))
                info = getCryptsyInfo();

            //networkHashObject = new JSONObject(conn3.get().text());
        } catch (Exception e) {
            foxbot.getLogger().error("Error occurred while performing Google search", e);
            channel.send().message(Utils.colourise(String.format("(%s) &cSomething went wrong...", user.getNick())));
            return;
        }

        channel.send().message(info);


    }

    public String getCryptsyInfo() {
        StringBuffer response = new StringBuffer();
        String resp = "";
        try {

            URL obj = new URL(address);
            HttpURLConnection con = (HttpURLConnection) obj.openConnection();

            // optional default is GET
            con.setRequestMethod("GET");

            //add request header
            con.setRequestProperty("User-Agent", "DGC Bot");

            int responseCode = con.getResponseCode();
            System.out.println("\nSending 'GET' request to URL : " + address);
            System.out.println("Response Code : " + responseCode);

            BufferedReader in = new BufferedReader(
                    new InputStreamReader(con.getInputStream()));
            String inputLine;


            while ((inputLine = in.readLine()) != null) {
                response.append(inputLine);
            }
            in.close();
        } catch (Exception e) {
            //channel.send().message(Utils.colourise(String.format("(%s) &cSomething went wrong...", user.getNick())));
        }
        String responseHTML = response.toString();
        String cur1 = "";
        String cur2 = "";
        String[] curs = currency.toUpperCase().split("/");
        cur1 = curs[0];
        cur2 = curs[1];

        String usdprice = "";
        if (!cur1.equals("BTC")) {
            Pattern btcUsd = Pattern.compile(cur2 + "/USD .*?>([0-9]*?\\.[0-9]*?)</span>");
            Matcher m = btcUsd.matcher(responseHTML);

            if (m.find()) {
                usdprice = m.group(1);
            }
        }

        Pattern curBtc = Pattern.compile(currency.toUpperCase() + " .*?>([0-9]*?\\.[0-9]*?)</span>");
        Matcher m2 = curBtc.matcher(responseHTML);
        String btcprice = "";
        if (m2.find())
            btcprice = m2.group(1);

        resp = amount + " " + cur1 + " equals " + String.format(Locale.US, "%.8f " + cur2, Float.parseFloat(btcprice) * amount);
        if (!cur1.equals("BTC"))
            resp += " or " + String.format(Locale.US, "%.2f$", (Float.parseFloat(btcprice) * Float.parseFloat(usdprice)) * amount);
        return resp;

    }


}
