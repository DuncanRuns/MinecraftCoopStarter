package me.duncanruns.mccoopstarter;

import com.formdev.flatlaf.FlatDarkLaf;

import javax.swing.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.regex.MatchResult;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static me.duncanruns.mccoopstarter.FileUtil.readString;

public class Main {
    private static Process ngrokProcess = null;
    private static NextAction nextAction;

    public static void main(String[] args) throws Exception {
        Path ngrokExePath = Paths.get("ngrok.exe");
        if (Arrays.asList(args).contains("--redownload") && Files.exists(ngrokExePath)) {
            System.out.println("Redownload requested, deleting ngrok.exe");
            tryDelete(ngrokExePath);
        }
        FlatDarkLaf.setup();
        boolean ngrokExists = Files.exists(ngrokExePath);
        if (!ngrokExists) {
            System.out.println("Ngrok not found, downloading...");
            DownloadingNgrokScreen screen = new DownloadingNgrokScreen();
            screen.download();
            screen.dispose();
            System.out.println("Ngrok downloaded");
        }
        Path ngrokConfig = Paths.get(System.getProperty("user.home")).resolve("AppData").resolve("Local").resolve("ngrok").resolve("ngrok.yml");
        boolean hasToken = Files.exists(ngrokConfig) && readString(ngrokConfig).contains("authtoken: ");
        System.out.println(hasToken ? "Auth token found" : "User has no auth token set");
        nextAction = hasToken ? NextAction.TRY_LAUNCH : NextAction.INPUT_AUTH;
        while (true) {
            switch (nextAction) {
                case TRY_LAUNCH:
                    tryLaunch();
                    break;
                case FAIL_ASK:
                    failAsk();
                    break;
                case INPUT_AUTH:
                    inputAuth();
                    break;
                case EXIT:
                    exit();
            }
        }
    }

    private static void tryDelete(Path path) {
        try {
            Files.delete(path);
        } catch (Exception ignored) {
            System.out.println("Failed to delete file " + path);
        }
    }

    private static void tryLaunch() throws Exception {
        Path ngrokOutPath = Paths.get("ngrokout.txt");
        tryDelete(ngrokOutPath);
        ngrokProcess = Runtime.getRuntime().exec("./ngrok.exe tcp 25565 --log ngrokout.txt --log-format logfmt");
        NgrokRunningScreen screen = new NgrokRunningScreen();
        Pattern urlPattern = Pattern.compile("url=tcp:.+:\\d+");
        boolean ipFound = false;
        while (ngrokProcess.isAlive() && !screen.isClosed()) {
            if (!ipFound && Files.exists(ngrokOutPath)) {
                String ngrokOut = FileUtil.readString(ngrokOutPath);
                Matcher matcher = urlPattern.matcher(ngrokOut);
                if (!matcher.find()) continue;
                MatchResult matchResult = matcher.toMatchResult();
                String ip = ngrokOut.substring(matchResult.start(), matchResult.end()).replace("url=tcp://", "");
                System.out.println("Ip found: " + ip);
                new Thread(() -> screen.notifyIp(ip)).start();
                ipFound = true;
            }
            Thread.sleep(50);
        }
        if (!ngrokProcess.isAlive()) {
            nextAction = NextAction.FAIL_ASK;
        } else {
            nextAction = NextAction.EXIT;
        }
        screen.dispose();
    }

    private static void failAsk() {
        int ans = JOptionPane.showOptionDialog(null, "Failed to launch! Ngrok might already be running, be sure to close any other coop starters and end ngrok.exe in task manager.", "Coop Starter: Failed to start", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE, null, new String[]{"Retry Launching", "Input New Auth Token", "Cancel"}, null);
        switch (ans) {
            case 0:
                nextAction = NextAction.TRY_LAUNCH;
                break;
            case 1:
                nextAction = NextAction.INPUT_AUTH;
                break;
            default:
                nextAction = NextAction.EXIT;
        }
    }

    private static void inputAuth() throws Exception {
        String out = JOptionPane.showInputDialog(null, "You need to input an ngrok auth token! Please sign up for ngrok and find your token. (You can enter just the token or the entire token command)", "Coop Starter: Input Auth Token", JOptionPane.QUESTION_MESSAGE);
        if (out == null) {
            nextAction = NextAction.EXIT;
            return;
        }
        if (out.isEmpty()) {
            return;
        }

        String[] args = out.split(" ");
        if (args.length == 0) {
            return;
        }
        String lastWord = args[args.length - 1];
        Runtime.getRuntime().exec("./ngrok.exe config add-authtoken " + lastWord.trim()).waitFor();
        nextAction = NextAction.TRY_LAUNCH;
    }

    private static void exit() {
        if (ngrokProcess != null && ngrokProcess.isAlive()) {
            ngrokProcess.destroy();
        }
        tryDelete(Paths.get("ngrokout.txt"));
        System.exit(0);
    }

    enum NextAction {
        TRY_LAUNCH,
        FAIL_ASK,
        INPUT_AUTH,
        EXIT
    }
}