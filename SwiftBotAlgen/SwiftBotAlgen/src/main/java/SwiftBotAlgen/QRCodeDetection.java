package SwiftBotAlgen;

import swiftbot.*;
import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Random;
import java.util.Scanner;
import java.util.concurrent.atomic.AtomicBoolean;

public class QRCodeDetection {
    static SwiftBotAPI swiftBot;
    static final int BUFFER_ZONE = 30;
    static final int SCAREDY_DISTANCE = 50;
    static String mode;
    static AtomicBoolean detecting = new AtomicBoolean(true);

    public static void main(String[] args) throws InterruptedException {
        try {
            swiftBot = new SwiftBotAPI();
        } catch (Exception e) {
            System.out.println("\nERROR: SwiftBot API initialization failed!");
            System.exit(1);
        }

        Scanner scanner = new Scanner(System.in);
        boolean running = true;

        while (running) {
            System.out.println("\n********************************************************");
            System.out.println("******************** QR CODE DETECTION *****************");
            System.out.println("********************************************************\n");
            System.out.println("1 = Scan QR Code for Mode Selection");
            System.out.println("2 = Start Object Detection");
            System.out.println("3 = View Execution Log");
            System.out.println("0 = Exit");
            System.out.print("\nEnter a number: ");

            String choice = scanner.next();

            switch (choice) {
                case "1":
                    scanQRCode();
                    break;
                case "2":
                    if (mode == null || mode.isEmpty()) {
                        System.out.println("\nERROR: No mode selected. Scan a QR code first!\n");
                    } else {
                        startObjectDetection();
                    }
                    break;
                case "3":
                    viewExecutionLog();
                    break;
                case "0":
                    System.out.println("\nExiting...");
                    running = false;
                    break;
                default:
                    System.out.println("\nERROR: Please enter a valid option.");
            }
        }
        scanner.close();
    }

    public static void scanQRCode() {
        System.out.println("\nScanning QR Code... Please show a QR code to the camera.");
        long startTime = System.currentTimeMillis();
        long timeout = 10_000;

        while (System.currentTimeMillis() - startTime < timeout) {
            try {
                BufferedImage img = swiftBot.getQRImage();
                String decodedMessage = swiftBot.decodeQRImage(img);

                if (!decodedMessage.isEmpty()) {
                    mode = decodedMessage.trim();
                    if (mode.equals("Curious SwiftBot") || mode.equals("Scaredy SwiftBot") || mode.equals("Dubious SwiftBot")) {
                        System.out.println("\nSUCCESS: Mode set to " + mode + ".");
                        return;
                    } else {
                        System.out.println("\nERROR: Invalid QR Code. Please scan again.");
                        mode = null;
                        return;
                    }
                }

                System.out.println("No QR code detected. Trying again...");
                Thread.sleep(1000);

            } catch (Exception e) {
                System.out.println("\nERROR: Unable to scan QR Code.");
            }
        }

        System.out.println("\nERROR: No QR code detected after 10 seconds. Please try again.");
    }

    public static void startObjectDetection() throws InterruptedException {
        System.out.println("\nStarting Object Detection in " + mode + " mode...");
        swiftBot.fillUnderlights(new int[]{0, 0, 255});
        detecting.set(true);

        swiftBot.disableButton(Button.X);
        swiftBot.enableButton(Button.X, () -> {
            System.out.println("\nButton X pressed. Stopping detection...");
            swiftBot.move(0, 0, 10);
            saveExecutionLog("\nDetection stopped by user.");
            detecting.set(false);
        });

        while (detecting.get()) {
            double distance = swiftBot.useUltrasound();

            if (distance > 0 && distance < 100) {
                handleObjectDetection(distance);
                saveObjectImage();
                saveExecutionLog("Object detected at " + distance + " cm.");
            } else {
                Thread.sleep(5000);
                swiftBot.move(30, 30, 1000);
            }
        }

        System.out.println("\nDetection stopped. Returning to CLI...");
    }

    public static void handleObjectDetection(double distance) throws InterruptedException {
        if (mode.equals("Dubious SwiftBot")) {
            mode = (new Random().nextBoolean()) ? "Curious SwiftBot" : "Scaredy SwiftBot";
            System.out.println("\nDubious SwiftBot randomly chose: " + mode);
        }

        if (mode.equals("Curious SwiftBot")) {
            swiftBot.fillUnderlights(new int[]{0, 255, 0});
            if (distance > BUFFER_ZONE) {
                System.out.println("Moving towards object...");
                swiftBot.move(40, 40, 1000);
            } else if (distance < BUFFER_ZONE) {
                System.out.println("Moving away to maintain gap...");
                swiftBot.move(-40, -40, 1000);
            } else {
                System.out.println("Maintaining distance...");
                for (int i = 0; i < 3; i++) {
                    swiftBot.fillUnderlights(new int[]{0, 255, 0});
                    Thread.sleep(500);
                    swiftBot.fillUnderlights(new int[]{0, 0, 0});
                    Thread.sleep(500);
                }
            }
        } else if (mode.equals("Scaredy SwiftBot")) {
            swiftBot.fillUnderlights(new int[]{255, 0, 0});
            if (distance < SCAREDY_DISTANCE) {
                System.out.println("Backing away from object...");
                swiftBot.move(-50, -50, 3000);
            }
        }
    }

    public static void saveObjectImage() {
        try {
            BufferedImage image = swiftBot.takeStill(ImageSize.SQUARE_720x720);
            if (image != null) {
                File outputfile = new File("/data/home/pi/object_detected.png");
                ImageIO.write(image, "png", outputfile);
                System.out.println("Image saved: " + outputfile.getAbsolutePath());
            }
        } catch (Exception e) {
            System.out.println("ERROR: Unable to save image.");
        }
    }

    public static void saveExecutionLog(String message) {
        try {
            FileWriter writer = new FileWriter("/data/home/pi/swiftbot_log.txt", true);
            writer.write(message + "\n");
            writer.close();
        } catch (IOException e) {
            System.out.println("ERROR: Unable to save execution log.");
        }
    }

    public static void viewExecutionLog() {
        System.out.println("\nRetrieving Execution Log...");
        try {
            File logFile = new File("/data/home/pi/swiftbot_log.txt");
            Scanner logScanner = new Scanner(logFile);
            while (logScanner.hasNextLine()) {
                System.out.println(logScanner.nextLine());
            }
            logScanner.close();
        } catch (Exception e) {
            System.out.println("ERROR: No log file found.");
        }
    }
}
