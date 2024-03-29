package main;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths; 
import java.util.Scanner;
import java.net.*;
import java.io.*;
import org.json.JSONObject;



import main.model.Trade;
import main.model.Trade.Stock;
import main.model.Trade.Type;
import main.model.account.Account;
import main.model.account.Personal;
import main.model.account.TFSA;
import main.utils.Color;

public class Main {

    static Account account; 
    static final double INITIAL_DEPOSIT = 10000;
    static Scanner scanner = new Scanner(System.in);
    static String AAPL = getStock("AAPL");
    static String META = getStock("META");
    static String GOOG = getStock("GOOG");
    static String TSLA = getStock("TSLA");

    public static void main(String[] args) {
        explainApp();
        switch (accountChoice()) {
            case "a": account = new Personal(INITIAL_DEPOSIT); break;
            case "b": account = new TFSA(INITIAL_DEPOSIT); break;
            case "A": account = new Personal(INITIAL_DEPOSIT); break;
            case "B": account = new TFSA(INITIAL_DEPOSIT); break;
        }


        initialBalance();
        for (int day = 1; day <= 2160; day++) {

            displayPrices(day);

            String choice = buyOrSell();
            String stock = chooseStock();
            int shares = numShares(choice);
        
            String result = account.makeTrade(new Trade(
                Stock.valueOf(stock.toUpperCase()),
                choice.equals("buy") ? Type.MARKET_BUY : Type.MARKET_SELL,
                Double.parseDouble(getPrice(Stock.valueOf(stock.toUpperCase()), day)),
                shares
            )) ? "successful" : "unsuccessful";

            tradeStatus(result);
        }

        scanner.close();
    }

    public static void explainApp() {
        System.out.print(Color.BLUE + "\n - PERSONAL: ");
        System.out.println(Color.YELLOW + "Every sale made in a personal account is charged a 5% fee.");
        System.out.print(Color.BLUE + "\n - TFSA: ");
        System.out.println(Color.YELLOW + "Every trade (buy/sell) made from a TFSA is charged a 1% fee.\n");
        System.out.println(Color.BLUE + " - Neither account has a limit on the amount of trades that can be made." + Color.RESET);
    }

    public static void initialBalance() {
        System.out.print("\n\n  You created a " + Color.YELLOW + account.getClass().getSimpleName() + Color.RESET + " account.");
        System.out.println(" Your account balance is " + Color.GREEN + "$" + account.getFunds() + Color.RESET);
        System.out.print("\n  Enter anything to start trading: ");
        scanner.nextLine();
    }


    public static String accountChoice() {
        System.out.print("\n  Respectively, type 'a' or 'b' to create a Personal account or TFSA: ");
        String choice = scanner.nextLine();
        while (!choice.equalsIgnoreCase("a") && !choice.equalsIgnoreCase("b")) {
            System.out.print("  Respectively, type 'a' or 'b' to create a Personal account or TFSA: ");
            choice = scanner.nextLine();
        }
        return choice;
    }

    
    public static String getPrice(Stock stock, int day) {
        Path path = getPath(stock.toString());
        final String st;
        if(stock.toString().equals("AAPL")){
            st = Main.AAPL;
        }else if(stock.toString().equals("META")){
            st = Main.META;
        }else if(stock.toString().equals("GOOG")){
            st = Main.GOOG;
        }else if(stock.toString().equals("TSLA")){
            st = Main.TSLA;
        }else{return null;}
        try {
           return Files.lines(path)
            .skip(1)
            .filter((line) -> Integer.valueOf(line.split(",")[0]) == day)
            .map((line) -> {
                Double k = Double.parseDouble(line.split(",")[1]) + Double.parseDouble(st);
                return Double.toString(k);
            })
            .findFirst()
            .orElse(null);
        } catch (IOException e) {
            System.out.println(e.getMessage());
            return null;
        }
    }

    public static Path getPath(String stock) {
        try {
            return Paths.get(Thread.currentThread().getContextClassLoader().getResource("main/data/"+stock+".csv").toURI());
        } catch (Exception e) {
            System.out.println(e.getMessage());
            return null;
        }
    }

    public static String buyOrSell() {
        System.out.print("\n\n  Would you like to 'buy' or 'sell': ");
        String choice = scanner.nextLine().toLowerCase();
        while (!choice.equals("buy") && !choice.equals("sell")) {
            System.out.print("  Would you like to 'buy' or 'sell': ");
            choice = scanner.nextLine();
        }
        return choice;
    }

    public static String chooseStock() {
        System.out.print("  Choose a stock: ");
        String stock = scanner.nextLine().toLowerCase(); 
        while (!stock.equals("aapl") && !stock.equals("meta") && !stock.equals("goog") && !stock.equals("tsla") ) {
            System.out.print("  Choose a stock: ");
            stock = scanner.nextLine();
        }
        return stock;
    }

    public static int numShares(String choice) {
        System.out.print("  Enter the number of shares you'd like to " + choice + ": ");
        int shares = scanner.nextInt(); 
        scanner.nextLine(); 
        while (shares <= 0) {
            System.out.print("  Enter the number of shares you'd like to " + choice + ": ");
            shares = scanner.nextInt();
            scanner.nextLine(); 

        }
        return shares;
    }

    public static void displayPrices(int day) {
        System.out.println("\n\n\t  DAY " + day + " PRICES\n");

        System.out.println("  " + Color.BLUE + "AAPL\t\t" + Color.GREEN + getPrice(Stock.AAPL, day));
        System.out.println("  " + Color.BLUE + "META\t\t" + Color.GREEN + getPrice(Stock.META, day));
        System.out.println("  " + Color.BLUE + "GOOG\t\t" + Color.GREEN + getPrice(Stock.GOOG, day));
        System.out.println("  " + Color.BLUE + "TSLA\t\t" + Color.GREEN + getPrice(Stock.TSLA, day) + Color.RESET);

    }

    public static void tradeStatus(String result) {
        System.out.println("\n  The trade was " + (result.equals("successful") ? Color.GREEN : Color.RED) + result + Color.RESET + ". Here is your portfolio:");
        System.out.println(account);
        System.out.print("\n  Press anything to continue");
        scanner.nextLine();
    }


    public static String getStock(String symbol){
        {
            String apiKey = System.getenv("API_KEY");
            String urlString = "https://www.alphavantage.co/query?function=GLOBAL_QUOTE&symbol=" + symbol + "&apikey=" + apiKey;
            try {
            URL url = new URL(urlString);
            HttpURLConnection con = (HttpURLConnection) url.openConnection();
            
                con.setRequestMethod("GET");
            con.setRequestProperty("User-Agent", "Mozilla/5.0");
    
            BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
            String inputLine;
            StringBuffer response = new StringBuffer();
            while ((inputLine = in.readLine()) != null) {
                response.append(inputLine);
            }
            
            in.close();
    
            // Parse the JSON response
            JSONObject jsonObj = new JSONObject(response.toString());
            JSONObject globalQuote = jsonObj.getJSONObject("Global Quote");
            String price = globalQuote.getString("05. price");
            return price;
        } catch (IOException e) {

            e.printStackTrace();
            return "";
        }
        
        }
    }

}
