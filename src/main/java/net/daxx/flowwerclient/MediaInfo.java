package net.daxx.flowwerclient;

import net.fabricmc.loader.api.FabricLoader;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.TimeUnit;

public class MediaInfo {

    private static final String SCRIPT = """
            param([string]$Action = "poll")
            [Console]::OutputEncoding = [System.Text.Encoding]::UTF8
            Add-Type -AssemblyName System.Runtime.WindowsRuntime
            $asTaskGeneric = ([System.WindowsRuntimeSystemExtensions].GetMethods() | Where-Object { $_.Name -eq 'AsTask' -and $_.GetParameters().Count -eq 1 -and $_.GetParameters()[0].ParameterType.Name -eq 'IAsyncOperation`1' })[0]
            function Await($task, $type) {
                $m = $asTaskGeneric.MakeGenericMethod($type)
                $t = $m.Invoke($null, @($task))
                $t.Wait(-1) | Out-Null
                $t.Result
            }
            [Windows.Media.Control.GlobalSystemMediaTransportControlsSessionManager, Windows.Media.Control, ContentType = WindowsRuntime] | Out-Null
            $mgr = Await ([Windows.Media.Control.GlobalSystemMediaTransportControlsSessionManager]::RequestAsync()) ([Windows.Media.Control.GlobalSystemMediaTransportControlsSessionManager])
            $s = $mgr.GetCurrentSession()
            if ($null -eq $s) { Write-Output "NONE"; exit }
            if ($Action -eq "toggle") { Await ($s.TryTogglePlayPauseAsync()) ([bool]) | Out-Null }
            if ($Action -eq "next") { Await ($s.TrySkipNextAsync()) ([bool]) | Out-Null }
            if ($Action -eq "prev") { Await ($s.TrySkipPreviousAsync()) ([bool]) | Out-Null }
            $props = Await ($s.TryGetMediaPropertiesAsync()) ([Windows.Media.Control.GlobalSystemMediaTransportControlsSessionMediaProperties])
            $pb = $s.GetPlaybackInfo()
            $tl = $s.GetTimelineProperties()
            $title = ([string]$props.Title).Replace('|', ' ')
            $artist = ([string]$props.Artist).Replace('|', ' ')
            Write-Output ("{0}|{1}|{2}|{3}|{4}" -f [int]$pb.PlaybackStatus, $title, $artist, [int]$tl.Position.TotalSeconds, [int]$tl.EndTime.TotalSeconds)
            """;

    public static volatile boolean available = false;
    public static volatile boolean playing = false;
    public static volatile String title = "";
    public static volatile String artist = "";

    private static volatile int position = 0;
    private static volatile int duration = 0;
    private static volatile long stamp = 0;

    private static Path script;
    private static boolean started = false;

    public static void start() {
        if (started) return;
        started = true;

        if (!System.getProperty("os.name").toLowerCase().contains("win")) return;

        script = FabricLoader.getInstance().getConfigDir().resolve("flowwerclient_media.ps1");
        try {
            Files.writeString(script, SCRIPT, StandardCharsets.UTF_8);
        } catch (IOException e) {
            return;
        }

        Thread thread = new Thread(() -> {
            while (true) {
                if (Modules.MUSIC.enabled) run("poll");
                try {
                    Thread.sleep(2000);
                } catch (InterruptedException e) {
                    return;
                }
            }
        }, "Media-Poll");
        thread.setDaemon(true);
        thread.start();
    }

    public static void action(String action) {
        if (script == null) return;
        Thread thread = new Thread(() -> run(action), "Media-Action");
        thread.setDaemon(true);
        thread.start();
    }

    public static int currentSeconds() {
        double now = position;
        if (playing) now += (System.currentTimeMillis() - stamp) / 1000.0;
        if (duration > 0 && now > duration) now = duration;
        return (int) now;
    }

    public static int duration() {
        return duration;
    }

    private static void run(String action) {
        try {
            ProcessBuilder builder = new ProcessBuilder(
                    "powershell.exe", "-NoProfile", "-ExecutionPolicy", "Bypass",
                    "-File", script.toString(), action);
            builder.redirectError(ProcessBuilder.Redirect.DISCARD);
            Process process = builder.start();

            String line;
            try (BufferedReader reader = new BufferedReader(
                    new InputStreamReader(process.getInputStream(), StandardCharsets.UTF_8))) {
                line = reader.readLine();
            }
            process.waitFor(5, TimeUnit.SECONDS);
            process.destroy();

            parse(line);
        } catch (Exception e) {
            available = false;
        }
    }

    private static void parse(String line) {
        if (line == null) {
            available = false;
            return;
        }
        if (line.startsWith("\uFEFF")) line = line.substring(1);
        if (line.startsWith("NONE")) {
            available = false;
            return;
        }

        String[] parts = line.split("\\|", -1);
        if (parts.length < 5) {
            available = false;
            return;
        }

        try {
            playing = parts[0].trim().equals("4");
            title = parts[1];
            artist = parts[2];
            position = Integer.parseInt(parts[3].trim());
            duration = Integer.parseInt(parts[4].trim());
            stamp = System.currentTimeMillis();
            available = true;
        } catch (NumberFormatException e) {
            available = false;
        }
    }
}